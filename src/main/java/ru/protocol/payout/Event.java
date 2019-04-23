package ru.protocol.payout;

public class Event {
    private EventType eventType;
    private byte[] data;

    public Event(EventType eventType) {
        this.eventType = eventType;
    }

    public Event(EventType eventType, byte[] data) {
        this.eventType = eventType;
        this.data = data;
    }

    public EventType getresponseType() {
        return eventType;
    }

    public void setresponseType(EventType responseType) {
        this.eventType = responseType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response: " + (eventType != null ? eventType : "null");
    }

    public String dataToString() {
        return data != null ? new String(data) : "";
    }
}
