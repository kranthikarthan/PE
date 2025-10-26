/**
 * Service Management Page
 * 
 * Real-time monitoring and control of microservices health and performance.
 * Integrates with Operations Management Service for service control, circuit breakers,
 * feature flags, and Kubernetes pod management.
 */

import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
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
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Skeleton,
  Alert,
  CircularProgress,
  Tooltip,
  Switch,
  FormControlLabel,
} from '@mui/material';
import { Grid } from '@mui/material';
import {
  PlayArrow,
  Pause,
  Stop,
  Refresh,
  Settings,
  Visibility,
  MoreVert,
  PowerSettingsNew,
  RestartAlt,
  Warning,
  CheckCircle,
  Error,
} from '@mui/icons-material';
import { useApi } from '../hooks';
import { getOperationsManagementService } from '../services';
import { ServiceHealth, CircuitBreakerInfo } from '../types/service';
import { useNotification } from '../contexts';
import { usePermissions } from '../hooks/usePermissions';

const ServiceManagement: React.FC = () => {
  const { showSuccess, showError } = useNotification();
  const { canManageServices, canManageChannels } = usePermissions();
  const [selectedService, setSelectedService] = useState<ServiceHealth | null>(null);
  const [controlDialogOpen, setControlDialogOpen] = useState(false);
  const [controlAction, setControlAction] = useState<'START' | 'STOP' | 'RESTART' | 'PAUSE' | 'RESUME'>('START');
  const [controlReason, setControlReason] = useState('');
  const [forceControl, setForceControl] = useState(false);

  // API hooks for fetching data
  const servicesApi = useApi(
    () => getOperationsManagementService().getAllServicesHealth(),
    { immediate: true, showNotifications: false }
  );

  const circuitBreakersApi = useApi(
    () => getOperationsManagementService().getAllCircuitBreakers(),
    { immediate: true, showNotifications: false }
  );

  const featureFlagsApi = useApi(
    () => getOperationsManagementService().getAllFeatureFlags(),
    { immediate: true, showNotifications: false }
  );

  const podsApi = useApi(
    () => getOperationsManagementService().getAllPods(),
    { immediate: true, showNotifications: false }
  );

  // Auto-refresh data every 30 seconds
  useEffect(() => {
    const interval = setInterval(() => {
      servicesApi.execute();
      circuitBreakersApi.execute();
      featureFlagsApi.execute();
      podsApi.execute();
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  // Handle service control
  const handleServiceControl = async (service: ServiceHealth, action: string) => {
    if (!canManageServices()) {
      showError('Access Denied', 'You do not have permission to manage services');
      return;
    }

    setSelectedService(service);
    setControlAction(action as any);
    setControlDialogOpen(true);
  };

  const confirmServiceControl = async () => {
    if (!selectedService) return;

    try {
      const opsService = getOperationsManagementService();
      await opsService.controlService(selectedService.id, {
        action: controlAction,
        reason: controlReason || `${controlAction} initiated by operations team`,
        force: forceControl,
      });

      showSuccess('Service Control', `Service ${controlAction.toLowerCase()} initiated successfully`);
      setControlDialogOpen(false);
      setControlReason('');
      setForceControl(false);
      
      // Refresh services data
      servicesApi.execute();
    } catch (error: any) {
      showError('Service Control Failed', error.message || 'Failed to control service');
    }
  };

  const handleCircuitBreakerToggle = async (serviceName: string, currentState: string) => {
    if (!canManageServices()) {
      showError('Access Denied', 'You do not have permission to manage circuit breakers');
      return;
    }

    try {
      const opsService = getOperationsManagementService();
      if (currentState === 'OPEN') {
        await opsService.closeCircuitBreaker(serviceName, 'Circuit breaker closed by operations team');
        showSuccess('Circuit Breaker', 'Circuit breaker closed successfully');
      } else {
        await opsService.openCircuitBreaker(serviceName, 'Circuit breaker opened by operations team');
        showSuccess('Circuit Breaker', 'Circuit breaker opened successfully');
      }
      
      circuitBreakersApi.execute();
    } catch (error: any) {
      showError('Circuit Breaker Failed', error.message || 'Failed to toggle circuit breaker');
    }
  };

  const handleFeatureFlagToggle = async (flagName: string, currentEnabled: boolean) => {
    try {
      const opsService = getOperationsManagementService();
      await opsService.toggleFeatureFlag(flagName, !currentEnabled);
      showSuccess('Feature Flag', `Feature flag ${flagName} ${!currentEnabled ? 'enabled' : 'disabled'}`);
      featureFlagsApi.execute();
    } catch (error: any) {
      showError('Feature Flag Failed', error.message || 'Failed to toggle feature flag');
    }
  };

  const handlePodRestart = async (podName: string) => {
    if (!canManageServices()) {
      showError('Access Denied', 'You do not have permission to manage pods');
      return;
    }

    try {
      const opsService = getOperationsManagementService();
      await opsService.restartPod(podName, 'Pod restarted by operations team');
      showSuccess('Pod Restart', 'Pod restart initiated successfully');
      podsApi.execute();
    } catch (error: any) {
      showError('Pod Restart Failed', error.message || 'Failed to restart pod');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'up': return 'success';
      case 'degraded': return 'warning';
      case 'down': return 'error';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'up': return <CheckCircle color="success" />;
      case 'degraded': return <Warning color="warning" />;
      case 'down': return <Error color="error" />;
      default: return <Warning color="disabled" />;
    }
  };

  const formatUptime = (uptime: number) => {
    const days = Math.floor(uptime / 86400);
    const hours = Math.floor((uptime % 86400) / 3600);
    const minutes = Math.floor((uptime % 3600) / 60);
    
    if (days > 0) return `${days}d ${hours}h`;
    if (hours > 0) return `${hours}h ${minutes}m`;
    return `${minutes}m`;
  };

  const formatResponseTime = (responseTime: number) => {
    if (responseTime < 1000) return `${responseTime}ms`;
    return `${(responseTime / 1000).toFixed(1)}s`;
  };

  // Loading skeleton component
  const ServiceTableSkeleton = () => (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Service</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Uptime</TableCell>
            <TableCell>Response Time</TableCell>
            <TableCell>Instances</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {[1, 2, 3, 4, 5].map((index) => (
            <TableRow key={index}>
              <TableCell><Skeleton variant="text" width="60%" /></TableCell>
              <TableCell><Skeleton variant="rectangular" width={80} height={24} /></TableCell>
              <TableCell><Skeleton variant="text" width="40%" /></TableCell>
              <TableCell><Skeleton variant="text" width="30%" /></TableCell>
              <TableCell><Skeleton variant="text" width="20%" /></TableCell>
              <TableCell><Skeleton variant="rectangular" width={120} height={32} /></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Service Management
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Monitor and control microservices health and performance
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Refresh />}
          onClick={() => {
            servicesApi.execute();
            circuitBreakersApi.execute();
            featureFlagsApi.execute();
            podsApi.execute();
          }}
          disabled={servicesApi.loading}
        >
          Refresh All
        </Button>
      </Box>

      <Grid container spacing={3}>
        {/* Services Overview */}
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Services Overview
              </Typography>
              
              {servicesApi.loading ? (
                <ServiceTableSkeleton />
              ) : servicesApi.error ? (
                <Alert severity="error" sx={{ mb: 2 }}>
                  Failed to load services: {servicesApi.error}
                </Alert>
              ) : (
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Service</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Uptime</TableCell>
                        <TableCell>Response Time</TableCell>
                        <TableCell>Instances</TableCell>
                        <TableCell>Version</TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {servicesApi.data?.map((service: ServiceHealth) => (
                        <TableRow key={service.id}>
                          <TableCell>
                            <Box display="flex" alignItems="center" gap={1}>
                              {getStatusIcon(service.status)}
                              <Typography variant="body2" fontWeight="medium">
                                {service.name}
                              </Typography>
                            </Box>
                          </TableCell>
                          <TableCell>
                            <Chip 
                              label={service.status} 
                              size="small" 
                              color={getStatusColor(service.status) as any}
                            />
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {formatUptime(service.uptime)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {formatResponseTime(service.responseTime)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {service.instances}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              v{service.version}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Box display="flex" gap={1}>
                              <Tooltip title="Start Service">
                                <IconButton
                                  size="small"
                                  onClick={() => handleServiceControl(service, 'START')}
                                  disabled={!canManageServices()}
                                >
                                  <PlayArrow />
                                </IconButton>
                              </Tooltip>
                              <Tooltip title="Stop Service">
                                <IconButton
                                  size="small"
                                  onClick={() => handleServiceControl(service, 'STOP')}
                                  disabled={!canManageServices()}
                                >
                                  <Stop />
                                </IconButton>
                              </Tooltip>
                              <Tooltip title="Restart Service">
                                <IconButton
                                  size="small"
                                  onClick={() => handleServiceControl(service, 'RESTART')}
                                  disabled={!canManageServices()}
                                >
                                  <RestartAlt />
                                </IconButton>
                              </Tooltip>
                              <Tooltip title="View Details">
                                <IconButton size="small">
                                  <Visibility />
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
        </Grid>

        {/* Circuit Breakers */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Circuit Breakers
              </Typography>
              
              {circuitBreakersApi.loading ? (
                <Box>
                  {[1, 2, 3].map((index) => (
                    <Box key={index} display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                      <Skeleton variant="text" width="40%" />
                      <Skeleton variant="rectangular" width={60} height={24} />
                    </Box>
                  ))}
                </Box>
              ) : circuitBreakersApi.error ? (
                <Alert severity="error">
                  Failed to load circuit breakers: {circuitBreakersApi.error}
                </Alert>
              ) : (
                <Box>
                  {circuitBreakersApi.data?.map((cb: CircuitBreakerInfo) => (
                    <Box key={cb.name} display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                      <Box>
                        <Typography variant="body2" fontWeight="medium">
                          {cb.name}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Failure Rate: {(cb.failureRate * 100).toFixed(2)}%
                        </Typography>
                      </Box>
                      <Box display="flex" alignItems="center" gap={1}>
                        <Chip 
                          label={cb.state} 
                          size="small" 
                          color={cb.state === 'CLOSED' ? 'success' : 'error'}
                        />
                        <Tooltip title={cb.state === 'OPEN' ? 'Close Circuit Breaker' : 'Open Circuit Breaker'}>
                          <Switch
                            checked={cb.state === 'CLOSED'}
                            onChange={() => handleCircuitBreakerToggle(cb.name, cb.state)}
                            disabled={!canManageServices()}
                            size="small"
                          />
                        </Tooltip>
                      </Box>
                    </Box>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Feature Flags */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Feature Flags
              </Typography>
              
              {featureFlagsApi.loading ? (
                <Box>
                  {[1, 2, 3].map((index) => (
                    <Box key={index} display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                      <Skeleton variant="text" width="50%" />
                      <Skeleton variant="rectangular" width={60} height={24} />
                    </Box>
                  ))}
                </Box>
              ) : featureFlagsApi.error ? (
                <Alert severity="error">
                  Failed to load feature flags: {featureFlagsApi.error}
                </Alert>
              ) : (
                <Box>
                  {featureFlagsApi.data?.map((flag: any) => (
                    <Box key={flag.name} display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                      <Box>
                        <Typography variant="body2" fontWeight="medium">
                          {flag.name}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {flag.description || 'No description'}
                        </Typography>
                      </Box>
                      <Box display="flex" alignItems="center" gap={1}>
                        <Chip 
                          label={flag.enabled ? 'ON' : 'OFF'} 
                          size="small" 
                          color={flag.enabled ? 'success' : 'default'}
                        />
                        <Tooltip title={flag.enabled ? 'Disable Feature' : 'Enable Feature'}>
                          <Switch
                            checked={flag.enabled}
                            onChange={() => handleFeatureFlagToggle(flag.name, flag.enabled)}
                            size="small"
                          />
                        </Tooltip>
                      </Box>
                    </Box>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Pods Management */}
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Kubernetes Pods
              </Typography>
              
              {podsApi.loading ? (
                <ServiceTableSkeleton />
              ) : podsApi.error ? (
                <Alert severity="error" sx={{ mb: 2 }}>
                  Failed to load pods: {podsApi.error}
                </Alert>
              ) : (
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Pod Name</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Restarts</TableCell>
                        <TableCell>Age</TableCell>
                        <TableCell>CPU</TableCell>
                        <TableCell>Memory</TableCell>
                        <TableCell>Node</TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {podsApi.data?.map((pod: any) => (
                        <TableRow key={pod.name}>
                          <TableCell>
                            <Typography variant="body2" fontWeight="medium">
                              {pod.name}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Chip 
                              label={pod.status} 
                              size="small" 
                              color={pod.status === 'Running' ? 'success' : 'error'}
                            />
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {pod.restarts}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {pod.age}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {pod.cpu}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {pod.memory}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {pod.node}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Tooltip title="Restart Pod">
                              <IconButton
                                size="small"
                                onClick={() => handlePodRestart(pod.name)}
                                disabled={!canManageServices()}
                              >
                                <RestartAlt />
                              </IconButton>
                            </Tooltip>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Service Control Dialog */}
      <Dialog open={controlDialogOpen} onClose={() => setControlDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {controlAction} Service: {selectedService?.name}
        </DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField
              label="Reason (Optional)"
              value={controlReason}
              onChange={(e) => setControlReason(e.target.value)}
              fullWidth
              multiline
              rows={3}
              placeholder="Enter reason for this action..."
            />
            <FormControlLabel
              control={
                <Switch
                  checked={forceControl}
                  onChange={(e) => setForceControl(e.target.checked)}
                />
              }
              label="Force Action (Skip Safety Checks)"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setControlDialogOpen(false)}>
            Cancel
          </Button>
          <Button 
            onClick={confirmServiceControl} 
            variant="contained"
            color={controlAction === 'STOP' ? 'error' : 'primary'}
          >
            {controlAction} Service
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ServiceManagement;