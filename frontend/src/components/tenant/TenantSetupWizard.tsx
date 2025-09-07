import React, { useState } from 'react';
import {
  Box,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Button,
  Typography,
  Card,
  CardContent,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Chip,
  Alert,
  Divider,
} from '@mui/material';
import {
  Business as BusinessIcon,
  Settings as SettingsIcon,
  Security as SecurityIcon,
  Payment as PaymentIcon,
  CloudUpload as CloudUploadIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material';
import ModernButton from '../common/ModernButton';
import ModernCard from '../common/ModernCard';
import StatusChip from '../common/StatusChip';

interface TenantSetupData {
  basicInfo: {
    tenantId: string;
    tenantName: string;
    description: string;
    environment: string;
  };
  configuration: {
    databaseUrl: string;
    redisHost: string;
    kafkaBootstrapServers: string;
    enableMonitoring: boolean;
    enableLogging: boolean;
  };
  security: {
    enableAuthentication: boolean;
    enableEncryption: boolean;
    enableAuditLogging: boolean;
    jwtSecret: string;
  };
  paymentSettings: {
    defaultCurrency: string;
    enableFraudDetection: boolean;
    enableRiskMonitoring: boolean;
    maxTransactionAmount: number;
  };
}

const TenantSetupWizard: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [setupData, setSetupData] = useState<TenantSetupData>({
    basicInfo: {
      tenantId: '',
      tenantName: '',
      description: '',
      environment: 'DEVELOPMENT',
    },
    configuration: {
      databaseUrl: 'jdbc:postgresql://localhost:5432/payment_engine',
      redisHost: 'localhost:6379',
      kafkaBootstrapServers: 'localhost:9092',
      enableMonitoring: true,
      enableLogging: true,
    },
    security: {
      enableAuthentication: true,
      enableEncryption: true,
      enableAuditLogging: true,
      jwtSecret: '',
    },
    paymentSettings: {
      defaultCurrency: 'USD',
      enableFraudDetection: true,
      enableRiskMonitoring: true,
      maxTransactionAmount: 1000000,
    },
  });

  const steps = [
    {
      label: 'Basic Information',
      icon: <BusinessIcon />,
      description: 'Set up basic tenant information and environment',
    },
    {
      label: 'Configuration',
      icon: <SettingsIcon />,
      description: 'Configure database, cache, and messaging settings',
    },
    {
      label: 'Security',
      icon: <SecurityIcon />,
      description: 'Set up authentication and security policies',
    },
    {
      label: 'Payment Settings',
      icon: <PaymentIcon />,
      description: 'Configure payment processing and risk management',
    },
    {
      label: 'Review & Deploy',
      icon: <CloudUploadIcon />,
      description: 'Review configuration and deploy tenant',
    },
  ];

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleReset = () => {
    setActiveStep(0);
  };

  const updateSetupData = (section: keyof TenantSetupData, field: string, value: any) => {
    setSetupData(prev => ({
      ...prev,
      [section]: {
        ...prev[section],
        [field]: value,
      },
    }));
  };

  const renderBasicInfoStep = () => (
    <Box sx={{ p: 2 }}>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Tenant ID"
            value={setupData.basicInfo.tenantId}
            onChange={(e) => updateSetupData('basicInfo', 'tenantId', e.target.value)}
            placeholder="e.g., bank-001"
            helperText="Unique identifier for the tenant"
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Tenant Name"
            value={setupData.basicInfo.tenantName}
            onChange={(e) => updateSetupData('basicInfo', 'tenantName', e.target.value)}
            placeholder="e.g., First National Bank"
          />
        </Grid>
        <Grid item xs={12}>
          <TextField
            fullWidth
            multiline
            rows={3}
            label="Description"
            value={setupData.basicInfo.description}
            onChange={(e) => updateSetupData('basicInfo', 'description', e.target.value)}
            placeholder="Brief description of the tenant and its purpose"
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <FormControl fullWidth>
            <InputLabel>Environment</InputLabel>
            <Select
              value={setupData.basicInfo.environment}
              onChange={(e) => updateSetupData('basicInfo', 'environment', e.target.value)}
              label="Environment"
            >
              <MenuItem value="DEVELOPMENT">Development</MenuItem>
              <MenuItem value="INTEGRATION">Integration</MenuItem>
              <MenuItem value="USER_ACCEPTANCE">User Acceptance</MenuItem>
              <MenuItem value="PRODUCTION">Production</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      </Grid>
    </Box>
  );

  const renderConfigurationStep = () => (
    <Box sx={{ p: 2 }}>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom>
            Database Configuration
          </Typography>
          <TextField
            fullWidth
            label="Database URL"
            value={setupData.configuration.databaseUrl}
            onChange={(e) => updateSetupData('configuration', 'databaseUrl', e.target.value)}
            helperText="PostgreSQL connection string"
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="h6" gutterBottom>
            Cache Configuration
          </Typography>
          <TextField
            fullWidth
            label="Redis Host"
            value={setupData.configuration.redisHost}
            onChange={(e) => updateSetupData('configuration', 'redisHost', e.target.value)}
            helperText="Redis server address and port"
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="h6" gutterBottom>
            Messaging Configuration
          </Typography>
          <TextField
            fullWidth
            label="Kafka Bootstrap Servers"
            value={setupData.configuration.kafkaBootstrapServers}
            onChange={(e) => updateSetupData('configuration', 'kafkaBootstrapServers', e.target.value)}
            helperText="Kafka broker addresses"
          />
        </Grid>
        <Grid item xs={12}>
          <Divider sx={{ my: 2 }} />
          <Typography variant="h6" gutterBottom>
            Feature Toggles
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <FormControlLabel
              control={
                <Switch
                  checked={setupData.configuration.enableMonitoring}
                  onChange={(e) => updateSetupData('configuration', 'enableMonitoring', e.target.checked)}
                />
              }
              label="Enable Monitoring"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={setupData.configuration.enableLogging}
                  onChange={(e) => updateSetupData('configuration', 'enableLogging', e.target.checked)}
                />
              }
              label="Enable Logging"
            />
          </Box>
        </Grid>
      </Grid>
    </Box>
  );

  const renderSecurityStep = () => (
    <Box sx={{ p: 2 }}>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom>
            Authentication & Authorization
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 3 }}>
            <FormControlLabel
              control={
                <Switch
                  checked={setupData.security.enableAuthentication}
                  onChange={(e) => updateSetupData('security', 'enableAuthentication', e.target.checked)}
                />
              }
              label="Enable Authentication"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={setupData.security.enableEncryption}
                  onChange={(e) => updateSetupData('security', 'enableEncryption', e.target.checked)}
                />
              }
              label="Enable Encryption"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={setupData.security.enableAuditLogging}
                  onChange={(e) => updateSetupData('security', 'enableAuditLogging', e.target.checked)}
                />
              }
              label="Enable Audit Logging"
            />
          </Box>
        </Grid>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="JWT Secret"
            type="password"
            value={setupData.security.jwtSecret}
            onChange={(e) => updateSetupData('security', 'jwtSecret', e.target.value)}
            helperText="Secret key for JWT token generation (leave empty for auto-generation)"
          />
        </Grid>
      </Grid>
    </Box>
  );

  const renderPaymentSettingsStep = () => (
    <Box sx={{ p: 2 }}>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <FormControl fullWidth>
            <InputLabel>Default Currency</InputLabel>
            <Select
              value={setupData.paymentSettings.defaultCurrency}
              onChange={(e) => updateSetupData('paymentSettings', 'defaultCurrency', e.target.value)}
              label="Default Currency"
            >
              <MenuItem value="USD">USD - US Dollar</MenuItem>
              <MenuItem value="EUR">EUR - Euro</MenuItem>
              <MenuItem value="GBP">GBP - British Pound</MenuItem>
              <MenuItem value="JPY">JPY - Japanese Yen</MenuItem>
              <MenuItem value="CAD">CAD - Canadian Dollar</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Max Transaction Amount"
            type="number"
            value={setupData.paymentSettings.maxTransactionAmount}
            onChange={(e) => updateSetupData('paymentSettings', 'maxTransactionAmount', Number(e.target.value))}
            helperText="Maximum allowed transaction amount"
          />
        </Grid>
        <Grid item xs={12}>
          <Divider sx={{ my: 2 }} />
          <Typography variant="h6" gutterBottom>
            Risk Management
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <FormControlLabel
              control={
                <Switch
                  checked={setupData.paymentSettings.enableFraudDetection}
                  onChange={(e) => updateSetupData('paymentSettings', 'enableFraudDetection', e.target.checked)}
                />
              }
              label="Enable Fraud Detection"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={setupData.paymentSettings.enableRiskMonitoring}
                  onChange={(e) => updateSetupData('paymentSettings', 'enableRiskMonitoring', e.target.checked)}
                />
              }
              label="Enable Risk Monitoring"
            />
          </Box>
        </Grid>
      </Grid>
    </Box>
  );

  const renderReviewStep = () => (
    <Box sx={{ p: 2 }}>
      <Alert severity="info" sx={{ mb: 3 }}>
        Review your tenant configuration before deployment. You can go back to modify any settings.
      </Alert>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <ModernCard title="Basic Information" elevation="low">
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Tenant ID:</Typography>
                <Typography variant="body2" fontWeight={500}>{setupData.basicInfo.tenantId}</Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Name:</Typography>
                <Typography variant="body2" fontWeight={500}>{setupData.basicInfo.tenantName}</Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Environment:</Typography>
                <StatusChip status="active" label={setupData.basicInfo.environment} />
              </Box>
            </Box>
          </ModernCard>
        </Grid>
        
        <Grid item xs={12} md={6}>
          <ModernCard title="Configuration Summary" elevation="low">
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Monitoring:</Typography>
                <StatusChip status={setupData.configuration.enableMonitoring ? 'active' : 'inactive'} />
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Logging:</Typography>
                <StatusChip status={setupData.configuration.enableLogging ? 'active' : 'inactive'} />
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Authentication:</Typography>
                <StatusChip status={setupData.security.enableAuthentication ? 'active' : 'inactive'} />
              </Box>
            </Box>
          </ModernCard>
        </Grid>
      </Grid>
    </Box>
  );

  const getStepContent = (step: number) => {
    switch (step) {
      case 0:
        return renderBasicInfoStep();
      case 1:
        return renderConfigurationStep();
      case 2:
        return renderSecurityStep();
      case 3:
        return renderPaymentSettingsStep();
      case 4:
        return renderReviewStep();
      default:
        return 'Unknown step';
    }
  };

  const isStepValid = (step: number) => {
    switch (step) {
      case 0:
        return setupData.basicInfo.tenantId && setupData.basicInfo.tenantName;
      case 1:
        return setupData.configuration.databaseUrl && setupData.configuration.redisHost;
      case 2:
        return true; // Security settings are optional
      case 3:
        return setupData.paymentSettings.defaultCurrency;
      case 4:
        return true; // Review step
      default:
        return false;
    }
  };

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto', p: 3 }}>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Tenant Setup Wizard
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Follow the guided steps to set up a new tenant with all necessary configurations.
      </Typography>

      <Stepper activeStep={activeStep} orientation="vertical">
        {steps.map((step, index) => (
          <Step key={step.label}>
            <StepLabel
              icon={step.icon}
              sx={{
                '& .MuiStepLabel-label': {
                  fontWeight: 600,
                },
              }}
            >
              <Box>
                <Typography variant="h6">{step.label}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {step.description}
                </Typography>
              </Box>
            </StepLabel>
            <StepContent>
              {getStepContent(index)}
              <Box sx={{ mb: 2, mt: 3 }}>
                <div>
                  <ModernButton
                    variant="primary"
                    onClick={handleNext}
                    disabled={!isStepValid(index)}
                    sx={{ mr: 1 }}
                  >
                    {index === steps.length - 1 ? 'Deploy Tenant' : 'Continue'}
                  </ModernButton>
                  <ModernButton
                    variant="outline"
                    disabled={index === 0}
                    onClick={handleBack}
                    sx={{ mr: 1 }}
                  >
                    Back
                  </ModernButton>
                </div>
              </Box>
            </StepContent>
          </Step>
        ))}
      </Stepper>

      {activeStep === steps.length && (
        <Card sx={{ mt: 3 }}>
          <CardContent sx={{ textAlign: 'center', py: 4 }}>
            <CheckCircleIcon sx={{ fontSize: 64, color: 'success.main', mb: 2 }} />
            <Typography variant="h5" gutterBottom fontWeight={600}>
              Tenant Setup Complete!
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
              Your tenant has been successfully configured and deployed.
            </Typography>
            <ModernButton variant="primary" onClick={handleReset}>
              Set Up Another Tenant
            </ModernButton>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default TenantSetupWizard;