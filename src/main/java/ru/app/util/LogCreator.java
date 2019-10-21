package ru.app.util;

import ru.app.bus.DeviceType;
import ru.app.hardware.AbstractManager;
import ru.app.hardware.smartPayout.Client;
import ru.app.main.Launcher;
import ru.app.main.Settings;

import java.util.Arrays;
import java.util.Date;

import static ru.app.util.StreamType.*;

public class LogCreator {
    private static AbstractManager manager;

    public static void init() {
        manager = Launcher.currentManager;
    }

    public static String console(String text) {
        if (manager == null) return null;
        manager.textArea.setText(manager.textArea.getText() + Settings.dateFormat.format(new Date()) + "\t" + text + "\n");
        return text;
    }

    public static String logOutput(byte[] transmitted) {
        return log(transmitted, OUTPUT);
    }

    public static String logInput(byte[] received) {
        return log(received, INPUT);
    }

    public static String logOutput(byte[] transmitted, byte[] encrypted) {
        String res = log(transmitted, OUTPUT);
        if (encrypted != null) res += log(encrypted, OUTPUT_ENCRYPT);
        return res;
    }

    public static String logInput(byte[] received, byte[] decrypted) {
        String res = log(received, INPUT);
        if (decrypted != null) res += log(decrypted, INPUT_DECRYPT);
        return res;
    }

    private static String log(byte[] buffer, StreamType type) {
        StringBuilder ascii = new StringBuilder();
        for (byte b : buffer) {
            if (b != '\r' || Settings.hardware == DeviceType.BNE_S110M) ascii.append((char) b);
        }
        String commandType = "";

        switch (Settings.hardware) {
            case SMART_PAYOUT:
                if (type == OUTPUT || type == OUTPUT_ENCRYPT) {
                    commandType = Client.currentCommand.getCommandType().toString();
                } else {
                    commandType = ResponseHandler.parseResponse(type, buffer);
                }
                break;
            case BNE_S110M:
                if (type == INPUT) {
                    commandType = ResponseHandler.parseResponse(type, buffer);
                }
                break;
            case EMULATOR:
                if (manager == null)
                    return null;
                if (Settings.deviceForEmulator.equals("CCNET CASHER")) {
                    if (type == INPUT)
                        commandType = manager.getCurrentCommand();
                    if (type == OUTPUT) {
                        commandType = manager.getCurrentResponse();
                    }
                }
        }

        String log = type + (Settings.properties.get("logLevel.bytes") ? "BYTES:  " + Arrays.toString(buffer) + "\t" : "") +
                (Settings.properties.get("logLevel.hex") ? "HEX:  " + Utils.bytes2hex((buffer)) + "\t" : "") +
                (Settings.properties.get("logLevel.ascii") ? "ASCII:  " + ascii.toString() + "\t" : "") + commandType;

        if (manager != null)
            manager.textArea.setText(manager.textArea.getText() + log + "\n");
        return log;
    }
}
