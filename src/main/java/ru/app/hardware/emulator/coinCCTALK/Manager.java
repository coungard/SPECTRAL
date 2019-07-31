package ru.app.hardware.emulator.coinCCTALK;

import ru.app.hardware.AbstractManager;

import javax.swing.*;
import java.awt.*;

public class Manager extends AbstractManager {
    private static final Color BACKGROUND_COLOR = new Color(155, 222, 225);
    private Client client;

    public Manager(String port) {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        struct();
    }

    @Override
    public void struct() {
        JLabel descr = formLabel("COIN MACHINE EMULATOR (CCTALK)", 0);
        add(descr);

        JButton coin1 = new JButton("coin 1");
        // TODO
    }
}
