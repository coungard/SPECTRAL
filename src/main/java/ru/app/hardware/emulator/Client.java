package ru.app.hardware.emulator;

import jssc.*;
import ru.app.main.Settings;
import ru.app.protocol.ccnet.BillStateType;
import ru.app.protocol.ccnet.Command;
import ru.app.protocol.ccnet.CommandType;
import ru.app.protocol.ccnet.emulator.ResponseType;
import ru.app.util.Crc16;
import ru.app.util.Utils;

import java.io.ByteArrayOutputStream;
import java.util.Date;

class Client {
    private SerialPort serialPort;
    private final byte SYNC = (byte) 0x02;
    private final byte PERIPHERIAL_CODE = (byte) 0x03;

    private RxThread rxThread;
    private byte[] currentDenom;
    private volatile BillStateType status = BillStateType.UnitDisabled;
    private volatile boolean change;
    private volatile long duration;

    void setCurrentDenom(byte[] currentDenom) {
        this.currentDenom = currentDenom;
    }

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

            rxThread = new RxThread();
            rxThread.start();
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    void escrowNominal() {
        rxThread.setStatus(BillStateType.Accepting, 1000);
        rxThread.setStatus(BillStateType.BillStacked, 1000);
    }

    private class RxThread extends Thread {
        BillStateType oldStatus = null;

        @Override
        public void run() {
            while (true) {
                if (change) {
                    System.out.println(Settings.dateFormat.format(new Date()) + "\tnew state : " + status);
                    change = false;
                    long started = System.currentTimeMillis();
                    do {
                        //todo nothing except sleep thread
                    } while (System.currentTimeMillis() - started < duration);

                    if (status == BillStateType.Stacking) {
                        System.out.println(Settings.dateFormat.format(new Date()) + "\tStatus stacking");
                        sendMessage(new Command(BillStateType.BillStacked, currentDenom));
                        status = BillStateType.BillStacked;
                        return;
                    }
                    status = oldStatus;
                }
            }
        }

        void setStatus(BillStateType billStateType, long ms) {
            if (status == BillStateType.UnitDisabled || status == BillStateType.Idling)
                oldStatus = status;
            System.out.println(Settings.dateFormat.format(new Date()) + "\tset status : " + billStateType + " , ms: " + ms);
            status = billStateType;
            duration = ms;
            change = true;
        }

        void setStatus(BillStateType billStateType) {
            status = billStateType;
        }

        BillStateType getStatus() {
            return status;
        }
    }

    private synchronized void sendMessage(Command command) {
        try {
            byte[] output = formPacket(command);
            System.out.println(Settings.dateFormat.format(new Date()) + "\toutput >> " + Utils.bytes2hex(output));
            serialPort.writeBytes(output);
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    private synchronized void sendBytes(byte[] bytes) {
        try {
            System.out.println(Settings.dateFormat.format(new Date()) + "\toutput >> " + Utils.bytes2hex(bytes));
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
                    int len = 6;
                    byte[] received = serialPort.readBytes(len, 20);
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
            System.out.println(Settings.dateFormat.format(new Date()) + "\tCOMMAND RESET");
            rxThread.setStatus(BillStateType.Initialize, 6000);
        }

        if (CommandType.getTypeByCode(command) == CommandType.ACK) {
            return;
        }

        if (CommandType.getTypeByCode(command) == CommandType.GetStatus) {
            sendMessage(new Command(ResponseType.GetStatus, new byte[]{0, 0, 0, 0, 0, 0}));
            return;
        }

        if (CommandType.getTypeByCode(command) == CommandType.GetBillTable) {
            byte[] data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
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
                    (byte) 0x42, (byte) 0x41, (byte) 0x52, (byte) 0x00};
            sendMessage(new Command(ResponseType.GetBillTable, data));
            return;
        }
        if (CommandType.getTypeByCode(command) == CommandType.Identification) {
            byte[] data = new byte[]{(byte) 0x53, (byte) 0x4D, (byte) 0x2D, (byte) 0x52,
                    (byte) 0x55, (byte) 0x31, (byte) 0x33, (byte) 0x35, (byte) 0x33, (byte) 0x20, (byte) 0x20, (byte) 0x20,
                    (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x32, (byte) 0x31, (byte) 0x4B, (byte) 0x43, (byte) 0x30, (byte) 0x37,
                    (byte) 0x30, (byte) 0x30, (byte) 0x36, (byte) 0x38, (byte) 0x35, (byte) 0x37, (byte) 0xE7, (byte) 0x00,
                    (byte) 0x4D, (byte) 0x53, (byte) 0x08, (byte) 0x12, (byte) 0xF0};
            sendMessage(new Command(ResponseType.Identefication, data));
            return;
        }

        if (CommandType.getTypeByCode(command) == CommandType.Poll) {
            switch (rxThread.getStatus()) {
                case Accepting:
                    sendMessage(new Command(BillStateType.Accepting));
                    return;
                case BillStacked:
                    sendMessage(new Command(BillStateType.BillStacked, currentDenom));
                    return;
                case Initialize:
                    sendMessage(new Command(BillStateType.Initialize));
                    return;
                case Idling:
                    sendMessage(new Command(BillStateType.Idling));
                    return;
                case UnitDisabled:
                    sendMessage(new Command(BillStateType.UnitDisabled));
                    return;
            }
        }

        if (CommandType.getTypeByCode(command) == CommandType.EnableBillTypes) {
            boolean idling = received[5] == (byte) 0xFF;
            rxThread.setStatus(idling ? BillStateType.Idling : BillStateType.UnitDisabled);
        }

        if (CommandType.getTypeByCode(command) == CommandType.Stack) {
            sendMessage(new Command(CommandType.ACK));
            rxThread.setStatus(BillStateType.Stacking, 1000);
            return;
        }

        sendMessage(new Command(CommandType.ACK));
    }

    private byte[] formPacket(Command command) {
        byte[] data = command.getData() != null ? command.getData() : new byte[0];
        boolean responseType = command.getType() instanceof ResponseType;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(SYNC);
        baos.write(PERIPHERIAL_CODE);
        int length = 6;
        baos.write(length + (responseType ? data.length - 1 : data.length)); // если тип команды ResponseType, в длину не входит байт команды.
        if (!responseType) baos.write(command.getType().getCode());
        for (byte b : data) {
            baos.write(b);
        }
        int checksum = Crc16.crc16(baos.toByteArray());
        baos.write((byte) (checksum & 0xFF));
        baos.write((byte) (checksum >> 8 & 0xFF));

        return baos.toByteArray();
    }

    private void alertMsg() {
        System.out.println(Settings.dateFormat.format(new Date()) + "\tCAN NOT EMULATE RESPONSE! WRONG DATA!");
        throw new RuntimeException();
    }

    BillStateType getStatus() {
        return rxThread.getStatus();
    }
}
