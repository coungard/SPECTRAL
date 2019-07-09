package ru.app.main;

import jssc.SerialPortList;
import ru.app.hardware.AbstractManager;
import ru.app.listeners.HardwareListener;
import ru.app.listeners.PortListener;
import ru.app.bus.DeviceType;
import ru.app.util.Logger;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Launcher extends Thread {
    private static JFrame window = new JFrame("Spectral" + " (v." + Settings.VERSION + " )");
    public static JPanel mainPanel = new JPanel();
    public static JPanel portsPanel = new JPanel();
    private final JPanel settingsPanel;
    public static AbstractManager currentManager;
    private static final Color BACKGROUND_COLOR = new Color(67, 159, 212);
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 23);

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
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
        window.setSize(1020, 600);

        addPanel(mainPanel);
        addPanel(portsPanel);
        settingsPanel = new SettingsPage();
        window.add(settingsPanel);
        init();

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

    private void init() {
        createMainPage();
        createPortsPage();
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
                settingsPanel.setVisible(true);
            }
        });
        mainPanel.add(settings);
    }

    private void createPortsPage() {
        JLabel label = formLabel("Choose port", 0);
        portsPanel.add(label);

        String[] ports = SerialPortList.getPortNames();
        for (int i = 0; i < ports.length; i++) {
            JButton button = new JButton(ports[i]);
            button.setFont(FONT);
            button.setBounds(window.getWidth() / 2 - 150, 60 + i * 60, 300, 40);
            button.addMouseListener(new PortListener(ports[i]));
            portsPanel.add(button);
        }
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

