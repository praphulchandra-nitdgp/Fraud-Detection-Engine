package za.co.capitec.ports;

import za.co.capitec.domain.model.FraudAlert;

import java.util.List;
import java.util.UUID;

public interface FraudAlertRepositoryPort {

    void save(FraudAlert fraudAlert);

    List<FraudAlert> findAll();

    List<FraudAlert> findByTransactionId(UUID transactionId);
}
