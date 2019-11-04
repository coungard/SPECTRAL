package ru.app.protocol.ccnet.emulator;

import java.util.LinkedHashMap;
import java.util.Map;

public class BillTable {
    private Map<String, byte[]> table = new LinkedHashMap<>();

    public BillTable() {
        table.put("10 RUB", new byte[]{(byte) 0x02});
        table.put("50 RUB", new byte[]{(byte) 0x03});
        table.put("100 RUB", new byte[]{(byte) 0x04});
        table.put("500 RUB", new byte[]{(byte) 0x05});
        table.put("1000 RUB", new byte[]{(byte) 0x06});
        table.put("5000 RUB", new byte[]{(byte) 0x07});
        table.put("1 RUB", new byte[]{(byte) 0x08});
        table.put("2 RUB", new byte[]{(byte) 0x09});
        table.put("5 RUB", new byte[]{(byte) 0x10});
//        table.put("10 RUB", new byte[]{(byte) 0x11}); // coin
        table.put("200 RUB", new byte[]{(byte) 0x12});
        table.put("2000 RUB", new byte[]{(byte) 0x13});
    }

    public Map<String, byte[]> getTable() {
        return table;
    }
}
