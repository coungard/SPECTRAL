package ru.app.main;

import ru.app.protocol.bus.DeviceType;

import java.util.HashMap;
import java.util.Map;

public class Settings {
    static final String VERSION = "1.0";
    public static final String COUNTRY = "ITL";
    public static DeviceType hardware;
    public static Map<String, Boolean> properties = new HashMap<>();

    static {
        properties.put("logLevel.hex", true);
        properties.put("logLevel.bytes", false);
        properties.put("logLevel.ascii", false);
    }
}