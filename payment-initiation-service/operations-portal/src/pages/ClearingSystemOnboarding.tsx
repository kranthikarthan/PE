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

interface ClearingSystem {
  id: string;
  name: string;
  type: string;
  status: 'active' | 'inactive' | 'pending';
  endpoint: string;
  createdAt: string;
  lastUpdated: string;
}

const ClearingSystemOnboarding: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [onboardingDialogOpen, setOnboardingDialogOpen] = useState(false);
  const [selectedSystem, setSelectedSystem] = useState<ClearingSystem | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);

  const [clearingSystems] = useState<ClearingSystem[]>([
    {
      id: 'CS-001',
      name: 'SAMOS Clearing System',
      type: 'SAMOS',
      status: 'active',
      endpoint: 'https://samos.payments.com/api',
      createdAt: '2025-10-15 09:00:00',
      lastUpdated: '2025-10-20 14:30:00',
    },
    {
      id: 'CS-002',
      name: 'BankservAfrica Clearing',
      type: 'BankservAfrica',
      status: 'active',
      endpoint: 'https://bankserv.payments.com/api',
      createdAt: '2025-10-10 10:15:00',
      lastUpdated: '2025-10-19 16:45:00',
    },
    {
      id: 'CS-003',
      name: 'RTC Clearing Network',
      type: 'RTC',
      status: 'pending',
      endpoint: 'https://rtc.payments.com/api',
      createdAt: '2025-10-20 11:20:00',
      lastUpdated: '2025-10-20 11:20:00',
    },
    {
      id: 'CS-004',
      name: 'PayShap Clearing',
      type: 'PayShap',
      status: 'active',
      endpoint: 'https://payshap.payments.com/api',
      createdAt: '2025-10-12 08:30:00',
      lastUpdated: '2025-10-18 12:15:00',
    },
  ]);

  const [formData, setFormData] = useState({
    systemName: '',
    systemType: '',
    endpoint: '',
    description: '',
    authentication: '',
    messageFormat: '',
    webhookUrl: '',
    enableLogging: false,
    enableMonitoring: false,
  });

  const steps = [
    'System Information',
    'Configuration',
    'Message Format',
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
      systemName: '',
      systemType: '',
      endpoint: '',
      description: '',
      authentication: '',
      messageFormat: '',
      webhookUrl: '',
      enableLogging: false,
      enableMonitoring: false,
    });
  };

  const handleViewDetails = (system: ClearingSystem) => {
    setSelectedSystem(system);
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
            Clearing System Onboarding
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Manage clearing systems and integration processes
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
        <Grid item xs={12}>
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
                      <TableCell>Endpoint</TableCell>
                      <TableCell>Created At</TableCell>
                      <TableCell>Last Updated</TableCell>
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
                        <TableCell>{system.type}</TableCell>
                        <TableCell>
                          <Chip 
                            label={system.status.toUpperCase()} 
                            color={getStatusColor(system.status) as any}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <Typography variant="caption" noWrap sx={{ maxWidth: 200 }}>
                            {system.endpoint}
                          </Typography>
                        </TableCell>
                        <TableCell>{system.createdAt}</TableCell>
                        <TableCell>{system.lastUpdated}</TableCell>
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
          Clearing System Onboarding Process
        </DialogTitle>
        <DialogContent>
          <Stepper activeStep={activeStep} orientation="vertical">
            <Step>
              <StepLabel>System Information</StepLabel>
              <StepContent>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      label="System Name"
                      value={formData.systemName}
                      onChange={(e) => setFormData({...formData, systemName: e.target.value})}
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <FormControl fullWidth>
                      <InputLabel>System Type</InputLabel>
                      <Select
                        value={formData.systemType}
                        label="System Type"
                        onChange={(e) => setFormData({...formData, systemType: e.target.value})}
                      >
                        <MenuItem value="SAMOS">SAMOS</MenuItem>
                        <MenuItem value="BankservAfrica">BankservAfrica</MenuItem>
                        <MenuItem value="RTC">RTC</MenuItem>
                        <MenuItem value="PayShap">PayShap</MenuItem>
                        <MenuItem value="SWIFT">SWIFT</MenuItem>
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
                      label="Webhook URL"
                      value={formData.webhookUrl}
                      onChange={(e) => setFormData({...formData, webhookUrl: e.target.value})}
                      fullWidth
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <FormControl fullWidth>
                      <InputLabel>Authentication Method</InputLabel>
                      <Select
                        value={formData.authentication}
                        label="Authentication Method"
                        onChange={(e) => setFormData({...formData, authentication: e.target.value})}
                      >
                        <MenuItem value="OAuth2">OAuth 2.0</MenuItem>
                        <MenuItem value="API Key">API Key</MenuItem>
                        <MenuItem value="Certificate">Certificate</MenuItem>
                        <MenuItem value="Basic">Basic Auth</MenuItem>
                      </Select>
                    </FormControl>
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
              <StepLabel>Message Format</StepLabel>
              <StepContent>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <FormControl fullWidth>
                      <InputLabel>Message Format</InputLabel>
                      <Select
                        value={formData.messageFormat}
                        label="Message Format"
                        onChange={(e) => setFormData({...formData, messageFormat: e.target.value})}
                      >
                        <MenuItem value="ISO20022">ISO 20022</MenuItem>
                        <MenuItem value="ISO8583">ISO 8583</MenuItem>
                        <MenuItem value="JSON">JSON</MenuItem>
                        <MenuItem value="XML">XML</MenuItem>
                        <MenuItem value="Fixed Width">Fixed Width</MenuItem>
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
                      label="Enable Message Logging"
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
                  This step will validate the clearing system configuration and perform connectivity tests.
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
                  Clearing system configuration is ready for deployment. Review the settings and deploy when ready.
                </Alert>
                <Box sx={{ mb: 2 }}>
                  <Button
                    variant="contained"
                    onClick={() => setOnboardingDialogOpen(false)}
                    sx={{ mt: 1, mr: 1 }}
                  >
                    Deploy System
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

      {/* System Details Dialog */}
      <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          System Details - {selectedSystem?.id}
        </DialogTitle>
        <DialogContent>
          {selectedSystem && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} md={6}>
                <TextField
                  label="System ID"
                  value={selectedSystem.id}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Status"
                  value={selectedSystem.status}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="System Name"
                  value={selectedSystem.name}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Type"
                  value={selectedSystem.type}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Endpoint"
                  value={selectedSystem.endpoint}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Created At"
                  value={selectedSystem.createdAt}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Last Updated"
                  value={selectedSystem.lastUpdated}
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
    </Box>
  );
};

export default ClearingSystemOnboarding;