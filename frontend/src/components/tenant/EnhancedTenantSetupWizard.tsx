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
  Accordion,
  AccordionSummary,
  AccordionDetails,
  RadioGroup,
  Radio,
  FormLabel,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Business as BusinessIcon,
  Settings as SettingsIcon,
  Security as SecurityIcon,
  Payment as PaymentIcon,
  CloudUpload as CloudUploadIcon,
  CheckCircle as CheckCircleIcon,
  ExpandMore as ExpandMoreIcon,
  Info as InfoIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  Api as ApiIcon,
  AccountTree as AccountTreeIcon,
} from '@mui/icons-material';
import ModernButton from '../common/ModernButton';
import ModernCard from '../common/ModernCard';
import StatusChip from '../common/StatusChip';
import {
  AuthMethod,
  Environment,
  PaymentType,
  ServiceType,
  ClearingSystemAuthConfigurationRequest,
  PaymentTypeAuthConfigurationRequest,
  DownstreamCallAuthConfigurationRequest,
} from '../../types/multiLevelAuth';

interface EnhancedTenantSetupData {
  basicInfo: {
    tenantId: string;
    tenantName: string;
    description: string;
    environment: Environment;
  };
  clearingSystemConfig: {
    authMethod: AuthMethod;
    jwtConfig?: {
      secret: string;
      issuer: string;
      audience: string;
      expirationSeconds: number;
    };
    jwsConfig?: {
      secret: string;
      algorithm: string;
      issuer: string;
      audience: string;
      expirationSeconds: number;
    };
    oauth2Config?: {
      tokenEndpoint: string;
      clientId: string;
      clientSecret: string;
      scope: string;
    };
    apiKeyConfig?: {
      apiKey: string;
      headerName: string;
    };
    basicAuthConfig?: {
      username: string;
      password: string;
    };
    clientHeaders: {
      includeClientHeaders: boolean;
      clientId: string;
      clientSecret: string;
      clientIdHeaderName: string;
      clientSecretHeaderName: string;
    };
  };
  paymentTypeConfigs: Array<{
    paymentType: PaymentType;
    authMethod: AuthMethod;
    clearingSystem: string;
    currency: string;
    isHighValue: boolean;
    clientHeaders: {
      includeClientHeaders: boolean;
      clientId: string;
      clientSecret: string;
      clientIdHeaderName: string;
      clientSecretHeaderName: string;
    };
  }>;
  downstreamCallConfigs: Array<{
    serviceType: ServiceType;
    endpoint: string;
    paymentType?: PaymentType;
    authMethod: AuthMethod;
    targetHost: string;
    targetPort: number;
    targetProtocol: 'HTTP' | 'HTTPS';
    timeoutSeconds: number;
    retryAttempts: number;
    clientHeaders: {
      includeClientHeaders: boolean;
      clientId: string;
      clientSecret: string;
      clientIdHeaderName: string;
      clientSecretHeaderName: string;
    };
  }>;
  review: {
    configurations: Array<{
      level: string;
      name: string;
      authMethod: AuthMethod;
      status: 'configured' | 'inherited' | 'default';
    }>;
  };
}

const EnhancedTenantSetupWizard: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [setupData, setSetupData] = useState<EnhancedTenantSetupData>({
    basicInfo: {
      tenantId: '',
      tenantName: '',
      description: '',
      environment: 'dev',
    },
    clearingSystemConfig: {
      authMethod: 'JWT',
      clientHeaders: {
        includeClientHeaders: true,
        clientId: '',
        clientSecret: '',
        clientIdHeaderName: 'X-Client-ID',
        clientSecretHeaderName: 'X-Client-Secret',
      },
    },
    paymentTypeConfigs: [],
    downstreamCallConfigs: [],
    review: {
      configurations: [],
    },
  });

  const steps = [
    {
      label: 'Basic Information',
      icon: <BusinessIcon />,
      description: 'Set up basic tenant information and environment',
    },
    {
      label: 'Clearing System Configuration',
      icon: <SettingsIcon />,
      description: 'Configure global clearing system authentication',
    },
    {
      label: 'Payment Type Configurations',
      icon: <PaymentIcon />,
      description: 'Configure authentication for different payment types',
    },
    {
      label: 'Downstream Call Configurations',
      icon: <ApiIcon />,
      description: 'Configure authentication for specific downstream calls',
    },
    {
      label: 'Configuration Hierarchy Review',
      icon: <AccountTreeIcon />,
      description: 'Review configuration hierarchy and precedence',
    },
    {
      label: 'Deploy & Test',
      icon: <CloudUploadIcon />,
      description: 'Deploy configurations and run tests',
    },
  ];

  const handleNext = () => {
    if (activeStep === 4) {
      // Generate review configurations
      generateReviewConfigurations();
    }
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleReset = () => {
    setActiveStep(0);
  };

  const updateSetupData = (section: keyof EnhancedTenantSetupData, field: string, value: any) => {
    setSetupData(prev => ({
      ...prev,
      [section]: {
        ...prev[section],
        [field]: value,
      },
    }));
  };

  const addPaymentTypeConfig = () => {
    const newConfig = {
      paymentType: 'SEPA' as PaymentType,
      authMethod: 'JWT' as AuthMethod,
      clearingSystem: 'SEPA_CLEARING',
      currency: 'EUR',
      isHighValue: false,
      clientHeaders: {
        includeClientHeaders: true,
        clientId: '',
        clientSecret: '',
        clientIdHeaderName: 'X-Client-ID',
        clientSecretHeaderName: 'X-Client-Secret',
      },
    };
    setSetupData(prev => ({
      ...prev,
      paymentTypeConfigs: [...prev.paymentTypeConfigs, newConfig],
    }));
  };

  const removePaymentTypeConfig = (index: number) => {
    setSetupData(prev => ({
      ...prev,
      paymentTypeConfigs: prev.paymentTypeConfigs.filter((_, i) => i !== index),
    }));
  };

  const addDownstreamCallConfig = () => {
    const newConfig = {
      serviceType: 'fraud' as ServiceType,
      endpoint: '/fraud',
      paymentType: 'SEPA' as PaymentType,
      authMethod: 'JWT' as AuthMethod,
      targetHost: 'fraud.bank-nginx.example.com',
      targetPort: 443,
      targetProtocol: 'HTTPS' as const,
      timeoutSeconds: 30,
      retryAttempts: 3,
      clientHeaders: {
        includeClientHeaders: true,
        clientId: '',
        clientSecret: '',
        clientIdHeaderName: 'X-Client-ID',
        clientSecretHeaderName: 'X-Client-Secret',
      },
    };
    setSetupData(prev => ({
      ...prev,
      downstreamCallConfigs: [...prev.downstreamCallConfigs, newConfig],
    }));
  };

  const removeDownstreamCallConfig = (index: number) => {
    setSetupData(prev => ({
      ...prev,
      downstreamCallConfigs: prev.downstreamCallConfigs.filter((_, i) => i !== index),
    }));
  };

  const generateReviewConfigurations = () => {
    const configurations = [
      {
        level: 'Clearing System Level',
        name: `${setupData.basicInfo.environment} Environment`,
        authMethod: setupData.clearingSystemConfig.authMethod,
        status: 'configured' as const,
      },
    ];

    setupData.paymentTypeConfigs.forEach(config => {
      configurations.push({
        level: 'Payment Type Level',
        name: `${config.paymentType} Payments`,
        authMethod: config.authMethod,
        status: 'configured' as const,
      });
    });

    setupData.downstreamCallConfigs.forEach(config => {
      configurations.push({
        level: 'Downstream Call Level',
        name: `${config.serviceType} - ${config.endpoint}`,
        authMethod: config.authMethod,
        status: 'configured' as const,
      });
    });

    setSetupData(prev => ({
      ...prev,
      review: {
        configurations,
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
              <MenuItem value="dev">Development</MenuItem>
              <MenuItem value="staging">Staging</MenuItem>
              <MenuItem value="prod">Production</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      </Grid>
    </Box>
  );

  const renderClearingSystemConfigStep = () => (
    <Box sx={{ p: 2 }}>
      <Alert severity="info" sx={{ mb: 3 }}>
        This is the global configuration for the clearing system environment. 
        It serves as the fallback when no higher-level configuration exists.
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <FormControl component="fieldset">
            <FormLabel component="legend">Authentication Method</FormLabel>
            <RadioGroup
              value={setupData.clearingSystemConfig.authMethod}
              onChange={(e) => updateSetupData('clearingSystemConfig', 'authMethod', e.target.value)}
            >
              <FormControlLabel value="JWT" control={<Radio />} label="JWT (JSON Web Token)" />
              <FormControlLabel value="JWS" control={<Radio />} label="JWS (JSON Web Signature)" />
              <FormControlLabel value="OAUTH2" control={<Radio />} label="OAuth2" />
              <FormControlLabel value="API_KEY" control={<Radio />} label="API Key" />
              <FormControlLabel value="BASIC" control={<Radio />} label="Basic Authentication" />
            </RadioGroup>
          </FormControl>
        </Grid>

        {setupData.clearingSystemConfig.authMethod === 'JWT' && (
          <Grid item xs={12}>
            <Accordion>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography>JWT Configuration</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="JWT Secret"
                      type="password"
                      value={setupData.clearingSystemConfig.jwtConfig?.secret || ''}
                      onChange={(e) => updateSetupData('clearingSystemConfig', 'jwtConfig', {
                        ...setupData.clearingSystemConfig.jwtConfig,
                        secret: e.target.value,
                      })}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="JWT Issuer"
                      value={setupData.clearingSystemConfig.jwtConfig?.issuer || ''}
                      onChange={(e) => updateSetupData('clearingSystemConfig', 'jwtConfig', {
                        ...setupData.clearingSystemConfig.jwtConfig,
                        issuer: e.target.value,
                      })}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="JWT Audience"
                      value={setupData.clearingSystemConfig.jwtConfig?.audience || ''}
                      onChange={(e) => updateSetupData('clearingSystemConfig', 'jwtConfig', {
                        ...setupData.clearingSystemConfig.jwtConfig,
                        audience: e.target.value,
                      })}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Expiration (seconds)"
                      type="number"
                      value={setupData.clearingSystemConfig.jwtConfig?.expirationSeconds || 3600}
                      onChange={(e) => updateSetupData('clearingSystemConfig', 'jwtConfig', {
                        ...setupData.clearingSystemConfig.jwtConfig,
                        expirationSeconds: parseInt(e.target.value),
                      })}
                    />
                  </Grid>
                </Grid>
              </AccordionDetails>
            </Accordion>
          </Grid>
        )}

        {setupData.clearingSystemConfig.authMethod === 'JWS' && (
          <Grid item xs={12}>
            <Accordion>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography>JWS Configuration</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="JWS Secret"
                      type="password"
                      value={setupData.clearingSystemConfig.jwsConfig?.secret || ''}
                      onChange={(e) => updateSetupData('clearingSystemConfig', 'jwsConfig', {
                        ...setupData.clearingSystemConfig.jwsConfig,
                        secret: e.target.value,
                      })}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <FormControl fullWidth>
                      <InputLabel>JWS Algorithm</InputLabel>
                      <Select
                        value={setupData.clearingSystemConfig.jwsConfig?.algorithm || 'HS256'}
                        onChange={(e) => updateSetupData('clearingSystemConfig', 'jwsConfig', {
                          ...setupData.clearingSystemConfig.jwsConfig,
                          algorithm: e.target.value,
                        })}
                      >
                        <MenuItem value="HS256">HS256</MenuItem>
                        <MenuItem value="HS384">HS384</MenuItem>
                        <MenuItem value="HS512">HS512</MenuItem>
                        <MenuItem value="RS256">RS256</MenuItem>
                        <MenuItem value="RS384">RS384</MenuItem>
                        <MenuItem value="RS512">RS512</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                </Grid>
              </AccordionDetails>
            </Accordion>
          </Grid>
        )}

        <Grid item xs={12}>
          <Divider sx={{ my: 2 }} />
          <Typography variant="h6" gutterBottom>
            Client Headers Configuration
          </Typography>
          <FormControlLabel
            control={
              <Switch
                checked={setupData.clearingSystemConfig.clientHeaders.includeClientHeaders}
                onChange={(e) => updateSetupData('clearingSystemConfig', 'clientHeaders', {
                  ...setupData.clearingSystemConfig.clientHeaders,
                  includeClientHeaders: e.target.checked,
                })}
              />
            }
            label="Include Client Headers in Outgoing Calls"
          />
          
          {setupData.clearingSystemConfig.clientHeaders.includeClientHeaders && (
            <Grid container spacing={2} sx={{ mt: 2 }}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Client ID"
                  value={setupData.clearingSystemConfig.clientHeaders.clientId}
                  onChange={(e) => updateSetupData('clearingSystemConfig', 'clientHeaders', {
                    ...setupData.clearingSystemConfig.clientHeaders,
                    clientId: e.target.value,
                  })}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Client Secret"
                  type="password"
                  value={setupData.clearingSystemConfig.clientHeaders.clientSecret}
                  onChange={(e) => updateSetupData('clearingSystemConfig', 'clientHeaders', {
                    ...setupData.clearingSystemConfig.clientHeaders,
                    clientSecret: e.target.value,
                  })}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Client ID Header Name"
                  value={setupData.clearingSystemConfig.clientHeaders.clientIdHeaderName}
                  onChange={(e) => updateSetupData('clearingSystemConfig', 'clientHeaders', {
                    ...setupData.clearingSystemConfig.clientHeaders,
                    clientIdHeaderName: e.target.value,
                  })}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Client Secret Header Name"
                  value={setupData.clearingSystemConfig.clientHeaders.clientSecretHeaderName}
                  onChange={(e) => updateSetupData('clearingSystemConfig', 'clientHeaders', {
                    ...setupData.clearingSystemConfig.clientHeaders,
                    clientSecretHeaderName: e.target.value,
                  })}
                />
              </Grid>
            </Grid>
          )}
        </Grid>
      </Grid>
    </Box>
  );

  const renderPaymentTypeConfigStep = () => (
    <Box sx={{ p: 2 }}>
      <Alert severity="info" sx={{ mb: 3 }}>
        Configure authentication for different payment types (SEPA, SWIFT, ACH, etc.).
        These configurations override the clearing system level configuration.
      </Alert>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h6">Payment Type Configurations</Typography>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={addPaymentTypeConfig}
        >
          Add Payment Type
        </Button>
      </Box>

      {setupData.paymentTypeConfigs.map((config, index) => (
        <Card key={index} sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">
                Payment Type Configuration #{index + 1}
              </Typography>
              <IconButton
                color="error"
                onClick={() => removePaymentTypeConfig(index)}
              >
                <DeleteIcon />
              </IconButton>
            </Box>

            <Grid container spacing={2}>
              <Grid item xs={12} md={3}>
                <FormControl fullWidth>
                  <InputLabel>Payment Type</InputLabel>
                  <Select
                    value={config.paymentType}
                    onChange={(e) => {
                      const newConfigs = [...setupData.paymentTypeConfigs];
                      newConfigs[index].paymentType = e.target.value as PaymentType;
                      setSetupData(prev => ({ ...prev, paymentTypeConfigs: newConfigs }));
                    }}
                  >
                    <MenuItem value="SEPA">SEPA</MenuItem>
                    <MenuItem value="SWIFT">SWIFT</MenuItem>
                    <MenuItem value="ACH">ACH</MenuItem>
                    <MenuItem value="CARD">CARD</MenuItem>
                    <MenuItem value="CUSTOM">CUSTOM</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} md={3}>
                <FormControl fullWidth>
                  <InputLabel>Auth Method</InputLabel>
                  <Select
                    value={config.authMethod}
                    onChange={(e) => {
                      const newConfigs = [...setupData.paymentTypeConfigs];
                      newConfigs[index].authMethod = e.target.value as AuthMethod;
                      setSetupData(prev => ({ ...prev, paymentTypeConfigs: newConfigs }));
                    }}
                  >
                    <MenuItem value="JWT">JWT</MenuItem>
                    <MenuItem value="JWS">JWS</MenuItem>
                    <MenuItem value="OAUTH2">OAuth2</MenuItem>
                    <MenuItem value="API_KEY">API Key</MenuItem>
                    <MenuItem value="BASIC">Basic Auth</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField
                  fullWidth
                  label="Clearing System"
                  value={config.clearingSystem}
                  onChange={(e) => {
                    const newConfigs = [...setupData.paymentTypeConfigs];
                    newConfigs[index].clearingSystem = e.target.value;
                    setSetupData(prev => ({ ...prev, paymentTypeConfigs: newConfigs }));
                  }}
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField
                  fullWidth
                  label="Currency"
                  value={config.currency}
                  onChange={(e) => {
                    const newConfigs = [...setupData.paymentTypeConfigs];
                    newConfigs[index].currency = e.target.value;
                    setSetupData(prev => ({ ...prev, paymentTypeConfigs: newConfigs }));
                  }}
                />
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      ))}

      {setupData.paymentTypeConfigs.length === 0 && (
        <Alert severity="info">
          No payment type configurations added yet. Click "Add Payment Type" to get started.
        </Alert>
      )}
    </Box>
  );

  const renderDownstreamCallConfigStep = () => (
    <Box sx={{ p: 2 }}>
      <Alert severity="info" sx={{ mb: 3 }}>
        Configure authentication for specific downstream calls (service type + endpoint).
        These configurations have the highest priority and override all other levels.
      </Alert>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h6">Downstream Call Configurations</Typography>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={addDownstreamCallConfig}
        >
          Add Downstream Call
        </Button>
      </Box>

      {setupData.downstreamCallConfigs.map((config, index) => (
        <Card key={index} sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">
                Downstream Call Configuration #{index + 1}
              </Typography>
              <IconButton
                color="error"
                onClick={() => removeDownstreamCallConfig(index)}
              >
                <DeleteIcon />
              </IconButton>
            </Box>

            <Grid container spacing={2}>
              <Grid item xs={12} md={3}>
                <FormControl fullWidth>
                  <InputLabel>Service Type</InputLabel>
                  <Select
                    value={config.serviceType}
                    onChange={(e) => {
                      const newConfigs = [...setupData.downstreamCallConfigs];
                      newConfigs[index].serviceType = e.target.value as ServiceType;
                      setSetupData(prev => ({ ...prev, downstreamCallConfigs: newConfigs }));
                    }}
                  >
                    <MenuItem value="fraud">Fraud</MenuItem>
                    <MenuItem value="clearing">Clearing</MenuItem>
                    <MenuItem value="banking">Banking</MenuItem>
                    <MenuItem value="custom">Custom</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField
                  fullWidth
                  label="Endpoint"
                  value={config.endpoint}
                  onChange={(e) => {
                    const newConfigs = [...setupData.downstreamCallConfigs];
                    newConfigs[index].endpoint = e.target.value;
                    setSetupData(prev => ({ ...prev, downstreamCallConfigs: newConfigs }));
                  }}
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <FormControl fullWidth>
                  <InputLabel>Payment Type</InputLabel>
                  <Select
                    value={config.paymentType || ''}
                    onChange={(e) => {
                      const newConfigs = [...setupData.downstreamCallConfigs];
                      newConfigs[index].paymentType = e.target.value as PaymentType;
                      setSetupData(prev => ({ ...prev, downstreamCallConfigs: newConfigs }));
                    }}
                  >
                    <MenuItem value="">None</MenuItem>
                    <MenuItem value="SEPA">SEPA</MenuItem>
                    <MenuItem value="SWIFT">SWIFT</MenuItem>
                    <MenuItem value="ACH">ACH</MenuItem>
                    <MenuItem value="CARD">CARD</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} md={3}>
                <FormControl fullWidth>
                  <InputLabel>Auth Method</InputLabel>
                  <Select
                    value={config.authMethod}
                    onChange={(e) => {
                      const newConfigs = [...setupData.downstreamCallConfigs];
                      newConfigs[index].authMethod = e.target.value as AuthMethod;
                      setSetupData(prev => ({ ...prev, downstreamCallConfigs: newConfigs }));
                    }}
                  >
                    <MenuItem value="JWT">JWT</MenuItem>
                    <MenuItem value="JWS">JWS</MenuItem>
                    <MenuItem value="OAUTH2">OAuth2</MenuItem>
                    <MenuItem value="API_KEY">API Key</MenuItem>
                    <MenuItem value="BASIC">Basic Auth</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Target Host"
                  value={config.targetHost}
                  onChange={(e) => {
                    const newConfigs = [...setupData.downstreamCallConfigs];
                    newConfigs[index].targetHost = e.target.value;
                    setSetupData(prev => ({ ...prev, downstreamCallConfigs: newConfigs }));
                  }}
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField
                  fullWidth
                  label="Target Port"
                  type="number"
                  value={config.targetPort}
                  onChange={(e) => {
                    const newConfigs = [...setupData.downstreamCallConfigs];
                    newConfigs[index].targetPort = parseInt(e.target.value);
                    setSetupData(prev => ({ ...prev, downstreamCallConfigs: newConfigs }));
                  }}
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <FormControl fullWidth>
                  <InputLabel>Protocol</InputLabel>
                  <Select
                    value={config.targetProtocol}
                    onChange={(e) => {
                      const newConfigs = [...setupData.downstreamCallConfigs];
                      newConfigs[index].targetProtocol = e.target.value as 'HTTP' | 'HTTPS';
                      setSetupData(prev => ({ ...prev, downstreamCallConfigs: newConfigs }));
                    }}
                  >
                    <MenuItem value="HTTP">HTTP</MenuItem>
                    <MenuItem value="HTTPS">HTTPS</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      ))}

      {setupData.downstreamCallConfigs.length === 0 && (
        <Alert severity="info">
          No downstream call configurations added yet. Click "Add Downstream Call" to get started.
        </Alert>
      )}
    </Box>
  );

  const renderConfigurationHierarchyStep = () => (
    <Box sx={{ p: 2 }}>
      <Alert severity="info" sx={{ mb: 3 }}>
        Review the configuration hierarchy and precedence. Higher levels override lower levels.
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom>
            Configuration Hierarchy
          </Typography>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Priority 1: Downstream Call Level (Highest)</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" color="text.secondary">
                Most specific configuration for individual service calls. Overrides all other levels.
              </Typography>
              {setupData.downstreamCallConfigs.map((config, index) => (
                <Chip
                  key={index}
                  label={`${config.serviceType} - ${config.endpoint} (${config.authMethod})`}
                  color="primary"
                  sx={{ mr: 1, mb: 1 }}
                />
              ))}
            </AccordionDetails>
          </Accordion>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Priority 2: Payment Type Level</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" color="text.secondary">
                Configuration for specific payment types (SEPA, SWIFT, etc.). Overrides tenant and clearing system levels.
              </Typography>
              {setupData.paymentTypeConfigs.map((config, index) => (
                <Chip
                  key={index}
                  label={`${config.paymentType} (${config.authMethod})`}
                  color="secondary"
                  sx={{ mr: 1, mb: 1 }}
                />
              ))}
            </AccordionDetails>
          </Accordion>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Priority 3: Tenant Level</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" color="text.secondary">
                Tenant-specific configuration. Overrides clearing system level.
              </Typography>
              <Chip
                label="Tenant Level (Not configured in this wizard)"
                color="default"
                sx={{ mr: 1, mb: 1 }}
              />
            </AccordionDetails>
          </Accordion>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Priority 4: Clearing System Level (Lowest)</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" color="text.secondary">
                Global configuration for the entire clearing system. Used as fallback when no higher-level configuration exists.
              </Typography>
              <Chip
                label={`${setupData.basicInfo.environment} Environment (${setupData.clearingSystemConfig.authMethod})`}
                color="default"
                sx={{ mr: 1, mb: 1 }}
              />
            </AccordionDetails>
          </Accordion>
        </Grid>

        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom>
            Configuration Summary
          </Typography>
          <Grid container spacing={2}>
            {setupData.review.configurations.map((config, index) => (
              <Grid item xs={12} md={6} key={index}>
                <ModernCard title={config.level} elevation="low">
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">Name:</Typography>
                      <Typography variant="body2" fontWeight={500}>{config.name}</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">Auth Method:</Typography>
                      <StatusChip status="active" label={config.authMethod} />
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">Status:</Typography>
                      <StatusChip status={config.status === 'configured' ? 'active' : 'inactive'} label={config.status} />
                    </Box>
                  </Box>
                </ModernCard>
              </Grid>
            ))}
          </Grid>
        </Grid>
      </Grid>
    </Box>
  );

  const renderDeployStep = () => (
    <Box sx={{ p: 2 }}>
      <Alert severity="success" sx={{ mb: 3 }}>
        Ready to deploy! All configurations have been validated and are ready for deployment.
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <ModernCard title="Deployment Summary" elevation="low">
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Tenant:</Typography>
                <Typography variant="body2" fontWeight={500}>{setupData.basicInfo.tenantName}</Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Environment:</Typography>
                <StatusChip status="active" label={setupData.basicInfo.environment} />
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Payment Types:</Typography>
                <Typography variant="body2" fontWeight={500}>{setupData.paymentTypeConfigs.length}</Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Downstream Calls:</Typography>
                <Typography variant="body2" fontWeight={500}>{setupData.downstreamCallConfigs.length}</Typography>
              </Box>
            </Box>
          </ModernCard>
        </Grid>

        <Grid item xs={12} md={6}>
          <ModernCard title="Next Steps" elevation="low">
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Typography variant="body2">1. Deploy configurations to environment</Typography>
              <Typography variant="body2">2. Run configuration tests</Typography>
              <Typography variant="body2">3. Validate authentication flows</Typography>
              <Typography variant="body2">4. Monitor configuration usage</Typography>
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
        return renderClearingSystemConfigStep();
      case 2:
        return renderPaymentTypeConfigStep();
      case 3:
        return renderDownstreamCallConfigStep();
      case 4:
        return renderConfigurationHierarchyStep();
      case 5:
        return renderDeployStep();
      default:
        return 'Unknown step';
    }
  };

  const isStepValid = (step: number) => {
    switch (step) {
      case 0:
        return setupData.basicInfo.tenantId && setupData.basicInfo.tenantName;
      case 1:
        return setupData.clearingSystemConfig.authMethod;
      case 2:
        return true; // Payment type configs are optional
      case 3:
        return true; // Downstream call configs are optional
      case 4:
        return true; // Review step
      case 5:
        return true; // Deploy step
      default:
        return false;
    }
  };

  return (
    <Box sx={{ maxWidth: 1000, mx: 'auto', p: 3 }}>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Enhanced Tenant Setup Wizard
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Follow the guided steps to set up a new tenant with multi-level authentication configuration.
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
                    {index === steps.length - 1 ? 'Deploy Configurations' : 'Continue'}
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
              Multi-Level Authentication Configuration Complete!
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
              Your tenant has been successfully configured with multi-level authentication settings.
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

export default EnhancedTenantSetupWizard;