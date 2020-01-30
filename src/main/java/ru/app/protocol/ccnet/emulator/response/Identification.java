package ru.app.protocol.ccnet.emulator.response;

import ru.app.main.Settings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Identification extends EmulatorCommand {
    private static Map<String, String> softwareMap = new LinkedHashMap<>();

    @Override
    public byte[] getData() throws IOException {
        return getSoftwareIdentification(Settings.propEmulator.get("casher.soft"));
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
        softwareMap.put("2", "P/N=SM-RU1353, S/N=39K820073069, A/N=99.14.77.83.2.28.-16");
        softwareMap.put("3", "P/N=SM-RU1353, S/N=21KE22015806, A/N=-57.4.77.83.7.20.-14");
        softwareMap.put("4", "P/N=SM-RU1353, S/N=21KA06005906, A/N=69.2.77.83.2.16.-16");
        softwareMap.put("5", "P/N=SM-RU1353, S/N=39K744052627, A/N=82.0.77.83.6.77.-16");
        softwareMap.put("6", "P/N=SM-RU1353, S/N=21KD11010097, A/N=-52.9.77.83.80.18.-14");
        softwareMap.put("7", "P/N=SM-RU1353, S/N=39K721030377, A/N=96.1.77.83.25.7.-16");
        softwareMap.put("8", "P/N=SM-RU1353, S/N=21K930003610, A/N=-87.0.77.83.48.9.-16");
        softwareMap.put("9", "P/N=SM-RU1353, S/N=21KB20021138, A/N=11.30.77.83.24.17.-16");
        softwareMap.put("10", "P/N=SM-RU1353, S/N=21KD08005839, A/N=-83.0.77.83.66.18.-14");
        softwareMap.put("11", "P/N=SM-RU1353, S/N=21K842041343, A/N=77.8.77.83.4.88.-16");
        softwareMap.put("12", "P/N=SM-RU1353, S/N=21KA05005518, A/N=89.1.77.83.3.16.-16");
        softwareMap.put("13", "P/N=SM-RU1353, S/N=21KD10008439, A/N=12.3.77.83.7.19.-16");
        softwareMap.put("14", "P/N=SM-RU1353, S/N=21KB14014655, A/N=-10.10.77.83.19.17.-16");
        softwareMap.put("15", "P/N=SM-RU1353, S/N=21KA48048474, A/N=-31.0.77.83.70.16.-16");
        softwareMap.put("16", "P/N=SM-RU1353, S/N=21KC28024911, A/N=-70.2.77.83.41.18.-16");
        softwareMap.put("17", "P/N=SM-RU1353, S/N=21K911068384, A/N=-89.1.77.83.1.79.-16");
        softwareMap.put("18", "P/N=SM-RU1353, S/N=21KD11008504, A/N=59.2.77.83.70.18.-14");
        softwareMap.put("19", "P/N=SM-RU1353, S/N=21KA37033996, A/N=7.7.77.83.55.16.-16");
        softwareMap.put("20", "P/N=SM-RU1353, S/N=21K918071642, A/N=97.5.77.83.2.19.-16");
        softwareMap.put("21", "P/N=SM-RU1353, S/N=21K825010969, A/N=110.1.77.83.36.8.-16");
        softwareMap.put("22", "P/N=SM-RU1353, S/N=21K918071410, A/N=44.0.77.83.2.39.-16");
        softwareMap.put("23", "P/N=SM-RU1353, S/N=21K946018599, A/N=-12.12.77.83.6.89.-16");
        softwareMap.put("24", "P/N=SM-RU1353, S/N=21K915069623, A/N=72.4.77.83.2.19.-16");
        softwareMap.put("25", "P/N=SM-RU1353, S/N=21KB10011126, A/N=40.3.77.83.17.17.-16");
        softwareMap.put("26", "P/N=SM-RU1353, S/N=21KC23019995, A/N=14.2.77.83.36.18.-16");
        softwareMap.put("27", "P/N=SM-RU1353, S/N=39K818072374, A/N=72.1.77.83.23.8.-16");
        softwareMap.put("28", "P/N=SM-RU1353, S/N=39K742049104, A/N=-52.2.77.83.57.7.-16");
        softwareMap.put("29", "P/N=SM-RU1353, S/N=21KA49050239, A/N=-12.52.77.83.80.16.-16");
        softwareMap.put("30", "P/N=SM-RU1353, S/N=21K944017987, A/N=85.0.77.83.68.9.-16");
        softwareMap.put("31", "P/N=SM-RU1353, S/N=21K918071503, A/N=-10.83.77.83.19.9.-16");
        softwareMap.put("32", "P/N=SM-RU1353, S/N=21KC24020848, A/N=50.1.77.83.36.18.-16");
        softwareMap.put("33", "P/N=SM-RU1353, S/N=21KC45043969, A/N=-12.22.77.83.53.18.-16");
        softwareMap.put("34", "P/N=SM-RU1353, S/N=39K743052081, A/N=82.2.77.83.6.77.-16");
        softwareMap.put("35", "P/N=SM-RU1353, S/N=39K807061877, A/N=16.10.77.83.6.77.-16");
        softwareMap.put("36", "P/N=SM-RU1353, S/N=21K906062082, A/N=41.2.77.83.7.9.-16");
        softwareMap.put("37", "P/N=SM-RU1353, S/N=21KA29254532, A/N=68.5.77.83.40.16.-16");
        softwareMap.put("38", "P/N=SM-RU1353, S/N=39K813067761, A/N=-51.2.77.83.19.8.-16");
        softwareMap.put("39", "P/N=SM-RU1353, S/N=21KD39035248, A/N=-10.72.77.83.19.19.-16");
        softwareMap.put("40", "P/N=SM-RU1353, S/N=21K945016308, A/N=31.2.77.83.5.49.-16");
    }

    public static Map<String, String> getSoftwareMap() {
        return softwareMap;
    }
}
