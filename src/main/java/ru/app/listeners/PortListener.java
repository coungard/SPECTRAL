package ru.app.listeners;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.bus.DeviceType;
import ru.app.main.Launcher;
import ru.app.main.Settings;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class PortListener extends MouseInputAdapter {
    private static final Logger LOGGER = Logger.getLogger(PortListener.class);
    private String portName;

    public PortListener(String portName) {
        this.portName = portName;
    }

    public void mousePressed(MouseEvent e) {
        DeviceType hardware = Settings.hardware;
        try {
            switch (hardware) {
                case SMART_SYSTEM:
                    switch (Settings.device) {
                        case "SMART_PAYOUT":
                            Launcher.defineManager(new ru.app.hardware.smartSystem.payout.Manager(portName));
                            break;
                        case "SMART_HOPPER":
                            Launcher.defineManager(new ru.app.hardware.smartSystem.hopper.Manager(portName));
                            break;
                        case "BV_20":
                            Launcher.defineManager(new ru.app.hardware.smartSystem.BV20.Manager(portName));
                            break;
                    }
                    break;
                case BNE_S110M:
                    Launcher.defineManager(new ru.app.hardware.bneS110M.Manager(portName));
                    break;
                case EMULATOR:
                    switch (Settings.device) {
                        case "CCNET CASHER":
                            Launcher.defineManager(new ru.app.hardware.emulator.cashcodeCCNET.Manager(portName));
                            break;
                        case "CCTALK COIN":
                            Launcher.defineManager(new ru.app.hardware.emulator.coinCCTALK.Manager(portName));
                            break;
                    }
                    break;
                case ACQUIRING:
                    Launcher.defineManager(new ru.app.hardware.ucs.Manager(portName));
                    break;
            }
        } catch (SerialPortException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        Launcher.portsPage.setVisible(false);
    }
}
