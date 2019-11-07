package ru.app.protocol.ucs;

public interface ClassType {
    byte getOperationClass();
    byte getOperationCode();
    String getOperation();
}
