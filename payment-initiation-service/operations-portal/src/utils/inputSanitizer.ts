/**
 * Input Sanitizer
 * 
 * Comprehensive input sanitization utilities for all user inputs
 * to prevent XSS, injection attacks, and data corruption.
 */

import { escapeHtml, sanitizeInput } from './security';

// Form Input Sanitization
export const sanitizeFormInput = (input: any): any => {
  if (input === null || input === undefined) {
    return input;
  }
  
  if (typeof input === 'string') {
    return sanitizeInput(input);
  }
  
  if (typeof input === 'number') {
    return isNaN(input) ? 0 : input;
  }
  
  if (typeof input === 'boolean') {
    return Boolean(input);
  }
  
  if (Array.isArray(input)) {
    return input.map(item => sanitizeFormInput(item));
  }
  
  if (typeof input === 'object') {
    const sanitized: Record<string, any> = {};
    for (const [key, value] of Object.entries(input)) {
      sanitized[sanitizeInput(key)] = sanitizeFormInput(value);
    }
    return sanitized;
  }
  
  return input;
};

// Text Input Sanitization
export const sanitizeTextInput = (text: string): string => {
  if (typeof text !== 'string') {
    return '';
  }
  
  return text
    .replace(/[<>]/g, '') // Remove HTML tags
    .replace(/javascript:/gi, '') // Remove javascript: protocol
    .replace(/vbscript:/gi, '') // Remove vbscript: protocol
    .replace(/data:/gi, '') // Remove data: protocol
    .replace(/on\w+=/gi, '') // Remove event handlers
    .replace(/[<>"'`]/g, '') // Remove dangerous characters
    .replace(/\s+/g, ' ') // Normalize whitespace
    .trim();
};

// HTML Input Sanitization
export const sanitizeHtmlInput = (html: string): string => {
  if (typeof html !== 'string') {
    return '';
  }
  
  // Remove dangerous tags and attributes
  return html
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, '')
    .replace(/<object\b[^<]*(?:(?!<\/object>)<[^<]*)*<\/object>/gi, '')
    .replace(/<embed\b[^<]*(?:(?!<\/embed>)<[^<]*)*<\/embed>/gi, '')
    .replace(/<link\b[^<]*(?:(?!<\/link>)<[^<]*)*<\/link>/gi, '')
    .replace(/<meta\b[^<]*(?:(?!<\/meta>)<[^<]*)*<\/meta>/gi, '')
    .replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '')
    .replace(/on\w+\s*=\s*["'][^"']*["']/gi, '')
    .replace(/javascript\s*:/gi, '')
    .replace(/vbscript\s*:/gi, '')
    .replace(/data\s*:/gi, '')
    .replace(/expression\s*\(/gi, '')
    .replace(/url\s*\(/gi, '');
};

// URL Input Sanitization
export const sanitizeUrlInput = (url: string): string => {
  if (typeof url !== 'string') {
    return '';
  }
  
  try {
    const parsedUrl = new URL(url);
    
    // Only allow http and https protocols
    if (!['http:', 'https:'].includes(parsedUrl.protocol)) {
      return '';
    }
    
    // Remove dangerous characters from pathname
    const sanitizedPathname = parsedUrl.pathname
      .replace(/[<>]/g, '')
      .replace(/javascript:/gi, '')
      .replace(/vbscript:/gi, '')
      .replace(/data:/gi, '');
    
    return `${parsedUrl.protocol}//${parsedUrl.host}${sanitizedPathname}${parsedUrl.search}${parsedUrl.hash}`;
  } catch {
    return '';
  }
};

// Email Input Sanitization
export const sanitizeEmailInput = (email: string): string => {
  if (typeof email !== 'string') {
    return '';
  }
  
  return email
    .toLowerCase()
    .replace(/[<>]/g, '')
    .replace(/javascript:/gi, '')
    .replace(/vbscript:/gi, '')
    .replace(/data:/gi, '')
    .replace(/on\w+=/gi, '')
    .trim();
};

// Phone Number Sanitization
export const sanitizePhoneInput = (phone: string): string => {
  if (typeof phone !== 'string') {
    return '';
  }
  
  return phone
    .replace(/[^\d\+\-\(\)\s]/g, '') // Keep only digits, +, -, (, ), and spaces
    .replace(/\s+/g, ' ') // Normalize spaces
    .trim();
};

// Numeric Input Sanitization
export const sanitizeNumericInput = (value: any): number => {
  if (typeof value === 'number') {
    return isNaN(value) ? 0 : value;
  }
  
  if (typeof value === 'string') {
    const numeric = parseFloat(value);
    return isNaN(numeric) ? 0 : numeric;
  }
  
  return 0;
};

// Date Input Sanitization
export const sanitizeDateInput = (date: any): string => {
  if (typeof date !== 'string') {
    return '';
  }
  
  // Remove any non-date characters
  const sanitized = date.replace(/[^0-9\-:TZ]/g, '');
  
  // Validate ISO date format
  const dateObj = new Date(sanitized);
  if (isNaN(dateObj.getTime())) {
    return '';
  }
  
  return dateObj.toISOString();
};

// JSON Input Sanitization
export const sanitizeJsonInput = (json: string): any => {
  if (typeof json !== 'string') {
    return null;
  }
  
  try {
    const parsed = JSON.parse(json);
    return sanitizeFormInput(parsed);
  } catch {
    return null;
  }
};

// Search Query Sanitization
export const sanitizeSearchQuery = (query: string): string => {
  if (typeof query !== 'string') {
    return '';
  }
  
  return query
    .replace(/[<>]/g, '') // Remove HTML tags
    .replace(/javascript:/gi, '') // Remove javascript: protocol
    .replace(/vbscript:/gi, '') // Remove vbscript: protocol
    .replace(/data:/gi, '') // Remove data: protocol
    .replace(/on\w+=/gi, '') // Remove event handlers
    .replace(/[<>"'`]/g, '') // Remove dangerous characters
    .replace(/\s+/g, ' ') // Normalize whitespace
    .trim()
    .substring(0, 1000); // Limit length
};

// File Name Sanitization
export const sanitizeFileName = (fileName: string): string => {
  if (typeof fileName !== 'string') {
    return '';
  }
  
  return fileName
    .replace(/[<>:"/\\|?*]/g, '') // Remove invalid characters
    .replace(/\.\./g, '') // Remove parent directory references
    .replace(/^\./, '') // Remove leading dots
    .trim()
    .substring(0, 255); // Limit length
};

// SQL Injection Prevention
export const sanitizeSqlInput = (input: string): string => {
  if (typeof input !== 'string') {
    return '';
  }
  
  return input
    .replace(/['"]/g, '') // Remove quotes
    .replace(/;/g, '') // Remove semicolons
    .replace(/--/g, '') // Remove SQL comments
    .replace(/\/\*/g, '') // Remove block comments
    .replace(/\*\//g, '') // Remove block comments
    .replace(/union/gi, '') // Remove UNION
    .replace(/select/gi, '') // Remove SELECT
    .replace(/insert/gi, '') // Remove INSERT
    .replace(/update/gi, '') // Remove UPDATE
    .replace(/delete/gi, '') // Remove DELETE
    .replace(/drop/gi, '') // Remove DROP
    .replace(/create/gi, '') // Remove CREATE
    .replace(/alter/gi, '') // Remove ALTER
    .replace(/exec/gi, '') // Remove EXEC
    .replace(/execute/gi, '') // Remove EXECUTE
    .replace(/script/gi, '') // Remove SCRIPT
    .trim();
};

// XSS Prevention
export const preventXSS = (input: string): string => {
  if (typeof input !== 'string') {
    return '';
  }
  
  return input
    .replace(/<script/gi, '&lt;script')
    .replace(/<\/script>/gi, '&lt;/script&gt;')
    .replace(/<iframe/gi, '&lt;iframe')
    .replace(/<\/iframe>/gi, '&lt;/iframe&gt;')
    .replace(/<object/gi, '&lt;object')
    .replace(/<\/object>/gi, '&lt;/object&gt;')
    .replace(/<embed/gi, '&lt;embed')
    .replace(/<\/embed>/gi, '&lt;/embed&gt;')
    .replace(/javascript:/gi, '')
    .replace(/vbscript:/gi, '')
    .replace(/data:/gi, '')
    .replace(/on\w+=/gi, '');
};

// Comprehensive Input Sanitizer
export class InputSanitizer {
  private static instance: InputSanitizer;
  
  static getInstance(): InputSanitizer {
    if (!InputSanitizer.instance) {
      InputSanitizer.instance = new InputSanitizer();
    }
    return InputSanitizer.instance;
  }
  
  sanitize(input: any, type: 'text' | 'html' | 'url' | 'email' | 'phone' | 'numeric' | 'date' | 'json' | 'search' | 'filename' | 'sql' | 'xss'): any {
    if (input === null || input === undefined) {
      return input;
    }
    
    switch (type) {
      case 'text':
        return sanitizeTextInput(input);
      case 'html':
        return sanitizeHtmlInput(input);
      case 'url':
        return sanitizeUrlInput(input);
      case 'email':
        return sanitizeEmailInput(input);
      case 'phone':
        return sanitizePhoneInput(input);
      case 'numeric':
        return sanitizeNumericInput(input);
      case 'date':
        return sanitizeDateInput(input);
      case 'json':
        return sanitizeJsonInput(input);
      case 'search':
        return sanitizeSearchQuery(input);
      case 'filename':
        return sanitizeFileName(input);
      case 'sql':
        return sanitizeSqlInput(input);
      case 'xss':
        return preventXSS(input);
      default:
        return sanitizeFormInput(input);
    }
  }
  
  sanitizeObject(obj: Record<string, any>, rules: Record<string, string>): Record<string, any> {
    const sanitized: Record<string, any> = {};
    
    for (const [key, value] of Object.entries(obj)) {
      const sanitizedKey = sanitizeTextInput(key);
      const rule = rules[key] || 'text';
      sanitized[sanitizedKey] = this.sanitize(value, rule as any);
    }
    
    return sanitized;
  }
}

// Export singleton instance
export const inputSanitizer = InputSanitizer.getInstance();
