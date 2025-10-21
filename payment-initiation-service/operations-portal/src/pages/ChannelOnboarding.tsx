import React, { useState } from 'react';
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
} from '@mui/material';
import { GridLegacy as Grid } from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Visibility,
  CheckCircle,
  Warning,
  Error,
} from '@mui/icons-material';

interface Channel {
  id: string;
  name: string;
  type: string;
  status: 'active' | 'inactive' | 'pending';
  endpoint: string;
  createdAt: string;
  lastUpdated: string;
}

const ChannelOnboarding: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [onboardingDialogOpen, setOnboardingDialogOpen] = useState(false);
  const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);

  const [channels] = useState<Channel[]>([
    {
      id: 'CH-001',
      name: 'Mobile Banking App',
      type: 'Mobile',
      status: 'active',
      endpoint: 'https://api.payments.com/mobile',
      createdAt: '2025-10-15 09:00:00',
      lastUpdated: '2025-10-20 14:30:00',
    },
    {
      id: 'CH-002',
      name: 'Web Portal',
      type: 'Web',
      status: 'active',
      endpoint: 'https://api.payments.com/web',
      createdAt: '2025-10-10 10:15:00',
      lastUpdated: '2025-10-19 16:45:00',
    },
    {
      id: 'CH-003',
      name: 'API Gateway',
      type: 'API',
      status: 'pending',
      endpoint: 'https://api.payments.com/gateway',
      createdAt: '2025-10-20 11:20:00',
      lastUpdated: '2025-10-20 11:20:00',
    },
  ]);

  const [formData, setFormData] = useState({
    channelName: '',
    channelType: '',
    endpoint: '',
    description: '',
    authentication: '',
    rateLimit: '',
    webhookUrl: '',
    enableLogging: false,
    enableMonitoring: false,
  });

  const steps = [
    'Channel Information',
    'Configuration',
    'Security Settings',
    'Testing & Validation',
    'Deployment',
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'success';
      case 'inactive': return 'error';
      case 'pending': return 'warning';
      default: return 'default';
    }
  };

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleReset = () => {
    setActiveStep(0);
    setFormData({
      channelName: '',
      channelType: '',
      endpoint: '',
      description: '',
      authentication: '',
      rateLimit: '',
      webhookUrl: '',
      enableLogging: false,
      enableMonitoring: false,
    });
  };

  const handleViewDetails = (channel: Channel) => {
    setSelectedChannel(channel);
    setDetailsDialogOpen(true);
  };

  const handleStartOnboarding = () => {
    setOnboardingDialogOpen(true);
    handleReset();
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Channel Onboarding
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Manage payment channels and onboarding processes
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleStartOnboarding}
        >
          New Channel
        </Button>
      </Box>

      <Grid container spacing={3}>
        {/* Existing Channels */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Existing Channels ({channels.length})
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Channel ID</TableCell>
                      <TableCell>Name</TableCell>
                      <TableCell>Type</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Endpoint</TableCell>
                      <TableCell>Created At</TableCell>
                      <TableCell>Last Updated</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {channels.map((channel) => (
                      <TableRow key={channel.id}>
                        <TableCell>
                          <Typography variant="subtitle2" fontWeight="bold">
                            {channel.id}
                          </Typography>
                        </TableCell>
                        <TableCell>{channel.name}</TableCell>
                        <TableCell>{channel.type}</TableCell>
                        <TableCell>
                          <Chip 
                            label={channel.status.toUpperCase()} 
                            color={getStatusColor(channel.status) as any}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <Typography variant="caption" noWrap sx={{ maxWidth: 200 }}>
                            {channel.endpoint}
                          </Typography>
                        </TableCell>
                        <TableCell>{channel.createdAt}</TableCell>
                        <TableCell>{channel.lastUpdated}</TableCell>
                        <TableCell>
                          <Box display="flex" gap={1}>
                            <IconButton
                              size="small"
                              onClick={() => handleViewDetails(channel)}
                            >
                              <Visibility />
                            </IconButton>
                            <IconButton size="small">
                              <Edit />
                            </IconButton>
                            <IconButton size="small">
                              <Delete />
                            </IconButton>
                          </Box>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Onboarding Dialog */}
      <Dialog open={onboardingDialogOpen} onClose={() => setOnboardingDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Channel Onboarding Process
        </DialogTitle>
        <DialogContent>
          <Stepper activeStep={activeStep} orientation="vertical">
            <Step>
              <StepLabel>Channel Information</StepLabel>
              <StepContent>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Channel Name"
                      value={formData.channelName}
                      onChange={(e) => setFormData({...formData, channelName: e.target.value})}
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <FormControl fullWidth>
                      <InputLabel>Channel Type</InputLabel>
                      <Select
                        value={formData.channelType}
                        label="Channel Type"
                        onChange={(e) => setFormData({...formData, channelType: e.target.value})}
                      >
                        <MenuItem value="Mobile">Mobile</MenuItem>
                        <MenuItem value="Web">Web</MenuItem>
                        <MenuItem value="API">API</MenuItem>
                        <MenuItem value="POS">POS</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      label="Description"
                      value={formData.description}
                      onChange={(e) => setFormData({...formData, description: e.target.value})}
                      fullWidth
                      multiline
                      rows={3}
                    />
                  </Grid>
                </Grid>
                <Box sx={{ mb: 2 }}>
                  <Button
                    variant="contained"
                    onClick={handleNext}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Continue
                  </Button>
                  <Button
                    disabled={activeStep === 0}
                    onClick={handleBack}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Back
                  </Button>
                </Box>
              </StepContent>
            </Step>

            <Step>
              <StepLabel>Configuration</StepLabel>
              <StepContent>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <TextField
                      label="Endpoint URL"
                      value={formData.endpoint}
                      onChange={(e) => setFormData({...formData, endpoint: e.target.value})}
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Rate Limit (requests/minute)"
                      value={formData.rateLimit}
                      onChange={(e) => setFormData({...formData, rateLimit: e.target.value})}
                      fullWidth
                      type="number"
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="Webhook URL"
                      value={formData.webhookUrl}
                      onChange={(e) => setFormData({...formData, webhookUrl: e.target.value})}
                      fullWidth
                    />
                  </Grid>
                </Grid>
                <Box sx={{ mb: 2 }}>
                  <Button
                    variant="contained"
                    onClick={handleNext}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Continue
                  </Button>
                  <Button
                    disabled={activeStep === 0}
                    onClick={handleBack}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Back
                  </Button>
                </Box>
              </StepContent>
            </Step>

            <Step>
              <StepLabel>Security Settings</StepLabel>
              <StepContent>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <FormControl fullWidth>
                      <InputLabel>Authentication Method</InputLabel>
                      <Select
                        value={formData.authentication}
                        label="Authentication Method"
                        onChange={(e) => setFormData({...formData, authentication: e.target.value})}
                      >
                        <MenuItem value="OAuth2">OAuth 2.0</MenuItem>
                        <MenuItem value="API Key">API Key</MenuItem>
                        <MenuItem value="JWT">JWT Token</MenuItem>
                        <MenuItem value="Basic">Basic Auth</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={12}>
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={formData.enableLogging}
                          onChange={(e) => setFormData({...formData, enableLogging: e.target.checked})}
                        />
                      }
                      label="Enable Request/Response Logging"
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={formData.enableMonitoring}
                          onChange={(e) => setFormData({...formData, enableMonitoring: e.target.checked})}
                        />
                      }
                      label="Enable Performance Monitoring"
                    />
                  </Grid>
                </Grid>
                <Box sx={{ mb: 2 }}>
                  <Button
                    variant="contained"
                    onClick={handleNext}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Continue
                  </Button>
                  <Button
                    disabled={activeStep === 0}
                    onClick={handleBack}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Back
                  </Button>
                </Box>
              </StepContent>
            </Step>

            <Step>
              <StepLabel>Testing & Validation</StepLabel>
              <StepContent>
                <Alert severity="info" sx={{ mb: 2 }}>
                  This step will validate the channel configuration and perform connectivity tests.
                </Alert>
                <Box sx={{ mb: 2 }}>
                  <Button
                    variant="contained"
                    onClick={handleNext}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Run Tests
                  </Button>
                  <Button
                    disabled={activeStep === 0}
                    onClick={handleBack}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Back
                  </Button>
                </Box>
              </StepContent>
            </Step>

            <Step>
              <StepLabel>Deployment</StepLabel>
              <StepContent>
                <Alert severity="success" sx={{ mb: 2 }}>
                  Channel configuration is ready for deployment. Review the settings and deploy when ready.
                </Alert>
                <Box sx={{ mb: 2 }}>
                  <Button
                    variant="contained"
                    onClick={() => setOnboardingDialogOpen(false)}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Deploy Channel
                  </Button>
                  <Button
                    disabled={activeStep === 0}
                    onClick={handleBack}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Back
                  </Button>
                </Box>
              </StepContent>
            </Step>
          </Stepper>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOnboardingDialogOpen(false)}>Cancel</Button>
          <Button 
            variant="contained" 
            onClick={activeStep === steps.length - 1 ? () => setOnboardingDialogOpen(false) : handleNext}
          >
            {activeStep === steps.length - 1 ? 'Complete' : 'Next'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Channel Details Dialog */}
      <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Channel Details - {selectedChannel?.id}
        </DialogTitle>
        <DialogContent>
          {selectedChannel && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Channel ID"
                  value={selectedChannel.id}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Status"
                  value={selectedChannel.status}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Channel Name"
                  value={selectedChannel.name}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Type"
                  value={selectedChannel.type}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Endpoint"
                  value={selectedChannel.endpoint}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Created At"
                  value={selectedChannel.createdAt}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Last Updated"
                  value={selectedChannel.lastUpdated}
                  fullWidth
                  disabled
                />
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsDialogOpen(false)}>Close</Button>
          <Button variant="contained" onClick={() => setDetailsDialogOpen(false)}>
            Edit Channel
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ChannelOnboarding;