package ru.app.util;

import ru.app.hardware.AbstractManager;
import ru.app.main.Launcher;
import ru.app.main.Settings;
import ru.app.bus.DeviceType;
import ru.app.protocol.cctalk.payout.StreamType;

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
        String type = "";

        switch (Settings.hardware) {
            case SMART_PAYOUT:
                if (streamType.toString().contains("OUTPUT")) {
//                    type = manager.client.currentCommand.commandType.toString();
                } else {
                    type = ResponseHandler.parseResponse(streamType, buffer);
                }
                break;
            case BNE_S110M:
                if (streamType == StreamType.INPUT) {
                    type = ResponseHandler.parseResponse(streamType, buffer);
                }
        }

        String log = streamType + (Settings.properties.get("logLevel.bytes") ? "BYTES:  " + Arrays.toString(buffer) + "\t" : "") +
                (Settings.properties.get("logLevel.hex") ? "HEX:  " + Utils.bytes2hex((buffer)) + "\t" : "") +
                (Settings.properties.get("logLevel.ascii") ? "ASCII:  " + ascii.toString() + "\t" : "") + type;

        System.out.println(log);
        manager.textArea.setText(manager.textArea.getText() + log + "\n");
    }
}
