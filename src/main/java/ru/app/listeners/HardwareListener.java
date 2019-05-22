package ru.app.listeners;

import ru.app.main.Launcher;
import ru.app.main.Settings;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class HardwareListener extends MouseInputAdapter {
    private String hardware;

    public HardwareListener(String hardware) {
        this.hardware = hardware;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Settings.hardware = hardware;
        Launcher.mainPanel.setVisible(false);
        Launcher.portsPanel.setVisible(true);
    }
}
