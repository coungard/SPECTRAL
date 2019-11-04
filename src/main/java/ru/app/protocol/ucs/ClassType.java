package ru.app.protocol.ucs;

public enum ClassType {

    AuthorizationRequest((byte) 0x01),      // Авторизационный запрос
    WorkWithOperationsArchive((byte) 0x02), // Работа с архивом операций
    SessionCommand((byte) 0x03),            // Сессионные команды
    RequestAcceptance((byte) 0x05),         // Подтверждение принятия запроса/первичный ответ
    AuthorizationAnswer((byte) 0x06);       // Авторизационный ответ

    private byte code;

    ClassType(byte code) {
        this.code = code;
    }
}
