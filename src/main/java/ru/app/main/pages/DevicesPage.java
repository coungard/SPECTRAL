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
        JLabel label = new JLabel();
        add(label);
        setVisible(false);
        setLayout(null);
        setSize(Settings.dimension);
        setBackground(new Color(67, 159, 212));
    }

    public void redraw(String hardware) {
        setVisible(true);
        formLabel("Choose device for " + hardware, 0);
        switch (hardware) {
            case "EMULATOR":
                final String[] emulDevices = new String[]{"CCNET CASHER", "CCTALK COIN"};
                for (int i = 0; i < emulDevices.length; i++) {
                    JButton button = new JButton(emulDevices[i]);
                    button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
                    button.setBounds(Settings.dimension.width / 2 - 160, 80 + i * 80, 320, 60);
                    final int emul = i;
                    button.addMouseListener(new MouseInputAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            Settings.device = emulDevices[emul];
                            if (emul == 0) {
                                setVisible(false);
                                optionPage.setDescription("Connect the real cashcode machine (Bridge mode) ?");
                                optionPage.setVisible(true);
                            } else {
                                setVisible(false);
                                portsPage.setVisible(true);
                            }
                        }
                    });
                    add(button);
                }
                break;
            case "SMART_SYSTEM":
                final String[] smartDevices = new String[]{"SMART_PAYOUT", "SMART_HOPPER", "BV_20"};
                for (int j = 0; j < smartDevices.length; j++) {
                    JButton button = new JButton(smartDevices[j]);
                    button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
                    button.setBounds(Settings.dimension.width / 2 - 160, 80 + j * 80, 320, 60);
                    final int device = j;
                    button.addMouseListener(new MouseInputAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            Settings.device = smartDevices[device];
                            setVisible(false);
                            portsPage.setVisible(true);
                        }
                    });
                    add(button);
                }
                break;
        }
    }

    private JLabel formLabel(String name, int y) {
        JLabel label = new JLabel(name);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        label.setBounds(0, y, Settings.dimension.width, 40);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        add(label);
        return label;
    }
}
