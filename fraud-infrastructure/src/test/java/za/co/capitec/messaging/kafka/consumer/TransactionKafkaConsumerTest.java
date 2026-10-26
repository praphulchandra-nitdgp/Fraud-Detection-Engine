package za.co.capitec.messaging.kafka.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.messaging.kafka.dto.TransactionEventMessage;
import za.co.capitec.messaging.kafka.service.EventIdempotencyService;
import za.co.capitec.usecases.ProcessTransactionUseCase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionKafkaConsumerTest {

    @Mock
    private ProcessTransactionUseCase processTransactionUseCase;

    @Mock
    private EventIdempotencyService eventIdempotencyService;

    @InjectMocks
    private TransactionKafkaConsumer consumer;

    private TransactionEventMessage validMessage;

    @BeforeEach
    void setUp() {
        validMessage = new TransactionEventMessage(
                "evt-001",
                "ATM",
                Instant.parse("2026-03-12T10:15:00Z"),
                new Transaction(
                        UUID.randomUUID(),
                        "ACC-123",
                        new BigDecimal("15000"),
                        "shady-store",
                        "US",
                        "ZA",
                        Instant.now()
                )
        );
    }

    @Test
    void shouldProcessValidKafkaMessage() {
        when(eventIdempotencyService.alreadyProcessed("evt-001")).thenReturn(false);

        consumer.consume(validMessage);

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(processTransactionUseCase).process(captor.capture());
        verify(eventIdempotencyService).markProcessed("evt-001", "ATM");

        Transaction command = captor.getValue();
        assertEquals("ACC-123", command.accountId());
        assertEquals(new BigDecimal("15000"), command.amount());
        assertEquals("shady-store", command.merchant());
        assertEquals("US", command.country());
        assertEquals("ZA", command.accountHomeCountry());
    }

    @Test
    void shouldSkipDuplicateKafkaMessage() {
        when(eventIdempotencyService.alreadyProcessed("evt-001")).thenReturn(true);

        consumer.consume(validMessage);

        verify(processTransactionUseCase, never()).process(any());
        verify(eventIdempotencyService, never()).markProcessed(any(), any());
    }

    @Test
    void shouldThrowForInvalidKafkaMessage() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> consumer.consume(new TransactionEventMessage(null, "ATM", Instant.now(), null))
        );

        assertEquals("Invalid transaction event received", ex.getMessage());
        verifyNoInteractions(processTransactionUseCase);
    }
}