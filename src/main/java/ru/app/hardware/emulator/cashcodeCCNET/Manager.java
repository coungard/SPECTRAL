package ru.app.hardware.emulator.cashcodeCCNET;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import ru.app.hardware.AbstractManager;
import ru.app.main.Settings;
import ru.app.network.Helper;
import ru.app.network.Payment;
import ru.app.network.Requester;
import ru.app.network.Status;
import ru.app.protocol.ccnet.BillStateType;
import ru.app.protocol.ccnet.emulator.BillTable;
import ru.app.util.LogCreator;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.DefaultCaret;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;


/**
 * <p>Менеджер для работы с эмулятором купюроприемника по протоколу CCNET.</p> <br> В данный менеджер заложены эмуляции вставки банкнот, снятие
 * кассеты (инкассация) и специальный параметр для эмулятора - переход между обычным режимом и мостом, если bridge mode был заранее активирован.
 */
public class Manager extends AbstractManager {
    private static final Logger LOGGER = Logger.getLogger(Manager.class);
    private static final Color BACKGROUND_COLOR = new Color(205, 186, 116);
    private JPanel paymentPanel;
    private Client client;
    private String portName;
    private static JCheckBox verboseLog;
    private static JCheckBox autoScroll;
    private boolean cassetteOut = false;
    private JLabel emul;
    private JLabel casher;
    private JLabel serialError = new JLabel("SERIAL PORT ERROR!");
    private JLabel modeLabel = new JLabel("change mode -->>");
    private Map<String, byte[]> billTable;
    private List<JButton> billButtons = new ArrayList<>();
    private JButton encashButton;
    private Requester requester;
    private boolean requesterStarted = false;
    private boolean botStarted = false;
    private static final int STATUS_TIME_OUT = Integer.parseInt(Settings.propEmulator.get("timeout.status")); // timeout between bot-statuses
    private static final int NOMINALS_TIME_OUT = Integer.parseInt(Settings.propEmulator.get("timeout.nominals")); // timeout between insert notes
    private static final int REQUESTER_TIME_OUT = Integer.parseInt(Settings.propEmulator.get("timeout.requester")); // timeout between insert notes
    private static final int BOT_STARTER_TIME_OUT = 60000 * 10; // timeout for start bot & receive identification command
    private static final long CASHER_TIME_OUT = 60000;  // timeout for expected cashmachine status

    private static final String URL = Settings.propEmulator.get("url");

    private JButton botButton;
    private JButton requesterButton;

    public Manager(String port) {
        portName = port;
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port, new ManagerListener() {
            @Override
            public void serialPortErrorReports() {
                serialError.setVisible(true);
                LOGGER.warn(LogCreator.console("SERIAL PORT ERROR!"));
            }
        });
        requester = new Requester(URL);
        billTable = new BillTable().getTable();
        content();

        if (Files.exists(Paths.get(Settings.autoLaunchPropFile))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    startProcess();
                }
            }).start();
        }
        LOGGER.info(LogCreator.console("Client manager started on port: " + portName));
    }

    private void startProcess() {
        if (!botStarted) {
            LOGGER.info(LogCreator.console("Bot starting..."));

            long started = System.currentTimeMillis();
            botButton.setEnabled(false);

            boolean access = false;
            try {
                Path path = Paths.get("payments/autoRun");
                if (Files.exists(path)) {
                    LOGGER.warn(LogCreator.console("Bot file already exists! Recreating."));
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
                    botStarted = true;
                    botButton.setIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("graphic/bot.gif"))));
                    botButton.setEnabled(true);
                    LOGGER.info(LogCreator.console("Bot started!"));

                    LOGGER.info(LogCreator.console("Waiting command Identification before Requesting..."));
                    long start = System.currentTimeMillis();
                    do {
                        Thread.sleep(500);
                        if (client.isActive()) break;
                    } while (System.currentTimeMillis() - start < BOT_STARTER_TIME_OUT);
                    Thread.sleep(5000);
                    if (!client.isActive()) {
                        LOGGER.error(LogCreator.console("COMMAND IDENTIFICATION TIME OUT! REQUESTER WILL NOT START!"));
                    } else {
                        LOGGER.info(LogCreator.console("Identification command received. 10 minutes waiting for repaints..."));
                        Thread.sleep(60000 * 10); // wait after terminal send command Identefication
                        if (!requesterStarted) startRequester();
                    }
                } else {
                    LOGGER.error(LogCreator.console("Can not starting bot!"));
                }
            } catch (IOException | InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            } finally {
                botButton.setEnabled(true);
            }
        } else {
            LOGGER.info(LogCreator.console("stop bot button pressed"));
            botButton.setIcon(null);
            botStarted = false;
        }
    }

    private void startRequester() {
        if (!requesterStarted) {
            if (!botStarted) {
                String[] buttons = new String[]{"Yes", "No"};
                int review = JOptionPane.showOptionDialog(null, "Bot not started, are you sure for start Requester?",
                        "Attention!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, buttons, buttons[0]);

                if (review == 0) {
                    requesterButton.setIcon(new ImageIcon(Objects.requireNonNull(Manager.this.getClass().getClassLoader().getResource("graphic/requester.gif"))));
                    requesterStarted = true;
                    requestLoop();
                }
            } else {
                requesterButton.setIcon(new ImageIcon(Objects.requireNonNull(Manager.this.getClass().getClassLoader().getResource("graphic/requester.gif"))));
                requesterStarted = true;
                requestLoop();
            }
        } else {
            LOGGER.info(LogCreator.console("stop requester button pressed!"));
            requesterButton.setIcon(null);
            requesterStarted = false;
        }
    }

    private void requestLoop() {
        new Thread(new Runnable() {
            private Payment payment;
            private long activity = System.currentTimeMillis();
            private String oldStatus = "";
            private File payFile;

            @Override
            public void run() {
                LOGGER.info(LogCreator.console("Requester loop started"));
                while (requesterStarted) {
                    try {
                        Thread.sleep(3000);
                        String response = requester.checkPayment();
                        LOGGER.info(LogCreator.console("response:\n" + response));
                        boolean isCommand = response != null && response.contains("command");

                        if (isCommand) {
                            LOGGER.info(LogCreator.console("Starting payment process.."));
                            if (!emul.isVisible()) {
                                LOGGER.info(LogCreator.console("Can not start payment procedure, emulator is not activated!"));
                                return;
                            }
                            payment = Helper.createPayment(response);
                            if (payment.getSum() > 0) {
                                activity = System.currentTimeMillis();
                                LOGGER.info(LogCreator.console("Starting payment operation from server..."));
                                for (JButton billButton : billButtons)
                                    billButton.setEnabled(false);

                                Map<String, String> payProperties = new HashMap<>();
                                payProperties.put("number", payment.getNumber());
                                payProperties.put("text", payment.getText());
                                payProperties.put("sum", "" + payment.getSum());
                                payProperties.put("provider", payment.getProvider());
                                payProperties.put("status", "ACCEPTED");

                                payFile = new File(Settings.paymentPath);
                                Helper.saveProp(payProperties, payFile);

                                boolean access = waitFor(Status.COMPLETED);
                                if (!access) continue;

                                boolean idling = waitFor2(BillStateType.Idling);
                                if (!idling) continue;

                                List<Integer> nominals = Utils.calculatePayment(payment.getSum());
                                int paid = 0;
                                boolean error;
                                int attempts = 1;
                                do {
                                    error = false;
                                    Iterator<Integer> iterator = nominals.iterator();
                                    LOGGER.info(LogCreator.console("attempt : " + attempts));
                                    while (iterator.hasNext()) {
                                        Thread.sleep(NOMINALS_TIME_OUT);
                                        Integer nominal = iterator.next();
                                        String bill = "" + nominal;
                                        boolean deposit = billAcceptance(billTable.get(bill));
                                        if (deposit) {
                                            paid += nominal;
                                            iterator.remove();
                                        } else
                                            error = true;
                                    }
                                    if (error) {
                                        LOGGER.warn(LogCreator.console("Required sum: " + payment.getSum() +
                                                ", Paid sum: + " + paid + ", Rest sum: " + (payment.getSum() - paid)));
                                        attempts++;
                                    } else
                                        break;
                                } while (attempts <= 3);

                                Thread.sleep(6000);
                                payProperties.put("status", "STACKED");
                                oldStatus = payProperties.get("status");
                                Helper.saveProp(payProperties, payFile);

                                if (error) {
                                    waitFor(Status.SUCCESS);
                                    saveAsError();
                                    Thread.sleep(CASHER_TIME_OUT);
                                    continue;
                                }

                                access = waitFor(Status.SUCCESS);
                                if (access) {
                                    LOGGER.info(LogCreator.console("Payment successfully complete!"));
                                    Helper.saveFile(payment, Status.SUCCESS);
                                    String request = requester.sendStatus(payment, Status.SUCCESS);
                                    LOGGER.info(LogCreator.console("Request status : " + request));
                                } else
                                    LOGGER.info(LogCreator.console("С ПЛАТЕЖОМ ЧТО-ТО НЕ ТО! ГОСПОДИ БОЖЕ МОЙ!"));
                            }
                        }
                        Thread.sleep(REQUESTER_TIME_OUT);
                    } catch (IOException | InterruptedException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                        LOGGER.info(LogCreator.console("Requester is crashed! Perhaps problems with network...checkPayment please"));
                        try {
                            Thread.sleep(CASHER_TIME_OUT);
                        } catch (InterruptedException exx) {
                            LOGGER.error(exx.getMessage(), exx);
                        }
                    } catch (SAXException | ParserConfigurationException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                        LOGGER.info(LogCreator.console("Can not parse Payment Response!"));
                    } finally {
                        for (JButton billButton : billButtons)
                            billButton.setEnabled(true);
                    }
                }
            }

            /**
             * Метод ожидания статуса
             *
             * @param expected - передаем статус, который мы ожидаем
             * @return true - если мы получили ожидаемый статус, false - в противном случае
             */
            private boolean
            waitFor(Status expected) throws InterruptedException, IOException {
                LOGGER.info(LogCreator.console("wait for status: " + expected));
                Status current;
                long start = System.currentTimeMillis();
                do {
                    Thread.sleep(400);
                    Map<String, String> data = Helper.loadProp(payFile); // бот изменяет содержимое файла
                    String cur = data.get("status");
                    current = Status.fromString(cur);
                    if (!cur.equals(oldStatus)) {
                        activity = System.currentTimeMillis();
                        LOGGER.info(LogCreator.console("Current Payment Status : " + current));
                    }
                    if (expected == Status.SUCCESS && System.currentTimeMillis() - start > 5000) {
                        LOGGER.info(LogCreator.console("Pay-Status: " + current)); // не всегда считывает SUCCESS (дополнительный лог)
                        start = System.currentTimeMillis();
                    }
                    oldStatus = cur;
                    if (current == Status.ERROR) {
                        LOGGER.info(LogCreator.console("Status Error. Break Payment Process!"));
                        break;
                    }
                } while (current != expected && System.currentTimeMillis() - activity < STATUS_TIME_OUT);

                if (current != expected) {
                    LOGGER.info(LogCreator.console("Payment Status is not " + expected + "!"));
                    saveAsError();
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
            private boolean waitFor2(BillStateType expected) throws InterruptedException, IOException {
                LOGGER.info(LogCreator.console("wait for casher state: " + expected));
                activity = System.currentTimeMillis();
                BillStateType state;
                do {
                    Thread.sleep(400);
                    state = client.getStatus();
                } while (state != expected && System.currentTimeMillis() - activity < CASHER_TIME_OUT);

                if (state != expected) {
                    LOGGER.error(LogCreator.console("Terminal still not " + expected + " yet! Time out error!"));
                    saveAsError();
                    return false;
                }
                return true;
            }

            private void saveAsError() throws IOException {
                Helper.saveFile(payment, Status.ERROR);
                String requestErr = requester.sendStatus(payment, Status.ERROR);
                LOGGER.info(LogCreator.console("Request status : " + requestErr));
            }
        }).start();
    }

    @Override
    public void content() {
        JLabel mainLabel = formLabel("EMULATOR CASHCODE CCNET", 0);
        add(mainLabel);

        verboseLog = new JCheckBox("verbose Log");
        verboseLog.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
        verboseLog.setBounds(getWidth() - 160, 5, 150, 20);
        add(verboseLog);

        autoScroll = new JCheckBox("auto scroll");
        autoScroll.setSelected(true);
        autoScroll.setFont(verboseLog.getFont());
        autoScroll.setBounds(getWidth() - 160, 30, 150, 30);
        autoScroll.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultCaret caret = (DefaultCaret) textArea.getCaret();
                boolean auto = autoScroll.isSelected();
                caret.setUpdatePolicy(auto ? DefaultCaret.ALWAYS_UPDATE : DefaultCaret.NEVER_UPDATE);
            }
        });
        add(autoScroll);

        scroll.setBounds(30, 190, 960, 340);

        emul = new JLabel();
        emul.setIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("graphic/doomguy.gif"))));

        emul.setSize(emul.getIcon().getIconWidth(), emul.getIcon().getIconHeight());
        emul.setLocation(865, 70);
        emul.setVisible(false);
        add(emul);

        casher = new JLabel();
        casher.setIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("graphic/casher.gif"))));
        casher.setSize(emul.getIcon().getIconWidth(), emul.getIcon().getIconHeight());
        casher.setLocation(885, 70);
        casher.setVisible(false);
        add(casher);

        serialError.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
        serialError.setForeground(Color.RED);
        serialError.setBounds(420, 140, 300, 40);
        serialError.setVisible(false);
        add(serialError);

        modeLabel.setBounds(700, 80, 200, 100);
        modeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        add(modeLabel);

        encashButton = new JButton("Encashment");
        encashButton.setBounds(510, 55, 180, 40);
        encashButton.setBackground(new Color(233, 217, 182));
        encashButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        add(encashButton);
        encashButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cassetteOut = !cassetteOut;
                if (cassetteOut) {
                    encashButton.setBackground(new Color(107, 233, 159));
                    client.setStatus(BillStateType.DropCassetteOutOfPosition);
                    encashButton.setText("Connect Cassette");
                } else {
                    encashButton.setBackground(new Color(233, 217, 182));
                    client.setStatus(BillStateType.UnitDisabled);
                    encashButton.setText("Encashment");
                }
            }
        });

        botButton = new JButton("Start Bot");
        botButton.setBounds(30, 145, 160, 40);
        botButton.setBackground(new Color(243, 245, 197));
        botButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        add(botButton);
        botButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startProcess();
                    }
                }).start();
            }
        });

        requesterButton = new JButton("Start Requester");
        requesterButton.setBounds(200, 145, 160, 40);
        requesterButton.setBackground(botButton.getBackground());
        requesterButton.setFont(botButton.getFont());
        add(requesterButton);
        requesterButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startRequester();
            }
        });

        JButton saveConfig = new JButton("Save config");
        saveConfig.setBounds(850, 145, 140, 40);
        saveConfig.setFont(requesterButton.getFont());
        saveConfig.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Utils.saveProp(Collections.singletonMap("port", portName), Settings.autoLaunchPropFile);
                LOGGER.info(LogCreator.console("start.cfg for auto launch script saved"));
            }
        });
        add(saveConfig);

        final JButton deleteConfig = new JButton("Delete config");
        deleteConfig.setBounds(saveConfig.getX() - 150, saveConfig.getY(), 140, 40);
        deleteConfig.setFont(saveConfig.getFont());
        deleteConfig.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    Files.delete(Paths.get(Settings.autoLaunchPropFile));
                    LOGGER.info(LogCreator.console("start.cfg deleted"));
                    deleteConfig.setEnabled(false);
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
        add(deleteConfig);
        deleteConfig.setVisible(Files.exists(Paths.get(Settings.autoLaunchPropFile)));

        paymentPanel = new JPanel();
        paymentPanel.setOpaque(false);
        paymentPanel.setBackground(new Color(26, 0, 75, 73));
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Вставка номинала банкноты (в рублях)"));
        paymentPanel.setBounds(30, 40, 480, 100);
        add(paymentPanel);

        for (String denomination : billTable.keySet()) {
            addBill(denomination);
        }

        for (final Component component : paymentPanel.getComponents()) {
            if (component instanceof JButton) {
                component.addMouseListener(new MouseInputAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        try {
                            byte[] denomination = billTable.get(((JButton) component).getText());
                            billAcceptance(denomination);
                        } catch (InterruptedException ex) {
                            LOGGER.error(ex.getMessage(), ex);
                        }
                    }
                });
            }
        }

        if (client.realDeviceConnected()) {
            activateEmulator(false);
            emul.addMouseListener(new MouseInputAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    activateEmulator(false);
                }
            });
            casher.addMouseListener(new MouseInputAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    activateEmulator(true);
                }
            });
        } else {
            modeLabel.setVisible(false);
            emul.setVisible(true);
        }
    }

    @Override
    public void redraw() {
        LOGGER.info(LogCreator.console("Emulator + v." + Settings.VERSION + " configuration: " + Settings.propEmulator));
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
                LOGGER.info(LogCreator.console("bill accepting : " + entry.getKey()));
            }
        }
        client.setCurrentDenom(denomination);
        client.setDepositEnded(false);
        client.setNominalStacked(false);
        sendEscrowPosition();
        long start = System.currentTimeMillis();
        do {
            Thread.sleep(40);
            if (client.isDepositEnded()) break;
        } while (System.currentTimeMillis() - start < 25000);
        return client.isNominalStacked();
    }

    private void activateEmulator(boolean activate) {
        emul.setVisible(activate);
        casher.setVisible(!activate);
        encashButton.setEnabled(activate);

        for (JButton btn : billButtons)
            btn.setEnabled(activate);
        client.activateCashcode(!activate);
    }

    private void sendEscrowPosition() {
        if (client.getStatus() != BillStateType.UnitDisabled || client.realDeviceConnected()) {
            client.deposit();
        } else {
            LOGGER.warn(LogCreator.console("can not escrow, casher disabled state now!"));
        }
    }

    private void addBill(String billName) {
        JButton bill = new JButton(billName.split(" ")[0]); // Отсекаем RU в названии
        bill.setPreferredSize(new Dimension(70, 28));
        bill.setForeground(Color.WHITE);
        bill.setBackground(Color.BLACK);
        bill.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        paymentPanel.add(bill);
        billButtons.add(bill);
    }

    static boolean isVerboseLog() {
        if (Settings.args.length > 0)
            return false;
        return (verboseLog == null || verboseLog.isSelected());
    }

    @Override
    public String getCurrentCommand() {
        return client.getCurrentCommand() == null ? "" : "Command: " + client.getCurrentCommand().toString();
    }

    @Override
    public String getCurrentResponse() {
        return client.getCurrentResponse();
    }

    @Override
    protected void closeAll() {
        LOGGER.info(LogCreator.console("close client & qiwi starting.."));
        try {
            client.close();
            closeQiwi();
            Thread.sleep(3000);
            LOGGER.info(LogCreator.console("All processes terminated!"));
            Thread.sleep(3000);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void closeQiwi() {
        if (!Utils.isUnix()) {
            Utils.runCmd(new String[]{"closeMaratl.bat"});
        } else {
            LOGGER.info("can not closeQiwi on linux");
        }
    }
}
