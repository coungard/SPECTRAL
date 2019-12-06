package ru.app.protocol.ccnet;

import ru.app.util.Utils;

import java.io.IOException;

/**
 * Команда протокола
 */
public class Command {
    private CCNetCommand type;
    private byte[] data;
    private boolean emulator;

    public Command() {
        emulator = true;
    }

    public Command(CCNetCommand type) {
        this.type = type;
    }

    public Command(CCNetCommand type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public CCNetCommand getType() {
        return type;
    }

    public void setType(CCNetCommand type) {
        this.type = type;
    }

    public byte[] getData() throws IOException {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isEmulator() {
        return emulator;
    }

    @Override
    public String toString() {
        return "" + (type != null ? type : "null") +
                "; " + (data != null ? "Data: " + Utils.byteArray2String(data, 0, data.length) : "");
    }

    public String dataToString() {
        return data != null ? new String(data) : "";
    }
}
