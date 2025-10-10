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
