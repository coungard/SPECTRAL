package ru.app.main;

import ru.app.bus.DeviceType;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    public static final String VERSION = "1.92";
    public static final Dimension dimension = new Dimension(1020, 600);
    public static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:S");
    public static final String COUNTRY = "RUB";
    public static DeviceType hardware;
    public static String device = null;
    public static String realPortForEmulator = null;
    public static String[] args;

    // директория с файлами конфигурации
    public static final String propDir = "config/";
    // общие настройки приложения (файл)
    public static final String propFile = "config/prop.cfg";
    // настройки эмулятора (файл)
    public static final String propEmulatorFile = "config/emulator.cfg";
    // настройки автозапуска эмулятора (файл)
    public static final String autoLaunchPropFile = "config/start.cfg";
    // общие настройки приложения
    public static Map<String, String> prop = new HashMap<>();
    // настройки эмулятора
    public static Map<String, String> propEmulator = new HashMap<>();

    public static final String paymentsDir = "payments/";
    public static final String successDir = "payments/success/";
    public static final String errorDir = "payments/error/";
    public static final String manualDir = "payments//manual/";
    public static final String paymentPath = "payments/payment";

    // свойства по умолчанию
    static {
        prop.put("logLevel.hex", "1");
        prop.put("logLevel.bytes", "0");
        prop.put("logLevel.int", "0");
        prop.put("logLevel.ascii", "0");

        prop.put("casher.soft", "1");

        propEmulator.put("url", "http://109.248.44.61:8080/ussdWorker/");
        propEmulator.put("timeout.status", "180000");
        propEmulator.put("timeout.requester", "60000");
        propEmulator.put("timeout.nominals", "3000");
        propEmulator.put("qiwi.log.dir", "C:/qiwi/logs/");
    }
}
