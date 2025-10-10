package com.paymentengine.corebanking.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database Performance Configuration
 * 
 * Optimized for high TPS (2000+ transactions per second)
 * Includes connection pooling, query optimization, and performance tuning
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.paymentengine.corebanking.repository")
@EnableTransactionManagement
public class DatabasePerformanceConfig {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${payment-engine.database.pool.max-size:200}")
    private int maxPoolSize;

    @Value("${payment-engine.database.pool.min-idle:50}")
    private int minIdle;

    @Value("${payment-engine.database.pool.connection-timeout:3000}")
    private long connectionTimeout;

    @Value("${payment-engine.database.pool.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${payment-engine.database.pool.max-lifetime:600000}")
    private long maxLifetime;

    @Value("${payment-engine.database.pool.leak-detection-threshold:10000}")
    private long leakDetectionThreshold;

    /**
     * High-performance HikariCP DataSource configuration
     * Optimized for 2000+ TPS
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(databaseUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Connection pool optimization for high TPS
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        
        // Performance optimizations
        config.setValidationTimeout(3000);
        config.setConnectionTestQuery("SELECT 1");
        config.setInitializationFailTimeout(-1);
        
        // Connection pool naming
        config.setPoolName("PaymentEnginePool");
        
        // Performance tuning
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // PostgreSQL specific optimizations
        config.addDataSourceProperty("application_name", "PaymentEngine");
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("socketTimeout", "30");
        config.addDataSourceProperty("loginTimeout", "10");
        
        return new HikariDataSource(config);
    }

    /**
     * JPA EntityManagerFactory with performance optimizations
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.paymentengine.corebanking.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(false);
        vendorAdapter.setGenerateDdl(false);
        em.setJpaVendorAdapter(vendorAdapter);
        
        // Performance-optimized Hibernate properties
        Properties properties = new Properties();
        
        // Connection and transaction settings
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        properties.setProperty("hibernate.connection.autocommit", "false");
        
        // Batch processing optimization
        properties.setProperty("hibernate.jdbc.batch_size", "100");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.batch_versioned_data", "true");
        
        // Query optimization
        properties.setProperty("hibernate.query.in_clause_parameter_padding", "true");
        properties.setProperty("hibernate.query.plan_cache_max_size", "2048");
        properties.setProperty("hibernate.query.plan_parameter_metadata_max_size", "128");
        
        // Second-level cache configuration
        properties.setProperty("hibernate.cache.use_second_level_cache", "true");
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
        properties.setProperty("hibernate.javax.cache.provider", "org.ehcache.jsr107.EhcacheCachingProvider");
        properties.setProperty("hibernate.javax.cache.uri", "classpath:ehcache.xml");
        
        // Performance monitoring
        properties.setProperty("hibernate.generate_statistics", "true");
        properties.setProperty("hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS", "100");
        
        // Connection pool settings
        properties.setProperty("hibernate.connection.provider_class", "com.zaxxer.hikari.hibernate.HikariConnectionProvider");
        
        // Dialect and schema settings
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.default_schema", "payment_engine");
        
        // Validation settings
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        properties.setProperty("hibernate.hbm2ddl.import_files", "");
        
        // Performance optimizations
        properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        em.setJpaProperties(properties);
        
        return em;
    }

    /**
     * Transaction manager with performance optimizations
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        
        // Performance optimizations
        transactionManager.setDefaultTimeout(30); // 30 seconds timeout
        transactionManager.setFailEarlyOnGlobalRollbackOnly(true);
        transactionManager.setRollbackOnCommitFailure(true);
        
        return transactionManager;
    }

    /**
     * Read-only DataSource for read replicas (if available)
     */
    @Bean
    @Profile("production")
    public DataSource readOnlyDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Use read replica URL if available
        String readOnlyUrl = databaseUrl.replace("localhost", "read-replica");
        config.setJdbcUrl(readOnlyUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Optimized for read operations
        config.setMaximumPoolSize(maxPoolSize * 2); // More connections for reads
        config.setMinimumIdle(minIdle * 2);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        
        // Read-only optimizations
        config.setReadOnly(true);
        config.setPoolName("PaymentEngineReadOnlyPool");
        
        // Same performance optimizations as primary
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        return new HikariDataSource(config);
    }
}