package za.co.capitec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import za.co.capitec.domain.model.FraudAlert;
import za.co.capitec.domain.model.FraudCheck;
import za.co.capitec.domain.model.RiskLevel;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.ports.FraudAlertRepositoryPort;
import za.co.capitec.ports.FraudCheckRepositoryPort;
import za.co.capitec.ports.TransactionRepositoryPort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = FraudEngineApplication.class, properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Transactional
class FraudQueryControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepositoryPort transactionRepositoryPort;

    @Autowired
    private FraudCheckRepositoryPort fraudCheckRepositoryPort;

    @Autowired
    private FraudAlertRepositoryPort fraudAlertRepositoryPort;

    private UUID transactionId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();

        Transaction transaction = new Transaction(
                transactionId,
                "ACC-123",
                new BigDecimal("15000"),
                "shady-store",
                "US",
                "ZA",
                Instant.now()
        );

        transactionRepositoryPort.save(transaction);

        fraudCheckRepositoryPort.save(new FraudCheck(
                transactionId,
                90,
                RiskLevel.HIGH,
                List.of(),
                Instant.now()
        ));

        fraudAlertRepositoryPort.save(new FraudAlert(
                UUID.randomUUID(),
                transactionId,
                90,
                RiskLevel.HIGH,
                List.of("LARGE_AMOUNT", "BLACKLISTED_MERCHANT"),
                Instant.now()
        ));
    }

    @Test
    void shouldGetFraudCheckByTransactionId() throws Exception {
        mockMvc.perform(get("/transactions/{transactionId}/fraud-check", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is(transactionId.toString())))
                .andExpect(jsonPath("$.riskScore", is(90)))
                .andExpect(jsonPath("$.riskLevel", is("HIGH")));
    }

    @Test
    void shouldGetFraudAlertsByTransactionId() throws Exception {
        mockMvc.perform(get("/fraud-alerts").param("transactionId", transactionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId", is(transactionId.toString())))
                .andExpect(jsonPath("$[0].riskLevel", is("HIGH")));
    }

    @Test
    void shouldGetFraudAlerts() throws Exception {
        mockMvc.perform(get("/fraud-alerts"))
                .andExpect(status().isOk());
    }
}
