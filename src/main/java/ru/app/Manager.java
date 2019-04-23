package ru.app;

import jssc.SerialPortException;
import jssc.SerialPortList;
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

public class Manager extends Thread {
    public volatile static JTextArea textArea;
    private JFrame window = new JFrame("Spectral" + " (v." + Settings.VERSION + " )");
    private final JPanel mainPanel;
    private JPanel managerPanel;
    private Client manager;
    private static boolean isEnabled;
    private String[] banknotes = new String[]{"10", "20", "50", "100", "500", "1000"};

    public static boolean flag;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Manager();
            }
        });
    }

    private Manager() {
        window.setSize(1020, 600);
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setSize(window.getSize());
        window.add(mainPanel);
        managerPanel = new JPanel();
        managerPanel.setLayout(null);
        managerPanel.setSize(window.getSize());
        managerPanel.setVisible(false);
        window.add(managerPanel);
        init();

        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
    }

    private void init() {
        createMainPage();
        createManagerPage();
    }

    private void createManagerPage() {
        JLabel cmdLabel = formLabel("CCTalk Manager", 0);
        managerPanel.add(cmdLabel);
        textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(30, 140, 960, 400);
        managerPanel.add(scroll);
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
                        manager.sendMessage(new Command(SimplePoll));
                        pause();
                        manager.sendMessage(new Command(RequestStatus));
                        pause();
                        manager.sendMessage(new Command(ReadBufferedBillEvents));
                        pause();
                        manager.sendMessage(new Command(RequestStatus));
                        pause();
                        manager.sendMessage(new Command(SetNoteInhibitStatus, new byte[]{(byte) 0xFF, (byte) 0xFF}));
                        pause();
                        manager.sendMessage(new Command(ModifyBillOperatingMode, new byte[0x01]));
                        pause();
                        manager.sendMessage(new Command(SetRouting,
                                new byte[]{(byte) 0x00, (byte) 0x88, (byte) 0x13, 0, 0, (byte) 0x49, (byte) 0x54, (byte) 0x4C}));
                        pause();
                        manager.sendMessage(new Command(RequestStatus));
                        pause();
                        manager.sendMessage(new Command(ModifyMasterInhibit, new byte[]{(byte) 0x01}));
                        pause();
                        manager.sendMessage(new Command(SimplePoll));
                        pause();

                        isEnabled = true;
//                        while (isEnabled && !flag) {
                        while (isEnabled) {
                            byte[] res = manager.sendMessage(new Command(RequestStatus));
                            pause();
                            if (res.length == 13 && res[4] == 20 && res[11] != 0) {
                                System.out.println("note credit!");
                                pause();
                                manager.sendMessage(new Command(RouteBill, new byte[]{0x01}));
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
                manager.sendMessage(new Command(ResetDevice));
            }
        });

        JButton disableButton = new JButton("Disable cashment");
        disableButton.setBounds(430, 60, 160, 30);
        disableButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Logger.console("Bill accepting stopped!");
//                isEnabled = false;
                manager.sendMessage(new Command(ModifyMasterInhibit, new byte[]{(byte) 0x00}));
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
                manager.sendMessage(new Command(PayoutAmount, note));
            }
        });

        JButton requestHopper = new JButton("Request Hopper");
        requestHopper.setBounds(230, 100, 160, 30);
        requestHopper.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pause();
                manager.sendMessage(new Command(RequestHopperCoin));
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

        managerPanel.add(testButton);
        managerPanel.add(enableButton);
        managerPanel.add(disableButton);
        managerPanel.add(resetButton);
        managerPanel.add(dispenseButton);
        managerPanel.add(requestHopper);
    }

    private void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createMainPage() {
        JLabel label = formLabel("Choose com-port", 0);
        mainPanel.add(label);

        String[] ports = SerialPortList.getPortNames();
        for (int i = 0; i < ports.length; i++) {
            JButton button = new JButton(ports[i]);
            button.setBounds(window.getWidth() / 2 - 110, 60 + i * 60, 220, 40);
            button.addMouseListener(new MyListener(ports[i]));
            mainPanel.add(button);
        }
    }

    private void switchPanel() {
        mainPanel.setVisible(false);
        managerPanel.setVisible(true);
    }

    private class MyListener extends MouseInputAdapter {
        String portName;

        MyListener(String portName) {
            this.portName = portName;
        }

        public void mousePressed(MouseEvent e) {
            try {
                manager = new Client(portName);
                switchPanel();
            } catch (SerialPortException ex) {
                Logger.console("COM PORT CONNECTION ERROR!");
                ex.printStackTrace();
            }
        }
    }

    private JLabel formLabel(String name, int y) {
        JLabel label = new JLabel(name);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        label.setBounds(0, y, window.getWidth(), 40);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        return label;
    }
}
