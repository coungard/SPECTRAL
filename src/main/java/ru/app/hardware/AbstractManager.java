package ru.app.hardware;

import ru.app.main.Launcher;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractManager extends JLayeredPane {
    protected JScrollPane scroll;
    public volatile JTextArea textArea;

    public abstract void content();

    public abstract void redraw();

    protected abstract void closeAll();

    protected AbstractManager() {
        textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scroll = new JScrollPane(textArea);
        scroll.setBounds(30, 140, 960, 390);
        add(scroll);

        final JButton exitButton = new JButton("Exit");
        exitButton.setBackground(new Color(208, 44, 50));
        exitButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        exitButton.setForeground(Color.WHITE);
        exitButton.setBounds(788, 530, 200, 40);
        exitButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        exitButton.setEnabled(false);
                        closeAll();
                        System.exit(0);
                    }
                }).start();
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

    /**
     * Строковое выражение текущей команды от клиента, необходимо для последующего переопределения в наследниках Manager
     *
     * @return current command
     */
    public String getCurrentCommand() {
        return "";
    }

    public String getCurrentResponse() {
        return null;
    }
}
