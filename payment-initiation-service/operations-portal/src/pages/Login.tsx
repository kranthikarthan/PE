/**
 * Login Page
 * 
 * Authentication page for user login with form validation
 * and error handling.
 */

import React, { useState, useEffect } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  InputAdornment,
  IconButton,
} from '@mui/material';
import {
  Visibility,
  VisibilityOff,
  Login as LoginIcon,
} from '@mui/icons-material';
import { useAuth } from '@contexts/AuthContext';
import { useTenant } from '@contexts/TenantContext';
import { useNotification } from '@contexts/NotificationContext';
import { LoginRequest } from '@types/auth';
import { config } from '@config/environment';

interface LoginFormData {
  username: string;
  password: string;
  tenantId: string;
  businessUnitId: string;
}

const Login: React.FC = () => {
  const { isAuthenticated, isLoading, login, error } = useAuth();
  const { tenantId, businessUnitId, setTenant, setBusinessUnit } = useTenant();
  const { showError, showSuccess } = useNotification();
  const navigate = useNavigate();
  const location = useLocation();

  const [formData, setFormData] = useState<LoginFormData>({
    username: '',
    password: '',
    tenantId: tenantId,
    businessUnitId: businessUnitId,
  });
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      const from = location.state?.from?.pathname || '/';
      navigate(from, { replace: true });
    }
  }, [isAuthenticated, navigate, location]);

  // Handle form input changes
  const handleInputChange = (field: keyof LoginFormData) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const value = event.target.value;
    setFormData(prev => ({ ...prev, [field]: value }));
    
    // Clear validation error for this field
    if (validationErrors[field]) {
      setValidationErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  // Handle tenant selection
  const handleTenantChange = (event: any) => {
    const newTenantId = event.target.value;
    setFormData(prev => ({ ...prev, tenantId: newTenantId }));
    setTenant(newTenantId);
  };

  // Handle business unit selection
  const handleBusinessUnitChange = (event: any) => {
    const newBusinessUnitId = event.target.value;
    setFormData(prev => ({ ...prev, businessUnitId: newBusinessUnitId }));
    setBusinessUnit(newBusinessUnitId);
  };

  // Toggle password visibility
  const togglePasswordVisibility = () => {
    setShowPassword(prev => !prev);
  };

  // Validate form
  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.username.trim()) {
      errors.username = 'Username is required';
    } else if (formData.username.length < 3) {
      errors.username = 'Username must be at least 3 characters';
    }

    if (!formData.password) {
      errors.password = 'Password is required';
    } else if (formData.password.length < 8) {
      errors.password = 'Password must be at least 8 characters';
    }

    if (!formData.tenantId) {
      errors.tenantId = 'Tenant is required';
    }

    if (!formData.businessUnitId) {
      errors.businessUnitId = 'Business unit is required';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const loginRequest: LoginRequest = {
        username: formData.username.trim(),
        password: formData.password,
        tenantId: formData.tenantId,
        businessUnitId: formData.businessUnitId,
      };

      await login(loginRequest);
      showSuccess('Login Successful', 'Welcome to the Payments Engine Operations Portal');
      
      // Navigate to the intended page or dashboard
      const from = location.state?.from?.pathname || '/';
      navigate(from, { replace: true });
    } catch (error: any) {
      console.error('Login error:', error);
      showError('Login Failed', error.message || 'Invalid credentials');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Mock tenant data for development
  const mockTenants = [
    {
      id: 'TENANT-001',
      name: 'Primary Bank',
      businessUnits: [
        { id: 'BU-001', name: 'Retail Banking' },
        { id: 'BU-002', name: 'Corporate Banking' },
        { id: 'BU-003', name: 'Investment Banking' },
      ],
    },
    {
      id: 'TENANT-002',
      name: 'Secondary Bank',
      businessUnits: [
        { id: 'BU-004', name: 'Consumer Banking' },
        { id: 'BU-005', name: 'Business Banking' },
      ],
    },
  ];

  const selectedTenant = mockTenants.find(t => t.id === formData.tenantId);
  const availableBusinessUnits = selectedTenant?.businessUnits || [];

  if (isLoading) {
    return (
      <Box 
        display="flex" 
        justifyContent="center" 
        alignItems="center" 
        minHeight="100vh"
      >
        <CircularProgress size={40} />
      </Box>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight="100vh"
      sx={{
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        padding: 2,
      }}
    >
      <Card sx={{ maxWidth: 400, width: '100%' }}>
        <CardContent sx={{ p: 4 }}>
          <Box textAlign="center" mb={3}>
            <Typography variant="h4" component="h1" gutterBottom>
              Payments Engine
            </Typography>
            <Typography variant="subtitle1" color="text.secondary">
              Operations Portal
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Username"
              value={formData.username}
              onChange={handleInputChange('username')}
              error={!!validationErrors.username}
              helperText={validationErrors.username}
              margin="normal"
              autoComplete="username"
              autoFocus
            />

            <TextField
              fullWidth
              label="Password"
              type={showPassword ? 'text' : 'password'}
              value={formData.password}
              onChange={handleInputChange('password')}
              error={!!validationErrors.password}
              helperText={validationErrors.password}
              margin="normal"
              autoComplete="current-password"
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={togglePasswordVisibility}
                      edge="end"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <FormControl fullWidth margin="normal">
              <InputLabel>Tenant</InputLabel>
              <Select
                value={formData.tenantId}
                onChange={handleTenantChange}
                label="Tenant"
                error={!!validationErrors.tenantId}
              >
                {mockTenants.map((tenant) => (
                  <MenuItem key={tenant.id} value={tenant.id}>
                    {tenant.name}
                  </MenuItem>
                ))}
              </Select>
              {validationErrors.tenantId && (
                <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.75 }}>
                  {validationErrors.tenantId}
                </Typography>
              )}
            </FormControl>

            <FormControl fullWidth margin="normal">
              <InputLabel>Business Unit</InputLabel>
              <Select
                value={formData.businessUnitId}
                onChange={handleBusinessUnitChange}
                label="Business Unit"
                error={!!validationErrors.businessUnitId}
                disabled={!formData.tenantId}
              >
                {availableBusinessUnits.map((bu) => (
                  <MenuItem key={bu.id} value={bu.id}>
                    {bu.name}
                  </MenuItem>
                ))}
              </Select>
              {validationErrors.businessUnitId && (
                <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.75 }}>
                  {validationErrors.businessUnitId}
                </Typography>
              )}
            </FormControl>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={isSubmitting}
              startIcon={isSubmitting ? <CircularProgress size={20} /> : <LoginIcon />}
              sx={{ mt: 3, mb: 2 }}
            >
              {isSubmitting ? 'Signing In...' : 'Sign In'}
            </Button>
          </form>

          {config.enableMockAuth && (
            <Box mt={2} p={2} bgcolor="grey.100" borderRadius={1}>
              <Typography variant="caption" color="text.secondary">
                <strong>Development Mode:</strong> Use any username/password to login
              </Typography>
            </Box>
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

export default Login;
