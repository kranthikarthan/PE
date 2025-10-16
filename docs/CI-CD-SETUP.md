# CI/CD Pipeline Setup

This document describes the comprehensive CI/CD pipeline setup for the Payments Engine project.

## 🚀 Pipeline Overview

The CI/CD pipeline consists of multiple workflows designed to ensure code quality, reliability, and safe deployments.

### Workflows

1. **CI Pipeline** (`.github/workflows/ci.yml`) - Main continuous integration pipeline
2. **PR Validation** (`.github/workflows/pr-validation.yml`) - Fast feedback for pull requests
3. **Deployment** (`.github/workflows/deploy.yml`) - Production deployment pipeline

## 🔍 Quality Gates

### 1. Code Quality Checks
- **Spotless**: Code formatting and style enforcement
- **Checkstyle**: Java coding standards compliance
- **SpotBugs**: Static code analysis for bug detection
- **OWASP Dependency Check**: Security vulnerability scanning

### 2. Testing Strategy
- **Unit Tests**: Fast, isolated tests for individual components
- **Integration Tests**: End-to-end tests with real infrastructure
- **Contract Tests**: API contract validation
- **Coverage Analysis**: Test coverage reporting with 80% minimum threshold

### 3. Build & Packaging
- **Multi-JDK Support**: Java 17 and 21 compatibility testing
- **Parallel Execution**: Fast feedback through parallel test execution
- **Artifact Management**: Build artifacts for deployment

## 📊 Pipeline Stages

### Stage 1: Quality Gates (10 minutes)
```yaml
quality-gates:
  - Code formatting check (Spotless)
  - Code style check (Checkstyle)
  - Static analysis (SpotBugs)
  - Security scan (OWASP)
```

### Stage 2: Compilation (15 minutes)
```yaml
compile:
  - Multi-JDK compilation (Java 17, 21)
  - Dependency resolution
  - Module compilation verification
```

### Stage 3: Unit Tests (20 minutes)
```yaml
unit-tests:
  - Parallel test execution across modules
  - Test result aggregation
  - Coverage collection
```

### Stage 4: Integration Tests (30 minutes)
```yaml
integration-tests:
  - Database integration (PostgreSQL)
  - Cache integration (Redis)
  - End-to-end workflow testing
```

### Stage 5: Coverage Analysis (15 minutes)
```yaml
coverage:
  - Coverage report generation
  - Threshold validation (80% minimum)
  - Codecov integration
```

### Stage 6: Build & Package (20 minutes)
```yaml
build:
  - Artifact creation
  - Package validation
  - Deployment readiness check
```

## 🛠️ Maven Configuration

### Code Quality Plugins

```xml
<!-- Spotless for code formatting -->
<plugin>
  <groupId>com.diffplug.spotless</groupId>
  <artifactId>spotless-maven-plugin</artifactId>
  <version>2.43.0</version>
</plugin>

<!-- Checkstyle for code style -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-checkstyle-plugin</artifactId>
  <version>3.3.1</version>
</plugin>

<!-- SpotBugs for static analysis -->
<plugin>
  <groupId>com.github.spotbugs</groupId>
  <artifactId>spotbugs-maven-plugin</artifactId>
  <version>4.8.2.0</version>
</plugin>

<!-- JaCoCo for coverage -->
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
</plugin>

<!-- OWASP for security scanning -->
<plugin>
  <groupId>org.owasp</groupId>
  <artifactId>dependency-check-maven</artifactId>
  <version>8.4.0</version>
</plugin>
```

### Test Configuration

```xml
<!-- Surefire for unit tests -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.2.5</version>
</plugin>

<!-- Failsafe for integration tests -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  <version>3.2.5</version>
</plugin>
```

## 🎯 Maven Profiles

### CI Profile
```bash
mvn clean test -Pci
```

### Integration Tests Profile
```bash
mvn clean verify -Pintegration-tests
```

### Coverage Profile
```bash
mvn clean test -Pcoverage
```

## 📈 Coverage Requirements

- **Minimum Coverage**: 80% instruction coverage
- **Coverage Reports**: Generated in `target/site/jacoco/`
- **Coverage Upload**: Automatic upload to Codecov
- **Coverage Gates**: Build fails if threshold not met

## 🔒 Security Scanning

### OWASP Dependency Check
- **CVSS Threshold**: 7.0 (High severity)
- **Suppression File**: `dependency-check-suppressions.xml`
- **Report Format**: XML, HTML, JSON
- **False Positive Handling**: Configured suppressions for common libraries

### Security Best Practices
- Regular dependency updates
- Vulnerability scanning on every build
- Security-focused code reviews
- Dependency license compliance

## 🚀 Deployment Strategy

### Staging Deployment
- **Trigger**: Push to `main` branch
- **Environment**: Staging
- **Validation**: Smoke tests, health checks
- **Rollback**: Automatic on failure

### Production Deployment
- **Trigger**: Git tags (`v*`)
- **Environment**: Production
- **Validation**: Full test suite, security scan
- **Rollback**: Manual trigger available

## 📋 Configuration Files

### Checkstyle Configuration
- **File**: `checkstyle.xml`
- **Suppressions**: `checkstyle-suppressions.xml`
- **Standards**: Google Java Style with customizations

### Dependency Check Suppressions
- **File**: `dependency-check-suppressions.xml`
- **Purpose**: Suppress false positives for common libraries
- **Maintenance**: Regular review and updates

## 🔧 Local Development

### Pre-commit Checks
```bash
# Format code
mvn spotless:apply

# Check code style
mvn checkstyle:check

# Run static analysis
mvn spotbugs:check

# Run tests with coverage
mvn clean test jacoco:report

# Run integration tests
mvn clean verify -Pintegration-tests
```

### IDE Integration
- **Spotless**: IDE plugin for real-time formatting
- **Checkstyle**: IDE plugin for style checking
- **SpotBugs**: IDE plugin for static analysis

## 📊 Monitoring & Metrics

### Build Metrics
- **Build Time**: Tracked per stage
- **Test Results**: Aggregated across modules
- **Coverage Trends**: Historical coverage data
- **Quality Gates**: Pass/fail rates

### Deployment Metrics
- **Deployment Frequency**: Track deployment cadence
- **Lead Time**: Time from commit to production
- **Mean Time to Recovery**: Rollback and recovery times
- **Change Failure Rate**: Deployment failure rates

## 🚨 Troubleshooting

### Common Issues

#### Build Failures
1. **Code Style**: Run `mvn spotless:apply`
2. **Checkstyle**: Review `checkstyle.xml` configuration
3. **SpotBugs**: Address static analysis findings
4. **Dependencies**: Update vulnerable dependencies

#### Test Failures
1. **Unit Tests**: Check test isolation and mocking
2. **Integration Tests**: Verify test infrastructure
3. **Coverage**: Increase test coverage to meet thresholds

#### Deployment Issues
1. **Staging**: Verify staging environment health
2. **Production**: Check production readiness
3. **Rollback**: Use rollback workflow if needed

### Support
- **Documentation**: This file and inline comments
- **Logs**: GitHub Actions logs for detailed information
- **Issues**: GitHub Issues for bug reports and feature requests

## 🔄 Continuous Improvement

### Pipeline Optimization
- **Parallel Execution**: Maximize parallel test execution
- **Caching**: Optimize Maven dependency caching
- **Resource Usage**: Monitor and optimize resource consumption

### Quality Improvements
- **Coverage Goals**: Increase coverage thresholds over time
- **Security**: Enhance security scanning capabilities
- **Performance**: Optimize build and test execution times

### Monitoring Enhancements
- **Metrics**: Add more detailed metrics collection
- **Alerting**: Implement alerting for critical failures
- **Reporting**: Enhanced reporting and dashboards
