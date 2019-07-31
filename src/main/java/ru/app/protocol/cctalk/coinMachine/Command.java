package ru.app.protocol.cctalk.coinMachine;

public interface Command {

    byte[] getData();
    CommandType getCommandType();
}
