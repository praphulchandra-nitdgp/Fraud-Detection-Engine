package za.co.capitec.messaging.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.messaging.kafka.dto.TransactionEventMessage;
import za.co.capitec.messaging.kafka.service.EventIdempotencyService;
import za.co.capitec.usecases.ProcessTransactionUseCase;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionKafkaConsumer {

    private final ProcessTransactionUseCase processTransactionUseCase;
    private final EventIdempotencyService eventIdempotencyService;

    @KafkaListener(
            topics = "${app.kafka.topics.transactions}",
            groupId = "fraud-engine-group",//"${spring.kafka.consumer.group-id}",
            containerFactory = "transactionKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(TransactionEventMessage message) {
        if (message == null || message.eventId() == null || message.transaction() == null) {
            throw new IllegalArgumentException("Invalid transaction event received");
        }

        if (eventIdempotencyService.alreadyProcessed(message.eventId())) {
            log.info("Skipping duplicate eventId={}", message.eventId());
            return;
        }

        Transaction command = new Transaction(
                UUID.randomUUID(),
                message.transaction().accountId(),
                message.transaction().amount(),
                message.transaction().merchant(),
                message.transaction().country(),
                message.transaction().accountHomeCountry(),
                Instant.now()
        );

        processTransactionUseCase.process(command);

        eventIdempotencyService.markProcessed(message.eventId(), message.source());
        log.info("Processed Kafka transaction eventId={} source={}", message.eventId(), message.source());
    }
}
