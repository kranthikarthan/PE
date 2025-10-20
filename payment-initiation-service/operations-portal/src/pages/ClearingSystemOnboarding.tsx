import React, { useState } from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
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
  Switch,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Visibility,
  CheckCircle,
  Warning,
  Error,
  Settings,
} from '@mui/icons-material';

interface ClearingSystem {
  id: string;
  name: string;
  type: 'ach' | 'wire' | 'rtgs' | 'swift' | 'faster_payments';
  status: 'active' | 'inactive' | 'pending' | 'maintenance';
  country: string;
  currency: string;
  cutOffTime: string;
  settlementTime: string;
  createdAt: string;
  lastUpdated: string;
}

const ClearingSystemOnboarding: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [clearingSystems, setClearingSystems] = useState<ClearingSystem[]>([
    {
      id: 'CS-001',
      name: 'ACH Network',
      type: 'ach',
      status: 'active',
      country: 'United States',
      currency: 'USD',
      cutOffTime: '14:00 EST',
      settlementTime: 'T+1',
      createdAt: '2025-10-15 09:00:00',
      lastUpdated: '2025-10-20 14:30:00',
    },
    {
      id: 'CS-002',
      name: 'Faster Payments',
      type: 'faster_payments',
      status: 'active',
      country: 'United Kingdom',
      currency: 'GBP',
      cutOffTime: '18:00 GMT',
      settlementTime: 'Real-time',
      createdAt: '2025-10-10 11:15:00',
      lastUpdated: '2025-10-18 16:45:00',
    },
    {
      id: 'CS-003',
      name: 'SWIFT Network',
      type: 'swift',
      status: 'pending',
      country: 'Global',
      currency: 'Multi',
      cutOffTime: '17:00 CET',
      settlementTime: 'T+1',
      createdAt: '2025-10-20 10:30:00',
      lastUpdated: '2025-10-20 10:30:00',
    },
  ]);

  const [selectedSystem, setSelectedSystem] = useState<ClearingSystem | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [onboardingDialogOpen, setOnboardingDialogOpen] = useState(false);

  const [onboardingData, setOnboardingData] = useState({
    systemName: '',
    systemType: '',
    country: '',
    currency: '',
    cutOffTime: '',
    settlementTime: '',
    participantId: '',
    routingNumber: '',
    bicCode: '',
    contactPerson: '',
    email: '',
    phone: '',
    apiEndpoint: '',
    authenticationType: '',
    webhookUrl: '',
    supportedMessageTypes: [] as string[],
    complianceRequirements: [] as string[],
    riskSettings: {
      maxTransactionAmount: '',
      dailyLimit: '',
      monthlyLimit: '',
      fraudDetection: false,
      realTimeMonitoring: false,
    },
  });

  const steps = [
    'System Information',
    'Technical Configuration',
    'Compliance & Security',
    'Risk Management',
    'Testing & Validation',
    'Go Live',
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'success';
      case 'inactive': return 'default';
      case 'pending': return 'warning';
      case 'maintenance': return 'info';
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
      systemName: '',
      systemType: '',
      country: '',
      currency: '',
      cutOffTime: '',
      settlementTime: '',
      participantId: '',
      routingNumber: '',
      bicCode: '',
      contactPerson: '',
      email: '',
      phone: '',
      apiEndpoint: '',
      authenticationType: '',
      webhookUrl: '',
      supportedMessageTypes: [],
      complianceRequirements: [],
      riskSettings: {
        maxTransactionAmount: '',
        dailyLimit: '',
        monthlyLimit: '',
        fraudDetection: false,
        realTimeMonitoring: false,
      },
    });
  };

  const handleViewDetails = (system: ClearingSystem) => {
    setSelectedSystem(system);
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
                label="System Name"
                value={onboardingData.systemName}
                onChange={(e) => setOnboardingData({...onboardingData, systemName: e.target.value})}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth required>
                <InputLabel>System Type</InputLabel>
                <Select
                  value={onboardingData.systemType}
                  label="System Type"
                  onChange={(e) => setOnboardingData({...onboardingData, systemType: e.target.value})}
                >
                  <MenuItem value="ach">ACH</MenuItem>
                  <MenuItem value="wire">Wire Transfer</MenuItem>
                  <MenuItem value="rtgs">RTGS</MenuItem>
                  <MenuItem value="swift">SWIFT</MenuItem>
                  <MenuItem value="faster_payments">Faster Payments</MenuItem>
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
                label="Cut-off Time"
                value={onboardingData.cutOffTime}
                onChange={(e) => setOnboardingData({...onboardingData, cutOffTime: e.target.value})}
                fullWidth
                placeholder="e.g., 14:00 EST"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Settlement Time"
                value={onboardingData.settlementTime}
                onChange={(e) => setOnboardingData({...onboardingData, settlementTime: e.target.value})}
                fullWidth
                placeholder="e.g., T+1, Real-time"
              />
            </Grid>
          </Grid>
        );
      case 1:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <TextField
                label="Participant ID"
                value={onboardingData.participantId}
                onChange={(e) => setOnboardingData({...onboardingData, participantId: e.target.value})}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Routing Number"
                value={onboardingData.routingNumber}
                onChange={(e) => setOnboardingData({...onboardingData, routingNumber: e.target.value})}
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="BIC Code"
                value={onboardingData.bicCode}
                onChange={(e) => setOnboardingData({...onboardingData, bicCode: e.target.value})}
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="API Endpoint"
                value={onboardingData.apiEndpoint}
                onChange={(e) => setOnboardingData({...onboardingData, apiEndpoint: e.target.value})}
                fullWidth
                required
                placeholder="https://api.clearing-system.com/v1"
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
                  <MenuItem value="certificate">Certificate</MenuItem>
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
              <FormLabel component="legend">Supported Message Types</FormLabel>
              <Box display="flex" flexWrap="wrap" gap={1} mt={1}>
                {['MT103', 'MT202', 'MT205', 'MT940', 'MT950', 'ISO20022'].map((type) => (
                  <FormControlLabel
                    key={type}
                    control={
                      <Checkbox
                        checked={onboardingData.supportedMessageTypes.includes(type)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setOnboardingData({
                              ...onboardingData,
                              supportedMessageTypes: [...onboardingData.supportedMessageTypes, type]
                            });
                          } else {
                            setOnboardingData({
                              ...onboardingData,
                              supportedMessageTypes: onboardingData.supportedMessageTypes.filter(t => t !== type)
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
                {['PCI DSS', 'GDPR', 'SOX', 'AML', 'KYC', 'ISO 27001', 'Basel III', 'MiFID II'].map((requirement) => (
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
              <Alert severity="warning">
                Clearing systems require additional compliance certifications. Please ensure all requirements are met.
              </Alert>
            </Grid>
          </Grid>
        );
      case 3:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <TextField
                label="Max Transaction Amount"
                value={onboardingData.riskSettings.maxTransactionAmount}
                onChange={(e) => setOnboardingData({
                  ...onboardingData,
                  riskSettings: {...onboardingData.riskSettings, maxTransactionAmount: e.target.value}
                })}
                fullWidth
                placeholder="e.g., 1000000"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Daily Limit"
                value={onboardingData.riskSettings.dailyLimit}
                onChange={(e) => setOnboardingData({
                  ...onboardingData,
                  riskSettings: {...onboardingData.riskSettings, dailyLimit: e.target.value}
                })}
                fullWidth
                placeholder="e.g., 10000000"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Monthly Limit"
                value={onboardingData.riskSettings.monthlyLimit}
                onChange={(e) => setOnboardingData({
                  ...onboardingData,
                  riskSettings: {...onboardingData.riskSettings, monthlyLimit: e.target.value}
                })}
                fullWidth
                placeholder="e.g., 100000000"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Box display="flex" flexDirection="column" gap={2}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={onboardingData.riskSettings.fraudDetection}
                      onChange={(e) => setOnboardingData({
                        ...onboardingData,
                        riskSettings: {...onboardingData.riskSettings, fraudDetection: e.target.checked}
                      })}
                    />
                  }
                  label="Enable Fraud Detection"
                />
                <FormControlLabel
                  control={
                    <Switch
                      checked={onboardingData.riskSettings.realTimeMonitoring}
                      onChange={(e) => setOnboardingData({
                        ...onboardingData,
                        riskSettings: {...onboardingData.riskSettings, realTimeMonitoring: e.target.checked}
                      })}
                    />
                  }
                  label="Enable Real-time Monitoring"
                />
              </Box>
            </Grid>
          </Grid>
        );
      case 4:
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
                  'Message format validation',
                  'Settlement process test',
                  'Cut-off time validation',
                  'Error handling validation',
                  'Performance testing',
                  'Security testing',
                  'Compliance validation',
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
      case 5:
        return (
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Alert severity="success" sx={{ mb: 2 }}>
                All tests completed successfully! Clearing system is ready for production.
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
              <br />
              <FormControlLabel
                control={<Checkbox />}
                label="Enable settlement reporting"
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
            Clearing System Onboarding
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Onboard new clearing systems and manage existing ones
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleStartOnboarding}
        >
          New Clearing System
        </Button>
      </Box>

      <Grid container spacing={3}>
        {/* Existing Clearing Systems */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Existing Clearing Systems ({clearingSystems.length})
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>System ID</TableCell>
                      <TableCell>Name</TableCell>
                      <TableCell>Type</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Country</TableCell>
                      <TableCell>Currency</TableCell>
                      <TableCell>Cut-off Time</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {clearingSystems.map((system) => (
                      <TableRow key={system.id}>
                        <TableCell>
                          <Typography variant="subtitle2" fontWeight="bold">
                            {system.id}
                          </Typography>
                        </TableCell>
                        <TableCell>{system.name}</TableCell>
                        <TableCell>
                          <Chip 
                            label={system.type.replace('_', ' ').toUpperCase()} 
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <Chip 
                            label={system.status.toUpperCase()} 
                            color={getStatusColor(system.status) as any}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>{system.country}</TableCell>
                        <TableCell>{system.currency}</TableCell>
                        <TableCell>{system.cutOffTime}</TableCell>
                        <TableCell>
                          <Box display="flex" gap={1}>
                            <IconButton
                              size="small"
                              onClick={() => handleViewDetails(system)}
                            >
                              <Visibility />
                            </IconButton>
                            <IconButton size="small">
                              <Edit />
                            </IconButton>
                            <IconButton size="small">
                              <Settings />
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
                New Clearing System Onboarding
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

      {/* System Details Dialog */}
      <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Clearing System Details - {selectedSystem?.id}
        </DialogTitle>
        <DialogContent>
          {selectedSystem && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={6}>
                <TextField
                  label="System ID"
                  value={selectedSystem.id}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Name"
                  value={selectedSystem.name}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Type"
                  value={selectedSystem.type}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Status"
                  value={selectedSystem.status}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Country"
                  value={selectedSystem.country}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Currency"
                  value={selectedSystem.currency}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Cut-off Time"
                  value={selectedSystem.cutOffTime}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Settlement Time"
                  value={selectedSystem.settlementTime}
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
            Edit System
          </Button>
        </DialogActions>
      </Dialog>

      {/* Onboarding Dialog */}
      <Dialog open={onboardingDialogOpen} onClose={() => setOnboardingDialogOpen(false)} maxWidth="lg" fullWidth>
        <DialogTitle>
          Clearing System Onboarding Wizard
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
    </Box>
  );
};

export default ClearingSystemOnboarding;
