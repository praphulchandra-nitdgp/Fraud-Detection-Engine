package za.co.capitec.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(

        @NotBlank(message = "accountId is required")
        String accountId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "amount must be zero or positive")
        BigDecimal amount,

        @NotBlank(message = "merchant is required")
        String merchant,

        @NotBlank(message = "country is required")
        String country,

        @NotBlank(message = "accountHomeCountry is required")
        String accountHomeCountry
) {
}
