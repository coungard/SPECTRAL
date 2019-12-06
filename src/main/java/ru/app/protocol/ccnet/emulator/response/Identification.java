package ru.app.protocol.ccnet.emulator.response;

import ru.app.main.Settings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Identification extends EmulatorCommand {
    private static Map<String, String> softwareMap = new HashMap<>();

    @Override
    public byte[] getData() throws IOException {
        return getSoftwareIdentification(Settings.prop.get("casher.soft"));
    }

    private byte[] getSoftwareIdentification(String softNumber) throws IOException {
        byte[] partNumber = new byte[15];
        byte[] serialNumber = new byte[12];
        byte[] assetNumber = new byte[7];

        for (int i = 0; i < partNumber.length; i++) {
            partNumber[i] = (byte) 0x20; // spaces
            if (i < serialNumber.length) serialNumber[i] = (byte) 0x20;
            if (i < assetNumber.length) assetNumber[i] = (byte) 0x20;
        }

        String soft = softwareMap.get(softNumber);

        String[] parts = soft.replaceAll(" ", "").split(",");
        String pnFull = parts[0];
        String snFull = parts[1];
        String anFull = parts[2];

        String pn = pnFull.replaceFirst("P/N=", "");
        String sn = snFull.replaceFirst("S/N=", "");
        String an = anFull.replaceFirst("A/N=", "");

        for (int i = 0; i < 15; i++) {
            if (i < pn.length()) partNumber[i] = (byte) pn.charAt(i);
            if (i < sn.length()) serialNumber[i] = (byte) sn.charAt(i);
        }

        String[] anParts = an.split("\\.");
        for (int i = 0; i < assetNumber.length; i++) {
            assetNumber[i] = Byte.parseByte(anParts[i]);
        }

        ByteArrayOutputStream res = new ByteArrayOutputStream();
        res.write(partNumber);
        res.write(serialNumber);
        res.write(assetNumber);

        return res.toByteArray();
    }

    static {
        softwareMap.put("1", "P/N=SM-RU1353, S/N=21KC07006857, A/N=-25.0.77.83.8.18.-16");
        softwareMap.put("2", "P/N=SM-RU1353, S/N=39K820073069, A/N=99.1.47.78.32.28.-16");
        softwareMap.put("3", "P/N=SM-RU1353, S/N=21KE22015806, A/N=-57.4.77.83.7.20.-14");
        softwareMap.put("4", "P/N=SM-RU1353, S/N=21KA06005906, A/N=69.2.77.83.2.16.-16");
        softwareMap.put("5", "P/N=SM-RU1353, S/N=39K744052627, A/N=82.0.77.83.6.77.-16");
        softwareMap.put("6", "P/N=SM-RU1353, S/N=21KD11010097, A/N=-52.9.77.83.80.18.-14");
        softwareMap.put("7", "P/N=SM-RU1353, S/N=39K721030377, A/N=96.1.77.83.25.7-16");
    }

    public static Map<String, String> getSoftwareMap() {
        return softwareMap;
    }
}
