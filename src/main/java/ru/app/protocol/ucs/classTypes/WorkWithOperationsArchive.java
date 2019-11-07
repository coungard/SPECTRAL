package ru.app.protocol.ucs.classTypes;

import ru.app.protocol.ucs.ClassType;

public class WorkWithOperationsArchive implements ClassType {

    private static byte code;
    private Operation operation;

    public WorkWithOperationsArchive(Operation operation) {
        this.operation = operation;
    }

    public enum Operation {
        GetTransactionDetails((byte) 0x00),             // получить детали операции (копия чека)
        FinalizeDayTotals((byte) 0x01),                 // сверка итогов. Запрос на передачу журнала транзакций на хост
        FinalizeDayTotalsResponse((byte) 0x02),         // подтверждение отправки журнала транзакций на хост
        FinalizeTransactionAmount((byte) 0x03),         // сверка итогов. Запрос на отправку отдельной транзакции на хост
        FinalizeTransactionAmountResponse((byte) 0x04), // подтверждение отправки отдельной транзакции
        GetReport((byte) 0x05),                         // запрос отчета о проведенных транзакциях
        Commit((byte) 0x06);                            // подтверждение финансовой транзакции

        Operation(byte code) {
            WorkWithOperationsArchive.code = code;
        }

        public byte getCode() {
            return code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) 0x02;
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
        return "WorkWithOperationsArchive{" +
                "operation=" + operation +
                '}';
    }
}
