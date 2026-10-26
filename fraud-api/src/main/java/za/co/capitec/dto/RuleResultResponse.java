package za.co.capitec.dto;

public record RuleResultResponse(
        String ruleName,
        boolean triggered,
        int score,
        String reason
) {
}
