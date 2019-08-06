package ru.app.hardware.emulator.cashcodeCCNET;

import jssc.*;
import ru.app.main.Settings;
import ru.app.protocol.ccnet.BillStateType;
import ru.app.protocol.ccnet.Command;
import ru.app.protocol.ccnet.CommandType;
import ru.app.protocol.ccnet.emulator.response.Identification;
import ru.app.protocol.ccnet.emulator.response.SetStatus;
import ru.app.protocol.ccnet.emulator.response.TakeBillTable;
import ru.app.util.Crc16;
import ru.app.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;


class Client {
    private SerialPort serialPort;
    private final byte SYNC = (byte) 0x02;
    private final byte PERIPHERIAL_CODE = (byte) 0x03;

    private RxThread rxThread;
    private byte[] currentDenom;
    private volatile BillStateType status = BillStateType.UnitDisabled;
    private volatile boolean change;
    private volatile long casherStateTime;
    private CommandType currentCommand;

    private long logDelay = 10000;
    private long activityDate;

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
                    Logger.console("new state : " + status);
                    change = false;
                    long started = System.currentTimeMillis();
                    do {
                        //todo nothing except sleep thread
                    } while (System.currentTimeMillis() - started < casherStateTime);

                    if (status == BillStateType.Stacking) {
                        Logger.console("Status stacking");
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
            casherStateTime = ms;
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
            if (accessLog(output))
                Logger.logOutput(output);
            serialPort.writeBytes(output);
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    private boolean accessLog(byte[] buffer) {
        if (Manager.isVerboseLog()) return true;
        if (currentCommand == CommandType.ACK) return false;

        long timestamp = System.currentTimeMillis();
        if (timestamp - activityDate > logDelay) {
            activityDate = timestamp;
            return true;
        }
        return false;
    }

    private synchronized void sendBytes(byte[] bytes) {
        try {
            Logger.logOutput(bytes);
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
                    currentCommand = CommandType.getTypeByCode(command[0]);
                    response.write(command);
                    byte[] message = serialPort.readBytes(length[0] - response.size(), 50);
                    response.write(message);

                    if (accessLog(response.toByteArray()))
                        Logger.logInput(response.toByteArray());
                    emulateProcess(response.toByteArray());
                } catch (SerialPortException | SerialPortTimeoutException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void emulateProcess(byte[] received) {
        switch (currentCommand) {
            case ACK:
                return;
            case Reset:
                rxThread.setStatus(BillStateType.Initialize, 6000);
            case GetStatus:
                sendMessage(new SetStatus());
                break;
            case GetBillTable:
                sendMessage(new TakeBillTable());
                break;
            case Identification:
                sendMessage(new Identification());
                break;
            case Stack:
                sendMessage(new Command(CommandType.ACK));
                rxThread.setStatus(BillStateType.Stacking, 1000);
                break;
            case Poll:
                sendMessage(new Command(rxThread.getStatus()));
                break;
            case EnableBillTypes:
                boolean idling = received[5] == (byte) 0xFF;
                rxThread.setStatus(idling ? BillStateType.Idling : BillStateType.UnitDisabled);
            default:
                sendMessage(new Command(CommandType.ACK));
        }
    }

    private byte[] formPacket(Command command) {
        byte[] data = command.getData() != null ? command.getData() : new byte[0];
        boolean emulCommand = command.isEmulator();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(SYNC);
        baos.write(PERIPHERIAL_CODE);
        int length = 6;
        baos.write(length + (emulCommand ? data.length - 1 : data.length)); // если тип команды ResponseType, в длину не входит байт команды.
        if (!emulCommand) baos.write(command.getType().getCode());
        for (byte b : data) {
            baos.write(b);
        }
        int checksum = Crc16.crc16(baos.toByteArray());
        baos.write((byte) (checksum & 0xFF));
        baos.write((byte) (checksum >> 8 & 0xFF));

        return baos.toByteArray();
    }

    BillStateType getStatus() {
        return rxThread.getStatus();
    }

    CommandType getCurrentCommand() {
        return currentCommand;
    }
}
