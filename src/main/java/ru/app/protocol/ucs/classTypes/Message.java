package ru.app.protocol.ucs.classTypes;

import ru.app.protocol.ucs.ClassType;

public class Message implements ClassType {

    private static byte code;
    private Operation operation;

    public Message(Operation operation) {
        this.operation = operation;
    }

    public enum Operation {

        DirectMessageToTheHost((byte) 0x00),        // сообщение, передаваемое без изменений на авторизационный сервер
        DirectMessageFromTheHost((byte) 0x01),      // сообщение, полученное от авторизационного сервера
        DirectMessageToTheEFTPOS((byte) 0x02),      // сообщение, передаваемое без изменений на EFTPOS устройство
        DirectMessageFromTheEFTPOS((byte) 0x03);    // сообщение, полученное от EFTPOS устройстваД

        Operation(byte code) {
            Message.code = code;
        }

        public byte getCode() {
            return code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) 0x07;
    }

    @Override
    public byte getOperationCode() {
        return code;
    }

    @Override
    public String getOperation() {
        for (Operation obj : Operation.values()) {
            if (obj.getCode() == code)
                return obj.name();
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
