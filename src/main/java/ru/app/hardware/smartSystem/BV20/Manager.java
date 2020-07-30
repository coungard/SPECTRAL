package ru.app.hardware.smartSystem.BV20;

import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.cctalk.BillTable;
import ru.app.protocol.cctalk.CCTalkCommand;
import ru.app.protocol.cctalk.CCTalkResponse;
import ru.app.protocol.cctalk.Command;
import ru.app.util.LogCreator;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Manager extends AbstractManager {
    private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());
    private static final Color BACKGROUND_COLOR = new Color(108, 152, 223);
    private Client client;
    private volatile boolean interrupted;
    private LinkedHashMap<Integer, String> billsIdTable = new LinkedHashMap<>();
    private LinkedHashMap<String, String> BV20Table = BillTable.getBv20Table();
    private JPanel billIdPanel;

    public Manager(String port) {
        client = new Client(port);
        setSize(1020, 600);
        setBackground(BACKGROUND_COLOR);
        setOpaque(true);
        content();
    }

    @Override
    public void content() {
        JLabel title = formLabel("BV 20 cashmachine (cctalk)", 0);
        add(title);
        final JFrame creditFrame = new JFrame("Credit Frame");

        JPanel creditPanel = new JPanel();
        creditPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        creditPanel.setLayout(new GridLayout(1, 2, 20, 5));

        final JLabel notesLabel = new JLabel("Notes credit: ");
        notesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        final JLabel sumLabel = new JLabel();
        sumLabel.setFont(notesLabel.getFont());
        creditPanel.add(notesLabel);
        creditPanel.add(sumLabel);
        creditFrame.add(creditPanel);
        creditFrame.pack();
        creditFrame.setLocationRelativeTo(null);

        final JButton start = createButton("Start", new Point(30, 50));
        start.setEnabled(false);
        start.setToolTipText("Start BV 20 after requesting Bills ID");
        start.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info(LogCreator.console("Start button pressed"));
                interrupted = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        creditFrame.setVisible(true);
                        sumLabel.setText("0");
                        client.sendMessage(new Command(CCTalkCommand.SetNoteInhibitStatus, new byte[]{(byte) 0xFF, (byte) 0xFF}));
                        client.sendMessage(new Command(CCTalkCommand.ModifyMasterInhibit, new byte[]{1}));
                        int lastCounter = 0;
                        boolean isWorked = false;

                        CCTalkResponse events = client.sendMessage(new Command(CCTalkCommand.ReadBufferedBillEvents));
                        if (events != null && events.getCommand() == 0x00) { // if response ACK
                            byte[] data = events.getData();
                            lastCounter = data[0] & 0xFF; // from byte range [-127:128] to int range [0:255]
                            isWorked = true;
                        }
                        if (isWorked)
                            do {
                                try {
                                    events = client.sendMessage(new Command(CCTalkCommand.ReadBufferedBillEvents));
                                    if (events != null && events.getCommand() == 0x00) { // if response ACK
                                        byte[] data = events.getData();
                                        int counter = data[0] & 0xFF;

                                        if (counter != lastCounter) {
                                            LOGGER.debug(LogCreator.console("Data : " + Arrays.toString(data)));
                                            int step = counter - lastCounter;
                                            if (step < 0) step += 255;
                                            int indexA = 1 + 2 * (step - 1);
                                            int indexB = 2 + 2 * (step - 1);
                                            if (data[indexA] == 0) {
                                                switch (data[indexA + 1]) {
                                                    case 0:
                                                        LOGGER.info(LogCreator.console("Master Inhibit Active"));
                                                        break;
                                                    case 1:
                                                        LOGGER.info(LogCreator.console("Bill returned from escrow"));
                                                        break;
                                                    case 2:
                                                        LOGGER.info(LogCreator.console("Validation Fail"));
                                                        break;
                                                    case 3:
                                                        LOGGER.info(LogCreator.console("Transport Problem"));
                                                        break;
                                                    case 4:
                                                        LOGGER.info(LogCreator.console("Channel Inhibit or Escrow Timeout"));
                                                        break;
                                                    case 6:
                                                        LOGGER.info(LogCreator.console("Unsafe Jam or Note Cleared From Front at Start-up"));
                                                        break;
                                                    default:
                                                        LOGGER.warn(LogCreator.console("Undefined A event!"));
                                                }
                                            } else {
                                                String bill = BV20Table.get(billsIdTable.get(data[indexA] & 0xFF));
                                                switch (data[indexB]) {
                                                    case 0:
                                                        LOGGER.info(LogCreator.console("Bill Credit: " + bill));
                                                        int sum = Integer.parseInt(sumLabel.getText());
                                                        int current = Integer.parseInt(bill);
                                                        sumLabel.setText(String.valueOf(sum + current));
                                                        break;
                                                    case 1:
                                                        LOGGER.info(LogCreator.console("Bill Escrow: " + bill));
                                                        client.sendMessage(new Command(CCTalkCommand.RouteBill, new byte[]{0x01}));
                                                        break;
                                                    default:
                                                        LOGGER.warn(LogCreator.console("Undefined B event!"));
                                                }
                                            }
                                            lastCounter = counter;
                                        }
                                    }
                                    // 1 11 00 00 00 00  Escrow  (10)
                                    // 2 10 11 00 00 00  Stacked (10)
                                    Thread.sleep(200);

                                } catch (InterruptedException ex) {
                                    LOGGER.error(LogCreator.console(ex.getMessage()), ex);
                                }
                            } while (!interrupted);
                        else
                            LOGGER.error(LogCreator.console("Bill Events command not worked!"));
                    }
                }).start();
            }
        });

        JButton stop = createButton("Stop", new Point(250, 50));
        stop.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info(LogCreator.console("Stop button pressed"));
                interrupted = true;
                if (creditFrame.isVisible())
                    creditFrame.dispose();
            }
        });

        JButton requestBillsId = createButton("Request Bills ID", new Point(470, 50));
        requestBillsId.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info(LogCreator.console("Request Bills ID button pressed"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 1; i <= 10; i++) {
                            try {
                                Thread.sleep(300);
                                LOGGER.info(LogCreator.console("Request Bill for ID = " + i));
                                CCTalkResponse response = client.sendMessage(new Command(CCTalkCommand.REQUEST_BILL_ID, new byte[]{(byte) i}));
                                if (response.getCommand() == 0x00) { // ACK
                                    String billType = new String(response.getData(), StandardCharsets.UTF_8);
                                    if (Utils.isRangedAscii(billType))
                                        billsIdTable.put(i, billType);
                                }
                            } catch (InterruptedException ex) {
                                LOGGER.error(LogCreator.console(ex.getMessage()), ex);
                            }
                        }
                        addBillTable();
                        start.setEnabled(true);
                    }
                }).start();
            }
        });
    }

    private void addBillTable() {
        if (billIdPanel != null)
            return;
        billIdPanel = new JPanel();
        for (Map.Entry<Integer, String> entry : billsIdTable.entrySet()) {
            billIdPanel.add(new JLabel("ID: " + entry.getKey())).setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            billIdPanel.add(new JLabel("Type: " + entry.getValue())).setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        }
        GridLayout gridLayout = new GridLayout();
        gridLayout.setRows(billsIdTable.size());
        gridLayout.setColumns(2);
        gridLayout.setHgap(10);
        gridLayout.setVgap(5);
        billIdPanel.setLayout(gridLayout);
        billIdPanel.setSize(300, 25 * billsIdTable.size());
        billIdPanel.setLocation(680, 20);
        billIdPanel.setBorder(BorderFactory.createTitledBorder("Bill ID Table"));
        billIdPanel.setOpaque(false);

        add(billIdPanel);
    }

    private JButton createButton(String text, Point point) {
        JButton button = new JButton(text);
        button.setBounds(point.x, point.y, 200, 40);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        add(button);
        return button;
    }

    private JToggleButton createToggleButton(String name) {
        JToggleButton button = new JToggleButton(name);
        button.setUI(new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return new Color(80, 193, 70);
            }
        });
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        return button;
    }

    @Override
    public void redraw() {
        setVisible(true);
    }

    @Override
    public String getCurrentCommand() {
        return super.getCurrentCommand();
    }

    @Override
    public String getCurrentResponse() {
        return super.getCurrentResponse();
    }

    @Override
    protected void closeAll() {
        client.close();
    }
}
