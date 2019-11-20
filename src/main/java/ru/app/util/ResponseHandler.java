package ru.app.util;

import ru.app.hardware.smartPayout.Manager;
import ru.app.hardware.ucs.Client;
import ru.app.main.Settings;
import ru.app.protocol.bne.DepositTable;
import ru.app.protocol.cctalk.payout.EventType;
import ru.app.protocol.ucs.UCSMessage;

import java.util.Arrays;
import java.util.Map;

public class ResponseHandler {
    private static Map<String, byte[]> deposits = DepositTable.getDeposits();

    /**
     * Parsing buffer for each peripherial, based on what streamType we have
     *
     * @param streamType some input/output (decrypt/encrypt) types
     * @param buffer     bytes, which we pass here
     * @return string value, represented more convenient for us
     */
    static String parseResponse(StreamType streamType, byte[] buffer) {
        switch (Settings.hardware) {
            case BNE_S110M:
                StringBuilder result = new StringBuilder("\n");
                if (streamType == StreamType.INPUT) {
                    if (buffer.length > 50) {
                        for (int i = 0; i < buffer.length - 4; i++) {
                            // we can not parse first 64 bytes, cuz there we find COUNTER (A, 0, 0, 0) or another, it's can be same of deposit
                            if (i < 64) continue;
                            byte[] temp = new byte[]{buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3]};
                            for (String s : deposits.keySet()) {
                                byte[] deposit = deposits.get(s);
                                if (Arrays.equals(deposit, temp)) {
                                    byte[] countHex = new byte[]{buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7]}; // next 4 bytes block
                                    Utils.reverse(countHex);
                                    int count = Integer.parseInt(Utils.toHexString(countHex), 16);

                                    result.append("Nominal detected : ").append(s).append(" (count = ").append(count).append(")\t");
                                    i = i + 6; // skip unnecessary cells
                                }
                            }
                        }
                    }
                }
                return result.toString();

            case SMART_PAYOUT:
                byte resp;
                if (buffer.length > 6) {
                    resp = buffer[4];
                } else {
                    resp = streamType == StreamType.INPUT ? buffer[3] : buffer[buffer.length - 2];
                }
                EventType eventType = EventType.valueOf(resp);

                if (eventType == EventType.NoteRead) {
                    Utils.sleep(100);
                    switch (buffer[6]) {
                        case (byte) 0x07:
                            Manager.flag = true;
                        case (byte) 0x13:
                            Manager.flag = true;
                        default:
                    }
                }
                if (eventType == EventType.NoteCredit) {
                    return eventType.toString();
                }
                if (eventType != null) {
                    return eventType.toString();
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    public static String parseUCS(byte[] buffer) {
        if (buffer == null) {
            return null;
        }
        if (buffer.length == 1) {
            switch (buffer[0]) {
                case 0x04:
                    return UCSMessage.EOT.toString();
                case 0x05:
                    return UCSMessage.ENQ.toString();
                case 0x06:
                    return UCSMessage.ACK.toString();
                case 0x15:
                    return UCSMessage.NAK.toString();
                default:
                    return null;
            }
        }
        if (buffer.length == 2) {
            if (buffer[0] == Client.DLE && buffer[1] == Client.STX)
                return "DLE/STX";
            if (buffer[0] == Client.DLE && buffer[1] == Client.ETX)
                return "DLE/ETX";
        }
        if (buffer.length > 6) {
            // buffer[2] = class; buffer[3] = operation.
            byte cl = buffer[2];
            byte op = buffer[3];

            // TODO...
        }
        return null;
    }
}
