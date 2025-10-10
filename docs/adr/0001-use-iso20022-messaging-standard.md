# ADR 0001: Use ISO20022 Messaging Standard for Payment Processing

## Status
Accepted

## Context
Our payment engine needs to support modern, standardized financial messaging to ensure interoperability with various financial institutions, clearing systems, and payment schemes worldwide. We must choose a messaging standard that:

- Provides rich, structured data for payment transactions
- Is widely adopted across the financial industry
- Supports various payment types (credit transfers, direct debits, etc.)
- Enables future extensibility
- Meets regulatory compliance requirements

## Decision
We will adopt **ISO20022** as our primary messaging standard for all payment processing workflows.

### Key ISO20022 Message Types We Support:
- **pain.001** - Customer Credit Transfer Initiation
- **pacs.008** - FI to FI Customer Credit Transfer
- **pacs.002** - FI to FI Payment Status Report
- **pacs.004** - Payment Return
- **camt.054** - Bank to Customer Debit/Credit Notification
- **camt.055** - Customer Payment Cancellation Request
- **camt.056** - FI to FI Payment Cancellation Request

## Consequences

### Positive:
- ✅ Global standard adopted by SWIFT, Fedwire, SEPA, and other major payment networks
- ✅ Rich data model with structured, semantic information
- ✅ Better compliance with regulations (e.g., transparency requirements)
- ✅ Improved straight-through processing (STP) rates
- ✅ Future-proof as industry moves towards ISO20022
- ✅ Strong support for end-to-end tracking via UETR

### Negative:
- ❌ More complex than legacy formats (e.g., MT messages)
- ❌ Requires transformation logic for systems using older standards
- ❌ Larger message sizes compared to fixed-length formats
- ❌ Learning curve for developers unfamiliar with XML-based standards

### Neutral:
- ⚠️ Need to maintain transformation services for legacy format support
- ⚠️ Requires comprehensive validation logic
- ⚠️ Must handle versioning of ISO20022 schemas

## Implementation Notes
- All ISO20022 messages are validated against official XSD schemas
- Message transformation services handle conversion between pain/pacs/camt formats
- Kafka topics follow naming convention: `iso20022.<message-type>.v<version>`
- All messages are persisted for audit and compliance purposes

## References
- [ISO20022 Official Documentation](https://www.iso20022.org/)
- [SWIFT ISO20022 Migration](https://www.swift.com/standards/iso-20022)
- Internal: ISO20022 Implementation Guide (link to internal docs)

## Date
2025-10-10

## Author
Payment Engine Team
