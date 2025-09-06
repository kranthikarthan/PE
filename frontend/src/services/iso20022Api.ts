import apiService from './api';
import {
  Pain001Message,
  Pain002Message,
  Camt055Message,
  Camt053Message,
  Iso20022MessageStatus,
  Iso20022Statistics,
  CancellationResult,
  Iso20022ValidationResult,
  BulkProcessingResult,
  PagedResponse,
  ApiResponse
} from '../types/iso20022';

/**
 * API service for ISO 20022 message operations
 */
class Iso20022ApiService {

  // ============================================================================
  // PAIN MESSAGES (Customer Initiated)
  // ============================================================================

  /**
   * Process pain.001 - Customer Credit Transfer Initiation
   */
  async processPain001(pain001Message: Pain001Message): Promise<ApiResponse<Pain002Message>> {
    const response = await apiService.request({
      method: 'POST',
      url: '/api/v1/iso20022/pain001',
      data: pain001Message,
    });
    return response;
  }

  /**
   * Get pain.002 - Customer Payment Status Report
   */
  async getPain002Status(transactionId: string, originalMessageId?: string): Promise<ApiResponse<Pain002Message>> {
    const params = originalMessageId ? { originalMessageId } : {};
    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/iso20022/pain002/${transactionId}`,
      params,
    });
    return response;
  }

  /**
   * Process pain.007 - Customer Payment Reversal
   */
  async processPain007(pain007Message: any): Promise<ApiResponse<any>> {
    const response = await apiService.request({
      method: 'POST',
      url: '/api/v1/iso20022/pain007',
      data: pain007Message,
    });
    return response;
  }

  // ============================================================================
  // CAMT MESSAGES (Cash Management)
  // ============================================================================

  /**
   * Process camt.055 - Customer Payment Cancellation Request
   */
  async processCamt055(camt055Message: Camt055Message): Promise<ApiResponse<{
    cancellationResults: CancellationResult[];
    camt029Response: any;
    totalCancellations: number;
  }>> {
    const response = await apiService.request({
      method: 'POST',
      url: '/api/v1/iso20022/camt055',
      data: camt055Message,
    });
    return response;
  }

  /**
   * Generate camt.053 - Bank to Customer Statement
   */
  async generateCamt053(
    accountId: string,
    fromDate?: string,
    toDate?: string,
    includeTransactionDetails: boolean = false
  ): Promise<Camt053Message> {
    const params: any = { includeTransactionDetails };
    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;

    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/iso20022/camt053/account/${accountId}`,
      params,
    });
    return response;
  }

  /**
   * Generate camt.054 - Bank to Customer Debit Credit Notification
   */
  async generateCamt054(
    accountId: string,
    fromDate?: string,
    toDate?: string
  ): Promise<any> {
    const params: any = {};
    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;

    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/iso20022/camt054/account/${accountId}`,
      params,
    });
    return response;
  }

  // ============================================================================
  // PACS MESSAGES (Scheme Processing)
  // ============================================================================

  /**
   * Process pacs.008 - FI to FI Customer Credit Transfer
   */
  async processPacs008(pacs008Message: any): Promise<ApiResponse<any>> {
    const response = await apiService.request({
      method: 'POST',
      url: '/api/v1/iso20022/pacs008',
      data: pacs008Message,
    });
    return response;
  }

  /**
   * Generate pacs.002 - FI to FI Payment Status Report
   */
  async generatePacs002(transactionId: string, originalMessageId?: string): Promise<ApiResponse<any>> {
    const params = originalMessageId ? { originalMessageId } : {};
    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/iso20022/pacs002/${transactionId}`,
      params,
    });
    return response;
  }

  /**
   * Generate pacs.004 - Payment Return
   */
  async generatePacs004(transactionId: string, returnReason: string, reasonCode: string): Promise<ApiResponse<any>> {
    const response = await apiService.request({
      method: 'POST',
      url: `/api/v1/iso20022/pacs004/${transactionId}`,
      data: {
        reason: returnReason,
        reasonCode: reasonCode,
      },
    });
    return response;
  }

  // ============================================================================
  // MESSAGE VALIDATION AND UTILITIES
  // ============================================================================

  /**
   * Validate any ISO 20022 message
   */
  async validateMessage(messageType: string, message: any): Promise<Iso20022ValidationResult> {
    const response = await apiService.request({
      method: 'POST',
      url: `/api/v1/iso20022/validate/${messageType}`,
      data: message,
    });
    return response;
  }

  /**
   * Get supported ISO 20022 message types
   */
  async getSupportedMessages(): Promise<ApiResponse<any>> {
    const response = await apiService.request({
      method: 'GET',
      url: '/api/v1/iso20022/supported-messages',
    });
    return response;
  }

  /**
   * Transform message between formats
   */
  async transformMessage(fromFormat: string, toFormat: string, message: any): Promise<ApiResponse<any>> {
    const response = await apiService.request({
      method: 'POST',
      url: `/api/v1/iso20022/transform/${fromFormat}/${toFormat}`,
      data: message,
    });
    return response;
  }

  // ============================================================================
  // BULK PROCESSING
  // ============================================================================

  /**
   * Process bulk pain.001 messages
   */
  async processBulkPain001(messages: Pain001Message[]): Promise<BulkProcessingResult> {
    const response = await apiService.request({
      method: 'POST',
      url: '/api/v1/iso20022/bulk/pain001',
      data: { messages },
    });
    return response;
  }

  /**
   * Get batch processing status
   */
  async getBatchStatus(batchId: string): Promise<ApiResponse<any>> {
    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/iso20022/batch/${batchId}/status`,
    });
    return response;
  }

  // ============================================================================
  // REPORTING AND ANALYTICS
  // ============================================================================

  /**
   * Get ISO 20022 message statistics
   */
  async getMessageStatistics(
    messageType?: string,
    fromDate?: string,
    toDate?: string
  ): Promise<Iso20022Statistics> {
    const params: any = {};
    if (messageType) params.messageType = messageType;
    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;

    const response = await apiService.request({
      method: 'GET',
      url: '/api/v1/iso20022/statistics',
      params,
    });
    return response;
  }

  /**
   * Search ISO 20022 messages
   */
  async searchMessages(criteria: {
    messageType?: string;
    status?: string;
    direction?: string;
    fromDate?: string;
    toDate?: string;
    endToEndId?: string;
    messageId?: string;
    page?: number;
    size?: number;
  }): Promise<PagedResponse<Iso20022MessageStatus>> {
    const response = await apiService.request({
      method: 'GET',
      url: '/api/v1/iso20022/messages/search',
      params: criteria,
    });
    return response;
  }

  // ============================================================================
  // CONVENIENCE METHODS
  // ============================================================================

  /**
   * Create simple pain.001 message from basic payment data
   */
  createPain001FromPayment(payment: {
    debtorName: string;
    debtorAccountId: string;
    creditorName: string;
    creditorAccountId: string;
    amount: number;
    currency: string;
    description: string;
    localInstrument?: string;
  }): Pain001Message {
    const messageId = `MSG-${Date.now()}`;
    const endToEndId = `E2E-${Date.now()}`;
    
    return {
      CstmrCdtTrfInitn: {
        GrpHdr: {
          MsgId: messageId,
          CreDtTm: new Date().toISOString(),
          NbOfTxs: '1',
          CtrlSum: payment.amount.toString(),
          InitgPty: {
            Nm: payment.debtorName,
          },
        },
        PmtInf: {
          PmtInfId: `PMT-${Date.now()}`,
          PmtMtd: 'TRF',
          PmtTpInf: {
            LclInstrm: {
              Cd: payment.localInstrument || 'RTP',
            },
          },
          ReqdExctnDt: new Date().toISOString().split('T')[0],
          Dbtr: {
            Nm: payment.debtorName,
          },
          DbtrAcct: {
            Id: {
              Othr: {
                Id: payment.debtorAccountId,
              },
            },
            Ccy: payment.currency,
          },
          CdtTrfTxInf: {
            PmtId: {
              EndToEndId: endToEndId,
            },
            Amt: {
              InstdAmt: {
                Ccy: payment.currency,
                value: payment.amount,
              },
            },
            Cdtr: {
              Nm: payment.creditorName,
            },
            CdtrAcct: {
              Id: {
                Othr: {
                  Id: payment.creditorAccountId,
                },
              },
            },
            RmtInf: {
              Ustrd: [payment.description],
            },
          },
        },
      },
    };
  }

  /**
   * Create camt.055 cancellation message
   */
  createCamt055FromCancellation(cancellation: {
    originalEndToEndId: string;
    reasonCode: string;
    reason: string;
    originalAmount: number;
    originalCurrency: string;
  }): Camt055Message {
    const messageId = `CANCEL-${Date.now()}`;
    
    return {
      CstmrPmtCxlReq: {
        GrpHdr: {
          MsgId: messageId,
          CreDtTm: new Date().toISOString(),
          NbOfTxs: '1',
          InitgPty: {
            Nm: 'Payment Engine Frontend',
          },
        },
        Undrlyg: [{
          TxInf: [{
            CxlId: `CXL-${Date.now()}`,
            OrgnlEndToEndId: cancellation.originalEndToEndId,
            OrgnlInstdAmt: {
              Ccy: cancellation.originalCurrency,
              value: cancellation.originalAmount,
            },
            CxlRsnInf: [{
              Rsn: {
                Cd: cancellation.reasonCode,
              },
              AddtlInf: [cancellation.reason],
            }],
          }],
        }],
      },
    };
  }

  /**
   * Health check for ISO 20022 service
   */
  async healthCheck(): Promise<ApiResponse<any>> {
    const response = await apiService.request({
      method: 'GET',
      url: '/api/v1/iso20022/health',
    });
    return response;
  }
}

// Create and export singleton instance
const iso20022ApiService = new Iso20022ApiService();
export default iso20022ApiService;