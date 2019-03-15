import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

public class App {
    private JFrame window = new JFrame("Spectral" + " (v." + Settings.VERSION + " )");
    private final JPanel mainPanel;
    private JPanel managerPanel;
    private Manager manager;
    volatile static JTextArea textArea;
    private static boolean isEnabled;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App();
            }
        });
    }

    private App() {
        window.setSize(600, 400);
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
        JLabel cmdLabel = formLabel("CashMachine Manager", 0);
        managerPanel.add(cmdLabel);
        textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setAutoscrolls(true);
        scroll.setBounds(30, 140, 520, 200);
        managerPanel.add(scroll);
        JButton enableButton = new JButton("Enable cashment");
        enableButton.setBounds(30, 60, 160, 30);
        enableButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled) {
                    textArea.setText(textArea.getText() + "cashmachine is already enabled\n");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        textArea.setText(textArea.getText() + "ENABLE CASHMACHINE PROCESS STARTED:\n");
                        manager.sendMessage(new CCTalkCommand(CCTalkCommandType.SimplePoll));
                        pause();
                        manager.sendMessage(new CCTalkCommand(CCTalkCommandType.ReadBufferedBillEvents));
                        pause();
                        manager.sendMessage(new CCTalkCommand(CCTalkCommandType.ModifyBillOperatingMode, new byte[]{(byte) 0x01}));
                        pause();
                        manager.sendMessage(new CCTalkCommand(CCTalkCommandType.ModifyInhibitStatus, new byte[]{(byte) 0xFF, (byte) 0xFF}));
                        pause();
                        manager.sendMessage(new CCTalkCommand(CCTalkCommandType.ModifyMasterInhibit, new byte[]{(byte) 0x01}));
                        pause();

                        isEnabled = true;
                        while (isEnabled) {
                                manager.sendMessage(new CCTalkCommand(CCTalkCommandType.ReadBufferedBillEvents));
                                pause();
                        }
                    }
                }).start();
            }
        });

        JButton disableButton = new JButton("Disable cashment");
        disableButton.setBounds(410, 60, 160, 30);
        disableButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isEnabled = false;
                textArea.setText(textArea.getText() + "Bill accepting stopped\n");
            }
        });

        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(220, 60, 160, 30);
        resetButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                manager.sendMessage(new CCTalkCommand(CCTalkCommandType.ResetDevice));
            }
        });

        JButton testButton = new JButton("Test");
        testButton.setBounds(30, 100, 160, 30);
        testButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                manager.sendMessage(new CCTalkCommand(CCTalkCommandType.REQ_ManufacturerId));
            }
        });

        managerPanel.add(testButton);
        managerPanel.add(enableButton);
        managerPanel.add(disableButton);
        managerPanel.add(resetButton);
    }

    private void pause() {
        try {
            Thread.sleep(1500);
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
                manager = new Manager(portName);
                switchPanel();
            } catch (SerialPortException e1) {
                System.out.println("COM PORT CONNECTION ERROR!");
                e1.printStackTrace();
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
