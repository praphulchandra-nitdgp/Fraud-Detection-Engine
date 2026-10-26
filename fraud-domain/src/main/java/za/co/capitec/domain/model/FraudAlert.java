package za.co.capitec.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record FraudAlert(UUID id, UUID transactionId, int riskScore, RiskLevel riskLevel, List<String> triggeredRules,
                         Instant createdAt) {

    public FraudAlert(
            UUID id,
            UUID transactionId,
            int riskScore,
            RiskLevel riskLevel,
            List<String> triggeredRules,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId must not be null");
        this.riskScore = riskScore;
        this.riskLevel = Objects.requireNonNull(riskLevel, "riskLevel must not be null");
        this.triggeredRules = List.copyOf(Objects.requireNonNull(triggeredRules, "triggeredRules must not be null"));
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
