package za.co.capitec.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Transaction(UUID id, String accountId, BigDecimal amount, String merchant, String country,
                          String accountHomeCountry, Instant timestamp) {

    public Transaction(
            UUID id,
            String accountId,
            BigDecimal amount,
            String merchant,
            String country,
            String accountHomeCountry,
            Instant timestamp
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.merchant = Objects.requireNonNull(merchant, "merchant must not be null");
        this.country = Objects.requireNonNull(country, "country must not be null");
        this.accountHomeCountry = Objects.requireNonNull(accountHomeCountry, "accountHomeCountry must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be zero or positive");
        }
    }
}
