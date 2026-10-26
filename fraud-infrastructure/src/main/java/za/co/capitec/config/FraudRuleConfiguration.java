package za.co.capitec.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import za.co.capitec.domain.rules.*;
import za.co.capitec.domain.services.FraudDetectionService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(FraudRuleProperties.class)
public class FraudRuleConfiguration {

    @Bean
    public List<FraudRule> fraudRules(
            FraudRuleProperties properties,
            TransactionHistoryPort transactionHistoryPort
    ) {
        List<FraudRule> rules = new ArrayList<>();

        FraudRuleProperties.LargeAmount largeAmount = properties.getLargeAmount();
        if (largeAmount != null && largeAmount.isEnabled()) {
            rules.add(new LargeAmountRule(
                    largeAmount.getThreshold(),
                    largeAmount.getScore()
            ));
        }

        FraudRuleProperties.Velocity velocity = properties.getVelocity();
        if (velocity != null && velocity.isEnabled()) {
            rules.add(new VelocityRule(
                    transactionHistoryPort,
                    velocity.getWindowSeconds(),
                    velocity.getMaxTransactions(),
                    velocity.getScore()
            ));
        }

        FraudRuleProperties.CountryMismatch countryMismatch = properties.getCountryMismatch();
        if (countryMismatch != null && countryMismatch.isEnabled()) {
            rules.add(new CountryMismatchRule(
                    countryMismatch.getScore()
            ));
        }

        FraudRuleProperties.BlacklistedMerchant blacklistedMerchant = properties.getBlacklistedMerchant();
        if (blacklistedMerchant != null && blacklistedMerchant.isEnabled()) {
            Set<String> merchants = blacklistedMerchant.getMerchants() == null
                    ? Set.of()
                    : new HashSet<>(blacklistedMerchant.getMerchants());

            rules.add(new BlacklistedMerchantRule(
                    merchants,
                    blacklistedMerchant.getScore()
            ));
        }

        return List.copyOf(rules);
    }

    @Bean
    public FraudDetectionService fraudDetectionService(List<FraudRule> fraudRules) {
        return new FraudDetectionService(fraudRules);
    }
}
