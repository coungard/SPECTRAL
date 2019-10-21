package ru.app.hardware.emulator.coinCCTALK;

import jssc.*;
import org.apache.log4j.Logger;
import ru.app.protocol.cctalk.coinMachine.CCTalkCommand;
import ru.app.protocol.cctalk.coinMachine.CCTalkCommandType;
import ru.app.protocol.cctalk.coinMachine.CoinTable;
import ru.app.protocol.cctalk.coinMachine.EmulatorCommand;
import ru.app.protocol.cctalk.coinMachine.emulatorCommands.ACK;
import ru.app.protocol.cctalk.coinMachine.emulatorCommands.BufferCredit;
import ru.app.protocol.cctalk.coinMachine.emulatorCommands.CoinID;
import ru.app.util.LogCreator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private SerialPort serialPort;
    private static final byte MACHINE_ADDR = (byte) 0x02;
    private static final byte COIN_ADDR = (byte) 0x01;
    private static Map<Integer, String> coinTable;
    private boolean enable = false;

    private byte currentCounter;
    private byte[] currentBuffer;

    Client(String portName) {
        serialPort = new SerialPort(portName);
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(new PortReader());

            initOther();
        } catch (SerialPortException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void initOther() {
        CoinTable table = new CoinTable();
        coinTable = table.getTable();
        enable = false;
        currentCounter = (byte) 0x01;
        currentBuffer = new byte[]{0, (byte) 0x14, 0, (byte) 0x14, 0, (byte) 0x14, 0, (byte) 0x14, 0, (byte) 0x14};
    }

    private synchronized void sendMessage(EmulatorCommand command) {
        try {
            byte[] output = formPacket(command);
            LOGGER.info(LogCreator.logOutput(output));
            serialPort.writeBytes(output);
        } catch (SerialPortException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private synchronized void sendBytes(byte[] buffer) {
        try {
            LOGGER.debug(LogCreator.logOutput(buffer));
            serialPort.writeBytes(buffer);
        } catch (SerialPortException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Высчитать контрольную сумму у части массива байт (modulo 256)
     *
     * @param data - данные
     */
    private static byte checksum(byte[] data) {
        int sum = 0;
        for (byte b : data) sum += 0xff & b;

        while (sum > 256)
            sum = sum - 256;

        return (byte) (256 - sum);
    }

    private byte[] formPacket(EmulatorCommand command) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(COIN_ADDR);
            byte[] data = command.getData();
            baos.write((byte) data.length);
            baos.write(MACHINE_ADDR);
            baos.write(0); // command byte always ZERO for emulator coin
            baos.write(data);
            baos.write(checksum(baos.toByteArray()));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return baos.toByteArray();
    }

    boolean isEnabled() {
        return enable;
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR && event.getEventValue() > 0) {
                try {
                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                    byte machine = serialPort.readBytes(1)[0];
                    if (machine != MACHINE_ADDR) return; //WRONG MACHINE ADDR!!
                    response.write(machine);
                    byte length = serialPort.readBytes(1)[0];
                    response.write(length);
                    byte coin = serialPort.readBytes(1)[0];
                    response.write(coin);
                    if (coin != COIN_ADDR) return; // WRONG COIN ADDRESS!
                    byte command = serialPort.readBytes(1)[0];
                    response.write(command);
                    byte[] data = serialPort.readBytes(length, 5);

                    ByteArrayOutputStream commandData = new ByteArrayOutputStream();
                    for (byte b : data) {
                        commandData.write(b);
                        response.write(b);
                    }
                    CCTalkCommand ccTalkCommand = new CCTalkCommand(CCTalkCommandType.getTypeByCode(command));
                    ccTalkCommand.setData(commandData.toByteArray());

                    byte checksum = serialPort.readBytes(1)[0];
                    response.write(checksum);

                    byte[] resp = response.toByteArray();
                    LOGGER.info(LogCreator.logInput(resp));
                    emulateProcess(ccTalkCommand, resp);
                } catch (SerialPortException | SerialPortTimeoutException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }
    }

    private void emulateProcess(CCTalkCommand command, byte[] buffer) {
        if (command.getCommandType() == null) return;
        byte[] data = command.getData();
        sendBytes(buffer); // send mirror response
        switch (command.getCommandType()) {
            case RequestCoinId:
                String ascii = coinTable.get((int) data[0]);
                byte[] nominal = ascii.getBytes(StandardCharsets.UTF_8);
                sendMessage(new CoinID(nominal));
                break;
            case ReadBufferedCreditOrErrorCodes:
                sendMessage(new BufferCredit(currentCounter, currentBuffer));
                break;
            case ModifyInhibitStatus:
                enable = (data[0] == (byte) 0xFF);
                sendMessage(new ACK());
                break;
            default:
                sendMessage(new ACK());
                break;
        }
    }

    void incrementCounter(int iter) {
        for (int i = 0; i < iter; i++) {
            currentCounter++;
            if (currentCounter == 0) currentCounter++;
        }
    }

    byte[] getCurrentBuffer() {
        return currentBuffer;
    }

    void setCurrentBuffer(byte[] buffer) {
        currentBuffer = buffer;
    }

    byte getCurrentCounter() {
        return currentCounter;
    }
}