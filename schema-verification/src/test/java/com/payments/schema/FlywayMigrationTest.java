package com.payments.schema;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

class FlywayMigrationTest {

  @Test
  void runAllMigrationsAgainstH2() {
    // Skip if Docker is not available in this environment
    boolean dockerUp;
    try {
      DockerClientFactory.instance().client();
      dockerUp = true;
    } catch (Throwable t) {
      dockerUp = false;
    }
    boolean enforce = Boolean.parseBoolean(System.getProperty("ci.enforceDocker", "false"));
    if (enforce) {
      // In CI, require Docker
      if (!dockerUp) {
        throw new IllegalStateException("Docker is required in CI for schema verification");
      }
    } else {
      Assumptions.assumeTrue(
          dockerUp, "Docker not available; skipping Flyway/Postgres verification");
    }
    // Resolve migrations folder from the multi-module project root
    String mmRoot = System.getProperty("maven.multiModuleProjectDirectory");
    Path base = (mmRoot != null) ? Paths.get(mmRoot) : Paths.get("..").toAbsolutePath().normalize();
    String locations = "filesystem:" + base.resolve("database-migrations").normalize().toString();

    try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")) {
      postgres.start();

      Flyway flyway =
          Flyway.configure()
              .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
              .locations(locations)
              .baselineOnMigrate(true)
              .load();

      assertDoesNotThrow(() -> flyway.migrate());
    }
  }
}
