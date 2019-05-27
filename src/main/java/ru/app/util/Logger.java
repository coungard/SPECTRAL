package ru.app.util;

import ru.app.listeners.AbstractManager;
import ru.app.main.Launcher;
import ru.app.main.Settings;
import ru.app.protocol.bus.DeviceType;
import ru.app.protocol.payout.StreamType;

import java.util.Arrays;

public class Logger {
    private static AbstractManager manager;

    public static void init() {
        manager = Launcher.currentManager;
    }

    public static void console(String text) {
        System.out.println(text);
        manager.textArea.setText(manager.textArea.getText() + text + "\n");
    }

    public static void logOutput(byte[] transmitted, byte[] encrypted) {
        log(transmitted, StreamType.OUTPUT);
        if (encrypted != null) log(encrypted, StreamType.OUTPUT_ENCRYPT);
    }

    public static void logInput(byte[] received, byte[] decrypted) {
        log(received, StreamType.INPUT);
        if (decrypted != null) log(decrypted, StreamType.INPUT_DECRYPT);
    }

    private static void log(byte[] buffer, StreamType streamType) {
        StringBuilder ascii = new StringBuilder();
        for (byte b : buffer) {
            if (b != '\r' || Settings.hardware == DeviceType.BNE_S110M) ascii.append((char) b);
        }
        String type = null;

        if (Settings.hardware == DeviceType.SMART_PAYOUT) {
            if (streamType.toString().contains("OUTPUT")) {
                type = manager.client.currentCommand.commandType.toString();
            } else {
                type = ResponseHandler.parseResponse(streamType, buffer);
            }
        }

        String log = streamType + "BYTES: " + Arrays.toString(buffer) +
                "\tHEX: " + Utils.byteArray2HexString((buffer)) +
                (Settings.hardware == DeviceType.SMART_PAYOUT ? "\tASCII: " + ascii.toString() + "\t" + type : "");

        System.out.println(log);
        manager.textArea.setText(manager.textArea.getText() + log + "\n");
    }
}
