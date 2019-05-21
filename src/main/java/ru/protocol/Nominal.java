package ru.protocol;

import ru.app.main.Settings;
import ru.protocol.payout.BillTable;
import ru.util.Utils;

import java.util.Arrays;
import java.util.Formatter;


public class Nominal {
    private String note;
    private byte[] value;

    public Nominal(byte[] value) {
        this.value = value;
    }

    public Nominal(String note) {
        this.note = note;
    }

    public byte[] getValue() {
        byte[] array = new byte[7];
        String hex = new Formatter().format("%08X", BillTable.getTable().get(note)).toString();

        String nominal = Utils.inverse(hex);
        char[] country = Settings.COUNTRY.toCharArray();
        StringBuilder countryBytes = new StringBuilder();
        for (char c : country) {
            countryBytes.append(new Formatter().format("%02X", (int) c));
        }

        nominal += countryBytes;

        int temp = 0;
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) Long.parseLong(nominal.substring(temp, temp + 2), 16);
            temp = temp + 2;
        }

        System.out.println(Arrays.toString(array));
        return array;
    }
}
