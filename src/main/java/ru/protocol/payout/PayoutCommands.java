package ru.protocol.payout;

import ru.protocol.CCTalkCommand;

public enum PayoutCommands implements CCTalkCommand {
    SimplePoll((byte) 254),
    AddressPoll((byte) 253),
    REQ_ManufacturerId((byte) 246),
    RequestEquipmentCategoryId((byte) 245),
    SetNoteInhibitStatus((byte) 231),
    ModifyMasterInhibit((byte) 228), // data bit 0 = disabled, bit 1 = enabled
    ReadBufferedBillEvents((byte) 159),
    RouteBill((byte) 154), // data bit 0 = Return escrow bill 1 = send to stack 255 = extend escrow hold time.
    ModifyBillOperatingMode((byte) 153), // data bit 0 = disable escrow, bit 1 = enabled
    PerformStackerCycle((byte) 147),
    RequestEncryptionSupport((byte) 111),
    SetCashboxPayoutLimits((byte) 54), // (Smart System)
    RequestStatus((byte) 47),
    PayoutByDenominationCurrent((byte) 44),
    SetRouting((byte) 37),              // data bit 0 = payout, 1 = cashbox
    PayoutByDenomination((byte) 32),
    PayoutAmount((byte) 22),
    ResetDevice((byte) 1);

    private byte code;

    PayoutCommands(byte code) {
        this.code = code;
    }

    @Override
    public byte getCode() {
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
    public static PayoutCommands valueOf(byte b) {
        for (PayoutCommands type : PayoutCommands.values())
            if (type.code == b)
                return type;
        return null;
    }
}
