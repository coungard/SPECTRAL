package ru.app.protocol.ccnet.emulator;

import java.util.LinkedHashMap;
import java.util.Map;

public class BillTable {
    private Map<String, byte[]> table = new LinkedHashMap<>();

    public BillTable() {
        table.put("10", new byte[]{(byte) 0x02});
        table.put("50", new byte[]{(byte) 0x03});
        table.put("100", new byte[]{(byte) 0x04});
        table.put("500", new byte[]{(byte) 0x05});
        table.put("1000", new byte[]{(byte) 0x06});
        table.put("5000", new byte[]{(byte) 0x07});
        table.put("1", new byte[]{(byte) 0x08});
        table.put("2", new byte[]{(byte) 0x09});
        table.put("5", new byte[]{(byte) 0x0A});
//        table.put("10 RUB", new byte[]{(byte) 0x0B}); // coin
        table.put("200", new byte[]{(byte) 0x0C});
        table.put("2000", new byte[]{(byte) 0x0D});
    }

    public Map<String, byte[]> getTable() {
        return table;
    }
}
