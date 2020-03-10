package ru.app.hardware.smartSystem.BV20;

import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.cctalk.CCTalkCommand;
import ru.app.protocol.cctalk.Command;
import ru.app.util.LogCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Manager extends AbstractManager {
    private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());
    private static final Color BACKGROUND_COLOR = new Color(184, 201, 232);
    private Client client;
    private volatile boolean interrupted;

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

        JButton start = createButton("Start", new Point(40, 50));
        start.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info(LogCreator.console("Start button pressed"));
                interrupted = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        do {
                            client.sendMessage(new Command(CCTalkCommand.SetNoteInhibitStatus, new byte[]{(byte) 0xFF, (byte) 0xFF}));
                            client.sendMessage(new Command(CCTalkCommand.ModifyMasterInhibit, new byte[]{1}));
                            try {
                                byte[] res = client.sendMessage(new Command(CCTalkCommand.ReadBufferedBillEvents));
                                Thread.sleep(400);
                            } catch (InterruptedException ex) {
                                LOGGER.error(LogCreator.console(ex.getMessage()), ex);
                            }
                        } while (!interrupted);
                    }
                }).start();
            }
        });

        JButton stop = createButton("Stop", new Point(300, 50));
        stop.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info(LogCreator.console("Stop button pressed"));
                interrupted = true;
            }
        });

        JButton requestFirmware = createButton("Request Bills ID", new Point(540, 50));
        requestFirmware.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info(LogCreator.console("Request Bills ID button pressed"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 1; i <= 16; i++) {
                            try {
                                Thread.sleep(300);
                                LOGGER.info(LogCreator.console("Request Bill for ID = " + i));
                                client.sendMessage(new Command(CCTalkCommand.REQUEST_BILL_ID, new byte[]{(byte) i}));
                            } catch (InterruptedException ex) {
                                LOGGER.error(LogCreator.console(ex.getMessage()), ex);
                            }
                        }
                    }
                }).start();
            }
        });
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
