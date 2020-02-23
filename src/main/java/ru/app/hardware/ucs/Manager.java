package ru.app.hardware.ucs;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.ucs.UCSCommand;
import ru.app.protocol.ucs.commands.AuthorizationRequest;
import ru.app.protocol.ucs.commands.RequestAcceptance;
import ru.app.protocol.ucs.commands.SessionCommands;
import ru.app.protocol.ucs.commands.WorkWithOperationsArchive;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Arrays;

public class Manager extends AbstractManager {
    private static final Logger LOGGER = Logger.getLogger(Manager.class);
    private static final Color BACKGROUND_COLOR = new Color(88, 155, 80);
    private final Client client;

    public Manager(String port) throws SerialPortException {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        content();
    }

    @Override
    public void content() {
        JLabel label = formLabel("UCS EFTPOS", 0);
        add(label);

        JButton hold = createButton("Hold");
        hold.setBounds(30, 35, 140, 40);
        add(hold);
        hold.addMouseListener(new UCSMouseAdapter(new UCSCommand(new RequestAcceptance(RequestAcceptance.HOLD), new byte[]{})));

        JButton preAuth = createButton("Pre-Auth");
        preAuth.setBounds(30, 85, 140, 40);
        add(preAuth);
        preAuth.addMouseListener(new UCSMouseAdapter(new UCSCommand(new AuthorizationRequest(AuthorizationRequest.PRE_AUTH), new byte[]{})));

        JButton login = createButton("Login");
        login.setBounds(180, 35, 140, 40);
        add(login);
        login.addMouseListener(new UCSMouseAdapter(new UCSCommand(new SessionCommands(SessionCommands.LOGIN), new byte[]{})));

        JButton sale = createButton("Sale");
        sale.setBounds(180, 85, 140, 40);
        add(sale);
        sale.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String text = JOptionPane.showInputDialog(null, "Enter amount");
                if (Utils.isNumeric(text)) {
                    byte[] pennies = calcPennies(new BigDecimal(text));
                    final UCSCommand command = new UCSCommand(new AuthorizationRequest(AuthorizationRequest.SALE), pennies);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            client.sendMessage(command);
                        }
                    }, "UCS-THREAD").start();
                }
            }
        });

        JButton activation = createButton("Activation");
        activation.setBounds(330, 35, 140, 40);
        add(activation);
        activation.addMouseListener(new UCSMouseAdapter(
                new UCSCommand(new AuthorizationRequest(AuthorizationRequest.ACTIVATION), new byte[]{})));

        JButton encashment = createButton("Encashment");
        encashment.setBounds(330, 85, 140, 40);
        add(encashment);
        encashment.addMouseListener(new UCSMouseAdapter(
                new UCSCommand(new WorkWithOperationsArchive(WorkWithOperationsArchive.FINALIZE_DAY_TOTALS), new byte[]{})));
    }

    /**
     * Данный метод принимает на вход число формата BigDecimal и возвращает массив из 12 байтов, содержащий копейки в
     * виде ASCII с лидирующими нулями. <p/>
     * Пример: BigDecimal("140.50") -> new byte[]{'0','0','0','0','0','0','0','1','4','0','5','0'}
     *
     * @param bigDecimal сумма
     * @return массив копеек [12 байт] для валидной команды SALE
     */
    private byte[] calcPennies(BigDecimal bigDecimal) {
        byte[] res = new byte[12];
        Arrays.fill(res, (byte) '0');

        char[] pennies = bigDecimal.toString().toCharArray();
        for (int i = 0, j = 0; i < pennies.length; i++, j++) {
            char c = pennies[pennies.length - 1 - i];
            if (c == '.') {
                j--;
                continue;
            }
            res[j] = (byte) c;
        }
        Utils.reverse(res);
        return res;
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
