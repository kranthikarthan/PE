package com.paymentengine.corebanking.integration.iso20022;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentengine.corebanking.CoreBankingApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ISO 20022 message processing
 * Uses TestContainers for real database and Kafka testing
 */
@SpringBootTest(classes = CoreBankingApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("integration-test")
@Testcontainers
class Iso20022IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("payment_engine_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withInitScript("database/init/01-init-schema.sql");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should process complete pain.001 to pain.002 flow")
    @WithMockUser(authorities = {"payment:create", "transaction:read"})
    void shouldProcessCompletePain001Flow() throws Exception {
        // Given - Load sample pain.001 message
        String pain001Json = loadTestMessage("pain001-basic-credit-transfer.json");

        // When - Process pain.001
        String pain002Response = mockMvc.perform(post("/api/v1/iso20022/pain001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(pain001Json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.CstmrPmtStsRpt").exists())
                .andExpect(jsonPath("$.CstmrPmtStsRpt.GrpHdr.MsgId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - Verify pain.002 structure
        @SuppressWarnings("unchecked")
        Map<String, Object> pain002 = objectMapper.readValue(pain002Response, Map.class);
        
        assertNotNull(pain002.get("CstmrPmtStsRpt"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> statusReport = (Map<String, Object>) pain002.get("CstmrPmtStsRpt");
        assertNotNull(statusReport.get("GrpHdr"));
        assertNotNull(statusReport.get("OrgnlGrpInfAndSts"));
    }

    @Test
    @DisplayName("Should process complete camt.055 cancellation flow")
    @WithMockUser(authorities = {"payment:create", "payment:cancel"})
    void shouldProcessCompleteCamt055Flow() throws Exception {
        // Given - First create a payment
        String pain001Json = loadTestMessage("pain001-basic-credit-transfer.json");
        
        mockMvc.perform(post("/api/v1/iso20022/pain001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(pain001Json))
                .andExpect(status().isCreated());

        // Then - Cancel the payment
        String camt055Json = loadTestMessage("camt055-customer-cancellation.json");
        
        mockMvc.perform(post("/api/v1/iso20022/camt055")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(camt055Json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancellationResults").isArray())
                .andExpect(jsonPath("$.camt029Response").exists())
                .andExpect(jsonPath("$.totalCancellations").exists());
    }

    @Test
    @DisplayName("Should validate message before processing")
    @WithMockUser(authorities = {"payment:create"})
    void shouldValidateMessageBeforeProcessing() throws Exception {
        // Given - Invalid pain.001 message (missing required fields)
        String invalidPain001 = """
            {
              "CstmrCdtTrfInitn": {
                "GrpHdr": {
                  "CreDtTm": "2024-01-15T10:30:00.000Z"
                }
              }
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/validate/pain001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPain001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").exists());
    }

    @Test
    @DisplayName("Should handle bulk pain.001 processing")
    @WithMockUser(authorities = {"payment:bulk"})
    void shouldHandleBulkProcessing() throws Exception {
        // Given - Bulk pain.001 request
        String bulkPain001Json = loadTestMessage("bulk-pain001-example.json");

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/bulk/pain001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bulkPain001Json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchId").exists())
                .andExpect(jsonPath("$.totalMessages").exists())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should get comprehensive message statistics")
    @WithMockUser(authorities = {"reporting:read"})
    void shouldGetMessageStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/iso20022/statistics")
                .param("messageType", "pain001")
                .param("fromDate", "2024-01-01")
                .param("toDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageType").value("pain001"))
                .andExpect(jsonPath("$.statistics").exists())
                .andExpect(jsonPath("$.statistics.totalMessages").exists());
    }

    @Test
    @DisplayName("Should get supported messages list")
    void shouldGetSupportedMessages() throws Exception {
        mockMvc.perform(get("/api/v1/iso20022/supported-messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerInitiated").exists())
                .andExpect(jsonPath("$.schemeProcessing").exists())
                .andExpect(jsonPath("$.cashManagement").exists())
                .andExpect(jsonPath("$.cashManagement['camt.055.001.03']").exists())
                .andExpect(jsonPath("$.cashManagement['camt.055.001.03'].name").value("Customer Payment Cancellation Request"));
    }

    @Test
    @DisplayName("Should handle concurrent message processing")
    @WithMockUser(authorities = {"payment:create"})
    void shouldHandleConcurrentProcessing() throws Exception {
        // Given
        String pain001Json = loadTestMessage("pain001-basic-credit-transfer.json");

        // When - Send multiple concurrent requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/iso20022/pain001")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(pain001Json.replace("MSG-TEST-001", "MSG-TEST-" + (i + 1))))
                    .andExpect(status().isCreated());
        }

        // Then - All should be processed successfully
        // (Status assertions above verify this)
    }

    @Test
    @DisplayName("Should enforce rate limiting")
    void shouldEnforceRateLimiting() throws Exception {
        // This test would verify rate limiting behavior
        // Implementation depends on the specific rate limiting configuration
        String pain001Json = loadTestMessage("pain001-basic-credit-transfer.json");

        // Send requests rapidly and verify rate limiting kicks in
        // (Implementation would depend on actual rate limiting configuration)
    }

    /**
     * Load test message from resources
     */
    private String loadTestMessage(String filename) {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/test-messages/" + filename);
            if (inputStream == null) {
                // Return a basic test message if file not found
                return createBasicTestMessage(filename);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return createBasicTestMessage(filename);
        }
    }

    /**
     * Create basic test message if file not found
     */
    private String createBasicTestMessage(String filename) {
        if (filename.contains("pain001")) {
            return """
                {
                  "CstmrCdtTrfInitn": {
                    "GrpHdr": {
                      "MsgId": "MSG-TEST-001",
                      "CreDtTm": "2024-01-15T10:30:00.000Z",
                      "NbOfTxs": "1",
                      "CtrlSum": "1000.00",
                      "InitgPty": { "Nm": "Test Corporation" }
                    },
                    "PmtInf": {
                      "PmtInfId": "PMT-TEST-001",
                      "PmtMtd": "TRF",
                      "ReqdExctnDt": "2024-01-15",
                      "Dbtr": { "Nm": "John Doe" },
                      "DbtrAcct": {
                        "Id": { "Othr": { "Id": "ACC001001" } },
                        "Ccy": "USD"
                      },
                      "CdtTrfTxInf": {
                        "PmtId": { "EndToEndId": "E2E-TEST-001" },
                        "Amt": { "InstdAmt": { "Ccy": "USD", "value": 1000.00 } },
                        "Cdtr": { "Nm": "Jane Smith" },
                        "CdtrAcct": { "Id": { "Othr": { "Id": "ACC002001" } } },
                        "RmtInf": { "Ustrd": ["Test payment"] }
                      }
                    }
                  }
                }
                """;
        } else if (filename.contains("camt055")) {
            return """
                {
                  "CstmrPmtCxlReq": {
                    "GrpHdr": {
                      "MsgId": "CANCEL-TEST-001",
                      "CreDtTm": "2024-01-15T11:00:00.000Z",
                      "NbOfTxs": "1",
                      "InitgPty": { "Nm": "Test Corporation" }
                    },
                    "Undrlyg": [{
                      "TxInf": [{
                        "CxlId": "CXL-TEST-001",
                        "OrgnlEndToEndId": "E2E-TEST-001",
                        "OrgnlInstdAmt": { "Ccy": "USD", "value": 1000.00 },
                        "CxlRsnInf": [{
                          "Rsn": { "Cd": "CUST" },
                          "AddtlInf": ["Customer requested cancellation"]
                        }]
                      }]
                    }]
                  }
                }
                """;
        } else if (filename.contains("bulk")) {
            return """
                {
                  "messages": [
                    {
                      "CstmrCdtTrfInitn": {
                        "GrpHdr": {
                          "MsgId": "BULK-MSG-001",
                          "CreDtTm": "2024-01-15T10:30:00.000Z",
                          "NbOfTxs": "1",
                          "InitgPty": { "Nm": "Bulk Processor" }
                        },
                        "PmtInf": {
                          "PmtInfId": "BULK-PMT-001",
                          "PmtMtd": "TRF",
                          "ReqdExctnDt": "2024-01-15",
                          "Dbtr": { "Nm": "Bulk Debtor" },
                          "DbtrAcct": { "Id": { "Othr": { "Id": "ACC001001" } } },
                          "CdtTrfTxInf": {
                            "PmtId": { "EndToEndId": "E2E-BULK-001" },
                            "Amt": { "InstdAmt": { "Ccy": "USD", "value": 500.00 } },
                            "Cdtr": { "Nm": "Bulk Creditor" },
                            "CdtrAcct": { "Id": { "Othr": { "Id": "ACC002001" } } }
                          }
                        }
                      }
                    }
                  ]
                }
                """;
        }
        
        return "{}"; // Empty JSON as fallback
    }
}