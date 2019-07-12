/**
 *
 */
package ru.app.protocol.ccnet;

/**
 * @author denis
 *
 */
public enum BillStateType implements CCNetCommand {

    PowerUp((byte) 0x10),
    PowerUpWithBillInValidator((byte) 0x11),
    PowerUpWithBillInStacjer((byte) 0x12),
    Initialize((byte) 0x13),
    Idling((byte) 0x14),
    Accepting((byte) 0x15),
    Stacking((byte) 0x17),
    Returning((byte) 0x18),
    UnitDisabled((byte) 0x19),
    Holding((byte) 0x1A),
    DeviceBusy((byte) 0x1B),
    Rejecting((byte) 0x1C),
    DropCassetteFull((byte) 0x41),
    DropCassetteOutOfPosition((byte) 0x42),
    ValidatorJammed((byte) 0x43),
    DropCassetteJammed((byte) 0x44),
    Cheated((byte) 0x45),
    Pause((byte) 0x46),
    Failure((byte) 0x47),
    EscrowPosition((byte) 0x80),
    BillStacked((byte) 0x81),
    BillReturned((byte) 0x82);


    private byte code;

    BillStateType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static BillStateType getTypeByCode(byte code) {
        for (BillStateType obj : BillStateType.values()) {
            if (obj.getCode() == code)
                return obj;
        }
        return null;
    }
}
