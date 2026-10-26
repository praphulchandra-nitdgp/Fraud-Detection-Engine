package za.co.capitec.ports.in;

import java.math.BigDecimal;

public record ProcessTransactionCommand(
        String accountId,
        BigDecimal amount,
        String merchant,
        String country,
        String accountHomeCountry
) {
}
