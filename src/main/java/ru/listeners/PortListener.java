package ru.listeners;

import jssc.SerialPortException;
import ru.app.main.Launcher;
import ru.app.smartPayout.Client;
import ru.app.main.Settings;
import ru.app.smartPayout.Manager;
import ru.util.Logger;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class PortListener extends MouseInputAdapter {
    String portName;

    public PortListener(String portName) {
        this.portName = portName;
    }

    public void mousePressed(MouseEvent e) {
        String hardware = Settings.hardware;
        switch (hardware) {
            case "Smart Payout":
                try {
                    Launcher.defineManager(new Manager(portName));
                } catch (SerialPortException e1) {
                    e1.printStackTrace();
                }
                break;
            case "BNE-S110M":
                break;
        }
        Launcher.portsPanel.setVisible(false);
    }
}
