package za.co.capitec.adapters;

import org.springframework.stereotype.Component;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.persistence.JpaTransactionRepository;
import za.co.capitec.persistence.TransactionEntity;
import za.co.capitec.ports.TransactionRepositoryPort;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final JpaTransactionRepository repository;

    public TransactionRepositoryAdapter(JpaTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(transaction.id());
        entity.setAccountId(transaction.accountId());
        entity.setAmount(transaction.amount());
        entity.setMerchant(transaction.merchant());
        entity.setCountry(transaction.country());
        entity.setAccountHomeCountry(transaction.accountHomeCountry());
        entity.setTimestamp(transaction.timestamp());

        repository.save(entity);
    }
}
