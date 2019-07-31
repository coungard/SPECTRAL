package ru.app.listeners;

import jssc.SerialPortException;
import ru.app.main.Launcher;
import ru.app.main.Settings;
import ru.app.bus.DeviceType;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class PortListener extends MouseInputAdapter {
    private String portName;

    public PortListener(String portName) {
        this.portName = portName;
    }

    public void mousePressed(MouseEvent e) {
        DeviceType hardware = Settings.hardware;
        try {
            switch (hardware) {
                case SMART_PAYOUT:
                    Launcher.defineManager(new ru.app.hardware.smartPayout.Manager(portName));
                    break;
                case BNE_S110M:
                    Launcher.defineManager(new ru.app.hardware.bneS110M.Manager(portName));
                    break;
                case EMULATOR:
                    switch (Settings.deviceForEmulator) {
                        case "CCNET CASHER":
                            Launcher.defineManager(new ru.app.hardware.emulator.cashcodeCCNET.Manager(portName));
                            break;
                        case "CCTALK COIN":
                            Launcher.defineManager(new ru.app.hardware.emulator.coinCCTALK.Manager(portName));
                            break;
                    }

            }
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
        Launcher.portsPanel.setVisible(false);
    }
}
