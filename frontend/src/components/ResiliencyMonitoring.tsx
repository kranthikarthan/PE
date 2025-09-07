import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  IconButton,
  LinearProgress,
  Tooltip,
  Badge,
  Tabs,
  Tab,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Settings as SettingsIcon,
  HealthAndSafety as HealthIcon,
  Error as ErrorIcon,
  CheckCircle as CheckCircleIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  PlayArrow as PlayArrowIcon,
  Stop as StopIcon,
  Pause as PauseIcon,
  ExpandMore as ExpandMoreIcon,
  Timeline as TimelineIcon,
  Speed as SpeedIcon,
  Memory as MemoryIcon,
  NetworkCheck as NetworkCheckIcon,
  Security as SecurityIcon,
  Monitor as MonitorIcon,
  Queue as QueueIcon,
  AutoFixHigh as AutoFixHighIcon
} from '@mui/icons-material';
import axios from 'axios';

interface ResiliencyService {
  serviceName: string;
  circuitBreakerState: string;
  bulkheadAvailableCalls: number;
  retryMetrics: any;
  timeLimiterMetrics: any;
  lastUpdated: string;
}

interface QueuedMessage {
  id: string;
  messageId: string;
  messageType: string;
  tenantId: string;
  serviceName: string;
  endpointUrl?: string;
  httpMethod: string;
  status: 'PENDING' | 'PROCESSING' | 'PROCESSED' | 'FAILED' | 'RETRY' | 'EXPIRED' | 'CANCELLED';
  priority: number;
  retryCount: number;
  maxRetries: number;
  nextRetryAt?: string;
  processingStartedAt?: string;
  processingCompletedAt?: string;
  processingTimeMs?: number;
  errorMessage?: string;
  correlationId?: string;
  createdAt: string;
  updatedAt: string;
}

interface SystemHealthStatus {
  tenantId: string;
  totalServices: number;
  healthyServices: number;
  unhealthyServices: number;
  overallHealth: 'HEALTHY' | 'DEGRADED' | 'ERROR';
  serviceHealth: Array<{
    serviceName: string;
    healthy: boolean;
    responseTimeMs: number;
    lastChecked: string;
    errorMessage?: string;
  }>;
  timestamp: string;
}

interface QueueStatistics {
  totalMessages: number;
  pendingMessages: number;
  processingMessages: number;
  processedMessages: number;
  failedMessages: number;
  expiredMessages: number;
  cancelledMessages: number;
  timestamp: string;
}

const ResiliencyMonitoring: React.FC = () => {
  const [resilientServices, setResilientServices] = useState<ResiliencyService[]>([]);
  const [queuedMessages, setQueuedMessages] = useState<QueuedMessage[]>([]);
  const [systemHealth, setSystemHealth] = useState<SystemHealthStatus | null>(null);
  const [queueStats, setQueueStats] = useState<QueueStatistics | null>(null);
  const [loading, setLoading] = useState(false);
  const [selectedTenant, setSelectedTenant] = useState<string>('tenant-001');
  const [selectedTab, setSelectedTab] = useState(0);
  const [monitoringActive, setMonitoringActive] = useState(true);

  useEffect(() => {
    loadData();
    if (monitoringActive) {
      const interval = setInterval(loadData, 30000); // Refresh every 30 seconds
      return () => clearInterval(interval);
    }
  }, [selectedTenant, monitoringActive]);

  const loadData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadResilientServices(),
        loadQueuedMessages(),
        loadSystemHealth(),
        loadQueueStatistics()
      ]);
    } catch (error) {
      console.error('Error loading resiliency data:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadResilientServices = async () => {
    try {
      const response = await axios.get(`/api/resiliency/services?tenantId=${selectedTenant}`);
      setResilientServices(response.data);
    } catch (error) {
      console.error('Error loading resilient services:', error);
    }
  };

  const loadQueuedMessages = async () => {
    try {
      const response = await axios.get(`/api/resiliency/queued-messages?tenantId=${selectedTenant}&limit=50`);
      setQueuedMessages(response.data);
    } catch (error) {
      console.error('Error loading queued messages:', error);
    }
  };

  const loadSystemHealth = async () => {
    try {
      const response = await axios.get(`/api/resiliency/health?tenantId=${selectedTenant}`);
      setSystemHealth(response.data);
    } catch (error) {
      console.error('Error loading system health:', error);
    }
  };

  const loadQueueStatistics = async () => {
    try {
      const response = await axios.get(`/api/resiliency/queue-statistics?tenantId=${selectedTenant}`);
      setQueueStats(response.data);
    } catch (error) {
      console.error('Error loading queue statistics:', error);
    }
  };

  const handleResetCircuitBreaker = async (serviceName: string) => {
    try {
      await axios.post(`/api/resiliency/services/${serviceName}/reset-circuit-breaker`);
      loadResilientServices();
    } catch (error) {
      console.error('Error resetting circuit breaker:', error);
    }
  };

  const handleRetryMessage = async (messageId: string) => {
    try {
      await axios.post(`/api/resiliency/queued-messages/${messageId}/retry`);
      loadQueuedMessages();
    } catch (error) {
      console.error('Error retrying message:', error);
    }
  };

  const handleCancelMessage = async (messageId: string) => {
    try {
      await axios.post(`/api/resiliency/queued-messages/${messageId}/cancel`, {
        reason: 'Cancelled by user'
      });
      loadQueuedMessages();
    } catch (error) {
      console.error('Error cancelling message:', error);
    }
  };

  const handleTriggerRecovery = async () => {
    try {
      await axios.post(`/api/resiliency/recovery/trigger?tenantId=${selectedTenant}`);
      loadSystemHealth();
    } catch (error) {
      console.error('Error triggering recovery:', error);
    }
  };

  const handleReprocessQueuedMessages = async () => {
    try {
      await axios.post(`/api/resiliency/queued-messages/reprocess?tenantId=${selectedTenant}`);
      loadQueuedMessages();
    } catch (error) {
      console.error('Error reprocessing queued messages:', error);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'HEALTHY':
      case 'PROCESSED':
        return 'success';
      case 'DEGRADED':
      case 'PENDING':
        return 'warning';
      case 'ERROR':
      case 'FAILED':
      case 'EXPIRED':
      case 'CANCELLED':
        return 'error';
      case 'PROCESSING':
      case 'RETRY':
        return 'info';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'HEALTHY':
      case 'PROCESSED':
        return <CheckCircleIcon />;
      case 'DEGRADED':
      case 'PENDING':
        return <WarningIcon />;
      case 'ERROR':
      case 'FAILED':
      case 'EXPIRED':
      case 'CANCELLED':
        return <ErrorIcon />;
      case 'PROCESSING':
      case 'RETRY':
        return <InfoIcon />;
      default:
        return <InfoIcon />;
    }
  };

  const formatDuration = (ms: number) => {
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}m`;
  };

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString();
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Resiliency Monitoring
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Tenant</InputLabel>
            <Select
              value={selectedTenant}
              onChange={(e) => setSelectedTenant(e.target.value)}
              label="Tenant"
            >
              <MenuItem value="tenant-001">Tenant 001</MenuItem>
              <MenuItem value="tenant-002">Tenant 002</MenuItem>
              <MenuItem value="tenant-003">Tenant 003</MenuItem>
            </Select>
          </FormControl>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={loadData}
            disabled={loading}
          >
            Refresh
          </Button>
          <Button
            variant="outlined"
            startIcon={monitoringActive ? <StopIcon /> : <PlayArrowIcon />}
            onClick={() => setMonitoringActive(!monitoringActive)}
            color={monitoringActive ? 'error' : 'success'}
          >
            {monitoringActive ? 'Stop Monitoring' : 'Start Monitoring'}
          </Button>
        </Box>
      </Box>

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      <Tabs value={selectedTab} onChange={(e, newValue) => setSelectedTab(newValue)} sx={{ mb: 3 }}>
        <Tab label="System Health" icon={<HealthIcon />} />
        <Tab label="Resilient Services" icon={<SettingsIcon />} />
        <Tab label="Queued Messages" icon={<QueueIcon />} />
        <Tab label="Statistics" icon={<MonitorIcon />} />
      </Tabs>

      {selectedTab === 0 && (
        <Grid container spacing={3}>
          {/* System Health Overview */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  System Health Overview
                </Typography>
                {systemHealth && (
                  <Grid container spacing={2}>
                    <Grid item xs={12} md={3}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="h4" color={getStatusColor(systemHealth.overallHealth)}>
                          {systemHealth.healthyServices}/{systemHealth.totalServices}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Healthy Services
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="h4" color="error">
                          {systemHealth.unhealthyServices}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Unhealthy Services
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Chip
                          label={systemHealth.overallHealth}
                          color={getStatusColor(systemHealth.overallHealth)}
                          icon={getStatusIcon(systemHealth.overallHealth)}
                        />
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                          Overall Health
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Button
                          variant="contained"
                          startIcon={<AutoFixHighIcon />}
                          onClick={handleTriggerRecovery}
                          disabled={systemHealth.unhealthyServices === 0}
                        >
                          Trigger Recovery
                        </Button>
                      </Box>
                    </Grid>
                  </Grid>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Service Health Details */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Service Health Details
                </Typography>
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Service Name</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Response Time</TableCell>
                        <TableCell>Last Checked</TableCell>
                        <TableCell>Error Message</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {systemHealth?.serviceHealth.map((service) => (
                        <TableRow key={service.serviceName}>
                          <TableCell>{service.serviceName}</TableCell>
                          <TableCell>
                            <Chip
                              label={service.healthy ? 'HEALTHY' : 'UNHEALTHY'}
                              color={getStatusColor(service.healthy ? 'HEALTHY' : 'ERROR')}
                              icon={getStatusIcon(service.healthy ? 'HEALTHY' : 'ERROR')}
                              size="small"
                            />
                          </TableCell>
                          <TableCell>{formatDuration(service.responseTimeMs)}</TableCell>
                          <TableCell>{formatDateTime(service.lastChecked)}</TableCell>
                          <TableCell>
                            {service.errorMessage && (
                              <Tooltip title={service.errorMessage}>
                                <ErrorIcon color="error" />
                              </Tooltip>
                            )}
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
      )}

      {selectedTab === 1 && (
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">Resilient Services</Typography>
              <Button
                variant="contained"
                startIcon={<RefreshIcon />}
                onClick={loadResilientServices}
                disabled={loading}
              >
                Refresh Services
              </Button>
            </Box>
          </Grid>

          {resilientServices.map((service) => (
            <Grid item xs={12} md={6} key={service.serviceName}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                    <Box>
                      <Typography variant="h6">{service.serviceName}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Last Updated: {formatDateTime(service.lastUpdated)}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <Chip
                        label={service.circuitBreakerState}
                        color={getStatusColor(service.circuitBreakerState)}
                        size="small"
                      />
                      <IconButton
                        size="small"
                        onClick={() => handleResetCircuitBreaker(service.serviceName)}
                        color="warning"
                        title="Reset Circuit Breaker"
                      >
                        <RefreshIcon />
                      </IconButton>
                    </Box>
                  </Box>

                  <Accordion>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography variant="subtitle2">Circuit Breaker Status</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Typography variant="body2">
                        State: {service.circuitBreakerState}<br />
                        Available Calls: {service.bulkheadAvailableCalls}<br />
                        Last Updated: {formatDateTime(service.lastUpdated)}
                      </Typography>
                    </AccordionDetails>
                  </Accordion>

                  <Accordion>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography variant="subtitle2">Retry Metrics</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Typography variant="body2">
                        {service.retryMetrics ? (
                          <>
                            Successful Calls: {service.retryMetrics.numberOfSuccessfulCallsWithRetryAttempt}<br />
                            Failed Calls: {service.retryMetrics.numberOfFailedCallsWithRetryAttempt}<br />
                            Total Retries: {service.retryMetrics.numberOfSuccessfulCallsWithRetryAttempt + service.retryMetrics.numberOfFailedCallsWithRetryAttempt}
                          </>
                        ) : (
                          'No retry metrics available'
                        )}
                      </Typography>
                    </AccordionDetails>
                  </Accordion>

                  <Accordion>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography variant="subtitle2">Bulkhead Metrics</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Typography variant="body2">
                        Available Concurrent Calls: {service.bulkheadAvailableCalls}<br />
                        Max Concurrent Calls: {service.bulkheadAvailableCalls + ' (configured)'}
                      </Typography>
                    </AccordionDetails>
                  </Accordion>

                  <Accordion>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography variant="subtitle2">Time Limiter Metrics</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Typography variant="body2">
                        {service.timeLimiterMetrics ? (
                          <>
                            Successful Calls: {service.timeLimiterMetrics.numberOfSuccessfulCalls}<br />
                            Timeout Calls: {service.timeLimiterMetrics.numberOfFailedCalls}
                          </>
                        ) : (
                          'No time limiter metrics available'
                        )}
                      </Typography>
                    </AccordionDetails>
                  </Accordion>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {selectedTab === 2 && (
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">Queued Messages</Typography>
              <Button
                variant="contained"
                startIcon={<AutoFixHighIcon />}
                onClick={handleReprocessQueuedMessages}
              >
                Reprocess All
              </Button>
            </Box>
          </Grid>

          <Grid item xs={12}>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Message ID</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Service</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Retry Count</TableCell>
                    <TableCell>Next Retry</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {queuedMessages.map((message) => (
                    <TableRow key={message.id}>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                          {message.messageId.substring(0, 8)}...
                        </Typography>
                      </TableCell>
                      <TableCell>{message.messageType}</TableCell>
                      <TableCell>{message.serviceName}</TableCell>
                      <TableCell>
                        <Chip
                          label={message.status}
                          color={getStatusColor(message.status)}
                          icon={getStatusIcon(message.status)}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Badge badgeContent={message.retryCount} color="primary">
                          <Typography variant="body2">
                            {message.retryCount}/{message.maxRetries}
                          </Typography>
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {message.nextRetryAt && formatDateTime(message.nextRetryAt)}
                      </TableCell>
                      <TableCell>{formatDateTime(message.createdAt)}</TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          {message.status === 'FAILED' && (
                            <Tooltip title="Retry Message">
                              <IconButton
                                size="small"
                                onClick={() => handleRetryMessage(message.messageId)}
                              >
                                <PlayArrowIcon />
                              </IconButton>
                            </Tooltip>
                          )}
                          {['PENDING', 'FAILED', 'RETRY'].includes(message.status) && (
                            <Tooltip title="Cancel Message">
                              <IconButton
                                size="small"
                                onClick={() => handleCancelMessage(message.messageId)}
                                color="error"
                              >
                                <StopIcon />
                              </IconButton>
                            </Tooltip>
                          )}
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Grid>
        </Grid>
      )}

      {selectedTab === 3 && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Queue Statistics
                </Typography>
                {queueStats && (
                  <List>
                    <ListItem>
                      <ListItemIcon>
                        <QueueIcon />
                      </ListItemIcon>
                      <ListItemText
                        primary="Total Messages"
                        secondary={queueStats.totalMessages}
                      />
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemIcon>
                        <WarningIcon color="warning" />
                      </ListItemIcon>
                      <ListItemText
                        primary="Pending Messages"
                        secondary={queueStats.pendingMessages}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon>
                        <InfoIcon color="info" />
                      </ListItemIcon>
                      <ListItemText
                        primary="Processing Messages"
                        secondary={queueStats.processingMessages}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon>
                        <CheckCircleIcon color="success" />
                      </ListItemIcon>
                      <ListItemText
                        primary="Processed Messages"
                        secondary={queueStats.processedMessages}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon>
                        <ErrorIcon color="error" />
                      </ListItemIcon>
                      <ListItemText
                        primary="Failed Messages"
                        secondary={queueStats.failedMessages}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon>
                        <ErrorIcon color="error" />
                      </ListItemIcon>
                      <ListItemText
                        primary="Expired Messages"
                        secondary={queueStats.expiredMessages}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon>
                        <StopIcon color="error" />
                      </ListItemIcon>
                      <ListItemText
                        primary="Cancelled Messages"
                        secondary={queueStats.cancelledMessages}
                      />
                    </ListItem>
                  </List>
                )}
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  System Metrics
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Box sx={{ textAlign: 'center' }}>
                      <SpeedIcon color="primary" sx={{ fontSize: 40 }} />
                      <Typography variant="h6">Response Time</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Avg: 150ms
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6}>
                    <Box sx={{ textAlign: 'center' }}>
                      <MemoryIcon color="primary" sx={{ fontSize: 40 }} />
                      <Typography variant="h6">Memory Usage</Typography>
                      <Typography variant="body2" color="text.secondary">
                        2.1GB / 8GB
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6}>
                    <Box sx={{ textAlign: 'center' }}>
                      <NetworkCheckIcon color="primary" sx={{ fontSize: 40 }} />
                      <Typography variant="h6">Network I/O</Typography>
                      <Typography variant="body2" color="text.secondary">
                        1.2MB/s
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6}>
                    <Box sx={{ textAlign: 'center' }}>
                      <SecurityIcon color="primary" sx={{ fontSize: 40 }} />
                      <Typography variant="h6">Security</Typography>
                      <Typography variant="body2" color="text.secondary">
                        All Green
                      </Typography>
                    </Box>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default ResiliencyMonitoring;