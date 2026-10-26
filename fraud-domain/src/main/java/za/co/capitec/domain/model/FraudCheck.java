package za.co.capitec.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record FraudCheck(UUID transactionId, int riskScore, RiskLevel riskLevel, List<RuleResult> ruleResults,
                         Instant evaluatedAt) {

    public FraudCheck(
            UUID transactionId,
            int riskScore,
            RiskLevel riskLevel,
            List<RuleResult> ruleResults,
            Instant evaluatedAt
    ) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId must not be null");
        this.riskScore = riskScore;
        this.riskLevel = Objects.requireNonNull(riskLevel, "riskLevel must not be null");
        this.ruleResults = List.copyOf(Objects.requireNonNull(ruleResults, "ruleResults must not be null"));
        this.evaluatedAt = Objects.requireNonNull(evaluatedAt, "evaluatedAt must not be null");
    }

    public boolean isFraudSuspected() {
        return riskLevel == RiskLevel.HIGH;
    }
}
