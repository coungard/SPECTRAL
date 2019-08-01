package ru.app.hardware.emulator.coinCCTALK;

import jssc.*;
import ru.app.protocol.cctalk.coinMachine.CCTalkCommand;
import ru.app.protocol.cctalk.coinMachine.CCTalkCommandType;
import ru.app.protocol.cctalk.coinMachine.CoinTable;
import ru.app.protocol.cctalk.coinMachine.EmulatorCommand;
import ru.app.protocol.cctalk.coinMachine.emulatorCommands.CoinID;
import ru.app.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class Client {
    private SerialPort serialPort;
    private static final byte MACHINE_ADDR = (byte) 0x02;
    private static final byte COIN_ADDR = (byte) 0x01;
    private static Map<Integer, String> coinTable;

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
            ex.printStackTrace();
        }
    }

    private void initOther() {
        CoinTable table = new CoinTable();
        coinTable = table.getTable();
    }

    private synchronized void sendMessage(EmulatorCommand command) {
        try {
            byte[] output = formPacket(command);
            Logger.logOutput(output);
            serialPort.writeBytes(output);
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    private synchronized void sendBytes(byte[] buffer) {
        try {
            Logger.logOutput(buffer);
            serialPort.writeBytes(buffer);
        } catch (SerialPortException ex) {
            ex.printStackTrace();
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
            ex.printStackTrace();
        }
        return baos.toByteArray();
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR && event.getEventValue() > 0) {
                try {
                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                    byte[] address = serialPort.readBytes(1);
                    if (address[0] != MACHINE_ADDR) return; //WRONG MACHINE ADDR!!
                    response.write(address[0]);
                    byte[] length = serialPort.readBytes(1);
                    response.write(length[0]);
                    byte[] message = serialPort.readBytes(length[0] - response.size(), 50);
                    if (message[0] != COIN_ADDR) return; // WRONG COIN ADDRESS!
                    response.write(message);

                    byte[] resp = response.toByteArray();
                    CCTalkCommand command = new CCTalkCommand(CCTalkCommandType.getTypeByCode(resp[3]));
                    ByteArrayOutputStream commandData = new ByteArrayOutputStream();
                    for (int i = 5; i < response.size() - 1; i++) {
                        commandData.write(resp[i]);
                    }
                    command.setData(commandData.toByteArray());

                    Logger.logInput(resp);
                    emulateProcess(command, resp);
                } catch (SerialPortException | SerialPortTimeoutException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void emulateProcess(CCTalkCommand command, byte[] buffer) {
        if (command == null) return;
        sendBytes(buffer); // send mirror response
        switch (command.getCommandType()) {
            case RequestCoinId:
                byte[] data = command.getData();
                String ascii = coinTable.get((int) data[0]);
                byte[] nominal = ascii.getBytes(StandardCharsets.UTF_8);
                sendMessage(new CoinID(nominal));
                break;
            case ReadBufferedCreditOrErrorCodes:
                break;
            default:
                break;
        }
    }
}