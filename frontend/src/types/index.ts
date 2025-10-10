// Type definitions for the Payment Engine frontend

export interface Account {
  id: string;
  accountNumber: string;
  customerId: string;
  accountTypeId: string;
  currencyCode: string;
  balance: number;
  availableBalance: number;
  status: AccountStatus;
  openedDate: string;
  closedDate?: string;
  createdAt: string;
  updatedAt: string;
}

export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  CLOSED = 'CLOSED'
}

export interface Transaction {
  id: string;
  transactionReference: string;
  externalReference?: string;
  fromAccountId?: string;
  toAccountId?: string;
  paymentTypeId: string;
  amount: number;
  currencyCode: string;
  feeAmount: number;
  status: TransactionStatus;
  transactionType: TransactionType;
  description?: string;
  metadata?: Record<string, any>;
  initiatedAt: string;
  processedAt?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  REVERSED = 'REVERSED'
}

export enum TransactionType {
  DEBIT = 'DEBIT',
  CREDIT = 'CREDIT',
  TRANSFER = 'TRANSFER',
  PAYMENT = 'PAYMENT',
  REFUND = 'REFUND'
}

export interface PaymentType {
  id: string;
  code: string;
  name: string;
  description?: string;
  isSynchronous: boolean;
  maxAmount?: number;
  minAmount: number;
  processingFee: number;
  isActive: boolean;
  configuration?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface Customer {
  id: string;
  customerNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  dateOfBirth?: string;
  status: CustomerStatus;
  kycStatus: KycStatus;
  createdAt: string;
  updatedAt: string;
}

export enum CustomerStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  CLOSED = 'CLOSED'
}

export enum KycStatus {
  PENDING = 'PENDING',
  VERIFIED = 'VERIFIED',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED'
}

export interface CreateTransactionRequest {
  externalReference?: string;
  fromAccountId?: string;
  toAccountId?: string;
  paymentTypeId: string;
  amount: number;
  currencyCode?: string;
  description?: string;
  metadata?: Record<string, any>;
  channel?: string;
  ipAddress?: string;
  deviceId?: string;
}

export interface TransactionSearchCriteria {
  transactionReference?: string;
  accountId?: string;
  status?: TransactionStatus;
  paymentTypeId?: string;
  minAmount?: number;
  maxAmount?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface AccountSearchCriteria {
  accountNumber?: string;
  customerId?: string;
  status?: AccountStatus;
  minBalance?: number;
  maxBalance?: number;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: number;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface DashboardStats {
  totalTransactions: number;
  totalAmount: number;
  successfulTransactions: number;
  failedTransactions: number;
  pendingTransactions: number;
  averageTransactionAmount: number;
  transactionVolumeToday: number;
  activeAccounts: number;
  totalCustomers: number;
}

export interface TransactionStats {
  totalCount: number;
  totalAmount: number;
  averageAmount: number;
  maxAmount: number;
  minAmount: number;
}

export interface ChartData {
  name: string;
  value: number;
  date?: string;
}

export interface AlertMessage {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
}

export interface User {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
  permissions: string[];
  lastLoginAt?: string;
  isActive: boolean;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  loading: boolean;
  error: string | null;
}

export interface AppState {
  auth: AuthState;
  transactions: {
    items: Transaction[];
    loading: boolean;
    error: string | null;
    totalPages: number;
    currentPage: number;
  };
  accounts: {
    items: Account[];
    loading: boolean;
    error: string | null;
    totalPages: number;
    currentPage: number;
  };
  paymentTypes: {
    items: PaymentType[];
    loading: boolean;
    error: string | null;
  };
  dashboard: {
    stats: DashboardStats | null;
    loading: boolean;
    error: string | null;
  };
  ui: {
    sidebarOpen: boolean;
    theme: 'light' | 'dark';
    alerts: AlertMessage[];
  };
}

export interface ApiError {
  message: string;
  status: number;
  code?: string;
  details?: Record<string, any>;
}

export interface LoadingState {
  [key: string]: boolean;
}

export interface FormErrors {
  [key: string]: string | undefined;
}

// Navigation and routing types
export interface NavItem {
  id: string;
  label: string;
  path: string;
  icon: string;
  children?: NavItem[];
  permissions?: string[];
}

export interface BreadcrumbItem {
  label: string;
  path?: string;
}

// Table and grid types
export interface TableColumn<T = any> {
  id: string;
  label: string;
  field: keyof T;
  sortable?: boolean;
  filterable?: boolean;
  width?: number;
  align?: 'left' | 'center' | 'right';
  format?: (value: any) => string;
  render?: (value: any, row: T) => React.ReactNode;
}

export interface TableProps<T> {
  data: T[];
  columns: TableColumn<T>[];
  loading?: boolean;
  pagination?: boolean;
  pageSize?: number;
  totalCount?: number;
  onPageChange?: (page: number) => void;
  onSortChange?: (field: string, direction: 'asc' | 'desc') => void;
  onRowClick?: (row: T) => void;
  selectedRows?: string[];
  onSelectionChange?: (selectedIds: string[]) => void;
}

// Filter and search types
export interface FilterOption {
  label: string;
  value: string | number;
}

export interface DateRange {
  startDate: Date | null;
  endDate: Date | null;
}

export interface SearchFilters {
  [key: string]: string | number | boolean | Date | null | undefined;
}