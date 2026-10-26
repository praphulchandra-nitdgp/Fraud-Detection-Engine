package za.co.capitec.application.usecases;

import org.junit.jupiter.api.Test;
import za.co.capitec.domain.model.FraudCheck;
import za.co.capitec.domain.model.RiskLevel;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.domain.rules.BlacklistedMerchantRule;
import za.co.capitec.domain.rules.CountryMismatchRule;
import za.co.capitec.domain.rules.FraudRule;
import za.co.capitec.domain.rules.LargeAmountRule;
import za.co.capitec.domain.services.FraudDetectionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FraudDetectionServiceTest {

    @Test
    void shouldCalculateHighRiskWhenMultipleRulesTrigger() {
        List<FraudRule> rules = List.of(
                new LargeAmountRule(new BigDecimal("10000"), 50),
                new CountryMismatchRule(40),
                new BlacklistedMerchantRule(Set.of("shady-store"), 70)
        );

        FraudDetectionService service = new FraudDetectionService(rules);

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                "ACC-1",
                new BigDecimal("15000"),
                "Shady-Store",
                "US",
                "ZA",
                Instant.now()
        );

        FraudCheck fraudCheck = service.evaluate(transaction);

        assertEquals(160, fraudCheck.riskScore());
        assertEquals(RiskLevel.HIGH, fraudCheck.riskLevel());
        assertTrue(fraudCheck.isFraudSuspected());
        assertEquals(3, fraudCheck.ruleResults().size());
    }

    @Test
    void shouldCalculateLowRiskWhenNoRulesTrigger() {
        List<FraudRule> rules = List.of(
                new LargeAmountRule(new BigDecimal("10000"), 50),
                new CountryMismatchRule(40)
        );

        FraudDetectionService service = new FraudDetectionService(rules);

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                "ACC-1",
                new BigDecimal("500"),
                "Takealot",
                "ZA",
                "ZA",
                Instant.now()
        );

        FraudCheck fraudCheck = service.evaluate(transaction);

        assertEquals(0, fraudCheck.riskScore());
        assertEquals(RiskLevel.LOW, fraudCheck.riskLevel());
        assertFalse(fraudCheck.isFraudSuspected());
    }

    @Test
    void shouldEvaluateFraudUsingConfiguredRules() {
        FraudDetectionService service = new FraudDetectionService(List.of(
                new LargeAmountRule(new BigDecimal("10000"), 50),
                new CountryMismatchRule(40)
        ));

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                "ACC-1",
                new BigDecimal("15000"),
                "Amazon",
                "US",
                "ZA",
                Instant.now()
        );

        FraudCheck result = service.evaluate(transaction);

        assertEquals(90, result.riskScore());
        assertEquals(RiskLevel.HIGH, result.riskLevel());
        assertTrue(result.isFraudSuspected());
        assertEquals(2, result.ruleResults().size());
    }
}

