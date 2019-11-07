package ru.app.hardware.ucs;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.ucs.UCSCommand;
import ru.app.protocol.ucs.classTypes.AuthorizationRequest;
import ru.app.protocol.ucs.classTypes.RequestAcceptance;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Manager extends AbstractManager {
    private static final Logger LOGGER = Logger.getLogger(Manager.class);
    private static final Color BACKGROUND_COLOR = new Color(88, 155, 80);
    private final Client client;

    public Manager(String port) throws SerialPortException {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        struct();
    }

    @Override
    public void struct() {
        JLabel label = formLabel("UCS EFTPOS", 0);
        add(label);

        JButton hold = createButton("HOLD");
        hold.setBounds(30, 30, 140, 40);
        add(hold);
        hold.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new UCSCommand(new RequestAcceptance(RequestAcceptance.Operation.HOLD), new byte[]{}));
            }
        });

        JButton preAuth = createButton("Pre-Auth");
        preAuth.setBounds(30, 80, 140, 40);
        add(preAuth);
        preAuth.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new UCSCommand(new AuthorizationRequest(AuthorizationRequest.Operation.PreAuth), new byte[]{}));
            }
        });
    }

    private JButton createButton(String name) {
        JButton button = new JButton(name);
        button.setForeground(Color.WHITE);
        button.setBackground(Color.BLACK);
        return button;
    }

    @Override
    public void redraw() {

    }

    @Override
    protected void closeAll() {
        try {
            client.close();
        } catch (SerialPortException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
