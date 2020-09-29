package ru.app.main;

import jssc.SerialPortList;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.SAXException;
import ru.app.bus.DeviceType;
import ru.app.hardware.emulator.cashcodeCCNET.Client;
import ru.app.hardware.emulator.cashcodeCCNET.ManagerListener;
import ru.app.network.Helper;
import ru.app.network.Payment;
import ru.app.network.Requester;
import ru.app.network.Status;
import ru.app.network.rmi.RmiServerInterface;
import ru.app.protocol.ccnet.BillStateType;
import ru.app.protocol.ccnet.emulator.BillTable;
import ru.app.util.LogCreator;
import ru.app.util.Utils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Специальный сервис для эмулятора, который работает исключительно в командной строке, не используя графический
 * интерфейс.
 */
public class RmiServer extends UnicastRemoteObject implements RmiServerInterface {
    public static final Logger LOGGER = Logger.getLogger(RmiServer.class);
    private static Client client;
    private Requester requester;
    private Map<String, byte[]> billTable;

    public static final String URL = Settings.propEmulator.get("url");

    private static final int STATUS_TIME_OUT = Integer.parseInt(Settings.propEmulator.get("timeout.status")); // timeout between bot-statuses
    private static final int NOMINALS_TIME_OUT = Integer.parseInt(Settings.propEmulator.get("timeout.nominals")); // timeout between insert notes
    private static final int REQUESTER_TIME_OUT = Integer.parseInt(Settings.propEmulator.get("timeout.requester")); // timeout between insert notes
    private static final int BOT_STARTER_TIME_OUT = 1000 * 60 * 10; // timeout for start bot & receive identification command
    private static final long CASHER_TIME_OUT = 1000 * 60;  // timeout for expected cashmachine status

    private Payment payment;
    private long activity = System.currentTimeMillis();
    private volatile String oldStatus = "";
    private volatile File payFile;
    private boolean cassetteOut;
    private boolean requesterStarted;
    private boolean mock;

    public RmiServer(boolean mock) throws RemoteException {
        String log4jPath = System.getProperty("os.name").contains("Linux") ? "log4j.xml" : "log4j_win.xml";
        DOMConfigurator.configure(Objects.requireNonNull(this.getClass().getClassLoader().getResource(log4jPath)));
        LogCreator.init();
        this.mock = mock;

        LOGGER.info("Emulator v." + Settings.VERSION + " started!");
        try {
            String emulPort;
            if (!Files.exists(Paths.get(Settings.autoLaunchPropFile))) {
                LOGGER.info("Not found comport for starting.");
                String[] ports = SerialPortList.getPortNames();
                System.out.println("serial port list: ");
                for (int i = 0; i < ports.length; i++) {
                    System.out.println(i + 1 + ": " + ports[i]);
                }
                System.out.print("choose port number: ");
                Scanner scanner = new Scanner(System.in);
                int port = scanner.nextInt();
                emulPort = ports[port - 1];
                Utils.saveProp(Collections.singletonMap("port", emulPort), Settings.autoLaunchPropFile);
                LOGGER.info("emul port: " + emulPort + " saved for autostart");
            } else {
                emulPort = Utils.getPropertyFromFile(Settings.autoLaunchPropFile, "port");
            }

            if (emulPort != null) {
                Settings.hardware = DeviceType.EMULATOR;
                Settings.device = "CCNET CASHER";
                startManager(emulPort);
            } else {
                LOGGER.info("emulPort = null!");
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void startManager(String emulPort) {
        client = new Client(emulPort, new ManagerListener() {
            @Override
            public void serialPortErrorReports() {
                LOGGER.warn("SERIAL PORT ERROR!");
            }
        });
        requester = new Requester(URL);
        billTable = new BillTable().getTable();
        LOGGER.info("Emulator properties: " + Settings.propEmulator);
        startRMI();
        startProcess();
    }

    private void startRMI() {
        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
            LOGGER.info("Security manager installed.");
        } else {
            LOGGER.info("Security manager already exists.");
        }
        try { //special exception handler for registry creation
            LocateRegistry.createRegistry(1099);
            LOGGER.info("java RMI registry created.");
        } catch (RemoteException ex) {
            //do nothing, error means registry already exists
            LOGGER.info("java RMI registry already exists.");
        }
        try {
            // Bind this object instance to the name "RmiServer"
            Naming.rebind("//127.0.0.1/RmiServer", this);
            LOGGER.info("PeerServer bound in registry");
        } catch (Exception ex) {
            LOGGER.error("RMI server exception:" + ex.getMessage());
        }
    }

    private void startProcess() {
        LOGGER.info("Bot starting...");
        long started = System.currentTimeMillis();
        boolean access = false;
        try {
            Path path = Paths.get("payments/autoRun");
            if (Files.exists(path)) {
                LOGGER.warn("Bot file already exists! Recreating.");
                Files.delete(path);
            }
            Files.createFile(path);
            Map<String, String> startOpt = new HashMap<>();
            startOpt.put("bot", "open");
            Helper.saveProp(startOpt, path.toFile());
            do {
                Thread.sleep(400);
                Map<String, String> bot = Helper.loadProp(path.toFile());
                if (bot.get("bot").equals("ok")) {
                    Files.delete(path);
                    access = true;
                    break;
                }
            } while (System.currentTimeMillis() - started < BOT_STARTER_TIME_OUT);

            if (access) {
                LOGGER.info("Bot started! Waiting command Identification before Requesting...");
                long start = System.currentTimeMillis();
                do {
                    Thread.sleep(500);
                    if (client.isActive() || mock) break;
                } while (System.currentTimeMillis() - start < BOT_STARTER_TIME_OUT);
                Thread.sleep(5000);
                if (!client.isActive() && !mock) {
                    LOGGER.error("COMMAND IDENTIFICATION TIME OUT! REQUESTER WILL NOT START!");
                } else {
                    LOGGER.info("Identification command received. 10 minutes waiting for repaints...");
                    Thread.sleep(mock ? 5000 : 1000 * 60 * 10);
                    if (!requesterStarted) startRequester();
                }
            } else {
                LOGGER.error("Can not starting bot!");
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void startRequester() {
        LOGGER.info("Requester loop started");
        requesterStarted = true;
        while (true) {
            try {
                Thread.sleep(3000);
                if (mock) {
                    payment = new Payment();
                    payment.setId(100500);
                    payment.setNumber("89634100211");
                    payment.setProvider("qiwi");
                    payment.setNumber("100.00");
                    payment.setText("mock");
                    payment.setCodeOperation("");

                    String requestErr = requester.sendStatus(payment, Status.ERROR);
                    LOGGER.info("Request status : " + requestErr);
                    break;
                }

                String response = requester.checkPayment();
                LOGGER.info("Response:\n" + response);
                boolean isCommand = response != null && response.contains("command");

                if (isCommand) {
                    LOGGER.info("Starting payment process..");

                    payment = Helper.createPayment(response);
                    if (payment.getSum() > 0) {
                        activity = System.currentTimeMillis();
                        LOGGER.info("Starting payment operation from server...");

                        Map<String, String> payProperties = new HashMap<>();
                        payProperties.put("number", payment.getNumber());
                        payProperties.put("text", payment.getText());
                        payProperties.put("sum", "" + payment.getSum());
                        payProperties.put("provider", payment.getProvider());
                        payProperties.put("status", Status.ACCEPTED.name());
                        payProperties.put("code_operation", "");     // empty value

                        payFile = new File(Settings.paymentPath);
                        Helper.saveProp(payProperties, payFile);

//                        boolean access = waitFor(Status.COMPLETED);
//                        if (!access) continue;

                        boolean idling = waitFor2(BillStateType.Idling, 1000 * 60 * 4);
                        if (!idling) continue;

                        Thread.sleep(8000);
                        Map<String, String> data = Helper.loadProp(payFile); // бот изменяет содержимое файла
                        String cur = data.get("status");
                        if (!cur.equals(Status.COMPLETED.name())) {
                            LOGGER.warn(LogCreator.console("Warning! Payment status still not completed yet!"));
                        }
                        List<Integer> nominals = Utils.calculatePayment(payment.getSum());
                        int paid = 0;
                        boolean error;
                        int attempts = 1;
                        do {
                            error = false;
                            Iterator<Integer> iterator = nominals.iterator();
                            LOGGER.info("Attempt : " + attempts);
                            while (iterator.hasNext()) {
                                Thread.sleep(NOMINALS_TIME_OUT);
                                Integer nominal = iterator.next();
                                String bill = String.valueOf(nominal);
                                boolean deposit = billAcceptance(billTable.get(bill));
                                if (deposit) {
                                    paid += nominal;
                                    iterator.remove();
                                } else
                                    error = true;
                            }
                            if (error) {
                                LOGGER.warn("Required sum: " + payment.getSum() +
                                        ", Paid sum: " + paid + ", Rest sum: " + (payment.getSum() - paid));
                                attempts++;
                            } else
                                break;
                        } while (attempts <= 3);

                        Thread.sleep(6000);
                        payProperties.put("status", Status.STACKED.name());
                        oldStatus = payProperties.get("status");
                        Helper.saveProp(payProperties, payFile);

                        if (error) {
                            waitFor(Status.SUCCESS, 1000 * 60 * 3);
                            saveAsError();
                            Thread.sleep(CASHER_TIME_OUT);
                            continue;
                        }

                        boolean success = waitFor(Status.SUCCESS, 1000 * 30);
                        if (success) {
                            LOGGER.info("Payment successfully complete! Waiting a minute before sending payment.");
                            Thread.sleep(1000 * 60 * 3);
                            data = Helper.loadProp(payFile);
                            payment.setCodeOperation(data.get("code_operation"));

                            Helper.saveFile(payment, Status.SUCCESS);
                            String request = requester.sendStatus(payment, Status.SUCCESS);
                            LOGGER.info("Request status : " + request);
                        } else
                            LOGGER.info("С ПЛАТЕЖОМ ЧТО-ТО НЕ ТО! ГОСПОДИ БОЖЕ МОЙ!");
                    }
                }
                Thread.sleep(REQUESTER_TIME_OUT);
            } catch (IOException | InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
                LOGGER.info("Requester is crashed! Perhaps problems with network...check Payment please");
                try {
                    Thread.sleep(CASHER_TIME_OUT);
                } catch (InterruptedException exx) {
                    LOGGER.error(exx.getMessage(), exx);
                    break;
                }
            } catch (SAXException | ParserConfigurationException ex) {
                LOGGER.error(ex.getMessage(), ex);
                LOGGER.info("Can not parse Payment Response!");
            }
        }
    }

    /**
     * Метод ожидания статуса
     *
     * @param expected - передаем статус, который мы ожидаем
     * @return true - если мы получили ожидаемый статус, false - в противном случае
     */
    private boolean waitFor(Status expected, long timeout) throws InterruptedException, IOException {
        LOGGER.info("Wait for status: " + expected);
        activity = System.currentTimeMillis();
        Status current;
        long start = System.currentTimeMillis();
        do {
            Thread.sleep(400);
            Map<String, String> data = Helper.loadProp(payFile); // бот изменяет содержимое файла
            String cur = data.get("status");
            current = Status.valueOf(cur);
            payment.setCodeOperation(data.get("code_operation"));
            if (!cur.equals(oldStatus)) {
                activity = System.currentTimeMillis();
                LOGGER.info("Current Payment Status : " + current);
            }
            if (expected == Status.SUCCESS && System.currentTimeMillis() - start > 5000) {
                LOGGER.info("Pay-Status: " + current); // не всегда считывает SUCCESS (дополнительный лог)
                start = System.currentTimeMillis();
            }
            oldStatus = cur;
            if (current == Status.ERROR) {
                LOGGER.info("Status Error. Break Payment Process!");
                break;
            }
        } while (current != expected && System.currentTimeMillis() - activity < timeout);

        if (current == Status.ERROR) {
            saveAsError();
            return false;
        }

        if (current != expected) {
            LOGGER.info("Payment Status is not " + expected + "!");
            saveAsManual();
            return false;
        }
        return true;
    }

    /**
     * Метод ожидания статуса купюроприемника
     *
     * @param expected - передаем статус купюроприемника, который мы ожидаем
     * @return true - если мы получили ожидаемый статус, false - в противном случае
     */
    private boolean waitFor2(BillStateType expected, long timeout) throws InterruptedException, IOException {
        LOGGER.info("wait for casher state: " + expected);
        activity = System.currentTimeMillis();
        BillStateType state;
        Status current;

        do {
            Thread.sleep(500);
            state = client.getStatus();

            Map<String, String> data = Helper.loadProp(payFile); // бот изменяет содержимое файла

            String cur = data.get("status");
            current = Status.valueOf(cur);
            payment.setCodeOperation(data.get("code_operation"));
            if (current == Status.ERROR) {
                LOGGER.info("Status Error. Break Payment Process!");
                break;
            }
        } while (state != expected && System.currentTimeMillis() - activity < timeout);

        if (current == Status.ERROR) {
            saveAsError();
            return false;
        }

        if (state != expected) {
            LOGGER.error("Terminal still not " + expected + " yet! Time out error!");
            saveAsManual();
            return false;
        }
        return true;
    }

    private void saveAsError() throws IOException {
        Helper.saveFile(payment, Status.ERROR);
        String requestErr = requester.sendStatus(payment, Status.ERROR);
        LOGGER.info("Request status : " + requestErr);
    }

    private void saveAsManual() throws IOException {
        Helper.saveFile(payment, Status.MANUAL);
        String requestErr = requester.sendStatus(payment, Status.MANUAL);
        LOGGER.info("Request status : " + requestErr);
    }

    /**
     * Эмуляция вставки банкноты по ее номиналу
     *
     * @param denomination - байт номинала банкноты (смотреть BillTable)
     * @return true - в случае успешного депозита банкноты, false - в случае провала
     */
    private boolean billAcceptance(byte[] denomination) throws InterruptedException {
        for (Map.Entry<String, byte[]> entry : billTable.entrySet()) {
            if (Arrays.equals(entry.getValue(), denomination)) {
                LOGGER.info("Bill accepting : " + entry.getKey());
            }
        }
        client.setCurrentDenom(denomination);
        client.setDepositEnded(false);
        client.setNominalStacked(false);
        client.deposit();
        long start = System.currentTimeMillis();
        do {
            Thread.sleep(40);
            if (client.isDepositEnded()) break;
        } while (System.currentTimeMillis() - start < 25000);
        return client.isNominalStacked();
    }

    @Override
    public String send(final String command) {
        final String[] answer = {null};
        Thread rmiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Command from RMIclient: " + command);
                switch (command) {
                    case "encash":
                        cassetteOut = !cassetteOut;
                        if (cassetteOut) {
                            LOGGER.info("DropCassetteOutOfPosition");
                            client.setStatus(BillStateType.DropCassetteOutOfPosition);
                            answer[0] = "CassetteOutOfPosition ";
                        } else {
                            LOGGER.info("Cassette Inserted");
                            client.setStatus(BillStateType.UnitDisabled);
                            answer[0] = "Cassette Inserted";
                        }
                        break;
                    case "bill":
                        answer[0] = "Bill nominal insert operation under development.";
                        // todo...
                        break;
                    case "requester":
                        if (requesterStarted) {
                            answer[0] = "Requester already started.";
                        } else {
                            answer[0] = "Requester starting...";
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    startRequester();
                                }
                            }).start();
                        }
                        break;
                    default:
                        answer[0] = "unknown command";
                }
            }
        });
        try {
            rmiThread.start();
            rmiThread.join();
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        LOGGER.info("Returned to RmiClient: " + answer[0]);
        return answer[0];
    }

    public static String getCurrentCommand() {
        return client.getCurrentCommand() == null ? "" : "Command: " + client.getCurrentCommand().toString();
    }

    public static String getCurrentResponse() {
        return client.getCurrentResponse();
    }
}
