package za.co.capitec.domain.model;

import java.util.Objects;

public record RuleResult(String ruleName, boolean triggered, int score, String reason) {

    public RuleResult(String ruleName, boolean triggered, int score, String reason) {
        this.ruleName = Objects.requireNonNull(ruleName, "ruleName must not be null");
        this.triggered = triggered;
        this.score = score;
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
    }

    public static RuleResult triggered(String ruleName, int score, String reason) {
        if (score <= 0) {
            throw new IllegalArgumentException("Triggered rule score must be greater than zero");
        }
        return new RuleResult(ruleName, true, score, reason);
    }

    public static RuleResult passed(String ruleName) {
        return new RuleResult(ruleName, false, 0, "Rule passed");
    }
}
