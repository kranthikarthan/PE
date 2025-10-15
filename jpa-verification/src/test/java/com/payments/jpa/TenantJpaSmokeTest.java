package com.payments.jpa;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.payments.domain.shared.TenantId;
import com.payments.domain.shared.UserId;
import com.payments.domain.tenant.Tenant;
import com.payments.domain.tenant.TenantType;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

class TenantJpaSmokeTest {

  private static PostgreSQLContainer<?> postgres;
  private static SessionFactory sessionFactory;

  @BeforeAll
  static void setup() {
    // Skip if Docker is not available in this environment (unless enforced)
    boolean dockerUp;
    try {
      org.testcontainers.DockerClientFactory.instance().client();
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
      org.junit.jupiter.api.Assumptions.assumeTrue(
          dockerUp, "Docker not available; skipping JPA/Postgres smoke test");
    }

    postgres =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");
    postgres.start();

    Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .locations("filesystem:" + getMigrationsPath())
        .load()
        .migrate();

    Map<String, Object> settings = new HashMap<>();
    settings.put("hibernate.hbm2ddl.auto", "validate");
    settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
    settings.put("hibernate.connection.url", postgres.getJdbcUrl());
    settings.put("hibernate.connection.username", postgres.getUsername());
    settings.put("hibernate.connection.password", postgres.getPassword());

    StandardServiceRegistry registry =
        new StandardServiceRegistryBuilder().applySettings(settings).build();

    sessionFactory =
        new MetadataSources(registry)
            .addAnnotatedClass(com.payments.domain.tenant.Tenant.class)
            .addAnnotatedClass(com.payments.domain.tenant.TenantConfiguration.class)
            .addAnnotatedClass(com.payments.domain.tenant.TenantUser.class)
            .addAnnotatedClassName("com.payments.domain.tenant.BusinessUnit")
            .buildMetadata()
            .buildSessionFactory();
  }

  @AfterAll
  static void tearDown() {
    if (sessionFactory != null) {
      sessionFactory.close();
    }
    if (postgres != null) {
      postgres.stop();
    }
  }

  @Test
  @DisplayName("Happy path: persist Tenant with default BU and add user")
  void happyPathPersistTenant() {
    try (Session session = sessionFactory.openSession()) {
      session.getTransaction().begin();

      Tenant tenant =
          Tenant.create(
              TenantId.of("T-100"), "Acme Corp", TenantType.BANK, "ops@acme.example", "tester");

      tenant.addUser(UserId.of("U-1"), "alice", "alice@acme.example", "TENANT_ADMIN", "tester");

      session.persist(tenant);
      session.getTransaction().commit();
    }
  }

  @Test
  @DisplayName("Negative path: missing contact email fails at domain validation")
  void negativeMissingEmail() {
    assertThrows(
        com.payments.domain.tenant.InvalidTenantException.class,
        () -> {
          Tenant.create(
              TenantId.of("T-200"),
              "No Email Corp",
              TenantType.BANK,
              "", // invalid per DDL NOT NULL
              "tester");
        });
  }

  private static String getMigrationsPath() {
    String mmRoot = System.getProperty("maven.multiModuleProjectDirectory");
    File projectRoot = (mmRoot != null) ? new File(mmRoot) : new File("..").getAbsoluteFile();
    File migrationsDir = new File(projectRoot, "database-migrations");
    return migrationsDir.getAbsolutePath();
  }
}
