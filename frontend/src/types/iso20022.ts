// ISO 20022 Message Types for Frontend

export interface Pain001Message {
  CstmrCdtTrfInitn: {
    GrpHdr: {
      MsgId: string;
      CreDtTm: string;
      NbOfTxs: string;
      CtrlSum?: string;
      InitgPty: {
        Nm: string;
        Id?: {
          OrgId?: {
            AnyBIC?: string;
            LEI?: string;
          };
          PrvtId?: {
            Othr?: Array<{
              Id: string;
              SchmeNm?: {
                Prtry: string;
              };
            }>;
          };
        };
      };
    };
    PmtInf: {
      PmtInfId: string;
      PmtMtd: string;
      PmtTpInf?: {
        InstrPrty?: string;
        SvcLvl?: {
          Cd: string;
        };
        LclInstrm?: {
          Cd: string;
        };
        CtgyPurp?: {
          Cd: string;
        };
      };
      ReqdExctnDt: string;
      Dbtr: {
        Nm: string;
        PstlAdr?: {
          Ctry: string;
          AdrLine?: string[];
        };
      };
      DbtrAcct: {
        Id: {
          IBAN?: string;
          Othr?: {
            Id: string;
            SchmeNm?: {
              Prtry: string;
            };
          };
        };
        Ccy: string;
      };
      DbtrAgt?: {
        FinInstnId: {
          BICFI?: string;
          Nm?: string;
        };
      };
      CdtTrfTxInf: {
        PmtId: {
          InstrId?: string;
          EndToEndId: string;
          TxId?: string;
          UETR?: string;
        };
        Amt: {
          InstdAmt: {
            Ccy: string;
            value: number;
          };
        };
        ChrgBr?: string;
        CdtrAgt?: {
          FinInstnId: {
            BICFI?: string;
            Nm?: string;
          };
        };
        Cdtr: {
          Nm: string;
          PstlAdr?: {
            Ctry: string;
            AdrLine?: string[];
          };
        };
        CdtrAcct: {
          Id: {
            IBAN?: string;
            Othr?: {
              Id: string;
              SchmeNm?: {
                Prtry: string;
              };
            };
          };
          Ccy?: string;
        };
        Purp?: {
          Cd: string;
        };
        RmtInf?: {
          Ustrd?: string[];
          Strd?: Array<{
            RfrdDocInf?: Array<{
              Tp?: {
                CdOrPrtry: {
                  Cd: string;
                };
              };
              Nb?: string;
              RltdDt?: string;
            }>;
          }>;
        };
      };
    };
  };
}

export interface Pain002Message {
  CstmrPmtStsRpt: {
    GrpHdr: {
      MsgId: string;
      CreDtTm: string;
      InitgPty: {
        Nm: string;
      };
    };
    OrgnlGrpInfAndSts: {
      OrgnlMsgId: string;
      OrgnlMsgNmId: string;
      OrgnlCreDtTm: string;
      GrpSts: string;
    };
    PmtInfSts: {
      PmtInfId: string;
      PmtInfSts: string;
      TxInfAndSts: {
        StsId: string;
        OrgnlInstrId?: string;
        OrgnlEndToEndId: string;
        TxSts: string;
        AccptncDtTm?: string;
      };
    };
  };
}

export interface Camt055Message {
  CstmrPmtCxlReq: {
    GrpHdr: {
      MsgId: string;
      CreDtTm: string;
      NbOfTxs: string;
      CtrlSum?: number;
      InitgPty: {
        Nm: string;
      };
    };
    Undrlyg: Array<{
      OrgnlGrpInfAndCxl?: {
        OrgnlMsgId: string;
        OrgnlMsgNmId: string;
        OrgnlCreDtTm: string;
        GrpCxlInd?: boolean;
        CxlRsnInf?: Array<{
          Rsn: {
            Cd: string;
          };
          AddtlInf?: string[];
        }>;
      };
      TxInf: Array<{
        CxlId?: string;
        OrgnlInstrId?: string;
        OrgnlEndToEndId: string;
        OrgnlTxId?: string;
        OrgnlInstdAmt: {
          Ccy: string;
          value: number;
        };
        CxlRsnInf: Array<{
          Rsn: {
            Cd: string;
          };
          AddtlInf?: string[];
        }>;
      }>;
    }>;
  };
}

export interface Camt053Message {
  BkToCstmrStmt: {
    GrpHdr: {
      MsgId: string;
      CreDtTm: string;
    };
    Stmt: Array<{
      Id: string;
      CreDtTm: string;
      FrToDt: {
        FrDtTm: string;
        ToDtTm: string;
      };
      Acct: {
        Id: {
          IBAN?: string;
          Othr?: {
            Id: string;
          };
        };
        Ccy: string;
      };
      Bal: Array<{
        Tp: {
          CdOrPrtry: {
            Cd: string; // OPBD, CLBD, etc.
          };
        };
        Amt: {
          Ccy: string;
          value: number;
        };
        CdtDbtInd: string; // CRDT, DBIT
        Dt: {
          Dt: string;
        };
      }>;
      Ntry?: Array<{
        NtryRef?: string;
        Amt: {
          Ccy: string;
          value: number;
        };
        CdtDbtInd: string;
        Sts: string; // BOOK, PDNG, INFO
        BookgDt?: {
          DtTm: string;
        };
        ValDt?: {
          Dt: string;
        };
        BkTxCd: {
          Domn: {
            Cd: string;
            Fmly: {
              Cd: string;
              SubFmlyCd: string;
            };
          };
        };
      }>;
    }>;
  };
}

export interface Iso20022MessageStatus {
  messageId: string;
  messageType: string;
  direction: 'INBOUND' | 'OUTBOUND';
  status: 'RECEIVED' | 'VALIDATED' | 'PROCESSING' | 'PROCESSED' | 'FAILED' | 'REJECTED';
  endToEndId?: string;
  correlationId?: string;
  transactionReference?: string;
  amount?: number;
  currencyCode?: string;
  errorCode?: string;
  errorMessage?: string;
  createdAt: string;
  processedAt?: string;
  processingTimeMs?: number;
}

export interface Iso20022Statistics {
  messageType: string;
  period: {
    fromDate: string;
    toDate: string;
  };
  statistics: {
    totalMessages: number;
    successfulMessages: number;
    failedMessages: number;
    successRate: number;
    averageProcessingTime: string;
    messageBreakdown: Record<string, number>;
  };
}

export interface CancellationRequest {
  originalEndToEndId: string;
  originalTransactionId?: string;
  cancellationReason: string;
  reasonCode: string;
  additionalInfo?: string[];
}

export interface CancellationResult {
  originalEndToEndId: string;
  cancellationId: string;
  status: 'ACCEPTED' | 'REJECTED' | 'PENDING';
  cancellationReason: string;
  reasonCode: string;
  cancelledAt?: string;
  newTransactionStatus?: string;
  errorMessage?: string;
}

export interface Iso20022ValidationResult {
  valid: boolean;
  messageId: string;
  messageType: string;
  errors: string[];
  warnings: string[];
  timestamp: string;
}

export interface BulkProcessingResult {
  batchId: string;
  totalMessages: number;
  successCount: number;
  failureCount: number;
  results: Array<{
    sequenceNumber: number;
    status: 'PROCESSED' | 'FAILED';
    transactionId?: string;
    error?: string;
    timestamp: string;
  }>;
  timestamp: string;
}

// Enums for ISO 20022 codes
export enum Iso20022MessageType {
  PAIN001 = 'pain001',
  PAIN002 = 'pain002', 
  PAIN007 = 'pain007',
  PAIN008 = 'pain008',
  PACS008 = 'pacs008',
  PACS002 = 'pacs002',
  PACS004 = 'pacs004',
  CAMT053 = 'camt053',
  CAMT054 = 'camt054',
  CAMT055 = 'camt055',
  CAMT056 = 'camt056'
}

export enum PaymentMethod {
  TRF = 'TRF', // Transfer
  DD = 'DD',   // Direct Debit
  CHK = 'CHK', // Check
  TRA = 'TRA'  // Transfer Advice
}

export enum LocalInstrument {
  RTP = 'RTP',   // Real-Time Payment
  ACH = 'ACH',   // Automated Clearing House
  WIRE = 'WIRE', // Wire Transfer
  SEPA = 'SEPA', // Single Euro Payments Area
  RTGS = 'RTGS', // Real-Time Gross Settlement
  INST = 'INST'  // Instant Payment
}

export enum ServiceLevel {
  SEPA = 'SEPA', // Single Euro Payments Area
  URGP = 'URGP', // Urgent Payment
  NURG = 'NURG'  // Non-Urgent Payment
}

export enum ChargeBearer {
  DEBT = 'DEBT', // Debtor
  CRED = 'CRED', // Creditor
  SHAR = 'SHAR', // Shared
  SLEV = 'SLEV'  // Service Level
}

export enum CancellationReasonCode {
  CUST = 'CUST', // Requested by Customer
  DUPL = 'DUPL', // Duplicate Payment
  FRAD = 'FRAD', // Fraudulent Payment
  TECH = 'TECH', // Technical Problem
  UPAY = 'UPAY', // Undue Payment
  CUTA = 'CUTA', // Cut-off Time
  AGNT = 'AGNT', // Incorrect Agent
  CURR = 'CURR'  // Incorrect Currency
}

export enum TransactionStatusCode {
  PDNG = 'PDNG', // Pending
  ACTC = 'ACTC', // Accepted Technical Validation
  ACSC = 'ACSC', // Accepted Settlement Completed
  RJCT = 'RJCT', // Rejected
  CANC = 'CANC'  // Cancelled
}

// Form interfaces for UI components
export interface Iso20022PaymentForm {
  messageId: string;
  paymentMethod: PaymentMethod;
  localInstrument: LocalInstrument;
  serviceLevel?: ServiceLevel;
  chargeBearer: ChargeBearer;
  requestedExecutionDate: string;
  debtorName: string;
  debtorAccountId: string;
  debtorAccountIban?: string;
  creditorName: string;
  creditorAccountId: string;
  creditorAccountIban?: string;
  amount: number;
  currency: string;
  purposeCode?: string;
  remittanceInfo?: string;
  urgency: 'HIGH' | 'NORM' | 'LOW';
}

export interface Iso20022CancellationForm {
  messageId: string;
  originalEndToEndId: string;
  originalTransactionId?: string;
  cancellationReason: string;
  reasonCode: CancellationReasonCode;
  additionalInfo?: string;
}

export interface Iso20022MessageFilter {
  messageType?: Iso20022MessageType;
  status?: string;
  direction?: 'INBOUND' | 'OUTBOUND';
  fromDate?: string;
  toDate?: string;
  endToEndId?: string;
  messageId?: string;
  correlationId?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}