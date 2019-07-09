package ru.app.hardware.emulator;

import jssc.SerialPortException;
import ru.app.hardware.AbstractManager;

import javax.swing.*;
import java.awt.*;

public class Manager extends AbstractManager {
    private static boolean isEnabled;
    private static final Color BACKGROUND_COLOR = new Color(205, 186, 116);
    private Client client;

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
        JLabel mainLabel = formLabel("EMULATOR CASHCODE CCNET", 0);
        add(mainLabel);
    }

    private void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
