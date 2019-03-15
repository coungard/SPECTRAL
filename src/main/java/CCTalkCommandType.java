
public enum CCTalkCommandType {
    SimplePoll((byte) 254),
    REQ_ManufacturerId((byte) 246),
    RequestEquipmentCategoryId((byte) 245),
    ModifyInhibitStatus((byte) 231),
    ModifyMasterInhibit((byte) 228),
    ReadBufferedBillEvents((byte) 159),
    ModifyBillOperatingMode((byte) 153),
    PerformStackerCycle((byte) 147),
    ResetDevice((byte) 1);

    private byte code;

    CCTalkCommandType(byte code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String toString() {
//        return this.name() +
//                '(' + Utils.getByteValue(code) + ')';
        return String.valueOf(code);
    }

    /**
     * Определение типа по коду
     */
    public static CCTalkCommandType valueOf(byte b) {
        for (CCTalkCommandType type : CCTalkCommandType.values())
            if (type.code == b)
                return type;
        return null;
    }
}
