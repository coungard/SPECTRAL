package ru.app.protocol.bne;

public enum CommandType {
    SetCurrencyCurrent((byte) 0x01), // устанавливает номинал (0‐CNY 3‐RUB 15‐BYR)
    StartSortMix((byte) 0x02),
    StopSortMix((byte) 0x03),
    StartDeposit((byte) 0x06),
    CashNotes((byte) 0x08),
    DispenseNotes((byte) 0x09),
    ActivateDepositProcedures((byte) 0x1E),
    OpenRecoverDoor((byte) 0x22),
    CloseRecoverDoor((byte) 0x23),
    OpenShutter((byte) 0x35),
    CloseShutter((byte) 0x36),
    Initialize((byte) 0x53),
    Restart((byte) 0x70);

    private byte code;

    CommandType(byte code) {
        this.code = code;
    }
}
