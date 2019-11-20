package ru.app.main;

import ru.app.bus.DeviceType;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    static final String VERSION = "1.56";
    public static final Dimension dimension = new Dimension(1020, 600);
    public static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:S");
    public static final String COUNTRY = "ITL";
    public static DeviceType hardware;
    public static String deviceForEmulator = null;
    public static String realPortForEmulator = null;
    // общие настройки приложения (файл)
    public static final String propFile = "prop.cfg";
    // настройки эмулятора (файл)
    public static final String promEmulFile = "requester.cfg";
    // общие настройки приложения
    public static Map<String, String> prop = new HashMap<>();
    // настройки эмулятора
    public static Map<String, String> propEmul = new HashMap<>();

    public static String paymentsDir = "payments/";
    public static String successDir = "payments/success/";
    public static String errorDir = "payments/error/";
    public static String paymentPath = "payments/payment";

    // свойства по умолчанию
    static {
        prop.put("logLevel.hex", "1");
        prop.put("logLevel.bytes", "0");
        prop.put("logLevel.ascii", "0");
    }
}
