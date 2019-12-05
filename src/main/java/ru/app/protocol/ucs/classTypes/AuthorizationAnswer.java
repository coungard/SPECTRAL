package ru.app.protocol.ucs.classTypes;

import ru.app.protocol.ucs.ClassType;

public class AuthorizationAnswer implements ClassType {

    private Operation operation;
    private byte operationCode;

    public AuthorizationAnswer(Operation operation) {
        this.operation = operation;
        this.operationCode = operation.code;
    }

    public enum Operation {

        AuthorizationResponse((byte) 0x00);     // авторизационный ответ/детальный отчет о транзакции

        private final byte code;

        Operation(byte code) {
            this.code = code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) 0x36;
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
        return "AuthorizationAnswer{" +
                "operation=" + operation +
                '}';
    }
}
