package ru.app.hardware.emulator.cashcodeCCNET;

import jssc.*;
import ru.app.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class CashCodeClient {
    private SerialPort serialPort;
    private final byte SYNC = (byte) 0x02;
    private final byte PERIPHERIAL_CODE = (byte) 0x03;
    private Client client;

    CashCodeClient(String port, Client client) {
        this.client = client;
        serialPort = new SerialPort(port);
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

    void sendBytes(byte[] buffer) {
        try {
            Logger.logOutput(buffer);
            serialPort.writeBytes(buffer);
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR && event.getEventValue() > 0) {
                try {
                    byte[] response = serialPort.readBytes();
                    client.sendBytes(response);
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
