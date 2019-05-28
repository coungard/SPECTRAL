package ru.app.protocol.cctalk.payout;

import java.util.HashMap;

public class BillTable {
    private static HashMap<String, Integer> table = new HashMap<>();

    static {
        table.put("10", 1000); // 10 ITL
        table.put("20", 2000); // 20 ITL
        table.put("50", 5000); // 50 ITL
        table.put("100", 10000); // 100 ITL
        table.put("200", 20000); // 200 ITL
        table.put("500", 50000); // 500 ITL
        table.put("1000", 100000); // 1000 ITL
    }

    public static HashMap<String, Integer> getTable() {
        return table;
    }
}
