package za.co.capitec.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TransactionEventMessageRequest(

        @NotBlank(message = "eventId is required")
        String eventId,
        @NotBlank(message = "source is required")
        String source,
        @NotNull(message = "occurredAt is required")
        Instant occurredAt,
        @NotNull(message = "        TransactionRequest transactionRequest\n is required")
        TransactionRequest transactionRequest
) {
}
