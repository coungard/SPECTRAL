package ru.app.protocol.cctalk.coinMachine.emulatorCommands;

import ru.app.protocol.cctalk.coinMachine.EmulatorCommand;

public class CoinID implements EmulatorCommand {
    private byte[] data;

    @Override
    public byte[] getData() {
        return data;
    }

    public CoinID(byte[] data) {
        this.data = data;
    }
}
