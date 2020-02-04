package ru.app.hardware.smartSystem.hopper;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.protocol.cctalk.Command;
import ru.app.util.LogCreator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Общий клиент для Smart Hopper-a, работающий на протоколе CC2 (расширенный cctalk).
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private SerialPort serialPort;
    private final byte DESTINATION_ADDRESS = 0x07;
    private final byte SOURCE_ADDRESS = 0x01;
    private byte[] received;
    private byte[] transmit;

    public Client(String portName) throws SerialPortException {
        serialPort = new SerialPort(portName);
        serialPort.openPort();

        serialPort.setParams(SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_2,
                SerialPort.PARITY_NONE);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPort.addEventListener(new PortReader());

        LOGGER.info(LogCreator.console("Initialization port " + portName + " was succesfull!"));
    }

    public synchronized byte[] sendMessage(Command command) {
        LOGGER.info(LogCreator.console(command.toString()));
        transmit = formPacket(command.getCommandType().getCode(), command.getData());
        try {
            LOGGER.info(LogCreator.logOutput(transmit));
            serialPort.writeBytes(transmit);
            long start = Calendar.getInstance().getTimeInMillis();
            do {
                Thread.sleep(10);
            } while (Calendar.getInstance().getTimeInMillis() - start < 1200 && received == null);
        } catch (SerialPortException | InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
        return received;
    }

    private byte[] formPacket(int command, byte[] data) {
        if (data == null) data = new byte[]{};
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        raw.write(DESTINATION_ADDRESS);
        raw.write((byte) data.length);
        raw.write(SOURCE_ADDRESS);
        raw.write((byte) command);
        try {
            raw.write(data);
        } catch (IOException ignored) {
        }
        raw.write(checksum(raw.toByteArray()));
        return raw.toByteArray();
    }

    /**
     * Высчитать контрольную сумму у части массива байт (modulo 256)
     *
     * @param data - данные
     */
    private byte checksum(byte[] data) {
        int sum = 0;
        for (byte b : data) sum += 0xff & b;

        while (sum > 256)
            sum = sum - 256;

        return (byte) (256 - sum);
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR) {
                try {
                    received = serialPort.readBytes();

                    boolean reply = true;
                    ByteArrayOutputStream msg = new ByteArrayOutputStream();
                    for (int i = 0; i < received.length; i++) {
                        if (i < transmit.length) {
                            if (transmit[i] != received[i])
                                reply = false;
                        } else {
                            msg.write(received[i]);
                        }
                    }
                    if (!reply)
                        LOGGER.error(LogCreator.console("No reply from hopper!"));
                    LOGGER.debug(LogCreator.logInput(msg.toByteArray()));
                } catch (SerialPortException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                }
            }
        }
    }

    public void close() {
        try {
            if (serialPort.isOpened())
                serialPort.closePort();
        } catch (SerialPortException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
    }
}
