package ru.app.protocol.cctalk;

import ru.app.protocol.cctalk.payout.BillTable;
import ru.app.util.Utils;

import java.util.Formatter;


public class Nominal {
    private String note;
    private String country = "RUB";

    public Nominal(String note) {
        this.note = note;
    }

    public byte[] getValue() {
        byte[] array = new byte[7];
        String hex = new Formatter().format("%08X", BillTable.getTable().get(note)).toString();

        String nominal = Utils.inverse(hex);
        char[] country = this.country.toCharArray();
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

        return array;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
