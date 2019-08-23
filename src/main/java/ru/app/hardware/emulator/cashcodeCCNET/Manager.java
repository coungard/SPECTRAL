package ru.app.hardware.emulator.cashcodeCCNET;

import ru.app.hardware.AbstractManager;
import ru.app.protocol.ccnet.BillStateType;
import ru.app.protocol.ccnet.emulator.BillTable;
import ru.app.util.Logger;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Map;

public class Manager extends AbstractManager {
    private static final Color BACKGROUND_COLOR = new Color(205, 186, 116);
    private JPanel paymentPanel;
    private Client client;
    private static JCheckBox verboseLog;
    private boolean cassetteOut = false;

    public Manager(String port) {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);

        struct();
    }

    @Override
    public void struct() {
        JLabel mainLabel = formLabel("EMULATOR CASHCODE CCNET", 0);
        add(mainLabel);

        final JButton encashButton = new JButton("Encashment");
        encashButton.setBounds(550, 40, 180, 50);
        add(encashButton);
        encashButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cassetteOut = !cassetteOut;
                if (cassetteOut) {
                    client.setStatus(BillStateType.DropCassetteOutOfPosition);
                    encashButton.setText("Connect Cassette");
                } else {
                    client.setStatus(BillStateType.UnitDisabled);
                    encashButton.setText("Encashment");
                }
            }
        });

        paymentPanel = new JPanel();
        paymentPanel.setBorder(BorderFactory.createTitledBorder("NOTE INSERTION COMMANDS"));
        paymentPanel.setBounds(30, 40, 500, 100);
        paymentPanel.setBackground(new Color(7, 146, 151));
        add(paymentPanel);

        verboseLog = new JCheckBox("verbose Log");
        verboseLog.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        verboseLog.setBounds(getWidth() - 160, 20, 150, 50);
        add(verboseLog);

        final Map<String, byte[]> table = new BillTable().getTable();
        for (String denomination : table.keySet()) {
            addBill(denomination);
        }

        for (final Component component : paymentPanel.getComponents()) {
            if (component instanceof JButton) {
                component.addMouseListener(new MouseInputAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        byte[] denomination = table.get(((JButton) component).getText());
                        client.setCurrentDenom(denomination);
                        sendEscrowPosition();
                    }
                });
            }
        }
    }

    private void sendEscrowPosition() {
        if (client.getStatus() == BillStateType.Idling) {
            client.escrowNominal();
        } else {
            Logger.console("can not escrow, casher not idling now!");
        }
    }

    private void addBill(String billName) {
        JButton bill = new JButton(billName);
        bill.setPreferredSize(new Dimension(100, 30));
        paymentPanel.add(bill);
    }

    static boolean isVerboseLog() {
        return (verboseLog == null || verboseLog.isSelected());
    }

    @Override
    public String getCurrentCommand() {
        return client.getCurrentCommand() == null ? "" : "Command: " + client.getCurrentCommand().toString();
    }

    @Override
    public String getCurrentResponse() {
        return client.getCurrentResponse();
    }
}
