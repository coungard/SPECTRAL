package ru.app.listeners;

import jssc.SerialPortException;
import ru.app.main.Launcher;
import ru.app.main.Settings;
import ru.app.protocol.bus.DeviceType;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class PortListener extends MouseInputAdapter {
    private String portName;

    public PortListener(String portName) {
        this.portName = portName;
    }

    public void mousePressed(MouseEvent e) {
        DeviceType hardware = Settings.hardware;
        switch (hardware) {
            case SMART_PAYOUT:
                try {
                    Launcher.defineManager(new ru.app.hardware.smartPayout.Manager(portName));
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
                break;
            case BNE_S110M:
                try {
                    Launcher.defineManager(new ru.app.hardware.bneS110M.Manager(portName));
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
                break;
        }
        Launcher.portsPanel.setVisible(false);
    }
}
