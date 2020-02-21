package ru.app.protocol.ucs.commands;

import ru.app.protocol.ucs.ClassType;

public class WorkWithOperationsArchive implements ClassType {
    public static final Operation FINALIZE_DAY_TOTALS = Operation.FinalizeDayTotals;

    private Operation operation;
    private byte operationCode;

    public WorkWithOperationsArchive(Operation operation) {
        this.operation = operation;
        this.operationCode = operation.code;
    }

    public enum Operation {
        GetTransactionDetails((byte) '0'),             // получить детали операции (копия чека)
        FinalizeDayTotals((byte) '1'),                 // сверка итогов. Запрос на передачу журнала транзакций на хост
        FinalizeDayTotalsResponse((byte) '2'),         // подтверждение отправки журнала транзакций на хост
        FinalizeTransactionAmount((byte) '3'),         // сверка итогов. Запрос на отправку отдельной транзакции на хост
        FinalizeTransactionAmountResponse((byte) '4'), // подтверждение отправки отдельной транзакции
        GetReport((byte) '5'),                         // запрос отчета о проведенных транзакциях
        Commit((byte) '6');                            // подтверждение финансовой транзакции

        private final byte code;

        Operation(byte code) {
            this.code = code;
        }
    }

    @Override
    public byte getOperationClass() {
        return (byte) '2';
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
        return "WorkWithOperationsArchive{" +
                "operation=" + operation +
                '}';
    }
}
