package za.co.capitec.domain.rules;

import org.junit.jupiter.api.Test;
import za.co.capitec.domain.model.RuleResult;
import za.co.capitec.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CountryMismatchRuleTest {

    @Test
    void shouldTriggerWhenTransactionCountryDiffersFromHomeCountry() {
        CountryMismatchRule rule = new CountryMismatchRule(40);

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                "ACC-1",
                new BigDecimal("500"),
                "Takealot",
                "US",
                "ZA",
                Instant.now()
        );

        RuleResult result = rule.evaluate(transaction);

        assertTrue(result.triggered());
        assertEquals(40, result.score());
        assertEquals("COUNTRY_MISMATCH", result.ruleName());
    }

    @Test
    void shouldPassWhenTransactionCountryMatchesHomeCountry() {
        CountryMismatchRule rule = new CountryMismatchRule(40);

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                "ACC-1",
                new BigDecimal("500"),
                "Takealot",
                "ZA",
                "ZA",
                Instant.now()
        );

        RuleResult result = rule.evaluate(transaction);

        assertFalse(result.triggered());
        assertEquals(0, result.score());
    }
}
