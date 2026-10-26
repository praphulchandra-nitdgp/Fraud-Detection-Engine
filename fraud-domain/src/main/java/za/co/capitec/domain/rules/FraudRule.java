package za.co.capitec.domain.rules;

import za.co.capitec.domain.model.RuleResult;
import za.co.capitec.domain.model.Transaction;

public interface FraudRule {

    String getName();

    RuleResult evaluate(Transaction transaction);
}
