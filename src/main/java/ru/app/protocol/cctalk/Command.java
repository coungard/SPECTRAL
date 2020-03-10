package ru.app.protocol.cctalk;

import ru.app.util.Utils;

/**
 * Команда протокола
 */
public class Command {
    private CCTalkCommandType commandType;
    private byte[] data;

    public Command(CCTalkCommandType commandType) {
        this.commandType = commandType;
    }

    public Command(CCTalkCommandType commandType, byte[] data) {
        this.commandType = commandType;
        this.data = data;
    }

    public CCTalkCommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CCTalkCommandType commandType) {
        this.commandType = commandType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Command: " + (commandType != null ? commandType : "null") +
                "; " + (data != null ? "Data: " + Utils.byteArray2String(data, 0, data.length) : "");
    }

    public String dataToString() {
        return data != null ? new String(data) : "";
    }
}
