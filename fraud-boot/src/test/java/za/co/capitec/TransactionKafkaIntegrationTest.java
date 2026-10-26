package za.co.capitec;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.messaging.kafka.dto.TransactionEventMessage;
import za.co.capitec.persistence.JpaFraudAlertRepository;
import za.co.capitec.persistence.JpaFraudCheckRepository;
import za.co.capitec.persistence.ProcessedEventJpaRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = "spring.profiles.active=test")
@EmbeddedKafka(partitions = 1, topics = {"fraud.transactions.v1"})
class TransactionKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, TransactionEventMessage> kafkaTemplate;

    @Autowired
    private ProcessedEventJpaRepository processedEventJpaRepository;

    @Autowired
    private JpaFraudCheckRepository jpaFraudCheckRepository;

    @Autowired
    private JpaFraudAlertRepository jpaFraudAlertRepository;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("app.kafka.topics.transactions", () -> "fraud.transactions.v1");
        registry.add("management.otlp.metrics.export.enabled", () -> "false");
    }

    @AfterEach
    void cleanUp() {
        jpaFraudAlertRepository.deleteAll();
        jpaFraudCheckRepository.deleteAll();
        processedEventJpaRepository.deleteAll();
    }

    @Test
    @Timeout(20)
    void shouldConsumeKafkaEventAndPersistProcessingOutcome() {
        String eventId = UUID.randomUUID().toString();

        TransactionEventMessage message = new TransactionEventMessage(
                eventId,
                "ATM",
                Instant.now(),
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

        kafkaTemplate.send("fraud.transactions.v1", eventId, message);

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertThat(processedEventJpaRepository.existsById(eventId)).isTrue();
                    assertThat(jpaFraudCheckRepository.count()).isEqualTo(1);
                    assertThat(jpaFraudAlertRepository.count()).isEqualTo(1);
                });
    }

    @Test
    @Timeout(20)
    void shouldIgnoreDuplicateKafkaEvent() {
        String eventId = UUID.randomUUID().toString();

        TransactionEventMessage message = new TransactionEventMessage(
                eventId,
                "ATM",
                Instant.now(),
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

        kafkaTemplate.send("fraud.transactions.v1", eventId, message);
        kafkaTemplate.send("fraud.transactions.v1", eventId, message);

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertThat(processedEventJpaRepository.count()).isEqualTo(1);
                    assertThat(jpaFraudCheckRepository.count()).isEqualTo(1);
                    assertThat(jpaFraudAlertRepository.count()).isEqualTo(1);
                });
    }
}