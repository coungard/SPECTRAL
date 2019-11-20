package ru.app.hardware.ucs;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.ucs.UCSCommand;
import ru.app.protocol.ucs.classTypes.AuthorizationRequest;
import ru.app.protocol.ucs.classTypes.RequestAcceptance;
import ru.app.protocol.ucs.classTypes.SessionCommands;

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

        JButton hold = createButton("Hold");
        hold.setBounds(30, 30, 140, 40);
        add(hold);
        hold.addMouseListener(new UCSMouseAdapter(new UCSCommand(new RequestAcceptance(RequestAcceptance.HOLD), new byte[]{})));

        JButton preAuth = createButton("Pre-Auth");
        preAuth.setBounds(30, 80, 140, 40);
        add(preAuth);
        preAuth.addMouseListener(new UCSMouseAdapter(new UCSCommand(new AuthorizationRequest(AuthorizationRequest.PRE_AUTH), new byte[]{})));

        JButton login = createButton("Login");
        login.setBounds(180, 30, 140, 40);
        add(login);
        login.addMouseListener(new UCSMouseAdapter(new UCSCommand(new SessionCommands(SessionCommands.LOGIN), new byte[]{})));

        JButton sale = createButton("Sale");
        sale.setBounds(180, 80, 140, 40);
        add(sale);
        sale.addMouseListener(new UCSMouseAdapter(new UCSCommand(new AuthorizationRequest(AuthorizationRequest.SALE),
                new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00})));

        JButton activation = createButton("Activation");
        activation.setBounds(330, 30, 140, 40);
        add(activation);
        activation.addMouseListener(new UCSMouseAdapter(new UCSCommand(new AuthorizationRequest(AuthorizationRequest.ACTIVATION), new byte[]{})));

        JButton test = createButton("Test");
        test.setBounds(580, 30, 140, 40);
        add(test);
        test.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendBytes(new byte[]{(byte) 0x33, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x31, (byte) 0x39, (byte) 0x39,
                        (byte) 0x39, (byte) 0x39, (byte) 0x33, (byte) 0x31, (byte) 0x39, (byte) 0x30, (byte) 0x30});
            }
        });
    }

    private class UCSMouseAdapter extends MouseInputAdapter {
        private final UCSCommand ucsCommand;

        UCSMouseAdapter(UCSCommand ucsCommand) {
            this.ucsCommand = ucsCommand;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.sendMessage(ucsCommand);
                }
            }, "UCS-THREAD").start();
        }
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
    public String getCurrentCommand() {
        return client.getCurrentCommand() == null ? "" : "Command: " + client.getCurrentCommand();
    }

    @Override
    public String getCurrentResponse() {
        return client.getCurrentResponse();
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
