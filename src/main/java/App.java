import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class App {
    private JFrame window = new JFrame("Manager");
    private final JPanel mainPanel;
    private JPanel managerPanel;
    private Manager manager;
    private JTextArea textArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App();
            }
        });
    }

    private App() {
        window.setSize(600, 400);
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setSize(window.getSize());
        window.add(mainPanel);
        managerPanel = new JPanel();
        managerPanel.setLayout(null);
        managerPanel.setSize(window.getSize());
        managerPanel.setVisible(false);
        window.add(managerPanel);
        init();

        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
    }

    private void init() {
        createMainPage();
        createManagerPage();
    }

    private void createManagerPage() {
        JLabel cmdLabel = formLabel("КОМАНДЫ", 0);
        managerPanel.add(cmdLabel);
        textArea = new JTextArea();
        textArea.setBounds(30, 140, 500, 200);
        managerPanel.add(textArea);
        JButton enableButton = new JButton("рыбалка");
        enableButton.setBounds(30, 60, 160, 30);
        enableButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    int[] buffer = new int[]{40, 1, 11, 245, 72, 107};
                    textArea.setText(textArea.getText() + "OUTPUT:  " + Arrays.toString(buffer) + "\n");

                    textArea.setText(textArea.getText() + manager.sendBytes(buffer) + "\n");
                } catch (InterruptedException | SerialPortException e1) {
                    e1.printStackTrace();
                }
            }
        });
        managerPanel.add(enableButton);
    }

    private void createMainPage() {
        JLabel label = formLabel("Выберите ком-порт", 30);
        mainPanel.add(label);

        String[] ports = SerialPortList.getPortNames();
        for (int i = 0; i < ports.length; i++) {
            JButton button = new JButton(ports[i]);
            button.setBounds(window.getWidth() / 2 - 100, 80 + i * 100, 200, 50);
            button.addMouseListener(new MyListener(ports[i]));
            mainPanel.add(button);
        }
    }

    private void switchPanel() {
        mainPanel.setVisible(false);
        managerPanel.setVisible(true);
    }

    private class MyListener extends MouseInputAdapter {
        String portName;

        MyListener(String portName) {
            this.portName = portName;
        }

        public void mousePressed(MouseEvent e) {
            try {
                manager = new Manager(portName);
                switchPanel();
            } catch (SerialPortException e1) {
                System.out.println("Проблемы с подключением к ком порту");
                e1.printStackTrace();
            }
        }
    }

    private JLabel formLabel(String name, int y) {
        JLabel label = new JLabel(name);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        label.setBounds(0, y, window.getWidth(), 50);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        return label;
    }
}
