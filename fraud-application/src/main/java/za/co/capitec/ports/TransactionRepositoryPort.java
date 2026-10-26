package za.co.capitec.ports;

import za.co.capitec.domain.model.Transaction;

public interface TransactionRepositoryPort {
    void save(Transaction transaction);
}
