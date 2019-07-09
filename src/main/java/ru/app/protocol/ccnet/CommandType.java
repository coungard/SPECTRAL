/**
 * 
 */
package ru.app.protocol.ccnet;

/**
 * @author denis
 *
 */
public enum CommandType {
	
	ACK((byte)0x00),
	NAK((byte)0xFF),
//	ILLEGAL_COMMAND((byte)0x30),
	Reset((byte)0x30),
	GetStatus((byte)0x31),
	SetSecurity((byte)0x32),
	Poll((byte)0x33),
	EnableBillTypes((byte)0x34),
	Stack((byte)0x35),
	Return((byte)0x36),
	Identification((byte)0x37),
	Hold((byte)0x38),
	SetBarcodeParameters((byte)0x39),
	ExtractBarcodeData((byte)0x3A),
	GetBillTable((byte)0x41),
	GetCRC32OfTheCode((byte)0x51),
	Download((byte)0x50),
	RequestStatistics((byte)0x60);
	
	private byte code;
	
	CommandType(byte code) {
		this.code = code;
	}
	
	public byte getCode() {
		return code;
	}
	
	public static CommandType getTypeByCode(byte code) {
		for (CommandType obj : CommandType.values()) {
			if (obj.getCode() == code)
				return obj;
		}
		return null;
	}
}
