/**
 * Formatters Utility
 * 
 * Centralized formatting functions for dates, currency, numbers, and other data types.
 * Provides consistent formatting across the application.
 */

/**
 * Format a date string or Date object to a readable format
 */
export const formatDate = (
  date: string | Date | null | undefined,
  options: Intl.DateTimeFormatOptions = {}
): string => {
  if (!date) return 'N/A';
  
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  
  if (isNaN(dateObj.getTime())) return 'Invalid Date';
  
  const defaultOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    ...options,
  };
  
  return dateObj.toLocaleDateString('en-US', defaultOptions);
};

/**
 * Format a date to a relative time (e.g., "2 hours ago", "3 days ago")
 */
export const formatRelativeTime = (date: string | Date | null | undefined): string => {
  if (!date) return 'N/A';
  
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  
  if (isNaN(dateObj.getTime())) return 'Invalid Date';
  
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - dateObj.getTime()) / 1000);
  
  if (diffInSeconds < 60) return 'Just now';
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} minutes ago`;
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hours ago`;
  if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)} days ago`;
  if (diffInSeconds < 31536000) return `${Math.floor(diffInSeconds / 2592000)} months ago`;
  
  return `${Math.floor(diffInSeconds / 31536000)} years ago`;
};

/**
 * Format currency amount
 */
export const formatCurrency = (
  amount: number | string | null | undefined,
  currency: string = 'USD',
  locale: string = 'en-US'
): string => {
  if (amount === null || amount === undefined || amount === '') return 'N/A';
  
  const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount;
  
  if (isNaN(numAmount)) return 'Invalid Amount';
  
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(numAmount);
};

/**
 * Format number with thousand separators
 */
export const formatNumber = (
  value: number | string | null | undefined,
  options: Intl.NumberFormatOptions = {}
): string => {
  if (value === null || value === undefined || value === '') return 'N/A';
  
  const numValue = typeof value === 'string' ? parseFloat(value) : value;
  
  if (isNaN(numValue)) return 'Invalid Number';
  
  const defaultOptions: Intl.NumberFormatOptions = {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
    ...options,
  };
  
  return new Intl.NumberFormat('en-US', defaultOptions).format(numValue);
};

/**
 * Format percentage
 */
export const formatPercentage = (
  value: number | string | null | undefined,
  decimals: number = 1
): string => {
  if (value === null || value === undefined || value === '') return 'N/A';
  
  const numValue = typeof value === 'string' ? parseFloat(value) : value;
  
  if (isNaN(numValue)) return 'Invalid Percentage';
  
  return `${numValue.toFixed(decimals)}%`;
};

/**
 * Format file size in human-readable format
 */
export const formatFileSize = (bytes: number | null | undefined): string => {
  if (bytes === null || bytes === undefined) return 'N/A';
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
};

/**
 * Format duration in human-readable format
 */
export const formatDuration = (seconds: number | null | undefined): string => {
  if (seconds === null || seconds === undefined) return 'N/A';
  if (seconds === 0) return '0s';
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = Math.floor(seconds % 60);
  
  const parts = [];
  if (hours > 0) parts.push(`${hours}h`);
  if (minutes > 0) parts.push(`${minutes}m`);
  if (secs > 0) parts.push(`${secs}s`);
  
  return parts.join(' ') || '0s';
};

/**
 * Format duration from start and end timestamps
 */
export const formatDurationFromTimestamps = (
  startTime: string | Date | null | undefined,
  endTime?: string | Date | null | undefined
): string => {
  if (!startTime) return 'N/A';
  
  const start = typeof startTime === 'string' ? new Date(startTime) : startTime;
  const end = endTime ? (typeof endTime === 'string' ? new Date(endTime) : endTime) : new Date();
  
  if (isNaN(start.getTime()) || isNaN(end.getTime())) return 'Invalid Time';
  
  const durationInSeconds = Math.floor((end.getTime() - start.getTime()) / 1000);
  return formatDuration(durationInSeconds);
};

/**
 * Format phone number
 */
export const formatPhoneNumber = (phone: string | null | undefined): string => {
  if (!phone) return 'N/A';
  
  // Remove all non-digit characters
  const cleaned = phone.replace(/\D/g, '');
  
  // Format as (XXX) XXX-XXXX for US numbers
  if (cleaned.length === 10) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6)}`;
  }
  
  // Return original if not a standard US number
  return phone;
};

/**
 * Format credit card number (masked)
 */
export const formatCreditCard = (cardNumber: string | null | undefined): string => {
  if (!cardNumber) return 'N/A';
  
  const cleaned = cardNumber.replace(/\D/g, '');
  
  if (cleaned.length < 4) return cardNumber;
  
  // Show last 4 digits, mask the rest
  const lastFour = cleaned.slice(-4);
  const masked = '*'.repeat(cleaned.length - 4);
  
  return `${masked}${lastFour}`;
};

/**
 * Format account number (masked)
 */
export const formatAccountNumber = (accountNumber: string | null | undefined): string => {
  if (!accountNumber) return 'N/A';
  
  const cleaned = accountNumber.replace(/\D/g, '');
  
  if (cleaned.length < 4) return accountNumber;
  
  // Show last 4 digits, mask the rest
  const lastFour = cleaned.slice(-4);
  const masked = '*'.repeat(Math.max(0, cleaned.length - 4));
  
  return `${masked}${lastFour}`;
};

/**
 * Format status with appropriate casing
 */
export const formatStatus = (status: string | null | undefined): string => {
  if (!status) return 'N/A';
  
  return status
    .toLowerCase()
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

/**
 * Format ID with truncation
 */
export const formatId = (id: string | null | undefined, maxLength: number = 8): string => {
  if (!id) return 'N/A';
  
  if (id.length <= maxLength) return id;
  
  return `${id.slice(0, maxLength)}...`;
};

/**
 * Format JSON for display
 */
export const formatJson = (obj: any, indent: number = 2): string => {
  try {
    return JSON.stringify(obj, null, indent);
  } catch (error) {
    return 'Invalid JSON';
  }
};

/**
 * Format error message for display
 */
export const formatErrorMessage = (error: any): string => {
  if (typeof error === 'string') return error;
  if (error?.message) return error.message;
  if (error?.error) return error.error;
  if (error?.detail) return error.detail;
  
  return 'An unknown error occurred';
};

/**
 * Format array as comma-separated string
 */
export const formatArray = (arr: any[] | null | undefined, maxItems: number = 5): string => {
  if (!arr || arr.length === 0) return 'N/A';
  
  if (arr.length <= maxItems) {
    return arr.join(', ');
  }
  
  const visible = arr.slice(0, maxItems);
  const remaining = arr.length - maxItems;
  
  return `${visible.join(', ')} and ${remaining} more`;
};

/**
 * Format boolean as Yes/No
 */
export const formatBoolean = (value: boolean | null | undefined): string => {
  if (value === null || value === undefined) return 'N/A';
  return value ? 'Yes' : 'No';
};

/**
 * Format enum value with proper casing
 */
export const formatEnum = (value: string | null | undefined): string => {
  if (!value) return 'N/A';
  
  return value
    .toLowerCase()
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

/**
 * Format memory usage
 */
export const formatMemoryUsage = (bytes: number | null | undefined): string => {
  if (bytes === null || bytes === undefined) return 'N/A';
  
  const mb = bytes / (1024 * 1024);
  
  if (mb < 1) return `${(bytes / 1024).toFixed(1)} KB`;
  if (mb < 1024) return `${mb.toFixed(1)} MB`;
  
  return `${(mb / 1024).toFixed(1)} GB`;
};

/**
 * Format uptime in human-readable format
 */
export const formatUptime = (uptime: number | null | undefined): string => {
  if (uptime === null || uptime === undefined) return 'N/A';
  
  const days = Math.floor(uptime / 86400);
  const hours = Math.floor((uptime % 86400) / 3600);
  const minutes = Math.floor((uptime % 3600) / 60);
  
  const parts = [];
  if (days > 0) parts.push(`${days}d`);
  if (hours > 0) parts.push(`${hours}h`);
  if (minutes > 0) parts.push(`${minutes}m`);
  
  return parts.join(' ') || '0m';
};
