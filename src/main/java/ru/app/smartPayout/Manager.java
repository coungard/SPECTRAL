package ru.app.smartPayout;

import jssc.SerialPortException;
import ru.listeners.ManagerPanel;
import ru.protocol.Command;
import ru.protocol.Nominal;
import ru.util.Logger;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseEvent;

import static ru.protocol.hopper.HopperCommands.RequestHopperCoin;
import static ru.protocol.payout.PayoutCommands.*;

public class Manager extends JLayeredPane implements ManagerPanel {
    public volatile static JTextArea textArea;
    private Client client;
    private static boolean isEnabled;
    private String[] banknotes = new String[]{"10", "20", "50", "100", "500", "1000"};
    private static final Color BACKGROUND_COLOR = new Color(176, 158, 193);

    public static boolean flag;

    public Manager(String port) throws SerialPortException {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        struct();
    }

    @Override
    public void struct() {
        JLabel cmdLabel = formLabel("CCTalk Smart Payout (CC2)", 0);
        add(cmdLabel);
        textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(30, 140, 960, 400);
        add(scroll);
        JButton enableButton = new JButton("Enable cashment");
        enableButton.setBounds(30, 60, 160, 30);
        enableButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled) {
                    Logger.console("cashmachine is already enabled\n");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.console("ENABLE CASHMACHINE PROCESS STARTED:\n");
                        client.sendMessage(new Command(SimplePoll));
                        pause();
                        client.sendMessage(new Command(RequestStatus));
                        pause();
                        client.sendMessage(new Command(ReadBufferedBillEvents));
                        pause();
                        client.sendMessage(new Command(RequestStatus));
                        pause();
                        client.sendMessage(new Command(SetNoteInhibitStatus, new byte[]{(byte) 0xFF, (byte) 0xFF}));
                        pause();
                        client.sendMessage(new Command(ModifyBillOperatingMode, new byte[0x01]));
                        pause();
                        client.sendMessage(new Command(SetRouting,
                                new byte[]{(byte) 0x00, (byte) 0x88, (byte) 0x13, 0, 0, (byte) 0x49, (byte) 0x54, (byte) 0x4C}));
                        pause();
                        client.sendMessage(new Command(RequestStatus));
                        pause();
                        client.sendMessage(new Command(ModifyMasterInhibit, new byte[]{(byte) 0x01}));
                        pause();
                        client.sendMessage(new Command(SimplePoll));
                        pause();

                        isEnabled = true;
//                        while (isEnabled && !flag) {
                        while (isEnabled) {
                            byte[] res = client.sendMessage(new Command(RequestStatus));
                            pause();
                            if (res.length == 13 && res[4] == 20 && res[11] != 0) {
                                System.out.println("note credit!");
                                pause();
                                client.sendMessage(new Command(RouteBill, new byte[]{0x01}));
                                pause();
                            }
                        }
                    }
                }).start();
            }
        });

        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(230, 60, 160, 30);
        resetButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Logger.console("Start reseting device!");
                client.sendMessage(new Command(ResetDevice));
            }
        });

        JButton disableButton = new JButton("Disable cashment");
        disableButton.setBounds(430, 60, 160, 30);
        disableButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Logger.console("Bill accepting stopped!");
//                isEnabled = false;
                client.sendMessage(new Command(ModifyMasterInhibit, new byte[]{(byte) 0x00}));
            }
        });


        JButton dispenseButton = new JButton("Dispense");
        dispenseButton.setBounds(630, 60, 160, 30);
        dispenseButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isEnabled = false;
                int choosen = JOptionPane.showOptionDialog(null, "Выберите номинал для выдачи", "dispense",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{10, 20, 50, 100, 500, 1000}, null);
                pause();

                byte[] note = new Nominal(banknotes[choosen]).getValue();
                client.sendMessage(new Command(PayoutAmount, note));
            }
        });

        JButton requestHopper = new JButton("Request Hopper");
        requestHopper.setBounds(230, 100, 160, 30);
        requestHopper.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pause();
                client.sendMessage(new Command(RequestHopperCoin));
            }
        });

        JButton testButton = new JButton("Test");
        testButton.setBounds(830, 60, 160, 30);
        testButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // zdes mojet bit vasha reklama
                isEnabled = !isEnabled;
            }
        });

        add(testButton);
        add(enableButton);
        add(disableButton);
        add(resetButton);
        add(dispenseButton);
        add(requestHopper);

        JButton restartButton = new JButton("Restart");
//        restartButton.setBounds();
    }

    private void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private JLabel formLabel(String name, int y) {
        JLabel label = new JLabel(name);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        label.setBounds(0, y, this.getWidth(), 40);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        return label;
    }
}
