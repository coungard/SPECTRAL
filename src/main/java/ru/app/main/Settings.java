package ru.app.main;

import ru.app.bus.DeviceType;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    static final String VERSION = "1.44";
    public static final Dimension dimension = new Dimension(1020, 600);
    public static final String COUNTRY = "ITL";
    public static DeviceType hardware;
    public static String deviceForEmulator = null;
    public static String realPortForEmulator = null;
    public static Map<String, Boolean> properties = new HashMap<>();
    public static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:S");
    public static String paymentsDir;
    public static String paymentPath;
    public static String separator = System.getProperty("file.separator");

    // свойства по умолчанию
    static {
        // {user.name}/emulator/
        paymentsDir = FileSystemView.getFileSystemView().getHomeDirectory().toString() + separator + "emulator" + separator;
        // {user.name}/emulator/payment
        paymentPath = paymentsDir + "payment";

        properties.put("logLevel.hex", true);
        properties.put("logLevel.bytes", false);
        properties.put("logLevel.ascii", false);
    }
}
