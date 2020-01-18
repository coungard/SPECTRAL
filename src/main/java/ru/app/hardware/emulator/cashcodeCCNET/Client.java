package ru.app.hardware.emulator.cashcodeCCNET;

import jssc.*;
import org.apache.commons.io.input.ReversedLinesFileReader;
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
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;


public class Client {
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
    private volatile boolean depositEnded = false;
    private volatile boolean nominalStacked = false;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    private volatile String denomValue;

    public Client(String portName, ManagerListener listener) {
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
        pollingActivity = System.currentTimeMillis();
    }

    public void setCurrentDenom(byte[] currentDenom) {
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
            if (currentResponse != null && (currentResponse.contains("BillStacked") || currentResponse.contains("EscrowPosition"))) {
                for (Map.Entry<String, byte[]> entry : billTable.entrySet()) {
                    if (Arrays.equals(entry.getValue(), currentDenom)) {
                        currentResponse += " [" + entry.getKey() + "]"; // example: BillStacked [100]
                        denomValue = entry.getKey();
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

    /**
     * Данный класс отслеживает эвенты с ком-порта и на основании полученных
     */
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
                } catch (SerialPortException | SerialPortTimeoutException | IOException ex) {
                    listener.serialPortErrorReports();
                    LOGGER.error(LogCreator.console(ex.getMessage()), ex);
                }
            }
        }
    }

    private void emulateProcess(byte[] received) {
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
                LOGGER.info("STACK COMMAND FROM TERMINAL");
                sendMessage(new Command(CommandType.ACK));
//                changeStatus(1000, BillStateType.Stacking);
                break;
            case Poll:
                long diff = new Date().getTime() - new Date(pollingActivity).getTime();

                if (diff > 1000)
                    LOGGER.warn(LogCreator.console("Polling activity timeout: " + diff));

                pollingActivity = System.currentTimeMillis();
                BillStateType status = getStatus();
                switch (status) {
                    case Accepting:
                        sendMessage(new Command(BillStateType.Accepting));
                        break;
                    case EscrowPosition:
                        sendMessage(new Command(BillStateType.EscrowPosition, currentDenom));
                        setStatus(BillStateType.Stacking);
                        break;
                    case Stacking:
                        sendMessage(new Command(BillStateType.Stacking));
                        break;
                    case BillStacked:
                        nominalStacked = true;
                        sendMessage(new Command(BillStateType.BillStacked, currentDenom));
                        setStatus(BillStateType.Idling);
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

    /**
     * <p>Процедура по внесению депозита, эмулирующая настоящее внесение купюры в полость купюроприемника.</p> <br>
     * Процедура не начинается, если текущий статус эмулятора не IDLING (ожидание принятия купюр). Первое действие в
     * открывшемся потоке - установка статуса ACCEPTING (принятие купюры) ровно на 1 секунду. По истечению секунды
     * меняем статус на ESCROW_POSITION(купюра в промежуточной позиции) и ждем, пока Polling терминала примет этот статус.
     * Сразу после отправки Polling-у меняем статус на Stacking (перемещение купюры в кассету) в течение секунды.
     * Завершенается процедура статусом BillStacked (купюра помещена в кассету) и кормлением его Polling-y за установленный
     * таймаут. По завершению процедуры, либо при какой-либо ошибке устанавливается статус IDLING.
     */
    public synchronized void deposit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Date date = new Date();
                    String log = Settings.qiwiLogPath + formatter.format(date) + ".log";
                    boolean qiwiLogExists = true;
                    if (!Files.exists(Paths.get(log))) {
                        LOGGER.warn(LogCreator.console("Not found qiwi log file!"));
                        qiwiLogExists = false;
                    }

                    setStatus(BillStateType.Accepting);
                    Thread.sleep(1000);
                    setStatus(BillStateType.EscrowPosition);
                    long start = System.currentTimeMillis();
                    boolean stacking;
                    do {
                        Thread.sleep(20);
                        stacking = BillStateType.Stacking == getStatus();
                    } while (!stacking && System.currentTimeMillis() - start < 8000);
                    if (!stacking) {
                        LOGGER.warn(LogCreator.console("NOT ESCROW status for the set time!"));
                        depositEnded = true;
                    } else {
                        Thread.sleep(1000);
                        setStatus(BillStateType.BillStacked);
                        start = System.currentTimeMillis();
                        do {
                            Thread.sleep(20);
                        } while (!nominalStacked && System.currentTimeMillis() - start < 10000);
                        if (nominalStacked) {
                            if (qiwiLogExists) {
                                boolean success = false;
                                File qiwiLog = Paths.get(log).toFile();
                                // проверка последних 10 строчек qiwi лога
                                lineReader:
                                for (int k = 0; k < 3; k++) {
                                    ReversedLinesFileReader reader = new ReversedLinesFileReader(qiwiLog, Charset.forName("windows-1251"));
                                    for (int i = 0; i < 10; i++) {
                                        String line = reader.readLine();
                                        if (line != null && (line.contains("Принята купюра " + denomValue) ||
                                                line.contains("Принята монета " + denomValue))) {
                                            success = true;
                                            reader.close();
                                            break lineReader;
                                        }
                                        reader.close();
                                    }
                                    Thread.sleep(1000);
                                }
                                if (success) {
                                    LOGGER.info(LogCreator.console("Deposit " + denomValue + " successfull"));
                                } else {
                                    LOGGER.warn(LogCreator.console("Deposit " + denomValue + " not sended!"));
                                }
                            } else {
                                LOGGER.info(LogCreator.console("Deposit nominal successfull!"));
                            }
                            setStatus(BillStateType.Idling);
                            depositEnded = true;
                        } else {
                            LOGGER.warn(LogCreator.console("NOT BILLSTACKED status for the set time!"));
                            setStatus(BillStateType.Idling);
                            depositEnded = true;
                        }
                    }
                } catch (InterruptedException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                    setStatus(BillStateType.Idling);
                    depositEnded = true;
                } catch (IOException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                }
            }
        }).start();
    }

    public void setStatus(BillStateType status) {
        LOGGER.info(LogCreator.console("new status = " + status + ", old status = " + getStatus()));
        this.status = status;
    }

    public BillStateType getStatus() {
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

    public void setDepositEnded(boolean depositEnded) {
        this.depositEnded = depositEnded;
    }

    public boolean isDepositEnded() {
        return depositEnded;
    }

    public void setNominalStacked(boolean nominalStacked) {
        this.nominalStacked = nominalStacked;
    }

    public boolean isNominalStacked() {
        return nominalStacked;
    }
}
