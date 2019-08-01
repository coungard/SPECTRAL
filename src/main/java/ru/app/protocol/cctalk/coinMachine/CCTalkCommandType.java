package ru.app.protocol.cctalk.coinMachine;

public enum CCTalkCommandType {

    RequestCoinId((byte) 0xB8),
    ReadBufferedCreditOrErrorCodes((byte) 0xE5);

    private byte code;

    CCTalkCommandType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static CCTalkCommandType getTypeByCode(byte code) {
        for (CCTalkCommandType obj : CCTalkCommandType.values()) {
            if (obj.getCode() == code)
                return obj;
        }
        return null;
    }
}
