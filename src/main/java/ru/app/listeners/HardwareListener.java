package ru.app.listeners;

import ru.app.bus.DeviceType;
import ru.app.main.Launcher;
import ru.app.main.Settings;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class HardwareListener extends MouseInputAdapter {
    private DeviceType hardware;

    public HardwareListener(DeviceType hardware) {
        this.hardware = hardware;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Settings.hardware = hardware;
        Launcher.mainPanel.setVisible(false);
        if ("EMULATOR".equals(Settings.hardware.toString()))
            Launcher.devicePanel.setVisible(true);
        else
            Launcher.portsPanel.setVisible(true);
    }
}
