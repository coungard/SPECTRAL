package ru.app.protocol.ucs.classTypes;

import ru.app.protocol.ucs.ClassType;

public class Message implements ClassType {

    private Operation operation;
    private byte operationCode;

    public Message(Operation operation) {
        this.operation = operation;
        this.operationCode = operation.code;
    }

    public enum Operation {

        DirectMessageToTheHost((byte) 0x00),        // сообщение, передаваемое без изменений на авторизационный сервер
        DirectMessageFromTheHost((byte) 0x01),      // сообщение, полученное от авторизационного сервера
        DirectMessageToTheEFTPOS((byte) 0x02),      // сообщение, передаваемое без изменений на EFTPOS устройство
        DirectMessageFromTheEFTPOS((byte) 0x03);    // сообщение, полученное от EFTPOS устройстваД

        private final byte code;

        Operation(byte code) {
            this.code = code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) 0x07;
    }

    @Override
    public byte getOperationCode() {
        return operationCode;
    }

    @Override
    public String getOperation() {
        for (Operation op : Operation.values()) {
            if (op.code == operationCode)
                return op.name();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Message{" +
                "operation=" + operation +
                '}';
    }
}
