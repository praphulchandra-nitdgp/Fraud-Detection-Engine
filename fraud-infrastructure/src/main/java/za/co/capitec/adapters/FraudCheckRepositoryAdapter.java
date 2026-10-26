package za.co.capitec.adapters;

import org.springframework.stereotype.Component;
import za.co.capitec.domain.model.FraudCheck;
import za.co.capitec.domain.model.RiskLevel;
import za.co.capitec.persistence.FraudCheckEntity;
import za.co.capitec.persistence.JpaFraudCheckRepository;
import za.co.capitec.ports.FraudCheckRepositoryPort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FraudCheckRepositoryAdapter implements FraudCheckRepositoryPort {

    private final JpaFraudCheckRepository repository;

    public FraudCheckRepositoryAdapter(JpaFraudCheckRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(FraudCheck fraudCheck) {
        repository.save(toEntity(fraudCheck));
    }

    @Override
    public Optional<FraudCheck> findByTransactionId(UUID transactionId) {
        return repository.findById(transactionId)
                .map(this::toDomain);
    }

    private FraudCheckEntity toEntity(FraudCheck fraudCheck) {
        FraudCheckEntity entity = new FraudCheckEntity();
        entity.setTransactionId(fraudCheck.transactionId());
        entity.setRiskScore(fraudCheck.riskScore());
        entity.setRiskLevel(fraudCheck.riskLevel().name());
        entity.setEvaluatedAt(fraudCheck.evaluatedAt());
        entity.setSuspectedFraud(fraudCheck.isFraudSuspected());
        return entity;
    }

    private FraudCheck toDomain(FraudCheckEntity entity) {
        return new FraudCheck(
                entity.getTransactionId(),
                entity.getRiskScore(),
                RiskLevel.valueOf(entity.getRiskLevel()),
                List.of(),
                entity.getEvaluatedAt()
        );
    }
}
