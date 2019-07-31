package ru.app.protocol.cctalk.coinMachine;

import java.util.LinkedHashMap;
import java.util.Map;

public class CoinTable {
    private Map<Integer, String> table = new LinkedHashMap<>();

    public CoinTable() {
        table.put(1, "MA050A");
        table.put(2, "MA050B");
        table.put(3, "MA100A");
        table.put(4, "MA100B");
        table.put(5, "MA100C");
        table.put(6, "MA200A");
        table.put(7, "MA500A");
        table.put(8, "MA500B");
        table.put(9, "MA500C");
        table.put(10, "MA500D");
        table.put(11, "MA1K0A");
        table.put(12, "MA1K0B");
        table.put(13, "MA1K0C");
        table.put(14, "......");
        table.put(15, "......");
        table.put(16, "......");
    }

    public Map<Integer, String> getTable() {
        return table;
    }
}
