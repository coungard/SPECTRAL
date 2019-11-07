package ru.app.protocol.ucs.classTypes;

import ru.app.protocol.ucs.ClassType;

public class RequestAcceptance implements ClassType {

    private static byte code;
    private Operation operation;

    public RequestAcceptance(Operation operation) {
        this.operation = operation;
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

        Operation(byte code) {
            RequestAcceptance.code = code;
        }

        public byte getCode() {
            return code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) 0x05;
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
        return "RequestAcceptance{" +
                "operation=" + operation +
                '}';
    }
}
