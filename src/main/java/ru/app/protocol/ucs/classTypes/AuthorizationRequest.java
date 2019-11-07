package ru.app.protocol.ucs.classTypes;

import ru.app.protocol.ucs.ClassType;

public class AuthorizationRequest implements ClassType {

    private Operation operation;
    private byte operationCode;

    public enum Operation {
        Sale((byte) 0x00),                  // продажа товаров и услуг
        PreAuth((byte) 0x01),               // преавторизация
        CashAdvance((byte) 0x02),           // выдача наличных
        MailOrder((byte) 0x03),             // заказ по почте/телефону (клиент и карта отсутствуют)
        Credit((byte) 0x04),                // возврат денег на счет клиента
        ElectronicCommerce((byte) 0x05),    // продажа товаров и услуг через Internet
        OfflineSale((byte) 0x06),           // введение транзакции после голосовой авторизации
        OfflineCash((byte) 0x07),           // выдача наличных после голосовой авторизации
        Recurring((byte) 0x08),             // регулярная транзакция
        Void((byte) 0x09),                  // отмена транзакции
        ReversalOfSale((byte) 0x0A),        // онлайн отмена продажи
        ReversalOfCredit((byte) 0x0B),      // онлайн отмена кредитовой транзакции
        ReversalOfCash((byte) 0x0C),        // онлайн отмена выдачи наличных
        SaleBonus((byte) 0x0D),             // продажа за бонусы
        OfflineSaleBonus((byte) 0x0E),      // продажа за бонусы (голосовая авторизация)
        CreditBonus((byte) 0x0F),           // начисление бонуса (офф-лайн)
        ReversalOfSaleBonus((byte) 0x10),   // отмена продажи за бонусы (в настоящее время – офф-лайн)
        ReversalOfCreditBonus((byte) 0x11), // отмена начисления бонусов (зарезервировано, не поддерживается)
        CashtoBonus((byte) 0x12),           // внесение денег на бонусный счет (зарезервировано, не поддерживается)
        Verification((byte) 0x13),          // проверка номера карты1
        OnlinePayment((byte) 0x14),         // зачисление денег на счет клиента в режиме он-лайн
        BalanceInquiry((byte) 0x15),        // запрос баланса
        InfoBonus((byte) 0x16),             // информация о продаже с начислением бонуса (офф-лайн)
        BonusCorrectionDebit((byte) 0x17),  // списание ранее начисленного бонуса (офф-лайн)
        InfoBonusReversal((byte) 0x18),     // отмена транзакции Info Bonus (офф-лайн)
        Activation((byte) 0x19),            // активация карты
        ActivationAndLoad((byte) 0x1A);     // активация и пополнение

        private byte code;

        Operation(byte code) {
            this.code = code;
        }
    }

    public AuthorizationRequest(Operation operation) {
        this.operation = operation;
        this.operationCode = operation.code;
    }

    @Override
    public byte getOperationClass() {
        return (byte) 0x01;
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
        return "AuthorizationRequest{" +
                "operation=" + operation +
                '}';
    }
}
