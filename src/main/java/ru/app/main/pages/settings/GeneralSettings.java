package ru.app.main.pages.settings;

import org.apache.log4j.Logger;
import ru.app.main.Launcher;
import ru.app.main.Settings;
import ru.app.protocol.ccnet.emulator.response.Identification;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static ru.app.main.Launcher.mainPanel;

public class GeneralSettings extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(GeneralSettings.class);
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 23);
    private final JTextField login;
    private final JTextField imei;
    private final JTextField password;
    private static boolean attention;
    private final JComboBox<Object> softBox;
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
        login.setText(Settings.propEmulator.get("login"));
        requesterPanel.add(login);

        JLabel imeiLabel = new JLabel("imei: ");
        imeiLabel.setFont(loginLabel.getFont());
        imeiLabel.setBounds(40, 70, 90, 40);
        requesterPanel.add(imeiLabel);
        imei = new JTextField(12);
        imei.setBounds(120, 75, 150, 30);
        imei.setFont(login.getFont());
        imei.setHorizontalAlignment(SwingConstants.CENTER);
        imei.setText(Settings.propEmulator.get("imei"));
        requesterPanel.add(imei);

        JLabel passwdLabel = new JLabel("passwd: ");
        passwdLabel.setFont(loginLabel.getFont());
        passwdLabel.setBounds(40, 115, 90, 40);
        requesterPanel.add(passwdLabel);
        password = new JTextField(12);
        password.setBounds(120, 115, 150, 30);
        password.setFont(login.getFont());
        password.setHorizontalAlignment(SwingConstants.CENTER);
        password.setText(Settings.propEmulator.get("passwd"));
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

        JLabel softLabel = new JLabel("CashMachine software settings");
        softLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        softLabel.setBounds(20, 180, 500, 60);
        add(softLabel);

        softBox = new JComboBox<>();
        for (Map.Entry<String, String> entry : Identification.getSoftwareMap().entrySet()) {
            softBox.addItem(entry.getKey() + ": " + entry.getValue());
        }
        for (int i = 0; i < softBox.getItemCount(); i++) {
            String value = (String) softBox.getModel().getElementAt(i);
            if (value.startsWith(Settings.prop.get("casher.soft"))) {
                softBox.setSelectedItem(value);
            }
        }
        softBox.setBounds(20, 230, 500, 40);
        add(softBox);
    }

    private void initialization() {
        try {
            if (Files.notExists(Paths.get(Settings.propDir)))
                Files.createDirectory(Paths.get(Settings.propDir));
            if (Files.notExists(Paths.get(Settings.propFile)))
                Files.createFile(Paths.get(Settings.propFile));
            if (Files.notExists(Paths.get(Settings.propEmulatorFile)))
                Files.createFile(Paths.get(Settings.propEmulatorFile));

            if (Files.notExists(Paths.get(Settings.paymentsDir))) {
                Files.createDirectory(Paths.get(Settings.paymentsDir));
            }
            if (Files.notExists(Paths.get(Settings.successDir))) {
                Files.createDirectory(Paths.get(Settings.successDir));
            }
            if (Files.notExists(Paths.get(Settings.errorDir))) {
                Files.createDirectory(Paths.get(Settings.errorDir));
            }
            if (Files.exists(Paths.get("payments/autoRun")))
                Files.delete(Paths.get("payments/autoRun"));
            if (Files.notExists(Paths.get("payments/loading/")))
                Files.createDirectory(Paths.get("payments/loading/"));
            if (Files.exists(Paths.get("payments/payment"))) {
                Files.copy(Paths.get("payments/payment"), Paths.get("payments/loading/payment_" + System.currentTimeMillis()));
                Files.delete(Paths.get("payments/payment"));
            }
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
            p.load(new FileReader(Settings.propEmulatorFile));
            for (String key : p.stringPropertyNames()) {
                Settings.propEmulator.put(key, p.getProperty(key));
            }

            Properties p2 = new Properties();
            p2.load(new FileReader(Settings.propFile));
            for (String key : p2.stringPropertyNames()) {
                Settings.prop.put(key, p2.getProperty(key));
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void saveParams() {
        Settings.prop.put("logLevel.hex", hexLog.isSelected() ? "1" : "0");
        Settings.prop.put("logLevel.bytes", bytesLog.isSelected() ? "1" : "0");
        Settings.prop.put("logLevel.ascii", asciiLog.isSelected() ? "1" : "0");
        Settings.prop.put("casher.soft", ((String) Objects.requireNonNull(softBox.getSelectedItem())).substring(0, 1));

        Settings.propEmulator.put("login", login.getText());
        Settings.propEmulator.put("imei", imei.getText());
        Settings.propEmulator.put("passwd", password.getText());

        Utils.saveProp(Settings.propEmulator, Settings.propEmulatorFile);
        Utils.saveProp(Settings.prop, Settings.propFile);
    }

    public static boolean isAttention() {
        return attention;
    }
}
