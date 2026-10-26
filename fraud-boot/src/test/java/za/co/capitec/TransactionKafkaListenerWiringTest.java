package za.co.capitec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.messaging.kafka.dto.TransactionEventMessage;
import za.co.capitec.usecases.ProcessTransactionUseCase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.time.Duration;

@SpringBootTest(properties = "spring.profiles.active=test")
@EmbeddedKafka(partitions = 1, topics = { "fraud.transactions.v1" })
class TransactionKafkaListenerWiringTest {

    @Autowired
    private KafkaTemplate<String, TransactionEventMessage> kafkaTemplate;


    @MockitoBean
    private ProcessTransactionUseCase processTransactionUseCase;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("app.kafka.topics.transactions", () -> "fraud.transactions.v1");
    }

    @Test
    @Timeout(20)
    void shouldInvokeUseCaseWhenKafkaMessageArrives() {
        String eventId = UUID.randomUUID().toString();

        TransactionEventMessage message = new TransactionEventMessage(
                eventId,
                "ATM",
                Instant.now(),
                new Transaction(
                        UUID.randomUUID(),
                        "ACC-999",
                        new BigDecimal("12000"),
                        "shady-store",
                        "US",
                        "ZA",
                        Instant.now()
                )
        );

        kafkaTemplate.send("fraud.transactions.v1", eventId, message);

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(processTransactionUseCase, atLeastOnce()).process(any())
                );
    }
}
