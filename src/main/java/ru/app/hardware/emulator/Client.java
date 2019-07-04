package ru.app.hardware.emulator;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import ru.app.listeners.AbstractClient;
import ru.app.protocol.ccnet.CommandType;
import ru.app.protocol.cctalk.Command;
import ru.app.util.Utils;

public class Client extends AbstractClient {
    private SerialPort serialPort;
    private byte[] received;
    private final byte SYNC = (byte) 0x02;
    private final byte PERIPHERIAL_CODE = (byte) 0x03;

    private boolean isEnabled;

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

            isEnabled = false;
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    synchronized public byte[] sendMessage(Command command) {
        return new byte[0];
    }

    @Override
    synchronized public void sendBytes(byte[] bytes) {
        try {
            serialPort.writeBytes(bytes);
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR && event.getEventValue() > 0) {
                try {
                    Thread.sleep(400);
                    received = serialPort.readBytes();
                    System.out.println(Utils.bytes2hex(received));

                    emulateProcess(received);
                } catch (InterruptedException | SerialPortException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void emulateProcess(byte[] received) {
        if (received[0] != SYNC) alertMsg();
        if (received[1] != PERIPHERIAL_CODE) alertMsg();
        int length = received[2];
        byte command = received[3];
        byte[] data;
        if (length - 5 > 0) {
            data = new byte[length - 5];
            System.arraycopy(data, 3, data, 0, data.length);
        }
        byte checksum0 = received[length - 2];
        byte checksum1 = received[length - 1];
        int checksum = (0xff & checksum0) + ((0xff & checksum1) << 8);
        //todo - проверка на checkSum

        if (CommandType.getTypeByCode(command) == CommandType.Poll) {
            if (!isEnabled) {
                sendBytes(new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x06, (byte) 0x19, (byte) 0x82, (byte) 0x0F}); // disabled status
                return;
            } else {
                sendBytes(new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x06, (byte) 0x14, (byte) 0x67, (byte) 0xD4}); // enabled status
                return;
            }
        }
        sendBytes(new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x06, (byte) 0x00, (byte) 0xC2, (byte) 0x82}); // ACK
    }

    private void alertMsg() {
        System.out.println("CAN NOT EMULATE RESPONSE! WRONG DATA!");
        throw new RuntimeException();
    }
}
