package za.co.capitec.domain.rules;

import org.junit.jupiter.api.Test;
import za.co.capitec.domain.model.RuleResult;
import za.co.capitec.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BlacklistedMerchantRuleTest {

    @Test
    void shouldTriggerWhenMerchantIsBlacklisted() {
        BlacklistedMerchantRule rule = new BlacklistedMerchantRule(
                Set.of("shady-store", "scam-pay"),
                70
        );

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                "ACC-1",
                new BigDecimal("250"),
                "Shady-Store",
                "ZA",
                "ZA",
                Instant.now()
        );

        RuleResult result = rule.evaluate(transaction);

        assertTrue(result.triggered());
        assertEquals(70, result.score());
        assertEquals("BLACKLISTED_MERCHANT", result.ruleName());
    }

    @Test
    void shouldPassWhenMerchantIsNotBlacklisted() {
        BlacklistedMerchantRule rule = new BlacklistedMerchantRule(
                Set.of("shady-store", "scam-pay"),
                70
        );

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                "ACC-1",
                new BigDecimal("250"),
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
