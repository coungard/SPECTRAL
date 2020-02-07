package ru.app.hardware.smartSystem.hopper;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.cctalk.Command;
import ru.app.protocol.cctalk.Nominal;
import ru.app.protocol.cctalk.hopper.HopperCommand;
import ru.app.util.LogCreator;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class Manager extends AbstractManager {
    private static final Logger LOGGER = Logger.getLogger(Manager.class);
    private static final Color BACKGROUND_COLOR = new Color(233, 236, 238);
    private Client client;
    private boolean interrupted = false;
    private String[] table = new String[]{"1", "2", "5", "10"};
    private Map<String, Integer> cash = new LinkedHashMap<>();

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

        scroll.setBounds(30, 190, 960, 340);

        JButton reset = createButton("Reset", new Point(40, 40));
        reset.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new Command(HopperCommand.ResetDevice));
            }
        });

        JButton requestSoftware = createButton("Request Software", new Point(230, 40));
        requestSoftware.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new Command(HopperCommand.SimplePoll));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestEquipmentCategoryID));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestManufacturerID));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestCommsRevision));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestProductCode));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestAddressMode));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestSoftwareRevision));
            }
        });

        JButton getDeviceSetup = createButton("Get Device Setup", new Point(420, 40));
        getDeviceSetup.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new Command(HopperCommand.MC_GET_DEVICE_SETUP));
            }
        });

        JButton payoutAmount = createButton("Payout Amount", new Point(610, 40));
        payoutAmount.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String input = JOptionPane.showInputDialog(null, "Enter sum payout");
                if (!Utils.isNumeric(input)) {
                    LOGGER.warn(LogCreator.console("Invalid sum entired!"));
                } else {
                    byte[] data = buildSum(input);
                    client.sendMessage(new Command(HopperCommand.PAYOUT_AMOUNT, data));
                }
            }
        });

        JButton setNoteAmount = createButton("Set Note Amount", new Point(800, 40));
        setNoteAmount.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Nominal nominal = new Nominal("10");
                try {
                    byte[] level = nominal.setLevel(3);
                    client.sendMessage(new Command(HopperCommand.MC_SET_DENOMINATION_AMOUNT, level));
                } catch (IOException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                }
            }
        });

        JButton run = createButton("Run Hopper", new Point(40, 90));
        run.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        interrupted = false;
                        while (!interrupted) {
                            client.sendMessage(new Command(HopperCommand.MC_REQUEST_STATUS));
                            pause(200);
                        }
                    }
                }).start();
            }
        });

        JButton stop = createButton("Stop Hopper", new Point(230, 90));
        stop.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                interrupted = true;
            }
        });

        JButton getNoteAmount = createButton("Get Notes Amount", new Point(420, 90));
        getNoteAmount.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (String sum : table) {
                    Nominal nominal = new Nominal(sum);
                    byte[] amount = client.sendMessage(new Command(HopperCommand.MC_GET_NOTE_AMOUNT, nominal.getValue()));
                    // todo... parse amount
                }
            }
        });

        cash.put("1", 0);
        cash.put("2", 0);
        cash.put("5", 0);
        cash.put("10", 0);
    }

    private byte[] buildSum(String input) {
        byte[] res = new byte[4];
        double d = Double.parseDouble(input);
        int x = (int) (d * 100);
        String hex = new Formatter().format("%08X", x).toString();
        String nominal = Utils.inverse(hex);

        int temp = 0;
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) Long.parseLong(nominal.substring(temp, temp + 2), 16);
            temp = temp + 2;
        }
        return res;
    }

    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
    }

    private JButton createButton(String text, Point point) {
        JButton button = new JButton(text);
        button.setBounds(point.x, point.y, 180, 40);
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
    protected void closeAll() {
        client.close();
    }

    @Override
    public String getCurrentCommand() {
        return client.getCurrentCommand();
    }
}
