package ru.app.protocol.cctalk.hopper;

import ru.app.protocol.cctalk.CCTalkCommand;

public enum HopperCommands implements CCTalkCommand {
    RequestHopperCoin((byte) 171),
    GetStatusCur((byte) 47),
    GetStatus((byte) 29);

    private byte code;

    HopperCommands(byte code) {
        this.code = code;
    }

    @Override
    public byte getCode() {
        return code;
    }

    public String toString() {
        return this.name();
    }

    /**
     * Определение типа по коду
     */
    public static HopperCommands valueOf(byte b) {
        for (HopperCommands type : HopperCommands.values())
            if (type.code == b)
                return type;
        return null;
    }
}
