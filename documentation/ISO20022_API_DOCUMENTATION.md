# Payment Engine - ISO 20022 API Documentation

## Overview

The Payment Engine fully supports **ISO 20022 pain.001** (Customer Credit Transfer Initiation) messages for payment initiation, aligning with international banking standards. This document covers the ISO 20022 compliant APIs, message formats, and integration guidelines.

## ISO 20022 Standards Compliance

- **Message Type**: pain.001.001.03 (Customer Credit Transfer Initiation)
- **Response Type**: pain.002.001.03 (Customer Payment Status Report)  
- **Format**: JSON representation of ISO 20022 XML structure
- **Validation**: Full ISO 20022 message validation
- **Transformation**: Bidirectional transformation between ISO 20022 and internal formats

## Base URLs

- **Production**: `https://api.payment-engine.com`
- **Staging**: `https://staging-api.payment-engine.com`
- **Development**: `http://localhost:8080`

## Authentication

All ISO 20022 API requests require authentication using Bearer tokens (same as other APIs).

```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## ISO 20022 pain.001 Payment Initiation

### Endpoint
`POST /api/v1/iso20022/pain001`

### Request Format (ISO 20022 pain.001 JSON)

```json
{
  "CstmrCdtTrfInitn": {
    "GrpHdr": {
      "MsgId": "MSG-20240115-001",
      "CreDtTm": "2024-01-15T10:30:00.000Z",
      "NbOfTxs": "1",
      "CtrlSum": "1000.00",
      "InitgPty": {
        "Nm": "ABC Corporation",
        "Id": {
          "OrgId": {
            "AnyBIC": "ABCBUS33XXX"
          }
        },
        "CtryOfRes": "US"
      }
    },
    "PmtInf": {
      "PmtInfId": "PMT-20240115-001",
      "PmtMtd": "TRF",
      "BtchBookg": false,
      "NbOfTxs": "1",
      "CtrlSum": "1000.00",
      "PmtTpInf": {
        "InstrPrty": "NORM",
        "SvcLvl": {
          "Cd": "SEPA"
        },
        "LclInstrm": {
          "Cd": "RTP"
        },
        "CtgyPurp": {
          "Cd": "CBFF"
        }
      },
      "ReqdExctnDt": "2024-01-15",
      "Dbtr": {
        "Nm": "John Doe",
        "PstlAdr": {
          "Ctry": "US",
          "AdrLine": ["123 Main Street", "New York, NY 10001"]
        },
        "Id": {
          "PrvtId": {
            "Othr": [{
              "Id": "123456789",
              "SchmeNm": {
                "Prtry": "SSN"
              }
            }]
          }
        }
      },
      "DbtrAcct": {
        "Id": {
          "Othr": {
            "Id": "ACC001001",
            "SchmeNm": {
              "Prtry": "ACCT"
            }
          }
        },
        "Ccy": "USD"
      },
      "DbtrAgt": {
        "FinInstnId": {
          "BICFI": "PAYMENTUS33XXX",
          "Nm": "Payment Engine Bank"
        }
      },
      "CdtTrfTxInf": {
        "PmtId": {
          "InstrId": "INSTR-001",
          "EndToEndId": "E2E-20240115-001",
          "TxId": "TXN-20240115-001"
        },
        "PmtTpInf": {
          "LclInstrm": {
            "Cd": "RTP"
          }
        },
        "Amt": {
          "InstdAmt": {
            "Ccy": "USD",
            "value": 1000.00
          }
        },
        "ChrgBr": "SHAR",
        "CdtrAgt": {
          "FinInstnId": {
            "BICFI": "CHASUS33XXX",
            "Nm": "Chase Bank"
          }
        },
        "Cdtr": {
          "Nm": "Jane Smith",
          "PstlAdr": {
            "Ctry": "US",
            "AdrLine": ["456 Oak Avenue", "Los Angeles, CA 90001"]
          }
        },
        "CdtrAcct": {
          "Id": {
            "Othr": {
              "Id": "ACC002001",
              "SchmeNm": {
                "Prtry": "ACCT"
              }
            }
          },
          "Ccy": "USD"
        },
        "Purp": {
          "Cd": "CBFF"
        },
        "RmtInf": {
          "Ustrd": ["Payment for services rendered"]
        }
      }
    }
  }
}
```

### Response Format (ISO 20022 pain.002 JSON)

```json
{
  "CstmrPmtStsRpt": {
    "GrpHdr": {
      "MsgId": "PAIN002-1705312200000-A1B2C3D4",
      "CreDtTm": "2024-01-15T10:30:00.000Z",
      "InitgPty": {
        "Nm": "Payment Engine"
      }
    },
    "OrgnlGrpInfAndSts": {
      "OrgnlMsgId": "MSG-20240115-001",
      "OrgnlMsgNmId": "pain.001.001.03",
      "OrgnlCreDtTm": "2024-01-15T10:30:00.000Z",
      "GrpSts": "ACSC"
    },
    "PmtInfSts": {
      "PmtInfId": "TXN-1705312200000-A1B2C3D4",
      "PmtInfSts": "ACSC",
      "TxInfAndSts": {
        "StsId": "bb0e8400-e29b-41d4-a716-446655440001",
        "OrgnlInstrId": "INSTR-001",
        "OrgnlEndToEndId": "E2E-20240115-001",
        "TxSts": "ACSC",
        "AccptncDtTm": "2024-01-15T10:30:15.000Z",
        "OrgnlTxRef": {
          "Amt": {
            "InstdAmt": {
              "Ccy": "USD",
              "value": 1000.00
            }
          }
        }
      }
    }
  }
}
```

## ISO 20022 Status Codes

| Internal Status | ISO 20022 Code | Description |
|----------------|----------------|-------------|
| PENDING | PDNG | Pending |
| PROCESSING | ACTC | Accepted Technical Validation |
| COMPLETED | ACSC | Accepted Settlement Completed |
| FAILED | RJCT | Rejected |
| CANCELLED | CANC | Cancelled |

## Payment Method Mapping

| ISO 20022 Local Instrument | Internal Payment Type | Description |
|---------------------------|---------------------|-------------|
| RTP | Real-Time Payment | Instant payment processing |
| RTGS | Wire Transfer | Real-time gross settlement |
| ACH | ACH Credit | Automated clearing house |
| WIRE | Wire Domestic | Domestic wire transfer |
| SEPA | ACH Credit | SEPA credit transfer |
| INST | Real-Time Payment | Instant payment |

## API Endpoints

### 1. Process pain.001 Message

**Endpoint**: `POST /api/v1/iso20022/pain001`

**Description**: Process ISO 20022 pain.001 Customer Credit Transfer Initiation message

**Required Permissions**: `payment:create`

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer <token>
```

**Response**: ISO 20022 pain.002 Customer Payment Status Report

### 2. Get Payment Status (pain.002)

**Endpoint**: `GET /api/v1/iso20022/pain002/{transactionId}`

**Description**: Get payment status in ISO 20022 pain.002 format

**Required Permissions**: `transaction:read`

**Query Parameters**:
- `originalMessageId` (optional): Original pain.001 message ID

**Response**: ISO 20022 pain.002 status report

### 3. Validate pain.001 Message

**Endpoint**: `POST /api/v1/iso20022/pain001/validate`

**Description**: Validate ISO 20022 pain.001 message without processing

**Required Permissions**: `payment:create`

**Response**:
```json
{
  "valid": true,
  "messageId": "MSG-20240115-001",
  "errors": [],
  "warnings": [],
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

### 4. Get Supported Messages

**Endpoint**: `GET /api/v1/iso20022/supported-messages`

**Description**: Get list of supported ISO 20022 message types

**Required Permissions**: `payment:read`

**Response**:
```json
{
  "supportedMessages": {
    "pain.001.001.03": {
      "name": "Customer Credit Transfer Initiation",
      "description": "Message for initiating credit transfers",
      "supported": true,
      "version": "1.3"
    },
    "pain.002.001.03": {
      "name": "Customer Payment Status Report",
      "description": "Message for reporting payment status", 
      "supported": true,
      "version": "1.3"
    }
  },
  "paymentMethods": {
    "TRF": "Credit Transfer",
    "DD": "Direct Debit",
    "CHK": "Check"
  },
  "localInstruments": {
    "RTP": "Real Time Payment",
    "ACH": "Automated Clearing House",
    "WIRE": "Wire Transfer",
    "SEPA": "Single Euro Payments Area"
  }
}
```

## Example Usage

### cURL Example

```bash
# Process pain.001 payment initiation
curl -X POST https://api.payment-engine.com/api/v1/iso20022/pain001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "CstmrCdtTrfInitn": {
      "GrpHdr": {
        "MsgId": "MSG-20240115-001",
        "CreDtTm": "2024-01-15T10:30:00.000Z",
        "NbOfTxs": "1",
        "CtrlSum": "1000.00",
        "InitgPty": {
          "Nm": "ABC Corporation"
        }
      },
      "PmtInf": {
        "PmtInfId": "PMT-20240115-001",
        "PmtMtd": "TRF",
        "ReqdExctnDt": "2024-01-15",
        "Dbtr": {
          "Nm": "John Doe"
        },
        "DbtrAcct": {
          "Id": {
            "Othr": {
              "Id": "ACC001001"
            }
          },
          "Ccy": "USD"
        },
        "CdtTrfTxInf": {
          "PmtId": {
            "EndToEndId": "E2E-20240115-001"
          },
          "Amt": {
            "InstdAmt": {
              "Ccy": "USD",
              "value": 1000.00
            }
          },
          "Cdtr": {
            "Nm": "Jane Smith"
          },
          "CdtrAcct": {
            "Id": {
              "Othr": {
                "Id": "ACC002001"
              }
            }
          },
          "RmtInf": {
            "Ustrd": ["Payment for services"]
          }
        }
      }
    }
  }'
```

### JavaScript SDK Example

```javascript
const PaymentEngine = require('@payment-engine/sdk');

const client = new PaymentEngine({
  apiKey: 'your-api-key',
  baseUrl: 'https://api.payment-engine.com'
});

// Create ISO 20022 pain.001 message
const pain001Message = {
  CstmrCdtTrfInitn: {
    GrpHdr: {
      MsgId: 'MSG-' + Date.now(),
      CreDtTm: new Date().toISOString(),
      NbOfTxs: '1',
      CtrlSum: '1000.00',
      InitgPty: {
        Nm: 'Your Company Name'
      }
    },
    PmtInf: {
      PmtInfId: 'PMT-' + Date.now(),
      PmtMtd: 'TRF',
      ReqdExctnDt: '2024-01-15',
      Dbtr: {
        Nm: 'John Doe'
      },
      DbtrAcct: {
        Id: {
          Othr: {
            Id: 'ACC001001'
          }
        },
        Ccy: 'USD'
      },
      CdtTrfTxInf: {
        PmtId: {
          EndToEndId: 'E2E-' + Date.now()
        },
        Amt: {
          InstdAmt: {
            Ccy: 'USD',
            value: 1000.00
          }
        },
        Cdtr: {
          Nm: 'Jane Smith'
        },
        CdtrAcct: {
          Id: {
            Othr: {
              Id: 'ACC002001'
            }
          }
        },
        RmtInf: {
          Ustrd: ['Payment for services']
        }
      }
    }
  }
};

// Process the payment
const response = await client.iso20022.processPain001(pain001Message);
console.log('Payment processed:', response);
```

### Python Example

```python
import requests
import json
from datetime import datetime

# ISO 20022 pain.001 message
pain001_message = {
    "CstmrCdtTrfInitn": {
        "GrpHdr": {
            "MsgId": f"MSG-{int(datetime.now().timestamp())}",
            "CreDtTm": datetime.now().isoformat() + "Z",
            "NbOfTxs": "1",
            "CtrlSum": "1000.00",
            "InitgPty": {
                "Nm": "Your Company Name"
            }
        },
        "PmtInf": {
            "PmtInfId": f"PMT-{int(datetime.now().timestamp())}",
            "PmtMtd": "TRF",
            "ReqdExctnDt": "2024-01-15",
            "Dbtr": {
                "Nm": "John Doe"
            },
            "DbtrAcct": {
                "Id": {
                    "Othr": {
                        "Id": "ACC001001"
                    }
                },
                "Ccy": "USD"
            },
            "CdtTrfTxInf": {
                "PmtId": {
                    "EndToEndId": f"E2E-{int(datetime.now().timestamp())}"
                },
                "Amt": {
                    "InstdAmt": {
                        "Ccy": "USD",
                        "value": 1000.00
                    }
                },
                "Cdtr": {
                    "Nm": "Jane Smith"
                },
                "CdtrAcct": {
                    "Id": {
                        "Othr": {
                            "Id": "ACC002001"
                        }
                    }
                },
                "RmtInf": {
                    "Ustrd": ["Payment for services"]
                }
            }
        }
    }
}

# Send request
response = requests.post(
    'https://api.payment-engine.com/api/v1/iso20022/pain001',
    headers={
        'Content-Type': 'application/json',
        'Authorization': 'Bearer your-token-here'
    },
    json=pain001_message
)

# Process response (pain.002 format)
if response.status_code == 201:
    pain002_response = response.json()
    status = pain002_response['CstmrPmtStsRpt']['PmtInfSts']['TxInfAndSts']['TxSts']
    print(f'Payment status: {status}')
else:
    print(f'Payment failed: {response.json()}')
```

## Message Validation

### Validate Before Processing

```bash
curl -X POST https://api.payment-engine.com/api/v1/iso20022/pain001/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{ "CstmrCdtTrfInitn": { ... } }'
```

**Response**:
```json
{
  "valid": true,
  "messageId": "MSG-20240115-001",
  "errors": [],
  "warnings": [
    "Debtor agent BIC not provided - using default routing"
  ],
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

## Error Handling

### ISO 20022 Specific Errors

| Error Code | Description | ISO 20022 Status |
|------------|-------------|-------------------|
| INVALID_ISO20022_MESSAGE | Message structure validation failed | RJCT |
| INVALID_PAYMENT_REQUEST | Business validation failed | RJCT |
| INSUFFICIENT_FUNDS | Account has insufficient funds | RJCT |
| INVALID_ACCOUNT | Account not found or inactive | RJCT |
| PROCESSING_ERROR | Internal processing error | RJCT |

### Error Response Format

```json
{
  "error": {
    "code": "INVALID_ISO20022_MESSAGE",
    "message": "pain.001 message validation failed",
    "details": {
      "errors": [
        "GroupHeader.MessageId is required",
        "PaymentInformation.DebtorAccount is required"
      ],
      "warnings": []
    },
    "timestamp": "2024-01-15T10:30:00.000Z"
  }
}
```

## Integration Patterns

### 1. Standard Banking Integration

```javascript
// Corporate banking system integration
class CorporateBankingGateway {
  async initiatePayment(paymentInstruction) {
    // Convert internal format to ISO 20022
    const pain001 = this.createPain001Message(paymentInstruction);
    
    // Send to Payment Engine
    const response = await this.paymentEngineClient.iso20022.processPain001(pain001);
    
    // Process pain.002 response
    return this.processPain002Response(response);
  }
  
  createPain001Message(instruction) {
    return {
      CstmrCdtTrfInitn: {
        GrpHdr: {
          MsgId: this.generateMessageId(),
          CreDtTm: new Date().toISOString(),
          NbOfTxs: '1',
          CtrlSum: instruction.amount.toString(),
          InitgPty: {
            Nm: instruction.initiatorName,
            Id: {
              OrgId: {
                AnyBIC: instruction.initiatorBIC
              }
            }
          }
        },
        PmtInf: {
          // ... complete pain.001 structure
        }
      }
    };
  }
}
```

### 2. SWIFT Integration

```java
// SWIFT MT to ISO 20022 transformation
@Service
public class SwiftToIso20022Service {
    
    public Pain001Message transformMT103ToPain001(MT103 swiftMessage) {
        Pain001Message pain001 = new Pain001Message();
        
        // Transform SWIFT MT103 fields to pain.001 structure
        Pain001Message.CustomerCreditTransferInitiation cctInitn = 
            new Pain001Message.CustomerCreditTransferInitiation();
        
        // Map SWIFT fields to ISO 20022 structure
        Pain001Message.GroupHeader grpHdr = new Pain001Message.GroupHeader();
        grpHdr.setMessageId(swiftMessage.getField20().getValue()); // :20: Reference
        grpHdr.setCreationDateTime(LocalDateTime.now().toString());
        // ... continue mapping
        
        return pain001;
    }
}
```

## Compliance Features

### Regulatory Reporting

The pain.001 message supports regulatory reporting fields:

```json
{
  "RgltryRptg": {
    "DbtCdtRptgInd": "BOTH",
    "Authrty": {
      "Nm": "Federal Reserve",
      "Ctry": "US"
    },
    "Dtls": [{
      "Tp": "CUST",
      "Cd": "BALANCE_OF_PAYMENTS",
      "Amt": {
        "InstdAmt": {
          "Ccy": "USD",
          "value": 1000.00
        }
      },
      "Inf": ["Trade payment for goods"]
    }]
  }
}
```

### Tax Information

```json
{
  "Tax": {
    "Cdtr": {
      "TaxId": "123456789",
      "RegnId": "REG123456"
    },
    "Dbtr": {
      "TaxId": "987654321"
    },
    "TtlTaxAmt": {
      "InstdAmt": {
        "Ccy": "USD",
        "value": 100.00
      }
    }
  }
}
```

## Migration Guide

### From Legacy API to ISO 20022

**Old Format**:
```json
{
  "fromAccountId": "acc_123",
  "toAccountId": "acc_456", 
  "amount": 1000.00,
  "description": "Payment"
}
```

**New ISO 20022 Format**:
```json
{
  "CstmrCdtTrfInitn": {
    "GrpHdr": { ... },
    "PmtInf": {
      "DbtrAcct": {
        "Id": { "Othr": { "Id": "acc_123" } }
      },
      "CdtTrfTxInf": {
        "Amt": {
          "InstdAmt": { "Ccy": "USD", "value": 1000.00 }
        },
        "CdtrAcct": {
          "Id": { "Othr": { "Id": "acc_456" } }
        },
        "RmtInf": {
          "Ustrd": ["Payment"]
        }
      }
    }
  }
}
```

## Testing

### Test Environment

- **Base URL**: `https://staging-api.payment-engine.com`
- **Test Accounts**: Pre-configured test accounts with IBANs
- **Message Validation**: Full ISO 20022 validation enabled

### Sample Test Messages

Complete test pain.001 messages are available in:
- `/tests/iso20022/sample-pain001-messages.json`
- `/tests/iso20022/validation-test-cases.json`

## Support

- **ISO 20022 Documentation**: [https://docs.payment-engine.com/iso20022](https://docs.payment-engine.com/iso20022)
- **Message Examples**: [https://examples.payment-engine.com/iso20022](https://examples.payment-engine.com/iso20022)
- **Support Email**: [iso20022-support@payment-engine.com](mailto:iso20022-support@payment-engine.com)
- **Banking Standards**: [ISO 20022 Official Site](https://www.iso20022.org/)