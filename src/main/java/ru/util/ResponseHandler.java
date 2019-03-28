package ru.util;

import ru.protocol.BillTable;
import ru.protocol.Command;
import ru.protocol.EventType;
import ru.protocol.StreamType;

class ResponseHandler {
    static String parseResponse(StreamType streamType, byte[] input) {
        byte resp;
        if (input.length > 6) {
            resp = input[4];
        } else {
            resp = streamType == StreamType.INPUT ? input[3] : input[input.length - 2];
        }
        EventType eventType = EventType.valueOf(resp);
        if (eventType == EventType.NoteCredit) {
            return eventType.toString() + "{" + BillTable.getTable().get(input[6]) + " ITL}";
        }
        if (eventType != null) {
            return eventType.toString();
        } else {
            return null;
        }
    }
}
