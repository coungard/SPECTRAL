package ru.app.protocol.cctalk.coinMachine.emulatorCommands;

import ru.app.protocol.cctalk.coinMachine.EmulatorCommand;

public class BufferCredit implements EmulatorCommand {
    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
