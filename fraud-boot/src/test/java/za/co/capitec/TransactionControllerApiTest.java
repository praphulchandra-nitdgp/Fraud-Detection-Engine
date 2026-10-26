package za.co.capitec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = FraudEngineApplication.class, properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Transactional
class TransactionControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldProcessTransactionAndReturnFraudCheck() throws Exception {
        String payload = """
                {
                  "accountId": "ACC-123",
                  "amount": 15000,
                  "merchant": "shady-store",
                  "country": "US",
                  "accountHomeCountry": "ZA"
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.riskScore", greaterThanOrEqualTo(70)))
                .andExpect(jsonPath("$.riskLevel", is("HIGH")))
                .andExpect(jsonPath("$.fraudSuspected", is(true)))
                .andExpect(jsonPath("$.ruleResults").isArray());
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        String payload = """
                {
                  "accountId": "",
                  "amount": -10,
                  "merchant": "",
                  "country": "",
                  "accountHomeCountry": ""
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}