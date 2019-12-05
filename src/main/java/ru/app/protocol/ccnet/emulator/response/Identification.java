package ru.app.protocol.ccnet.emulator.response;

import ru.app.main.Settings;

import java.util.HashMap;
import java.util.Map;

public class Identification extends EmulatorCommand {
    private static Map<String, String> softwareMap = new HashMap<>();

    @Override
    public byte[] getData() {
        if (Settings.prop.get("casher.soft") == null) {
            return new byte[]{(byte) 0x53, (byte) 0x4D, (byte) 0x2D, (byte) 0x52, (byte) 0x55, (byte) 0x31, (byte) 0x33, (byte) 0x35,
                    (byte) 0x33, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x32, (byte) 0x31,
                    (byte) 0x4B, (byte) 0x43, (byte) 0x30, (byte) 0x37, (byte) 0x30, (byte) 0x30, (byte) 0x36, (byte) 0x38, (byte) 0x35,
                    (byte) 0x37, (byte) 0xE7, (byte) 0x00, (byte) 0x4D, (byte) 0x53, (byte) 0x08, (byte) 0x12, (byte) 0xF0};
        } else {
            return new byte[]{};
        }
    }

    private byte[] getSoftwareIdentification(String soft) {
        byte[] partNumber = new byte[15];
        byte[] serialNumber = new byte[12];
        byte[] assetNumber = new byte[7];

        for (int i = 0; i < partNumber.length; i++) {
            partNumber[i] = (byte) 0x20; // spaces
            if (i < serialNumber.length) serialNumber[i] = (byte) 0x20;
            if (i < assetNumber.length) assetNumber[i] = (byte) 0x20;
        }

        String[] parts = soft.replaceAll(" ", "").split(",");
        String pn = parts[0];
        String sn = parts[1];
        String an = parts[2];

        return new byte[]{};
    }

    static {
        softwareMap.put("1", "P/N=SM-RU1353, S/N=21KC07006857, A/N=-25.0.77.83.8.18.-16");
    }

    public static Map<String, String> getSoftwareMap() {
        return softwareMap;
    }
}


// ASSET NUMBER -2507783818-16