package ru.app.main;

import ru.app.bus.DeviceType;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    static final String VERSION = "1.38";
    public static final Dimension dimension = new Dimension(1020, 600);
    public static final String COUNTRY = "ITL";
    public static DeviceType hardware;
    public static String deviceForEmulator = null;
    public static String realPortForEmulator = null;
    public static Map<String, Boolean> properties = new HashMap<>();
    public static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:S");

    static {
        properties.put("logLevel.hex", true);
        properties.put("logLevel.bytes", false);
        properties.put("logLevel.ascii", false);
    }
}
