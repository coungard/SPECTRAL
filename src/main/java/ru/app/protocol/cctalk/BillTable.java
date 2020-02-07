package ru.app.protocol.cctalk;

import java.util.LinkedHashMap;

public class BillTable {
    private static LinkedHashMap<String, Integer> table = new LinkedHashMap<>();

    static {
        table.put("1", 100);
        table.put("2", 200);
        table.put("5", 500);
        table.put("10", 1000);
        table.put("20", 2000);
        table.put("50", 5000);
        table.put("100", 10000);
        table.put("200", 20000);
        table.put("500", 50000);
        table.put("1000", 100000);
        table.put("2000", 200000);
        table.put("5000", 500000);
    }

    public static LinkedHashMap<String, Integer> getTable() {
        return table;
    }
}
