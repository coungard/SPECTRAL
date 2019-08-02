package ru.app.protocol.cctalk.coinMachine.emulatorCommands;

import ru.app.protocol.cctalk.coinMachine.EmulatorCommand;
import ru.app.util.Utils;

public class BufferCredit implements EmulatorCommand {
    private byte counter;
    private byte[] data;

    @Override
    public byte[] getData() {
        return Utils.concat(new byte[]{counter}, data);
    }

    public BufferCredit(byte counter, byte[] data) {
        this.counter = counter;
        this.data = data;
    }
}
