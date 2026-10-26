package za.co.capitec.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudAlertResponse(
        UUID id,
        UUID transactionId,
        int riskScore,
        String riskLevel,
        List<String> triggeredRules,
        Instant createdAt
) {
}