package ru.app.hardware.emulator.cashcodeCCNET;

import org.xml.sax.SAXException;
import ru.app.hardware.AbstractManager;
import ru.app.network.Helper;
import ru.app.network.Payment;
import ru.app.network.Requester;
import ru.app.network.Status;
import ru.app.protocol.ccnet.BillStateType;
import ru.app.protocol.ccnet.emulator.BillTable;
import ru.app.util.Logger;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;


/**
 * <p>Менеджер для работы с эмулятором купюроприемника по протоколу CCNET.</p> <br> В данный менеджер заложены эмуляции вставки банкнот, снятие
 * кассеты (инкассация) и специальный параметр для эмулятора - переход между обычным режимом и мостом, если bridge mode был заранее активирован.
 */
public class Manager extends AbstractManager {
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
    private static final String PAYMENT_PATH = "emulator/payment";
    private static final int TIME_OUT = 60000 * 5;

    public Manager(String port) {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        requester = new Requester(URL);
        billTable = new BillTable().getTable();
        struct();
        requestLoop();
    }

    private void requestLoop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        String response = requester.check();
                        boolean isCommand = response != null && response.contains("command");

                        if (isCommand) {
                            Logger.console("Request payment:\n" + response);
                            if (!emul.isVisible()) {
                                Logger.console("Can not start payment procedure, emulator is not activated!");
                                return;
                            }
                            Payment payment = Helper.createPayment(response);
                            if (payment.getSum() > 0) {
                                long activity = System.currentTimeMillis();
                                Logger.console("Starting payment operation from server...");
                                for (JButton billButton : billButtons)
                                    billButton.setEnabled(false);

                                Map<String, String> prop = new HashMap<>();
                                prop.put("number", payment.getNumber());
                                prop.put("text", payment.getText());
                                prop.put("id", "" + payment.getId());
                                prop.put("sum", "" + payment.getSum());
                                prop.put("status", "ACCEPTED");

                                StringBuilder filePath = new StringBuilder();
                                String sep = System.getProperty("file.separator");
                                filePath.append(FileSystemView.getFileSystemView().getHomeDirectory().toString()).append(sep);
                                filePath.append(PAYMENT_PATH);
                                new File(filePath.toString());
                                Helper.saveProp(prop, filePath.toString());

                                Status status = Status.ACCEPTED;
                                String old = "";
                                do {
                                    Thread.sleep(400);
                                    Map<String, String> data = Helper.loadProp(filePath.toString());
                                    String current = data.get("status");
                                    if (current != null) {
                                        status = Status.fromString(current);
                                        if (!current.equals(old)) {
                                            activity = System.currentTimeMillis();
                                            Logger.console("Current Payment Status : " + status.toString());
                                        }
                                    }
                                    old = current;
                                } while (status != Status.COMPLETED && System.currentTimeMillis() - activity < TIME_OUT || status != Status.ERROR);

                                if (status == Status.ERROR) {
                                    Logger.console("Received Error STatus! Break Requster for " + TIME_OUT / 1000 + " seconds");
                                    Thread.sleep(TIME_OUT);
                                    continue;
                                }
                                List<Integer> nominals = Utils.calculatePayment(payment.getSum());
                                for (Integer nominal : nominals) {
                                    Thread.sleep(4000);
                                    String bill = nominal + " RUB";
                                    billAcceptance(billTable.get(bill));
                                }
                                Thread.sleep(2000);
                                prop.put("status", Status.STACKED.toString());
                                Helper.saveProp(prop, filePath.toString());
                                Thread.sleep(2000);
                            }
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        Logger.console("REQUESTER IS CRASHED!");
                        break;
                    } catch (SAXException | ParserConfigurationException e) {
                        e.printStackTrace();
                        Logger.console("Can not parse Payment Response!");
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

        emul = new JLabel();
        emul.setIcon(new ImageIcon("src/main/resources/graphic/emulator.gif"));
        emul.setSize(emul.getIcon().getIconWidth(), emul.getIcon().getIconHeight());
        emul.setLocation(865, 70);
        emul.setVisible(false);
        add(emul);

        casher = new JLabel();
        casher.setIcon(new ImageIcon("src/main/resources/graphic/casher.gif"));
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

        if (client.readDeviceConnected()) {
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

    /**
     * Эмуляция вставки банкноты по ее номиналу
     *
     * @param denomination - байт номинала банкноты (смотреть BillTable)
     */
    private void billAcceptance(byte[] denomination) {
        for (Map.Entry<String, byte[]> entry : billTable.entrySet()) {
            if (Arrays.equals(entry.getValue(), denomination)) {
                Logger.console("bill accept : " + entry.getKey());
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
        if (client.getStatus() == BillStateType.Idling || client.readDeviceConnected()) {
            client.escrowNominal();
        } else {
            Logger.console("can not escrow, casher not idling now!");
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
}
