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
    private final JTextField url;
    private final JTextField statusTimeout;
    private final JTextField nominalTimeout;
    private final JTextField requesterTimeout;
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
        requesterPanel.setBounds(20, 250, 600, 220);
        add(requesterPanel);

        JLabel loginLabel = new JLabel("login: ");
        loginLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        loginLabel.setBounds(20, 25, 90, 40);
        requesterPanel.add(loginLabel);
        login = new JTextField(12);
        login.setBounds(100, 30, 150, 30);
        login.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 12));
        login.setHorizontalAlignment(SwingConstants.CENTER);
        login.setText(Settings.propEmulator.get("login"));
        requesterPanel.add(login);

        JLabel imeiLabel = new JLabel("imei: ");
        imeiLabel.setFont(loginLabel.getFont());
        imeiLabel.setBounds(20, 70, 90, 40);
        requesterPanel.add(imeiLabel);
        imei = new JTextField(12);
        imei.setBounds(100, 75, 150, 30);
        imei.setFont(login.getFont());
        imei.setHorizontalAlignment(SwingConstants.CENTER);
        imei.setText(Settings.propEmulator.get("imei"));
        requesterPanel.add(imei);

        JLabel passwdLabel = new JLabel("passwd: ");
        passwdLabel.setFont(loginLabel.getFont());
        passwdLabel.setBounds(20, 115, 90, 40);
        requesterPanel.add(passwdLabel);
        password = new JTextField(12);
        password.setBounds(100, 115, 150, 30);
        password.setFont(login.getFont());
        password.setHorizontalAlignment(SwingConstants.CENTER);
        password.setText(Settings.propEmulator.get("passwd"));
        requesterPanel.add(password);

        JLabel urlLabel = new JLabel("url: ");
        urlLabel.setFont(loginLabel.getFont());
        urlLabel.setBounds(20, 160, 90, 40);
        requesterPanel.add(urlLabel);
        url = new JTextField();
        url.setBounds(70, 160, 310, 35);
        url.setFont(login.getFont());
        url.setHorizontalAlignment(SwingConstants.CENTER);
        url.setText(Settings.propEmulator.get("url"));
        requesterPanel.add(url);

        JLabel statusTimeoutLabel = new JLabel("status T/O: ");
        statusTimeoutLabel.setFont(loginLabel.getFont());
        statusTimeoutLabel.setBounds(280, 25, 140, 40);
        statusTimeoutLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        requesterPanel.add(statusTimeoutLabel);
        statusTimeout = new JTextField();
        statusTimeout.setBounds(440, 30, 150, 30);
        statusTimeout.setFont(login.getFont());
        statusTimeout.setHorizontalAlignment(SwingConstants.CENTER);
        statusTimeout.setText(Settings.propEmulator.get("timeout.status"));
        requesterPanel.add(statusTimeout);

        JLabel nominalTimeoutLabel = new JLabel("nominals T/O: ");
        nominalTimeoutLabel.setFont(loginLabel.getFont());
        nominalTimeoutLabel.setBounds(280, 70, 140, 40);
        nominalTimeoutLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        requesterPanel.add(nominalTimeoutLabel);
        nominalTimeout = new JTextField();
        nominalTimeout.setBounds(440, 75, 150, 30);
        nominalTimeout.setFont(login.getFont());
        nominalTimeout.setHorizontalAlignment(SwingConstants.CENTER);
        nominalTimeout.setText(Settings.propEmulator.get("timeout.nominals"));
        requesterPanel.add(nominalTimeout);

        JLabel requesterTimeoutLabel = new JLabel("requester T/O: ");
        requesterTimeoutLabel.setFont(loginLabel.getFont());
        requesterTimeoutLabel.setBounds(280, 115, 140, 40);
        requesterTimeoutLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        requesterPanel.add(requesterTimeoutLabel);
        requesterTimeout = new JTextField();
        requesterTimeout.setBounds(440, 115, 150, 30);
        requesterTimeout.setFont(login.getFont());
        requesterTimeout.setHorizontalAlignment(SwingConstants.CENTER);
        requesterTimeout.setText(Settings.propEmulator.get("timeout.requester"));
        requesterPanel.add(requesterTimeout);

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
        softLabel.setBounds(20, 130, 500, 60);
        add(softLabel);

        softBox = new JComboBox<>();
        for (Map.Entry<String, String> entry : Identification.getSoftwareMap().entrySet()) {
            softBox.addItem(entry.getKey() + ": " + entry.getValue());
        }
        String soft = Settings.propEmulator.get("casher.soft");
        for (int i = 0; i < softBox.getItemCount(); i++) {
            String value = (String) softBox.getModel().getElementAt(i);
            if (value.split(":")[0].equals(soft)) {
                softBox.setSelectedItem(value);
            }
        }
        softBox.setBounds(20, 180, 500, 40);
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

        Settings.propEmulator.put("casher.soft", ((String) Objects.requireNonNull(softBox.getSelectedItem())).split(":")[0]);
        Settings.propEmulator.put("login", login.getText());
        Settings.propEmulator.put("imei", imei.getText());
        Settings.propEmulator.put("passwd", password.getText());
        Settings.propEmulator.put("url", url.getText());
        Settings.propEmulator.put("timeout.status", statusTimeout.getText());
        Settings.propEmulator.put("timeout.nominals", nominalTimeout.getText());
        Settings.propEmulator.put("timeout.requester", requesterTimeout.getText());

        Utils.saveProp(Settings.propEmulator, Settings.propEmulatorFile);
        Utils.saveProp(Settings.prop, Settings.propFile);
    }

    public static boolean isAttention() {
        return attention;
    }
}
