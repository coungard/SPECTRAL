package ru.app.protocol.ccnet;

public enum StatusType {
    ACK((byte)0x00),
    NAK((byte)0xFF),
    DISABLED((byte) 0x19),
    ENABLED((byte) 0x14);

    private byte code;

    StatusType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static CommandType getTypeByCode(byte code) {
        for (CommandType obj : CommandType.values()) {
            if (obj.getCode() == code)
                return obj;
        }
        return null;
    }
}
