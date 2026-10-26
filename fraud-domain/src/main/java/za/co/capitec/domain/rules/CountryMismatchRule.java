package za.co.capitec.domain.rules;

import za.co.capitec.domain.model.RuleResult;
import za.co.capitec.domain.model.Transaction;

public record CountryMismatchRule(int score) implements FraudRule {

    public CountryMismatchRule {
        if (score <= 0) {
            throw new IllegalArgumentException("score must be greater than zero");
        }
    }

    @Override
    public String getName() {
        return "COUNTRY_MISMATCH";
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        if (!transaction.country().equalsIgnoreCase(transaction.accountHomeCountry())) {
            return RuleResult.triggered(
                    getName(),
                    score,
                    "Transaction country does not match account home country"
            );
        }

        return RuleResult.passed(getName());
    }
}
