package ru.app.protocol.ucs.classTypes;

import ru.app.protocol.ucs.ClassType;

public class SessionCommands implements ClassType {

    public static final Operation LOGIN = Operation.Login;

    private Operation operation;
    private byte operationCode;

    public SessionCommands(Operation operation) {
        this.operation = operation;
        this.operationCode = operation.code;
    }

    public enum Operation {
        Login((byte) 0x30),                 // инициация рабочей сессии
        LoginResponse((byte) 0x31),         // ответ на запрос инициации рабочей сессии
        PrintLine((byte) 0x32),             // печать одной или нескольких линий текста на чековом принтере
        Break((byte) 0x33),                 // прервать операцию
        BreakResponse((byte) 0x34),         // ответ на запрос прерывания операции
        InfoMessage((byte) 0x36),           // информационное сообщение общего характера
        ResponseInformation((byte) 0x37),   // получение информации от кассы
        RequestInformation((byte) 0x38);    // отправка информации на кассу

        private final byte code;

        Operation(byte code) {
            this.code = code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) 0x33;
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
        return "SessionCommands{" +
                "operation=" + operation +
                '}';
    }
}
