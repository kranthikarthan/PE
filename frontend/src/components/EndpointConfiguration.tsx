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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Tooltip,
  Badge,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  ExpandMore as ExpandMoreIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  TestTube as TestIcon,
  Api as ApiIcon,
  Settings as SettingsIcon,
  Code as CodeIcon,
  Schema as SchemaIcon,
  Transform as TransformIcon,
  Visibility as ViewIcon,
  PlayArrow as PlayIcon,
} from '@mui/icons-material';
import { useForm, Controller, useFieldArray } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import JSONEditor from 'react-json-editor-ajrm';
import locale from 'react-json-editor-ajrm/locale/en';

// Types
interface EndpointConfiguration {
  id?: string;
  coreBankingConfigId: string;
  endpointName: string;
  endpointType: string;
  httpMethod: string;
  endpointPath: string;
  baseUrlOverride?: string;
  requestHeaders?: Record<string, string>;
  queryParameters?: Record<string, string>;
  authenticationConfig?: Record<string, any>;
  timeoutMs?: number;
  retryAttempts?: number;
  circuitBreakerConfig?: Record<string, any>;
  rateLimitingConfig?: Record<string, any>;
  requestTransformationConfig?: Record<string, any>;
  responseTransformationConfig?: Record<string, any>;
  validationRules?: Record<string, any>;
  errorHandlingConfig?: Record<string, any>;
  priority: number;
  isActive: boolean;
  description?: string;
}

interface PayloadSchemaMapping {
  id?: string;
  endpointConfigId: string;
  mappingName: string;
  mappingType: string;
  direction: string;
  sourceSchema?: Record<string, any>;
  targetSchema?: Record<string, any>;
  fieldMappings?: Record<string, any>;
  transformationRules?: Record<string, any>;
  validationRules?: Record<string, any>;
  defaultValues?: Record<string, any>;
  conditionalMappings?: Record<string, any>;
  arrayHandlingConfig?: Record<string, any>;
  nestedObjectConfig?: Record<string, any>;
  errorHandlingConfig?: Record<string, any>;
  version: string;
  priority: number;
  isActive: boolean;
  description?: string;
}

interface CoreBankingConfiguration {
  id: string;
  tenantId: string;
  adapterType: string;
  bankName: string;
  bankCode: string;
}

// Validation schemas
const endpointSchema = yup.object({
  coreBankingConfigId: yup.string().required('Core Banking Configuration is required'),
  endpointName: yup.string().required('Endpoint name is required'),
  endpointType: yup.string().required('Endpoint type is required'),
  httpMethod: yup.string().required('HTTP method is required'),
  endpointPath: yup.string().required('Endpoint path is required'),
  timeoutMs: yup.number().min(1000).max(300000),
  retryAttempts: yup.number().min(0).max(10),
  priority: yup.number().min(1).max(100),
});

const payloadMappingSchema = yup.object({
  endpointConfigId: yup.string().required('Endpoint Configuration is required'),
  mappingName: yup.string().required('Mapping name is required'),
  mappingType: yup.string().required('Mapping type is required'),
  direction: yup.string().required('Direction is required'),
  version: yup.string().required('Version is required'),
  priority: yup.number().min(1).max(100),
});

const EndpointConfiguration: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [endpoints, setEndpoints] = useState<EndpointConfiguration[]>([]);
  const [payloadMappings, setPayloadMappings] = useState<PayloadSchemaMapping[]>([]);
  const [coreBankingConfigs, setCoreBankingConfigs] = useState<CoreBankingConfiguration[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [endpointDialogOpen, setEndpointDialogOpen] = useState(false);
  const [mappingDialogOpen, setMappingDialogOpen] = useState(false);
  const [editingEndpoint, setEditingEndpoint] = useState<EndpointConfiguration | null>(null);
  const [editingMapping, setEditingMapping] = useState<PayloadSchemaMapping | null>(null);
  const [testingEndpoint, setTestingEndpoint] = useState(false);
  const [selectedEndpoint, setSelectedEndpoint] = useState<string | null>(null);

  const {
    control: endpointControl,
    handleSubmit: handleEndpointSubmit,
    reset: resetEndpoint,
    watch: watchEndpoint,
    formState: { errors: endpointErrors, isDirty: endpointIsDirty },
  } = useForm<EndpointConfiguration>({
    resolver: yupResolver(endpointSchema),
    defaultValues: {
      httpMethod: 'POST',
      timeoutMs: 30000,
      retryAttempts: 3,
      priority: 1,
      isActive: true,
    },
  });

  const {
    control: mappingControl,
    handleSubmit: handleMappingSubmit,
    reset: resetMapping,
    watch: watchMapping,
    formState: { errors: mappingErrors, isDirty: mappingIsDirty },
  } = useForm<PayloadSchemaMapping>({
    resolver: yupResolver(payloadMappingSchema),
    defaultValues: {
      mappingType: 'FIELD_MAPPING',
      direction: 'REQUEST',
      version: '1.0',
      priority: 1,
      isActive: true,
    },
  });

  // Load data on component mount
  useEffect(() => {
    loadCoreBankingConfigs();
    loadEndpoints();
    loadPayloadMappings();
  }, []);

  const loadCoreBankingConfigs = async () => {
    try {
      // Mock data for now
      setCoreBankingConfigs([
        {
          id: '1',
          tenantId: 'tenant1',
          adapterType: 'REST',
          bankName: 'Sample Bank 1',
          bankCode: 'BANK001',
        },
        {
          id: '2',
          tenantId: 'tenant2',
          adapterType: 'GRPC',
          bankName: 'Sample Bank 2',
          bankCode: 'BANK002',
        },
      ]);
    } catch (err) {
      setError('Failed to load core banking configurations');
    }
  };

  const loadEndpoints = async () => {
    try {
      // Mock data for now
      setEndpoints([
        {
          id: '1',
          coreBankingConfigId: '1',
          endpointName: 'Get Account Info',
          endpointType: 'ACCOUNT_INFO',
          httpMethod: 'GET',
          endpointPath: '/api/v1/accounts/{accountNumber}',
          requestHeaders: { 'Content-Type': 'application/json' },
          timeoutMs: 30000,
          retryAttempts: 3,
          priority: 1,
          isActive: true,
          description: 'Get account information endpoint',
        },
        {
          id: '2',
          coreBankingConfigId: '1',
          endpointName: 'Process Debit Transaction',
          endpointType: 'DEBIT_TRANSACTION',
          httpMethod: 'POST',
          endpointPath: '/api/v1/transactions/debit',
          requestHeaders: { 'Content-Type': 'application/json' },
          timeoutMs: 30000,
          retryAttempts: 3,
          priority: 1,
          isActive: true,
          description: 'Process debit transaction endpoint',
        },
      ]);
    } catch (err) {
      setError('Failed to load endpoints');
    }
  };

  const loadPayloadMappings = async () => {
    try {
      // Mock data for now
      setPayloadMappings([
        {
          id: '1',
          endpointConfigId: '1',
          mappingName: 'Account Info Request Mapping',
          mappingType: 'FIELD_MAPPING',
          direction: 'REQUEST',
          fieldMappings: {
            accountNumber: 'accountNumber',
            tenantId: 'tenantId',
          },
          validationRules: {
            accountNumber: { required: true, type: 'string' },
            tenantId: { required: true, type: 'string' },
          },
          version: '1.0',
          priority: 1,
          isActive: true,
          description: 'Mapping for account info request payload',
        },
        {
          id: '2',
          endpointConfigId: '1',
          mappingName: 'Account Info Response Mapping',
          mappingType: 'FIELD_MAPPING',
          direction: 'RESPONSE',
          fieldMappings: {
            accountNumber: 'accountNumber',
            accountName: 'accountName',
            balance: 'balance',
            status: 'status',
          },
          version: '1.0',
          priority: 1,
          isActive: true,
          description: 'Mapping for account info response payload',
        },
      ]);
    } catch (err) {
      setError('Failed to load payload mappings');
    }
  };

  const onEndpointSubmit = async (data: EndpointConfiguration) => {
    try {
      setLoading(true);
      setError(null);

      if (editingEndpoint) {
        // Update existing endpoint
        setEndpoints(prev =>
          prev.map(endpoint => endpoint.id === editingEndpoint.id ? { ...data, id: editingEndpoint.id } : endpoint)
        );
        setSuccess('Endpoint configuration updated successfully');
      } else {
        // Create new endpoint
        const newEndpoint = { ...data, id: Date.now().toString() };
        setEndpoints(prev => [...prev, newEndpoint]);
        setSuccess('Endpoint configuration created successfully');
      }

      setEndpointDialogOpen(false);
      setEditingEndpoint(null);
      resetEndpoint();
    } catch (err) {
      setError('Failed to save endpoint configuration');
    } finally {
      setLoading(false);
    }
  };

  const onMappingSubmit = async (data: PayloadSchemaMapping) => {
    try {
      setLoading(true);
      setError(null);

      if (editingMapping) {
        // Update existing mapping
        setPayloadMappings(prev =>
          prev.map(mapping => mapping.id === editingMapping.id ? { ...data, id: editingMapping.id } : mapping)
        );
        setSuccess('Payload mapping updated successfully');
      } else {
        // Create new mapping
        const newMapping = { ...data, id: Date.now().toString() };
        setPayloadMappings(prev => [...prev, newMapping]);
        setSuccess('Payload mapping created successfully');
      }

      setMappingDialogOpen(false);
      setEditingMapping(null);
      resetMapping();
    } catch (err) {
      setError('Failed to save payload mapping');
    } finally {
      setLoading(false);
    }
  };

  const handleEditEndpoint = (endpoint: EndpointConfiguration) => {
    setEditingEndpoint(endpoint);
    resetEndpoint(endpoint);
    setEndpointDialogOpen(true);
  };

  const handleEditMapping = (mapping: PayloadSchemaMapping) => {
    setEditingMapping(mapping);
    resetMapping(mapping);
    setMappingDialogOpen(true);
  };

  const handleDeleteEndpoint = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this endpoint configuration?')) {
      try {
        setEndpoints(prev => prev.filter(endpoint => endpoint.id !== id));
        setPayloadMappings(prev => prev.filter(mapping => mapping.endpointConfigId !== id));
        setSuccess('Endpoint configuration deleted successfully');
      } catch (err) {
        setError('Failed to delete endpoint configuration');
      }
    }
  };

  const handleDeleteMapping = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this payload mapping?')) {
      try {
        setPayloadMappings(prev => prev.filter(mapping => mapping.id !== id));
        setSuccess('Payload mapping deleted successfully');
      } catch (err) {
        setError('Failed to delete payload mapping');
      }
    }
  };

  const handleTestEndpoint = async (endpoint: EndpointConfiguration) => {
    try {
      setTestingEndpoint(true);
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 2000));
      setSuccess('Endpoint test successful');
    } catch (err) {
      setError('Endpoint test failed');
    } finally {
      setTestingEndpoint(false);
    }
  };

  const handleCloseEndpointDialog = () => {
    setEndpointDialogOpen(false);
    setEditingEndpoint(null);
    resetEndpoint();
  };

  const handleCloseMappingDialog = () => {
    setMappingDialogOpen(false);
    setEditingMapping(null);
    resetMapping();
  };

  const getEndpointTypeColor = (type: string) => {
    const colors: Record<string, 'primary' | 'secondary' | 'success' | 'warning' | 'error'> = {
      'ACCOUNT_INFO': 'primary',
      'DEBIT_TRANSACTION': 'error',
      'CREDIT_TRANSACTION': 'success',
      'TRANSFER_TRANSACTION': 'warning',
      'ISO20022_PAYMENT': 'secondary',
    };
    return colors[type] || 'default';
  };

  const getDirectionColor = (direction: string) => {
    const colors: Record<string, 'primary' | 'secondary' | 'success'> = {
      'REQUEST': 'primary',
      'RESPONSE': 'secondary',
      'BIDIRECTIONAL': 'success',
    };
    return colors[direction] || 'default';
  };

  const renderEndpoints = () => (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Endpoint Configurations</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setEndpointDialogOpen(true)}
        >
          Add Endpoint
        </Button>
      </Box>

      <Grid container spacing={3}>
        {endpoints.map((endpoint) => (
          <Grid item xs={12} md={6} lg={4} key={endpoint.id}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                  <Box>
                    <Typography variant="h6" gutterBottom>
                      {endpoint.endpointName}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {endpoint.httpMethod} {endpoint.endpointPath}
                    </Typography>
                  </Box>
                  <Box>
                    <Chip
                      label={endpoint.endpointType}
                      color={getEndpointTypeColor(endpoint.endpointType)}
                      size="small"
                    />
                    <Chip
                      label={endpoint.isActive ? 'Active' : 'Inactive'}
                      color={endpoint.isActive ? 'success' : 'default'}
                      size="small"
                      sx={{ ml: 1 }}
                    />
                  </Box>
                </Box>

                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Timeout:</strong> {endpoint.timeoutMs}ms
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Retry Attempts:</strong> {endpoint.retryAttempts}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Priority:</strong> {endpoint.priority}
                  </Typography>
                </Box>

                {endpoint.description && (
                  <Typography variant="body2" color="text.secondary" mb={2}>
                    {endpoint.description}
                  </Typography>
                )}

                <Box display="flex" gap={1}>
                  <Button
                    size="small"
                    startIcon={<EditIcon />}
                    onClick={() => handleEditEndpoint(endpoint)}
                  >
                    Edit
                  </Button>
                  <Button
                    size="small"
                    startIcon={<TestIcon />}
                    onClick={() => handleTestEndpoint(endpoint)}
                    disabled={testingEndpoint}
                  >
                    Test
                  </Button>
                  <Button
                    size="small"
                    startIcon={<ViewIcon />}
                    onClick={() => setSelectedEndpoint(endpoint.id!)}
                  >
                    Mappings
                  </Button>
                  <Button
                    size="small"
                    color="error"
                    startIcon={<DeleteIcon />}
                    onClick={() => handleDeleteEndpoint(endpoint.id!)}
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

  const renderPayloadMappings = () => {
    const filteredMappings = selectedEndpoint
      ? payloadMappings.filter(mapping => mapping.endpointConfigId === selectedEndpoint)
      : payloadMappings;

    return (
      <Box>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Typography variant="h6">
            Payload Schema Mappings
            {selectedEndpoint && (
              <Chip
                label={`Filtered by endpoint`}
                size="small"
                sx={{ ml: 2 }}
                onDelete={() => setSelectedEndpoint(null)}
              />
            )}
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setMappingDialogOpen(true)}
          >
            Add Mapping
          </Button>
        </Box>

        <Grid container spacing={3}>
          {filteredMappings.map((mapping) => (
            <Grid item xs={12} md={6} key={mapping.id}>
              <Card>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                    <Box>
                      <Typography variant="h6" gutterBottom>
                        {mapping.mappingName}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        v{mapping.version}
                      </Typography>
                    </Box>
                    <Box>
                      <Chip
                        label={mapping.mappingType}
                        color="primary"
                        size="small"
                      />
                      <Chip
                        label={mapping.direction}
                        color={getDirectionColor(mapping.direction)}
                        size="small"
                        sx={{ ml: 1 }}
                      />
                      <Chip
                        label={mapping.isActive ? 'Active' : 'Inactive'}
                        color={mapping.isActive ? 'success' : 'default'}
                        size="small"
                        sx={{ ml: 1 }}
                      />
                    </Box>
                  </Box>

                  <Box mb={2}>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Priority:</strong> {mapping.priority}
                    </Typography>
                    {mapping.fieldMappings && (
                      <Typography variant="body2" color="text.secondary">
                        <strong>Field Mappings:</strong> {Object.keys(mapping.fieldMappings).length} fields
                      </Typography>
                    )}
                  </Box>

                  {mapping.description && (
                    <Typography variant="body2" color="text.secondary" mb={2}>
                      {mapping.description}
                    </Typography>
                  )}

                  <Box display="flex" gap={1}>
                    <Button
                      size="small"
                      startIcon={<EditIcon />}
                      onClick={() => handleEditMapping(mapping)}
                    >
                      Edit
                    </Button>
                    <Button
                      size="small"
                      startIcon={<ViewIcon />}
                      onClick={() => {
                        // Show mapping details
                      }}
                    >
                      View
                    </Button>
                    <Button
                      size="small"
                      color="error"
                      startIcon={<DeleteIcon />}
                      onClick={() => handleDeleteMapping(mapping.id!)}
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
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Endpoint & Payload Configuration
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Configure endpoints and payload schema mappings for external core banking integration.
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
            <Tab label="Endpoints" icon={<ApiIcon />} />
            <Tab label="Payload Mappings" icon={<SchemaIcon />} />
          </Tabs>
        </Box>

        <Box sx={{ p: 3 }}>
          {activeTab === 0 && renderEndpoints()}
          {activeTab === 1 && renderPayloadMappings()}
        </Box>
      </Card>

      {/* Endpoint Configuration Dialog */}
      <Dialog open={endpointDialogOpen} onClose={handleCloseEndpointDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingEndpoint ? 'Edit Endpoint Configuration' : 'Add Endpoint Configuration'}
        </DialogTitle>
        <form onSubmit={handleEndpointSubmit(onEndpointSubmit)}>
          <DialogContent>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="coreBankingConfigId"
                  control={endpointControl}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!endpointErrors.coreBankingConfigId}>
                      <InputLabel>Core Banking Configuration</InputLabel>
                      <Select {...field} label="Core Banking Configuration">
                        {coreBankingConfigs.map((config) => (
                          <MenuItem key={config.id} value={config.id}>
                            {config.bankName} ({config.adapterType})
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="endpointName"
                  control={endpointControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Endpoint Name"
                      fullWidth
                      error={!!endpointErrors.endpointName}
                      helperText={endpointErrors.endpointName?.message}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="endpointType"
                  control={endpointControl}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!endpointErrors.endpointType}>
                      <InputLabel>Endpoint Type</InputLabel>
                      <Select {...field} label="Endpoint Type">
                        <MenuItem value="ACCOUNT_INFO">Account Info</MenuItem>
                        <MenuItem value="ACCOUNT_BALANCE">Account Balance</MenuItem>
                        <MenuItem value="ACCOUNT_HOLDER">Account Holder</MenuItem>
                        <MenuItem value="DEBIT_TRANSACTION">Debit Transaction</MenuItem>
                        <MenuItem value="CREDIT_TRANSACTION">Credit Transaction</MenuItem>
                        <MenuItem value="TRANSFER_TRANSACTION">Transfer Transaction</MenuItem>
                        <MenuItem value="TRANSACTION_STATUS">Transaction Status</MenuItem>
                        <MenuItem value="ISO20022_PAYMENT">ISO 20022 Payment</MenuItem>
                        <MenuItem value="ISO20022_RESPONSE">ISO 20022 Response</MenuItem>
                        <MenuItem value="HEALTH_CHECK">Health Check</MenuItem>
                        <MenuItem value="CUSTOM">Custom</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="httpMethod"
                  control={endpointControl}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!endpointErrors.httpMethod}>
                      <InputLabel>HTTP Method</InputLabel>
                      <Select {...field} label="HTTP Method">
                        <MenuItem value="GET">GET</MenuItem>
                        <MenuItem value="POST">POST</MenuItem>
                        <MenuItem value="PUT">PUT</MenuItem>
                        <MenuItem value="DELETE">DELETE</MenuItem>
                        <MenuItem value="PATCH">PATCH</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Controller
                  name="endpointPath"
                  control={endpointControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Endpoint Path"
                      fullWidth
                      error={!!endpointErrors.endpointPath}
                      helperText={endpointErrors.endpointPath?.message}
                      placeholder="/api/v1/accounts/{accountNumber}"
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="timeoutMs"
                  control={endpointControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Timeout (ms)"
                      type="number"
                      fullWidth
                      error={!!endpointErrors.timeoutMs}
                      helperText={endpointErrors.timeoutMs?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="retryAttempts"
                  control={endpointControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Retry Attempts"
                      type="number"
                      fullWidth
                      error={!!endpointErrors.retryAttempts}
                      helperText={endpointErrors.retryAttempts?.message}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="priority"
                  control={endpointControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Priority"
                      type="number"
                      fullWidth
                      error={!!endpointErrors.priority}
                      helperText={endpointErrors.priority?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="isActive"
                  control={endpointControl}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Active"
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Controller
                  name="description"
                  control={endpointControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Description"
                      fullWidth
                      multiline
                      rows={3}
                    />
                  )}
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseEndpointDialog} startIcon={<CancelIcon />}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              startIcon={<SaveIcon />}
              disabled={loading}
            >
              {editingEndpoint ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Payload Mapping Dialog */}
      <Dialog open={mappingDialogOpen} onClose={handleCloseMappingDialog} maxWidth="lg" fullWidth>
        <DialogTitle>
          {editingMapping ? 'Edit Payload Schema Mapping' : 'Add Payload Schema Mapping'}
        </DialogTitle>
        <form onSubmit={handleMappingSubmit(onMappingSubmit)}>
          <DialogContent>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="endpointConfigId"
                  control={mappingControl}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!mappingErrors.endpointConfigId}>
                      <InputLabel>Endpoint Configuration</InputLabel>
                      <Select {...field} label="Endpoint Configuration">
                        {endpoints.map((endpoint) => (
                          <MenuItem key={endpoint.id} value={endpoint.id}>
                            {endpoint.endpointName} ({endpoint.endpointType})
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="mappingName"
                  control={mappingControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Mapping Name"
                      fullWidth
                      error={!!mappingErrors.mappingName}
                      helperText={mappingErrors.mappingName?.message}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="mappingType"
                  control={mappingControl}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!mappingErrors.mappingType}>
                      <InputLabel>Mapping Type</InputLabel>
                      <Select {...field} label="Mapping Type">
                        <MenuItem value="FIELD_MAPPING">Field Mapping</MenuItem>
                        <MenuItem value="OBJECT_MAPPING">Object Mapping</MenuItem>
                        <MenuItem value="ARRAY_MAPPING">Array Mapping</MenuItem>
                        <MenuItem value="NESTED_MAPPING">Nested Mapping</MenuItem>
                        <MenuItem value="CONDITIONAL_MAPPING">Conditional Mapping</MenuItem>
                        <MenuItem value="TRANSFORMATION_MAPPING">Transformation Mapping</MenuItem>
                        <MenuItem value="CUSTOM_MAPPING">Custom Mapping</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="direction"
                  control={mappingControl}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!mappingErrors.direction}>
                      <InputLabel>Direction</InputLabel>
                      <Select {...field} label="Direction">
                        <MenuItem value="REQUEST">Request</MenuItem>
                        <MenuItem value="RESPONSE">Response</MenuItem>
                        <MenuItem value="BIDIRECTIONAL">Bidirectional</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="version"
                  control={mappingControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Version"
                      fullWidth
                      error={!!mappingErrors.version}
                      helperText={mappingErrors.version?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="priority"
                  control={mappingControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Priority"
                      type="number"
                      fullWidth
                      error={!!mappingErrors.priority}
                      helperText={mappingErrors.priority?.message}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Controller
                  name="description"
                  control={mappingControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Description"
                      fullWidth
                      multiline
                      rows={3}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Field Mappings (JSON)
                </Typography>
                <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 1 }}>
                  <JSONEditor
                    id="fieldMappings"
                    placeholder={{
                      "accountNumber": "accountNumber",
                      "tenantId": "tenantId",
                      "amount": "amount"
                    }}
                    locale={locale}
                    height="200px"
                    width="100%"
                  />
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Validation Rules (JSON)
                </Typography>
                <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 1 }}>
                  <JSONEditor
                    id="validationRules"
                    placeholder={{
                      "accountNumber": {
                        "required": true,
                        "type": "string",
                        "minLength": 1,
                        "maxLength": 50
                      }
                    }}
                    locale={locale}
                    height="200px"
                    width="100%"
                  />
                </Box>
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseMappingDialog} startIcon={<CancelIcon />}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              startIcon={<SaveIcon />}
              disabled={loading}
            >
              {editingMapping ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default EndpointConfiguration;