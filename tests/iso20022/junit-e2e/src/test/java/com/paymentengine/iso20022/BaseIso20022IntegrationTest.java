package com.paymentengine.iso20022;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class BaseIso20022IntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15.5").withDatabaseName("payment_engine")
                    .withUsername("pe")
                    .withPassword("pe");

    protected static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @BeforeAll
    static void startInfrastructure() {
        POSTGRES.start();
        KAFKA.start();
    }

    @AfterAll
    static void stopInfrastructure() {
        KAFKA.stop();
        POSTGRES.stop();
    }
}
