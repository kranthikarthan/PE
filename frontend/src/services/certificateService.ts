import axios from 'axios';
import {
  CertificateInfo,
  CertificateGenerationRequest,
  CertificateGenerationResult,
  PfxImportRequest,
  PfxImportResult,
  CertificateValidationResult,
  CertificateFilter,
  TrustedCertificate,
  CertificateError,
  CertificateApiResponse,
  CertificateStats
} from '../types/certificate';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

const certificateApi = axios.create({
  baseURL: `${API_BASE_URL}/api/v1/certificates`,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
certificateApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
certificateApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const certificateService = {
  /**
   * Generate a new certificate and private key pair
   */
  generateCertificate: async (request: CertificateGenerationRequest): Promise<CertificateGenerationResult> => {
    try {
      const response = await certificateApi.post<CertificateApiResponse<CertificateGenerationResult>>('/generate', request);
      
      if (response.data.certificate) {
        return response.data.certificate;
      }
      
      throw new Error('Invalid response format');
    } catch (error: any) {
      if (error.response?.data) {
        throw new Error(error.response.data.message || 'Failed to generate certificate');
      }
      throw new Error('Failed to generate certificate');
    }
  },

  /**
   * Import a .pfx certificate file
   */
  importPfxCertificate: async (file: File, request: PfxImportRequest): Promise<PfxImportResult> => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('password', request.password);
      formData.append('tenantId', request.tenantId || '');
      formData.append('certificateType', request.certificateType || 'PFX_IMPORTED');
      formData.append('description', request.description || '');
      formData.append('validateCertificate', request.validateCertificate?.toString() || 'true');
      formData.append('extractPrivateKey', request.extractPrivateKey?.toString() || 'true');
      formData.append('extractCertificateChain', request.extractCertificateChain?.toString() || 'true');

      const response = await certificateApi.post<CertificateApiResponse<PfxImportResult>>('/import/pfx', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      if (response.data.certificate) {
        return response.data.certificate;
      }
      
      throw new Error('Invalid response format');
    } catch (error: any) {
      if (error.response?.data) {
        throw new Error(error.response.data.message || 'Failed to import PFX certificate');
      }
      throw new Error('Failed to import PFX certificate');
    }
  },

  /**
   * Validate a certificate
   */
  validateCertificate: async (certificateId: string): Promise<CertificateValidationResult> => {
    try {
      const response = await certificateApi.post<CertificateApiResponse<CertificateValidationResult>>(`/${certificateId}/validate`);
      
      if (response.data.validation) {
        return response.data.validation;
      }
      
      throw new Error('Invalid response format');
    } catch (error: any) {
      if (error.response?.data) {
        throw new Error(error.response.data.message || 'Failed to validate certificate');
      }
      throw new Error('Failed to validate certificate');
    }
  },

  /**
   * Get all certificates with optional filtering
   */
  getAllCertificates: async (filter?: CertificateFilter): Promise<CertificateInfo[]> => {
    try {
      const params = new URLSearchParams();
      
      if (filter?.tenantId) params.append('tenantId', filter.tenantId);
      if (filter?.certificateType) params.append('certificateType', filter.certificateType);
      if (filter?.status) params.append('status', filter.status);
      if (filter?.validFrom) params.append('validFrom', filter.validFrom);
      if (filter?.validTo) params.append('validTo', filter.validTo);

      const response = await certificateApi.get<CertificateInfo[]>(`?${params.toString()}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.data) {
        throw new Error(error.response.data.message || 'Failed to get certificates');
      }
      throw new Error('Failed to get certificates');
    }
  },

  /**
   * Get certificate by ID
   */
  getCertificateById: async (certificateId: string): Promise<CertificateInfo> => {
    try {
      const response = await certificateApi.get<CertificateInfo>(`/${certificateId}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.data) {
        throw new Error(error.response.data.message || 'Failed to get certificate');
      }
      throw new Error('Failed to get certificate');
    }
  },

  /**
   * Delete certificate
   */
  deleteCertificate: async (certificateId: string): Promise<void> => {
    try {
      await certificateApi.delete(`/${certificateId}`);
    } catch (error: any) {
      if (error.response?.data) {
        throw new Error(error.response.data.message || 'Failed to delete certificate');
      }
      throw new Error('Failed to delete certificate');
    }
  },

  /**
   * Rotate certificate
   */
  rotateCertificate: async (certificateId: string, request: CertificateGenerationRequest): Promise<CertificateGenerationResult> => {
    try {
      const response = await certificateApi.post<CertificateApiResponse<CertificateGenerationResult>>(`/${certificateId}/rotate`, request);
      
      if (response.data.certificate) {
        return response.data.certificate;
      }
      
      throw new Error('Invalid response format');
    } catch (error: any) {
      if (error.response?.data) {
        throw new Error(error.response.data.message || 'Failed to rotate certificate');
      }
      throw new Error('Failed to rotate certificate');
    }
  },

  /**
   * Get certificates expiring soon
   */
  getExpiringCertificates: async (daysAhead: number = 30): Promise<CertificateInfo[]> => {
    try {
      const response = await certificateApi.get<CertificateInfo[]>(`/expiring?daysAhead=${daysAhead}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.data) {
        throw new Error(error.response.data.message || 'Failed to get expiring certificates');
      }
      throw new Error('Failed to get expiring certificates');
    }
  },

  /**
   * Get certificate statistics
   */
  getCertificateStats: async (): Promise<CertificateStats> => {
    try {
      const certificates = await certificateService.getAllCertificates();
      
      const stats: CertificateStats = {
        totalCertificates: certificates.length,
        activeCertificates: certificates.filter(c => c.status === 'ACTIVE').length,
        expiredCertificates: certificates.filter(c => c.status === 'EXPIRED').length,
        expiringSoon: certificates.filter(c => {
          const validTo = new Date(c.validTo);
          const now = new Date();
          const daysUntilExpiry = Math.ceil((validTo.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
          return daysUntilExpiry <= 30 && daysUntilExpiry > 0;
        }).length,
        rotatedCertificates: certificates.filter(c => c.status === 'ROTATED').length,
        byType: {},
        byStatus: {},
        byTenant: {}
      };

      // Calculate by type
      certificates.forEach(cert => {
        stats.byType[cert.certificateType] = (stats.byType[cert.certificateType] || 0) + 1;
      });

      // Calculate by status
      certificates.forEach(cert => {
        stats.byStatus[cert.status] = (stats.byStatus[cert.status] || 0) + 1;
      });

      // Calculate by tenant
      certificates.forEach(cert => {
        const tenant = cert.tenantId || 'default';
        stats.byTenant[tenant] = (stats.byTenant[tenant] || 0) + 1;
      });

      return stats;
    } catch (error: any) {
      throw new Error('Failed to get certificate statistics');
    }
  },

  /**
   * Download certificate as PEM file
   */
  downloadCertificatePem: (certificate: CertificateInfo, certificatePem: string): void => {
    const blob = new Blob([certificatePem], { type: 'application/x-pem-file' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${certificate.subjectDN.replace(/[^a-zA-Z0-9]/g, '_')}_certificate.pem`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },

  /**
   * Download private key as PEM file
   */
  downloadPrivateKeyPem: (certificate: CertificateInfo, privateKeyPem: string): void => {
    const blob = new Blob([privateKeyPem], { type: 'application/x-pem-file' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${certificate.subjectDN.replace(/[^a-zA-Z0-9]/g, '_')}_private_key.pem`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },

  /**
   * Download public key as PEM file
   */
  downloadPublicKeyPem: (certificate: CertificateInfo, publicKeyPem: string): void => {
    const blob = new Blob([publicKeyPem], { type: 'application/x-pem-file' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${certificate.subjectDN.replace(/[^a-zA-Z0-9]/g, '_')}_public_key.pem`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },

  /**
   * Copy text to clipboard
   */
  copyToClipboard: async (text: string): Promise<void> => {
    try {
      await navigator.clipboard.writeText(text);
    } catch (error) {
      // Fallback for older browsers
      const textArea = document.createElement('textarea');
      textArea.value = text;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
    }
  },

  /**
   * Format certificate details for display
   */
  formatCertificateDetails: (certificate: CertificateInfo): Record<string, string> => {
    return {
      'Subject DN': certificate.subjectDN,
      'Issuer DN': certificate.issuerDN,
      'Serial Number': certificate.serialNumber,
      'Valid From': new Date(certificate.validFrom).toLocaleString(),
      'Valid To': new Date(certificate.validTo).toLocaleString(),
      'Public Key Algorithm': certificate.publicKeyAlgorithm,
      'Key Size': certificate.keySize?.toString() || 'N/A',
      'Signature Algorithm': certificate.signatureAlgorithm,
      'Certificate Type': certificate.certificateType,
      'Status': certificate.status,
      'Validation Status': certificate.validationStatus || 'N/A',
      'Created At': new Date(certificate.createdAt).toLocaleString(),
      'Updated At': new Date(certificate.updatedAt).toLocaleString()
    };
  },

  /**
   * Check if certificate is expiring soon
   */
  isExpiringSoon: (certificate: CertificateInfo, daysAhead: number = 30): boolean => {
    const validTo = new Date(certificate.validTo);
    const now = new Date();
    const daysUntilExpiry = Math.ceil((validTo.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    return daysUntilExpiry <= daysAhead && daysUntilExpiry > 0;
  },

  /**
   * Check if certificate is expired
   */
  isExpired: (certificate: CertificateInfo): boolean => {
    const validTo = new Date(certificate.validTo);
    const now = new Date();
    return validTo < now;
  },

  /**
   * Get days until expiry
   */
  getDaysUntilExpiry: (certificate: CertificateInfo): number => {
    const validTo = new Date(certificate.validTo);
    const now = new Date();
    return Math.ceil((validTo.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  }
};

export default certificateService;