package za.co.capitec.domain.services;

import za.co.capitec.domain.model.FraudCheck;
import za.co.capitec.domain.model.RiskLevel;
import za.co.capitec.domain.model.RuleResult;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.domain.rules.FraudRule;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record FraudDetectionService(List<FraudRule> rules) {

    public FraudDetectionService(List<FraudRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules must not be null"));
    }

    public FraudCheck evaluate(Transaction transaction) {
        List<RuleResult> results = rules.stream()
                .map(rule -> rule.evaluate(transaction))
                .toList();

        int riskScore = results.stream()
                .filter(RuleResult::triggered)
                .mapToInt(RuleResult::score)
                .sum();

        RiskLevel riskLevel = determineRiskLevel(riskScore);

        return new FraudCheck(
                transaction.id(),
                riskScore,
                riskLevel,
                results,
                Instant.now()
        );
    }

    private RiskLevel determineRiskLevel(int riskScore) {
        if (riskScore >= 70) {
            return RiskLevel.HIGH;
        }
        if (riskScore >= 30) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
