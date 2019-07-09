package ru.app.hardware;

import ru.app.main.Launcher;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;

public abstract class AbstractManager extends JLayeredPane {
    public volatile JTextArea textArea;
//    public AbstractClient client;

    public abstract void struct();

    protected AbstractManager() {
        textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(30, 140, 960, 390);
        add(scroll);

        JButton restartButton = new JButton("Restart");
        restartButton.setBackground(new Color(132, 47, 197));
        restartButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBounds(30, 530, 200, 40);
        restartButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    Utils.restartApplication(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Launcher.main(new String[0]);
                            } catch (UnsupportedLookAndFeelException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }));
                    System.exit(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(restartButton);

        JButton exitButton = new JButton("Exit");
        exitButton.setBackground(new Color(208, 44, 50));
        exitButton.setFont(restartButton.getFont());
        exitButton.setForeground(restartButton.getForeground());
        exitButton.setBounds(788, 530, 200, 40);
        exitButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.exit(0);
            }
        });
        add(exitButton);
    }

    protected JLabel formLabel(String name, int y) {
        JLabel label = new JLabel(name);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        label.setBounds(0, y, this.getWidth(), 40);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        return label;
    }
}
