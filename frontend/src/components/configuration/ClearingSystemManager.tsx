import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Tabs,
  Tab,
  Button,
  IconButton,
  Chip,
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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  Snackbar,
  Tooltip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Divider,
  Badge,
  CircularProgress,
  Fab,
  Menu,
  ListItemIcon,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  TestTube as TestIcon,
  Settings as SettingsIcon,
  AccountTree as RouteIcon,
  Api as ApiIcon,
  Security as SecurityIcon,
  Speed as SpeedIcon,
  Public as PublicIcon,
  ExpandMore as ExpandMoreIcon,
  MoreVert as MoreVertIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  Refresh as RefreshIcon,
  Download as DownloadIcon,
  Upload as UploadIcon,
  ContentCopy as CopyIcon,
} from '@mui/icons-material';
import { useForm, Controller, useFieldArray } from 'react-hook-form';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import clearingSystemApiService from '../../services/clearingSystemApi';
import {
  ClearingSystem,
  ClearingSystemForm,
  ClearingSystemSummary,
  ClearingSystemStats,
  ClearingSystemEndpoint,
  TenantClearingSystemMapping,
  TenantMappingForm,
  ClearingSystemRoute,
  ClearingSystemTestRequest,
  ClearingSystemTestResponse,
} from '../../types/clearingSystem';

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
      id={`clearing-system-tabpanel-${index}`}
      aria-labelledby={`clearing-system-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const ClearingSystemManager: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [clearingSystems, setClearingSystems] = useState<ClearingSystemSummary[]>([]);
  const [tenantMappings, setTenantMappings] = useState<TenantClearingSystemMapping[]>([]);
  const [stats, setStats] = useState<ClearingSystemStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  // Dialog states
  const [clearingSystemDialog, setClearingSystemDialog] = useState(false);
  const [mappingDialog, setMappingDialog] = useState(false);
  const [testDialog, setTestDialog] = useState(false);
  const [routeDialog, setRouteDialog] = useState(false);
  const [selectedClearingSystem, setSelectedClearingSystem] = useState<ClearingSystem | null>(null);
  const [selectedMapping, setSelectedMapping] = useState<TenantClearingSystemMapping | null>(null);
  const [testResult, setTestResult] = useState<ClearingSystemTestResponse | null>(null);
  const [routeResult, setRouteResult] = useState<ClearingSystemRoute | null>(null);
  
  // Form states
  const [editingClearingSystem, setEditingClearingSystem] = useState<ClearingSystem | null>(null);
  const [editingMapping, setEditingMapping] = useState<TenantClearingSystemMapping | null>(null);
  
  // Menu states
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [menuItem, setMenuItem] = useState<any>(null);

  // Form setup
  const clearingSystemForm = useForm<ClearingSystemForm>({
    defaultValues: {
      code: '',
      name: '',
      description: '',
      countryCode: '',
      currency: '',
      isActive: true,
      processingMode: 'SYNCHRONOUS',
      timeoutSeconds: 30,
      endpointUrl: '',
      authenticationType: 'API_KEY',
      authenticationConfig: {},
      supportedMessageTypes: {},
      supportedPaymentTypes: {},
      supportedLocalInstruments: {},
      endpoints: [],
    },
  });

  const mappingForm = useForm<TenantMappingForm>({
    defaultValues: {
      tenantId: '',
      paymentType: '',
      localInstrumentCode: '',
      clearingSystemCode: '',
      priority: 1,
      isActive: true,
      description: '',
    },
  });

  const testForm = useForm<ClearingSystemTestRequest>({
    defaultValues: {
      clearingSystemId: '',
      endpointId: '',
      messageType: 'pacs008',
      messagePayload: {},
      expectedStatus: 200,
    },
  });

  const routeForm = useForm({
    defaultValues: {
      tenantId: '',
      paymentType: '',
      localInstrumentCode: '',
    },
  });

  // Load data on component mount
  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [clearingSystemsResponse, mappingsResponse, statsResponse] = await Promise.all([
        clearingSystemApiService.getClearingSystems(),
        clearingSystemApiService.getTenantMappings(),
        clearingSystemApiService.getClearingSystemStats(),
      ]);

      if (clearingSystemsResponse.success) {
        setClearingSystems(clearingSystemsResponse.data);
      }
      if (mappingsResponse.success) {
        setTenantMappings(mappingsResponse.data);
      }
      if (statsResponse.success) {
        setStats(statsResponse.data);
      }
    } catch (err) {
      setError('Failed to load clearing system data');
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleCreateClearingSystem = () => {
    setEditingClearingSystem(null);
    clearingSystemForm.reset();
    setClearingSystemDialog(true);
  };

  const handleEditClearingSystem = (clearingSystem: ClearingSystemSummary) => {
    // Load full clearing system details
    clearingSystemApiService.getClearingSystemById(clearingSystem.id).then(response => {
      if (response.success) {
        setEditingClearingSystem(response.data);
        clearingSystemForm.reset(response.data);
        setClearingSystemDialog(true);
      }
    });
  };

  const handleSaveClearingSystem = async (data: ClearingSystemForm) => {
    try {
      let response;
      if (editingClearingSystem) {
        response = await clearingSystemApiService.updateClearingSystem(editingClearingSystem.id, data);
      } else {
        response = await clearingSystemApiService.createClearingSystem(data);
      }

      if (response.success) {
        setSuccess(editingClearingSystem ? 'Clearing system updated successfully' : 'Clearing system created successfully');
        setClearingSystemDialog(false);
        loadData();
      } else {
        setError(response.message || 'Failed to save clearing system');
      }
    } catch (err) {
      setError('Failed to save clearing system');
    }
  };

  const handleDeleteClearingSystem = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this clearing system?')) {
      try {
        const response = await clearingSystemApiService.deleteClearingSystem(id);
        if (response.success) {
          setSuccess('Clearing system deleted successfully');
          loadData();
        } else {
          setError(response.message || 'Failed to delete clearing system');
        }
      } catch (err) {
        setError('Failed to delete clearing system');
      }
    }
  };

  const handleToggleClearingSystemStatus = async (id: string, isActive: boolean) => {
    try {
      const response = await clearingSystemApiService.toggleClearingSystemStatus(id, isActive);
      if (response.success) {
        setSuccess(`Clearing system ${isActive ? 'activated' : 'deactivated'} successfully`);
        loadData();
      } else {
        setError(response.message || 'Failed to update clearing system status');
      }
    } catch (err) {
      setError('Failed to update clearing system status');
    }
  };

  const handleCreateMapping = () => {
    setEditingMapping(null);
    mappingForm.reset();
    setMappingDialog(true);
  };

  const handleEditMapping = (mapping: TenantClearingSystemMapping) => {
    setEditingMapping(mapping);
    mappingForm.reset(mapping);
    setMappingDialog(true);
  };

  const handleSaveMapping = async (data: TenantMappingForm) => {
    try {
      let response;
      if (editingMapping) {
        response = await clearingSystemApiService.updateTenantMapping(editingMapping.id, data);
      } else {
        response = await clearingSystemApiService.createTenantMapping(data);
      }

      if (response.success) {
        setSuccess(editingMapping ? 'Tenant mapping updated successfully' : 'Tenant mapping created successfully');
        setMappingDialog(false);
        loadData();
      } else {
        setError(response.message || 'Failed to save tenant mapping');
      }
    } catch (err) {
      setError('Failed to save tenant mapping');
    }
  };

  const handleDeleteMapping = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this tenant mapping?')) {
      try {
        const response = await clearingSystemApiService.deleteTenantMapping(id);
        if (response.success) {
          setSuccess('Tenant mapping deleted successfully');
          loadData();
        } else {
          setError(response.message || 'Failed to delete tenant mapping');
        }
      } catch (err) {
        setError('Failed to delete tenant mapping');
      }
    }
  };

  const handleTestClearingSystem = async (data: ClearingSystemTestRequest) => {
    try {
      const response = await clearingSystemApiService.testClearingSystemEndpoint(data);
      if (response.success) {
        setTestResult(response.data);
      } else {
        setError(response.message || 'Failed to test clearing system');
      }
    } catch (err) {
      setError('Failed to test clearing system');
    }
  };

  const handleTestRouting = async (data: any) => {
    try {
      const response = await clearingSystemApiService.getClearingSystemRoute(
        data.tenantId,
        data.paymentType,
        data.localInstrumentCode
      );
      if (response.success) {
        setRouteResult(response.data);
      } else {
        setError(response.message || 'Failed to test routing');
      }
    } catch (err) {
      setError('Failed to test routing');
    }
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, item: any) => {
    setAnchorEl(event.currentTarget);
    setMenuItem(item);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setMenuItem(null);
  };

  const getStatusIcon = (isActive: boolean) => {
    return isActive ? <CheckCircleIcon color="success" /> : <ErrorIcon color="error" />;
  };

  const getStatusChip = (isActive: boolean) => {
    return (
      <Chip
        icon={getStatusIcon(isActive)}
        label={isActive ? 'Active' : 'Inactive'}
        color={isActive ? 'success' : 'error'}
        size="small"
      />
    );
  };

  const getProcessingModeChip = (mode: string) => {
    const colors = {
      SYNCHRONOUS: 'primary',
      ASYNCHRONOUS: 'secondary',
      BATCH: 'default',
    } as const;
    
    return (
      <Chip
        label={mode}
        color={colors[mode as keyof typeof colors] || 'default'}
        size="small"
      />
    );
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box>
        <Typography variant="h4" gutterBottom>
          Clearing System Management
        </Typography>
        
        {stats && (
          <Grid container spacing={2} sx={{ mb: 3 }}>
            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    Total Clearing Systems
                  </Typography>
                  <Typography variant="h4">
                    {stats.totalClearingSystems}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    Active Systems
                  </Typography>
                  <Typography variant="h4" color="success.main">
                    {stats.activeClearingSystems}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    Total Endpoints
                  </Typography>
                  <Typography variant="h4">
                    {stats.totalEndpoints}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    Tenant Mappings
                  </Typography>
                  <Typography variant="h4">
                    {stats.totalTenantMappings}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        )}

        <Card>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={tabValue} onChange={handleTabChange} aria-label="clearing system tabs">
              <Tab
                label="Clearing Systems"
                icon={<PublicIcon />}
                iconPosition="start"
              />
              <Tab
                label="Tenant Mappings"
                icon={<RouteIcon />}
                iconPosition="start"
              />
              <Tab
                label="Endpoints"
                icon={<ApiIcon />}
                iconPosition="start"
              />
              <Tab
                label="Testing"
                icon={<TestIcon />}
                iconPosition="start"
              />
            </Tabs>
          </Box>

          <TabPanel value={tabValue} index={0}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">Clearing Systems</Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={handleCreateClearingSystem}
              >
                Add Clearing System
              </Button>
            </Box>

            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Code</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Country</TableCell>
                    <TableCell>Currency</TableCell>
                    <TableCell>Processing Mode</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Mappings</TableCell>
                    <TableCell>Endpoints</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {clearingSystems.map((system) => (
                    <TableRow key={system.id}>
                      <TableCell>
                        <Typography variant="body2" fontWeight="bold">
                          {system.code}
                        </Typography>
                      </TableCell>
                      <TableCell>{system.name}</TableCell>
                      <TableCell>{system.countryCode}</TableCell>
                      <TableCell>{system.currency}</TableCell>
                      <TableCell>{getProcessingModeChip(system.processingMode)}</TableCell>
                      <TableCell>{getStatusChip(system.isActive)}</TableCell>
                      <TableCell>
                        <Badge badgeContent={system.tenantMappingCount} color="primary">
                          <RouteIcon />
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge badgeContent={system.endpointCount} color="secondary">
                          <ApiIcon />
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <IconButton
                          size="small"
                          onClick={(e) => handleMenuOpen(e, system)}
                        >
                          <MoreVertIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </TabPanel>

          <TabPanel value={tabValue} index={1}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">Tenant Mappings</Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={handleCreateMapping}
              >
                Add Mapping
              </Button>
            </Box>

            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Tenant ID</TableCell>
                    <TableCell>Payment Type</TableCell>
                    <TableCell>Local Instrument</TableCell>
                    <TableCell>Clearing System</TableCell>
                    <TableCell>Priority</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {tenantMappings.map((mapping) => (
                    <TableRow key={mapping.id}>
                      <TableCell>{mapping.tenantId}</TableCell>
                      <TableCell>{mapping.paymentType}</TableCell>
                      <TableCell>{mapping.localInstrumentCode || '-'}</TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight="bold">
                          {mapping.clearingSystemCode}
                        </Typography>
                        {mapping.clearingSystemName && (
                          <Typography variant="caption" color="textSecondary">
                            {mapping.clearingSystemName}
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>{mapping.priority}</TableCell>
                      <TableCell>{getStatusChip(mapping.isActive)}</TableCell>
                      <TableCell>
                        <IconButton
                          size="small"
                          onClick={(e) => handleMenuOpen(e, mapping)}
                        >
                          <MoreVertIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </TabPanel>

          <TabPanel value={tabValue} index={2}>
            <Typography variant="h6" gutterBottom>
              Endpoints
            </Typography>
            <Alert severity="info">
              Endpoints are managed within each clearing system configuration.
              Use the "Edit" action on clearing systems to manage their endpoints.
            </Alert>
          </TabPanel>

          <TabPanel value={tabValue} index={3}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">Testing</Typography>
              <Box>
                <Button
                  variant="outlined"
                  startIcon={<TestIcon />}
                  onClick={() => setTestDialog(true)}
                  sx={{ mr: 1 }}
                >
                  Test Endpoint
                </Button>
                <Button
                  variant="outlined"
                  startIcon={<RouteIcon />}
                  onClick={() => setRouteDialog(true)}
                >
                  Test Routing
                </Button>
              </Box>
            </Box>

            {testResult && (
              <Card sx={{ mb: 2 }}>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Test Result
                  </Typography>
                  <Box display="flex" alignItems="center" mb={2}>
                    {testResult.success ? (
                      <CheckCircleIcon color="success" />
                    ) : (
                      <ErrorIcon color="error" />
                    )}
                    <Typography variant="body1" sx={{ ml: 1 }}>
                      Status: {testResult.status} - {testResult.success ? 'Success' : 'Failed'}
                    </Typography>
                  </Box>
                  <Typography variant="body2">
                    Processing Time: {testResult.processingTimeMs}ms
                  </Typography>
                  {testResult.errorMessage && (
                    <Alert severity="error" sx={{ mt: 2 }}>
                      {testResult.errorMessage}
                    </Alert>
                  )}
                </CardContent>
              </Card>
            )}

            {routeResult && (
              <Card sx={{ mb: 2 }}>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Routing Result
                  </Typography>
                  <Grid container spacing={2}>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="textSecondary">
                        Clearing System
                      </Typography>
                      <Typography variant="body1" fontWeight="bold">
                        {routeResult.clearingSystemCode} - {routeResult.clearingSystemName}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="textSecondary">
                        Endpoint URL
                      </Typography>
                      <Typography variant="body1">
                        {routeResult.endpointUrl}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="textSecondary">
                        Authentication
                      </Typography>
                      <Typography variant="body1">
                        {routeResult.authenticationType}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="textSecondary">
                        Priority
                      </Typography>
                      <Typography variant="body1">
                        {routeResult.routingPriority}
                      </Typography>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            )}
          </TabPanel>
        </Card>

        {/* Context Menu */}
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
        >
          <MenuItem onClick={() => {
            if (menuItem) {
              if ('code' in menuItem) {
                handleEditClearingSystem(menuItem);
              } else {
                handleEditMapping(menuItem);
              }
            }
            handleMenuClose();
          }}>
            <ListItemIcon>
              <EditIcon fontSize="small" />
            </ListItemIcon>
            Edit
          </MenuItem>
          <MenuItem onClick={() => {
            if (menuItem) {
              if ('code' in menuItem) {
                handleToggleClearingSystemStatus(menuItem.id, !menuItem.isActive);
              } else {
                // Toggle mapping status
              }
            }
            handleMenuClose();
          }}>
            <ListItemIcon>
              <SettingsIcon fontSize="small" />
            </ListItemIcon>
            Toggle Status
          </MenuItem>
          <MenuItem onClick={() => {
            if (menuItem) {
              if ('code' in menuItem) {
                handleDeleteClearingSystem(menuItem.id);
              } else {
                handleDeleteMapping(menuItem.id);
              }
            }
            handleMenuClose();
          }}>
            <ListItemIcon>
              <DeleteIcon fontSize="small" />
            </ListItemIcon>
            Delete
          </MenuItem>
        </Menu>

        {/* Clearing System Dialog */}
        <Dialog
          open={clearingSystemDialog}
          onClose={() => setClearingSystemDialog(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>
            {editingClearingSystem ? 'Edit Clearing System' : 'Create Clearing System'}
          </DialogTitle>
          <form onSubmit={clearingSystemForm.handleSubmit(handleSaveClearingSystem)}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="code"
                    control={clearingSystemForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Code"
                        fullWidth
                        required
                        disabled={!!editingClearingSystem}
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="name"
                    control={clearingSystemForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Name"
                        fullWidth
                        required
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Controller
                    name="description"
                    control={clearingSystemForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Description"
                        fullWidth
                        multiline
                        rows={2}
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Controller
                    name="countryCode"
                    control={clearingSystemForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Country Code"
                        fullWidth
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Controller
                    name="currency"
                    control={clearingSystemForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Currency"
                        fullWidth
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Controller
                    name="processingMode"
                    control={clearingSystemForm.control}
                    render={({ field }) => (
                      <FormControl fullWidth>
                        <InputLabel>Processing Mode</InputLabel>
                        <Select {...field} label="Processing Mode">
                          <MenuItem value="SYNCHRONOUS">Synchronous</MenuItem>
                          <MenuItem value="ASYNCHRONOUS">Asynchronous</MenuItem>
                          <MenuItem value="BATCH">Batch</MenuItem>
                        </Select>
                      </FormControl>
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="timeoutSeconds"
                    control={clearingSystemForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Timeout (seconds)"
                        type="number"
                        fullWidth
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="authenticationType"
                    control={clearingSystemForm.control}
                    render={({ field }) => (
                      <FormControl fullWidth>
                        <InputLabel>Authentication Type</InputLabel>
                        <Select {...field} label="Authentication Type">
                          <MenuItem value="NONE">None</MenuItem>
                          <MenuItem value="API_KEY">API Key</MenuItem>
                          <MenuItem value="JWT">JWT</MenuItem>
                          <MenuItem value="OAUTH2">OAuth2</MenuItem>
                          <MenuItem value="MTLS">mTLS</MenuItem>
                        </Select>
                      </FormControl>
                    )}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Controller
                    name="endpointUrl"
                    control={clearingSystemForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Base Endpoint URL"
                        fullWidth
                        required
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Controller
                    name="isActive"
                    control={clearingSystemForm.control}
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
              <Button onClick={() => setClearingSystemDialog(false)}>
                Cancel
              </Button>
              <Button type="submit" variant="contained">
                {editingClearingSystem ? 'Update' : 'Create'}
              </Button>
            </DialogActions>
          </form>
        </Dialog>

        {/* Tenant Mapping Dialog */}
        <Dialog
          open={mappingDialog}
          onClose={() => setMappingDialog(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>
            {editingMapping ? 'Edit Tenant Mapping' : 'Create Tenant Mapping'}
          </DialogTitle>
          <form onSubmit={mappingForm.handleSubmit(handleSaveMapping)}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="tenantId"
                    control={mappingForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Tenant ID"
                        fullWidth
                        required
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="paymentType"
                    control={mappingForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Payment Type"
                        fullWidth
                        required
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="localInstrumentCode"
                    control={mappingForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Local Instrument Code"
                        fullWidth
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="clearingSystemCode"
                    control={mappingForm.control}
                    render={({ field }) => (
                      <FormControl fullWidth required>
                        <InputLabel>Clearing System</InputLabel>
                        <Select {...field} label="Clearing System">
                          {clearingSystems.map((system) => (
                            <MenuItem key={system.id} value={system.code}>
                              {system.code} - {system.name}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="priority"
                    control={mappingForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Priority"
                        type="number"
                        fullWidth
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="isActive"
                    control={mappingForm.control}
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
                    control={mappingForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Description"
                        fullWidth
                        multiline
                        rows={2}
                      />
                    )}
                  />
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setMappingDialog(false)}>
                Cancel
              </Button>
              <Button type="submit" variant="contained">
                {editingMapping ? 'Update' : 'Create'}
              </Button>
            </DialogActions>
          </form>
        </Dialog>

        {/* Test Dialog */}
        <Dialog
          open={testDialog}
          onClose={() => setTestDialog(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>Test Clearing System Endpoint</DialogTitle>
          <form onSubmit={testForm.handleSubmit(handleTestClearingSystem)}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <Controller
                    name="clearingSystemId"
                    control={testForm.control}
                    render={({ field }) => (
                      <FormControl fullWidth required>
                        <InputLabel>Clearing System</InputLabel>
                        <Select {...field} label="Clearing System">
                          {clearingSystems.map((system) => (
                            <MenuItem key={system.id} value={system.id}>
                              {system.code} - {system.name}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    )}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Controller
                    name="messageType"
                    control={testForm.control}
                    render={({ field }) => (
                      <FormControl fullWidth required>
                        <InputLabel>Message Type</InputLabel>
                        <Select {...field} label="Message Type">
                          <MenuItem value="pacs008">PACS.008 - FI to FI Customer Credit Transfer</MenuItem>
                          <MenuItem value="pacs002">PACS.002 - FI to FI Payment Status Report</MenuItem>
                          <MenuItem value="pacs004">PACS.004 - Payment Return</MenuItem>
                          <MenuItem value="pacs007">PACS.007 - Payment Cancellation Request</MenuItem>
                          <MenuItem value="pacs028">PACS.028 - Payment Status Request</MenuItem>
                          <MenuItem value="pain001">PAIN.001 - Customer Credit Transfer Initiation</MenuItem>
                          <MenuItem value="pain002">PAIN.002 - Customer Payment Status Report</MenuItem>
                          <MenuItem value="camt054">CAMT.054 - Bank to Customer Debit Credit Notification</MenuItem>
                          <MenuItem value="camt055">CAMT.055 - FI to FI Payment Cancellation Request</MenuItem>
                          <MenuItem value="camt056">CAMT.056 - FI to FI Payment Status Request</MenuItem>
                          <MenuItem value="camt029">CAMT.029 - Resolution of Investigation</MenuItem>
                          <MenuItem value="status">Status - General Status Messages</MenuItem>
                        </Select>
                      </FormControl>
                    )}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Controller
                    name="expectedStatus"
                    control={testForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Expected Status Code"
                        type="number"
                        fullWidth
                      />
                    )}
                  />
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setTestDialog(false)}>
                Cancel
              </Button>
              <Button type="submit" variant="contained">
                Test
              </Button>
            </DialogActions>
          </form>
        </Dialog>

        {/* Route Test Dialog */}
        <Dialog
          open={routeDialog}
          onClose={() => setRouteDialog(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>Test Clearing System Routing</DialogTitle>
          <form onSubmit={routeForm.handleSubmit(handleTestRouting)}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <Controller
                    name="tenantId"
                    control={routeForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Tenant ID"
                        fullWidth
                        required
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="paymentType"
                    control={routeForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Payment Type"
                        fullWidth
                        required
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Controller
                    name="localInstrumentCode"
                    control={routeForm.control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Local Instrument Code"
                        fullWidth
                      />
                    )}
                  />
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setRouteDialog(false)}>
                Cancel
              </Button>
              <Button type="submit" variant="contained">
                Test Routing
              </Button>
            </DialogActions>
          </form>
        </Dialog>

        {/* Snackbars */}
        <Snackbar
          open={!!error}
          autoHideDuration={6000}
          onClose={() => setError(null)}
        >
          <Alert onClose={() => setError(null)} severity="error">
            {error}
          </Alert>
        </Snackbar>

        <Snackbar
          open={!!success}
          autoHideDuration={6000}
          onClose={() => setSuccess(null)}
        >
          <Alert onClose={() => setSuccess(null)} severity="success">
            {success}
          </Alert>
        </Snackbar>
      </Box>
    </LocalizationProvider>
  );
};

export default ClearingSystemManager;