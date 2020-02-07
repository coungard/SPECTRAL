package ru.app.protocol.cctalk.hopper;

import ru.app.protocol.cctalk.CCTalkEvent;

public enum ErrorEvent implements CCTalkEvent {

    NotEnoughValueInDevice((byte) 0x01),
    CannotPayThisExactAmount((byte) 0x02),
    DeviceBusy((byte) 0x03),
    DeviceDisabled((byte) 0x04),
    DeviceLid_PathOpen((byte) 0x05),
    DeviceJam((byte) 0x06),
    CalibrationError((byte) 0x07),
    FraudDetected((byte) 0x08),
    DeviceDisconnected((byte) 0x09);

    private byte code;

    ErrorEvent(byte code) {
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
    public static ErrorEvent valueOf(byte b) {
        for (ErrorEvent type : ErrorEvent.values())
            if (type.code == b)
                return type;
        return null;
    }
}
