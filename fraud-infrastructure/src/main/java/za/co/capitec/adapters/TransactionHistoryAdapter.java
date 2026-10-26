package za.co.capitec.adapters;

import org.springframework.stereotype.Component;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.domain.rules.TransactionHistoryPort;
import za.co.capitec.persistence.JpaTransactionRepository;

import java.time.Instant;

@Component
public class TransactionHistoryAdapter implements TransactionHistoryPort {

    private final JpaTransactionRepository transactionRepository;

    public TransactionHistoryAdapter(JpaTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public long countTransactionsForAccountWithinSeconds(Transaction transaction, long seconds) {
        Instant toTimestamp = transaction.timestamp();
        Instant fromTimestamp = toTimestamp.minusSeconds(seconds);

        return transactionRepository.countTransactionsWithinWindow(
                transaction.accountId(),
                fromTimestamp,
                toTimestamp
        );
    }
}
