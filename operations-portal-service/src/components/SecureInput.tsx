/**
 * Secure Input Component
 * 
 * A secure input component that automatically sanitizes user input
 * and provides protection against XSS and injection attacks.
 */

import React, { useState, useEffect, useRef } from 'react';
import { TextField, TextFieldProps } from '@mui/material';
import { useSecurity } from './SecurityProvider';
import { inputSanitizer } from '../utils/inputSanitizer';

interface SecureInputProps extends Omit<TextFieldProps, 'onChange' | 'value'> {
  value: string;
  onChange: (value: string) => void;
  sanitizeType?: 'text' | 'html' | 'url' | 'email' | 'phone' | 'numeric' | 'date' | 'json' | 'search' | 'filename' | 'sql' | 'xss';
  validate?: (value: string) => boolean;
  onValidationError?: (error: string) => void;
  maxLength?: number;
  allowEmpty?: boolean;
}

export const SecureInput: React.FC<SecureInputProps> = ({
  value,
  onChange,
  sanitizeType = 'text',
  validate,
  onValidationError,
  maxLength = 1000,
  allowEmpty = true,
  ...textFieldProps
}) => {
  const security = useSecurity();
  const [internalValue, setInternalValue] = useState(value);
  const [error, setError] = useState<string | null>(null);
  const [isValidating, setIsValidating] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  
  // Update internal value when prop value changes
  useEffect(() => {
    setInternalValue(value);
  }, [value]);
  
  // Sanitize input on change
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const rawValue = event.target.value;
    
    // Check length limit
    if (rawValue.length > maxLength) {
      setError(`Input must be less than ${maxLength} characters`);
      return;
    }
    
    // Sanitize input
    const sanitizedValue = security.sanitizeInput(rawValue, sanitizeType);
    
    // Check if sanitization changed the value
    if (sanitizedValue !== rawValue) {
      setError('Invalid characters detected and removed');
      setTimeout(() => setError(null), 3000);
    }
    
    // Update internal value
    setInternalValue(sanitizedValue);
    
    // Validate if validation function provided
    if (validate) {
      setIsValidating(true);
      try {
        const isValid = validate(sanitizedValue);
        if (!isValid) {
          setError('Invalid input format');
          onValidationError?.('Invalid input format');
        } else {
          setError(null);
        }
      } catch (validationError) {
        setError('Validation failed');
        onValidationError?.((validationError as Error).message);
      } finally {
        setIsValidating(false);
      }
    }
    
    // Call onChange with sanitized value
    onChange(sanitizedValue);
  };
  
  // Handle blur event
  const handleBlur = () => {
    // Final sanitization on blur
    const finalValue = security.sanitizeInput(internalValue, sanitizeType);
    if (finalValue !== internalValue) {
      setInternalValue(finalValue);
      onChange(finalValue);
    }
    
    // Check for empty value if not allowed
    if (!allowEmpty && finalValue.trim() === '') {
      setError('This field is required');
    }
  };
  
  // Handle focus event
  const handleFocus = () => {
    setError(null);
  };
  
  // Log security events
  useEffect(() => {
    if (error) {
      security.logSecurityEvent('input_validation_error', {
        field: textFieldProps.name || 'unknown',
        error,
        value: internalValue.substring(0, 100) // Log first 100 chars only
      });
    }
  }, [error, internalValue, security, textFieldProps.name]);
  
  return (
    <TextField
      {...textFieldProps}
      ref={inputRef}
      value={internalValue}
      onChange={handleChange}
      onBlur={handleBlur}
      onFocus={handleFocus}
      error={!!error}
      helperText={error || textFieldProps.helperText}
      inputProps={{
        maxLength,
        ...textFieldProps.inputProps
      }}
      InputProps={{
        ...textFieldProps.InputProps,
        endAdornment: isValidating ? (
          <div style={{ color: '#666', fontSize: '12px' }}>Validating...</div>
        ) : textFieldProps.InputProps?.endAdornment
      }}
    />
  );
};

// Secure Text Area Component
interface SecureTextAreaProps extends Omit<TextFieldProps, 'onChange' | 'value' | 'multiline'> {
  value: string;
  onChange: (value: string) => void;
  sanitizeType?: 'text' | 'html' | 'url' | 'email' | 'phone' | 'numeric' | 'date' | 'json' | 'search' | 'filename' | 'sql' | 'xss';
  validate?: (value: string) => boolean;
  onValidationError?: (error: string) => void;
  maxLength?: number;
  allowEmpty?: boolean;
  rows?: number;
}

export const SecureTextArea: React.FC<SecureTextAreaProps> = ({
  value,
  onChange,
  sanitizeType = 'text',
  validate,
  onValidationError,
  maxLength = 5000,
  allowEmpty = true,
  rows = 4,
  ...textFieldProps
}) => {
  const security = useSecurity();
  const [internalValue, setInternalValue] = useState(value);
  const [error, setError] = useState<string | null>(null);
  const [isValidating, setIsValidating] = useState(false);
  
  // Update internal value when prop value changes
  useEffect(() => {
    setInternalValue(value);
  }, [value]);
  
  // Sanitize input on change
  const handleChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    const rawValue = event.target.value;
    
    // Check length limit
    if (rawValue.length > maxLength) {
      setError(`Input must be less than ${maxLength} characters`);
      return;
    }
    
    // Sanitize input
    const sanitizedValue = security.sanitizeInput(rawValue, sanitizeType);
    
    // Check if sanitization changed the value
    if (sanitizedValue !== rawValue) {
      setError('Invalid characters detected and removed');
      setTimeout(() => setError(null), 3000);
    }
    
    // Update internal value
    setInternalValue(sanitizedValue);
    
    // Validate if validation function provided
    if (validate) {
      setIsValidating(true);
      try {
        const isValid = validate(sanitizedValue);
        if (!isValid) {
          setError('Invalid input format');
          onValidationError?.('Invalid input format');
        } else {
          setError(null);
        }
      } catch (validationError) {
        setError('Validation failed');
        onValidationError?.((validationError as Error).message);
      } finally {
        setIsValidating(false);
      }
    }
    
    // Call onChange with sanitized value
    onChange(sanitizedValue);
  };
  
  // Handle blur event
  const handleBlur = () => {
    // Final sanitization on blur
    const finalValue = security.sanitizeInput(internalValue, sanitizeType);
    if (finalValue !== internalValue) {
      setInternalValue(finalValue);
      onChange(finalValue);
    }
    
    // Check for empty value if not allowed
    if (!allowEmpty && finalValue.trim() === '') {
      setError('This field is required');
    }
  };
  
  // Handle focus event
  const handleFocus = () => {
    setError(null);
  };
  
  // Log security events
  useEffect(() => {
    if (error) {
      security.logSecurityEvent('input_validation_error', {
        field: textFieldProps.name || 'unknown',
        error,
        value: internalValue.substring(0, 100) // Log first 100 chars only
      });
    }
  }, [error, internalValue, security, textFieldProps.name]);
  
  return (
    <TextField
      {...textFieldProps}
      value={internalValue}
      onChange={handleChange}
      onBlur={handleBlur}
      onFocus={handleFocus}
      error={!!error}
      helperText={error || textFieldProps.helperText}
      multiline
      rows={rows}
      inputProps={{
        maxLength,
        ...textFieldProps.inputProps
      }}
      InputProps={{
        ...textFieldProps.InputProps,
        endAdornment: isValidating ? (
          <div style={{ color: '#666', fontSize: '12px' }}>Validating...</div>
        ) : textFieldProps.InputProps?.endAdornment
      }}
    />
  );
};

// Secure Password Input Component
interface SecurePasswordInputProps extends Omit<TextFieldProps, 'onChange' | 'value' | 'type'> {
  value: string;
  onChange: (value: string) => void;
  validate?: (value: string) => boolean;
  onValidationError?: (error: string) => void;
  showStrength?: boolean;
  minLength?: number;
  maxLength?: number;
}

export const SecurePasswordInput: React.FC<SecurePasswordInputProps> = ({
  value,
  onChange,
  validate,
  onValidationError,
  showStrength = true,
  minLength = 8,
  maxLength = 128,
  ...textFieldProps
}) => {
  const security = useSecurity();
  const [internalValue, setInternalValue] = useState(value);
  const [error, setError] = useState<string | null>(null);
  const [isValidating, setIsValidating] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  
  // Update internal value when prop value changes
  useEffect(() => {
    setInternalValue(value);
  }, [value]);
  
  // Calculate password strength
  const calculateStrength = (password: string): { score: number; label: string; color: string } => {
    let score = 0;
    
    if (password.length >= minLength) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[a-z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) score++;
    if (password.length >= 12) score++;
    
    if (score <= 2) return { score, label: 'Weak', color: '#f44336' };
    if (score <= 4) return { score, label: 'Medium', color: '#ff9800' };
    return { score, label: 'Strong', color: '#4caf50' };
  };
  
  const strength = calculateStrength(internalValue);
  
  // Sanitize input on change
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const rawValue = event.target.value;
    
    // Check length limits
    if (rawValue.length > maxLength) {
      setError(`Password must be less than ${maxLength} characters`);
      return;
    }
    
    if (rawValue.length < minLength && rawValue.length > 0) {
      setError(`Password must be at least ${minLength} characters`);
      return;
    }
    
    // Sanitize input (passwords need minimal sanitization)
    const sanitizedValue = security.sanitizeInput(rawValue, 'text');
    
    // Update internal value
    setInternalValue(sanitizedValue);
    
    // Validate if validation function provided
    if (validate) {
      setIsValidating(true);
      try {
        const isValid = validate(sanitizedValue);
        if (!isValid) {
          setError('Invalid password format');
          onValidationError?.('Invalid password format');
        } else {
          setError(null);
        }
      } catch (validationError) {
        setError('Validation failed');
        onValidationError?.((validationError as Error).message);
      } finally {
        setIsValidating(false);
      }
    }
    
    // Call onChange with sanitized value
    onChange(sanitizedValue);
  };
  
  // Handle blur event
  const handleBlur = () => {
    // Final sanitization on blur
    const finalValue = security.sanitizeInput(internalValue, 'text');
    if (finalValue !== internalValue) {
      setInternalValue(finalValue);
      onChange(finalValue);
    }
  };
  
  // Handle focus event
  const handleFocus = () => {
    setError(null);
  };
  
  // Log security events
  useEffect(() => {
    if (error) {
      security.logSecurityEvent('password_validation_error', {
        field: textFieldProps.name || 'unknown',
        error,
        strength: strength.label
      });
    }
  }, [error, internalValue, security, textFieldProps.name, strength.label]);
  
  return (
    <TextField
      {...textFieldProps}
      value={internalValue}
      onChange={handleChange}
      onBlur={handleBlur}
      onFocus={handleFocus}
      error={!!error}
      helperText={
        error || 
        (showStrength && internalValue.length > 0 ? (
          <div style={{ color: strength.color, fontSize: '12px' }}>
            Password strength: {strength.label}
          </div>
        ) : textFieldProps.helperText)
      }
      type={showPassword ? 'text' : 'password'}
      inputProps={{
        maxLength,
        minLength,
        ...textFieldProps.inputProps
      }}
      InputProps={{
        ...textFieldProps.InputProps,
        endAdornment: (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            {isValidating && (
              <div style={{ color: '#666', fontSize: '12px' }}>Validating...</div>
            )}
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              style={{
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                color: '#666',
                fontSize: '12px'
              }}
            >
              {showPassword ? 'Hide' : 'Show'}
            </button>
          </div>
        )
      }}
    />
  );
};
