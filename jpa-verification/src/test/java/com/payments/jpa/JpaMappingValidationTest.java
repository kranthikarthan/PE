package com.payments.jpa;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public class JpaMappingValidationTest {

  @Test
  void validateEntityMappingsAgainstFlywaySchema() {
    boolean dockerUp;
    try {
      DockerClientFactory.instance().client();
      dockerUp = true;
    } catch (Throwable t) {
      dockerUp = false;
    }
    boolean enforce = Boolean.parseBoolean(System.getProperty("ci.enforceDocker", "false"));
    if (enforce) {
      if (!dockerUp) {
        throw new IllegalStateException("Docker is required in CI for JPA verification");
      }
    } else {
      Assumptions.assumeTrue(dockerUp, "Docker not available; skipping JPA/Postgres verification");
    }

    try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")) {
      postgres.start();

      // Run Flyway migrations first
      String mmRoot = System.getProperty("maven.multiModuleProjectDirectory");
      Path base =
          (mmRoot != null) ? Paths.get(mmRoot) : Paths.get("..").toAbsolutePath().normalize();
      String locations = "filesystem:" + base.resolve("database-migrations").normalize().toString();

      Flyway.configure()
          .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
          .locations(locations)
          .baselineOnMigrate(true)
          .load()
          .migrate();

      // Build Hibernate SessionFactory with validation
      Map<String, Object> settings = new HashMap<>();
      settings.put("hibernate.hbm2ddl.auto", "validate");
      settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
      settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
      settings.put("hibernate.connection.url", postgres.getJdbcUrl());
      settings.put("hibernate.connection.username", postgres.getUsername());
      settings.put("hibernate.connection.password", postgres.getPassword());

      StandardServiceRegistry registry =
          new StandardServiceRegistryBuilder().applySettings(settings).build();

      MetadataSources sources =
          new MetadataSources(registry)
              .addAnnotatedClass(com.payments.domain.payment.Payment.class)
              .addAnnotatedClass(com.payments.domain.validation.ValidationResult.class)
              .addAnnotatedClass(com.payments.domain.transaction.Transaction.class)
              .addAnnotatedClassName("com.payments.domain.transaction.LedgerEntry")
              .addAnnotatedClassName("com.payments.domain.transaction.TransactionEvent")
              .addAnnotatedClass(com.payments.domain.tenant.Tenant.class)
              .addAnnotatedClass(com.payments.domain.tenant.TenantConfiguration.class)
              .addAnnotatedClass(com.payments.domain.tenant.TenantUser.class)
              .addAnnotatedClassName("com.payments.domain.tenant.BusinessUnit");

      assertDoesNotThrow(
          () -> {
            try (SessionFactory ignored = sources.buildMetadata().buildSessionFactory()) {}
          });
    }
  }
}
