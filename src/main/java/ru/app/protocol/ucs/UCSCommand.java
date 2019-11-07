package ru.app.protocol.ucs;

import java.util.Arrays;

public class UCSCommand {
    private ClassType classType;
    private byte[] data;

    public UCSCommand(ClassType classType, byte[] data) {
        this.classType = classType;
        this.data = data;
    }

    @Override
    public String toString() {
        return "UCSCommand{" +
                "class=" + classType +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public ClassType getClassType() {
        return classType;
    }

    public byte[] getData() {
        return data;
    }
}
