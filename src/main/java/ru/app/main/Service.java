package ru.app.main;

import jssc.SerialPortList;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import ru.app.bus.DeviceType;
import ru.app.hardware.emulator.cashcodeCCNET.Client;
import ru.app.hardware.emulator.cashcodeCCNET.ManagerListener;
import ru.app.network.Requester;
import ru.app.protocol.ccnet.emulator.BillTable;
import ru.app.util.LogCreator;
import ru.app.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * Специальный сервис для эмулятора, который работает исключительно в командной строке, не используя графический
 * интерфейс.
 */
public class Service {
    public static final Logger LOGGER = Logger.getLogger(Service.class);
    private Scanner scanner = new Scanner(System.in);
    private String portName;
    private Client client;
    private Requester requester;
    private Map<String, byte[]> billTable;

    public static final String URL = Settings.propEmulator.get("url");

    public Service() {
        String log4jPath = System.getProperty("os.name").contains("Linux") ? "log4j.xml" : "log4j_win.xml";
        DOMConfigurator.configure(Objects.requireNonNull(this.getClass().getClassLoader().getResource(log4jPath)));

        LOGGER.info("Spectral emulator service started!");

        LOGGER.info(LogCreator.console("emulator auto launcher starting..."));
        try {
            String emulPort;
            if (!Files.exists(Paths.get(Settings.autoLaunchPropFile))) {
                LOGGER.info("Not found comport for starting.");
                String[] ports = SerialPortList.getPortNames();
                System.out.println("serial port list: ");
                for (int i = 0; i < ports.length; i++) {
                    System.out.println(i + 1 + ": " + ports[i]);
                }
                System.out.print("choose port number: ");
                scanner = new Scanner(System.in);
                int port = scanner.nextInt();
                emulPort = ports[port - 1];
                Utils.saveProp(Collections.singletonMap("port", emulPort), Settings.autoLaunchPropFile);
                LOGGER.info(LogCreator.console("emul port: " + emulPort + " saved for autostart"));
            } else {
                emulPort = Utils.getPropertyFromFile(Settings.autoLaunchPropFile, "port");
            }

            if (emulPort != null) {
                Settings.hardware = DeviceType.EMULATOR;
                Settings.deviceForEmulator = "CCNET CASHER";
                startManager(emulPort);
            } else {
                LOGGER.info("emulPort = null!");
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void startManager(String emulPort) {
        portName = emulPort;
        client = new Client(emulPort, new ManagerListener() {
            @Override
            public void serialPortErrorReports() {
                LOGGER.warn("SERIAL PORT ERROR!");
            }
        });
        requester = new Requester(URL);
        billTable = new BillTable().getTable();
//        struct();

        if (Files.exists(Paths.get(Settings.autoLaunchPropFile))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    startProcess();
                }
            }).start();
        }
        LOGGER.info(LogCreator.console("Client manager started on port: " + portName));
    }
}
