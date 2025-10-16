package com.payments.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for OpenTelemetry tracing */
@Configuration
@Slf4j
public class TracingConfig {

  @Value("${telemetry.tracing.enabled:true}")
  private boolean tracingEnabled;

  @Value("${telemetry.tracing.exporter:jaeger}")
  private String exporter;

  @Value("${telemetry.tracing.jaeger.endpoint:http://localhost:14250}")
  private String jaegerEndpoint;

  @Value("${telemetry.tracing.otlp.endpoint:http://localhost:4317}")
  private String otlpEndpoint;

  @Value("${telemetry.tracing.zipkin.endpoint:http://localhost:9411/api/v2/spans}")
  private String zipkinEndpoint;

  @Value("${telemetry.tracing.service.name:payments-engine}")
  private String serviceName;

  @Value("${telemetry.tracing.service.version:0.1.0}")
  private String serviceVersion;

  @Bean
  @ConditionalOnProperty(name = "telemetry.tracing.enabled", havingValue = "true")
  public OpenTelemetry openTelemetry() {
    log.info("Configuring OpenTelemetry with exporter: {}", exporter);

    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(createSpanProcessor()).build();

    return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();
  }

  @Bean
  @ConditionalOnProperty(name = "telemetry.tracing.enabled", havingValue = "true")
  public Tracer tracer(OpenTelemetry openTelemetry) {
    return openTelemetry.getTracer(serviceName, serviceVersion);
  }

  private BatchSpanProcessor createSpanProcessor() {
    return switch (exporter.toLowerCase()) {
      case "jaeger" -> BatchSpanProcessor.builder(createJaegerExporter())
          .setMaxExportBatchSize(512)
          .setScheduleDelay(Duration.ofSeconds(5))
          .build();
      case "otlp" -> BatchSpanProcessor.builder(createOtlpExporter())
          .setMaxExportBatchSize(512)
          .setScheduleDelay(Duration.ofSeconds(5))
          .build();
      case "zipkin" -> BatchSpanProcessor.builder(createZipkinExporter())
          .setMaxExportBatchSize(512)
          .setScheduleDelay(Duration.ofSeconds(5))
          .build();
      default -> throw new IllegalArgumentException("Unsupported tracing exporter: " + exporter);
    };
  }

  private JaegerGrpcSpanExporter createJaegerExporter() {
    log.info("Creating Jaeger exporter with endpoint: {}", jaegerEndpoint);
    return JaegerGrpcSpanExporter.builder().setEndpoint(jaegerEndpoint).build();
  }

  private OtlpGrpcSpanExporter createOtlpExporter() {
    log.info("Creating OTLP exporter with endpoint: {}", otlpEndpoint);
    return OtlpGrpcSpanExporter.builder().setEndpoint(otlpEndpoint).build();
  }

  private ZipkinSpanExporter createZipkinExporter() {
    log.info("Creating Zipkin exporter with endpoint: {}", zipkinEndpoint);
    return ZipkinSpanExporter.builder().setEndpoint(zipkinEndpoint).build();
  }
}
