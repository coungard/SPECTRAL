package ru.app.main.pages;

import ru.app.main.pages.settings.GeneralSettings;

import javax.swing.*;

public class SettingsPage extends JTabbedPane {
    private GeneralSettings generalSettings = new GeneralSettings();

    public SettingsPage() {
        addTab("General", generalSettings);
        // todo addTab("Requester", new RequesterSettings());

        initialize();
    }

    private void initialize() {

//        JButton save = new JButton("Save");
//        save.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
//        save.setBounds(560, 500, 200, 40);
//        save.setBackground(new Color(220, 255, 226));
//        save.addMouseListener(new MouseInputAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
////                saveParams();
//                JOptionPane.showMessageDialog(null, "Parameters saved!", "info", JOptionPane.PLAIN_MESSAGE, null);
//            }
//        });
//        add(save);
//
//        JButton back = new JButton("Back");
//        back.setFont(save.getFont());
//        back.setBounds(780, 500, 200, 40);
//        back.setBackground(new Color(252, 220, 206));
//        back.addMouseListener(new MouseInputAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                setVisible(false);
//                Launcher.mainPanel.setVisible(true);
//            }
//        });
//        add(back);

        setVisible(false);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        generalSettings.setVisible(aFlag);
    }
}
