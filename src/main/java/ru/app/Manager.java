package ru.app;

import jssc.SerialPortException;
import jssc.SerialPortList;
import ru.protocol.Command;
import ru.protocol.CommandType;
import ru.util.Logger;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Manager {
    public volatile static JTextArea textArea;
    private JFrame window = new JFrame("Spectral" + " (v." + Settings.VERSION + " )");
    private final JPanel mainPanel;
    private JPanel managerPanel;
    private Client manager;
    private static boolean isEnabled;

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
                        manager.sendMessage(new Command(CommandType.SimplePoll));
                        pause();
                        manager.sendMessage(new Command(CommandType.RequestStatus));
                        pause();
                        manager.sendMessage(new Command(CommandType.ReadBufferedBillEvents));
                        pause();
                        manager.sendMessage(new Command(CommandType.RequestStatus));
                        pause();
                        manager.sendMessage(new Command(CommandType.ModifyBillOperatingMode, new byte[]{(byte) 0x01}));
                        pause();
                        manager.sendMessage(new Command(CommandType.RequestStatus));
                        pause();
                        manager.sendMessage(new Command(CommandType.ModifyInhibitStatus, new byte[]{(byte) 0xFF, (byte) 0xFF}));
                        pause();
                        manager.sendMessage(new Command(CommandType.RequestStatus));
                        pause();
                        manager.sendMessage(new Command(CommandType.ModifyMasterInhibit, new byte[]{(byte) 0x01}));
                        pause();
                        manager.sendMessage(new Command(CommandType.SimplePoll));
                        pause();

                        isEnabled = true;
                        while (isEnabled) {
                            manager.sendMessage(new Command(CommandType.RequestStatus));
                            pause();
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
                manager.sendMessage(new Command(CommandType.ResetDevice));
            }
        });

        JButton disableButton = new JButton("Disable cashment");
        disableButton.setBounds(430, 60, 160, 30);
        disableButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Logger.console("Bill accepting stopped!");
//                isEnabled = false;
                manager.sendMessage(new Command(CommandType.ModifyMasterInhibit, new byte[]{(byte) 0x00}));
            }
        });


        JButton dispenseButton = new JButton("Dispense");
        dispenseButton.setBounds(630, 60, 160, 30);
        dispenseButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
//                manager.sendMessage(new Command(CommandType.ModifyMasterInhibit, new byte[]{(byte) 0x00}));
                isEnabled = false;
                pause();
                manager.sendMessage(new Command(CommandType.PayoutAmount, new byte[]{0, 0x15, (byte) 0xD0, 7, 0, 0, 0x49, 0x54, (byte) 0x4C, 0x19}));
                pause();
                manager.sendMessage(new Command(CommandType.PayoutAmount, new byte[]{1}));
//                manager.sendMessage(new Command(CommandType.PayoutByDenomination, new byte[]{0, 0x15, (byte) 0xD0, 7, 0, 0, 0x49, 0x54, (byte) 0x4C, 0x19}));
//                pause();
//                manager.sendMessage(new Command(CommandType.PayoutByDenominationCurrent, new byte[]{0, 0x15, (byte) 0xD0, 7, 0, 0, 0x49, 0x54, (byte) 0x4C, 0x19}));
            }
        });

        JButton testButton = new JButton("Test");
        testButton.setBounds(830, 60, 160, 30);
        testButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // zdes mojet bit vasha reklama
                isEnabled = false;
                pause();
                manager.sendMessage(new Command(CommandType.PayoutByDenominationCurrent, new byte[]{0, 0, (byte) 0x13, (byte) 0x88}));
                pause();
                manager.sendMessage(new Command(CommandType.PayoutByDenominationCurrent, new byte[]{0, 0, (byte) 0x07, (byte) 0xD0}));

//                manager.sendMessage(new Command(CommandType.GetDenominationAmount, new byte[]{(byte) 0x88, (byte) 0x13, 0, 0}));
//                pause();
//                manager.sendMessage(new Command(CommandType.GetDenominationAmount, new byte[]{(byte) 0xD0, (byte) 0x07, 0, 0}));
            }
        });

        managerPanel.add(testButton);
        managerPanel.add(enableButton);
        managerPanel.add(disableButton);
        managerPanel.add(resetButton);
        managerPanel.add(dispenseButton);
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
