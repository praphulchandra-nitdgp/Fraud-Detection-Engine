package za.co.capitec.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaFraudAlertRepository extends JpaRepository<FraudAlertEntity, UUID> {

    List<FraudAlertEntity> findByTransactionId(UUID transactionId);
}
