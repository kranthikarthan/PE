package com.payments.domain.transaction;

enum TransactionStatus { CREATED, PROCESSING, CLEARING, COMPLETED, FAILED }
enum TransactionType { DEBIT, CREDIT, REVERSAL }
enum LedgerEntryType { DEBIT, CREDIT }


