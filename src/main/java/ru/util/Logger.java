package ru.util;

import ru.app.Client;
import ru.app.Manager;
import ru.protocol.payout.StreamType;

import java.util.Arrays;

public class Logger {

    public static void console(String text) {
        System.out.println(text);
        Manager.textArea.setText(Manager.textArea.getText() + text + "\n");
    }

    public static void logOutput(byte[] transmitted, byte[] encrypted) {
        log(transmitted, StreamType.OUTPUT);
        log(encrypted, StreamType.OUTPUT_ENCRYPT);
    }

    public static void logInput(byte[] received, byte[] decrypted) {
        log(received, StreamType.INPUT);
        log(decrypted, StreamType.INPUT_DECRYPT);
    }

    private static void log(byte[] buffer, StreamType streamType) {
        StringBuilder ascii = new StringBuilder();
        for (byte b : buffer) {
            if (b != '\r') ascii.append((char) b);
        }
        String type;
        if (streamType.toString().contains("OUTPUT")) {
            type = Client.currentCommand.commandType.toString();
        } else {
            type = ResponseHandler.parseResponse(streamType, buffer);
        }

        String log = streamType + "BYTES: " + Arrays.toString(buffer) +
                "\tHEX: " + Utils.byteArray2HexString((buffer)) +
                "\tASCII: " + ascii.toString() + "\t" + type;

        System.out.println(log);
        Manager.textArea.setText(Manager.textArea.getText() + log + "\n");
    }
}
