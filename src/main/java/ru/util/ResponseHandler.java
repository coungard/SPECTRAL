package ru.util;

import ru.app.smartPayout.Manager;
import ru.protocol.payout.EventType;
import ru.protocol.payout.StreamType;

public class ResponseHandler {
    public volatile static int currentNote = 0;
    public volatile static boolean noteRead;

    static String parseResponse(StreamType streamType, byte[] input) {
        byte resp;
        if (input.length > 6) {
            resp = input[4];
        } else {
            resp = streamType == StreamType.INPUT ? input[3] : input[input.length - 2];
        }
        EventType eventType = EventType.valueOf(resp);

        if (eventType == EventType.NoteRead) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switch (input[6]) {
                case (byte) 0x07:
                    currentNote = 20;
                    noteRead = true;
                    Manager.flag = true;
                case (byte) 0x13:
                    currentNote = 50;
                    Manager.flag = true;
                default:
                    currentNote = 0;
            }
        } else {
            noteRead = false;
            currentNote = 0;
        }
        if (eventType == EventType.NoteCredit) {
//            return eventType.toString() + "{" + BillTable.getTable().get(input[6]) + " ITL}";
            return eventType.toString();
        }
        if (eventType != null) {
            return eventType.toString();
        } else {
            return null;
        }
    }
}
