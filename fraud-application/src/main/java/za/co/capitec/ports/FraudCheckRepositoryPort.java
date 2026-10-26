package za.co.capitec.ports;

import za.co.capitec.domain.model.FraudCheck;

import java.util.Optional;
import java.util.UUID;

public interface FraudCheckRepositoryPort {

    void save(FraudCheck fraudCheck);

    Optional<FraudCheck> findByTransactionId(UUID transactionId);
}