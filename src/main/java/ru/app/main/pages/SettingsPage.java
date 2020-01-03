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
        setVisible(false);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        generalSettings.setVisible(aFlag);
    }
}
