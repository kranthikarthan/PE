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
  CircularProgress
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  Security as SecurityIcon,
  Assessment as AssessmentIcon,
  Api as ApiIcon,
  Settings as SettingsIcon,
  ExpandMore as ExpandMoreIcon,
  Warning as WarningIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  Refresh as RefreshIcon,
  PlayArrow as TestIcon,
  History as HistoryIcon,
  TrendingUp as TrendingUpIcon,
  Timeline as TimelineIcon
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';

// Types
interface FraudRiskConfiguration {
  id?: string;
  configurationName: string;
  tenantId: string;
  paymentType?: string;
  localInstrumentationCode?: string;
  clearingSystemCode?: string;
  paymentSource: 'BANK_CLIENT' | 'CLEARING_SYSTEM' | 'BOTH';
  riskAssessmentType: 'REAL_TIME' | 'BATCH' | 'HYBRID' | 'CUSTOM';
  externalApiConfig?: any;
  riskRules?: any;
  decisionCriteria?: any;
  thresholds?: any;
  timeoutConfig?: any;
  retryConfig?: any;
  circuitBreakerConfig?: any;
  fallbackConfig?: any;
  monitoringConfig?: any;
  alertingConfig?: any;
  isEnabled: boolean;
  priority: number;
  version: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

interface FraudRiskAssessment {
  id: string;
  assessmentId: string;
  transactionReference: string;
  tenantId: string;
  paymentType?: string;
  localInstrumentationCode?: string;
  clearingSystemCode?: string;
  paymentSource: string;
  riskAssessmentType: string;
  externalApiUsed?: string;
  riskScore?: number;
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  decision: 'APPROVE' | 'REJECT' | 'MANUAL_REVIEW' | 'HOLD' | 'ESCALATE';
  decisionReason?: string;
  status: string;
  processingTimeMs?: number;
  externalApiResponseTimeMs?: number;
  assessedAt: string;
  createdAt: string;
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
      id={`fraud-tabpanel-${index}`}
      aria-labelledby={`fraud-tab-${index}`}
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
  configurationName: yup.string().required('Configuration name is required'),
  tenantId: yup.string().required('Tenant ID is required'),
  paymentSource: yup.string().required('Payment source is required'),
  riskAssessmentType: yup.string().required('Risk assessment type is required'),
  priority: yup.number().min(1, 'Priority must be at least 1').required('Priority is required'),
  version: yup.string().required('Version is required'),
  description: yup.string()
});

const FraudRiskConfiguration: React.FC = () => {
  const [configurations, setConfigurations] = useState<FraudRiskConfiguration[]>([]);
  const [assessments, setAssessments] = useState<FraudRiskAssessment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingConfig, setEditingConfig] = useState<FraudRiskConfiguration | null>(null);
  const [viewingConfig, setViewingConfig] = useState<FraudRiskConfiguration | null>(null);
  const [tabValue, setTabValue] = useState(0);
  const [expandedConfig, setExpandedConfig] = useState<string | false>(false);
  const [testingApi, setTestingApi] = useState<string | null>(null);

  const { control, handleSubmit, reset, formState: { errors } } = useForm<FraudRiskConfiguration>({
    resolver: yupResolver(configurationSchema),
    defaultValues: {
      configurationName: '',
      tenantId: '',
      paymentSource: 'BANK_CLIENT',
      riskAssessmentType: 'REAL_TIME',
      isEnabled: true,
      priority: 1,
      version: '1.0',
      description: ''
    }
  });

  // Load configurations
  const loadConfigurations = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/v1/fraud-risk/configurations');
      if (!response.ok) throw new Error('Failed to load configurations');
      const data = await response.json();
      setConfigurations(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load configurations');
    } finally {
      setLoading(false);
    }
  };

  // Load assessments
  const loadAssessments = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/v1/fraud-risk/assessments');
      if (!response.ok) throw new Error('Failed to load assessments');
      const data = await response.json();
      setAssessments(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load assessments');
    } finally {
      setLoading(false);
    }
  };

  // Load data on component mount
  useEffect(() => {
    loadConfigurations();
    loadAssessments();
  }, []);

  // Handle form submission
  const onSubmit = async (data: FraudRiskConfiguration) => {
    setLoading(true);
    try {
      const url = editingConfig 
        ? `/api/v1/fraud-risk/configurations/${editingConfig.id}`
        : '/api/v1/fraud-risk/configurations';
      
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
      const response = await fetch(`/api/v1/fraud-risk/configurations/${id}`, {
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
  const handleEdit = (config: FraudRiskConfiguration) => {
    setEditingConfig(config);
    reset(config);
    setOpenDialog(true);
  };

  // Handle view
  const handleView = (config: FraudRiskConfiguration) => {
    setViewingConfig(config);
  };

  // Handle test API
  const handleTestApi = async (config: FraudRiskConfiguration) => {
    setTestingApi(config.id || '');
    try {
      const response = await fetch(`/api/v1/fraud-risk/configurations/${config.id}/test-api`, {
        method: 'POST'
      });

      if (!response.ok) throw new Error('API test failed');

      const result = await response.json();
      setSuccess(`API test successful: ${result.message}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'API test failed');
    } finally {
      setTestingApi(null);
    }
  };

  // Handle toggle enabled
  const handleToggleEnabled = async (config: FraudRiskConfiguration) => {
    setLoading(true);
    try {
      const response = await fetch(`/api/v1/fraud-risk/configurations/${config.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...config, isEnabled: !config.isEnabled })
      });

      if (!response.ok) throw new Error('Failed to update configuration');

      setSuccess('Configuration updated successfully');
      loadConfigurations();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update configuration');
    } finally {
      setLoading(false);
    }
  };

  // Get risk level color
  const getRiskLevelColor = (level?: string) => {
    switch (level) {
      case 'LOW': return 'success';
      case 'MEDIUM': return 'warning';
      case 'HIGH': return 'error';
      case 'CRITICAL': return 'error';
      default: return 'default';
    }
  };

  // Get decision color
  const getDecisionColor = (decision: string) => {
    switch (decision) {
      case 'APPROVE': return 'success';
      case 'REJECT': return 'error';
      case 'MANUAL_REVIEW': return 'warning';
      case 'HOLD': return 'info';
      case 'ESCALATE': return 'error';
      default: return 'default';
    }
  };

  // Get status color
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'FAILED': return 'error';
      case 'ERROR': return 'error';
      case 'PENDING': return 'warning';
      case 'IN_PROGRESS': return 'info';
      default: return 'default';
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Fraud/Risk Monitoring Configuration
      </Typography>

      <Tabs value={tabValue} onChange={(e, newValue) => setTabValue(newValue)} sx={{ mb: 3 }}>
        <Tab label="Configurations" icon={<SettingsIcon />} />
        <Tab label="Assessments" icon={<AssessmentIcon />} />
        <Tab label="Statistics" icon={<TrendingUpIcon />} />
      </Tabs>

      {/* Configurations Tab */}
      <TabPanel value={tabValue} index={0}>
        <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">Fraud/Risk Configurations</Typography>
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
                  <Typography variant="h6">{config.configurationName}</Typography>
                  <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
                    <Chip
                      label={config.paymentSource}
                      size="small"
                      color="primary"
                      variant="outlined"
                    />
                    <Chip
                      label={config.riskAssessmentType}
                      size="small"
                      color="secondary"
                      variant="outlined"
                    />
                    <Chip
                      label={`Priority: ${config.priority}`}
                      size="small"
                      variant="outlined"
                    />
                    <Chip
                      label={config.isEnabled ? 'Enabled' : 'Disabled'}
                      size="small"
                      color={config.isEnabled ? 'success' : 'default'}
                    />
                  </Box>
                </Box>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Tooltip title="View Details">
                    <IconButton onClick={(e) => { e.stopPropagation(); handleView(config); }}>
                      <ViewIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Test API">
                    <IconButton 
                      onClick={(e) => { e.stopPropagation(); handleTestApi(config); }}
                      disabled={testingApi === config.id}
                    >
                      {testingApi === config.id ? <CircularProgress size={20} /> : <TestIcon />}
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
                  <Typography variant="subtitle2" gutterBottom>Basic Information</Typography>
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
                    <strong>Version:</strong> {config.version}
                  </Typography>
                </Grid>
                <Grid item xs={12} md={6}>
                  <Typography variant="subtitle2" gutterBottom>Configuration Details</Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>External API:</strong> {config.externalApiConfig?.apiName || 'None'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Risk Rules:</strong> {config.riskRules ? 'Configured' : 'None'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Decision Criteria:</strong> {config.decisionCriteria ? 'Configured' : 'None'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Thresholds:</strong> {config.thresholds ? 'Configured' : 'None'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Description:</strong> {config.description || 'None'}
                  </Typography>
                </Grid>
              </Grid>
            </AccordionDetails>
          </Accordion>
        ))}
      </TabPanel>

      {/* Assessments Tab */}
      <TabPanel value={tabValue} index={1}>
        <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">Recent Assessments</Typography>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={loadAssessments}
          >
            Refresh
          </Button>
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Assessment ID</TableCell>
                <TableCell>Transaction Reference</TableCell>
                <TableCell>Tenant ID</TableCell>
                <TableCell>Payment Source</TableCell>
                <TableCell>Risk Score</TableCell>
                <TableCell>Risk Level</TableCell>
                <TableCell>Decision</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Processing Time</TableCell>
                <TableCell>Assessed At</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {assessments.map((assessment) => (
                <TableRow key={assessment.id}>
                  <TableCell>{assessment.assessmentId}</TableCell>
                  <TableCell>{assessment.transactionReference}</TableCell>
                  <TableCell>{assessment.tenantId}</TableCell>
                  <TableCell>
                    <Chip
                      label={assessment.paymentSource}
                      size="small"
                      variant="outlined"
                    />
                  </TableCell>
                  <TableCell>
                    {assessment.riskScore ? (
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <Typography variant="body2">
                          {(assessment.riskScore * 100).toFixed(1)}%
                        </Typography>
                        <Box sx={{ ml: 1, width: 50 }}>
                          <LinearProgress
                            variant="determinate"
                            value={assessment.riskScore * 100}
                            color={getRiskLevelColor(assessment.riskLevel)}
                          />
                        </Box>
                      </Box>
                    ) : 'N/A'}
                  </TableCell>
                  <TableCell>
                    {assessment.riskLevel && (
                      <Chip
                        label={assessment.riskLevel}
                        size="small"
                        color={getRiskLevelColor(assessment.riskLevel)}
                      />
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={assessment.decision}
                      size="small"
                      color={getDecisionColor(assessment.decision)}
                    />
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={assessment.status}
                      size="small"
                      color={getStatusColor(assessment.status)}
                    />
                  </TableCell>
                  <TableCell>
                    {assessment.processingTimeMs ? `${assessment.processingTimeMs}ms` : 'N/A'}
                  </TableCell>
                  <TableCell>
                    {new Date(assessment.assessedAt).toLocaleString()}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </TabPanel>

      {/* Statistics Tab */}
      <TabPanel value={tabValue} index={2}>
        <Typography variant="h6" gutterBottom>Fraud/Risk Assessment Statistics</Typography>
        <Grid container spacing={3}>
          <Grid item xs={12} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <CheckCircleIcon color="success" sx={{ mr: 1 }} />
                  <Box>
                    <Typography variant="h4">85%</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Approval Rate
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
                  <ErrorIcon color="error" sx={{ mr: 1 }} />
                  <Box>
                    <Typography variant="h4">8%</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Rejection Rate
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
                  <WarningIcon color="warning" sx={{ mr: 1 }} />
                  <Box>
                    <Typography variant="h4">7%</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Manual Review Rate
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
                    <Typography variant="h4">2.1%</Typography>
                    <Typography variant="body2" color="text.secondary">
                      High Risk Rate
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
                  name="configurationName"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Configuration Name"
                      fullWidth
                      error={!!errors.configurationName}
                      helperText={errors.configurationName?.message}
                    />
                  )}
                />
              </Grid>
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
                  name="paymentSource"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!errors.paymentSource}>
                      <InputLabel>Payment Source</InputLabel>
                      <Select {...field} label="Payment Source">
                        <MenuItem value="BANK_CLIENT">Bank Client</MenuItem>
                        <MenuItem value="CLEARING_SYSTEM">Clearing System</MenuItem>
                        <MenuItem value="BOTH">Both</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="riskAssessmentType"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!errors.riskAssessmentType}>
                      <InputLabel>Risk Assessment Type</InputLabel>
                      <Select {...field} label="Risk Assessment Type">
                        <MenuItem value="REAL_TIME">Real-time</MenuItem>
                        <MenuItem value="BATCH">Batch</MenuItem>
                        <MenuItem value="HYBRID">Hybrid</MenuItem>
                        <MenuItem value="CUSTOM">Custom</MenuItem>
                      </Select>
                    </FormControl>
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
                  name="version"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Version"
                      fullWidth
                      error={!!errors.version}
                      helperText={errors.version?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="description"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Description"
                      fullWidth
                      multiline
                      rows={3}
                      error={!!errors.description}
                      helperText={errors.description?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
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
              <Typography variant="h6" gutterBottom>Basic Information</Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <Typography variant="body2"><strong>Name:</strong> {viewingConfig.configurationName}</Typography>
                  <Typography variant="body2"><strong>Tenant ID:</strong> {viewingConfig.tenantId}</Typography>
                  <Typography variant="body2"><strong>Payment Source:</strong> {viewingConfig.paymentSource}</Typography>
                  <Typography variant="body2"><strong>Risk Assessment Type:</strong> {viewingConfig.riskAssessmentType}</Typography>
                </Grid>
                <Grid item xs={12} md={6}>
                  <Typography variant="body2"><strong>Priority:</strong> {viewingConfig.priority}</Typography>
                  <Typography variant="body2"><strong>Version:</strong> {viewingConfig.version}</Typography>
                  <Typography variant="body2"><strong>Enabled:</strong> {viewingConfig.isEnabled ? 'Yes' : 'No'}</Typography>
                  <Typography variant="body2"><strong>Created:</strong> {viewingConfig.createdAt ? new Date(viewingConfig.createdAt).toLocaleString() : 'N/A'}</Typography>
                </Grid>
              </Grid>
              
              <Divider sx={{ my: 2 }} />
              
              <Typography variant="h6" gutterBottom>External API Configuration</Typography>
              <Box sx={{ bgcolor: 'grey.100', p: 2, borderRadius: 1 }}>
                <pre style={{ margin: 0, fontSize: '12px' }}>
                  {JSON.stringify(viewingConfig.externalApiConfig, null, 2)}
                </pre>
              </Box>
              
              <Divider sx={{ my: 2 }} />
              
              <Typography variant="h6" gutterBottom>Risk Rules</Typography>
              <Box sx={{ bgcolor: 'grey.100', p: 2, borderRadius: 1 }}>
                <pre style={{ margin: 0, fontSize: '12px' }}>
                  {JSON.stringify(viewingConfig.riskRules, null, 2)}
                </pre>
              </Box>
              
              <Divider sx={{ my: 2 }} />
              
              <Typography variant="h6" gutterBottom>Decision Criteria</Typography>
              <Box sx={{ bgcolor: 'grey.100', p: 2, borderRadius: 1 }}>
                <pre style={{ margin: 0, fontSize: '12px' }}>
                  {JSON.stringify(viewingConfig.decisionCriteria, null, 2)}
                </pre>
              </Box>
              
              <Divider sx={{ my: 2 }} />
              
              <Typography variant="h6" gutterBottom>Thresholds</Typography>
              <Box sx={{ bgcolor: 'grey.100', p: 2, borderRadius: 1 }}>
                <pre style={{ margin: 0, fontSize: '12px' }}>
                  {JSON.stringify(viewingConfig.thresholds, null, 2)}
                </pre>
              </Box>
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
  );
};

export default FraudRiskConfiguration;