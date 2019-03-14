/**
 * Команда протокола
 */
public class CCTalkCommand
{
    protected CCTalkCommandType commandType;
    protected byte[] data;

    public CCTalkCommand(CCTalkCommandType commandType) {
        this.commandType = commandType;
    }

    public CCTalkCommand(CCTalkCommandType commandType, byte[] data) {
        this.commandType = commandType;
        this.data = data;
    }

    public CCTalkCommandType getCommandType()
    {
        return commandType;
    }

    public void setCommandType(CCTalkCommandType commandType)
    {
        this.commandType = commandType;
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }

    @Override
    public String toString()
    {
        return "Command: " + (commandType != null ? commandType : "null") +
                "; " +
                "Data: " + (data != null ? Utils.byteArray2String(data, 0, data.length) : "null");
    }

    public String dataToString()
    {
        return data != null ? new String(data) : "";
    }
}
