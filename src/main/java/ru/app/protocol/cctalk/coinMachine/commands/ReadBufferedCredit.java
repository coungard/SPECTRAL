package ru.app.protocol.cctalk.coinMachine.commands;

import ru.app.protocol.cctalk.coinMachine.Command;
import ru.app.protocol.cctalk.coinMachine.CommandType;

public class ReadBufferedCredit implements Command {

    public byte[] getData() {
        return null;
    }

    public CommandType getCommandType() {
        return CommandType.ReadBufferedCreditOrErrorCodes;
    }
}
