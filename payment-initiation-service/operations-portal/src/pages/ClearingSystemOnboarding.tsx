/**
 * Clearing System Onboarding Page
 * 
 * Complete clearing system onboarding wizard for all 5 clearing systems.
 * Integrates with Operations Management Service for clearing system configuration.
 */

import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Checkbox,
  FormControlLabel,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Grid2 as Grid,
  Skeleton,
  Tooltip,
  CircularProgress,
  LinearProgress,
  Divider,
  Tabs,
  Tab,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Visibility,
  CheckCircle,
  Warning,
  Error,
  PlayArrow,
  Stop,
  Refresh,
  Save,
  TestTube,
  CloudUpload,
  AccountBalance,
  CreditCard,
  Payment,
} from '@mui/icons-material';
import { useApi } from '@hooks';
import { getOperationsManagementService } from '@services';
import { ClearingSystem, ClearingSystemConfiguration, ClearingSystemTestResult } from '@types/onboarding';
import { useNotification } from '@contexts';
import { usePermissions } from '@hooks/usePermissions';

interface ClearingSystemFormData {
  name: string;
  type: 'SAMOS' | 'BANKSERV_AFRICA' | 'RTC' | 'PAYSHAP' | 'SWIFT';
  description: string;
  endpoint: string;
  authentication: {
    type: string;
    credentials: Record<string, string>;
  };
  messageFormat: {
    format: string;
    version: string;
    encoding: string;
  };
  configuration: Record<string, any>;
  isActive: boolean;
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
      id={`clearing-tabpanel-${index}`}
      aria-labelledby={`clearing-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const ClearingSystemOnboarding: React.FC = () => {
  const { showSuccess, showError } = useNotification();
  const { canManageClearingSystems, canViewClearingSystems } = usePermissions();
  const [activeTab, setActiveTab] = useState(0);
  const [activeStep, setActiveStep] = useState(0);
  const [clearingSystems, setClearingSystems] = useState<ClearingSystem[]>([]);
  const [selectedSystem, setSelectedSystem] = useState<ClearingSystem | null>(null);
  const [systemDialogOpen, setSystemDialogOpen] = useState(false);
  const [testDialogOpen, setTestDialogOpen] = useState(false);
  const [testResult, setTestResult] = useState<ClearingSystemTestResult | null>(null);
  const [formData, setFormData] = useState<ClearingSystemFormData>({
    name: '',
    type: 'SAMOS',
    description: '',
    endpoint: '',
    authentication: {
      type: 'API_KEY',
      credentials: {},
    },
    messageFormat: {
      format: 'ISO20022',
      version: '1.0',
      encoding: 'UTF-8',
    },
    configuration: {},
    isActive: false,
  });

  // API hooks
  const clearingSystemsApi = useApi(
    () => getOperationsManagementService().getAllClearingSystems(),
    { immediate: true, showNotifications: false }
  );

  const testConnectionApi = useApi(
    (systemId: string) => getOperationsManagementService().testClearingSystemConnection(systemId),
    { immediate: false, showNotifications: true }
  );

  const saveSystemApi = useApi(
    (systemData: ClearingSystemFormData) => getOperationsManagementService().createClearingSystem(systemData),
    { immediate: false, showNotifications: true }
  );

  const updateSystemApi = useApi(
    (systemId: string, systemData: ClearingSystemFormData) => 
      getOperationsManagementService().updateClearingSystem(systemId, systemData),
    { immediate: false, showNotifications: true }
  );

  const deleteSystemApi = useApi(
    (systemId: string) => getOperationsManagementService().deleteClearingSystem(systemId),
    { immediate: false, showNotifications: true }
  );

  // Handle tab change
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  // Handle step navigation
  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleReset = () => {
    setActiveStep(0);
    setFormData({
      name: '',
      type: 'SAMOS',
      description: '',
      endpoint: '',
      authentication: {
        type: 'API_KEY',
        credentials: {},
      },
      messageFormat: {
        format: 'ISO20022',
        version: '1.0',
        encoding: 'UTF-8',
      },
      configuration: {},
      isActive: false,
    });
  };

  // Handle form data changes
  const handleFormChange = (field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleAuthChange = (field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      authentication: {
        ...prev.authentication,
        [field]: value,
      },
    }));
  };

  const handleMessageFormatChange = (field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      messageFormat: {
        ...prev.messageFormat,
        [field]: value,
      },
    }));
  };

  // Handle clearing system operations
  const handleCreateSystem = async () => {
    if (!canManageClearingSystems()) {
      showError('Access Denied', 'You do not have permission to create clearing systems');
      return;
    }

    try {
      await saveSystemApi.execute(formData);
      showSuccess('Clearing System Created', 'Clearing system has been created successfully');
      setSystemDialogOpen(false);
      handleReset();
      clearingSystemsApi.execute();
    } catch (error: any) {
      showError('Creation Failed', error.message || 'Failed to create clearing system');
    }
  };

  const handleUpdateSystem = async () => {
    if (!selectedSystem || !canManageClearingSystems()) {
      showError('Access Denied', 'You do not have permission to update clearing systems');
      return;
    }

    try {
      await updateSystemApi.execute(selectedSystem.id, formData);
      showSuccess('Clearing System Updated', 'Clearing system has been updated successfully');
      setSystemDialogOpen(false);
      clearingSystemsApi.execute();
    } catch (error: any) {
      showError('Update Failed', error.message || 'Failed to update clearing system');
    }
  };

  const handleDeleteSystem = async (systemId: string) => {
    if (!canManageClearingSystems()) {
      showError('Access Denied', 'You do not have permission to delete clearing systems');
      return;
    }

    try {
      await deleteSystemApi.execute(systemId);
      showSuccess('Clearing System Deleted', 'Clearing system has been deleted successfully');
      clearingSystemsApi.execute();
    } catch (error: any) {
      showError('Deletion Failed', error.message || 'Failed to delete clearing system');
    }
  };

  const handleTestConnection = async (systemId: string) => {
    try {
      const result = await testConnectionApi.execute(systemId);
      setTestResult(result);
      setTestDialogOpen(true);
    } catch (error: any) {
      showError('Test Failed', error.message || 'Failed to test clearing system connection');
    }
  };

  const handleEditSystem = (system: ClearingSystem) => {
    setSelectedSystem(system);
    setFormData({
      name: system.name,
      type: system.type as any,
      description: system.description || '',
      endpoint: system.endpoint,
      authentication: system.authentication || {
        type: 'API_KEY',
        credentials: {},
      },
      messageFormat: system.messageFormat || {
        format: 'ISO20022',
        version: '1.0',
        encoding: 'UTF-8',
      },
      configuration: system.configuration || {},
      isActive: system.status === 'active',
    });
    setSystemDialogOpen(true);
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active': return 'success';
      case 'inactive': return 'error';
      case 'pending': return 'warning';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active': return <CheckCircle color="success" />;
      case 'inactive': return <Error color="error" />;
      case 'pending': return <Warning color="warning" />;
      default: return <Warning color="disabled" />;
    }
  };

  const getSystemIcon = (type: string) => {
    switch (type) {
      case 'SAMOS': return <AccountBalance color="primary" />;
      case 'BANKSERV_AFRICA': return <CreditCard color="secondary" />;
      case 'RTC': return <Payment color="success" />;
      case 'PAYSHAP': return <Payment color="info" />;
      case 'SWIFT': return <Payment color="warning" />;
      default: return <Payment color="disabled" />;
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  // Loading skeleton component
  const SystemTableSkeleton = () => (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell><Skeleton variant="text" width="60%" /></TableCell>
            <TableCell><Skeleton variant="text" width="40%" /></TableCell>
            <TableCell><Skeleton variant="rectangular" width={80} height={24} /></TableCell>
            <TableCell><Skeleton variant="text" width="50%" /></TableCell>
            <TableCell><Skeleton variant="text" width="40%" /></TableCell>
            <TableCell><Skeleton variant="rectangular" width={100} height={32} /></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {[1, 2, 3, 4, 5].map((index) => (
            <TableRow key={index}>
              <TableCell><Skeleton variant="text" width="60%" /></TableCell>
              <TableCell><Skeleton variant="text" width="40%" /></TableCell>
              <TableCell><Skeleton variant="rectangular" width={80} height={24} /></TableCell>
              <TableCell><Skeleton variant="text" width="50%" /></TableCell>
              <TableCell><Skeleton variant="text" width="40%" /></TableCell>
              <TableCell><Skeleton variant="rectangular" width={100} height={32} /></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );

  const steps = [
    'System Information',
    'Authentication Setup',
    'Message Format Configuration',
    'System Configuration',
    'Testing & Validation',
    'Deployment',
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Clearing System Onboarding
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Configure and manage clearing systems (SAMOS, BankservAfrica, RTC, PayShap, SWIFT)
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => {
            setSelectedSystem(null);
            handleReset();
            setSystemDialogOpen(true);
          }}
          disabled={!canManageClearingSystems()}
        >
          Add Clearing System
        </Button>
      </Box>

      {/* Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={handleTabChange} aria-label="clearing system tabs">
            <Tab label="Onboarding Wizard" />
            <Tab label="Existing Systems" />
            <Tab label="System Status" />
          </Tabs>
        </Box>

        {/* Onboarding Wizard Tab */}
        <TabPanel value={activeTab} index={0}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Clearing System Onboarding Wizard
            </Typography>
            <Stepper activeStep={activeStep} orientation="horizontal">
              {steps.map((label, index) => (
                <Step key={label}>
                  <StepLabel>{label}</StepLabel>
                </Step>
              ))}
            </Stepper>
            
            <Box sx={{ mt: 3 }}>
              {/* Step 1: System Information */}
              {activeStep === 0 && (
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="System Name"
                      value={formData.name}
                      onChange={(e) => handleFormChange('name', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <FormControl fullWidth required>
                      <InputLabel>Clearing System Type</InputLabel>
                      <Select
                        value={formData.type}
                        onChange={(e) => handleFormChange('type', e.target.value)}
                        label="Clearing System Type"
                      >
                        <MenuItem value="SAMOS">SAMOS</MenuItem>
                        <MenuItem value="BANKSERV_AFRICA">BankservAfrica</MenuItem>
                        <MenuItem value="RTC">RTC</MenuItem>
                        <MenuItem value="PAYSHAP">PayShap</MenuItem>
                        <MenuItem value="SWIFT">SWIFT</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Description"
                      value={formData.description}
                      onChange={(e) => handleFormChange('description', e.target.value)}
                      multiline
                      rows={3}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      label="Endpoint URL"
                      value={formData.endpoint}
                      onChange={(e) => handleFormChange('endpoint', e.target.value)}
                      required
                      placeholder="https://clearing.example.com/api"
                    />
                  </Grid>
                </Grid>
              )}

              {/* Step 2: Authentication Setup */}
              {activeStep === 1 && (
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <FormControl fullWidth required>
                      <InputLabel>Authentication Type</InputLabel>
                      <Select
                        value={formData.authentication.type}
                        onChange={(e) => handleAuthChange('type', e.target.value)}
                        label="Authentication Type"
                      >
                        <MenuItem value="API_KEY">API Key</MenuItem>
                        <MenuItem value="OAUTH2">OAuth 2.0</MenuItem>
                        <MenuItem value="BASIC_AUTH">Basic Authentication</MenuItem>
                        <MenuItem value="CERTIFICATE">Certificate</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="API Key / Username"
                      value={formData.authentication.credentials.apiKey || ''}
                      onChange={(e) => handleAuthChange('credentials', {
                        ...formData.authentication.credentials,
                        apiKey: e.target.value,
                      })}
                      required
                    />
                  </Grid>
                  {formData.authentication.type === 'BASIC_AUTH' && (
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField
                        fullWidth
                        label="Password"
                        type="password"
                        value={formData.authentication.credentials.password || ''}
                        onChange={(e) => handleAuthChange('credentials', {
                          ...formData.authentication.credentials,
                          password: e.target.value,
                        })}
                        required
                      />
                    </Grid>
                  )}
                  {formData.authentication.type === 'OAUTH2' && (
                    <>
                      <Grid size={{ xs: 12, md: 6 }}>
                        <TextField
                          fullWidth
                          label="Client ID"
                          value={formData.authentication.credentials.clientId || ''}
                          onChange={(e) => handleAuthChange('credentials', {
                            ...formData.authentication.credentials,
                            clientId: e.target.value,
                          })}
                          required
                        />
                      </Grid>
                      <Grid size={{ xs: 12, md: 6 }}>
                        <TextField
                          fullWidth
                          label="Client Secret"
                          type="password"
                          value={formData.authentication.credentials.clientSecret || ''}
                          onChange={(e) => handleAuthChange('credentials', {
                            ...formData.authentication.credentials,
                            clientSecret: e.target.value,
                          })}
                          required
                        />
                      </Grid>
                    </>
                  )}
                </Grid>
              )}

              {/* Step 3: Message Format Configuration */}
              {activeStep === 2 && (
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <Typography variant="h6" gutterBottom>
                      Message Format Configuration
                    </Typography>
                    <Alert severity="info" sx={{ mb: 2 }}>
                      Configure message format for {formData.type} clearing system
                    </Alert>
                  </Grid>
                  <Grid size={{ xs: 12, md: 4 }}>
                    <FormControl fullWidth required>
                      <InputLabel>Message Format</InputLabel>
                      <Select
                        value={formData.messageFormat.format}
                        onChange={(e) => handleMessageFormatChange('format', e.target.value)}
                        label="Message Format"
                      >
                        <MenuItem value="ISO20022">ISO 20022</MenuItem>
                        <MenuItem value="ISO8583">ISO 8583</MenuItem>
                        <MenuItem value="XML">XML</MenuItem>
                        <MenuItem value="JSON">JSON</MenuItem>
                        <MenuItem value="FIX">FIX Protocol</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid size={{ xs: 12, md: 4 }}>
                    <TextField
                      fullWidth
                      label="Format Version"
                      value={formData.messageFormat.version}
                      onChange={(e) => handleMessageFormatChange('version', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 4 }}>
                    <FormControl fullWidth required>
                      <InputLabel>Encoding</InputLabel>
                      <Select
                        value={formData.messageFormat.encoding}
                        onChange={(e) => handleMessageFormatChange('encoding', e.target.value)}
                        label="Encoding"
                      >
                        <MenuItem value="UTF-8">UTF-8</MenuItem>
                        <MenuItem value="ASCII">ASCII</MenuItem>
                        <MenuItem value="EBCDIC">EBCDIC</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                </Grid>
              )}

              {/* Step 4: System Configuration */}
              {activeStep === 3 && (
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <Typography variant="h6" gutterBottom>
                      System Configuration
                    </Typography>
                    <Alert severity="info" sx={{ mb: 2 }}>
                      Configure system-specific settings and parameters
                    </Alert>
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="Timeout (seconds)"
                      type="number"
                      value={formData.configuration.timeout || 30}
                      onChange={(e) => handleFormChange('configuration', {
                        ...formData.configuration,
                        timeout: parseInt(e.target.value),
                      })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="Retry Attempts"
                      type="number"
                      value={formData.configuration.retryAttempts || 3}
                      onChange={(e) => handleFormChange('configuration', {
                        ...formData.configuration,
                        retryAttempts: parseInt(e.target.value),
                      })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="Batch Size"
                      type="number"
                      value={formData.configuration.batchSize || 100}
                      onChange={(e) => handleFormChange('configuration', {
                        ...formData.configuration,
                        batchSize: parseInt(e.target.value),
                      })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="Processing Interval (minutes)"
                      type="number"
                      value={formData.configuration.processingInterval || 15}
                      onChange={(e) => handleFormChange('configuration', {
                        ...formData.configuration,
                        processingInterval: parseInt(e.target.value),
                      })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={formData.configuration.enableLogging || false}
                          onChange={(e) => handleFormChange('configuration', {
                            ...formData.configuration,
                            enableLogging: e.target.checked,
                          })}
                        />
                      }
                      label="Enable Detailed Logging"
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={formData.configuration.enableMonitoring || false}
                          onChange={(e) => handleFormChange('configuration', {
                            ...formData.configuration,
                            enableMonitoring: e.target.checked,
                          })}
                        />
                      }
                      label="Enable Performance Monitoring"
                    />
                  </Grid>
                </Grid>
              )}

              {/* Step 5: Testing & Validation */}
              {activeStep === 4 && (
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <Typography variant="h6" gutterBottom>
                      Connection Testing
                    </Typography>
                    <Alert severity="info" sx={{ mb: 2 }}>
                      Test the clearing system connection to ensure proper configuration
                    </Alert>
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <Button
                      variant="contained"
                      startIcon={<TestTube />}
                      onClick={() => handleTestConnection('test')}
                      disabled={!formData.name || !formData.endpoint}
                      fullWidth
                    >
                      Test Connection
                    </Button>
                  </Grid>
                  {testResult && (
                    <Grid size={{ xs: 12 }}>
                      <Alert 
                        severity={testResult.success ? 'success' : 'error'}
                        sx={{ mt: 2 }}
                      >
                        {testResult.message}
                      </Alert>
                    </Grid>
                  )}
                </Grid>
              )}

              {/* Step 6: Deployment */}
              {activeStep === 5 && (
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <Typography variant="h6" gutterBottom>
                      Deploy Clearing System
                    </Typography>
                    <Alert severity="warning" sx={{ mb: 2 }}>
                      Review all settings before deploying the clearing system
                    </Alert>
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={formData.isActive}
                          onChange={(e) => handleFormChange('isActive', e.target.checked)}
                        />
                      }
                      label="Activate Clearing System Immediately"
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <Button
                      variant="contained"
                      startIcon={<CloudUpload />}
                      onClick={handleCreateSystem}
                      disabled={!formData.name || !formData.type || !formData.endpoint}
                      fullWidth
                      size="large"
                    >
                      Deploy Clearing System
                    </Button>
                  </Grid>
                </Grid>
              )}

              <Box sx={{ mt: 3, display: 'flex', justifyContent: 'space-between' }}>
                <Button
                  disabled={activeStep === 0}
                  onClick={handleBack}
                >
                  Back
                </Button>
                <Button
                  variant="contained"
                  onClick={activeStep === steps.length - 1 ? handleCreateSystem : handleNext}
                  disabled={activeStep === steps.length - 1 && (!formData.name || !formData.type || !formData.endpoint)}
                >
                  {activeStep === steps.length - 1 ? 'Deploy' : 'Next'}
                </Button>
              </Box>
            </Box>
          </CardContent>
        </TabPanel>

        {/* Existing Systems Tab */}
        <TabPanel value={activeTab} index={1}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Existing Clearing Systems
            </Typography>
            
            {clearingSystemsApi.loading ? (
              <SystemTableSkeleton />
            ) : clearingSystemsApi.error ? (
              <Alert severity="error" sx={{ mb: 2 }}>
                Failed to load clearing systems: {clearingSystemsApi.error}
              </Alert>
            ) : (
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>System Name</TableCell>
                      <TableCell>Type</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Endpoint</TableCell>
                      <TableCell>Message Format</TableCell>
                      <TableCell>Created</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {clearingSystemsApi.data?.map((system: ClearingSystem) => (
                      <TableRow key={system.id}>
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1}>
                            {getSystemIcon(system.type)}
                            <Typography variant="body2" fontWeight="medium">
                              {system.name}
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Chip label={system.type} size="small" />
                        </TableCell>
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1}>
                            {getStatusIcon(system.status)}
                            <Chip 
                              label={system.status} 
                              size="small" 
                              color={getStatusColor(system.status) as any}
                            />
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {system.endpoint}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">
                            {system.messageFormat?.format || 'N/A'}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">
                            {formatDate(system.createdAt)}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Box display="flex" gap={1}>
                            <Tooltip title="Test Connection">
                              <IconButton
                                size="small"
                                onClick={() => handleTestConnection(system.id)}
                                disabled={!canManageClearingSystems()}
                              >
                                <PlayArrow />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="Edit System">
                              <IconButton
                                size="small"
                                onClick={() => handleEditSystem(system)}
                                disabled={!canManageClearingSystems()}
                              >
                                <Edit />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="View Details">
                              <IconButton size="small">
                                <Visibility />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="Delete System">
                              <IconButton
                                size="small"
                                onClick={() => handleDeleteSystem(system.id)}
                                disabled={!canManageClearingSystems()}
                              >
                                <Delete />
                              </IconButton>
                            </Tooltip>
                          </Box>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </CardContent>
        </TabPanel>

        {/* System Status Tab */}
        <TabPanel value={activeTab} index={2}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              System Status Overview
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Real-time status of all clearing systems
            </Typography>
            
            <Grid container spacing={2}>
              {clearingSystemsApi.data?.map((system: ClearingSystem) => (
                <Grid size={{ xs: 12, md: 6, lg: 4 }} key={system.id}>
                  <Card>
                    <CardContent>
                      <Box display="flex" alignItems="center" gap={1} mb={2}>
                        {getSystemIcon(system.type)}
                        <Typography variant="h6">
                          {system.name}
                        </Typography>
                      </Box>
                      <Box display="flex" alignItems="center" gap={1} mb={1}>
                        {getStatusIcon(system.status)}
                        <Chip 
                          label={system.status} 
                          size="small" 
                          color={getStatusColor(system.status) as any}
                        />
                      </Box>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {system.type} • {system.messageFormat?.format || 'N/A'}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Last updated: {formatDate(system.updatedAt || system.createdAt)}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </CardContent>
        </TabPanel>
      </Card>

      {/* Test Result Dialog */}
      <Dialog open={testDialogOpen} onClose={() => setTestDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          Connection Test Result
        </DialogTitle>
        <DialogContent>
          {testResult && (
            <Box>
              <Alert 
                severity={testResult.success ? 'success' : 'error'}
                sx={{ mb: 2 }}
              >
                {testResult.message}
              </Alert>
              {testResult.details && (
                <Box>
                  <Typography variant="h6" gutterBottom>
                    Test Details
                  </Typography>
                  <Typography variant="body2" component="pre" sx={{ 
                    backgroundColor: 'grey.100', 
                    p: 2, 
                    borderRadius: 1,
                    overflow: 'auto',
                    maxHeight: 200,
                  }}>
                    {JSON.stringify(testResult.details, null, 2)}
                  </Typography>
                </Box>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTestDialogOpen(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ClearingSystemOnboarding;