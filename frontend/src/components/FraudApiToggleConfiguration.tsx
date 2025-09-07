import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Grid,
  Chip,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  Snackbar,
  Tabs,
  Tab,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Divider,
  Tooltip,
  Badge,
  LinearProgress,
  CircularProgress,
  Autocomplete,
  Stack
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  ToggleOn as ToggleOnIcon,
  ToggleOff as ToggleOffIcon,
  Schedule as ScheduleIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  ExpandMore as ExpandMoreIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  Refresh as RefreshIcon,
  History as HistoryIcon,
  TrendingUp as TrendingUpIcon,
  Timeline as TimelineIcon,
  Security as SecurityIcon,
  Api as ApiIcon
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

// Types
interface FraudApiToggleConfiguration {
  id?: string;
  tenantId: string;
  paymentType?: string;
  localInstrumentationCode?: string;
  clearingSystemCode?: string;
  isEnabled: boolean;
  enabledReason?: string;
  disabledReason?: string;
  effectiveFrom?: string;
  effectiveUntil?: string;
  priority: number;
  isActive: boolean;
  createdBy?: string;
  updatedBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`fraud-toggle-tabpanel-${index}`}
      aria-labelledby={`fraud-toggle-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

// Validation schemas
const configurationSchema = yup.object({
  tenantId: yup.string().required('Tenant ID is required'),
  isEnabled: yup.boolean().required('Enabled status is required'),
  priority: yup.number().min(1, 'Priority must be at least 1').required('Priority is required'),
  enabledReason: yup.string().when('isEnabled', {
    is: true,
    then: (schema) => schema.required('Enabled reason is required when enabled'),
    otherwise: (schema) => schema.notRequired()
  }),
  disabledReason: yup.string().when('isEnabled', {
    is: false,
    then: (schema) => schema.required('Disabled reason is required when disabled'),
    otherwise: (schema) => schema.notRequired()
  })
});

const FraudApiToggleConfiguration: React.FC = () => {
  const [configurations, setConfigurations] = useState<FraudApiToggleConfiguration[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingConfig, setEditingConfig] = useState<FraudApiToggleConfiguration | null>(null);
  const [viewingConfig, setViewingConfig] = useState<FraudApiToggleConfiguration | null>(null);
  const [tabValue, setTabValue] = useState(0);
  const [expandedConfig, setExpandedConfig] = useState<string | false>(false);
  const [checkingStatus, setCheckingStatus] = useState<string | null>(null);
  const [statusResult, setStatusResult] = useState<any>(null);

  const { control, handleSubmit, reset, watch, formState: { errors } } = useForm<FraudApiToggleConfiguration>({
    resolver: yupResolver(configurationSchema),
    defaultValues: {
      tenantId: '',
      isEnabled: true,
      priority: 100,
      isActive: true,
      enabledReason: '',
      disabledReason: ''
    }
  });

  const isEnabled = watch('isEnabled');

  // Load configurations
  const loadConfigurations = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/v1/fraud-api-toggle/configurations');
      if (!response.ok) throw new Error('Failed to load configurations');
      const data = await response.json();
      setConfigurations(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load configurations');
    } finally {
      setLoading(false);
    }
  };

  // Load data on component mount
  useEffect(() => {
    loadConfigurations();
  }, []);

  // Handle form submission
  const onSubmit = async (data: FraudApiToggleConfiguration) => {
    setLoading(true);
    try {
      const url = editingConfig 
        ? `/api/v1/fraud-api-toggle/configurations/${editingConfig.id}`
        : '/api/v1/fraud-api-toggle/configurations';
      
      const method = editingConfig ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });

      if (!response.ok) throw new Error('Failed to save configuration');

      setSuccess(editingConfig ? 'Configuration updated successfully' : 'Configuration created successfully');
      setOpenDialog(false);
      setEditingConfig(null);
      reset();
      loadConfigurations();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save configuration');
    } finally {
      setLoading(false);
    }
  };

  // Handle delete
  const handleDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this configuration?')) return;

    setLoading(true);
    try {
      const response = await fetch(`/api/v1/fraud-api-toggle/configurations/${id}`, {
        method: 'DELETE'
      });

      if (!response.ok) throw new Error('Failed to delete configuration');

      setSuccess('Configuration deleted successfully');
      loadConfigurations();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete configuration');
    } finally {
      setLoading(false);
    }
  };

  // Handle edit
  const handleEdit = (config: FraudApiToggleConfiguration) => {
    setEditingConfig(config);
    reset(config);
    setOpenDialog(true);
  };

  // Handle view
  const handleView = (config: FraudApiToggleConfiguration) => {
    setViewingConfig(config);
  };

  // Handle toggle
  const handleToggle = async (config: FraudApiToggleConfiguration) => {
    setLoading(true);
    try {
      const response = await fetch('/api/v1/fraud-api-toggle/toggle', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tenantId: config.tenantId,
          paymentType: config.paymentType,
          localInstrumentationCode: config.localInstrumentationCode,
          clearingSystemCode: config.clearingSystemCode,
          reason: `Toggled from ${config.isEnabled ? 'enabled' : 'disabled'} to ${!config.isEnabled ? 'enabled' : 'disabled'}`,
          createdBy: 'admin'
        })
      });

      if (!response.ok) throw new Error('Failed to toggle configuration');

      setSuccess('Configuration toggled successfully');
      loadConfigurations();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to toggle configuration');
    } finally {
      setLoading(false);
    }
  };

  // Handle check status
  const handleCheckStatus = async (tenantId: string, paymentType?: string, localInstrumentationCode?: string, clearingSystemCode?: string) => {
    setCheckingStatus(`${tenantId}:${paymentType}:${localInstrumentationCode}:${clearingSystemCode}`);
    try {
      const params = new URLSearchParams({ tenantId });
      if (paymentType) params.append('paymentType', paymentType);
      if (localInstrumentationCode) params.append('localInstrumentationCode', localInstrumentationCode);
      if (clearingSystemCode) params.append('clearingSystemCode', clearingSystemCode);

      const response = await fetch(`/api/v1/fraud-api-toggle/check?${params}`);
      if (!response.ok) throw new Error('Failed to check status');

      const result = await response.json();
      setStatusResult(result);
      setSuccess('Status checked successfully');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check status');
    } finally {
      setCheckingStatus(null);
    }
  };

  // Get configuration level
  const getConfigurationLevel = (config: FraudApiToggleConfiguration) => {
    if (config.clearingSystemCode) return 'CLEARING_SYSTEM';
    if (config.localInstrumentationCode) return 'LOCAL_INSTRUMENT';
    if (config.paymentType) return 'PAYMENT_TYPE';
    return 'TENANT';
  };

  // Get configuration level color
  const getConfigurationLevelColor = (level: string) => {
    switch (level) {
      case 'TENANT': return 'primary';
      case 'PAYMENT_TYPE': return 'secondary';
      case 'LOCAL_INSTRUMENT': return 'success';
      case 'CLEARING_SYSTEM': return 'warning';
      default: return 'default';
    }
  };

  // Get status color
  const getStatusColor = (isEnabled: boolean) => {
    return isEnabled ? 'success' : 'error';
  };

  // Get status icon
  const getStatusIcon = (isEnabled: boolean) => {
    return isEnabled ? <CheckCircleIcon /> : <CancelIcon />;
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom>
          <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
          Fraud API Toggle Configuration
        </Typography>

        <Tabs value={tabValue} onChange={(e, newValue) => setTabValue(newValue)} sx={{ mb: 3 }}>
          <Tab label="Configurations" icon={<ApiIcon />} />
          <Tab label="Status Check" icon={<CheckCircleIcon />} />
          <Tab label="Statistics" icon={<TrendingUpIcon />} />
        </Tabs>

        {/* Configurations Tab */}
        <TabPanel value={tabValue} index={0}>
          <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6">Fraud API Toggle Configurations</Typography>
            <Stack direction="row" spacing={2}>
              <Button
                variant="outlined"
                startIcon={<RefreshIcon />}
                onClick={loadConfigurations}
              >
                Refresh
              </Button>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => {
                  setEditingConfig(null);
                  reset();
                  setOpenDialog(true);
                }}
              >
                Add Configuration
              </Button>
            </Stack>
          </Box>

          {loading && <LinearProgress sx={{ mb: 2 }} />}

          {configurations.map((config) => (
            <Accordion
              key={config.id}
              expanded={expandedConfig === config.id}
              onChange={(e, isExpanded) => setExpandedConfig(isExpanded ? config.id || false : false)}
              sx={{ mb: 2 }}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="h6">
                      {config.tenantId}
                      {config.paymentType && ` - ${config.paymentType}`}
                      {config.localInstrumentationCode && ` - ${config.localInstrumentationCode}`}
                      {config.clearingSystemCode && ` - ${config.clearingSystemCode}`}
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
                      <Chip
                        label={getConfigurationLevel(config)}
                        size="small"
                        color={getConfigurationLevelColor(getConfigurationLevel(config))}
                        variant="outlined"
                      />
                      <Chip
                        label={config.isEnabled ? 'Enabled' : 'Disabled'}
                        size="small"
                        color={getStatusColor(config.isEnabled)}
                        icon={getStatusIcon(config.isEnabled)}
                      />
                      <Chip
                        label={`Priority: ${config.priority}`}
                        size="small"
                        variant="outlined"
                      />
                      <Chip
                        label={config.isActive ? 'Active' : 'Inactive'}
                        size="small"
                        color={config.isActive ? 'success' : 'default'}
                      />
                    </Box>
                  </Box>
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <Tooltip title="Check Status">
                      <IconButton 
                        onClick={(e) => { 
                          e.stopPropagation(); 
                          handleCheckStatus(config.tenantId, config.paymentType, config.localInstrumentationCode, config.clearingSystemCode);
                        }}
                        disabled={checkingStatus === `${config.tenantId}:${config.paymentType}:${config.localInstrumentationCode}:${config.clearingSystemCode}`}
                      >
                        {checkingStatus === `${config.tenantId}:${config.paymentType}:${config.localInstrumentationCode}:${config.clearingSystemCode}` ? 
                          <CircularProgress size={20} /> : <CheckCircleIcon />}
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Toggle">
                      <IconButton onClick={(e) => { e.stopPropagation(); handleToggle(config); }}>
                        {config.isEnabled ? <ToggleOffIcon /> : <ToggleOnIcon />}
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="View Details">
                      <IconButton onClick={(e) => { e.stopPropagation(); handleView(config); }}>
                        <ViewIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Edit">
                      <IconButton onClick={(e) => { e.stopPropagation(); handleEdit(config); }}>
                        <EditIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton onClick={(e) => { e.stopPropagation(); handleDelete(config.id!); }}>
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </Box>
              </AccordionSummary>
              <AccordionDetails>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" gutterBottom>Configuration Details</Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Tenant ID:</strong> {config.tenantId}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Payment Type:</strong> {config.paymentType || 'All'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Local Instrument Code:</strong> {config.localInstrumentationCode || 'All'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Clearing System Code:</strong> {config.clearingSystemCode || 'All'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Priority:</strong> {config.priority}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" gutterBottom>Status & Timing</Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Status:</strong> {config.isEnabled ? 'Enabled' : 'Disabled'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Reason:</strong> {config.isEnabled ? config.enabledReason : config.disabledReason}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Effective From:</strong> {config.effectiveFrom ? new Date(config.effectiveFrom).toLocaleString() : 'Immediately'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Effective Until:</strong> {config.effectiveUntil ? new Date(config.effectiveUntil).toLocaleString() : 'Never'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      <strong>Created:</strong> {config.createdAt ? new Date(config.createdAt).toLocaleString() : 'N/A'}
                    </Typography>
                  </Grid>
                </Grid>
              </AccordionDetails>
            </Accordion>
          ))}
        </TabPanel>

        {/* Status Check Tab */}
        <TabPanel value={tabValue} index={1}>
          <Typography variant="h6" gutterBottom>Check Fraud API Status</Typography>
          
          {statusResult && (
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>Status Check Result</Typography>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2">
                      <strong>Tenant ID:</strong> {statusResult.tenantId}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Payment Type:</strong> {statusResult.paymentType}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Local Instrument Code:</strong> {statusResult.localInstrumentationCode}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Clearing System Code:</strong> {statusResult.clearingSystemCode}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2">
                      <strong>Status:</strong> 
                      <Chip
                        label={statusResult.isEnabled ? 'Enabled' : 'Disabled'}
                        color={statusResult.isEnabled ? 'success' : 'error'}
                        icon={statusResult.isEnabled ? <CheckCircleIcon /> : <CancelIcon />}
                        sx={{ ml: 1 }}
                      />
                    </Typography>
                    <Typography variant="body2">
                      <strong>Checked At:</strong> {new Date(statusResult.checkedAt).toLocaleString()}
                    </Typography>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          )}

          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Quick Status Check</Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={3}>
                  <TextField
                    label="Tenant ID"
                    fullWidth
                    placeholder="Enter tenant ID"
                    id="quick-check-tenant"
                  />
                </Grid>
                <Grid item xs={12} md={3}>
                  <TextField
                    label="Payment Type"
                    fullWidth
                    placeholder="Optional"
                    id="quick-check-payment-type"
                  />
                </Grid>
                <Grid item xs={12} md={3}>
                  <TextField
                    label="Local Instrument Code"
                    fullWidth
                    placeholder="Optional"
                    id="quick-check-local-instrument"
                  />
                </Grid>
                <Grid item xs={12} md={3}>
                  <TextField
                    label="Clearing System Code"
                    fullWidth
                    placeholder="Optional"
                    id="quick-check-clearing-system"
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    startIcon={<CheckCircleIcon />}
                    onClick={() => {
                      const tenantId = (document.getElementById('quick-check-tenant') as HTMLInputElement)?.value;
                      const paymentType = (document.getElementById('quick-check-payment-type') as HTMLInputElement)?.value;
                      const localInstrumentationCode = (document.getElementById('quick-check-local-instrument') as HTMLInputElement)?.value;
                      const clearingSystemCode = (document.getElementById('quick-check-clearing-system') as HTMLInputElement)?.value;
                      
                      if (tenantId) {
                        handleCheckStatus(tenantId, paymentType || undefined, localInstrumentationCode || undefined, clearingSystemCode || undefined);
                      }
                    }}
                  >
                    Check Status
                  </Button>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </TabPanel>

        {/* Statistics Tab */}
        <TabPanel value={tabValue} index={2}>
          <Typography variant="h6" gutterBottom>Fraud API Toggle Statistics</Typography>
          <Grid container spacing={3}>
            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <CheckCircleIcon color="success" sx={{ mr: 1 }} />
                    <Box>
                      <Typography variant="h4">75%</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Enabled Rate
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <CancelIcon color="error" sx={{ mr: 1 }} />
                    <Box>
                      <Typography variant="h4">25%</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Disabled Rate
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <InfoIcon color="info" sx={{ mr: 1 }} />
                    <Box>
                      <Typography variant="h4">12</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Total Configurations
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <ScheduleIcon color="warning" sx={{ mr: 1 }} />
                    <Box>
                      <Typography variant="h4">3</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Scheduled Changes
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>

        {/* Configuration Dialog */}
        <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="md" fullWidth>
          <DialogTitle>
            {editingConfig ? 'Edit Configuration' : 'Add New Configuration'}
          </DialogTitle>
          <form onSubmit={handleSubmit(onSubmit)}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
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
                <Grid item xs={12} md={6}>
                  <Controller
                    name="paymentType"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Payment Type"
                        fullWidth
                        placeholder="Optional - leave empty for all payment types"
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <Controller
                    name="localInstrumentationCode"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Local Instrumentation Code"
                        fullWidth
                        placeholder="Optional - leave empty for all local instruments"
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <Controller
                    name="clearingSystemCode"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Clearing System Code"
                        fullWidth
                        placeholder="Optional - leave empty for all clearing systems"
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
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
                <Grid item xs={12} md={6}>
                  <Controller
                    name="isEnabled"
                    control={control}
                    render={({ field }) => (
                      <FormControlLabel
                        control={<Switch {...field} checked={field.value} />}
                        label="Enabled"
                      />
                    )}
                  />
                </Grid>
                {isEnabled && (
                  <Grid item xs={12}>
                    <Controller
                      name="enabledReason"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Enabled Reason"
                          fullWidth
                          multiline
                          rows={2}
                          error={!!errors.enabledReason}
                          helperText={errors.enabledReason?.message}
                        />
                      )}
                    />
                  </Grid>
                )}
                {!isEnabled && (
                  <Grid item xs={12}>
                    <Controller
                      name="disabledReason"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Disabled Reason"
                          fullWidth
                          multiline
                          rows={2}
                          error={!!errors.disabledReason}
                          helperText={errors.disabledReason?.message}
                        />
                      )}
                    />
                  </Grid>
                )}
                <Grid item xs={12} md={6}>
                  <Controller
                    name="effectiveFrom"
                    control={control}
                    render={({ field }) => (
                      <DateTimePicker
                        label="Effective From (Optional)"
                        value={field.value ? new Date(field.value) : null}
                        onChange={(date) => field.onChange(date?.toISOString())}
                        renderInput={(params) => <TextField {...params} fullWidth />}
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <Controller
                    name="effectiveUntil"
                    control={control}
                    render={({ field }) => (
                      <DateTimePicker
                        label="Effective Until (Optional)"
                        value={field.value ? new Date(field.value) : null}
                        onChange={(date) => field.onChange(date?.toISOString())}
                        renderInput={(params) => <TextField {...params} fullWidth />}
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
              <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
              <Button type="submit" variant="contained" disabled={loading}>
                {loading ? 'Saving...' : 'Save'}
              </Button>
            </DialogActions>
          </form>
        </Dialog>

        {/* View Configuration Dialog */}
        <Dialog open={!!viewingConfig} onClose={() => setViewingConfig(null)} maxWidth="lg" fullWidth>
          <DialogTitle>Configuration Details</DialogTitle>
          <DialogContent>
            {viewingConfig && (
              <Box>
                <Typography variant="h6" gutterBottom>Configuration Information</Typography>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2"><strong>Tenant ID:</strong> {viewingConfig.tenantId}</Typography>
                    <Typography variant="body2"><strong>Payment Type:</strong> {viewingConfig.paymentType || 'All'}</Typography>
                    <Typography variant="body2"><strong>Local Instrument Code:</strong> {viewingConfig.localInstrumentationCode || 'All'}</Typography>
                    <Typography variant="body2"><strong>Clearing System Code:</strong> {viewingConfig.clearingSystemCode || 'All'}</Typography>
                    <Typography variant="body2"><strong>Priority:</strong> {viewingConfig.priority}</Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2"><strong>Status:</strong> {viewingConfig.isEnabled ? 'Enabled' : 'Disabled'}</Typography>
                    <Typography variant="body2"><strong>Reason:</strong> {viewingConfig.isEnabled ? viewingConfig.enabledReason : viewingConfig.disabledReason}</Typography>
                    <Typography variant="body2"><strong>Effective From:</strong> {viewingConfig.effectiveFrom ? new Date(viewingConfig.effectiveFrom).toLocaleString() : 'Immediately'}</Typography>
                    <Typography variant="body2"><strong>Effective Until:</strong> {viewingConfig.effectiveUntil ? new Date(viewingConfig.effectiveUntil).toLocaleString() : 'Never'}</Typography>
                    <Typography variant="body2"><strong>Active:</strong> {viewingConfig.isActive ? 'Yes' : 'No'}</Typography>
                  </Grid>
                </Grid>
                
                <Divider sx={{ my: 2 }} />
                
                <Typography variant="h6" gutterBottom>Audit Information</Typography>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2"><strong>Created By:</strong> {viewingConfig.createdBy || 'N/A'}</Typography>
                    <Typography variant="body2"><strong>Created At:</strong> {viewingConfig.createdAt ? new Date(viewingConfig.createdAt).toLocaleString() : 'N/A'}</Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2"><strong>Updated By:</strong> {viewingConfig.updatedBy || 'N/A'}</Typography>
                    <Typography variant="body2"><strong>Updated At:</strong> {viewingConfig.updatedAt ? new Date(viewingConfig.updatedAt).toLocaleString() : 'N/A'}</Typography>
                  </Grid>
                </Grid>
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setViewingConfig(null)}>Close</Button>
          </DialogActions>
        </Dialog>

        {/* Snackbars */}
        <Snackbar open={!!error} autoHideDuration={6000} onClose={() => setError(null)}>
          <Alert onClose={() => setError(null)} severity="error">
            {error}
          </Alert>
        </Snackbar>
        
        <Snackbar open={!!success} autoHideDuration={6000} onClose={() => setSuccess(null)}>
          <Alert onClose={() => setSuccess(null)} severity="success">
            {success}
          </Alert>
        </Snackbar>
      </Box>
    </LocalizationProvider>
  );
};

export default FraudApiToggleConfiguration;