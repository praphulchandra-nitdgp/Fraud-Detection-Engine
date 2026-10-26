package za.co.capitec.messaging.kafka.dto;

import za.co.capitec.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEventMessage(
        String eventId,
        String source,
        Instant occurredAt,
        Transaction transaction
){}
