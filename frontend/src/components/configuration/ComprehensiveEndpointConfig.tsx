import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Button,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Divider,
  Chip,
  Alert,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  Info as InfoIcon,
  Settings as SettingsIcon,
  Security as SecurityIcon,
  Speed as SpeedIcon,
  Monitor as MonitorIcon,
} from '@mui/icons-material';
import { Controller, useForm, useFieldArray } from 'react-hook-form';
import { 
  Iso20022MessageType, 
  MessageFormat, 
  ResponseMode, 
  FlowDirection,
  ClearingSystemEndpoint 
} from '../../types/clearingSystem';

interface ComprehensiveEndpointConfigProps {
  endpoint?: ClearingSystemEndpoint;
  onSave: (endpoint: Partial<ClearingSystemEndpoint>) => void;
  onCancel: () => void;
  clearingSystemId: string;
}

const ComprehensiveEndpointConfig: React.FC<ComprehensiveEndpointConfigProps> = ({
  endpoint,
  onSave,
  onCancel,
  clearingSystemId,
}) => {
  const [expandedSections, setExpandedSections] = useState<string[]>(['basic', 'message', 'auth']);

  const { control, handleSubmit, watch, setValue } = useForm<ClearingSystemEndpoint>({
    defaultValues: {
      id: endpoint?.id || '',
      clearingSystemId: clearingSystemId,
      name: endpoint?.name || '',
      endpointType: endpoint?.endpointType || 'SYNC',
      messageType: endpoint?.messageType || 'pacs008',
      url: endpoint?.url || '',
      httpMethod: endpoint?.httpMethod || 'POST',
      timeoutMs: endpoint?.timeoutMs || 30000,
      retryAttempts: endpoint?.retryAttempts || 3,
      authenticationType: endpoint?.authenticationType || 'API_KEY',
      authenticationConfig: endpoint?.authenticationConfig || {},
      defaultHeaders: endpoint?.defaultHeaders || {},
      isActive: endpoint?.isActive ?? true,
      priority: endpoint?.priority || 1,
      description: endpoint?.description || '',
      messageFormat: endpoint?.messageFormat || 'JSON',
      responseMode: endpoint?.responseMode || 'IMMEDIATE',
      flowDirection: endpoint?.flowDirection || 'CLIENT_TO_CLEARING',
      transformationRules: endpoint?.transformationRules || {
        inputMapping: {},
        outputMapping: {},
        validationRules: {},
      },
      rateLimiting: endpoint?.rateLimiting || {
        enabled: false,
        requestsPerMinute: 100,
        burstLimit: 10,
      },
      monitoring: endpoint?.monitoring || {
        enabled: false,
        healthCheckIntervalMs: 60000,
        alertThresholdMs: 5000,
        alertEmails: [],
      },
    },
  });

  const { fields: headerFields, append: appendHeader, remove: removeHeader } = useFieldArray({
    control,
    name: 'defaultHeaders',
  });

  const { fields: authFields, append: appendAuth, remove: removeAuth } = useFieldArray({
    control,
    name: 'authenticationConfig',
  });

  const watchedMessageType = watch('messageType');
  const watchedEndpointType = watch('endpointType');
  const watchedFlowDirection = watch('flowDirection');

  const handleSectionToggle = (section: string) => {
    setExpandedSections(prev => 
      prev.includes(section) 
        ? prev.filter(s => s !== section)
        : [...prev, section]
    );
  };

  const getMessageTypeDescription = (messageType: Iso20022MessageType): string => {
    const descriptions: Record<Iso20022MessageType, string> = {
      'pacs008': 'FI to FI Customer Credit Transfer - Used for sending payments between financial institutions',
      'pacs002': 'FI to FI Payment Status Report - Used for reporting payment status between financial institutions',
      'pacs004': 'Payment Return - Used for returning payments that cannot be processed',
      'pacs007': 'Payment Cancellation Request - Used for requesting payment cancellations',
      'pacs028': 'Payment Status Request - Used for requesting payment status information',
      'pain001': 'Customer Credit Transfer Initiation - Used for initiating customer payments',
      'pain002': 'Customer Payment Status Report - Used for reporting customer payment status',
      'camt054': 'Bank to Customer Debit Credit Notification - Used for notifying customers of account changes',
      'camt055': 'FI to FI Payment Cancellation Request - Used for requesting payment cancellations',
      'camt056': 'FI to FI Payment Status Request - Used for requesting payment status information',
      'camt029': 'Resolution of Investigation - Used for resolving payment investigations',
      'status': 'General Status Messages - Used for general status and health check messages',
    };
    return descriptions[messageType] || 'Unknown message type';
  };

  const getFlowDirectionDescription = (direction: FlowDirection): string => {
    const descriptions: Record<FlowDirection, string> = {
      'CLIENT_TO_CLEARING': 'Messages flow from client to clearing system (e.g., PAIN.001 → PACS.008)',
      'CLEARING_TO_CLIENT': 'Messages flow from clearing system to client (e.g., PACS.002 → PAIN.002)',
      'BIDIRECTIONAL': 'Messages can flow in both directions (e.g., status requests and responses)',
    };
    return descriptions[direction] || 'Unknown flow direction';
  };

  const getRecommendedSettings = (messageType: Iso20022MessageType) => {
    const recommendations: Record<Iso20022MessageType, Partial<ClearingSystemEndpoint>> = {
      'pacs008': { endpointType: 'SYNC', responseMode: 'IMMEDIATE', flowDirection: 'CLIENT_TO_CLEARING' },
      'pacs002': { endpointType: 'WEBHOOK', responseMode: 'ASYNC', flowDirection: 'CLEARING_TO_CLIENT' },
      'pacs004': { endpointType: 'WEBHOOK', responseMode: 'ASYNC', flowDirection: 'CLEARING_TO_CLIENT' },
      'pacs007': { endpointType: 'SYNC', responseMode: 'IMMEDIATE', flowDirection: 'CLIENT_TO_CLEARING' },
      'pacs028': { endpointType: 'SYNC', responseMode: 'IMMEDIATE', flowDirection: 'CLIENT_TO_CLEARING' },
      'pain001': { endpointType: 'SYNC', responseMode: 'IMMEDIATE', flowDirection: 'CLIENT_TO_CLEARING' },
      'pain002': { endpointType: 'WEBHOOK', responseMode: 'ASYNC', flowDirection: 'CLEARING_TO_CLIENT' },
      'camt054': { endpointType: 'WEBHOOK', responseMode: 'ASYNC', flowDirection: 'CLEARING_TO_CLIENT' },
      'camt055': { endpointType: 'SYNC', responseMode: 'IMMEDIATE', flowDirection: 'CLIENT_TO_CLEARING' },
      'camt056': { endpointType: 'SYNC', responseMode: 'IMMEDIATE', flowDirection: 'CLIENT_TO_CLEARING' },
      'camt029': { endpointType: 'WEBHOOK', responseMode: 'ASYNC', flowDirection: 'CLEARING_TO_CLIENT' },
      'status': { endpointType: 'POLLING', responseMode: 'IMMEDIATE', flowDirection: 'BIDIRECTIONAL' },
    };
    return recommendations[messageType] || {};
  };

  const applyRecommendedSettings = () => {
    const recommended = getRecommendedSettings(watchedMessageType);
    Object.entries(recommended).forEach(([key, value]) => {
      setValue(key as keyof ClearingSystemEndpoint, value as any);
    });
  };

  const onSubmit = (data: ClearingSystemEndpoint) => {
    onSave(data);
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Comprehensive Endpoint Configuration
      </Typography>
      
      <Alert severity="info" sx={{ mb: 2 }}>
        Configure endpoints for all ISO 20022 message types with full support for REST API, JSON/XML formats, 
        synchronous/asynchronous processing, and tenant-based routing.
      </Alert>

      <form onSubmit={handleSubmit(onSubmit)}>
        {/* Basic Configuration */}
        <Accordion 
          expanded={expandedSections.includes('basic')} 
          onChange={() => handleSectionToggle('basic')}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box display="flex" alignItems="center">
              <SettingsIcon sx={{ mr: 1 }} />
              <Typography variant="h6">Basic Configuration</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="name"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Endpoint Name"
                      fullWidth
                      required
                      helperText="Descriptive name for this endpoint"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="description"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Description"
                      fullWidth
                      helperText="Optional description of this endpoint"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="url"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Endpoint URL"
                      fullWidth
                      required
                      helperText="Full URL for the clearing system endpoint"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="httpMethod"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>HTTP Method</InputLabel>
                      <Select {...field} label="HTTP Method">
                        <MenuItem value="GET">GET</MenuItem>
                        <MenuItem value="POST">POST</MenuItem>
                        <MenuItem value="PUT">PUT</MenuItem>
                        <MenuItem value="DELETE">DELETE</MenuItem>
                        <MenuItem value="PATCH">PATCH</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="timeoutMs"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Timeout (ms)"
                      type="number"
                      fullWidth
                      helperText="Request timeout in milliseconds"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="retryAttempts"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Retry Attempts"
                      type="number"
                      fullWidth
                      helperText="Number of retry attempts on failure"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="priority"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Priority"
                      type="number"
                      fullWidth
                      helperText="Priority for this endpoint (1 = highest)"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
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
            </Grid>
          </AccordionDetails>
        </Accordion>

        {/* Message Configuration */}
        <Accordion 
          expanded={expandedSections.includes('message')} 
          onChange={() => handleSectionToggle('message')}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box display="flex" alignItems="center">
              <InfoIcon sx={{ mr: 1 }} />
              <Typography variant="h6">Message Configuration</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="messageType"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
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
              <Grid item xs={12} sm={6}>
                <Controller
                  name="endpointType"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Endpoint Type</InputLabel>
                      <Select {...field} label="Endpoint Type">
                        <MenuItem value="SYNC">Synchronous</MenuItem>
                        <MenuItem value="ASYNC">Asynchronous</MenuItem>
                        <MenuItem value="WEBHOOK">Webhook</MenuItem>
                        <MenuItem value="POLLING">Polling</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="messageFormat"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Message Format</InputLabel>
                      <Select {...field} label="Message Format">
                        <MenuItem value="JSON">JSON</MenuItem>
                        <MenuItem value="XML">XML</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="responseMode"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Response Mode</InputLabel>
                      <Select {...field} label="Response Mode">
                        <MenuItem value="IMMEDIATE">Immediate</MenuItem>
                        <MenuItem value="ASYNC">Asynchronous</MenuItem>
                        <MenuItem value="KAFKA">Kafka</MenuItem>
                        <MenuItem value="WEBHOOK">Webhook</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="flowDirection"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Flow Direction</InputLabel>
                      <Select {...field} label="Flow Direction">
                        <MenuItem value="CLIENT_TO_CLEARING">Client to Clearing System</MenuItem>
                        <MenuItem value="CLEARING_TO_CLIENT">Clearing System to Client</MenuItem>
                        <MenuItem value="BIDIRECTIONAL">Bidirectional</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Button
                  variant="outlined"
                  onClick={applyRecommendedSettings}
                  startIcon={<SettingsIcon />}
                >
                  Apply Recommended Settings
                </Button>
              </Grid>
            </Grid>
            
            {watchedMessageType && (
              <Alert severity="info" sx={{ mt: 2 }}>
                <Typography variant="body2">
                  <strong>{watchedMessageType.toUpperCase()}:</strong> {getMessageTypeDescription(watchedMessageType)}
                </Typography>
              </Alert>
            )}
            
            {watchedFlowDirection && (
              <Alert severity="info" sx={{ mt: 1 }}>
                <Typography variant="body2">
                  <strong>Flow Direction:</strong> {getFlowDirectionDescription(watchedFlowDirection)}
                </Typography>
              </Alert>
            )}
          </AccordionDetails>
        </Accordion>

        {/* Authentication Configuration */}
        <Accordion 
          expanded={expandedSections.includes('auth')} 
          onChange={() => handleSectionToggle('auth')}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box display="flex" alignItems="center">
              <SecurityIcon sx={{ mr: 1 }} />
              <Typography variant="h6">Authentication Configuration</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="authenticationType"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Authentication Type</InputLabel>
                      <Select {...field} label="Authentication Type">
                        <MenuItem value="NONE">None</MenuItem>
                        <MenuItem value="API_KEY">API Key</MenuItem>
                        <MenuItem value="JWT">JWT Token</MenuItem>
                        <MenuItem value="OAUTH2">OAuth 2.0</MenuItem>
                        <MenuItem value="MTLS">Mutual TLS</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  Authentication Configuration
                </Typography>
                {authFields.map((field, index) => (
                  <Box key={field.id} display="flex" alignItems="center" mb={1}>
                    <Controller
                      name={`authenticationConfig.${index}.key`}
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Key"
                          size="small"
                          sx={{ mr: 1, flex: 1 }}
                        />
                      )}
                    />
                    <Controller
                      name={`authenticationConfig.${index}.value`}
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Value"
                          size="small"
                          sx={{ mr: 1, flex: 1 }}
                        />
                      )}
                    />
                    <IconButton onClick={() => removeAuth(index)} color="error">
                      <DeleteIcon />
                    </IconButton>
                  </Box>
                ))}
                <Button
                  variant="outlined"
                  startIcon={<AddIcon />}
                  onClick={() => appendAuth({ key: '', value: '' })}
                  size="small"
                >
                  Add Auth Config
                </Button>
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>

        {/* Headers Configuration */}
        <Accordion 
          expanded={expandedSections.includes('headers')} 
          onChange={() => handleSectionToggle('headers')}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box display="flex" alignItems="center">
              <SettingsIcon sx={{ mr: 1 }} />
              <Typography variant="h6">Headers Configuration</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Typography variant="subtitle2" gutterBottom>
              Default Headers
            </Typography>
            {headerFields.map((field, index) => (
              <Box key={field.id} display="flex" alignItems="center" mb={1}>
                <Controller
                  name={`defaultHeaders.${index}.key`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Header Name"
                      size="small"
                      sx={{ mr: 1, flex: 1 }}
                    />
                  )}
                />
                <Controller
                  name={`defaultHeaders.${index}.value`}
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Header Value"
                      size="small"
                      sx={{ mr: 1, flex: 1 }}
                    />
                  )}
                />
                <IconButton onClick={() => removeHeader(index)} color="error">
                  <DeleteIcon />
                </IconButton>
              </Box>
            ))}
            <Button
              variant="outlined"
              startIcon={<AddIcon />}
              onClick={() => appendHeader({ key: '', value: '' })}
              size="small"
            >
              Add Header
            </Button>
          </AccordionDetails>
        </Accordion>

        {/* Rate Limiting Configuration */}
        <Accordion 
          expanded={expandedSections.includes('rateLimiting')} 
          onChange={() => handleSectionToggle('rateLimiting')}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box display="flex" alignItems="center">
              <SpeedIcon sx={{ mr: 1 }} />
              <Typography variant="h6">Rate Limiting Configuration</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Controller
                  name="rateLimiting.enabled"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Enable Rate Limiting"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="rateLimiting.requestsPerMinute"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Requests Per Minute"
                      type="number"
                      fullWidth
                      helperText="Maximum requests per minute"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="rateLimiting.burstLimit"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Burst Limit"
                      type="number"
                      fullWidth
                      helperText="Maximum burst requests"
                    />
                  )}
                />
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>

        {/* Monitoring Configuration */}
        <Accordion 
          expanded={expandedSections.includes('monitoring')} 
          onChange={() => handleSectionToggle('monitoring')}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box display="flex" alignItems="center">
              <MonitorIcon sx={{ mr: 1 }} />
              <Typography variant="h6">Monitoring Configuration</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Controller
                  name="monitoring.enabled"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Switch {...field} checked={field.value} />}
                      label="Enable Monitoring"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="monitoring.healthCheckIntervalMs"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Health Check Interval (ms)"
                      type="number"
                      fullWidth
                      helperText="Interval between health checks"
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="monitoring.alertThresholdMs"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Alert Threshold (ms)"
                      type="number"
                      fullWidth
                      helperText="Response time threshold for alerts"
                    />
                  )}
                />
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>

        {/* Action Buttons */}
        <Box display="flex" justifyContent="flex-end" gap={2} mt={3}>
          <Button variant="outlined" onClick={onCancel}>
            Cancel
          </Button>
          <Button type="submit" variant="contained">
            Save Endpoint
          </Button>
        </Box>
      </form>
    </Box>
  );
};

export default ComprehensiveEndpointConfig;