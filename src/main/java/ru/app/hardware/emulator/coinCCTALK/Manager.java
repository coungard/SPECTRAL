package ru.app.hardware.emulator.coinCCTALK;

import ru.app.hardware.AbstractManager;
import ru.app.util.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

public class Manager extends AbstractManager {
    private static final Color BACKGROUND_COLOR = new Color(107, 225, 224);
    private Client client;
    private JPanel coinsPanel;
    private JTextField counter;
    private JLabel currentCounter = new JLabel();

    public Manager(String port) {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        struct();
    }

    @Override
    public void struct() {
        JLabel descr = formLabel("COIN MACHINE EMULATOR (CCTALK)", 0);
        add(descr);

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent) {
                    redrawCounter();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);

        coinsPanel = new JPanel();
        TitledBorder border = BorderFactory.createTitledBorder("COINS INSERTION COMMANDS");
        border.setTitleColor(Color.WHITE);
        border.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        coinsPanel.setBorder(border);
        coinsPanel.getBorder();
        coinsPanel.setBackground(new Color(235, 31, 255));
        coinsPanel.setBounds(30, 40, 400, 100);
        add(coinsPanel);

        for (int i = 1; i <= 4; i++) {
            addCoinButton(i);
        }

        JButton counterBtn = new JButton("<html>Jump Counter Value</html>");
        counterBtn.setBackground(new Color(136, 22, 126));
        counterBtn.setForeground(Color.WHITE);
        counterBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
        counterBtn.setBounds(700, 40, 220, 50);
        add(counterBtn);

        counter = new JTextField();
        counter.setEditable(false);
        counter.setHorizontalAlignment(SwingConstants.CENTER);
        counter.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        counter.setBounds(counterBtn.getX() + counterBtn.getWidth() + 10, counterBtn.getY(), 50, counterBtn.getHeight());
        counter.setText("1");
        add(counter);

        counterBtn.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int jump = Integer.parseInt(counter.getText());
                jump = (jump < 4) ? ++jump : 1;
                counter.setText(String.valueOf(jump));
            }
        });

        currentCounter.setFont(counterBtn.getFont());
        currentCounter.setForeground(Color.BLACK);
        currentCounter.setBounds(counterBtn.getX(), counterBtn.getY() + counterBtn.getHeight() + 10, counterBtn.getWidth() + 100, 50);
        add(currentCounter);
        redrawCounter();

        JButton skip = new JButton("SKIP");
        skip.setBackground(new Color(214, 72, 52));
        skip.setFont(counter.getFont());
        skip.setForeground(Color.BLACK);
        skip.setBounds(550, 40, 120, 50);
        add(skip);

        skip.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                changeCreditBuffer(0);
            }
        });

        for (final Component component : coinsPanel.getComponents()) {
            if (component instanceof JButton) {
                component.addMouseListener(new MouseInputAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!client.isEnabled()) {
                            Logger.console("Coin Machine is disabled!");
                        } else {
                            String nominal = ((JButton) component).getText().split(" ")[1];
                            switch (nominal) {
                                case "1":
                                    changeCreditBuffer(3);
                                    break;
                                case "2":
                                    changeCreditBuffer(6);
                                    break;
                                case "5":
                                    changeCreditBuffer(9);
                                    break;
                                case "10":
                                    changeCreditBuffer(12);
                                    break;
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void redraw() {
        setVisible(true);
    }


    private void changeCreditBuffer(int value) {
        byte[] current = client.getCurrentBuffer();
        byte[] creditBuffer = new byte[current.length];
        int jump = getCounter();
        client.incrementCounter(jump);

        byte nominal = (byte) value;
        for (int i = current.length - 1; i > 0; i--) {
            creditBuffer[i] = current[i != 1 ? i - 2 : i];
        }
        creditBuffer[0] = nominal;
        for (int j = 0; j < jump - 1; j++) {
            System.arraycopy(creditBuffer, 0, creditBuffer, 2, current.length - 2);
            creditBuffer[j * 2] = 0;
        }
        client.setCurrentBuffer(creditBuffer);
    }

    private void addCoinButton(int i) {
        int nominal;
        if (i > 2)
            nominal = (i * i - i) / 5 * 5;
        else
            nominal = i;
        JButton coin = new JButton("COIN " + nominal);
        coin.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        coin.setPreferredSize(new Dimension(120, 30));
        coinsPanel.add(coin);
    }

    private void redrawCounter() {
        currentCounter.setText("Current counter: 0x" + String.format("%x", client.getCurrentCounter()).toUpperCase());
    }

    private int getCounter() {
        return Integer.parseInt(counter.getText());
    }
}
