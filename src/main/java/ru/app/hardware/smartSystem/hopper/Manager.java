package ru.app.hardware.smartSystem.hopper;

import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.hardware.AbstractManager;
import ru.app.protocol.cctalk.Command;
import ru.app.protocol.cctalk.Nominal;
import ru.app.protocol.cctalk.hopper.HopperCommand;
import ru.app.util.CCTalkParser;
import ru.app.util.LogCreator;
import ru.app.util.Utils;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Manager extends AbstractManager {
    private static final Logger LOGGER = Logger.getLogger(Manager.class);
    private static final Color BACKGROUND_COLOR = new Color(233, 236, 238);
    private Client client;
    private boolean interrupted = false;
    private String[] table = new String[]{"1", "2", "5", "10"};
    private Map<String, Integer> cash = new LinkedHashMap<>();

    public Manager(String port) throws SerialPortException {
        setSize(1020, 600);
        setOpaque(true);
        setBackground(BACKGROUND_COLOR);
        client = new Client(port);
        content();
    }

    @Override
    public void content() {
        JLabel cmdLabel = formLabel("CCTalk Smart Payout (CC2)", 0);
        add(cmdLabel);

        scroll.setBounds(30, 190, 960, 340);

        JButton run = createButton("Run Hopper", new Point(40, 40));
        run.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        interrupted = false;
                        while (!interrupted) {
                            client.sendMessage(new Command(HopperCommand.MC_REQUEST_STATUS));
                            pause(200);
                        }
                    }
                }).start();
            }
        });

        JButton stop = createButton("Stop Hopper", new Point(230, 40));
        stop.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                interrupted = true;
            }
        });

        JButton getNotesAmount = createButton("Get Notes Amount", new Point(40, 90));
        getNotesAmount.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                getNotesAmountMap();
            }
        });

        JButton setNotesAmount = createButton("Set Notes Amount", new Point(230, 90));
        setNotesAmount.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Map<String, Integer> map = getNotesAmountMap();
                JFrame frame = new JFrame("Set notes amount");
                frame.setSize(300, 200);
                JPanel panel = new JPanel();
                panel.setBounds(frame.getBounds());
                panel.setLayout(null);
                for (int i = 0; i < table.length; i++) {
                    JLabel coinL = new JLabel("coin " + table[i]);
                    coinL.setBounds(10, i * 20 + 10, 70, 20);
                    panel.add(coinL);

                    JTextField field = new JTextField();
                    field.setBounds(110, i * 20 + 10, 70, 20);
                    panel.add(field);

                    JLabel amount = new JLabel();
                    amount.setText(String.valueOf(map.get(table[i])));
                    amount.setBounds(200, i * 20 + 10, 40, 20);
                    panel.add(amount);

                    // TODO...
                }
                frame.add(panel);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });

        JButton payoutAmount = createButton("Payout Amount", new Point(420, 40));
        payoutAmount.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String input = JOptionPane.showInputDialog(null, "Enter sum payout");
                if (!Utils.isNumeric(input)) {
                    LOGGER.warn(LogCreator.console("Invalid sum entired!"));
                } else {
                    byte[] data = buildSum(input);
                    client.sendMessage(new Command(HopperCommand.PAYOUT_AMOUNT, data));
                }
            }
        });

        JButton getCurrentSum = createButton("Get Current Sum", new Point(420, 90));
        getCurrentSum.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Map<String, Integer> map = getNotesAmountMap();
                int sum = 0;
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    int money = Integer.parseInt(entry.getKey()) * entry.getValue();
                    sum += money;
                }
                JOptionPane.showMessageDialog(null, sum);
            }
        });

        JButton reset = createButton("Reset", new Point(610, 40));
        reset.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new Command(HopperCommand.ResetDevice));
            }
        });

        JButton requestSoftware = createButton("Request Software", new Point(800, 40));
        requestSoftware.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                client.sendMessage(new Command(HopperCommand.SimplePoll));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestEquipmentCategoryID));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestManufacturerID));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestCommsRevision));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestProductCode));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestAddressMode));
                pause(40);
                client.sendMessage(new Command(HopperCommand.RequestSoftwareRevision));
            }
        });

        cash.put("1", 0);
        cash.put("2", 0);
        cash.put("5", 0);
        cash.put("10", 0);
    }

    private Map<String, Integer> getNotesAmountMap() {
        Map<String, Integer> result = new HashMap<>();
        for (String note : table) {
            Nominal nominal = new Nominal(note);
            Command command = new Command(HopperCommand.MC_GET_NOTE_AMOUNT, nominal.getValue());
            byte[] resp = client.sendMessage(command);
            String logic = CCTalkParser.parseCC2(command, resp);
            int amount = getAmount(logic);
            result.put(note, amount);
        }
        return result;
    }

    private int getAmount(String text) {
        int result = 0;
        try {
            String[] parts = text.split(" ");
            if (parts[0].equals("Amount")) {
                result = Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private byte[] buildSum(String input) {
        byte[] res = new byte[4];
        double d = Double.parseDouble(input);
        int x = (int) (d * 100);
        String hex = new Formatter().format("%08X", x).toString();
        String nominal = Utils.inverse(hex);

        int temp = 0;
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) Long.parseLong(nominal.substring(temp, temp + 2), 16);
            temp = temp + 2;
        }
        return res;
    }

    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
    }

    private JButton createButton(String text, Point point) {
        JButton button = new JButton(text);
        button.setBounds(point.x, point.y, 180, 40);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        add(button);
        return button;
    }

    @Override
    public void redraw() {
        setVisible(true);
    }

    @Override
    protected void closeAll() {
        client.close();
    }

    @Override
    public String getCurrentCommand() {
        return client.getCurrentCommand();
    }
}
