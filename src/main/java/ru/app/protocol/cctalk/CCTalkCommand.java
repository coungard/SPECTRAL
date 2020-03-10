package ru.app.protocol.cctalk;

public enum CCTalkCommand implements CCTalkCommandType {

    GetCurrentInterface             ((byte) 255), //not working on current device (CC2 return expected)
    SimplePoll                      ((byte) 254),
    AddressPoll                     ((byte) 253),
    RequestManufacturerID           ((byte) 246),
    RequestEquipmentCategoryID      ((byte) 245),
    RequestProductCode              ((byte) 244),
    RequestSoftwareRevision         ((byte) 241),
    SetNoteInhibitStatus            ((byte) 231),
    ModifyMasterInhibit             ((byte) 228), // data bit 0 = disabled, bit 1 = enabled
    RequestAddressMode              ((byte) 169),
    ReadBufferedBillEvents          ((byte) 159),
    REQUEST_BILL_ID                 ((byte) 157),
    RouteBill                       ((byte) 154), // data bit 0 = Return escrow bill 1 = send to stack 255 = extend escrow hold time.
    ModifyBillOperatingMode         ((byte) 153), // data bit 0 = disable escrow, bit 1 = enabled
    PerformStackerCycle             ((byte) 147),
    RequestEncryptionSupport        ((byte) 111),
    SetCashboxPayoutLimits          ((byte) 54), // (Smart System)
    MC_REQUEST_STATUS               ((byte) 47),
    MC_GET_DEVICE_SETUP             ((byte) 46),
    PayoutByDenominationCurrent     ((byte) 44),
    MC_SET_DENOMINATION_AMOUNT      ((byte) 43),
    MC_GET_NOTE_AMOUNT              ((byte) 42),
    MC_GET_MINIMUM_PAYOUT           ((byte) 41),
    SetRouting                      ((byte) 37), // data bit 0 = payout, 1 = cashbox
    PayoutByDenomination            ((byte) 32),
    RequestStatus                   ((byte) 29),
    GetMinimumPayout                ((byte) 25),
    PayoutAmount                    ((byte) 22),
    RequestCommsRevision            ((byte) 4),
    ResetDevice                     ((byte) 1);

    private byte code;

    CCTalkCommand(byte code) {
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
    public static CCTalkCommand valueOf(byte b) {
        for (CCTalkCommand type : CCTalkCommand.values())
            if (type.code == b)
                return type;
        return null;
    }
}
