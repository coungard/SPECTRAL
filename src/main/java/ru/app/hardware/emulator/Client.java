package ru.app.hardware.emulator;

import jssc.*;
import ru.app.listeners.AbstractClient;
import ru.app.main.Settings;
import ru.app.protocol.ccnet.CommandType;
import ru.app.protocol.cctalk.Command;
import ru.app.util.Utils;

import java.util.Date;

public class Client extends AbstractClient {
    private SerialPort serialPort;
    private byte[] received;
    private final byte SYNC = (byte) 0x02;
    private final byte PERIPHERIAL_CODE = (byte) 0x03;

    private boolean isEnabled;
    private long initializeStart;

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
    public byte[] sendMessage(Command command) {
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
//                    Thread.sleep(20);
                    int len = 6;
                    received = serialPort.readBytes(len, 20);
                    int newLen = received[2] - len;
                    if (newLen > 0) {
                        byte[] restInput = serialPort.readBytes(newLen, 50);
                        received = Utils.concat(received, restInput);
                    }
                    System.out.println(Settings.dateFormat.format(new Date()) + "\tinput << " + Utils.bytes2hex(received));
                    emulateProcess(received);
                } catch (SerialPortException | SerialPortTimeoutException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void emulateProcess(byte[] received) {
        if (received[0] != SYNC) {
            alertMsg();
            return;
        }
        if (received[1] != PERIPHERIAL_CODE) alertMsg();
        int length = received[2];
        byte command = received[3];
//        byte[] data;
//        if (length - 5 > 0) {
//            data = new byte[length - 5];
//            System.arraycopy(data, 3, data, 0, data.length);
//        }
//        byte checksum0 = received[length - 2];
//        byte checksum1 = received[length - 1];
//        int checksum = (0xff & checksum0) + ((0xff & checksum1) << 8);
        //todo - проверка на checkSum

        if (CommandType.getTypeByCode(command) == CommandType.Reset) {
            System.out.println("COMMAND RESET");
            initializeStart = System.currentTimeMillis();
            isEnabled = false;
        }

        if (CommandType.getTypeByCode(command) == CommandType.ACK) {
            System.out.println("COMMAND ACK >> NOTHING FOR OUTPUT");
            return;
        }

        if (CommandType.getTypeByCode(command) == CommandType.GetStatus) {
            byte[] output = new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x0B, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA8, (byte) 0x67};
            System.out.println("output >> " + Utils.bytes2hex(output) + "\tGet Status Command");
            sendBytes(output);
            return;
        }

        if (CommandType.getTypeByCode(command) == CommandType.GetBillTable) {
            byte[] output = new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x7D, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x52,
                    (byte) 0x55, (byte) 0x53, (byte) 0x01, (byte) 0x05, (byte) 0x52, (byte) 0x55, (byte) 0x53, (byte) 0x01,
                    (byte) 0x01, (byte) 0x52, (byte) 0x55, (byte) 0x53, (byte) 0x02, (byte) 0x05, (byte) 0x52, (byte) 0x55,
                    (byte) 0x53, (byte) 0x02, (byte) 0x01, (byte) 0x52, (byte) 0x55, (byte) 0x53, (byte) 0x03, (byte) 0x05,
                    (byte) 0x52, (byte) 0x55, (byte) 0x53, (byte) 0x03, (byte) 0x01, (byte) 0x52, (byte) 0x55, (byte) 0x53,
                    (byte) 0x00, (byte) 0x02, (byte) 0x52, (byte) 0x55, (byte) 0x53, (byte) 0x00, (byte) 0x05, (byte) 0x52,
                    (byte) 0x55, (byte) 0x53, (byte) 0x00, (byte) 0x01, (byte) 0x52, (byte) 0x55, (byte) 0x53, (byte) 0x01,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                    (byte) 0x42, (byte) 0x41, (byte) 0x52, (byte) 0x00, (byte) 0xDD, (byte) 0xE8};
            System.out.println(Settings.dateFormat.format(new Date()) + "\toutput >> " + Utils.bytes2hex(output));
            sendBytes(output);
            return;
        }
        if (CommandType.getTypeByCode(command) == CommandType.Identification) {
            byte[] output = new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x27, (byte) 0x53, (byte) 0x4D, (byte) 0x2D, (byte) 0x52,
                    (byte) 0x55, (byte) 0x31, (byte) 0x33, (byte) 0x35, (byte) 0x33, (byte) 0x20, (byte) 0x20, (byte) 0x20,
                    (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x32, (byte) 0x31, (byte) 0x4B, (byte) 0x43, (byte) 0x30, (byte) 0x37,
                    (byte) 0x30, (byte) 0x30, (byte) 0x36, (byte) 0x38, (byte) 0x35, (byte) 0x37, (byte) 0xE7, (byte) 0x00,
                    (byte) 0x4D, (byte) 0x53, (byte) 0x08, (byte) 0x12, (byte) 0xF0, (byte) 0xED, (byte) 0xAA};
            System.out.println(Settings.dateFormat.format(new Date()) + "\toutput >> " + Utils.bytes2hex(output));
            sendBytes(output);
            return;
        }

        if (CommandType.getTypeByCode(command) == CommandType.Poll) {
            if (initialize()) {
                byte[] output = new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x06, (byte) 0x13, (byte) 0xD8, (byte) 0xA0};
                System.out.println(Settings.dateFormat.format(new Date()) + "\toutput >> " + Utils.bytes2hex(output) + "\tisInitialized");
                sendBytes(output);
                return;
            }
            if (!isEnabled) {
                byte[] output = new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x06, (byte) 0x19, (byte) 0x82, (byte) 0x0F};
                System.out.println(Settings.dateFormat.format(new Date()) + "\toutput >> " + Utils.bytes2hex(output) + "\tDISABLED");
                sendBytes(output); // disabled status
                return;
            } else {
                byte[] output = new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x06, (byte) 0x14, (byte) 0x67, (byte) 0xD4};
                System.out.println(Settings.dateFormat.format(new Date()) + "\toutput >> " + Utils.bytes2hex(output) + "\tIDLING");
                sendBytes(output); // enabled status
                return;
            }
        }

        if (CommandType.getTypeByCode(command) == CommandType.EnableBillTypes) {
            isEnabled = received[5] == (byte) 0xFF;
        }

        byte[] output = new byte[]{(byte) 0x02, (byte) 0x03, (byte) 0x06, (byte) 0x00, (byte) 0xC2, (byte) 0x82};
        System.out.println("output >> " + Utils.bytes2hex(output) + "\tACK");
        sendBytes(output); // ACK
    }

    private boolean initialize() {
        return System.currentTimeMillis() - initializeStart < 6000;
    }

    private void alertMsg() {
        System.out.println("CAN NOT EMULATE RESPONSE! WRONG DATA!");
        throw new RuntimeException();
    }
}
