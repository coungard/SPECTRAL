package ru.app.hardware.emulator.cashcodeCCNET;

import jssc.SerialPortException;
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
import ru.app.protocol.ccnet.emulator.response.Identification;
import ru.app.util.LogCreator;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
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
    private boolean cassetteOut = false;
    private JLabel emul;
    private JLabel casher;
    private JLabel modeLabel = new JLabel("change mode -->>");
    private Map<String, byte[]> billTable;
    private List<JButton> billButtons = new ArrayList<>();
    private JButton encashButton;
    private Requester requester;
    private boolean requesterStarted = false;
    private boolean botStarted = false;
    //    private static final String URL = "http://192.168.15.121:8080/ussdWww/";
    private static final String URL = "http://109.248.44.61:8080/ussdWww/";
    private static final int TIME_OUT = 60000 * 20;
    private static final int ERROR_TIME_OUT = 60000 * 60;
    private static final int BOT_STARTER_TIME_OUT = 60000 * 10; // 10 minutes
    private JButton botButton;
    private JButton requesterButton;

    public Manager(String port) {
        portName = port;
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        requester = new Requester(URL);
        billTable = new BillTable().getTable();
        struct();

        if (Files.exists(Paths.get(Settings.autoLaunchPropFile))) {
            startProcess(true);
        }
        LOGGER.info(LogCreator.console("Client manager started on port: " + portName));
    }

    private void startProcess(final boolean withRequester) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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

                            if (withRequester) {
                                LOGGER.info(LogCreator.console("Waiting command Identification before Requesting..."));
                                long action = System.currentTimeMillis();
                                do {
                                    Thread.sleep(500);
                                    if (client.isActive()) break;
                                } while (System.currentTimeMillis() - action < 60000 * 10); // 10 minutes
                                Thread.sleep(5000);
                                if (!client.isActive()) {
                                    LOGGER.error(LogCreator.console("COMMAND IDENTIFICATION TIME OUT! REQUESTER WILL NOT START!"));
                                } else {
                                    LOGGER.info(LogCreator.console("Identification command received. 5 minutes waiting for repaints..."));
                                    Thread.sleep(60000 * 5); // wait after terminal send command Identefication
                                    startRequester();
                                }
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
        }).start();
    }

    private void startRequester() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!requesterStarted) {
                    if (!botStarted) {
                        String[] buttons = new String[]{"Yes", "No"};
                        int review = JOptionPane.showOptionDialog(null, "Bot not started, are you sure to start Requester?",
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
        }).start();
    }

    private void requestLoop() {
        new Thread(new Runnable() {
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
                            LOGGER.info(LogCreator.console("Request payment:\n" + response));
                            if (!emul.isVisible()) {
                                LOGGER.info(LogCreator.console("Can not start payment procedure, emulator is not activated!"));
                                return;
                            }
                            Payment payment = Helper.createPayment(response);
                            if (payment.getSum() > 0) {
                                long activity = System.currentTimeMillis();
                                LOGGER.info(LogCreator.console("Starting payment operation from server..."));
                                for (JButton billButton : billButtons)
                                    billButton.setEnabled(false);

                                Map<String, String> payProperties = new HashMap<>();
                                payProperties.put("number", payment.getNumber());
                                payProperties.put("text", payment.getText());
                                payProperties.put("sum", "" + payment.getSum());
                                payProperties.put("provider", payment.getProvider());
                                payProperties.put("status", "ACCEPTED");

                                File payFile = new File(Settings.paymentPath);
                                Helper.saveProp(payProperties, payFile);

                                Status status = Status.ACCEPTED;
                                String old = "";
                                // Как только создается payment файл, начинается работа бота, из другого ПО
                                // нужно дождаться статуса COMPLETED, который уведомляет о том, что мы находимся на странице платежа.
                                do {
                                    Thread.sleep(400);
                                    Map<String, String> data = Helper.loadProp(payFile); // бот изменяет содержимое файла
                                    String current = data.get("status");
                                    if (current != null) {
                                        status = Status.fromString(current);
                                        if (!current.equals(old)) {
                                            activity = System.currentTimeMillis();
                                            LOGGER.info(LogCreator.console("Current Payment Status : " + status.toString()));
                                        }
                                    }
                                    old = current;
                                    if (status == Status.ERROR)
                                        break;
                                } while (status != Status.COMPLETED && System.currentTimeMillis() - activity < TIME_OUT);

                                if (status != Status.COMPLETED) {
                                    LOGGER.error(LogCreator.console("Payment Status not Completed!"));
                                    String req = requester.sendStatus(payment, status);
                                    LOGGER.info(LogCreator.console("Error payment! Req = " + req));
                                    Thread.sleep(TIME_OUT);
                                    continue;
                                }
                                activity = System.currentTimeMillis();

                                // wait for idling status from qiwi
                                BillStateType state;
                                do {
                                    Thread.sleep(300);
                                    state = client.getStatus();
                                } while (state != BillStateType.Idling && System.currentTimeMillis() - activity < TIME_OUT);

                                if (state != BillStateType.Idling) {
                                    LOGGER.error(LogCreator.console("Terminal still not idling yet! Time out error!"));
                                    Thread.sleep(TIME_OUT);
                                    continue;
                                }
                                List<Integer> nominals = Utils.calculatePayment(payment.getSum());
                                for (Integer nominal : nominals) {
                                    Thread.sleep(4000);
                                    String bill = "" + nominal;
                                    billAcceptance(billTable.get(bill));
                                }
                                Thread.sleep(5000);
                                payProperties.put("status", Status.STACKED.toString());
                                old = payProperties.get("status");
                                Helper.saveProp(payProperties, payFile);
                                do {
                                    Thread.sleep(400);
                                    Map<String, String> data = Helper.loadProp(payFile);
                                    String current = data.get("status");
                                    if (current != null) {
                                        status = Status.fromString(current);
                                        if (!current.equals(old)) {
                                            activity = System.currentTimeMillis();
                                            LOGGER.info(LogCreator.console("Current Payment Status : " + status.toString()));
                                        }
                                    }
                                    if (status == Status.ERROR) {
                                        LOGGER.error(LogCreator.console("Error bot status!"));
                                        break;
                                    }
                                } while (status != Status.SUCCESS && System.currentTimeMillis() - activity < TIME_OUT / 5);
                                Thread.sleep(1000);
                                if (status == Status.SUCCESS) {
                                    LOGGER.info(LogCreator.console("Payment successfully complete!"));
                                    Helper.saveFile(payment, status);
                                    String request = requester.sendStatus(payment, Status.SUCCESS);
                                    LOGGER.info(LogCreator.console("Request status : " + request));
                                } else {
                                    LOGGER.info(LogCreator.console("Payment error!"));
                                    Helper.saveFile(payment, status);
                                    String requestErr = requester.sendStatus(payment, status);
                                    LOGGER.info(LogCreator.console("Request status : " + requestErr));
                                }
                                Thread.sleep(1000);
                            }
                        }
                        Thread.sleep(60000);
                    } catch (IOException | InterruptedException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                        LOGGER.info(LogCreator.console("Requester is crashed! Perhaps problems with network...checkPayment please"));
                        try {
                            Thread.sleep(60000);
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
        }).start();
    }

    @Override
    public void struct() {
        JLabel mainLabel = formLabel("EMULATOR CASHCODE CCNET", 0);
        add(mainLabel);
        scroll.setBounds(30, 190, 960, 340);

        emul = new JLabel();
//        emul.setIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("graphic/emulator.gif"))));
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
                        startProcess(true);
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

        verboseLog = new JCheckBox("verbose Log");
        verboseLog.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        verboseLog.setBounds(getWidth() - 160, 10, 150, 50);
        add(verboseLog);

        for (String denomination : billTable.keySet()) {
            addBill(denomination);
        }

        for (final Component component : paymentPanel.getComponents()) {
            if (component instanceof JButton) {
                component.addMouseListener(new MouseInputAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        byte[] denomination = billTable.get(((JButton) component).getText());
                        billAcceptance(denomination);
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
    }

    /**
     * Эмуляция вставки банкноты по ее номиналу
     *
     * @param denomination - байт номинала банкноты (смотреть BillTable)
     */
    private void billAcceptance(byte[] denomination) {
        for (Map.Entry<String, byte[]> entry : billTable.entrySet()) {
            if (Arrays.equals(entry.getValue(), denomination)) {
                LOGGER.info(LogCreator.console("bill accept : " + entry.getKey()));
            }
        }
        client.setCurrentDenom(denomination);
        sendEscrowPosition();
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
        if (client.getStatus() == BillStateType.Idling || client.realDeviceConnected()) {
            client.escrowNominal();
        } else {
            LOGGER.warn(LogCreator.console("can not escrow, casher not idling now!"));
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
        try {
            client.close();
        } catch (SerialPortException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        BufferedImage image;
//        try {
//            image = ImageIO.read(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("graphic/emulator_bg.jpg")).getPath()));
//            g.drawImage(image, 0, 0, null);
//        } catch (IOException ex) {
//            LOGGER.error(ex.getMessage(), ex);
//        }
//    }
}
