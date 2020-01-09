package ru.app.hardware.emulator.cashcodeCCNET;

import jssc.*;
import org.apache.log4j.Logger;
import ru.app.main.Settings;
import ru.app.protocol.ccnet.BillStateType;
import ru.app.protocol.ccnet.Command;
import ru.app.protocol.ccnet.CommandType;
import ru.app.protocol.ccnet.emulator.BillTable;
import ru.app.protocol.ccnet.emulator.response.Identification;
import ru.app.protocol.ccnet.emulator.response.SetStatus;
import ru.app.protocol.ccnet.emulator.response.TakeBillTable;
import ru.app.util.Crc16;
import ru.app.util.LogCreator;
import ru.app.util.StreamType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;


class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private final ManagerListener listener;

    private SerialPort serialPort;
    private final byte SYNC = (byte) 0x02;
    private final byte PERIPHERIAL_CODE = (byte) 0x03;

    private Map<String, byte[]> billTable;
    private volatile byte[] currentDenom;
    private CommandType currentCommand;
    private String currentResponse = "";

    private long activityDate;
    private byte[] inputBuffer = null;
    private byte[] outputBuffer = null;

    private CashCodeClient cashCodeClient;
    private CashCodeClient tempClient;
    private volatile BillStateType status = BillStateType.UnitDisabled;
    private boolean active = false;
    private long pollingActivity;
    private boolean isPoll;
    private boolean depositTransaktion = false;
    private boolean sentMessage;

    Client(String portName, ManagerListener listener) {
        serialPort = new SerialPort(portName);
        this.listener = listener;
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
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }

        if (Settings.realPortForEmulator != null)
            cashCodeClient = new CashCodeClient(Settings.realPortForEmulator, this);

        billTable = new BillTable().getTable();
    }

    void escrowNominal() {
        changeStatus(1000, BillStateType.Accepting, BillStateType.BillStacked);
    }

    void setCurrentDenom(byte[] currentDenom) {
        this.currentDenom = currentDenom;
    }

    private synchronized void sendMessage(Command command) {
        try {
            byte[] output = formPacket(command);
            if (!command.isEmulator()) {
                if (CommandType.getTypeByCode(command.getType().getCode()) == null) {
                    currentResponse = Objects.requireNonNull(BillStateType.getTypeByCode(command.getType().getCode())).toString();
                } else
                    currentResponse = command.toString();
            }
            if (currentResponse != null && currentResponse.contains("BillStacked")) {
                for (Map.Entry<String, byte[]> entry : billTable.entrySet()) {
                    if (Arrays.equals(entry.getValue(), currentDenom)) {
                        currentResponse += " [" + entry.getKey() + "]"; // example: BillStacked [100]
                        break;
                    }
                }
            }
            if (accessLog(output, StreamType.OUTPUT))
                LOGGER.info(LogCreator.logOutput(output));
            serialPort.writeBytes(output);
        } catch (SerialPortException | IOException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
    }

    synchronized void sendBytes(byte[] bytes) {
        try {
            LOGGER.debug(LogCreator.logOutput(bytes));
            serialPort.writeBytes(bytes);
        } catch (SerialPortException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
    }

    void close() throws SerialPortException {
        if (serialPort.isOpened()) {
            serialPort.closePort();
        }
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR && event.getEventValue() > 0) {
                try {
                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                    byte[] sync = serialPort.readBytes(1);
                    if (sync[0] != SYNC) {
                        LOGGER.warn("Wrong SYNC from port!");
                        return;
                    }
                    response.write(sync);
                    byte[] addr = serialPort.readBytes(1);
                    if (addr[0] != PERIPHERIAL_CODE) {
                        LOGGER.warn("Wrong address byte from port!");
                        return;
                    }
                    response.write(addr);
                    byte[] length = serialPort.readBytes(1);
                    response.write(length);
                    byte[] command = serialPort.readBytes(1);
                    currentCommand = CommandType.getTypeByCode(command[0]);
                    response.write(command);
                    byte[] message = serialPort.readBytes(length[0] - response.size(), 50);
                    response.write(message);

                    if (accessLog(response.toByteArray(), StreamType.INPUT))
                        LOGGER.info(LogCreator.logInput(response.toByteArray()));
                    if (cashCodeClient != null)
                        cashCodeClient.sendBytes(response.toByteArray());
                    else
                        emulateProcess(response.toByteArray());
                } catch (SerialPortException | SerialPortTimeoutException | IOException | InterruptedException ex) {
                    listener.serialPortErrorReports();
                    LOGGER.error(LogCreator.console(ex.getMessage()), ex);
                }
            }
        }
    }

    private void emulateProcess(byte[] received) throws InterruptedException {
        switch (currentCommand) {
            case ACK:
                return;
            case Reset:
                setStatus(BillStateType.UnitDisabled); // after reset always disabled, no idling
                changeStatus(6000, BillStateType.Initialize);
            case GetStatus:
                currentResponse = "Set Status [Emulator]";
                sendMessage(new SetStatus());
                break;
            case GetBillTable:
                currentResponse = "Take Bill Table [Emulator]";
                sendMessage(new TakeBillTable());
                break;
            case Identification:
                currentResponse = "Identification [Emulator]";
                active = true;
                sendMessage(new Identification());
                break;
            case Stack:
                sendMessage(new Command(CommandType.ACK));
                changeStatus(1000, BillStateType.Stacking);
                break;
            case Poll:
                pollingActivity = System.currentTimeMillis();
                isPoll = true;
                if (depositTransaktion)
                    Thread.sleep(50);
                BillStateType status = getStatus();
                switch (status) {
                    case Accepting:
                        sendMessage(new Command(BillStateType.Accepting));
                        break;
                    case EscrowPosition:
                        sendMessage(new Command(BillStateType.EscrowPosition, currentDenom));
                        sentMessage = true;
                        break;
                    case Stacking:
                        sendMessage(new Command(BillStateType.Stacking));
                        break;
                    case BillStacked:
                        sendMessage(new Command(BillStateType.BillStacked, currentDenom));
                        sentMessage = true;
                        break;
                    case Initialize:
                        sendMessage(new Command(BillStateType.Initialize));
                        break;
                    case Idling:
                        sendMessage(new Command(BillStateType.Idling));
                        break;
                    case UnitDisabled:
                        sendMessage(new Command(BillStateType.UnitDisabled));
                        break;
                    case DropCassetteOutOfPosition:
                        sendMessage(new Command(BillStateType.DropCassetteOutOfPosition));
                        break;
                }
                break;
            case EnableBillTypes:
                boolean disabled = received[5] == (byte) 0x00;
                setStatus(disabled ? BillStateType.UnitDisabled : BillStateType.Idling);
            default:
                sendMessage(new Command(CommandType.ACK));
        }
    }

    private byte[] formPacket(Command command) throws IOException {
        byte[] commandData = command.getData();
        byte[] data = commandData != null ? commandData : new byte[0];
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

    private boolean accessLog(byte[] buffer, StreamType type) {
        if (Manager.isVerboseLog()) return true;
        if (currentCommand == CommandType.ACK) return false;

        switch (type) {
            case INPUT:
                if (!Arrays.equals(buffer, inputBuffer)) {
                    inputBuffer = buffer;
                    return true;
                }
                break;
            case OUTPUT:
                if (!Arrays.equals(buffer, outputBuffer)) {
                    outputBuffer = buffer;
                    return true;
                }
                break;
        }

        long timestamp = System.currentTimeMillis();
        if (timestamp - activityDate > 60000) {
            activityDate = timestamp;
            return true;
        }
        return false;
    }

    boolean realDeviceConnected() {
        return cashCodeClient != null;
    }

    synchronized private void changeStatus(final long ms, final BillStateType... types) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BillStateType oldStatus = getStatus();
                try {
                    for (BillStateType type : types) {
                        setStatus(type);
                        Thread.sleep(ms);
                    }
                } catch (InterruptedException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                }
                if (oldStatus == BillStateType.Accepting || oldStatus == BillStateType.BillStacked) {
                    LOGGER.warn(LogCreator.console("Old emulator status = " + oldStatus + ". This status is not expected!"));
                    setStatus(BillStateType.Idling);
                } else
                    setStatus(oldStatus);
            }
        }).start();
    }

    synchronized void deposit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                depositTransaktion = true;
                try {
                    setStatus(BillStateType.Accepting);
                    Thread.sleep(1000);
                    boolean access = setStatusAfterPolling(BillStateType.EscrowPosition);
                    // todo ... использовать access для менеджера, в котором используется billAcceptance
                    long start = System.currentTimeMillis();
                    do {
                        if (sentMessage)
                            break;
                    } while (System.currentTimeMillis() - start < 1000);
                    if (!sentMessage) {
                        LOGGER.info(LogCreator.console("Message was not sent!"));
                        setStatus(BillStateType.Idling);
                        depositTransaktion = false;
                        return;
                    }
                    setStatus(BillStateType.Stacking);
                    Thread.sleep(1000);
                    setStatusAfterPolling(BillStateType.BillStacked);
                    start = System.currentTimeMillis();
                    do {
                        if (sentMessage)
                            break;
                    } while (System.currentTimeMillis() - start < 1000);
                    if (!sentMessage) {
                        LOGGER.info(LogCreator.console("Message was not sent!"));
                        setStatus(BillStateType.Idling);
                        depositTransaktion = false;
                        return;
                    }
                } catch (InterruptedException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                    depositTransaktion = false;
                }
                depositTransaktion = false;
                setStatus(BillStateType.Idling);
            }
        }).start();
    }

    synchronized void setStatus(BillStateType status) {
        LOGGER.info(LogCreator.console("new status = " + status + ", old status = " + getStatus()));
        this.status = status;
    }

    synchronized boolean setStatusAfterPolling(BillStateType status) throws InterruptedException {
        long start = System.currentTimeMillis();
        isPoll = false;
        do {
            Thread.sleep(20);
            if (isPoll) {
                setStatus(status);
                return true;
            }
        } while (System.currentTimeMillis() - start < 5000);
        if (isPoll = false) {
            LOGGER.info(LogCreator.console("Still not polling yet!"));
        }
        return false;
    }

    synchronized BillStateType getStatus() {
        return status;
    }

    CommandType getCurrentCommand() {
        return currentCommand;
    }

    String getCurrentResponse() {
        return "Response: " + currentResponse;
    }

    void activateCashcode(boolean enable) {
        LOGGER.info(LogCreator.console("activate cashcode = " + enable));
        if (enable && cashCodeClient == null) {
            cashCodeClient = tempClient;
        }
        if (!enable) {
            tempClient = cashCodeClient;
            cashCodeClient = null;
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPollingActivity() {
        return System.currentTimeMillis() - pollingActivity < 1000;
    }
}
