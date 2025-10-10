export interface CertificateInfo {
  id: string;
  subjectDN: string;
  issuerDN: string;
  serialNumber: string;
  validFrom: string;
  validTo: string;
  publicKeyAlgorithm: string;
  keySize?: number;
  signatureAlgorithm: string;
  certificateType: string;
  tenantId: string;
  status: 'ACTIVE' | 'INACTIVE' | 'EXPIRED' | 'ROTATED' | 'REVOKED';
  alias?: string;
  validationStatus?: 'VALID' | 'INVALID' | 'PENDING' | 'EXPIRED' | 'REVOKED';
  validationMessage?: string;
  lastValidated?: string;
  rotatedTo?: string;
  rotatedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CertificateGenerationRequest {
  subjectDN: string;
  tenantId?: string;
  certificateType?: string;
  validityDays?: number;
  keyUsage?: string[];
  extendedKeyUsage?: string[];
  description?: string;
  includePrivateKey?: boolean;
  includePublicKey?: boolean;
}

export interface CertificateGenerationResult {
  certificateId: string;
  subjectDN: string;
  issuerDN: string;
  validFrom: string;
  validTo: string;
  serialNumber: string;
  publicKeyAlgorithm: string;
  keySize?: number;
  signatureAlgorithm: string;
  certificatePem: string;
  privateKeyPem: string;
  publicKeyPem?: string;
}

export interface PfxImportRequest {
  password: string;
  tenantId?: string;
  certificateType?: string;
  description?: string;
  validateCertificate?: boolean;
  extractPrivateKey?: boolean;
  extractCertificateChain?: boolean;
}

export interface PfxImportResult {
  certificateId: string;
  subjectDN: string;
  issuerDN: string;
  validFrom: string;
  validTo: string;
  serialNumber: string;
  publicKeyAlgorithm: string;
  signatureAlgorithm: string;
  certificatePem: string;
  privateKeyPem: string;
  certificateChain?: string[];
  alias: string;
}

export interface CertificateValidationResult {
  status: 'VALID' | 'INVALID' | 'PENDING' | 'EXPIRED' | 'REVOKED';
  message: string;
  validFrom: string;
  validTo: string;
  issuerDN: string;
  subjectDN: string;
  serialNumber?: string;
  publicKeyAlgorithm?: string;
  signatureAlgorithm?: string;
  isValid: boolean;
  validationDetails?: string;
}

export interface CertificateFilter {
  tenantId?: string;
  certificateType?: string;
  status?: string;
  validFrom?: string;
  validTo?: string;
  subjectDN?: string;
  issuerDN?: string;
  serialNumber?: string;
  includeExpired?: boolean;
  includeRotated?: boolean;
}

export interface TrustedCertificate {
  id: string;
  alias: string;
  subjectDN: string;
  issuerDN: string;
  serialNumber: string;
  validFrom: string;
  validTo: string;
  publicKeyAlgorithm: string;
  keySize?: number;
  signatureAlgorithm: string;
  certificateType: string;
  tenantId: string;
  status: 'ACTIVE' | 'INACTIVE' | 'EXPIRED' | 'REVOKED';
  description?: string;
  source: string;
  createdAt: string;
  updatedAt: string;
}

export interface CertificateError {
  error: string;
  message: string;
  certificateId?: string;
}

export interface CertificateApiResponse<T> {
  certificate?: T;
  validation?: T;
  message?: string;
  error?: string;
  certificateId?: string;
}

export interface CertificateStats {
  totalCertificates: number;
  activeCertificates: number;
  expiredCertificates: number;
  expiringSoon: number;
  rotatedCertificates: number;
  byType: Record<string, number>;
  byStatus: Record<string, number>;
  byTenant: Record<string, number>;
}