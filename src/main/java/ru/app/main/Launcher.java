package ru.app.main;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import ru.app.bus.DeviceType;
import ru.app.hardware.AbstractManager;
import ru.app.listeners.HardwareListener;
import ru.app.main.pages.DevicesPage;
import ru.app.main.pages.OptionPage;
import ru.app.main.pages.PortsPage;
import ru.app.main.pages.SettingsPage;
import ru.app.main.pages.settings.GeneralSettings;
import ru.app.util.LogCreator;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Objects;

public class Launcher extends Thread {
    private static final Logger LOGGER = Logger.getLogger(Launcher.class);
    private static JFrame window = new JFrame("Spectral" + " (v." + Settings.VERSION + " )");
    public static JPanel mainPanel = new JPanel();
    public static PortsPage portsPage = new PortsPage();
    public static DevicesPage devicesPage = new DevicesPage();
    public static OptionPage optionPage = new OptionPage();
    public static SettingsPage settingsPage = new SettingsPage();
    public static AbstractManager currentManager;
    private static final Color BACKGROUND_COLOR = new Color(67, 159, 212);
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 23);

    public static void main(String[] args) throws UnsupportedLookAndFeelException, InvocationTargetException, InterruptedException, RemoteException {
        Settings.args = args;
        if (args.length > 0 && (args[0].equals("--service") || args[1].equals("--service"))) {
            if (args.length > 1 && (args[0].equals("mock") || args[1].equals("mock"))) {
                new RmiServer(true);
            } else {
                new RmiServer(false);
            }
        } else {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    new Launcher();
                }
            });
        }
    }

    public static void defineManager(AbstractManager manager) {
        currentManager = manager;
        LogCreator.init();
        window.add(manager);
        manager.redraw();
    }

    private Launcher() {
        String log4jPath = System.getProperty("os.name").contains("Linux") ? "log4j.xml" : "log4j_win.xml";
        DOMConfigurator.configure(Objects.requireNonNull(this.getClass().getClassLoader().getResource(log4jPath)));

        LOGGER.info(LogCreator.console("Spectral v." + Settings.VERSION + " started"));
        window.setSize(Settings.dimension);

        addPanel(mainPanel);
        window.add(portsPage);
        window.add(devicesPage);
        window.add(optionPage);
        window.add(settingsPage);

        createMainPage();

        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setLocationRelativeTo(null);

        if (Files.exists(Paths.get(Settings.autoLaunchPropFile))) {
            launchEmulator();
        }
    }

    private void launchEmulator() {
        LOGGER.info(LogCreator.console("emulator auto launcher starting..."));
        try {
            String emulPort = Utils.getPropertyFromFile(Settings.autoLaunchPropFile, "port");

            if (emulPort != null) {
                mainPanel.setVisible(false);
                Settings.hardware = DeviceType.EMULATOR;
                Settings.device = "CCNET CASHER";
                defineManager(new ru.app.hardware.emulator.cashcodeCCNET.Manager(emulPort));
            } else {
                LOGGER.info("emulPort = null!");
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void addPanel(JPanel panel) {
        panel.setLayout(null);
        panel.setSize(window.getSize());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setVisible(false);
        window.add(panel);
    }

    private void createMainPage() {
        JLabel label = formLabel("Choose hardware", 0);
        mainPanel.setVisible(true);
        mainPanel.add(label);

        DeviceType[] hw = DeviceType.values();
        for (int i = 0; i < hw.length; i++) {
            JButton button = new JButton(hw[i].name());
            button.setFont(FONT);
            button.setBounds(window.getWidth() / 2 - 160, 80 + i * 80, 320, 60);
            button.addMouseListener(new HardwareListener(hw[i]));
            if (hw[i].equals(DeviceType.EMULATOR)) {
                button.setBackground(new Color(133, 221, 238));
            }
            mainPanel.add(button);
        }

        JButton settings = new JButton("Settings");
        settings.setFont(FONT);
        settings.setBackground(Color.BLACK);
        settings.setForeground(Color.WHITE);
        if (GeneralSettings.isAttention())
            settings.setForeground(Color.RED);
        settings.setBounds(20, 500, 320, 50);
        settings.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mainPanel.setVisible(false);
                settingsPage.setVisible(true);
            }
        });

        JButton exit = new JButton("Exit");
        exit.setFont(FONT);
        exit.setBackground(new Color(156, 30, 198));
        exit.setForeground(Color.WHITE);
        exit.setBounds(Settings.dimension.width - 200, Settings.dimension.height - 100, 180, 50);
        exit.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LOGGER.info("Exit application");
                System.exit(0);
            }
        });

        mainPanel.add(exit);
        mainPanel.add(settings);
    }

    private JLabel formLabel(String name, int y) {
        JLabel label = new JLabel(name);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        label.setBounds(0, y, window.getWidth(), 40);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        return label;
    }
}

