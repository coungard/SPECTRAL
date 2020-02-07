package ru.app.protocol.cctalk;

import ru.app.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Formatter;


public class Nominal {
    private String country = "RUB";
    private String note;
    private byte[] value;

    public Nominal(String note) {
        this.note = note;
        createValue();
    }

    /**
     * Конвертация валюты в 7-значный массив байтов, где первые 4 байта - это сам номинал, умноженный на 100 и инвертированный
     * в Хексодецимальном выражении, оставшие 3 - ASCII представление названия валюты. <br>
     * Пример: value = "10" --> new byte[]{E8  3  0  0 52 55 42}
     */
    public void createValue() {
        byte[] array = new byte[7];
        int numeric = BillTable.getTable().get(note);
        String hex = new Formatter().format("%08X", numeric).toString();

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
        value = array;
    }

    public byte[] getValue() {
        return value;
    }

    /**
     * Вставляет 2-х значное инвертированное значение кол-ва монет между 4-мя байтами номинала и 3-мя байтами названия валюты
     *
     * @param level кол-во номиналов
     * @return преобразовынный 9-значный byte array
     */
    public byte[] setLevel(int level) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < value.length; i++) {
            if (i == 4) {
                baos.write(getByteLen(level));
            }
            baos.write(value[i]);
        }
        return baos.toByteArray();
    }

    private byte[] getByteLen(int value) {
        return new byte[]{
                (byte) value,
                (byte) (value >>> 8)};
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
