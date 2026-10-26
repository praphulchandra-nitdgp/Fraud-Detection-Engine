package za.co.capitec.adapters;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.persistence.JpaTransactionRepository;
import za.co.capitec.persistence.TransactionEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TransactionHistoryAdapterIntegrationTest.TestConfig.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionHistoryAdapterIntegrationTest {

    @Autowired
    private JpaTransactionRepository repository;

    @Autowired
    private TransactionHistoryAdapter adapter;

    @Test
    @DisplayName("Should count matching account transactions inside rolling window")
    void shouldCountMatchingTransactionsInsideWindow() {
        Instant now = Instant.now();

        repository.save(entity("ACC-1", now.minusSeconds(50)));
        repository.save(entity("ACC-1", now.minusSeconds(40)));
        repository.save(entity("ACC-1", now.minusSeconds(5)));
        repository.save(entity("ACC-1", now.minusSeconds(120)));
        repository.save(entity("ACC-2", now.minusSeconds(5)));

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                "ACC-1",
                new BigDecimal("999"),
                "Amazon",
                "ZA",
                "ZA",
                now
        );

        long count = adapter.countTransactionsForAccountWithinSeconds(transaction, 60);

        assertThat(count).isEqualTo(3);
    }

    private TransactionEntity entity(String accountId, Instant timestamp) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setAccountId(accountId);
        entity.setAmount(new BigDecimal("100"));
        entity.setMerchant("merchant");
        entity.setCountry("ZA");
        entity.setAccountHomeCountry("ZA");
        entity.setTimestamp(timestamp);
        return entity;
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackageClasses = JpaTransactionRepository.class)
    @EntityScan(basePackageClasses = TransactionEntity.class)
    @Import(TransactionHistoryAdapter.class)
    static class TestConfig {
        @Bean
        Tracer tracer() {
            return GlobalOpenTelemetry.getTracer("test-tracer");
        }
    }
}