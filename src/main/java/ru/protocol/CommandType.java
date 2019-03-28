package ru.protocol;

public enum CommandType {
    SimplePoll((byte) 254),
    REQ_ManufacturerId((byte) 246),
    RequestEquipmentCategoryId((byte) 245),
    ModifyInhibitStatus((byte) 231),
    ModifyMasterInhibit((byte) 228), // data bit 0 = disabled, bit 1 = enabled
    ReadBufferedBillEvents((byte) 159),
    ModifyBillOperatingMode((byte) 153), // data bit 0 = disable escrow, bit 1 = enabled
    PerformStackerCycle((byte) 147),
    SetCashboxPayoutLimits((byte) 54), // (Smart System)
    RequestStatus((byte) 47),
    PayoutByDenominationCurrent((byte) 44),
    PayoutByDenomination((byte) 32),
    PayoutAmount((byte) 22),
    ResetDevice((byte) 1);

    private byte code;

    CommandType(byte code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String toString() {
        return this.name();
//                '(' + ru.util.Utils.getByteValue(code) + ')';
//        return String.valueOf(code);
    }

    /**
     * Определение типа по коду
     */
    public static CommandType valueOf(byte b) {
        for (CommandType type : CommandType.values())
            if (type.code == b)
                return type;
        return null;
    }
}
