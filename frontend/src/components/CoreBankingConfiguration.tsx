import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Switch,
  FormControlLabel,
  Button,
  Alert,
  Divider,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tabs,
  Tab,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  ExpandMore as ExpandMoreIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  TestTube as TestIcon,
  Security as SecurityIcon,
  Api as ApiIcon,
  Settings as SettingsIcon,
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';

// Types
interface CoreBankingConfiguration {
  id?: string;
  tenantId: string;
  adapterType: 'REST' | 'GRPC' | 'INTERNAL';
  baseUrl?: string;
  grpcHost?: string;
  grpcPort?: number;
  authenticationMethod: string;
  apiKey?: string;
  username?: string;
  password?: string;
  certificatePath?: string;
  processingMode: 'SYNC' | 'ASYNC' | 'BATCH';
  messageFormat: 'JSON' | 'XML';
  timeoutMs: number;
  retryAttempts: number;
  priority: number;
  isActive: boolean;
  bankCode: string;
  bankName: string;
  additionalConfig?: Record<string, any>;
}

interface ClearingSystemConfiguration {
  id?: string;
  clearingSystemCode: string;
  clearingSystemName: string;
  tenantId: string;
  endpointUrl: string;
  authenticationMethod: string;
  processingMode: 'SYNC' | 'ASYNC' | 'BATCH';
  messageFormat: 'JSON' | 'XML';
  supportedPaymentTypes: string[];
  supportedCurrencies: string[];
  localInstrumentationCodes: string[];
  isActive: boolean;
}

interface PaymentRoutingRule {
  id?: string;
  tenantId: string;
  paymentType: string;
  localInstrumentationCode: string;
  routingType: 'SAME_BANK' | 'OTHER_BANK' | 'INCOMING_CLEARING' | 'EXTERNAL_SYSTEM';
  clearingSystemCode?: string;
  processingMode: 'SYNC' | 'ASYNC' | 'BATCH';
  messageFormat: 'JSON' | 'XML';
  priority: number;
  isActive: boolean;
}

// Validation schemas
const coreBankingSchema = yup.object({
  tenantId: yup.string().required('Tenant ID is required'),
  adapterType: yup.string().oneOf(['REST', 'GRPC', 'INTERNAL']).required('Adapter type is required'),
  baseUrl: yup.string().when('adapterType', {
    is: 'REST',
    then: (schema) => schema.required('Base URL is required for REST adapter'),
    otherwise: (schema) => schema.notRequired(),
  }),
  grpcHost: yup.string().when('adapterType', {
    is: 'GRPC',
    then: (schema) => schema.required('gRPC Host is required for gRPC adapter'),
    otherwise: (schema) => schema.notRequired(),
  }),
  grpcPort: yup.number().when('adapterType', {
    is: 'GRPC',
    then: (schema) => schema.required('gRPC Port is required for gRPC adapter').min(1).max(65535),
    otherwise: (schema) => schema.notRequired(),
  }),
  authenticationMethod: yup.string().required('Authentication method is required'),
  processingMode: yup.string().oneOf(['SYNC', 'ASYNC', 'BATCH']).required('Processing mode is required'),
  messageFormat: yup.string().oneOf(['JSON', 'XML']).required('Message format is required'),
  timeoutMs: yup.number().required('Timeout is required').min(1000).max(300000),
  retryAttempts: yup.number().required('Retry attempts is required').min(0).max(10),
  priority: yup.number().required('Priority is required').min(1).max(100),
  bankCode: yup.string().required('Bank code is required'),
  bankName: yup.string().required('Bank name is required'),
});

const CoreBankingConfiguration: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [configurations, setConfigurations] = useState<CoreBankingConfiguration[]>([]);
  const [clearingSystems, setClearingSystems] = useState<ClearingSystemConfiguration[]>([]);
  const [routingRules, setRoutingRules] = useState<PaymentRoutingRule[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingConfig, setEditingConfig] = useState<CoreBankingConfiguration | null>(null);
  const [testingConnection, setTestingConnection] = useState(false);

  const {
    control,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isDirty },
  } = useForm<CoreBankingConfiguration>({
    resolver: yupResolver(coreBankingSchema),
    defaultValues: {
      adapterType: 'REST',
      processingMode: 'SYNC',
      messageFormat: 'JSON',
      timeoutMs: 30000,
      retryAttempts: 3,
      priority: 1,
      isActive: true,
    },
  });

  const adapterType = watch('adapterType');

  // Load configurations on component mount
  useEffect(() => {
    loadConfigurations();
    loadClearingSystems();
    loadRoutingRules();
  }, []);

  const loadConfigurations = async () => {
    try {
      setLoading(true);
      // API call to load configurations
      // const response = await api.get('/core-banking/configurations');
      // setConfigurations(response.data);
      
      // Mock data for now
      setConfigurations([
        {
          id: '1',
          tenantId: 'tenant1',
          adapterType: 'REST',
          baseUrl: 'http://localhost:8081',
          authenticationMethod: 'API_KEY',
          processingMode: 'SYNC',
          messageFormat: 'JSON',
          timeoutMs: 30000,
          retryAttempts: 3,
          priority: 1,
          isActive: true,
          bankCode: 'BANK001',
          bankName: 'Sample Bank 1',
        },
        {
          id: '2',
          tenantId: 'tenant2',
          adapterType: 'GRPC',
          grpcHost: 'localhost',
          grpcPort: 9090,
          authenticationMethod: 'CERTIFICATE',
          processingMode: 'ASYNC',
          messageFormat: 'JSON',
          timeoutMs: 30000,
          retryAttempts: 3,
          priority: 1,
          isActive: true,
          bankCode: 'BANK002',
          bankName: 'Sample Bank 2',
        },
      ]);
    } catch (err) {
      setError('Failed to load configurations');
    } finally {
      setLoading(false);
    }
  };

  const loadClearingSystems = async () => {
    try {
      // API call to load clearing systems
      // const response = await api.get('/clearing-systems');
      // setClearingSystems(response.data);
      
      // Mock data for now
      setClearingSystems([
        {
          id: '1',
          clearingSystemCode: 'CLEARING_001',
          clearingSystemName: 'Sample Clearing System 1',
          tenantId: 'tenant1',
          endpointUrl: 'https://clearing1.example.com/api',
          authenticationMethod: 'API_KEY',
          processingMode: 'ASYNC',
          messageFormat: 'XML',
          supportedPaymentTypes: ['TRANSFER', 'PAYMENT'],
          supportedCurrencies: ['USD', 'EUR'],
          localInstrumentationCodes: ['LOCAL_INSTR_001', 'LOCAL_INSTR_002'],
          isActive: true,
        },
      ]);
    } catch (err) {
      setError('Failed to load clearing systems');
    }
  };

  const loadRoutingRules = async () => {
    try {
      // API call to load routing rules
      // const response = await api.get('/payment-routing/rules');
      // setRoutingRules(response.data);
      
      // Mock data for now
      setRoutingRules([
        {
          id: '1',
          tenantId: 'tenant1',
          paymentType: 'TRANSFER',
          localInstrumentationCode: 'LOCAL_INSTR_001',
          routingType: 'SAME_BANK',
          processingMode: 'SYNC',
          messageFormat: 'JSON',
          priority: 1,
          isActive: true,
        },
        {
          id: '2',
          tenantId: 'tenant1',
          paymentType: 'PAYMENT',
          localInstrumentationCode: 'LOCAL_INSTR_002',
          routingType: 'OTHER_BANK',
          clearingSystemCode: 'CLEARING_001',
          processingMode: 'ASYNC',
          messageFormat: 'XML',
          priority: 1,
          isActive: true,
        },
      ]);
    } catch (err) {
      setError('Failed to load routing rules');
    }
  };

  const onSubmit = async (data: CoreBankingConfiguration) => {
    try {
      setLoading(true);
      setError(null);
      
      if (editingConfig) {
        // Update existing configuration
        // await api.put(`/core-banking/configurations/${editingConfig.id}`, data);
        setConfigurations(prev => 
          prev.map(config => config.id === editingConfig.id ? { ...data, id: editingConfig.id } : config)
        );
        setSuccess('Configuration updated successfully');
      } else {
        // Create new configuration
        // const response = await api.post('/core-banking/configurations', data);
        const newConfig = { ...data, id: Date.now().toString() };
        setConfigurations(prev => [...prev, newConfig]);
        setSuccess('Configuration created successfully');
      }
      
      setDialogOpen(false);
      setEditingConfig(null);
      reset();
    } catch (err) {
      setError('Failed to save configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (config: CoreBankingConfiguration) => {
    setEditingConfig(config);
    reset(config);
    setDialogOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this configuration?')) {
      try {
        // await api.delete(`/core-banking/configurations/${id}`);
        setConfigurations(prev => prev.filter(config => config.id !== id));
        setSuccess('Configuration deleted successfully');
      } catch (err) {
        setError('Failed to delete configuration');
      }
    }
  };

  const handleTestConnection = async (config: CoreBankingConfiguration) => {
    try {
      setTestingConnection(true);
      // await api.post(`/core-banking/configurations/${config.id}/test`);
      setSuccess('Connection test successful');
    } catch (err) {
      setError('Connection test failed');
    } finally {
      setTestingConnection(false);
    }
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingConfig(null);
    reset();
  };

  const renderCoreBankingConfigurations = () => (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Core Banking Configurations</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setDialogOpen(true)}
        >
          Add Configuration
        </Button>
      </Box>

      <Grid container spacing={3}>
        {configurations.map((config) => (
          <Grid item xs={12} md={6} lg={4} key={config.id}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                  <Box>
                    <Typography variant="h6" gutterBottom>
                      {config.bankName}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {config.bankCode} - {config.tenantId}
                    </Typography>
                  </Box>
                  <Box>
                    <Chip
                      label={config.adapterType}
                      color={config.adapterType === 'REST' ? 'primary' : 'secondary'}
                      size="small"
                    />
                    <Chip
                      label={config.isActive ? 'Active' : 'Inactive'}
                      color={config.isActive ? 'success' : 'default'}
                      size="small"
                      sx={{ ml: 1 }}
                    />
                  </Box>
                </Box>

                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Processing Mode:</strong> {config.processingMode}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Message Format:</strong> {config.messageFormat}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Authentication:</strong> {config.authenticationMethod}
                  </Typography>
                </Box>

                <Box display="flex" gap={1}>
                  <Button
                    size="small"
                    startIcon={<EditIcon />}
                    onClick={() => handleEdit(config)}
                  >
                    Edit
                  </Button>
                  <Button
                    size="small"
                    startIcon={<TestIcon />}
                    onClick={() => handleTestConnection(config)}
                    disabled={testingConnection}
                  >
                    Test
                  </Button>
                  <Button
                    size="small"
                    color="error"
                    startIcon={<DeleteIcon />}
                    onClick={() => handleDelete(config.id!)}
                  >
                    Delete
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );

  const renderClearingSystems = () => (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Clearing System Configurations</Typography>
        <Button variant="contained" startIcon={<AddIcon />}>
          Add Clearing System
        </Button>
      </Box>

      <Grid container spacing={3}>
        {clearingSystems.map((system) => (
          <Grid item xs={12} md={6} key={system.id}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                  <Box>
                    <Typography variant="h6" gutterBottom>
                      {system.clearingSystemName}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {system.clearingSystemCode} - {system.tenantId}
                    </Typography>
                  </Box>
                  <Chip
                    label={system.isActive ? 'Active' : 'Inactive'}
                    color={system.isActive ? 'success' : 'default'}
                    size="small"
                  />
                </Box>

                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Endpoint:</strong> {system.endpointUrl}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Processing Mode:</strong> {system.processingMode}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Message Format:</strong> {system.messageFormat}
                  </Typography>
                </Box>

                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Supported Payment Types:</strong>
                  </Typography>
                  <Box display="flex" gap={1} flexWrap="wrap" mt={1}>
                    {system.supportedPaymentTypes.map((type) => (
                      <Chip key={type} label={type} size="small" />
                    ))}
                  </Box>
                </Box>

                <Box display="flex" gap={1}>
                  <Button size="small" startIcon={<EditIcon />}>
                    Edit
                  </Button>
                  <Button size="small" startIcon={<TestIcon />}>
                    Test
                  </Button>
                  <Button size="small" color="error" startIcon={<DeleteIcon />}>
                    Delete
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );

  const renderPaymentRoutingRules = () => (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Payment Routing Rules</Typography>
        <Button variant="contained" startIcon={<AddIcon />}>
          Add Routing Rule
        </Button>
      </Box>

      <Grid container spacing={3}>
        {routingRules.map((rule) => (
          <Grid item xs={12} key={rule.id}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                  <Box>
                    <Typography variant="h6" gutterBottom>
                      {rule.paymentType} - {rule.localInstrumentationCode}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {rule.tenantId}
                    </Typography>
                  </Box>
                  <Box>
                    <Chip
                      label={rule.routingType}
                      color={rule.routingType === 'SAME_BANK' ? 'success' : 'primary'}
                      size="small"
                    />
                    <Chip
                      label={rule.isActive ? 'Active' : 'Inactive'}
                      color={rule.isActive ? 'success' : 'default'}
                      size="small"
                      sx={{ ml: 1 }}
                    />
                  </Box>
                </Box>

                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Processing Mode:</strong> {rule.processingMode}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Message Format:</strong> {rule.messageFormat}
                  </Typography>
                  {rule.clearingSystemCode && (
                    <Typography variant="body2" color="text.secondary">
                      <strong>Clearing System:</strong> {rule.clearingSystemCode}
                    </Typography>
                  )}
                </Box>

                <Box display="flex" gap={1}>
                  <Button size="small" startIcon={<EditIcon />}>
                    Edit
                  </Button>
                  <Button size="small" color="error" startIcon={<DeleteIcon />}>
                    Delete
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Core Banking Configuration
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Configure core banking integration, clearing systems, and payment routing rules.
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={(e, newValue) => setActiveTab(newValue)}>
            <Tab label="Core Banking" icon={<ApiIcon />} />
            <Tab label="Clearing Systems" icon={<SettingsIcon />} />
            <Tab label="Payment Routing" icon={<SecurityIcon />} />
          </Tabs>
        </Box>

        <Box sx={{ p: 3 }}>
          {activeTab === 0 && renderCoreBankingConfigurations()}
          {activeTab === 1 && renderClearingSystems()}
          {activeTab === 2 && renderPaymentRoutingRules()}
        </Box>
      </Card>

      {/* Configuration Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingConfig ? 'Edit Core Banking Configuration' : 'Add Core Banking Configuration'}
        </DialogTitle>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogContent>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="tenantId"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Tenant ID"
                      fullWidth
                      error={!!errors.tenantId}
                      helperText={errors.tenantId?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="adapterType"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!errors.adapterType}>
                      <InputLabel>Adapter Type</InputLabel>
                      <Select {...field} label="Adapter Type">
                        <MenuItem value="REST">REST</MenuItem>
                        <MenuItem value="GRPC">gRPC</MenuItem>
                        <MenuItem value="INTERNAL">Internal</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>

              {adapterType === 'REST' && (
                <>
                  <Grid item xs={12}>
                    <Controller
                      name="baseUrl"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Base URL"
                          fullWidth
                          error={!!errors.baseUrl}
                          helperText={errors.baseUrl?.message}
                        />
                      )}
                    />
                  </Grid>
                </>
              )}

              {adapterType === 'GRPC' && (
                <>
                  <Grid item xs={12} sm={6}>
                    <Controller
                      name="grpcHost"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="gRPC Host"
                          fullWidth
                          error={!!errors.grpcHost}
                          helperText={errors.grpcHost?.message}
                        />
                      )}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Controller
                      name="grpcPort"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="gRPC Port"
                          type="number"
                          fullWidth
                          error={!!errors.grpcPort}
                          helperText={errors.grpcPort?.message}
                        />
                      )}
                    />
                  </Grid>
                </>
              )}

              <Grid item xs={12} sm={6}>
                <Controller
                  name="authenticationMethod"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!errors.authenticationMethod}>
                      <InputLabel>Authentication Method</InputLabel>
                      <Select {...field} label="Authentication Method">
                        <MenuItem value="API_KEY">API Key</MenuItem>
                        <MenuItem value="BASIC_AUTH">Basic Auth</MenuItem>
                        <MenuItem value="CERTIFICATE">Certificate</MenuItem>
                        <MenuItem value="OAUTH2">OAuth2</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="processingMode"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!errors.processingMode}>
                      <InputLabel>Processing Mode</InputLabel>
                      <Select {...field} label="Processing Mode">
                        <MenuItem value="SYNC">Synchronous</MenuItem>
                        <MenuItem value="ASYNC">Asynchronous</MenuItem>
                        <MenuItem value="BATCH">Batch</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="messageFormat"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!errors.messageFormat}>
                      <InputLabel>Message Format</InputLabel>
                      <Select {...field} label="Message Format">
                        <MenuItem value="JSON">JSON</MenuItem>
                        <MenuItem value="XML">XML</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="timeoutMs"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Timeout (ms)"
                      type="number"
                      fullWidth
                      error={!!errors.timeoutMs}
                      helperText={errors.timeoutMs?.message}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="retryAttempts"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Retry Attempts"
                      type="number"
                      fullWidth
                      error={!!errors.retryAttempts}
                      helperText={errors.retryAttempts?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="priority"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Priority"
                      type="number"
                      fullWidth
                      error={!!errors.priority}
                      helperText={errors.priority?.message}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="bankCode"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Bank Code"
                      fullWidth
                      error={!!errors.bankCode}
                      helperText={errors.bankCode?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="bankName"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Bank Name"
                      fullWidth
                      error={!!errors.bankName}
                      helperText={errors.bankName?.message}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Controller
                  name="isActive"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Active"
                    />
                  )}
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog} startIcon={<CancelIcon />}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              startIcon={<SaveIcon />}
              disabled={loading}
            >
              {editingConfig ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default CoreBankingConfiguration;