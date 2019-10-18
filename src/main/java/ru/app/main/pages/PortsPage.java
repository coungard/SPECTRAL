package ru.app.main.pages;

import jssc.SerialPortList;
import ru.app.listeners.PortListener;
import ru.app.main.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PortsPage extends JPanel {
    private final JLabel casher;
    private JLabel mainLabel;
    private List<JButton> buttonList = new ArrayList<>();
    private JLabel sorry = new JLabel();

    public PortsPage() {
        setLayout(null);
        setVisible(false);
        setSize(Settings.dimension);
        setBackground(new Color(67, 159, 212));

        mainLabel = formLabel("Choose port", 0);
        add(mainLabel);

        casher = new JLabel();
        casher.setIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("graphic/cashcode.png"))));
        casher.setSize(casher.getIcon().getIconWidth(), casher.getIcon().getIconHeight());
        casher.setVisible(false);
        add(casher);

        sorry.setIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("graphic/sorry.png"))));
        sorry.setSize(sorry.getIcon().getIconWidth(), sorry.getIcon().getIconHeight());
        sorry.setLocation(getWidth() / 2 - sorry.getWidth() / 2, 300);
        sorry.setVisible(false);
        add(sorry);

        init();
    }

    private void init() {
        String[] ports = SerialPortList.getPortNames();
        if (ports.length == 0) {
            sorry.setVisible(true);
            mainLabel.setText("Sorry, you don't have a com-ports");
        }
        for (int i = 0; i < ports.length; i++) {
            JButton button = new JButton(ports[i]);
            button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
            button.setBounds(Settings.dimension.width / 2 - 150, 60 + i * 90, 300, 60);
            button.addMouseListener(new PortListener(ports[i]));
            buttonList.add(button);
            add(button);
        }
    }

    void redraw() {
        casher.setVisible(false);
        for (JButton button : buttonList) {
            if (button.getText().equals(Settings.realPortForEmulator)) {
                button.setEnabled(false);
                casher.setVisible(true);
                casher.setLocation(button.getX() + button.getWidth() + 10, button.getY() - 30);
            } else {
                button.setEnabled(true);
            }
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
