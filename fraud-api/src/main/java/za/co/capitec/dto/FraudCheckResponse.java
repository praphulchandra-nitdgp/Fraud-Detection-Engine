package za.co.capitec.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudCheckResponse(
        UUID transactionId,
        int riskScore,
        String riskLevel,
        boolean fraudSuspected,
        Instant evaluatedAt,
        List<RuleResultResponse> ruleResults
) {
}
