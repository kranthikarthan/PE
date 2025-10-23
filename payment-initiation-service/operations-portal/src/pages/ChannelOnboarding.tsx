/**
 * Channel Onboarding Page
 * 
 * Complete channel onboarding wizard with validation and testing.
 * Integrates with Tenant Management Service for channel configuration.
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
  Skeleton,
  Tooltip,
  CircularProgress,
  LinearProgress,
  Divider,
} from '@mui/material';
import { Grid } from '@mui/material';
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
  Science,
  CloudUpload,
} from '@mui/icons-material';
import { useApi } from '../hooks';
import { getTenantManagementService } from '../services';
import { Channel, ChannelConfiguration, ChannelTestResult } from '../types/onboarding';
import { useNotification } from '../contexts';
import { usePermissions } from '../hooks/usePermissions';
import { ChannelType, AuthenticationMethod } from '../types/onboarding';

interface ChannelFormData {
  name: string;
  type: ChannelType;
  description: string;
  endpoint: string;
  authentication: AuthenticationMethod;
  rateLimit?: number;
  webhookUrl?: string;
  enableLogging: boolean;
  enableMonitoring: boolean;
  tenantId: string;
  businessUnitId: string;
}

const ChannelOnboarding: React.FC = () => {
  const { showSuccess, showError } = useNotification();
  const { canManageChannels, canViewChannels } = usePermissions();
  const [activeStep, setActiveStep] = useState(0);
  const [channels, setChannels] = useState<Channel[]>([]);
  const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);
  const [channelDialogOpen, setChannelDialogOpen] = useState(false);
  const [testDialogOpen, setTestDialogOpen] = useState(false);
  const [testResult, setTestResult] = useState<ChannelTestResult | null>(null);
  const [formData, setFormData] = useState<ChannelFormData>({
    name: '',
    type: ChannelType.API,
    description: '',
    endpoint: '',
    authentication: AuthenticationMethod.API_KEY,
    rateLimit: 100,
    webhookUrl: '',
    enableLogging: true,
    enableMonitoring: true,
    tenantId: '',
    businessUnitId: '',
  });

  // API hooks
  const channelsApi = useApi(
    () => getTenantManagementService().getAllChannels(),
    { immediate: true, showNotifications: false }
  );

  const testConnectionApi = useApi(
    (channelId: string) => getTenantManagementService().testChannelConnection(channelId),
    { immediate: false, showNotifications: true }
  );

  const saveChannelApi = useApi(
    (channelData: ChannelFormData) => getTenantManagementService().createChannel(channelData),
    { immediate: false, showNotifications: true }
  );

  const updateChannelApi = useApi(
    (channelId: string, channelData: ChannelFormData) => 
      getTenantManagementService().updateChannel(channelId, channelData),
    { immediate: false, showNotifications: true }
  );

  const deleteChannelApi = useApi(
    (channelId: string) => getTenantManagementService().deleteChannel(channelId),
    { immediate: false, showNotifications: true }
  );

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
      type: ChannelType.API,
      description: '',
      endpoint: '',
      authentication: AuthenticationMethod.API_KEY,
      rateLimit: 100,
      webhookUrl: '',
      enableLogging: true,
      enableMonitoring: true,
      tenantId: '',
      businessUnitId: '',
    });
  };

  // Handle form data changes
  const handleFormChange = (field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleAuthChange = (value: AuthenticationMethod) => {
    setFormData(prev => ({
      ...prev,
      authentication: value,
    }));
  };

  // Handle channel operations
  const handleCreateChannel = async () => {
    if (!canManageChannels()) {
      showError('Access Denied', 'You do not have permission to create channels');
      return;
    }

    try {
      await saveChannelApi.execute(formData);
      showSuccess('Channel Created', 'Channel has been created successfully');
      setChannelDialogOpen(false);
      handleReset();
      channelsApi.execute();
    } catch (error: any) {
      showError('Creation Failed', error.message || 'Failed to create channel');
    }
  };

  const handleUpdateChannel = async () => {
    if (!selectedChannel || !canManageChannels()) {
      showError('Access Denied', 'You do not have permission to update channels');
      return;
    }

    try {
      await updateChannelApi.execute(selectedChannel.id, formData);
      showSuccess('Channel Updated', 'Channel has been updated successfully');
      setChannelDialogOpen(false);
      channelsApi.execute();
    } catch (error: any) {
      showError('Update Failed', error.message || 'Failed to update channel');
    }
  };

  const handleDeleteChannel = async (channelId: string) => {
    if (!canManageChannels()) {
      showError('Access Denied', 'You do not have permission to delete channels');
      return;
    }

    try {
      await deleteChannelApi.execute(channelId);
      showSuccess('Channel Deleted', 'Channel has been deleted successfully');
      channelsApi.execute();
    } catch (error: any) {
      showError('Deletion Failed', error.message || 'Failed to delete channel');
    }
  };

  const handleTestConnection = async (channelId: string) => {
    try {
      const result = await testConnectionApi.execute(channelId);
      setTestResult(result);
      setTestDialogOpen(true);
    } catch (error: any) {
      showError('Test Failed', error.message || 'Failed to test channel connection');
    }
  };

  const handleEditChannel = (channel: Channel) => {
    setSelectedChannel(channel);
    setFormData({
      name: channel.name,
      type: channel.type,
      description: channel.description || '',
      endpoint: channel.endpoint,
      authentication: channel.authentication,
      rateLimit: channel.rateLimit,
      webhookUrl: channel.webhookUrl,
      enableLogging: channel.enableLogging,
      enableMonitoring: channel.enableMonitoring,
      tenantId: channel.tenantId,
      businessUnitId: channel.businessUnitId,
    });
    setChannelDialogOpen(true);
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

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  // Loading skeleton component
  const ChannelTableSkeleton = () => (
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
    'Channel Information',
    'Authentication Setup',
    'Configuration',
    'Testing & Validation',
    'Deployment',
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Channel Onboarding
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Configure and manage payment channels
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => {
            setSelectedChannel(null);
            handleReset();
            setChannelDialogOpen(true);
          }}
          disabled={!canManageChannels()}
        >
          Add Channel
        </Button>
      </Box>

      {/* Onboarding Wizard */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Channel Onboarding Wizard
          </Typography>
          <Stepper activeStep={activeStep} orientation="horizontal">
            {steps.map((label, index) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>
          
          <Box sx={{ mt: 3 }}>
            {/* Step 1: Channel Information */}
            {activeStep === 0 && (
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Channel Name"
                    value={formData.name}
                    onChange={(e) => handleFormChange('name', e.target.value)}
                    required
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <FormControl fullWidth required>
                    <InputLabel>Channel Type</InputLabel>
                    <Select
                      value={formData.type}
                      onChange={(e) => handleFormChange('type', e.target.value)}
                      label="Channel Type"
                    >
                      <MenuItem value="BANK_API">Bank API</MenuItem>
                      <MenuItem value="CARD_NETWORK">Card Network</MenuItem>
                      <MenuItem value="DIGITAL_WALLET">Digital Wallet</MenuItem>
                      <MenuItem value="MOBILE_MONEY">Mobile Money</MenuItem>
                      <MenuItem value="CRYPTOCURRENCY">Cryptocurrency</MenuItem>
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
                    placeholder="https://api.example.com/v1"
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
                      value={formData.authentication}
                      onChange={(e) => handleAuthChange(e.target.value as AuthenticationMethod)}
                      label="Authentication Type"
                    >
                      <MenuItem value={AuthenticationMethod.API_KEY}>API Key</MenuItem>
                      <MenuItem value={AuthenticationMethod.OAUTH2}>OAuth 2.0</MenuItem>
                      <MenuItem value={AuthenticationMethod.BASIC}>Basic Authentication</MenuItem>
                      <MenuItem value={AuthenticationMethod.JWT}>JWT Token</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Alert severity="info">
                    Authentication method selected: {formData.authentication.replace('_', ' ')}
                    <br />
                    <strong>Note:</strong> Credentials will be configured separately through the channel management interface.
                  </Alert>
                </Grid>
              </Grid>
            )}

            {/* Step 3: Configuration */}
            {activeStep === 2 && (
              <Grid container spacing={2}>
                <Grid size={{ xs: 12 }}>
                  <Typography variant="h6" gutterBottom>
                    Channel Configuration
                  </Typography>
                  <Alert severity="info" sx={{ mb: 2 }}>
                    Configure channel-specific settings and parameters
                  </Alert>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Rate Limit"
                    type="number"
                    value={formData.rateLimit || 100}
                    onChange={(e) => setFormData(prev => ({
                      ...prev,
                      rateLimit: parseInt(e.target.value)
                    }))}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Webhook URL"
                    value={formData.webhookUrl || ''}
                    onChange={(e) => setFormData(prev => ({
                      ...prev,
                      webhookUrl: e.target.value
                    }))}
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={formData.enableLogging}
                        onChange={(e) => setFormData(prev => ({
                          ...prev,
                          enableLogging: e.target.checked
                        }))}
                      />
                    }
                    label="Enable Detailed Logging"
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={formData.enableMonitoring}
                        onChange={(e) => setFormData(prev => ({
                          ...prev,
                          enableMonitoring: e.target.checked
                        }))}
                      />
                    }
                    label="Enable Performance Monitoring"
                  />
                </Grid>
              </Grid>
            )}

            {/* Step 4: Testing & Validation */}
            {activeStep === 3 && (
              <Grid container spacing={2}>
                <Grid size={{ xs: 12 }}>
                  <Typography variant="h6" gutterBottom>
                    Connection Testing
                  </Typography>
                  <Alert severity="info" sx={{ mb: 2 }}>
                    Test the channel connection to ensure proper configuration
                  </Alert>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Button
                    variant="contained"
                    startIcon={<Science />}
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

            {/* Step 5: Deployment */}
            {activeStep === 4 && (
              <Grid container spacing={2}>
                <Grid size={{ xs: 12 }}>
                  <Typography variant="h6" gutterBottom>
                    Deploy Channel
                  </Typography>
                  <Alert severity="warning" sx={{ mb: 2 }}>
                    Review all settings before deploying the channel
                  </Alert>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Button
                    variant="contained"
                    startIcon={<CloudUpload />}
                    onClick={handleCreateChannel}
                    disabled={!formData.name || !formData.type || !formData.endpoint}
                    fullWidth
                    size="large"
                  >
                    Deploy Channel
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
                onClick={activeStep === steps.length - 1 ? handleCreateChannel : handleNext}
                disabled={activeStep === steps.length - 1 && (!formData.name || !formData.type || !formData.endpoint)}
              >
                {activeStep === steps.length - 1 ? 'Deploy' : 'Next'}
              </Button>
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Existing Channels */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Existing Channels
          </Typography>
          
          {channelsApi.loading ? (
            <ChannelTableSkeleton />
          ) : channelsApi.error ? (
            <Alert severity="error" sx={{ mb: 2 }}>
              Failed to load channels: {channelsApi.error}
            </Alert>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Channel Name</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Endpoint</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {channelsApi.data?.map((channel: Channel) => (
                    <TableRow key={channel.id}>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">
                          {channel.name}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip label={channel.type} size="small" />
                      </TableCell>
                      <TableCell>
                        <Box display="flex" alignItems="center" gap={1}>
                          {getStatusIcon(channel.status)}
                          <Chip 
                            label={channel.status} 
                            size="small" 
                            color={getStatusColor(channel.status) as any}
                          />
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {channel.endpoint}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {formatDate(channel.createdAt)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box display="flex" gap={1}>
                          <Tooltip title="Test Connection">
                            <IconButton
                              size="small"
                              onClick={() => handleTestConnection(channel.id)}
                              disabled={!canManageChannels()}
                            >
                              <PlayArrow />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Edit Channel">
                            <IconButton
                              size="small"
                              onClick={() => handleEditChannel(channel)}
                              disabled={!canManageChannels()}
                            >
                              <Edit />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="View Details">
                            <IconButton size="small">
                              <Visibility />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Delete Channel">
                            <IconButton
                              size="small"
                              onClick={() => handleDeleteChannel(channel.id)}
                              disabled={!canManageChannels()}
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

export default ChannelOnboarding;