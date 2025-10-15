# Changelog

All notable changes to this project will be documented in this file.
This changelog follows a simplified Keep a Changelog style.

## [Unreleased]

- Docs: minor formatting/tweak placeholder to enable PR.
- Meta: PR test change on feature branch.

## [0.1.0] - 2025-10-15

- Added
  - JPA verification for ValidationResult in `jpa-verification`.
  - Schema verification module to run Flyway migrations against Postgres.
  - `.gitattributes` for cross‑OS line endings (LF for POSIX files; CRLF for Windows scripts).
  - JPA unique constraint on `payments (tenant_id, idempotency_key)`.

- Changed
  - Payment entity: explicit Money column overrides; added `idempotencyKey`; trimmed TenantContext overrides to persisted fields.
  - ValidationResult entity: aligned to DDL with explicit column mappings; persisted `failed_rules` as JSONB; kept in‑memory rule lists transient.
  - Tenant default country to `ZAF` (code + migration).
  - Standardized TenantContext overrides across AccountAdapter, ClearingAdapter, and SagaOrchestrator.
  - Spotless set to respect Git attributes for line endings.

- Fixed
  - Flyway migrations validate cleanly from V1 to V5.
  - Reduced newline normalization issues by centralizing policy in `.gitattributes`.

Commit reference: 6468045
