package ru.app.protocol.ucs.classTypes;

import ru.app.protocol.ucs.ClassType;

public class RequestAcceptance implements ClassType {

    public static final Operation HOLD = Operation.HOLD;

    private Operation operation;
    private byte operationCode;

    public RequestAcceptance(Operation operation) {
        this.operation = operation;
        this.operationCode = operation.code;
    }

    public enum Operation {
        InitialResponse((byte) 0x00),                       // OK первичный позитивный ответ на авторизационный запрос класса 1 или 2-6
        InitialResponseRequiresLoginFirst((byte) 0x01),     // негативный ответ на первичный запрос. Требуется инициация рабочей сессии
        PINEntryRequired((byte) 0x02),                      // требуется ввод пин-кода
        OnlineAuthorisationRequired((byte) 0x03),           // требуется он-лайн авторизация
        InitialResponseNoPreviousTransaction((byte) 0x04),  // ??
        HOLD((byte) 0x05),                                  // необходимо обнулить таймаут ожидания
        ConsoleMessage((byte) 0x16),                        // сообщение на экран кассы
        InitialResponseErrorParsingRequest((byte) 0x21);     // ошибка валидации ответа

        private final byte code;

        Operation(byte code) {
            this.code = code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) 0x05;
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
        return "RequestAcceptance{" +
                "operation=" + operation +
                '}';
    }
}
