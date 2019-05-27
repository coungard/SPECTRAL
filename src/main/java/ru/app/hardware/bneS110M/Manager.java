package ru.app.hardware.bneS110M;

import jssc.SerialPortException;
import ru.app.listeners.AbstractManager;
import ru.app.util.Crc16;
import ru.app.util.Logger;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Manager extends AbstractManager {
    private static final Color BACKGROUND_COLOR = new Color(103, 193, 140);

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
        initButton.setBounds(30, 50, 160, 30);
        initButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x53, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(initButton);

        JButton restartButton = new JButton("Restart");
        restartButton.setBounds(230, 50, 160, 30);
        restartButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x70, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(restartButton);

        JButton openRecoverDoor = new JButton("Open Door");
        openRecoverDoor.setBounds(430, 50, 160, 30);
        openRecoverDoor.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x22, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(openRecoverDoor);

        JButton closeRecoverDoor = new JButton("Close Door");
        closeRecoverDoor.setBounds(630, 50, 160, 30);
        closeRecoverDoor.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, (byte) 0x23, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(closeRecoverDoor);

        JButton startMixSort = new JButton("Start Mix Sort");
        startMixSort.setBounds(30, 90, 160, 30);
        startMixSort.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(startMixSort);

        JButton stopMixSort = new JButton("Stop Mix Sort");
        stopMixSort.setBounds(230, 90, 160, 30);
        stopMixSort.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            }
        });
        add(stopMixSort);

        JButton setCurrencyRUB = new JButton("Set Currency RUB");
        setCurrencyRUB.setBounds(430, 90, 160, 30);
        setCurrencyRUB.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(formPacket(new byte[]{1, 0, 0, 0, 8, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3}));
            }
        });
        add(setCurrencyRUB);

        JButton testButton = new JButton("Test");
        testButton.setBounds(830, 50, 160, 30);
        testButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // zdes mojet bit vasha reklama
                Logger.console("BUTTON TEST PRESSED");
            }
        });
        add(testButton);
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

