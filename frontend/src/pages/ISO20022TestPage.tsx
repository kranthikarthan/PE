import React, { useState } from 'react';
import axios from 'axios';

/**
 * ISO20022 Test Page
 * Simple page to test ISO20022 message submission and view parsed results
 */
const ISO20022TestPage: React.FC = () => {
  const [payload, setPayload] = useState<string>(
    JSON.stringify({
      CstmrCdtTrfInitn: {
        GrpHdr: {
          MsgId: `MSG-${Date.now()}`,
          CreDtTm: new Date().toISOString(),
          NbOfTxs: '1',
          CtrlSum: 1000.00,
          InitgPty: {
            Nm: 'Test Corporation'
          }
        },
        PmtInf: {
          PmtInfId: `PMT-${Date.now()}`,
          PmtMtd: 'TRF',
          ReqdExctnDt: new Date().toISOString().split('T')[0],
          Dbtr: {
            Nm: 'John Doe'
          },
          DbtrAcct: {
            Id: {
              IBAN: 'US64SVBKUS6S3300958879'
            }
          },
          CdtTrfTxInf: {
            PmtId: {
              EndToEndId: `E2E-${Date.now()}`
            },
            Amt: {
              InstdAmt: {
                Ccy: 'USD',
                Value: 1000.00
              }
            },
            Cdtr: {
              Nm: 'Jane Smith'
            },
            CdtrAcct: {
              Id: {
                IBAN: 'US64SVBKUS6S3300123456'
              }
            }
          }
        }
      }
    }, null, 2)
  );

  const [response, setResponse] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [correlationId, setCorrelationId] = useState<string | null>(null);
  const [token, setToken] = useState<string>('');

  const apiBase = process.env.REACT_APP_API_BASE || 'http://localhost:8082';

  // Get JWT token
  const handleGetToken = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const tokenResponse = await axios.post(
        `${apiBase}/payment-processing/api/auth/admin-token`
      );
      
      setToken(tokenResponse.data.token);
      setError(null);
    } catch (err: any) {
      setError(`Failed to get token: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  // Submit ISO20022 payload
  const handleSubmit = async () => {
    if (!token) {
      setError('Please get authentication token first');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setResponse(null);
      setCorrelationId(null);

      const parsedPayload = JSON.parse(payload);
      
      const result = await axios.post(
        `${apiBase}/payment-processing/api/v1/iso20022/comprehensive/pain001-to-clearing-system`,
        parsedPayload,
        {
          params: {
            tenantId: 'tenant-001',
            paymentType: 'CREDIT_TRANSFER',
            localInstrumentCode: 'IMMEDIATE',
            responseMode: 'IMMEDIATE'
          },
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            'X-Idempotency-Key': `IDEM-${Date.now()}`,
            'X-Tenant-ID': 'tenant-001'
          }
        }
      );

      setResponse(result.data);
      
      // Extract correlation ID from response
      if (result.data.correlationId) {
        setCorrelationId(result.data.correlationId);
      }
      
      // Also check headers
      const headerCorrelationId = result.headers['x-correlation-id'];
      if (headerCorrelationId && !correlationId) {
        setCorrelationId(headerCorrelationId);
      }

    } catch (err: any) {
      if (err.response) {
        // Server responded with error
        setError(JSON.stringify(err.response.data, null, 2));
      } else {
        setError(`Error: ${err.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  // Format payload
  const handleFormat = () => {
    try {
      const parsed = JSON.parse(payload);
      setPayload(JSON.stringify(parsed, null, 2));
      setError(null);
    } catch (err: any) {
      setError(`Invalid JSON: ${err.message}`);
    }
  };

  // Parse summary from response
  const renderSummary = () => {
    if (!response) return null;

    return (
      <div className="summary-card">
        <h3>📊 Parsed Summary</h3>
        <div className="summary-grid">
          <div className="summary-item">
            <span className="label">Message ID:</span>
            <span className="value">{response.messageId || 'N/A'}</span>
          </div>
          <div className="summary-item">
            <span className="label">Correlation ID:</span>
            <span className="value correlation-id">{response.correlationId || correlationId || 'N/A'}</span>
          </div>
          <div className="summary-item">
            <span className="label">Status:</span>
            <span className={`value status-${response.status?.toLowerCase()}`}>
              {response.status || 'UNKNOWN'}
            </span>
          </div>
          <div className="summary-item">
            <span className="label">Transaction ID:</span>
            <span className="value">{response.transactionId || 'N/A'}</span>
          </div>
          <div className="summary-item">
            <span className="label">Clearing System:</span>
            <span className="value">{response.clearingSystemCode || 'N/A'}</span>
          </div>
          <div className="summary-item">
            <span className="label">Processing Time:</span>
            <span className="value">{response.processingTimeMs ? `${response.processingTimeMs}ms` : 'N/A'}</span>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="iso20022-test-page">
      <div className="container">
        <h1>🏦 ISO20022 Message Tester</h1>
        <p className="subtitle">Test ISO20022 PAIN.001 payment messages</p>

        {/* Authentication Section */}
        <div className="auth-section">
          <button 
            onClick={handleGetToken} 
            disabled={loading}
            className="btn btn-primary"
          >
            {token ? '🔄 Refresh Token' : '🔑 Get Authentication Token'}
          </button>
          {token && (
            <div className="token-display">
              ✅ Token acquired (expires in 1 hour)
            </div>
          )}
        </div>

        {/* Payload Editor */}
        <div className="editor-section">
          <div className="editor-header">
            <h2>📝 ISO20022 PAIN.001 Payload</h2>
            <button onClick={handleFormat} className="btn btn-secondary">
              Format JSON
            </button>
          </div>
          <textarea
            value={payload}
            onChange={(e) => setPayload(e.target.value)}
            className="payload-editor"
            rows={20}
            spellCheck={false}
          />
        </div>

        {/* Action Buttons */}
        <div className="action-buttons">
          <button 
            onClick={handleSubmit} 
            disabled={loading || !token}
            className="btn btn-success btn-large"
          >
            {loading ? '⏳ Processing...' : '🚀 Submit Payment'}
          </button>
        </div>

        {/* Error Display */}
        {error && (
          <div className="error-card">
            <h3>❌ Error</h3>
            <pre>{error}</pre>
          </div>
        )}

        {/* Summary Display */}
        {response && renderSummary()}

        {/* Full Response */}
        {response && (
          <div className="response-card">
            <h3>📄 Full Response</h3>
            <pre>{JSON.stringify(response, null, 2)}</pre>
          </div>
        )}

        {/* API Info */}
        <div className="api-info">
          <h3>ℹ️ API Information</h3>
          <p><strong>Base URL:</strong> {apiBase}</p>
          <p><strong>Endpoint:</strong> /payment-processing/api/v1/iso20022/comprehensive/pain001-to-clearing-system</p>
          <p><strong>Correlation ID:</strong> {correlationId ? <code>{correlationId}</code> : 'Submit a request to see'}</p>
        </div>
      </div>

      <style jsx>{`
        .iso20022-test-page {
          padding: 20px;
          max-width: 1200px;
          margin: 0 auto;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        }

        .container {
          background: #ffffff;
          border-radius: 8px;
          padding: 30px;
          box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        h1 {
          color: #1a1a1a;
          margin-bottom: 10px;
        }

        .subtitle {
          color: #666;
          margin-bottom: 30px;
        }

        .auth-section {
          margin-bottom: 30px;
          padding: 20px;
          background: #f8f9fa;
          border-radius: 6px;
        }

        .token-display {
          margin-top: 10px;
          padding: 10px;
          background: #d4edda;
          color: #155724;
          border-radius: 4px;
        }

        .editor-section {
          margin-bottom: 20px;
        }

        .editor-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
        }

        .payload-editor {
          width: 100%;
          font-family: 'Monaco', 'Courier New', monospace;
          font-size: 13px;
          padding: 15px;
          border: 2px solid #e0e0e0;
          border-radius: 6px;
          resize: vertical;
        }

        .payload-editor:focus {
          outline: none;
          border-color: #007bff;
        }

        .action-buttons {
          margin: 20px 0;
          text-align: center;
        }

        .btn {
          padding: 10px 20px;
          border: none;
          border-radius: 6px;
          cursor: pointer;
          font-size: 14px;
          font-weight: 600;
          transition: all 0.2s;
        }

        .btn:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        .btn-primary {
          background: #007bff;
          color: white;
        }

        .btn-primary:hover:not(:disabled) {
          background: #0056b3;
        }

        .btn-secondary {
          background: #6c757d;
          color: white;
        }

        .btn-success {
          background: #28a745;
          color: white;
        }

        .btn-success:hover:not(:disabled) {
          background: #218838;
        }

        .btn-large {
          padding: 15px 40px;
          font-size: 16px;
        }

        .summary-card,
        .response-card,
        .error-card,
        .api-info {
          margin: 20px 0;
          padding: 20px;
          border-radius: 6px;
          border: 1px solid #e0e0e0;
        }

        .summary-card {
          background: #f0f8ff;
          border-color: #007bff;
        }

        .summary-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
          gap: 15px;
          margin-top: 15px;
        }

        .summary-item {
          display: flex;
          flex-direction: column;
          gap: 5px;
        }

        .summary-item .label {
          font-size: 12px;
          color: #666;
          font-weight: 600;
          text-transform: uppercase;
        }

        .summary-item .value {
          font-size: 16px;
          color: #1a1a1a;
          font-weight: 500;
        }

        .correlation-id {
          font-family: 'Monaco', monospace;
          background: #fff;
          padding: 5px 10px;
          border-radius: 4px;
          border: 1px solid #007bff;
        }

        .status-success {
          color: #28a745;
          font-weight: bold;
        }

        .status-error,
        .status-failed {
          color: #dc3545;
          font-weight: bold;
        }

        .error-card {
          background: #fff3cd;
          border-color: #ffc107;
        }

        .error-card h3 {
          color: #856404;
        }

        .response-card pre,
        .error-card pre {
          background: #f8f9fa;
          padding: 15px;
          border-radius: 4px;
          overflow-x: auto;
          font-size: 13px;
        }

        .api-info {
          background: #e7f3ff;
          border-color: #0066cc;
        }

        .api-info code {
          background: #fff;
          padding: 2px 6px;
          border-radius: 3px;
          font-family: 'Monaco', monospace;
        }

        @media (max-width: 768px) {
          .summary-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  );
};

export default ISO20022TestPage;
