package ru.app.listeners;

import ru.app.protocol.cctalk.Command;

public abstract class AbstractClient {
    public Command currentCommand;

    abstract public byte[] sendMessage(Command command);
    abstract public void sendBytes(byte[] bytes);
}
