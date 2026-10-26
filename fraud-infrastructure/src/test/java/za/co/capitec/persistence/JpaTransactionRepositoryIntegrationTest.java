package za.co.capitec.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaTransactionRepositoryIntegrationTest.JpaTestConfig.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class JpaTransactionRepositoryIntegrationTest {

    @Autowired
    private JpaTransactionRepository repository;

    @Test
    @DisplayName("Should count transactions within the configured time window")
    void shouldCountTransactionsWithinWindow() {
        Instant now = Instant.now();

        repository.save(buildEntity("ACC-1", new BigDecimal("100"), now.minusSeconds(30)));
        repository.save(buildEntity("ACC-1", new BigDecimal("200"), now.minusSeconds(20)));
        repository.save(buildEntity("ACC-1", new BigDecimal("300"), now.minusSeconds(10)));

        repository.save(buildEntity("ACC-1", new BigDecimal("400"), now.minusSeconds(120)));
        repository.save(buildEntity("ACC-2", new BigDecimal("500"), now.minusSeconds(10)));

        long count = repository.countTransactionsWithinWindow(
                "ACC-1",
                now.minusSeconds(60),
                now
        );

        assertThat(count).isEqualTo(3);
    }

    private TransactionEntity buildEntity(String accountId, BigDecimal amount, Instant timestamp) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setAccountId(accountId);
        entity.setAmount(amount);
        entity.setMerchant("merchant");
        entity.setCountry("ZA");
        entity.setAccountHomeCountry("ZA");
        entity.setTimestamp(timestamp);
        return entity;
    }

    @Configuration
    @EnableJpaRepositories(basePackageClasses = JpaTransactionRepository.class)
    @EntityScan(basePackageClasses = TransactionEntity.class)
    @EnableConfigurationProperties
    static class JpaTestConfig {
    }
}