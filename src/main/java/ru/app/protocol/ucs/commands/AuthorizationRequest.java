package ru.app.protocol.ucs.commands;

import ru.app.protocol.ucs.ClassType;

public class AuthorizationRequest implements ClassType {

    public static final Operation PRE_AUTH = Operation.PreAuth;
    public static final Operation SALE = Operation.Sale;
    public static final Operation ACTIVATION = Operation.Activation;

    private Operation operation;
    private byte operationCode;

    public enum Operation {
        Sale((byte) '0'),                  // продажа товаров и услуг
        PreAuth((byte) '1'),               // преавторизация
        CashAdvance((byte) '2'),           // выдача наличных
        MailOrder((byte) '3'),             // заказ по почте/телефону (клиент и карта отсутствуют)
        Credit((byte) '4'),                // возврат денег на счет клиента
        ElectronicCommerce((byte) '5'),    // продажа товаров и услуг через Internet
        OfflineSale((byte) '6'),           // введение транзакции после голосовой авторизации
        OfflineCash((byte) '7'),           // выдача наличных после голосовой авторизации
        Recurring((byte) '8'),             // регулярная транзакция
        Void((byte) '9'),                  // отмена транзакции
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
        return (byte) 0x31;
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
