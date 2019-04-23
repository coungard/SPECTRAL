package ru.util;

import java.util.Formatter;

public class Utils {
    /**
     * Переводим значение байта в лонг
     */
    public static long getByteValue(byte b) {
        return 0xff & b;
    }

    /**
     * Перевод массива байтов в строку
     */
    public static String byteArray2String(byte[] buf, int offset, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < count; i++)
            sb.append(getByteValue(buf[i])).append(" ");

        return sb.toString();
    }

    /**
     * Перевод массива байтов в строку в шеснадцатиричном виде
     */
    public static String byteArray2HexString(byte[] buf) {
        Formatter formatter = new Formatter();
        for (byte b : buf) formatter.format("%X ", b);
        return formatter.toString();
    }

    /**
     * Инвертирование строки (по 2 символам - 16ричные значения)<p>Пример {"1B442C" ==> "2C441B"}
     *
     * @param s упорядоченный набор 16-ричных значений, идущих по 2 символа слитно друг за другом
     * @return обратный набор 16-ричных значений в виде String
     */
    public static String inverse(String s) {
        if ((null == s) || (s.length() <= 1)) {
            return s;
        }
        return inverse(s.substring(2)) + s.charAt(0) + s.charAt(1);
    }
}
