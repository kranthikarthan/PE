package com.payments.telemetry;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import net.logstash.logback.encoder.LogstashEncoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for structured logging
 */
@Configuration
public class LoggingConfig {

    @Value("${telemetry.logging.enabled:true}")
    private boolean loggingEnabled;

    @Value("${telemetry.logging.structured:true}")
    private boolean structuredLogging;

    @Value("${telemetry.logging.level:INFO}")
    private String logLevel;

    @Value("${telemetry.logging.file.path:logs/payments-engine.log}")
    private String logFilePath;

    @Value("${telemetry.logging.file.max-size:100MB}")
    private String maxFileSize;

    @Value("${telemetry.logging.file.max-history:30}")
    private int maxHistory;

    @Bean
    @ConditionalOnProperty(name = "telemetry.logging.enabled", havingValue = "true")
    public LoggingContext loggingContext() {
        return new LoggingContext();
    }

    /**
     * Context for managing logging configuration
     */
    public static class LoggingContext {
        private final Map<String, String> context = new HashMap<>();

        public void addContext(String key, String value) {
            context.put(key, value);
        }

        public void removeContext(String key) {
            context.remove(key);
        }

        public Map<String, String> getContext() {
            return new HashMap<>(context);
        }

        public void clearContext() {
            context.clear();
        }
    }

    /**
     * Custom logback configuration for structured logging
     */
    @Bean
    @ConditionalOnProperty(name = "telemetry.logging.structured", havingValue = "true")
    public LogstashEncoder logstashEncoder() {
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setIncludeContext(true);
        encoder.setIncludeMdc(true);
        encoder.setCustomFields("{\"service\":\"payments-engine\",\"version\":\"0.1.0\"}");
        return encoder;
    }

    /**
     * Console appender with structured logging
     */
    @Bean
    @ConditionalOnProperty(name = "telemetry.logging.structured", havingValue = "true")
    public ConsoleAppender<ILoggingEvent> consoleAppender() {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setName("CONSOLE");
        appender.setEncoder(logstashEncoder());
        appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        appender.start();
        return appender;
    }

    /**
     * File appender with rolling policy
     */
    @Bean
    @ConditionalOnProperty(name = "telemetry.logging.structured", havingValue = "true")
    public RollingFileAppender<ILoggingEvent> fileAppender() {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setName("FILE");
        appender.setFile(logFilePath);
        appender.setEncoder(logstashEncoder());
        appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> sizeAndTimePolicy = new SizeAndTimeBasedRollingPolicy<>();
        sizeAndTimePolicy.setFileNamePattern(logFilePath + ".%d{yyyy-MM-dd}.%i.gz");
        sizeAndTimePolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
        sizeAndTimePolicy.setMaxHistory(maxHistory);
        sizeAndTimePolicy.setParent(appender);
        sizeAndTimePolicy.setContext(appender.getContext());
        sizeAndTimePolicy.start();

        appender.setRollingPolicy(sizeAndTimePolicy);
        appender.start();
        return appender;
    }
}
