package ru.app.protocol.ccnet.emulator;

import ru.app.protocol.ccnet.CCNetCommand;

public enum ResponseType implements CCNetCommand {
    GetStatus,
    GetBillTable,
    Identefication;

    ResponseType() {
    }

    @Override
    public byte getCode() {
        return 0;
    }
}
