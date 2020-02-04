package ru.app.protocol.cctalk.hopper;

import ru.app.protocol.cctalk.CCTalkCommand;

public enum HopperCommands implements CCTalkCommand {
    GetCurrentInterface((byte) 0xFF), // 255 todo...no working on current device (CC2 return expected)
    SimplePoll((byte) 0xFE), // 254
    RequestManufacturerID((byte) 0xF6), // 246
    RequestEquipmentCategoryID((byte) 0xF5), // 245
    RequestProductCode((byte) 0xF4), // 244
    RequestSoftwareRevision((byte) 0xF1), // 241
    RequestAddressMode((byte) 0xA9), // 169
    RequestStatusCur((byte) 0x2F), // 47,
    MC_GET_DEVICE_SETUP((byte) 0x2E), // 46,
    MC_SET_DENOMINATION_AMOUNT((byte) 0x2B), // 43,
    RequestStatus((byte) 0x1D), // 29
    PAYOUT_AMOUNT((byte) 0x16), // 22,
    RequestCommsRevision((byte) 0x04),
    ResetDevice((byte) 1);

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
