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
  RadioGroup,
  Radio,
  FormLabel,
  Alert,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
} from '@mui/material';
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
  type: 'bank' | 'fintech' | 'payment_provider';
  status: 'active' | 'inactive' | 'pending' | 'suspended';
  country: string;
  currency: string;
  createdAt: string;
  lastUpdated: string;
}

const ChannelOnboarding: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [channels, setChannels] = useState<Channel[]>([
    {
      id: 'CH-001',
      name: 'Bank of America',
      type: 'bank',
      status: 'active',
      country: 'United States',
      currency: 'USD',
      createdAt: '2025-10-15 09:00:00',
      lastUpdated: '2025-10-20 14:30:00',
    },
    {
      id: 'CH-002',
      name: 'Stripe',
      type: 'payment_provider',
      status: 'active',
      country: 'United States',
      currency: 'USD',
      createdAt: '2025-10-10 11:15:00',
      lastUpdated: '2025-10-18 16:45:00',
    },
    {
      id: 'CH-003',
      name: 'Revolut',
      type: 'fintech',
      status: 'pending',
      country: 'United Kingdom',
      currency: 'GBP',
      createdAt: '2025-10-20 10:30:00',
      lastUpdated: '2025-10-20 10:30:00',
    },
  ]);

  const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [onboardingDialogOpen, setOnboardingDialogOpen] = useState(false);

  const [onboardingData, setOnboardingData] = useState({
    channelName: '',
    channelType: '',
    country: '',
    currency: '',
    contactPerson: '',
    email: '',
    phone: '',
    apiEndpoint: '',
    authenticationType: '',
    webhookUrl: '',
    supportedPaymentTypes: [] as string[],
    complianceRequirements: [] as string[],
  });

  const steps = [
    'Channel Information',
    'Technical Configuration',
    'Compliance & Security',
    'Testing & Validation',
    'Go Live',
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'success';
      case 'inactive': return 'default';
      case 'pending': return 'warning';
      case 'suspended': return 'error';
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
    setOnboardingData({
      channelName: '',
      channelType: '',
      country: '',
      currency: '',
      contactPerson: '',
      email: '',
      phone: '',
      apiEndpoint: '',
      authenticationType: '',
      webhookUrl: '',
      supportedPaymentTypes: [],
      complianceRequirements: [],
    });
  };

  const handleViewDetails = (channel: Channel) => {
    setSelectedChannel(channel);
    setDetailsDialogOpen(true);
  };

  const handleStartOnboarding = () => {
    setOnboardingDialogOpen(true);
  };

  const getStepContent = (step: number) => {
    switch (step) {
      case 0:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <TextField
                label="Channel Name"
                value={onboardingData.channelName}
                onChange={(e) => setOnboardingData({...onboardingData, channelName: e.target.value})}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth required>
                <InputLabel>Channel Type</InputLabel>
                <Select
                  value={onboardingData.channelType}
                  label="Channel Type"
                  onChange={(e) => setOnboardingData({...onboardingData, channelType: e.target.value})}
                >
                  <MenuItem value="bank">Bank</MenuItem>
                  <MenuItem value="fintech">Fintech</MenuItem>
                  <MenuItem value="payment_provider">Payment Provider</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Country"
                value={onboardingData.country}
                onChange={(e) => setOnboardingData({...onboardingData, country: e.target.value})}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Currency"
                value={onboardingData.currency}
                onChange={(e) => setOnboardingData({...onboardingData, currency: e.target.value})}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Contact Person"
                value={onboardingData.contactPerson}
                onChange={(e) => setOnboardingData({...onboardingData, contactPerson: e.target.value})}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Email"
                type="email"
                value={onboardingData.email}
                onChange={(e) => setOnboardingData({...onboardingData, email: e.target.value})}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Phone"
                value={onboardingData.phone}
                onChange={(e) => setOnboardingData({...onboardingData, phone: e.target.value})}
                fullWidth
                required
              />
            </Grid>
          </Grid>
        );
      case 1:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField
                label="API Endpoint"
                value={onboardingData.apiEndpoint}
                onChange={(e) => setOnboardingData({...onboardingData, apiEndpoint: e.target.value})}
                fullWidth
                required
                placeholder="https://api.example.com/v1"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth required>
                <InputLabel>Authentication Type</InputLabel>
                <Select
                  value={onboardingData.authenticationType}
                  label="Authentication Type"
                  onChange={(e) => setOnboardingData({...onboardingData, authenticationType: e.target.value})}
                >
                  <MenuItem value="api_key">API Key</MenuItem>
                  <MenuItem value="oauth2">OAuth 2.0</MenuItem>
                  <MenuItem value="jwt">JWT</MenuItem>
                  <MenuItem value="basic">Basic Auth</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Webhook URL"
                value={onboardingData.webhookUrl}
                onChange={(e) => setOnboardingData({...onboardingData, webhookUrl: e.target.value})}
                fullWidth
                placeholder="https://your-domain.com/webhooks"
              />
            </Grid>
            <Grid item xs={12}>
              <FormLabel component="legend">Supported Payment Types</FormLabel>
              <Box display="flex" flexWrap="wrap" gap={1} mt={1}>
                {['Credit Card', 'Debit Card', 'Bank Transfer', 'Digital Wallet', 'Cryptocurrency'].map((type) => (
                  <FormControlLabel
                    key={type}
                    control={
                      <Checkbox
                        checked={onboardingData.supportedPaymentTypes.includes(type)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setOnboardingData({
                              ...onboardingData,
                              supportedPaymentTypes: [...onboardingData.supportedPaymentTypes, type]
                            });
                          } else {
                            setOnboardingData({
                              ...onboardingData,
                              supportedPaymentTypes: onboardingData.supportedPaymentTypes.filter(t => t !== type)
                            });
                          }
                        }}
                      />
                    }
                    label={type}
                  />
                ))}
              </Box>
            </Grid>
          </Grid>
        );
      case 2:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <FormLabel component="legend">Compliance Requirements</FormLabel>
              <Box display="flex" flexWrap="wrap" gap={1} mt={1}>
                {['PCI DSS', 'GDPR', 'SOX', 'AML', 'KYC', 'ISO 27001'].map((requirement) => (
                  <FormControlLabel
                    key={requirement}
                    control={
                      <Checkbox
                        checked={onboardingData.complianceRequirements.includes(requirement)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setOnboardingData({
                              ...onboardingData,
                              complianceRequirements: [...onboardingData.complianceRequirements, requirement]
                            });
                          } else {
                            setOnboardingData({
                              ...onboardingData,
                              complianceRequirements: onboardingData.complianceRequirements.filter(r => r !== requirement)
                            });
                          }
                        }}
                      />
                    }
                    label={requirement}
                  />
                ))}
              </Box>
            </Grid>
            <Grid item xs={12}>
              <Alert severity="info">
                Please ensure all compliance requirements are met before proceeding to testing phase.
              </Alert>
            </Grid>
          </Grid>
        );
      case 3:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Testing Checklist
              </Typography>
              <Box display="flex" flexDirection="column" gap={2}>
                {[
                  'API connectivity test',
                  'Authentication validation',
                  'Payment processing test',
                  'Webhook delivery test',
                  'Error handling validation',
                  'Performance testing',
                  'Security testing',
                ].map((test, index) => (
                  <FormControlLabel
                    key={index}
                    control={<Checkbox />}
                    label={test}
                  />
                ))}
              </Box>
            </Grid>
          </Grid>
        );
      case 4:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Alert severity="success" sx={{ mb: 2 }}>
                All tests completed successfully! Channel is ready for production.
              </Alert>
              <Typography variant="h6" gutterBottom>
                Go Live Configuration
              </Typography>
              <FormControlLabel
                control={<Checkbox />}
                label="Enable real-time monitoring"
              />
              <br />
              <FormControlLabel
                control={<Checkbox />}
                label="Set up alerting for failures"
              />
              <br />
              <FormControlLabel
                control={<Checkbox />}
                label="Enable audit logging"
              />
            </Grid>
          </Grid>
        );
      default:
        return 'Unknown step';
    }
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Channel Onboarding
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Onboard new payment channels and manage existing ones
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
        <Grid item xs={12} md={8}>
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
                      <TableCell>Country</TableCell>
                      <TableCell>Currency</TableCell>
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
                        <TableCell>
                          <Chip 
                            label={channel.type.replace('_', ' ').toUpperCase()} 
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <Chip 
                            label={channel.status.toUpperCase()} 
                            color={getStatusColor(channel.status) as any}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>{channel.country}</TableCell>
                        <TableCell>{channel.currency}</TableCell>
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

        {/* Onboarding Wizard */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                New Channel Onboarding
              </Typography>
              <Stepper activeStep={activeStep} orientation="vertical">
                {steps.map((label, index) => (
                  <Step key={label}>
                    <StepLabel>{label}</StepLabel>
                    <StepContent>
                      {getStepContent(index)}
                      <Box sx={{ mb: 2 }}>
                        <div>
                          <Button
                            variant="contained"
                            onClick={handleNext}
                            sx={{ mt: 1, mr: 1 }}
                          >
                            {index === steps.length - 1 ? 'Finish' : 'Continue'}
                          </Button>
                          <Button
                            disabled={index === 0}
                            onClick={handleBack}
                            sx={{ mt: 1, mr: 1 }}
                          >
                            Back
                          </Button>
                        </div>
                      </Box>
                    </StepContent>
                  </Step>
                ))}
              </Stepper>
              {activeStep === steps.length && (
                <Box>
                  <Typography>All steps completed - you&apos;re finished</Typography>
                  <Button onClick={handleReset} sx={{ mt: 1, mr: 1 }}>
                    Reset
                  </Button>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

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
                  label="Name"
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
                  label="Status"
                  value={selectedChannel.status}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Country"
                  value={selectedChannel.country}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Currency"
                  value={selectedChannel.currency}
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

      {/* Onboarding Dialog */}
      <Dialog open={onboardingDialogOpen} onClose={() => setOnboardingDialogOpen(false)} maxWidth="lg" fullWidth>
        <DialogTitle>
          Channel Onboarding Wizard
        </DialogTitle>
        <DialogContent>
          <Stepper activeStep={activeStep} sx={{ mb: 3 }}>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>
          {getStepContent(activeStep)}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOnboardingDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleBack} disabled={activeStep === 0}>
            Back
          </Button>
          <Button
            variant="contained"
            onClick={activeStep === steps.length - 1 ? () => setOnboardingDialogOpen(false) : handleNext}
          >
            {activeStep === steps.length - 1 ? 'Complete' : 'Next'}
          </Button>
        </DialogActions>
      </Dialog>
            </Grid>
  );
};

export default ChannelOnboarding;
