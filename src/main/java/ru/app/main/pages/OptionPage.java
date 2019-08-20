package ru.app.main.pages;

import ru.app.main.Settings;
import ru.app.util.ConnectionResolver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static ru.app.main.Launcher.portsPage;

public class OptionPage extends JPanel {
    private JLabel descr = new JLabel("DESCRIPTION");
    private JButton okayButton = new JButton("OK");
    private JButton noButton = new JButton("NO");
    private volatile JLabel loading = new JLabel();

    public OptionPage() {
        setLayout(null);
        setBackground(Color.BLACK);
        setSize(Settings.dimension);
        setVisible(false);

        descr.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        descr.setForeground(Color.WHITE);
        descr.setHorizontalAlignment(SwingConstants.CENTER);
        descr.setBounds(0, 0, getWidth(), 100);
        add(descr);

        add(okayButton);
        add(noButton);

        okayButton.setBounds(200, 400, 200, 70);
        okayButton.setFont(descr.getFont());
        noButton.setBounds(620, 400, 200, 70);
        noButton.setFont(descr.getFont());

        loading.setIcon(new ImageIcon("src/main/resources/loading.gif"));
        loading.setOpaque(false);
        loading.setSize(loading.getIcon().getIconWidth(), loading.getIcon().getIconHeight());
        loading.setLocation(getWidth() / 2 - loading.getWidth() / 2, 150);
        loading.setVisible(false);
        add(loading);

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
                            loading.setVisible(true);
                            ConnectionResolver resolver = new ConnectionResolver();
                            Settings.realPortForEmulator = resolver.findCCNetPort();
                            if (Settings.realPortForEmulator != null)
                                descr.setText("PORT FOUND!");
                            else
                                descr.setText("PORT NOT FOUND!");
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        setVisible(false);
                        portsPage.redraw();
                        portsPage.setVisible(true);
                    }
                }).start();

            }
        });
    }

    void setDescription(String text) {
        descr.setText(text);
    }
}
