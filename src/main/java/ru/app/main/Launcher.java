package ru.app.main;

import ru.app.bus.DeviceType;
import ru.app.hardware.AbstractManager;
import ru.app.listeners.HardwareListener;
import ru.app.main.pages.DevicesPage;
import ru.app.main.pages.OptionPage;
import ru.app.main.pages.PortsPage;
import ru.app.main.pages.SettingsPage;
import ru.app.util.Logger;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Launcher extends Thread {
    private static JFrame window = new JFrame("Spectral" + " (v." + Settings.VERSION + " )");
    public static JPanel mainPanel = new JPanel();
    public static PortsPage portsPage = new PortsPage();
    public static DevicesPage devicesPage = new DevicesPage();
    public static OptionPage optionPage = new OptionPage();
    private final JPanel settingsPage = new SettingsPage();
    public static AbstractManager currentManager;
    private static final Color BACKGROUND_COLOR = new Color(67, 159, 212);
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 23);

    public static void main(String args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Launcher();
            }
        });
    }

    public static void defineManager(AbstractManager manager) {
        currentManager = manager;
        Logger.init();
        window.add(manager);
    }

    private Launcher() {
        window.setSize(Settings.dimension);

        addPanel(mainPanel);
        window.add(portsPage);
        window.add(devicesPage);
        window.add(optionPage);
        window.add(settingsPage);

        createMainPage();

        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
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

        String[] hw = new String[]{"SMART_PAYOUT", "BNE_S110M", "EMULATOR"};
        for (int i = 0; i < hw.length; i++) {
            JButton button = new JButton(hw[i]);
            button.setFont(FONT);
            button.setBounds(window.getWidth() / 2 - 160, 80 + i * 80, 320, 60);
            button.addMouseListener(new HardwareListener(DeviceType.valueOf(hw[i])));
            mainPanel.add(button);
        }

        JButton settings = new JButton("Settings");
        settings.setFont(FONT);
        settings.setBackground(Color.BLACK);
        settings.setForeground(Color.WHITE);
        settings.setBounds(20, 500, 320, 50);
        settings.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mainPanel.setVisible(false);
                settingsPage.setVisible(true);
            }
        });
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

