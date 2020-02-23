package ru.app.protocol.ucs.commands;

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
        Login((byte) '0'),                 // инициация рабочей сессии
        LoginResponse((byte) '1'),         // ответ на запрос инициации рабочей сессии
        PrintLine((byte) '2'),             // печать одной или нескольких линий текста на чековом принтере
        Break((byte) '3'),                 // прервать операцию
        BreakResponse((byte) '4'),         // ответ на запрос прерывания операции
        InfoMessage((byte) '6'),           // информационное сообщение общего характера
        ResponseInformation((byte) '7'),   // получение информации от кассы
        RequestInformation((byte) '8');    // отправка информации на кассу

        private final byte code;

        Operation(byte code) {
            this.code = code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) '3';
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
