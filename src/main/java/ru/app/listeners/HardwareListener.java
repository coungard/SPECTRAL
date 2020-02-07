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
        String hw = Settings.hardware.toString();
        if (hw.equals("EMULATOR") || hw.equals("SMART_SYSTEM")) {
            Launcher.devicesPage.redraw(hw);
        } else {
            Launcher.portsPage.setVisible(true);
        }
    }
}
