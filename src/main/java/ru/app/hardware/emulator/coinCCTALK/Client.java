package ru.app.hardware.emulator.coinCCTALK;

import jssc.*;
import ru.app.protocol.ccnet.Command;
import ru.app.protocol.cctalk.coinMachine.CommandType;
import ru.app.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class Client {
    private SerialPort serialPort;
    private final byte SYNC = (byte) 0x02;

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

        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    private synchronized void sendMessage(Command command) {
        try {
            byte[] output = formPacket(command);
            Logger.logOutput(output, null);
            serialPort.writeBytes(output);
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    private byte[] formPacket(Command command) {
        return new byte[0];
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR && event.getEventValue() > 0) {
                try {
                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                    byte[] sync = serialPort.readBytes(1);
                    if (sync[0] != SYNC) return; //WRONG SYNC!!
                    response.write(sync[0]);
                    byte[] length = serialPort.readBytes(1);
                    response.write(length[0]);
                    byte[] message = serialPort.readBytes(length[0] - response.size(), 50);
                    response.write(message);

                    Logger.logInput(response.toByteArray(), null);
                    emulateProcess(response.toByteArray());
                } catch (SerialPortException | SerialPortTimeoutException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void emulateProcess(byte[] response) {
        CommandType command = CommandType.getTypeByCode(response[3]);
        if (command == null) return;
        switch (command) {
            case RequestCoinId:
                Logger.console("Пришла монета!!");
                break;
            case ReadBufferedCreditOrErrorCodes:
                Logger.console("Идет опрос!");
                break;
            default:
                System.out.println("Что это такое?!");
                break;
        }
    }
}
