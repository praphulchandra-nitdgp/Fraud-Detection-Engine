package za.co.capitec.domain.rules;

import za.co.capitec.domain.model.RuleResult;
import za.co.capitec.domain.model.Transaction;

public record VelocityRule(TransactionHistoryPort transactionHistoryPort, long windowInSeconds, long maxTransactions,
                           int score) implements FraudRule {

    public VelocityRule {
        if (windowInSeconds <= 0) {
            throw new IllegalArgumentException("windowInSeconds must be greater than zero");
        }
        if (maxTransactions <= 0) {
            throw new IllegalArgumentException("maxTransactions must be greater than zero");
        }
        if (score <= 0) {
            throw new IllegalArgumentException("score must be greater than zero");
        }

    }

    @Override
    public String getName() {
        return "VELOCITY";
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        long count = transactionHistoryPort.countTransactionsForAccountWithinSeconds(transaction, windowInSeconds);

        if (count > maxTransactions) {
            return RuleResult.triggered(
                    getName(),
                    score,
                    "Transaction velocity exceeded max allowed transactions in time window"
            );
        }

        return RuleResult.passed(getName());
    }
}
