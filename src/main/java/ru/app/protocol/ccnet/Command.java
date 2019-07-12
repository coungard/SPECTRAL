package ru.app.protocol.ccnet;

import ru.app.util.Utils;

/**
 * Команда протокола
 */
public class Command {
    private CCNetCommand getType;
    private byte[] data;

    public Command(CCNetCommand getType) {
        this.getType = getType;
    }

    public Command(CCNetCommand getType, byte[] data) {
        this.getType = getType;
        this.data = data;
    }

    public CCNetCommand getType() {
        return getType;
    }

    public void setType(CCNetCommand getType) {
        this.getType = getType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Command: " + (getType != null ? getType : "null") +
                "; " + (data != null ? "Data: " + Utils.byteArray2String(data, 0, data.length) : "");
    }

    public String dataToString() {
        return data != null ? new String(data) : "";
    }
}
