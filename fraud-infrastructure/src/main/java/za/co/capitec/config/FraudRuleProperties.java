package za.co.capitec.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "fraud.rules")
public class FraudRuleProperties {

    private LargeAmount largeAmount = new LargeAmount();
    private Velocity velocity = new Velocity();
    private CountryMismatch countryMismatch = new CountryMismatch();
    private BlacklistedMerchant blacklistedMerchant = new BlacklistedMerchant();

    @Setter
    @Getter
    public static class LargeAmount {
        private boolean enabled = true;
        private BigDecimal threshold = new BigDecimal("10000");
        private int score = 50;
    }

    @Setter
    @Getter
    public static class Velocity {
        private boolean enabled = true;
        private long windowSeconds = 60;
        private long maxTransactions = 5;
        private int score = 30;
    }

    @Setter
    @Getter
    public static class CountryMismatch {
        private boolean enabled = true;
        private int score = 40;
    }

    @Setter
    @Getter
    public static class BlacklistedMerchant {
        private boolean enabled = true;
        private int score = 70;
        private List<String> merchants = new ArrayList<>();
    }
}
