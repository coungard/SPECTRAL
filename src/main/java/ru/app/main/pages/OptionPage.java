package ru.app.main.pages;

import jssc.SerialPortException;
import ru.app.main.Settings;
import ru.app.util.ConnectionResolver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static ru.app.main.Launcher.portsPage;

public class OptionPage extends JPanel {
    private JLabel descr = new JLabel("DESCRIPTION");
    private JButton okayButton = new JButton("YES");
    private JButton noButton = new JButton("NO");
    private volatile JLabel loading = new JLabel();

    public OptionPage() {
        setLayout(null);
        setBackground(new Color(99, 165, 225));
        setSize(Settings.dimension);
        setVisible(false);

        descr.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        descr.setForeground(Color.BLACK);
        descr.setHorizontalAlignment(SwingConstants.CENTER);
        descr.setBounds(0, 0, getWidth(), 100);
        add(descr);

        add(okayButton);
        add(noButton);

        okayButton.setBounds(200, 150, 200, 70);
        okayButton.setFont(descr.getFont());
        okayButton.setBackground(new Color(143, 224, 225));
        noButton.setBounds(620, 150, 200, 70);
        noButton.setFont(descr.getFont());
        noButton.setBackground(new Color(253, 185, 255));

        loading.setIcon(new ImageIcon("src/main/resources/graphic/loading.gif"));
        loading.setOpaque(false);
        loading.setSize(loading.getIcon().getIconWidth(), loading.getIcon().getIconHeight());
        loading.setLocation(getWidth() / 2 - loading.getWidth() / 2, 240);
        loading.setVisible(false);
        add(loading);

        final JLabel nullCabel = new JLabel();
        nullCabel.setIcon(new ImageIcon("src/main/resources/graphic/null.png"));
        nullCabel.setSize(nullCabel.getIcon().getIconWidth(), nullCabel.getIcon().getIconHeight());
        nullCabel.setLocation(250, 220);
        add(nullCabel);

        okayButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            descr.setText("WAIT CONNECTION RESOLVER...");
                            okayButton.setEnabled(false);
                            noButton.setEnabled(false);
                            nullCabel.setVisible(false);
                            loading.setVisible(true);
                            ConnectionResolver resolver = new ConnectionResolver();
                            Settings.realPortForEmulator = resolver.findCCNetPort();
                            if (Settings.realPortForEmulator != null)
                                descr.setText("PORT FOUND! " + Settings.realPortForEmulator);
                            else
                                descr.setText("PORT NOT FOUND!");
                            Thread.sleep(3000);
                        } catch (InterruptedException | SerialPortException ex) {
                            ex.printStackTrace();
                        }
                        setVisible(false);
                        portsPage.redraw();
                        portsPage.setVisible(true);
                    }
                }).start();
            }
        });

        noButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                portsPage.setVisible(true);
            }
        });
    }

    void setDescription(String text) {
        descr.setText(text);
    }
}
