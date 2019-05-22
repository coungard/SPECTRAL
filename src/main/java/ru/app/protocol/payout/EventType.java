package ru.app.protocol.payout;

public enum EventType {
    PeripheralDeviceDisabled((byte) 0x38), // Smart System
    CashboxReplaced((byte) 0x20), // Smart Payout
    CashboxRemoved((byte) 0x1F), // Smart Payout
    Stacking((byte) 0x18), // Smart Payout
    Rejected((byte) 0x17), // Smart Payout {The device has rejected an invalid bill back to the user}
    Rejecting((byte) 0x16), // Smart Payout {The device rejecting an invalid bill back to the user}
    NoteCredit((byte) 0x15), // Smart Payout
    NoteRead((byte) 0x14), // Smart Payout
    Disabled((byte) 0x11), // Smart Payout & Smart System
    Idle((byte) 0x00); // Smart Payout & Smart System

    private byte code;

    EventType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public String toString() {
        return this.name();
    }

    /**
     * Определение типа по коду
     */
    public static EventType valueOf(byte b) {
        for (EventType type : EventType.values())
            if (type.code == b)
                return type;
        return null;
    }
}
