package ru.app.protocol.ucs.classTypes;

import ru.app.protocol.ucs.ClassType;

public class AuthorizationAnswer implements ClassType {

    private static byte code;
    private Operation operation;

    public AuthorizationAnswer(Operation operation) {
        this.operation = operation;
    }

    public enum Operation {

        AuthorizationResponse((byte) 0x00);     // авторизационный ответ/детальный отчет о транзакции

        Operation(byte code) {
            AuthorizationAnswer.code = code;
        }

        public byte getCode() {
            return code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) 0x06;
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
        return "AuthorizationAnswer{" +
                "operation=" + operation +
                '}';
    }
}
