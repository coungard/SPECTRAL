package ru.app.main.pages.settings;

import org.apache.log4j.Logger;
import ru.app.main.Launcher;
import ru.app.main.Settings;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static ru.app.main.Launcher.mainPanel;

public class GeneralSettings extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(GeneralSettings.class);
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 23);
    private final JTextField login;
    private final JTextField imei;
    private final JTextField password;
    private static boolean attention;
    private JCheckBox hexLog;
    private JCheckBox bytesLog;
    private JCheckBox asciiLog;

    public GeneralSettings() {
        setSize(1020, 450);
        setLayout(null);
        setBackground(Color.WHITE);

        initialization();
        loadConfig();

        JLabel label = new JLabel("General settings", SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        label.setBounds(0, 10, getWidth(), 40);
        add(label);

        JLabel logsLabel = new JLabel("Log level:");
        logsLabel.setBounds(20, 70, 300, 50);
        logsLabel.setFont(FONT);
        add(logsLabel);

        hexLog = createCheckBox("Hex", 200, logsLabel.getY(), "logLevel.hex");
        bytesLog = createCheckBox("Bytes", 320, logsLabel.getY(), "logLevel.bytes");
        asciiLog = createCheckBox("Ascii", 440, logsLabel.getY(), "logLevel.ascii");

        JPanel requesterPanel = new JPanel();
        requesterPanel.setLayout(null);
        TitledBorder border = BorderFactory.createTitledBorder("Requester");
        border.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        requesterPanel.setBorder(border);
        requesterPanel.setOpaque(false);
        requesterPanel.setBounds(20, 300, 300, 160);
        add(requesterPanel);

        JLabel loginLabel = new JLabel("login: ");
        loginLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        loginLabel.setBounds(40, 25, 90, 40);
        requesterPanel.add(loginLabel);
        login = new JTextField(12);
        login.setBounds(120, 30, 150, 30);
        login.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 12));
        login.setHorizontalAlignment(SwingConstants.CENTER);
        login.setText(Settings.propEmul.get("login"));
        requesterPanel.add(login);

        JLabel imeiLabel = new JLabel("imei: ");
        imeiLabel.setFont(loginLabel.getFont());
        imeiLabel.setBounds(40, 70, 90, 40);
        requesterPanel.add(imeiLabel);
        imei = new JTextField(12);
        imei.setBounds(120, 75, 150, 30);
        imei.setFont(login.getFont());
        imei.setHorizontalAlignment(SwingConstants.CENTER);
        imei.setText(Settings.propEmul.get("imei"));
        requesterPanel.add(imei);

        JLabel passwdLabel = new JLabel("passwd: ");
        passwdLabel.setFont(loginLabel.getFont());
        passwdLabel.setBounds(40, 115, 90, 40);
        requesterPanel.add(passwdLabel);
        password = new JTextField(12);
        password.setBounds(120, 115, 150, 30);
        password.setFont(login.getFont());
        password.setHorizontalAlignment(SwingConstants.CENTER);
        password.setText(Settings.propEmul.get("passwd"));
        requesterPanel.add(password);

        JButton save = new JButton("Save");
        save.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        save.setBounds(560, 500, 200, 40);
        save.setBackground(new Color(220, 255, 226));
        save.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                saveParams();
                JOptionPane.showMessageDialog(null, "Parameters saved!", "info", JOptionPane.PLAIN_MESSAGE, null);
            }
        });
        add(save);

        JButton back = new JButton("Back");
        back.setFont(save.getFont());
        back.setBounds(780, 500, 200, 40);
        back.setBackground(new Color(252, 220, 206));
        back.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Launcher.settingsPage.setVisible(false);
                mainPanel.setVisible(true);
            }
        });
        add(back);

        if (login.getText().equals("") && imei.getText().equals("") && password.getText().equals("")) {
            attention = true;
        }
    }

    private void initialization() {
        try {
            if (Files.notExists(Paths.get(Settings.propFile)))
                Files.createFile(Paths.get(Settings.propFile));
            if (Files.notExists(Paths.get(Settings.promEmulFile)))
                Files.createFile(Paths.get(Settings.promEmulFile));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private JCheckBox createCheckBox(String name, int x, int y, String propertyKey) {
        JCheckBox cb = new JCheckBox(name);
        cb.setFont(FONT.deriveFont(Font.PLAIN));
        cb.setBounds(x, y, 120, 50);
        cb.setSelected("1".equals(Settings.prop.get(propertyKey)));
        add(cb);
        return cb;
    }

    private void loadConfig() {
        try {
            Properties p = new Properties();
            p.load(new FileReader(Settings.promEmulFile));
            for (String key : p.stringPropertyNames()) {
                Settings.propEmul.put(key, p.getProperty(key));
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void saveParams() {
        Settings.prop.put("logLevel.hex", hexLog.isSelected() ? "1" : "0");
        Settings.prop.put("logLevel.bytes", bytesLog.isSelected() ? "1" : "0");
        Settings.prop.put("logLevel.ascii", asciiLog.isSelected() ? "1" : "0");

        Settings.propEmul.put("login", login.getText());
        Settings.propEmul.put("imei", imei.getText());
        Settings.propEmul.put("passwd", password.getText());

        saveConfig(Settings.propEmul, Settings.promEmulFile);
        saveConfig(Settings.prop, Settings.propFile);
    }

    /**
     * Сохранить настройки в systemCfg
     */
    private static void saveConfig(Map<String, String> prms, String file) {
        try {
            Properties prop = new Properties();
            for (Map.Entry<String, String> e : prms.entrySet()) {
                prop.setProperty(e.getKey(), e.getValue());
            }
            OutputStream os = new FileOutputStream(file);
            prop.store(os, null);
            os.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean isAttention() {
        return attention;
    }
}
