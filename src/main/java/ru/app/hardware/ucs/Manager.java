package ru.app.hardware.ucs;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.ucs.UCSCommand;
import ru.app.protocol.ucs.classTypes.AuthorizationRequest;
import ru.app.protocol.ucs.classTypes.WorkWithOperationsArchive;
import ru.app.util.LogCreator;

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

        JButton btn = new JButton("put there");
        btn.setBounds(getWidth() / 2 - 200, 60, 150, 50);
        add(btn);
        btn.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LOGGER.info(LogCreator.console("blablabla"));
                client.sendMessage(new UCSCommand(new AuthorizationRequest(AuthorizationRequest.Operation.Sale), new byte[]{}));
            }
        });

        JButton btn2 = new JButton("put there");
        btn2.setBounds(getWidth() / 2 + 200, 60, 150, 50);
        add(btn2);
        btn2.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LOGGER.info(LogCreator.console("blobloblo"));
                client.sendMessage(new UCSCommand(new WorkWithOperationsArchive(WorkWithOperationsArchive.Operation.GetReport), new byte[]{}));
            }
        });
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
