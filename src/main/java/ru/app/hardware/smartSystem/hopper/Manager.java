package ru.app.hardware.smartSystem.hopper;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.cctalk.Command;
import ru.app.protocol.cctalk.hopper.HopperCommands;
import ru.app.util.LogCreator;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Formatter;

public class Manager extends AbstractManager {
    private static final Logger LOGGER = Logger.getLogger(Manager.class);
    private static final Color BACKGROUND_COLOR = new Color(233, 236, 238);
    private Client client;

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

        JButton reset = createButton("Reset", new Point(20, 40));
        reset.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new Command(HopperCommands.ResetDevice));
            }
        });

        JButton requestSoftware = createButton("Request Software", new Point(230, 40));
        requestSoftware.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new Command(HopperCommands.SimplePoll));
                pause();
                client.sendMessage(new Command(HopperCommands.RequestEquipmentCategoryID));
                pause();
                client.sendMessage(new Command(HopperCommands.RequestManufacturerID));
                pause();
                client.sendMessage(new Command(HopperCommands.RequestCommsRevision));
                pause();
                client.sendMessage(new Command(HopperCommands.RequestProductCode));
                pause();
                client.sendMessage(new Command(HopperCommands.RequestAddressMode));
                pause();
                client.sendMessage(new Command(HopperCommands.RequestSoftwareRevision));
            }
        });

        JButton getDeviceSetup = createButton("Get Device Setup", new Point(440, 40));
        getDeviceSetup.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new Command(HopperCommands.MC_GET_DEVICE_SETUP));
            }
        });

        JButton payoutAmount = createButton("Payout Amount", new Point(650, 40));
        payoutAmount.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String input = JOptionPane.showInputDialog(null, "Enter sum payout");
                if (!Utils.isNumeric(input)) {
                    LOGGER.warn(LogCreator.console("Invalid sum entired!"));
                } else {
                    byte[] data = buildSum(input);
                    client.sendMessage(new Command(HopperCommands.PAYOUT_AMOUNT, data));
                }
            }
        });
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

    private void pause() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
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
    protected void closeAll() {
        client.close();
    }
}
