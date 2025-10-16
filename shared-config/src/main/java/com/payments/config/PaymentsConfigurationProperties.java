package com.payments.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Central configuration properties for the payments engine */
@Data
@ConfigurationProperties(prefix = "payments")
@Validated
public class PaymentsConfigurationProperties {

  @Valid private Database database = new Database();

  @Valid private Cache cache = new Cache();

  @Valid private Messaging messaging = new Messaging();

  @Valid private Security security = new Security();

  @Valid private Telemetry telemetry = new Telemetry();

  @Valid private Services services = new Services();

  @Valid private Profiles profiles = new Profiles();

  @Data
  public static class Database {
    @NotBlank private String url;

    @NotBlank private String username;

    @NotBlank private String password;

    @NotBlank private String driverClassName = "org.postgresql.Driver";

    @Positive private int maximumPoolSize = 20;

    @Positive private int minimumIdle = 5;

    @Positive private long connectionTimeout = 30000;

    @Positive private long idleTimeout = 600000;

    @Positive private long maxLifetime = 1800000;

    private boolean autoCommit = true;
    private String poolName = "PaymentsEnginePool";
  }

  @Data
  public static class Cache {
    @NotBlank private String host = "localhost";

    @Positive private int port = 6379;

    @NotBlank private String password;

    @Positive private int database = 0;

    @Positive private int timeout = 2000;

    @Positive private int maxTotal = 8;

    @Positive private int maxIdle = 8;

    @Positive private int minIdle = 0;

    @Positive private long maxWaitMillis = -1;

    private boolean testOnBorrow = true;
    private boolean testOnReturn = false;
    private boolean testWhileIdle = true;
  }

  @Data
  public static class Messaging {
    @Valid private Kafka kafka = new Kafka();

    @Valid private RabbitMQ rabbitmq = new RabbitMQ();

    @Data
    public static class Kafka {
      @NotBlank private String bootstrapServers = "localhost:9092";

      @NotBlank private String groupId = "payments-engine";

      @NotBlank private String clientId = "payments-engine-client";

      @Positive private int sessionTimeoutMs = 30000;

      @Positive private int heartbeatIntervalMs = 10000;

      @Positive private int maxPollRecords = 500;

      @NotBlank private String autoOffsetReset = "earliest";

      private boolean enableAutoCommit = false;
      private boolean enableIdempotence = true;
      private int maxInFlightRequestsPerConnection = 5;
      private int retries = 3;
      private long retryBackoffMs = 100;
      private long requestTimeoutMs = 30000;
    }

    @Data
    public static class RabbitMQ {
      @NotBlank private String host = "localhost";

      @Positive private int port = 5672;

      @NotBlank private String username;

      @NotBlank private String password;

      @NotBlank private String virtualHost = "/";

      @Positive private int connectionTimeout = 60000;

      @Positive private int requestedHeartbeat = 60;

      @Positive private int networkRecoveryInterval = 5000;

      private boolean automaticRecoveryEnabled = true;
      private boolean topologyRecoveryEnabled = true;
    }
  }

  @Data
  public static class Security {
    @Valid private Jwt jwt = new Jwt();

    @Valid private OAuth2 oauth2 = new OAuth2();

    @Valid private Encryption encryption = new Encryption();

    @Data
    public static class Jwt {
      @NotBlank private String secret;

      @Positive private long expiration = 86400000; // 24 hours

      @NotBlank private String issuer = "payments-engine";

      @NotBlank private String audience = "payments-engine";

      @Positive private long refreshExpiration = 604800000; // 7 days
    }

    @Data
    public static class OAuth2 {
      @NotBlank private String clientId;

      @NotBlank private String clientSecret;

      @NotBlank private String tokenUri;

      @NotBlank private String authorizationUri;

      @NotBlank private String userInfoUri;

      @Positive private long tokenValiditySeconds = 3600;

      @Positive private long refreshTokenValiditySeconds = 86400;
    }

    @Data
    public static class Encryption {
      @NotBlank private String algorithm = "AES";

      @NotBlank private String key;

      @NotBlank private String salt;

      @Positive private int keyLength = 256;

      @Positive private int iterations = 10000;
    }
  }

  @Data
  public static class Telemetry {
    @Valid private Tracing tracing = new Tracing();

    @Valid private Metrics metrics = new Metrics();

    @Valid private Logging logging = new Logging();

    @Data
    public static class Tracing {
      private boolean enabled = true;

      @NotBlank private String exporter = "jaeger";

      @NotBlank private String serviceName = "payments-engine";

      @NotBlank private String serviceVersion = "0.1.0";

      @Valid private Jaeger jaeger = new Jaeger();

      @Valid private Otlp otlp = new Otlp();

      @Valid private Zipkin zipkin = new Zipkin();

      @Data
      public static class Jaeger {
        @NotBlank private String endpoint = "http://localhost:14250";

        @NotBlank private String serviceName = "payments-engine";
      }

      @Data
      public static class Otlp {
        @NotBlank private String endpoint = "http://localhost:4317";

        @NotBlank private String protocol = "grpc";
      }

      @Data
      public static class Zipkin {
        @NotBlank private String endpoint = "http://localhost:9411/api/v2/spans";
      }
    }

    @Data
    public static class Metrics {
      private boolean enabled = true;

      @Valid private Prometheus prometheus = new Prometheus();

      @Data
      public static class Prometheus {
        private boolean enabled = true;

        @NotBlank private String endpoint = "/actuator/prometheus";

        @Positive private int step = 10;
      }
    }

    @Data
    public static class Logging {
      private boolean enabled = true;

      private boolean structured = true;

      @NotBlank private String level = "INFO";

      @Valid private File file = new File();

      @Data
      public static class File {
        @NotBlank private String path = "logs/payments-engine.log";

        @NotBlank private String maxSize = "100MB";

        @Positive private int maxHistory = 30;
      }
    }
  }

  @Data
  public static class Services {
    @Valid private PaymentInitiation paymentInitiation = new PaymentInitiation();

    @Valid private Validation validation = new Validation();

    @Valid private AccountAdapter accountAdapter = new AccountAdapter();

    @Valid private Routing routing = new Routing();

    @Valid private TransactionProcessing transactionProcessing = new TransactionProcessing();

    @Valid private SagaOrchestrator sagaOrchestrator = new SagaOrchestrator();

    @Data
    public static class PaymentInitiation {
      @NotBlank private String baseUrl = "http://localhost:8081";

      @Positive private int timeout = 30000;

      @Positive private int retryAttempts = 3;

      @Positive private long retryDelay = 1000;
    }

    @Data
    public static class Validation {
      @NotBlank private String baseUrl = "http://localhost:8082";

      @Positive private int timeout = 30000;

      @Positive private int retryAttempts = 3;

      @Positive private long retryDelay = 1000;
    }

    @Data
    public static class AccountAdapter {
      @NotBlank private String baseUrl = "http://localhost:8083";

      @Positive private int timeout = 30000;

      @Positive private int retryAttempts = 3;

      @Positive private long retryDelay = 1000;
    }

    @Data
    public static class Routing {
      @NotBlank private String baseUrl = "http://localhost:8084";

      @Positive private int timeout = 30000;

      @Positive private int retryAttempts = 3;

      @Positive private long retryDelay = 1000;
    }

    @Data
    public static class TransactionProcessing {
      @NotBlank private String baseUrl = "http://localhost:8085";

      @Positive private int timeout = 30000;

      @Positive private int retryAttempts = 3;

      @Positive private long retryDelay = 1000;
    }

    @Data
    public static class SagaOrchestrator {
      @NotBlank private String baseUrl = "http://localhost:8086";

      @Positive private int timeout = 30000;

      @Positive private int retryAttempts = 3;

      @Positive private long retryDelay = 1000;
    }
  }

  @Data
  public static class Profiles {
    @NotBlank private String active = "local";

    @Valid
    private Map<String, ProfileConfig> environments =
        Map.of(
            "local", new ProfileConfig("local", "localhost", "dev"),
            "dev", new ProfileConfig("dev", "dev.payments.com", "dev"),
            "staging", new ProfileConfig("staging", "staging.payments.com", "staging"),
            "prod", new ProfileConfig("prod", "payments.com", "prod"));

    @Data
    public static class ProfileConfig {
      @NotBlank private String name;

      @NotBlank private String host;

      @NotBlank private String environment;

      public ProfileConfig() {}

      public ProfileConfig(String name, String host, String environment) {
        this.name = name;
        this.host = host;
        this.environment = environment;
      }
    }
  }
}
