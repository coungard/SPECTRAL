package ru.app.hardware.bneS110M;

import jssc.SerialPortException;
import ru.app.hardware.AbstractManager;
import ru.app.util.Crc16;
import ru.app.util.Logger;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Manager extends AbstractManager {
    private static final Color BACKGROUND_COLOR = new Color(175, 198, 170);
    private Client client;

    public Manager(String port) throws SerialPortException {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        struct();
    }

    @Override
    public void struct() {
        JLabel cmdLabel = formLabel("BNE-S110M", 0);
        add(cmdLabel);

        JButton initButton = new JButton("Initialize");
        initButton.setBounds(30, 40, 150, 30);
        initButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x53, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(initButton);

        JButton restartButton = new JButton("Restart");
        restartButton.setBounds(190, 40, 150, 30);
        restartButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x70, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(restartButton);

        JButton openRecoverDoor = new JButton("Open Door");
        openRecoverDoor.setBounds(350, 40, 150, 30);
        openRecoverDoor.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x22, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(openRecoverDoor);

        JButton closeRecoverDoor = new JButton("Close Door");
        closeRecoverDoor.setBounds(510, 40, 150, 30);
        closeRecoverDoor.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x23, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(closeRecoverDoor);

        JButton setCurrencyRUB = new JButton("Set Currency RUB");
        setCurrencyRUB.setBounds(670, 40, 150, 30);
        setCurrencyRUB.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x01, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3}));
            }
        });
        add(setCurrencyRUB);

        JButton openShutter = new JButton("Open Shutter");
        openShutter.setBounds(350, 75, 150, 30);
        openShutter.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x35, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(openShutter);

        JButton closeShutter = new JButton("Close Shutter");
        closeShutter.setBounds(510, 75, 150, 30);
        closeShutter.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x36, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(closeShutter);

        JButton startMixSort = new JButton("Start Mix Sort");
        startMixSort.setBounds(670, 75, 150, 30);
        startMixSort.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x02, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(startMixSort);

        JButton stopMixSort = new JButton("Stop Mix Sort");
        stopMixSort.setBounds(830, 75, 150, 30);
        stopMixSort.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x03, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(stopMixSort);

        JButton activate = new JButton("Activate Deposits");
        activate.setBounds(30, 110, 150, 30);
        activate.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x1E, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(activate);

        JButton startDeposit = new JButton("Start Deposit");
        startDeposit.setBounds(190, 110, 150, 30);
        startDeposit.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x06, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(startDeposit);

        JButton stopDeposit = new JButton("Stop Deposit");
        stopDeposit.setBounds(350, 110, 150, 30);
        stopDeposit.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x07, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(stopDeposit);

        JButton cashNotes = new JButton("Cash Notes");
        cashNotes.setBounds(510, 110, 150, 30);
        cashNotes.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x08, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(cashNotes);

        JButton dispenseNotes = new JButton("Dispense Notes");
        dispenseNotes.setBounds(670, 110, 150, 30);
        dispenseNotes.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x09, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(dispenseNotes);

        JButton testButton = new JButton("Test");
        testButton.setBounds(830, 40, 150, 30);
        testButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // zdes mojet bit vasha reklama
                Logger.console("BUTTON TEST PRESSED");
            }
        });
        add(testButton);
    }

    @Override
    public void redraw() {
        setVisible(true);
    }

    private byte[] formPacket(byte[] data) {
        byte[] res = new byte[data.length + 2];
        data[20] = Client.counter;
        int checksum = Crc16.crc16(data);

        byte lsb = (byte) (checksum & 0xFF);
        byte msb = (byte) (checksum >>> 8 & 0xFF);

        System.arraycopy(data, 0, res, 0, data.length);
        res[res.length - 2] = lsb;
        res[res.length - 1] = msb;

        return res;
    }
}

