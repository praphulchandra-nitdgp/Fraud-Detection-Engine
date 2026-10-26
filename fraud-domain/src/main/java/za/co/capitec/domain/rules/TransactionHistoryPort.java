package za.co.capitec.domain.rules;

import za.co.capitec.domain.model.Transaction;

public interface TransactionHistoryPort {

    long countTransactionsForAccountWithinSeconds(Transaction transaction, long seconds);
}
