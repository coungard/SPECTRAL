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
                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                    byte[] sync = serialPort.readBytes(1);
                    if (sync[0] != SYNC) return; //WRONG SYNC!!logOJChecJCheckBoxkBoxutput
                    response.write(sync);
                    byte[] addr = serialPort.readBytes(1);
                    if (addr[0] != PERIPHERIAL_CODE) return; // WRONG ADDRESS!!
                    response.write(addr);
                    byte[] length = serialPort.readBytes(1);
                    response.write(length);
                    byte[] command = serialPort.readBytes(1);
                    response.write(command);
                    byte[] message = serialPort.readBytes(length[0] - response.size(), 50);
                    response.write(message);

//                    Logger.logInput(response.toByteArray());
                    client.sendBytes(response.toByteArray());

                } catch (SerialPortException | SerialPortTimeoutException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
