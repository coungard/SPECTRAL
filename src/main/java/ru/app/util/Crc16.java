package ru.app.util;

public class Crc16 {

    private int crc = 0;          // initial value
    private final int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)

    public int getCRC() {
        return crc & 0xffff;
    }

    public void update(byte[] bytes) {
        for (byte b : bytes) {
            update(b);
        }
    }

    public void update(byte b) {
        for (int i = 0; i < 8; i++) {
            boolean bit = ((b   >> (7-i) & 1) == 1);
            boolean c15 = ((crc >> 15    & 1) == 1);
            crc <<= 1;
            if (c15 ^ bit) crc ^= polynomial;
        }
    }

    /**
     Высчитать контрольную сумму (crc16 - Kermit)
     */
    public static int crc16(byte[] data) {
        int crc = 0;
        for (byte datum : data) {
            crc ^= datum & 0xFF;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = 0x8408 ^ crc >>> 1;
                } else {
                    crc >>>= 1;
                }
            }
        }
        return crc;
    }

    public static int checkCrc16(byte[] data, int offset, int count) {
        int crc = 0;
        for (int i = offset; i < count; i++) {
            crc ^= data[i] & 0xFF;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = 0x8408 ^ crc >>> 1;
                } else {
                    crc >>>= 1;
                }
            }
        }
        return crc;
    }

}
