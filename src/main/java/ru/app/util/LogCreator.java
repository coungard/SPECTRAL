package ru.app.util;

import ru.app.bus.DeviceType;
import ru.app.hardware.AbstractManager;
import ru.app.hardware.smartSystem.hopper.Client;
import ru.app.main.Launcher;
import ru.app.main.RmiServer;
import ru.app.main.Settings;
import ru.app.protocol.cctalk.Command;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

import static ru.app.util.StreamType.*;

public class LogCreator {
    private static AbstractManager manager;
    private static boolean isService;

    public static void init() {
        manager = Launcher.currentManager;
        isService = Settings.args.length > 0 && Settings.args[0].equals("--service");
    }

    public static String console(String text) {
        if (manager != null && Settings.args.length == 0)
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
        if (Settings.hardware == DeviceType.ACQUIRING) {
            try {
                ascii.append(new String(buffer, "windows-1251"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else
            for (byte b : buffer) {
                if (b != '\r' || Settings.hardware == DeviceType.BNE_S110M) ascii.append((char) b);
            }
        String commandType = "";

        switch (Settings.hardware) {
            case SMART_SYSTEM:
                if (Settings.device.equals("SMART_HOPPER")) {
                    commandType = manager.getCurrentCommand();
                    Command hopperCommand = Client.getHopperCommand();
                    if (type == INPUT) {
                        commandType = CCTalkParser.parseCC2(hopperCommand, buffer);
                    }
                } else {
                    if (type == OUTPUT || type == OUTPUT_ENCRYPT) {
                        commandType = manager.getCurrentCommand();
                    } else {
                        commandType = ResponseHandler.parseResponse(type, buffer);
                    }
                }
                break;
            case BNE_S110M:
                if (type == INPUT) {
                    commandType = ResponseHandler.parseResponse(type, buffer);
                }
                break;
            case EMULATOR:
                if (manager == null && !isService)
                    return null;
                if (Settings.device.equals("CCNET CASHER")) {
                    if (isService) {
                        if (type == INPUT) commandType = RmiServer.getCurrentCommand();
                        if (type == OUTPUT) commandType = RmiServer.getCurrentResponse();
                    } else {
                        if (type == INPUT) commandType = manager.getCurrentCommand();
                        if (type == OUTPUT) commandType = manager.getCurrentResponse();
                    }
                }
                break;
            case ACQUIRING:
                if (manager == null)
                    return null;
                if (type == OUTPUT)
                    commandType = manager.getCurrentCommand();
                if (type == INPUT) {
                    commandType = manager.getCurrentResponse();
                }
        }
        String log = type + ("1".equals(Settings.prop.get("logLevel.bytes")) ? "BYTES:  " + Arrays.toString(buffer) + "\t" : "") +
                ("1".equals(Settings.prop.get("logLevel.int")) ? "INT:  " + Utils.bytes2int((buffer)) + "\t" : "") +
                ("1".equals(Settings.prop.get("logLevel.hex")) ? "HEX:  " + Utils.bytes2hex((buffer)) + "\t" : "") +
                ("1".equals(Settings.prop.get("logLevel.ascii")) ? "ASCII:  " + ascii.toString() + "\t\t" : "") + commandType;

        if (manager != null && !isService)
            manager.textArea.setText(manager.textArea.getText() + Settings.dateFormat.format(new Date()) + "\t" + log + "\n");
        return log;
    }
}
