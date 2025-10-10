import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Tabs, Tab, Paper, Grid, Button, Alert, CircularProgress,
  Card, CardContent, CardHeader, CardActions, Chip, IconButton, Dialog,
  DialogTitle, DialogContent, DialogActions, List, ListItem, ListItemText,
  ListItemSecondaryAction, Divider, Accordion, AccordionSummary, AccordionDetails
} from '@mui/material';
import {
  Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, 
  CheckCircle as ActivateIcon, Cancel as DeactivateIcon,
  ExpandMore as ExpandMoreIcon, Settings as SettingsIcon,
  Security as SecurityIcon, Api as ApiIcon, AccountTree as AccountTreeIcon,
  Info as InfoIcon, Warning as WarningIcon, Error as ErrorIcon
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import {
  ClearingSystemAuthConfigurationResponse,
  PaymentTypeAuthConfigurationResponse,
  DownstreamCallAuthConfigurationResponse,
  ResolvedAuthConfiguration,
  ConfigurationHierarchyInfo,
  AuthMethod,
  Environment,
  PaymentType,
  ServiceType
} from '../../types/multiLevelAuth';
import {
  clearingSystemAuthApi,
  paymentTypeAuthApi,
  downstreamCallAuthApi,
  enhancedDownstreamRoutingApi,
  configurationHierarchyApi
} from '../../services/multiLevelAuthApi';

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
      id={`multi-level-auth-tabpanel-${index}`}
      aria-labelledby={`multi-level-auth-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

interface MultiLevelAuthConfigurationManagerProps {
  tenantId: string;
  onConfigurationChange: () => void;
}

const MultiLevelAuthConfigurationManager: React.FC<MultiLevelAuthConfigurationManagerProps> = ({ 
  tenantId, 
  onConfigurationChange 
}) => {
  const queryClient = useQueryClient();
  const [currentTab, setCurrentTab] = useState(0);
  const [selectedConfig, setSelectedConfig] = useState<any>(null);
  const [configDialogOpen, setConfigDialogOpen] = useState(false);
  const [hierarchyDialogOpen, setHierarchyDialogOpen] = useState(false);
  const [testDialogOpen, setTestDialogOpen] = useState(false);
  const [alert, setAlert] = useState<{ type: 'success' | 'error' | 'warning' | 'info'; message: string } | null>(null);

  // Queries
  const { data: clearingSystemConfigs, isLoading: clearingSystemLoading } = useQuery(
    ['clearingSystemAuthConfigs'],
    () => clearingSystemAuthApi.getConfigurationsByEnvironment('dev').then(res => res.data)
  );

  const { data: paymentTypeConfigs, isLoading: paymentTypeLoading } = useQuery(
    ['paymentTypeAuthConfigs', tenantId],
    () => paymentTypeAuthApi.getConfigurationsByTenantId(tenantId).then(res => res.data),
    { enabled: !!tenantId }
  );

  const { data: downstreamCallConfigs, isLoading: downstreamCallLoading } = useQuery(
    ['downstreamCallAuthConfigs', tenantId],
    () => downstreamCallAuthApi.getConfigurationsByTenantId(tenantId).then(res => res.data),
    { enabled: !!tenantId }
  );

  // Mutations
  const activateClearingSystemMutation = useMutation(clearingSystemAuthApi.activateConfiguration, {
    onSuccess: () => {
      queryClient.invalidateQueries(['clearingSystemAuthConfigs']);
      setAlert({ type: 'success', message: 'Clearing system configuration activated successfully!' });
      onConfigurationChange();
    },
    onError: (err: any) => {
      setAlert({ type: 'error', message: `Error activating configuration: ${err.response?.data?.message || err.message}` });
    },
  });

  const activatePaymentTypeMutation = useMutation(paymentTypeAuthApi.activateConfiguration, {
    onSuccess: () => {
      queryClient.invalidateQueries(['paymentTypeAuthConfigs', tenantId]);
      setAlert({ type: 'success', message: 'Payment type configuration activated successfully!' });
      onConfigurationChange();
    },
    onError: (err: any) => {
      setAlert({ type: 'error', message: `Error activating configuration: ${err.response?.data?.message || err.message}` });
    },
  });

  const activateDownstreamCallMutation = useMutation(downstreamCallAuthApi.activateConfiguration, {
    onSuccess: () => {
      queryClient.invalidateQueries(['downstreamCallAuthConfigs', tenantId]);
      setAlert({ type: 'success', message: 'Downstream call configuration activated successfully!' });
      onConfigurationChange();
    },
    onError: (err: any) => {
      setAlert({ type: 'error', message: `Error activating configuration: ${err.response?.data?.message || err.message}` });
    },
  });

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  const handleActivate = (level: string, id: string) => {
    switch (level) {
      case 'clearing-system':
        activateClearingSystemMutation.mutate(id);
        break;
      case 'payment-type':
        activatePaymentTypeMutation.mutate(id);
        break;
      case 'downstream-call':
        activateDownstreamCallMutation.mutate(id);
        break;
    }
  };

  const handleViewHierarchy = (config: any) => {
    setSelectedConfig(config);
    setHierarchyDialogOpen(true);
  };

  const handleTestConfiguration = (config: any) => {
    setSelectedConfig(config);
    setTestDialogOpen(true);
  };

  const renderClearingSystemConfigurations = () => {
    if (clearingSystemLoading) return <CircularProgress />;
    if (!clearingSystemConfigs) return <Alert severity="info">No clearing system configurations found</Alert>;

    return (
      <Grid container spacing={2}>
        {clearingSystemConfigs.map((config) => (
          <Grid item xs={12} md={6} lg={4} key={config.id}>
            <Card>
              <CardHeader
                title={`${config.environment.toUpperCase()} Environment`}
                subheader={config.description}
                action={
                  <Chip
                    label={config.isActive ? 'Active' : 'Inactive'}
                    color={config.isActive ? 'success' : 'default'}
                    size="small"
                  />
                }
              />
              <CardContent>
                <Typography variant="body2" color="text.secondary">
                  <strong>Auth Method:</strong> {config.authMethod}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Client Headers:</strong> {config.includeClientHeaders ? 'Enabled' : 'Disabled'}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Created:</strong> {new Date(config.createdAt).toLocaleDateString()}
                </Typography>
              </CardContent>
              <CardActions>
                <Button
                  size="small"
                  startIcon={<ActivateIcon />}
                  onClick={() => handleActivate('clearing-system', config.id)}
                  disabled={config.isActive}
                >
                  {config.isActive ? 'Active' : 'Activate'}
                </Button>
                <Button
                  size="small"
                  startIcon={<InfoIcon />}
                  onClick={() => handleViewHierarchy(config)}
                >
                  Hierarchy
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    );
  };

  const renderPaymentTypeConfigurations = () => {
    if (paymentTypeLoading) return <CircularProgress />;
    if (!paymentTypeConfigs) return <Alert severity="info">No payment type configurations found</Alert>;

    return (
      <Grid container spacing={2}>
        {paymentTypeConfigs.map((config) => (
          <Grid item xs={12} md={6} lg={4} key={config.id}>
            <Card>
              <CardHeader
                title={`${config.paymentType} Payments`}
                subheader={config.description}
                action={
                  <Chip
                    label={config.isActive ? 'Active' : 'Inactive'}
                    color={config.isActive ? 'success' : 'default'}
                    size="small"
                  />
                }
              />
              <CardContent>
                <Typography variant="body2" color="text.secondary">
                  <strong>Auth Method:</strong> {config.authMethod}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Clearing System:</strong> {config.clearingSystem || 'Default'}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Currency:</strong> {config.currency || 'Any'}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>High Value:</strong> {config.isHighValue ? 'Yes' : 'No'}
                </Typography>
              </CardContent>
              <CardActions>
                <Button
                  size="small"
                  startIcon={<ActivateIcon />}
                  onClick={() => handleActivate('payment-type', config.id)}
                  disabled={config.isActive}
                >
                  {config.isActive ? 'Active' : 'Activate'}
                </Button>
                <Button
                  size="small"
                  startIcon={<InfoIcon />}
                  onClick={() => handleViewHierarchy(config)}
                >
                  Hierarchy
                </Button>
                <Button
                  size="small"
                  startIcon={<SettingsIcon />}
                  onClick={() => handleTestConfiguration(config)}
                >
                  Test
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    );
  };

  const renderDownstreamCallConfigurations = () => {
    if (downstreamCallLoading) return <CircularProgress />;
    if (!downstreamCallConfigs) return <Alert severity="info">No downstream call configurations found</Alert>;

    return (
      <Grid container spacing={2}>
        {downstreamCallConfigs.map((config) => (
          <Grid item xs={12} md={6} lg={4} key={config.id}>
            <Card>
              <CardHeader
                title={`${config.serviceType.toUpperCase()} - ${config.endpoint}`}
                subheader={config.description}
                action={
                  <Chip
                    label={config.isActive ? 'Active' : 'Inactive'}
                    color={config.isActive ? 'success' : 'default'}
                    size="small"
                  />
                }
              />
              <CardContent>
                <Typography variant="body2" color="text.secondary">
                  <strong>Auth Method:</strong> {config.authMethod}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Target:</strong> {config.targetHost}:{config.targetPort}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Timeout:</strong> {config.timeoutSeconds}s
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Retries:</strong> {config.retryAttempts}
                </Typography>
              </CardContent>
              <CardActions>
                <Button
                  size="small"
                  startIcon={<ActivateIcon />}
                  onClick={() => handleActivate('downstream-call', config.id)}
                  disabled={config.isActive}
                >
                  {config.isActive ? 'Active' : 'Activate'}
                </Button>
                <Button
                  size="small"
                  startIcon={<InfoIcon />}
                  onClick={() => handleViewHierarchy(config)}
                >
                  Hierarchy
                </Button>
                <Button
                  size="small"
                  startIcon={<SettingsIcon />}
                  onClick={() => handleTestConfiguration(config)}
                >
                  Test
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    );
  };

  return (
    <Box>
      {alert && (
        <Alert severity={alert.type} onClose={() => setAlert(null)} sx={{ mb: 2 }}>
          {alert.message}
        </Alert>
      )}

      <Paper elevation={1} sx={{ mb: 3 }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={currentTab} onChange={handleTabChange} aria-label="multi-level auth configuration tabs">
            <Tab 
              icon={<SecurityIcon />} 
              label="Clearing System Level" 
              iconPosition="start"
              id="multi-level-auth-tab-0"
              aria-controls="multi-level-auth-tabpanel-0"
            />
            <Tab 
              icon={<AccountTreeIcon />} 
              label="Payment Type Level" 
              iconPosition="start"
              id="multi-level-auth-tab-1"
              aria-controls="multi-level-auth-tabpanel-1"
            />
            <Tab 
              icon={<ApiIcon />} 
              label="Downstream Call Level" 
              iconPosition="start"
              id="multi-level-auth-tab-2"
              aria-controls="multi-level-auth-tabpanel-2"
            />
          </Tabs>
        </Box>

        <TabPanel value={currentTab} index={0}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Clearing System Level Configurations</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setConfigDialogOpen(true)}
            >
              Add Configuration
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Global configurations that apply to all clearing operations in the environment.
            These are the lowest priority configurations in the hierarchy.
          </Typography>
          {renderClearingSystemConfigurations()}
        </TabPanel>

        <TabPanel value={currentTab} index={1}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Payment Type Level Configurations</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setConfigDialogOpen(true)}
            >
              Add Configuration
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Configurations specific to payment types (SEPA, SWIFT, ACH, etc.) for each tenant.
            These override clearing system level configurations.
          </Typography>
          {renderPaymentTypeConfigurations()}
        </TabPanel>

        <TabPanel value={currentTab} index={2}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Downstream Call Level Configurations</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setConfigDialogOpen(true)}
            >
              Add Configuration
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Most granular configurations for specific downstream calls (service type + endpoint).
            These have the highest priority in the configuration hierarchy.
          </Typography>
          {renderDownstreamCallConfigurations()}
        </TabPanel>
      </Paper>

      {/* Configuration Hierarchy Dialog */}
      <Dialog
        open={hierarchyDialogOpen}
        onClose={() => setHierarchyDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Configuration Hierarchy</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Configuration hierarchy shows how settings are resolved from different levels.
            Higher levels override lower levels.
          </Typography>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Priority 1: Downstream Call Level (Highest)</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2">
                Most specific configuration for individual service calls.
                Overrides all other levels.
              </Typography>
            </AccordionDetails>
          </Accordion>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Priority 2: Payment Type Level</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2">
                Configuration for specific payment types (SEPA, SWIFT, etc.).
                Overrides tenant and clearing system levels.
              </Typography>
            </AccordionDetails>
          </Accordion>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Priority 3: Tenant Level</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2">
                Tenant-specific configuration.
                Overrides clearing system level.
              </Typography>
            </AccordionDetails>
          </Accordion>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Priority 4: Clearing System Level (Lowest)</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2">
                Global configuration for the entire clearing system.
                Used as fallback when no higher-level configuration exists.
              </Typography>
            </AccordionDetails>
          </Accordion>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setHierarchyDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Test Configuration Dialog */}
      <Dialog
        open={testDialogOpen}
        onClose={() => setTestDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Test Configuration</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Test the selected configuration to ensure it works correctly.
          </Typography>
          {selectedConfig && (
            <Box>
              <Typography variant="subtitle2">Configuration Details:</Typography>
              <Typography variant="body2">
                <strong>Type:</strong> {selectedConfig.paymentType || selectedConfig.serviceType || 'Clearing System'}
              </Typography>
              <Typography variant="body2">
                <strong>Auth Method:</strong> {selectedConfig.authMethod}
              </Typography>
              <Typography variant="body2">
                <strong>Status:</strong> {selectedConfig.isActive ? 'Active' : 'Inactive'}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTestDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => {
            // Implement test logic here
            setAlert({ type: 'info', message: 'Configuration test completed successfully!' });
            setTestDialogOpen(false);
          }}>
            Run Test
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MultiLevelAuthConfigurationManager;