# Payment Engine - Enterprise Makefile
# MAANG-level build and deployment automation

.PHONY: help
.DEFAULT_GOAL := help

# Colors for output
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[0;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

# Project Configuration
PROJECT_NAME := payment-engine
VERSION := $(shell cat VERSION 2>/dev/null || echo "1.0.0-SNAPSHOT")
SERVICES := payment-processing core-banking auth-service api-gateway config-service discovery-service
DOCKER_REGISTRY := docker.io
DOCKER_ORG := paymentengine

# Maven Configuration
MVN := ./mvnw
MVN_OPTS := -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
SKIP_TESTS := -DskipTests
WITH_TESTS := -Dmaven.test.skip=false

# Docker Compose Files
COMPOSE_DEV := docker-compose.dev.yml
COMPOSE_TEST := docker-compose.test.yml
COMPOSE_PROD := docker-compose.yml

##@ Help

help: ## Display this help message
	@awk 'BEGIN {FS = ":.*##"; printf "\n${BLUE}Usage:${NC}\n  make ${GREEN}<target>${NC}\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  ${GREEN}%-20s${NC} %s\n", $$1, $$2 } /^##@/ { printf "\n${YELLOW}%s${NC}\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Development Environment

up: ## Start development environment (all services)
	@echo "${GREEN}Starting development environment...${NC}"
	@docker-compose -f $(COMPOSE_DEV) up -d
	@echo "${GREEN}✓ Development environment started${NC}"
	@echo "${BLUE}Services available at:${NC}"
	@echo "  - PostgreSQL:     localhost:5432"
	@echo "  - PgAdmin:        http://localhost:5050"
	@echo "  - Kafka:          localhost:9092"
	@echo "  - Kafka UI:       http://localhost:8080"
	@echo "  - Redis:          localhost:6379"
	@echo "  - Redis Commander: http://localhost:8081"
	@echo "  - Prometheus:     http://localhost:9090"
	@echo "  - Grafana:        http://localhost:3001"
	@echo "  - Elasticsearch:  http://localhost:9200"
	@echo "  - Kibana:         http://localhost:5601"
	@echo "  - Jaeger UI:      http://localhost:16686"
	@echo "  - Mailhog UI:     http://localhost:8025"

down: ## Stop development environment
	@echo "${YELLOW}Stopping development environment...${NC}"
	@docker-compose -f $(COMPOSE_DEV) down
	@echo "${GREEN}✓ Development environment stopped${NC}"

clean: down ## Stop and clean development environment (removes volumes)
	@echo "${RED}Cleaning development environment (removing volumes)...${NC}"
	@docker-compose -f $(COMPOSE_DEV) down -v --remove-orphans
	@echo "${GREEN}✓ Development environment cleaned${NC}"

restart: down up ## Restart development environment

logs: ## Tail logs from all containers
	@docker-compose -f $(COMPOSE_DEV) logs -f

logs-service: ## Tail logs from specific service (make logs-service SERVICE=postgres)
	@docker-compose -f $(COMPOSE_DEV) logs -f $(SERVICE)

ps: ## Show running containers
	@docker-compose -f $(COMPOSE_DEV) ps

stats: ## Show container resource usage
	@docker stats $$(docker-compose -f $(COMPOSE_DEV) ps -q) --no-stream

##@ Build & Test

build: ## Build all services
	@echo "${GREEN}Building all services...${NC}"
	@$(MVN) $(MVN_OPTS) clean package $(SKIP_TESTS)
	@echo "${GREEN}✓ Build completed${NC}"

build-service: ## Build specific service (make build-service SERVICE=payment-processing)
	@echo "${GREEN}Building service: $(SERVICE)...${NC}"
	@$(MVN) $(MVN_OPTS) -pl services/$(SERVICE) -am clean package $(SKIP_TESTS)
	@echo "${GREEN}✓ Service $(SERVICE) built${NC}"

compile: ## Compile all services without packaging
	@echo "${GREEN}Compiling all services...${NC}"
	@$(MVN) $(MVN_OPTS) clean compile
	@echo "${GREEN}✓ Compilation completed${NC}"

test: ## Run all unit tests
	@echo "${GREEN}Running unit tests...${NC}"
	@$(MVN) $(MVN_OPTS) test
	@echo "${GREEN}✓ Unit tests completed${NC}"

test-service: ## Run tests for specific service (make test-service SERVICE=payment-processing)
	@echo "${GREEN}Running tests for service: $(SERVICE)...${NC}"
	@$(MVN) $(MVN_OPTS) -pl services/$(SERVICE) test
	@echo "${GREEN}✓ Tests for $(SERVICE) completed${NC}"

integration-test: ## Run integration tests with Testcontainers
	@echo "${GREEN}Running integration tests...${NC}"
	@$(MVN) $(MVN_OPTS) verify -Pfailsafe
	@echo "${GREEN}✓ Integration tests completed${NC}"

test-coverage: ## Run tests and generate coverage report
	@echo "${GREEN}Running tests with coverage...${NC}"
	@$(MVN) $(MVN_OPTS) clean verify -Pci
	@echo "${GREEN}✓ Coverage report generated at target/site/jacoco/index.html${NC}"

test-contract: ## Run contract tests
	@echo "${GREEN}Running contract tests...${NC}"
	@$(MVN) $(MVN_OPTS) test -Dtest=**/*ContractTest
	@echo "${GREEN}✓ Contract tests completed${NC}"

test-e2e: up-test ## Run end-to-end tests
	@echo "${GREEN}Running end-to-end tests...${NC}"
	@sleep 10  # Wait for services to be ready
	@$(MVN) $(MVN_OPTS) verify -Pe2e
	@echo "${GREEN}✓ E2E tests completed${NC}"
	@$(MAKE) down-test

test-all: test integration-test test-contract ## Run all tests

##@ Test Environment

up-test: ## Start test environment
	@echo "${GREEN}Starting test environment...${NC}"
	@docker-compose -f $(COMPOSE_TEST) up -d
	@echo "${GREEN}✓ Test environment started${NC}"

down-test: ## Stop test environment
	@echo "${YELLOW}Stopping test environment...${NC}"
	@docker-compose -f $(COMPOSE_TEST) down -v
	@echo "${GREEN}✓ Test environment stopped${NC}"

##@ Code Quality

lint: ## Run linters and code quality checks
	@echo "${GREEN}Running code quality checks...${NC}"
	@$(MVN) $(MVN_OPTS) checkstyle:check
	@echo "${GREEN}✓ Code quality checks passed${NC}"

format: ## Format code using Maven formatter
	@echo "${GREEN}Formatting code...${NC}"
	@$(MVN) $(MVN_OPTS) formatter:format
	@echo "${GREEN}✓ Code formatted${NC}"

sonar: ## Run SonarQube analysis (requires SonarQube running)
	@echo "${GREEN}Running SonarQube analysis...${NC}"
	@$(MVN) $(MVN_OPTS) clean verify sonar:sonar \
		-Dsonar.host.url=${SONAR_HOST_URL:-http://localhost:9000} \
		-Dsonar.login=${SONAR_TOKEN}
	@echo "${GREEN}✓ SonarQube analysis completed${NC}"

security-scan: ## Run OWASP dependency check
	@echo "${GREEN}Running security scan...${NC}"
	@$(MVN) $(MVN_OPTS) verify -Psecurity
	@echo "${GREEN}✓ Security scan completed${NC}"

verify: test lint ## Run tests and quality checks

##@ Docker

docker-build: ## Build Docker images for all services
	@echo "${GREEN}Building Docker images...${NC}"
	@for service in $(SERVICES); do \
		if [ -f services/$$service/Dockerfile ]; then \
			echo "${BLUE}Building $$service...${NC}"; \
			docker build -t $(DOCKER_REGISTRY)/$(DOCKER_ORG)/$$service:$(VERSION) \
				-t $(DOCKER_REGISTRY)/$(DOCKER_ORG)/$$service:latest \
				-f services/$$service/Dockerfile .; \
		fi \
	done
	@echo "${GREEN}✓ Docker images built${NC}"

docker-build-service: ## Build Docker image for specific service (make docker-build-service SERVICE=payment-processing)
	@echo "${GREEN}Building Docker image for $(SERVICE)...${NC}"
	@docker build -t $(DOCKER_REGISTRY)/$(DOCKER_ORG)/$(SERVICE):$(VERSION) \
		-t $(DOCKER_REGISTRY)/$(DOCKER_ORG)/$(SERVICE):latest \
		-f services/$(SERVICE)/Dockerfile .
	@echo "${GREEN}✓ Docker image built for $(SERVICE)${NC}"

docker-push: ## Push Docker images to registry
	@echo "${GREEN}Pushing Docker images...${NC}"
	@for service in $(SERVICES); do \
		if [ -f services/$$service/Dockerfile ]; then \
			echo "${BLUE}Pushing $$service...${NC}"; \
			docker push $(DOCKER_REGISTRY)/$(DOCKER_ORG)/$$service:$(VERSION); \
			docker push $(DOCKER_REGISTRY)/$(DOCKER_ORG)/$$service:latest; \
		fi \
	done
	@echo "${GREEN}✓ Docker images pushed${NC}"

docker-scan: ## Scan Docker images for vulnerabilities using Trivy
	@echo "${GREEN}Scanning Docker images for vulnerabilities...${NC}"
	@for service in $(SERVICES); do \
		if [ -f services/$$service/Dockerfile ]; then \
			echo "${BLUE}Scanning $$service...${NC}"; \
			trivy image --severity HIGH,CRITICAL $(DOCKER_REGISTRY)/$(DOCKER_ORG)/$$service:$(VERSION); \
		fi \
	done
	@echo "${GREEN}✓ Docker security scan completed${NC}"

##@ Kubernetes & Helm

helm-lint: ## Lint Helm charts
	@echo "${GREEN}Linting Helm charts...${NC}"
	@for service in $(SERVICES); do \
		if [ -d helm/$$service ]; then \
			echo "${BLUE}Linting $$service chart...${NC}"; \
			helm lint helm/$$service; \
		fi \
	done
	@echo "${GREEN}✓ Helm charts linted${NC}"

helm-package: ## Package Helm charts
	@echo "${GREEN}Packaging Helm charts...${NC}"
	@mkdir -p dist/helm
	@for service in $(SERVICES); do \
		if [ -d helm/$$service ]; then \
			echo "${BLUE}Packaging $$service chart...${NC}"; \
			helm package helm/$$service -d dist/helm; \
		fi \
	done
	@echo "${GREEN}✓ Helm charts packaged in dist/helm/${NC}"

helm-template: ## Generate Kubernetes manifests from Helm charts
	@echo "${GREEN}Generating Kubernetes manifests...${NC}"
	@mkdir -p dist/k8s
	@for service in $(SERVICES); do \
		if [ -d helm/$$service ]; then \
			echo "${BLUE}Generating manifests for $$service...${NC}"; \
			helm template $$service helm/$$service > dist/k8s/$$service.yaml; \
		fi \
	done
	@echo "${GREEN}✓ Manifests generated in dist/k8s/${NC}"

k8s-deploy-dev: ## Deploy to local Kubernetes (kind/minikube)
	@echo "${GREEN}Deploying to local Kubernetes...${NC}"
	@kubectl apply -f k8s/namespace.yaml
	@kubectl apply -f k8s/
	@echo "${GREEN}✓ Deployed to local Kubernetes${NC}"

##@ Database

db-migrate: ## Run database migrations
	@echo "${GREEN}Running database migrations...${NC}"
	@$(MVN) $(MVN_OPTS) flyway:migrate
	@echo "${GREEN}✓ Database migrations completed${NC}"

db-clean: ## Clean database
	@echo "${RED}Cleaning database...${NC}"
	@$(MVN) $(MVN_OPTS) flyway:clean
	@echo "${GREEN}✓ Database cleaned${NC}"

db-info: ## Show database migration info
	@$(MVN) $(MVN_OPTS) flyway:info

db-shell: ## Connect to PostgreSQL shell
	@docker exec -it payment-engine-postgres-dev psql -U dev_user -d payment_engine_dev

##@ Kafka

kafka-topics: ## List Kafka topics
	@docker exec payment-engine-kafka-dev kafka-topics --bootstrap-server localhost:9092 --list

kafka-create-topics: ## Create required Kafka topics
	@echo "${GREEN}Creating Kafka topics...${NC}"
	@docker exec payment-engine-kafka-dev kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic payment.inbound.v1 --partitions 3 --replication-factor 1
	@docker exec payment-engine-kafka-dev kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic payment.outbound.v1 --partitions 3 --replication-factor 1
	@docker exec payment-engine-kafka-dev kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic payment.dlq.v1 --partitions 3 --replication-factor 1
	@docker exec payment-engine-kafka-dev kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic payment.ack.v1 --partitions 3 --replication-factor 1
	@echo "${GREEN}✓ Kafka topics created${NC}"

kafka-console-consumer: ## Start Kafka console consumer for a topic (make kafka-console-consumer TOPIC=payment.inbound.v1)
	@docker exec -it payment-engine-kafka-dev kafka-console-consumer --bootstrap-server localhost:9092 --topic $(TOPIC) --from-beginning

kafka-console-producer: ## Start Kafka console producer for a topic (make kafka-console-producer TOPIC=payment.inbound.v1)
	@docker exec -it payment-engine-kafka-dev kafka-console-producer --bootstrap-server localhost:9092 --topic $(TOPIC)

kafka-consumer-lag: ## Check Kafka consumer group lag
	@docker exec payment-engine-kafka-dev kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group payment-processing-service

##@ Utilities

clean-build: ## Clean Maven build artifacts
	@echo "${GREEN}Cleaning build artifacts...${NC}"
	@$(MVN) $(MVN_OPTS) clean
	@echo "${GREEN}✓ Build artifacts cleaned${NC}"

clean-docker: ## Clean Docker resources
	@echo "${RED}Cleaning Docker resources...${NC}"
	@docker system prune -af --volumes
	@echo "${GREEN}✓ Docker resources cleaned${NC}"

clean-all: clean-build clean ## Clean everything

install: ## Install all artifacts to local Maven repository
	@echo "${GREEN}Installing artifacts to local repository...${NC}"
	@$(MVN) $(MVN_OPTS) clean install $(SKIP_TESTS)
	@echo "${GREEN}✓ Artifacts installed${NC}"

dependency-tree: ## Show dependency tree
	@$(MVN) $(MVN_OPTS) dependency:tree

dependency-updates: ## Check for dependency updates
	@$(MVN) $(MVN_OPTS) versions:display-dependency-updates

version: ## Display project version
	@echo "${GREEN}Project version: $(VERSION)${NC}"

set-version: ## Set project version (make set-version VERSION=1.1.0)
	@echo "${GREEN}Setting project version to $(VERSION)...${NC}"
	@$(MVN) $(MVN_OPTS) versions:set -DnewVersion=$(VERSION)
	@echo $(VERSION) > VERSION
	@echo "${GREEN}✓ Version set to $(VERSION)${NC}"

##@ CI/CD

ci-build: ## CI build (used by pipelines)
	@echo "${GREEN}Running CI build...${NC}"
	@$(MVN) $(MVN_OPTS) clean verify -Pci
	@echo "${GREEN}✓ CI build completed${NC}"

ci-package: ci-build docker-build ## CI package (build + docker images)

ci-deploy: ci-package docker-push ## CI deploy (build + docker + push)

##@ Documentation

docs-generate: ## Generate API documentation
	@echo "${GREEN}Generating API documentation...${NC}"
	@$(MVN) $(MVN_OPTS) javadoc:aggregate
	@echo "${GREEN}✓ Documentation generated at target/site/apidocs/${NC}"

docs-serve: docs-generate ## Generate and serve documentation
	@echo "${GREEN}Serving documentation at http://localhost:8000${NC}"
	@cd target/site && python3 -m http.server 8000

##@ Monitoring

metrics: ## Show application metrics (requires app running)
	@curl -s http://localhost:8082/payment-processing/actuator/metrics | jq .

health: ## Check application health (requires app running)
	@curl -s http://localhost:8082/payment-processing/actuator/health | jq .

##@ Quick Actions

dev: up kafka-create-topics ## Quick start: up + create topics
	@echo "${GREEN}✓ Development environment ready!${NC}"

full-test: clean test integration-test test-coverage ## Full test suite

pre-commit: format lint test ## Run before committing

pre-push: clean build test integration-test ## Run before pushing

release: clean ci-deploy helm-package ## Complete release workflow
SHELL := /bin/sh

.PHONY: up down test e2e

up:
	docker compose -f docker-compose.dev.yml up -d
	docker compose -f docker-compose.dev.yml ps
	docker compose -f docker-compose.dev.yml logs -f --tail=50

down:
	docker compose -f docker-compose.dev.yml down -v

test:
	./mvnw -q -T1C verify

e2e:
	./mvnw -q -pl tests/iso20022/junit-e2e -am verify
