package ru.app.hardware.smartSystem.payout;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.cctalk.Command;
import ru.app.protocol.cctalk.Nominal;
import ru.app.util.LogCreator;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

import static ru.app.protocol.cctalk.payout.PayoutCommands.*;

public class Manager extends AbstractManager {
    private static final Logger LOGGER = Logger.getLogger(Manager.class);
    private static boolean isEnabled;
    private String[] banknotes = new String[]{"10", "20", "50", "100", "500", "1000"};
    private static final Color BACKGROUND_COLOR = new Color(107, 230, 235);
    private Client client;

    public static boolean flag;

    public Manager(String port) throws SerialPortException {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        content();
    }

    @Override
    public void content() {
        JLabel cmdLabel = formLabel("CCTalk Smart Payout (CC2)", 0);
        add(cmdLabel);
        JButton enableButton = new JButton("Enable cashment");
        enableButton.setBounds(30, 50, 160, 30);
        enableButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled) {
                    LOGGER.info(LogCreator.console("cashmachine is already enabled\n"));
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.info(LogCreator.console("ENABLE CASHMACHINE PROCESS STARTED:\n"));
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
        resetButton.setBounds(230, 50, 160, 30);
        resetButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LOGGER.info(LogCreator.console("Start reseting device!"));
                client.sendMessage(new Command(ResetDevice));
            }
        });

        JButton disableButton = new JButton("Disable cashment");
        disableButton.setBounds(430, 50, 160, 30);
        disableButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LOGGER.info(LogCreator.console("Bill accepting stopped!"));
                client.sendMessage(new Command(ModifyMasterInhibit, new byte[]{(byte) 0x00}));
            }
        });


        JButton dispenseButton = new JButton("Dispense");
        dispenseButton.setBounds(630, 50, 160, 30);
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
        requestHopper.setBounds(230, 90, 160, 30);

        JButton testButton = new JButton("Test");
        testButton.setBounds(830, 50, 160, 30);
        testButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LOGGER.info(LogCreator.console("Здесь могла быть ваша реклама!"));
            }
        });

        add(testButton);
        add(enableButton);
        add(disableButton);
        add(resetButton);
        add(dispenseButton);
        add(requestHopper);
    }

    @Override
    public void redraw() {
        setVisible(true);
    }

    @Override
    protected void closeAll() {
        client.close();
    }

    private void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
