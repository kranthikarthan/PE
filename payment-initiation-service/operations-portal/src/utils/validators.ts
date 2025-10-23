/**
 * Validators Utility
 * 
 * Centralized validation functions for form inputs and data validation.
 * Provides consistent validation rules across the application.
 */

/**
 * Email validation
 */
export const isValidEmail = (email: string | null | undefined): boolean => {
  if (!email) return false;
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Phone number validation (US format)
 */
export const isValidPhoneNumber = (phone: string | null | undefined): boolean => {
  if (!phone) return false;
  
  const phoneRegex = /^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$/;
  return phoneRegex.test(phone);
};

/**
 * URL validation
 */
export const isValidUrl = (url: string | null | undefined): boolean => {
  if (!url) return false;
  
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

/**
 * Credit card number validation (Luhn algorithm)
 */
export const isValidCreditCard = (cardNumber: string | null | undefined): boolean => {
  if (!cardNumber) return false;
  
  const cleaned = cardNumber.replace(/\D/g, '');
  
  if (cleaned.length < 13 || cleaned.length > 19) return false;
  
  // Luhn algorithm
  let sum = 0;
  let isEven = false;
  
  for (let i = cleaned.length - 1; i >= 0; i--) {
    let digit = parseInt(cleaned[i]);
    
    if (isEven) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }
    
    sum += digit;
    isEven = !isEven;
  }
  
  return sum % 10 === 0;
};

/**
 * Credit card type detection
 */
export const getCreditCardType = (cardNumber: string | null | undefined): string => {
  if (!cardNumber) return 'unknown';
  
  const cleaned = cardNumber.replace(/\D/g, '');
  
  if (/^4/.test(cleaned)) return 'visa';
  if (/^5[1-5]/.test(cleaned)) return 'mastercard';
  if (/^3[47]/.test(cleaned)) return 'amex';
  if (/^6(?:011|5)/.test(cleaned)) return 'discover';
  
  return 'unknown';
};

/**
 * Password strength validation
 */
export const validatePasswordStrength = (password: string | null | undefined): {
  isValid: boolean;
  score: number;
  feedback: string[];
} => {
  if (!password) {
    return {
      isValid: false,
      score: 0,
      feedback: ['Password is required'],
    };
  }
  
  const feedback: string[] = [];
  let score = 0;
  
  // Length check
  if (password.length < 8) {
    feedback.push('Password must be at least 8 characters long');
  } else {
    score += 1;
  }
  
  // Uppercase check
  if (!/[A-Z]/.test(password)) {
    feedback.push('Password must contain at least one uppercase letter');
  } else {
    score += 1;
  }
  
  // Lowercase check
  if (!/[a-z]/.test(password)) {
    feedback.push('Password must contain at least one lowercase letter');
  } else {
    score += 1;
  }
  
  // Number check
  if (!/\d/.test(password)) {
    feedback.push('Password must contain at least one number');
  } else {
    score += 1;
  }
  
  // Special character check
  if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    feedback.push('Password must contain at least one special character');
  } else {
    score += 1;
  }
  
  return {
    isValid: score >= 4,
    score,
    feedback,
  };
};

/**
 * Required field validation
 */
export const isRequired = (value: any): boolean => {
  if (value === null || value === undefined) return false;
  if (typeof value === 'string') return value.trim().length > 0;
  if (Array.isArray(value)) return value.length > 0;
  return true;
};

/**
 * Minimum length validation
 */
export const hasMinLength = (value: string | null | undefined, minLength: number): boolean => {
  if (!value) return false;
  return value.length >= minLength;
};

/**
 * Maximum length validation
 */
export const hasMaxLength = (value: string | null | undefined, maxLength: number): boolean => {
  if (!value) return true;
  return value.length <= maxLength;
};

/**
 * Numeric validation
 */
export const isNumeric = (value: any): boolean => {
  if (value === null || value === undefined || value === '') return false;
  return !isNaN(Number(value));
};

/**
 * Integer validation
 */
export const isInteger = (value: any): boolean => {
  if (!isNumeric(value)) return false;
  return Number.isInteger(Number(value));
};

/**
 * Positive number validation
 */
export const isPositive = (value: any): boolean => {
  if (!isNumeric(value)) return false;
  return Number(value) > 0;
};

/**
 * Negative number validation
 */
export const isNegative = (value: any): boolean => {
  if (!isNumeric(value)) return false;
  return Number(value) < 0;
};

/**
 * Range validation
 */
export const isInRange = (value: any, min: number, max: number): boolean => {
  if (!isNumeric(value)) return false;
  const numValue = Number(value);
  return numValue >= min && numValue <= max;
};

/**
 * Date validation
 */
export const isValidDate = (date: string | Date | null | undefined): boolean => {
  if (!date) return false;
  
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return !isNaN(dateObj.getTime());
};

/**
 * Future date validation
 */
export const isFutureDate = (date: string | Date | null | undefined): boolean => {
  if (!isValidDate(date)) return false;
  
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return dateObj ? dateObj.getTime() > Date.now() : false;
};

/**
 * Past date validation
 */
export const isPastDate = (date: string | Date | null | undefined): boolean => {
  if (!isValidDate(date)) return false;
  
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return dateObj ? dateObj.getTime() < Date.now() : false;
};

/**
 * Age validation
 */
export const isValidAge = (birthDate: string | Date | null | undefined, minAge: number = 18): boolean => {
  if (!isValidDate(birthDate)) return false;
  
  const birth = typeof birthDate === 'string' ? new Date(birthDate) : birthDate;
  if (!birth) return false;
  
  const today = new Date();
  const age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    return age - 1 >= minAge;
  }
  
  return age >= minAge;
};

/**
 * Currency amount validation
 */
export const isValidCurrencyAmount = (amount: any, minAmount: number = 0, maxAmount?: number): boolean => {
  if (!isNumeric(amount)) return false;
  
  const numAmount = Number(amount);
  
  if (numAmount < minAmount) return false;
  if (maxAmount !== undefined && numAmount > maxAmount) return false;
  
  return true;
};

/**
 * Account number validation
 */
export const isValidAccountNumber = (accountNumber: string | null | undefined): boolean => {
  if (!accountNumber) return false;
  
  // Remove spaces and hyphens
  const cleaned = accountNumber.replace(/[\s-]/g, '');
  
  // Check if it's numeric and has reasonable length
  return /^\d{8,20}$/.test(cleaned);
};

/**
 * Routing number validation (US)
 */
export const isValidRoutingNumber = (routingNumber: string | null | undefined): boolean => {
  if (!routingNumber) return false;
  
  const cleaned = routingNumber.replace(/\D/g, '');
  
  if (cleaned.length !== 9) return false;
  
  // Check digit validation
  const digits = cleaned.split('').map(Number);
  const weights = [3, 7, 1, 3, 7, 1, 3, 7, 1];
  
  let sum = 0;
  for (let i = 0; i < 9; i++) {
    sum += digits[i] * weights[i];
  }
  
  return sum % 10 === 0;
};

/**
 * IBAN validation
 */
export const isValidIBAN = (iban: string | null | undefined): boolean => {
  if (!iban) return false;
  
  const cleaned = iban.replace(/\s/g, '').toUpperCase();
  
  if (cleaned.length < 15 || cleaned.length > 34) return false;
  
  // Move first 4 characters to end
  const rearranged = cleaned.slice(4) + cleaned.slice(0, 4);
  
  // Convert letters to numbers
  const numeric = rearranged.replace(/[A-Z]/g, (char) => (char.charCodeAt(0) - 55).toString());
  
  // Calculate mod 97
  let remainder = 0;
  for (let i = 0; i < numeric.length; i++) {
    remainder = (remainder * 10 + parseInt(numeric[i])) % 97;
  }
  
  return remainder === 1;
};

/**
 * SWIFT code validation
 */
export const isValidSwiftCode = (swift: string | null | undefined): boolean => {
  if (!swift) return false;
  
  const cleaned = swift.replace(/\s/g, '').toUpperCase();
  
  // SWIFT codes are 8 or 11 characters
  if (cleaned.length !== 8 && cleaned.length !== 11) return false;
  
  // Format: AAAA BB CC DDD
  const swiftRegex = /^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$/;
  return swiftRegex.test(cleaned);
};

/**
 * JSON validation
 */
export const isValidJson = (jsonString: string | null | undefined): boolean => {
  if (!jsonString) return false;
  
  try {
    JSON.parse(jsonString);
    return true;
  } catch {
    return false;
  }
};

/**
 * UUID validation
 */
export const isValidUUID = (uuid: string | null | undefined): boolean => {
  if (!uuid) return false;
  
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegex.test(uuid);
};

/**
 * Alphanumeric validation
 */
export const isAlphanumeric = (value: string | null | undefined): boolean => {
  if (!value) return false;
  return /^[a-zA-Z0-9]+$/.test(value);
};

/**
 * Alpha validation
 */
export const isAlpha = (value: string | null | undefined): boolean => {
  if (!value) return false;
  return /^[a-zA-Z]+$/.test(value);
};

/**
 * Numeric string validation
 */
export const isNumericString = (value: string | null | undefined): boolean => {
  if (!value) return false;
  return /^\d+$/.test(value);
};

/**
 * Hex color validation
 */
export const isValidHexColor = (color: string | null | undefined): boolean => {
  if (!color) return false;
  return /^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/.test(color);
};

/**
 * IP address validation
 */
export const isValidIPAddress = (ip: string | null | undefined): boolean => {
  if (!ip) return false;
  
  const ipv4Regex = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
  const ipv6Regex = /^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$/;
  
  return ipv4Regex.test(ip) || ipv6Regex.test(ip);
};

/**
 * Domain validation
 */
export const isValidDomain = (domain: string | null | undefined): boolean => {
  if (!domain) return false;
  
  const domainRegex = /^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
  return domainRegex.test(domain);
};

/**
 * Form validation helper
 */
export const validateForm = (data: Record<string, any>, rules: Record<string, any>): {
  isValid: boolean;
  errors: Record<string, string>;
} => {
  const errors: Record<string, string> = {};
  let isValid = true;
  
  for (const [field, value] of Object.entries(data)) {
    const fieldRules = rules[field];
    if (!fieldRules) continue;
    
    for (const [rule, ruleValue] of Object.entries(fieldRules)) {
      let fieldValid = true;
      let errorMessage = '';
      
      switch (rule) {
        case 'required':
          if (!isRequired(value)) {
            fieldValid = false;
            errorMessage = `${field} is required`;
          }
          break;
        case 'email':
          if (value && !isValidEmail(value)) {
            fieldValid = false;
            errorMessage = `${field} must be a valid email`;
          }
          break;
        case 'minLength':
          if (value && typeof ruleValue === 'number' && !hasMinLength(value, ruleValue)) {
            fieldValid = false;
            errorMessage = `${field} must be at least ${ruleValue} characters`;
          }
          break;
        case 'maxLength':
          if (value && typeof ruleValue === 'number' && !hasMaxLength(value, ruleValue)) {
            fieldValid = false;
            errorMessage = `${field} must be no more than ${ruleValue} characters`;
          }
          break;
        case 'numeric':
          if (value && !isNumeric(value)) {
            fieldValid = false;
            errorMessage = `${field} must be numeric`;
          }
          break;
        case 'positive':
          if (value && !isPositive(value)) {
            fieldValid = false;
            errorMessage = `${field} must be positive`;
          }
          break;
        case 'range':
          if (value && typeof ruleValue === 'object' && ruleValue !== null && 'min' in ruleValue && 'max' in ruleValue) {
            const rangeValue = ruleValue as { min: number; max: number };
            if (!isInRange(value, rangeValue.min, rangeValue.max)) {
              fieldValid = false;
              errorMessage = `${field} must be between ${rangeValue.min} and ${rangeValue.max}`;
            }
          }
          break;
      }
      
      if (!fieldValid) {
        errors[field] = errorMessage;
        isValid = false;
        break;
      }
    }
  }
  
  return { isValid, errors };
};
