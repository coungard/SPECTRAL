package ru.protocol;

import java.util.HashMap;

public class BillTable {
    private static HashMap<Byte, Integer> table = new HashMap<>();

    static {
        table.put((byte) 0x07, 20); // 20 ITL
        table.put((byte) 0x13, 50); // 50 ITL
    }

    public static HashMap<Byte, Integer> getTable() {
        return table;
    }
}
