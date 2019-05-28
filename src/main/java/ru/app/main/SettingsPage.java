package ru.app.main;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

import static ru.app.main.Launcher.mainPanel;

class SettingsPage extends JPanel {
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 23);
    private JCheckBox hexLog;
    private JCheckBox bytesLog;
    private JCheckBox asciiLog;

    SettingsPage() {
        setSize(1020, 600);
        setLayout(null);
        setVisible(false);
        setBackground(Color.WHITE);

        JLabel label = new JLabel("General settings", SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        label.setBounds(0, 10, getWidth(), 40);
        add(label);

        JLabel logsLabel = new JLabel("Log level");
        logsLabel.setBounds(20, 70, 300, 50);
        logsLabel.setFont(FONT);
        add(logsLabel);

        hexLog = createCheckBox("Hex", 200, logsLabel.getY(), "logLevel.hex");
        bytesLog = createCheckBox("Bytes", 320, logsLabel.getY(), "logLevel.bytes");
        asciiLog = createCheckBox("Ascii", 440, logsLabel.getY(), "logLevel.ascii");

        JButton save = new JButton("Save");
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
        back.setBounds(780, 500, 200, 40);
        back.setBackground(new Color(252, 220, 206));
        back.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setVisible(false);
                mainPanel.setVisible(true);
            }
        });
        add(back);
    }

    private JCheckBox createCheckBox(String name, int x, int y, String propertyKey) {
        JCheckBox cb = new JCheckBox(name);
        cb.setFont(FONT.deriveFont(Font.PLAIN));
        cb.setBounds(x, y, 120, 50);
        cb.setSelected(Settings.properties.get(propertyKey));
        add(cb);
        return cb;
    }

    private void saveParams() {
        Settings.properties.put("logLevel.hex", hexLog.isSelected());
        Settings.properties.put("logLevel.bytes", bytesLog.isSelected());
        Settings.properties.put("logLevel.ascii", asciiLog.isSelected());
    }
}
