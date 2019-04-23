package ru.protocol;

import ru.util.Utils;

/**
 * Команда протокола
 */
public class Command {
    public CCTalkCommand commandType;
    private byte[] data;

    public Command(CCTalkCommand commandType) {
        this.commandType = commandType;
    }

    public Command(CCTalkCommand commandType, byte[] data) {
        this.commandType = commandType;
        this.data = data;
    }

    public CCTalkCommand getCommandType() {
        return commandType;
    }

    public void setCommandType(CCTalkCommand commandType) {
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
