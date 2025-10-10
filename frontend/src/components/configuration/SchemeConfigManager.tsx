import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Switch,
  FormControlLabel,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  Tabs,
  Tab,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Divider,
  Tooltip,
  Badge
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  PlayArrow as TestIcon,
  Settings as SettingsIcon,
  ExpandMore as ExpandMoreIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon
} from '@mui/icons-material';
import { useForm, Controller, useFieldArray } from 'react-hook-form';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import {
  SchemeInteractionConfig,
  SchemeConfigForm,
  InteractionMode,
  MessageFormat,
  ResponseMode,
  RetryPolicy,
  AuthenticationConfig,
  EndpointConfig,
  SchemeConfigTestRequest,
  SchemeConfigTestResponse
} from '../../types/scheme';
import apiClient from '../../services/apiClient';

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
      id={`scheme-tabpanel-${index}`}
      aria-labelledby={`scheme-tab-${index}`}
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

const SchemeConfigManager: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [configs, setConfigs] = useState<SchemeInteractionConfig[]>([]);
  const [selectedConfig, setSelectedConfig] = useState<SchemeInteractionConfig | null>(null);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isTestDialogOpen, setIsTestDialogOpen] = useState(false);
  const [testResults, setTestResults] = useState<SchemeConfigTestResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { control, handleSubmit, reset, watch, formState: { errors } } = useForm<SchemeConfigForm>({
    defaultValues: {
      name: '',
      description: '',
      isActive: true,
      interactionMode: InteractionMode.SYNCHRONOUS,
      messageFormat: MessageFormat.JSON,
      responseMode: ResponseMode.IMMEDIATE,
      timeoutMs: 30000,
      retryPolicy: {
        maxRetries: 3,
        backoffMs: 1000,
        exponentialBackoff: true,
        retryableStatusCodes: [500, 502, 503, 504]
      },
      authentication: {
        type: 'NONE'
      },
      endpoints: []
    }
  });

  const { fields: endpointFields, append: appendEndpoint, remove: removeEndpoint } = useFieldArray({
    control,
    name: 'endpoints'
  });

  const watchedInteractionMode = watch('interactionMode');
  const watchedMessageFormat = watch('messageFormat');

  useEffect(() => {
    loadConfigurations();
  }, []);

  const mapConfigResponse = (config: any): SchemeInteractionConfig => ({
    id: config.id,
    name: config.name,
    description: config.description ?? '',
    isActive: config.isActive ?? false,
    interactionMode: config.interactionMode as InteractionMode,
    messageFormat: config.messageFormat as MessageFormat,
    responseMode: config.responseMode as ResponseMode,
    timeoutMs: Number(config.timeoutMs ?? 0),
    retryPolicy: {
      maxRetries: config.retryPolicy?.maxRetries ?? 0,
      backoffMs: Number(config.retryPolicy?.backoffMs ?? 0),
      exponentialBackoff: Boolean(config.retryPolicy?.exponentialBackoff ?? false),
      retryableStatusCodes: config.retryPolicy?.retryableStatusCodes ?? []
    },
    authentication: {
      type: (config.authentication?.type ?? 'NONE') as AuthenticationConfig['type'],
      apiKey: config.authentication?.apiKey,
      jwtSecret: config.authentication?.jwtSecret,
      oauth2Config: config.authentication?.oauth2Config,
      certificatePath: config.authentication?.certificatePath,
      keyPath: config.authentication?.keyPath
    },
    endpoints: (config.endpoints ?? []).map((endpoint: any, index: number) => ({
      id: endpoint.id ?? `endpoint-${index}`,
      name: endpoint.name,
      url: endpoint.url,
      method: endpoint.method,
      isActive: endpoint.isActive ?? false,
      timeoutMs: Number(endpoint.timeoutMs ?? 0),
      headers: endpoint.headers ?? {},
      supportedMessageTypes: endpoint.supportedMessageTypes ?? [],
      priority: endpoint.priority ?? 1
    })),
    createdAt: config.createdAt,
    updatedAt: config.updatedAt,
    createdBy: config.createdBy ?? '',
    updatedBy: config.updatedBy ?? ''
  });

  const mapFormToRequest = (data: SchemeConfigForm) => ({
    name: data.name,
    description: data.description,
    isActive: data.isActive,
    interactionMode: data.interactionMode,
    messageFormat: data.messageFormat,
    responseMode: data.responseMode,
    timeoutMs: data.timeoutMs,
    retryPolicy: {
      maxRetries: data.retryPolicy.maxRetries,
      backoffMs: data.retryPolicy.backoffMs,
      exponentialBackoff: data.retryPolicy.exponentialBackoff,
      retryableStatusCodes: data.retryPolicy.retryableStatusCodes
    },
    authentication: {
      type: data.authentication.type,
      apiKey: data.authentication.apiKey,
      jwtSecret: data.authentication.jwtSecret,
      oauth2Config: data.authentication.oauth2Config,
      certificatePath: data.authentication.certificatePath,
      keyPath: data.authentication.keyPath
    },
    endpoints: data.endpoints.map((endpoint) => ({
      name: endpoint.name,
      url: endpoint.url,
      method: endpoint.method,
      isActive: endpoint.isActive,
      timeoutMs: endpoint.timeoutMs,
      headers: endpoint.headers,
      supportedMessageTypes: endpoint.supportedMessageTypes,
      priority: endpoint.priority
    }))
  });

  const mapConfigToForm = (config: SchemeInteractionConfig): SchemeConfigForm => ({
    name: config.name,
    description: config.description,
    isActive: config.isActive,
    interactionMode: config.interactionMode,
    messageFormat: config.messageFormat,
    responseMode: config.responseMode,
    timeoutMs: config.timeoutMs,
    retryPolicy: config.retryPolicy,
    authentication: config.authentication,
    endpoints: config.endpoints.map(({ id: _id, ...rest }) => rest)
  });

  const loadConfigurations = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/api/v1/scheme/configurations', {
        params: { page: 0, size: 50, sortBy: 'updatedAt', sortDirection: 'desc' }
      });
      const mapped = (response.data?.content ?? []).map(mapConfigResponse);
      setConfigs(mapped);
    } catch (err) {
      setError('Failed to load configurations');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateConfig = async (data: SchemeConfigForm) => {
    try {
      setLoading(true);
      await apiClient.post('/api/v1/scheme/configurations', mapFormToRequest(data));
      setIsCreateDialogOpen(false);
      reset();
      loadConfigurations();
    } catch (err) {
      setError('Failed to create configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleEditConfig = async (data: SchemeConfigForm) => {
    try {
      setLoading(true);
      if (!selectedConfig) {
        throw new Error('No configuration selected');
      }
      await apiClient.put(`/api/v1/scheme/configurations/${selectedConfig.id}`, mapFormToRequest(data));
      setIsEditDialogOpen(false);
      reset();
      loadConfigurations();
    } catch (err) {
      setError('Failed to update configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteConfig = async (configId: string) => {
    if (window.confirm('Are you sure you want to delete this configuration?')) {
      try {
        setLoading(true);
        await apiClient.delete(`/api/v1/scheme/configurations/${configId}`);
        loadConfigurations();
      } catch (err) {
        setError('Failed to delete configuration');
      } finally {
        setLoading(false);
      }
    }
  };

  const handleTestConfig = async (configId: string) => {
    try {
      setLoading(true);
      const requestPayload: SchemeConfigTestRequest = {
        configId,
        validateOnly: false,
        testMessage: {
          messageType: 'test-message',
          messageId: `TEST-${Date.now()}`,
          correlationId: `CORR-${Date.now()}`,
          format: MessageFormat.JSON,
          interactionMode: InteractionMode.SYNCHRONOUS,
          payload: { test: true }
        }
      };
      const response = await apiClient.post('/api/v1/scheme/configurations/test', requestPayload);
      setTestResults(response.data as SchemeConfigTestResponse);
      setIsTestDialogOpen(true);
    } catch (err) {
      setError('Failed to test configuration');
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (isActive: boolean) => {
    return isActive ? (
      <CheckCircleIcon color="success" />
    ) : (
      <ErrorIcon color="error" />
    );
  };

  const getModeChip = (mode: InteractionMode) => {
    const colors = {
      [InteractionMode.SYNCHRONOUS]: 'primary',
      [InteractionMode.ASYNCHRONOUS]: 'secondary',
      [InteractionMode.HYBRID]: 'info'
    } as const;
    
    return (
      <Chip
        label={mode}
        color={colors[mode]}
        size="small"
        variant="outlined"
      />
    );
  };

  const getFormatChip = (format: MessageFormat) => {
    const colors = {
      [MessageFormat.JSON]: 'success',
      [MessageFormat.XML]: 'warning',
      [MessageFormat.BOTH]: 'info'
    } as const;
    
    return (
      <Chip
        label={format}
        color={colors[format]}
        size="small"
        variant="outlined"
      />
    );
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ width: '100%' }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={(e, newValue) => setActiveTab(newValue)}>
            <Tab label="Configurations" />
            <Tab label="Statistics" />
            <Tab label="Test Results" />
          </Tabs>
        </Box>

        <TabPanel value={activeTab} index={0}>
          <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h5" component="h1">
              Scheme Interaction Configurations
            </Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setIsCreateDialogOpen(true)}
            >
              Create Configuration
            </Button>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
              {error}
            </Alert>
          )}

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Mode</TableCell>
                  <TableCell>Format</TableCell>
                  <TableCell>Endpoints</TableCell>
                  <TableCell>Last Updated</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {configs.map((config) => (
                  <TableRow key={config.id}>
                    <TableCell>
                      <Box>
                        <Typography variant="subtitle2">{config.name}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {config.description}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {getStatusIcon(config.isActive)}
                        <Typography variant="caption">
                          {config.isActive ? 'Active' : 'Inactive'}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>{getModeChip(config.interactionMode)}</TableCell>
                    <TableCell>{getFormatChip(config.messageFormat)}</TableCell>
                    <TableCell>
                      <Badge badgeContent={config.endpoints.length} color="primary">
                        <SettingsIcon />
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Typography variant="caption">
                        {new Date(config.updatedAt).toLocaleDateString()}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <Tooltip title="Test Configuration">
                          <IconButton
                            size="small"
                            onClick={() => handleTestConfig(config.id)}
                          >
                            <TestIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Edit Configuration">
                          <IconButton
                            size="small"
                            onClick={() => {
                              setSelectedConfig(config);
                              reset(mapConfigToForm(config));
                              setIsEditDialogOpen(true);
                            }}
                          >
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete Configuration">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleDeleteConfig(config.id)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </TabPanel>

        <TabPanel value={activeTab} index={1}>
          <Typography variant="h5" component="h1" sx={{ mb: 3 }}>
            Scheme Interaction Statistics
          </Typography>
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle2" color="text.secondary">
                    Total Configurations
                  </Typography>
                  <Typography variant="h4">{configs.length}</Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle2" color="text.secondary">
                    Active Configurations
                  </Typography>
                  <Typography variant="h4">{configs.filter((config) => config.isActive).length}</Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle2" color="text.secondary">
                    Interaction Modes
                  </Typography>
                  <Box>
                    {Object.values(InteractionMode).map((mode) => (
                      <Typography key={mode} variant="body2">
                        {mode}: {configs.filter((config) => config.interactionMode === mode).length}
                      </Typography>
                    ))}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={2}>
          <Typography variant="h5" component="h1" sx={{ mb: 3 }}>
            Test Results
          </Typography>
          {testResults ? (
            <Card>
              <CardContent>
                <Typography variant="subtitle1" gutterBottom>
                  Result
                </Typography>
                <Typography variant="body2">Success: {testResults.success ? 'Yes' : 'No'}</Typography>
                <Typography variant="body2">Response Time: {testResults.responseTimeMs ?? 'N/A'} ms</Typography>
                {testResults.response && (
                  <Box sx={{ mt: 2 }}>
                    <Typography variant="subtitle2">Response Details</Typography>
                    <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
                      {JSON.stringify(testResults.response, null, 2)}
                    </pre>
                  </Box>
                )}
                {testResults.error && (
                  <Box sx={{ mt: 2 }}>
                    <Typography variant="subtitle2" color="error">
                      Error
                    </Typography>
                    <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
                      {JSON.stringify(testResults.error, null, 2)}
                    </pre>
                  </Box>
                )}
              </CardContent>
            </Card>
          ) : (
            <Alert severity="info">No test results available. Run a configuration test to view results.</Alert>
          )}
        </TabPanel>

        {/* Create Configuration Dialog */}
        <Dialog
          open={isCreateDialogOpen}
          onClose={() => setIsCreateDialogOpen(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>Create Scheme Configuration</DialogTitle>
          <DialogContent>
            <SchemeConfigForm
              control={control}
              errors={errors}
              endpointFields={endpointFields}
              appendEndpoint={appendEndpoint}
              removeEndpoint={removeEndpoint}
              watchedInteractionMode={watchedInteractionMode}
              watchedMessageFormat={watchedMessageFormat}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setIsCreateDialogOpen(false)}>Cancel</Button>
            <Button
              onClick={handleSubmit(handleCreateConfig)}
              variant="contained"
              disabled={loading}
            >
              Create
            </Button>
          </DialogActions>
        </Dialog>

        {/* Edit Configuration Dialog */}
        <Dialog
          open={isEditDialogOpen}
          onClose={() => setIsEditDialogOpen(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>Edit Scheme Configuration</DialogTitle>
          <DialogContent>
            <SchemeConfigForm
              control={control}
              errors={errors}
              endpointFields={endpointFields}
              appendEndpoint={appendEndpoint}
              removeEndpoint={removeEndpoint}
              watchedInteractionMode={watchedInteractionMode}
              watchedMessageFormat={watchedMessageFormat}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setIsEditDialogOpen(false)}>Cancel</Button>
            <Button
              onClick={handleSubmit(handleEditConfig)}
              variant="contained"
              disabled={loading}
            >
              Update
            </Button>
          </DialogActions>
        </Dialog>

        {/* Test Results Dialog */}
        <Dialog
          open={isTestDialogOpen}
          onClose={() => setIsTestDialogOpen(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>Test Results</DialogTitle>
          <DialogContent>
            {testResults && (
              <Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                  {testResults.success ? (
                    <CheckCircleIcon color="success" />
                  ) : (
                    <ErrorIcon color="error" />
                  )}
                  <Typography variant="h6">
                    {testResults.success ? 'Test Successful' : 'Test Failed'}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Response Time: {testResults.responseTimeMs}ms
                </Typography>
                {testResults.response && (
                  <Box sx={{ mt: 2 }}>
                    <Typography variant="subtitle2">Response Details:</Typography>
                    <Typography variant="body2">
                      Status: {testResults.response.status}
                    </Typography>
                    <Typography variant="body2">
                      Code: {testResults.response.responseCode}
                    </Typography>
                    <Typography variant="body2">
                      Message: {testResults.response.responseMessage}
                    </Typography>
                  </Box>
                )}
                {testResults.error && (
                  <Box sx={{ mt: 2 }}>
                    <Typography variant="subtitle2" color="error">Error Details:</Typography>
                    <Typography variant="body2" color="error">
                      {testResults.error.errorMessage}
                    </Typography>
                  </Box>
                )}
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setIsTestDialogOpen(false)}>Close</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
};

// Separate form component for reusability
interface SchemeConfigFormProps {
  control: any;
  errors: any;
  endpointFields: any[];
  appendEndpoint: (endpoint: Omit<EndpointConfig, 'id'>) => void;
  removeEndpoint: (index: number) => void;
  watchedInteractionMode: InteractionMode;
  watchedMessageFormat: MessageFormat;
}

const SchemeConfigForm: React.FC<SchemeConfigFormProps> = ({
  control,
  errors,
  endpointFields,
  appendEndpoint,
  removeEndpoint,
  watchedInteractionMode,
  watchedMessageFormat
}) => {
  return (
    <Box sx={{ pt: 1 }}>
      <Grid container spacing={3}>
        {/* Basic Configuration */}
        <Grid item xs={12}>
          <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h6">Basic Configuration</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <Controller
                    name="name"
                    control={control}
                    rules={{ required: 'Name is required' }}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Configuration Name"
                        fullWidth
                        error={!!errors.name}
                        helperText={errors.name?.message}
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
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
                        rows={2}
                      />
                    )}
                  />
                </Grid>
              </Grid>
            </AccordionDetails>
          </Accordion>
        </Grid>

        {/* Interaction Settings */}
        <Grid item xs={12}>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h6">Interaction Settings</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Grid container spacing={2}>
                <Grid item xs={12} md={4}>
                  <Controller
                    name="interactionMode"
                    control={control}
                    render={({ field }) => (
                      <FormControl fullWidth>
                        <InputLabel>Interaction Mode</InputLabel>
                        <Select {...field} label="Interaction Mode">
                          <MenuItem value={InteractionMode.SYNCHRONOUS}>Synchronous</MenuItem>
                          <MenuItem value={InteractionMode.ASYNCHRONOUS}>Asynchronous</MenuItem>
                          <MenuItem value={InteractionMode.HYBRID}>Hybrid</MenuItem>
                        </Select>
                      </FormControl>
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={4}>
                  <Controller
                    name="messageFormat"
                    control={control}
                    render={({ field }) => (
                      <FormControl fullWidth>
                        <InputLabel>Message Format</InputLabel>
                        <Select {...field} label="Message Format">
                          <MenuItem value={MessageFormat.JSON}>JSON</MenuItem>
                          <MenuItem value={MessageFormat.XML}>XML</MenuItem>
                          <MenuItem value={MessageFormat.BOTH}>Both</MenuItem>
                        </Select>
                      </FormControl>
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={4}>
                  <Controller
                    name="responseMode"
                    control={control}
                    render={({ field }) => (
                      <FormControl fullWidth>
                        <InputLabel>Response Mode</InputLabel>
                        <Select {...field} label="Response Mode">
                          <MenuItem value={ResponseMode.IMMEDIATE}>Immediate</MenuItem>
                          <MenuItem value={ResponseMode.WEBHOOK}>Webhook</MenuItem>
                          <MenuItem value={ResponseMode.KAFKA_TOPIC}>Kafka Topic</MenuItem>
                          <MenuItem value={ResponseMode.POLLING}>Polling</MenuItem>
                        </Select>
                      </FormControl>
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <Controller
                    name="timeoutMs"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label="Timeout (ms)"
                        type="number"
                        fullWidth
                        onChange={(e) => field.onChange(parseInt(e.target.value))}
                      />
                    )}
                  />
                </Grid>
              </Grid>
            </AccordionDetails>
          </Accordion>
        </Grid>

        {/* Endpoints Configuration */}
        <Grid item xs={12}>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h6">
                Endpoints ({endpointFields.length})
              </Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Box sx={{ mb: 2 }}>
                <Button
                  variant="outlined"
                  startIcon={<AddIcon />}
                  onClick={() => appendEndpoint({
                    name: '',
                    url: '',
                    method: 'POST',
                    isActive: true,
                    timeoutMs: 30000,
                    headers: {},
                    supportedMessageTypes: [],
                    priority: 1
                  })}
                >
                  Add Endpoint
                </Button>
              </Box>
              {endpointFields.map((field, index) => (
                <Box key={field.id} sx={{ mb: 2, p: 2, border: 1, borderColor: 'divider', borderRadius: 1 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="subtitle2">Endpoint {index + 1}</Typography>
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => removeEndpoint(index)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </Box>
                  <Grid container spacing={2}>
                    <Grid item xs={12} md={6}>
                      <Controller
                        name={`endpoints.${index}.name`}
                        control={control}
                        render={({ field }) => (
                          <TextField
                            {...field}
                            label="Endpoint Name"
                            fullWidth
                            size="small"
                          />
                        )}
                      />
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <Controller
                        name={`endpoints.${index}.url`}
                        control={control}
                        render={({ field }) => (
                          <TextField
                            {...field}
                            label="URL"
                            fullWidth
                            size="small"
                          />
                        )}
                      />
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <Controller
                        name={`endpoints.${index}.method`}
                        control={control}
                        render={({ field }) => (
                          <FormControl fullWidth size="small">
                            <InputLabel>Method</InputLabel>
                            <Select {...field} label="Method">
                              <MenuItem value="GET">GET</MenuItem>
                              <MenuItem value="POST">POST</MenuItem>
                              <MenuItem value="PUT">PUT</MenuItem>
                              <MenuItem value="DELETE">DELETE</MenuItem>
                            </Select>
                          </FormControl>
                        )}
                      />
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <Controller
                        name={`endpoints.${index}.priority`}
                        control={control}
                        render={({ field }) => (
                          <TextField
                            {...field}
                            label="Priority"
                            type="number"
                            fullWidth
                            size="small"
                            onChange={(e) => field.onChange(parseInt(e.target.value))}
                          />
                        )}
                      />
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <Controller
                        name={`endpoints.${index}.timeoutMs`}
                        control={control}
                        render={({ field }) => (
                          <TextField
                            {...field}
                            label="Timeout (ms)"
                            type="number"
                            fullWidth
                            size="small"
                            onChange={(e) => field.onChange(parseInt(e.target.value))}
                          />
                        )}
                      />
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <Controller
                        name={`endpoints.${index}.isActive`}
                        control={control}
                        render={({ field }) => (
                          <FormControlLabel
                            control={<Switch {...field} checked={field.value} size="small" />}
                            label="Active"
                          />
                        )}
                      />
                    </Grid>
                  </Grid>
                </Box>
              ))}
            </AccordionDetails>
          </Accordion>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SchemeConfigManager;