package ru.app.protocol.cctalk.hopper;

import ru.app.protocol.cctalk.CCTalkEvent;

public enum HopperEvent implements CCTalkEvent {

    Dispensing((byte) 0x01),
    Idle((byte) 0x00),
    Dispensed((byte) 0x02),
    CoinsLow((byte) 0x03),
    Empty((byte) 0x04),
    Halted((byte) 0x06),
    Floating((byte) 0x07),
    Floated((byte) 0x08),
    Timeout((byte) 0x09),
    CashboxPaid((byte) 0x0C),   // 12
    CoinCredit((byte) 0x0D),    // 13
    Emptying((byte) 0x0E),      // 14
    Emptied((byte) 0x0F),       // 15
    FraudAttempt((byte) 0x10),  // 16
    Disabled((byte) 0x11),      // 17
    SlaveReset((byte) 0x13),   // 19
    LidOpen((byte) 0x21),       // 33
    LidClosed((byte) 0x22),     // 34
    CalibrationFault((byte) 0x24),   // 36
    AttachedMechJam((byte) 0x25),    // 37
    AttachedMechOpen((byte) 0x26),   // 38
    SmartEmptying((byte) 0x27),      // 39
    SmartEmptied((byte) 0x28),      // 40
    IncompletePayout((byte) 0x0A),   // 10
    IncompleteFloat((byte) 0x0B);    // 11

    private byte code;

    HopperEvent(byte code) {
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
    public static HopperEvent valueOf(byte b) {
        for (HopperEvent type : HopperEvent.values())
            if (type.code == b)
                return type;
        return null;
    }
}
