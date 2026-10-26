package za.co.capitec.adapters;

import org.springframework.stereotype.Component;
import za.co.capitec.domain.model.FraudAlert;
import za.co.capitec.domain.model.RiskLevel;
import za.co.capitec.persistence.FraudAlertEntity;
import za.co.capitec.persistence.JpaFraudAlertRepository;
import za.co.capitec.ports.FraudAlertRepositoryPort;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class FraudAlertRepositoryAdapter implements FraudAlertRepositoryPort {

    private final JpaFraudAlertRepository repository;

    public FraudAlertRepositoryAdapter(JpaFraudAlertRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(FraudAlert fraudAlert) {
        repository.save(toEntity(fraudAlert));
    }

    @Override
    public List<FraudAlert> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<FraudAlert> findByTransactionId(UUID transactionId) {
        return repository.findByTransactionId(transactionId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private FraudAlertEntity toEntity(FraudAlert fraudAlert) {
        FraudAlertEntity entity = new FraudAlertEntity();
        entity.setId(fraudAlert.id());
        entity.setTransactionId(fraudAlert.transactionId());
        entity.setRiskScore(fraudAlert.riskScore());
        entity.setRiskLevel(fraudAlert.riskLevel().name());
        entity.setTriggeredRules(String.join(",", fraudAlert.triggeredRules()));
        entity.setCreatedAt(fraudAlert.createdAt());
        return entity;
    }

    private FraudAlert toDomain(FraudAlertEntity entity) {
        List<String> triggeredRules = entity.getTriggeredRules() == null || entity.getTriggeredRules().isBlank()
                ? List.of()
                : Arrays.stream(entity.getTriggeredRules().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        return new FraudAlert(
                entity.getId(),
                entity.getTransactionId(),
                entity.getRiskScore(),
                RiskLevel.valueOf(entity.getRiskLevel()),
                triggeredRules,
                entity.getCreatedAt()
        );
    }
}
