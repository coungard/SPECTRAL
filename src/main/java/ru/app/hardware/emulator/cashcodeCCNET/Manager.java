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
    private static JCheckBox verboseLog;
    private boolean cassetteOut = false;
    private JLabel emul;
    private JLabel casher;
    private JLabel modeLabel = new JLabel("change mode -->>");
    private Map<String, byte[]> billTable;
    private List<JButton> billButtons = new ArrayList<>();
    private JButton encashButton;
    private Requester requester;
    private static final String URL = "http://192.168.15.121:8080/ussdWww/";
    private static final int TIME_OUT = 60000 * 20;
    private static final int ERROR_TIME_OUT = 60000 * 60;

    public Manager(String port) {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        requester = new Requester(URL);
        billTable = new BillTable().getTable();
        struct();
    }

    private void requestLoop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info(LogCreator.console("Request loop started"));

//                startBot();
//
                while (true) {
                    try {
                        Thread.sleep(3000);
                        String response = requester.checkPayment();
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
                                    String bill = nominal + " RUB";
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

    private void startBot() {
        try {
//            Runtime.getRuntime().exec("c:\\starterPy.bat");
            Runtime.getRuntime().exec("cmd /c start \"\" starterPy.bat");
            LOGGER.info(LogCreator.console("Bot started.."));
        } catch (IOException ex) {
            LOGGER.error(LogCreator.console("Can not start Bot!"), ex);
        }
    }

    @Override
    public void struct() {
        JLabel mainLabel = formLabel("EMULATOR CASHCODE CCNET", 0);
        add(mainLabel);

        JButton test = new JButton("test");
        test.setBounds(740, 90, 120, 40);
        test.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Requester.goPay();
            }
        });
        add(test);

        emul = new JLabel();
        emul.setIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("graphic/emulator.gif"))));

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
        encashButton.setBounds(550, 40, 180, 50);
        encashButton.setBackground(new Color(233, 217, 182));
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

        paymentPanel = new JPanel();
        paymentPanel.setBorder(BorderFactory.createTitledBorder("NOTE INSERTION COMMANDS"));
        paymentPanel.setBounds(30, 40, 500, 100);
        paymentPanel.setBackground(new Color(7, 146, 151));
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

        createCatalogPayments();
    }

    private void createCatalogPayments() {
        try {
            if (Files.notExists(Paths.get(Settings.paymentsDir))) {
                Files.createDirectory(Paths.get(Settings.paymentsDir));
            }
            if (Files.notExists(Paths.get(Settings.successDir))) {
                Files.createDirectory(Paths.get(Settings.successDir));
            }
            if (Files.notExists(Paths.get(Settings.errorDir))) {
                Files.createDirectory(Paths.get(Settings.errorDir));
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
            LOGGER.info(LogCreator.console("Can not create payment directory: "));
        }
    }

    @Override
    public void redraw() {
        requestLoop();
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
        JButton bill = new JButton(billName);
        bill.setPreferredSize(new Dimension(100, 30));
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
}
