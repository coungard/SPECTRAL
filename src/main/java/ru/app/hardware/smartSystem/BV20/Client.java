package ru.app.hardware.smartSystem.BV20;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.protocol.cctalk.CCTalkCommand;
import ru.app.protocol.cctalk.Command;
import ru.app.util.BNVEncode;
import ru.app.util.Crc16;
import ru.app.util.LogCreator;
import ru.app.util.StreamType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;


/**
 * Клиент для работы с купюроприемником BV20 (Itl продукт) на протоколе CCTALK с режимом шифрования
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static final long LOG_ACTIVITY_TIMEOUT = 10000;
    private SerialPort serialPort;
    private byte[] input;
    private byte[] output;
    private long activityInput;
    private long activityOutput;
    private Command transmitCommand;

    /**
     * Базовый конструктор клиента, в котором инициализируется и настраивается порт, передающийся в аргументе
     *
     * @param port ком-порт устройства
     */
    public Client(String port) {
        try {
            serialPort = new SerialPort(port);
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.addEventListener(new PortReader());
        } catch (SerialPortException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()), ex);
        }
    }

    /**
     * Метод отправки команд в порт устройства. Так как используется режим шифрования, отправляются 2 массива байтов:
     * обычный и зашифрованный (encrypted). По таймеру мы ждем эвент и последующую его расшифровку.
     *
     * @param command CCTALK команда
     * @return ответ купюроприемника в расшифрованном виде (decrypt_input)
     */
    public synchronized byte[] sendMessage(Command command) {
        transmitCommand = command;
        input = null;
        byte[] output = formPacket(command.getCommandType().getCode(), command.getData());
        try {
            serialPort.writeBytes(output);
            byte[] temp = Arrays.copyOf(output, output.length);
            byte[] encrypt = encryptPacket(output);
            if (accessLog(output, StreamType.OUTPUT))
                LOGGER.info(LogCreator.logOutput(temp, encrypt));
            serialPort.writeBytes(encrypt);

            long start = Calendar.getInstance().getTimeInMillis();
            do {
                Thread.sleep(10);
            } while (Calendar.getInstance().getTimeInMillis() - start < 1200 && input == null);
        } catch (SerialPortException | InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
        return input;
    }

    /**
     * Построение скелета сообщения по протоколу режима шифрования.<br>
     * Порядок команды : [адрес устройства], [длина данных команды], [младший crc байт], [команда], [дата], [старший crc байт].
     *
     * @param command команда CCTALK
     * @param data данные команды
     * @return построенное сообщение
     */
    private byte[] formPacket(int command, byte[] data) {
        if (data == null) data = new byte[]{};
        Crc16 crc16 = calcCrc16(command, data);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        raw.write((byte) 0x28); // destination address
        raw.write((byte) data.length);
        raw.write((byte) (crc16.getCRC() & 0xff));// LSB
        raw.write((byte) command);
        try {
            raw.write(data);
        } catch (IOException ignored) {
        }
        raw.write((byte) ((crc16.getCRC() >> 8) & 0xff)); // MSB

        return raw.toByteArray();
    }

    private Crc16 calcCrc16(int command, byte[] data) {
        Crc16 crc16 = new Crc16();
        crc16.update((byte) 0x28); // destination address
        crc16.update((byte) data.length); // data length
        crc16.update((byte) command); // commands
        crc16.update(data); // data

        return crc16;
    }

    private byte[] encryptPacket(byte[] packet) {
        byte[] toEncrypt = Arrays.copyOfRange(packet, 2, packet.length);
        BNVEncode.BNV_encrypt(toEncrypt);
        System.arraycopy(toEncrypt, 0, packet, 2, toEncrypt.length);

        return packet;
    }

    private byte[] decryptPacket(byte[] packet) {
        byte[] toDecrypt = Arrays.copyOfRange(packet, 2, packet.length);
        BNVEncode.BNV_decrypt(toDecrypt);
        System.arraycopy(toDecrypt, 0, packet, 2, toDecrypt.length);

        return packet;
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR) {
                try {
                    Thread.sleep(40);
                    byte[] received = serialPort.readBytes();
                    if (received.length >= 5) {
                        byte[] temp = Arrays.copyOf(received, received.length);
                        byte[] decrypt = decryptPacket(received);
                        if (accessLog(received, StreamType.INPUT))
                            LOGGER.debug(LogCreator.logInput(temp, decrypt));
                        input = decrypt;
                    } else {
                        LOGGER.debug("Input : " + LogCreator.console(Arrays.toString(input)));
                    }
                } catch (SerialPortException | InterruptedException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                }
            }
        }
    }

    private void parse(byte[] decrypt) {

    }

    private boolean accessLog(byte[] buffer, StreamType type) {
        if (transmitCommand.getCommandType() != CCTalkCommand.ReadBufferedBillEvents) {
            return true;
        }
        long timestamp = System.currentTimeMillis();
        switch (type) {
            case INPUT:
                if (timestamp - activityInput > LOG_ACTIVITY_TIMEOUT) {
                    activityInput = timestamp;
                    return true;
                }
                if (!Arrays.equals(buffer, input)) {
                    input = buffer;
                    return true;
                }
                break;
            case OUTPUT:
                if (timestamp - activityOutput > LOG_ACTIVITY_TIMEOUT) {
                    activityOutput = timestamp;
                    return true;
                }
                if (!Arrays.equals(buffer, output)) {
                    output = buffer;
                    return true;
                }
                break;
        }
        return false;
    }

    public void close() {
        try {
            if (serialPort.isOpened())
                serialPort.closePort();
        } catch (SerialPortException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()), ex);
        }
    }
}
