package ru.app.main.pages;

import ru.app.main.Settings;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

import static ru.app.main.Launcher.optionPage;
import static ru.app.main.Launcher.portsPage;


public class DevicesPage extends JPanel {

    public DevicesPage() {
        JLabel label = formLabel("Choose device for emulatorCommands", 0);
        add(label);
        setVisible(false);
        setLayout(null);
        setSize(Settings.dimension);
        setBackground(new Color(67, 159, 212));

        final String[] devices = new String[]{"CCNET CASHER", "CCTALK COIN"};
        for (int i = 0; i < devices.length; i++) {
            JButton button = new JButton(devices[i]);
            button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
            button.setBounds(Settings.dimension.width / 2 - 160, 80 + i * 80, 320, 60);
            final int emul = i;
            button.addMouseListener(new MouseInputAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Settings.deviceForEmulator = devices[emul];
                    if (emul == 0) {
                        setVisible(false);
                        optionPage.setDescription("Connect the real cashcode machine?");
                        optionPage.setVisible(true);
                    } else {
                        setVisible(false);
                        portsPage.setVisible(true);
                    }
                }
            });
            add(button);
        }
    }

    private JLabel formLabel(String name, int y) {
        JLabel label = new JLabel(name);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        label.setBounds(0, y, Settings.dimension.width, 40);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        return label;
    }
}
