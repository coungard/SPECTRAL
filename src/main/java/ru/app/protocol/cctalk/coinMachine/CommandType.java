package ru.app.protocol.cctalk.coinMachine;

public enum CommandType {

    RequestCoinId((byte) 0xB8),
    ReadBufferedCreditOrErrorCodes((byte) 0xE5);

    private byte code;

    CommandType(byte code) {
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
