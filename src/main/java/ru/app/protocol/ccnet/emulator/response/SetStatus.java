package ru.app.protocol.ccnet.emulator.response;

public class SetStatus extends EmulatorCommand {
    @Override
    public byte[] getData() {
        return new byte[]{0, 0, 0, 0, 0, 0};
    }
}
