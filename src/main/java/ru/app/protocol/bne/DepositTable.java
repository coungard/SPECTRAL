package ru.app.protocol.bne;

import java.util.HashMap;

public class DepositTable {
    private static HashMap<String, byte[]> deposits = new HashMap<>();

    static {
        deposits.put("10", new byte[]{(byte) 0x0A, 0, 0, 0});
        deposits.put("20", new byte[]{(byte) 0x14, 0, 0, 0});
        deposits.put("50", new byte[]{(byte) 0x32, 0, 0, 0});
        deposits.put("100", new byte[]{(byte) 0x64, 0, 0, 0});
        deposits.put("200", new byte[]{(byte) 0xC8, 0, 0, 0});
        deposits.put("500", new byte[]{(byte) 0xF4, 1, 0, 0});
        deposits.put("1000", new byte[]{(byte) 0xE8, 3, 0, 0});
        deposits.put("2000", new byte[]{(byte) 0xD0, 7, 0, 0});
        deposits.put("5000", new byte[]{(byte) 0x88, 13, 0, 0});
    }

    public static HashMap<String, byte[]> getDeposits() {
        return deposits;
    }
}
