import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  LinearProgress,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  IconButton,
  Skeleton,
  Alert,
} from '@mui/material';
import { Grid } from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Warning,
  CheckCircle,
  Refresh,
  MoreVert,
} from '@mui/icons-material';
import { useApi } from '../hooks';
import { getOperationsManagementService } from '../services';
import { ServiceHealth } from '../types/service';
import { useNotification } from '../contexts';

const Dashboard: React.FC = () => {
  const { showError } = useNotification();
  const [refreshInterval, setRefreshInterval] = useState<NodeJS.Timeout | null>(null);

  // API hooks for fetching data
  const servicesApi = useApi(
    () => getOperationsManagementService().getAllServicesHealth(),
    { immediate: true, showNotifications: false }
  );

  const healthSummaryApi = useApi(
    () => getOperationsManagementService().getServiceHealthSummary(),
    { immediate: true, showNotifications: false }
  );

  const alertsApi = useApi(
    () => getOperationsManagementService().getServiceAlerts(),
    { immediate: true, showNotifications: false }
  );

  // Auto-refresh data every 30 seconds
  useEffect(() => {
    const interval = setInterval(() => {
      servicesApi.execute();
      healthSummaryApi.execute();
      alertsApi.execute();
    }, 30000);

    setRefreshInterval(interval);

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, []);

  // Handle refresh button click
  const handleRefresh = () => {
    servicesApi.execute();
    healthSummaryApi.execute();
    alertsApi.execute();
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'up': return 'success';
      case 'degraded': return 'warning';
      case 'down': return 'error';
      default: return 'default';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'critical': return 'error';
      case 'high': return 'error';
      case 'medium': return 'warning';
      case 'low': return 'info';
      default: return 'default';
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
  const ServiceHealthSkeleton = () => (
    <List>
      {[1, 2, 3, 4].map((index) => (
        <ListItem key={index} divider>
          <ListItemIcon>
            <Skeleton variant="circular" width={24} height={24} />
          </ListItemIcon>
          <ListItemText
            primary={<Skeleton variant="text" width="60%" />}
            secondary={<Skeleton variant="text" width="40%" />}
          />
        </ListItem>
      ))}
    </List>
  );

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Operations Dashboard
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Real-time monitoring and management of payment processing systems
          </Typography>
        </Box>
        <IconButton 
          onClick={handleRefresh}
          disabled={servicesApi.loading || healthSummaryApi.loading}
          size="large"
        >
          <Refresh />
        </IconButton>
      </Box>

      <Grid container spacing={3}>
        {/* System Health Overview */}
        <Grid size={{ xs: 12, md: 8 }}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">System Health Overview</Typography>
                <Typography variant="caption" color="text.secondary">
                  Last updated: {new Date().toLocaleTimeString()}
                </Typography>
              </Box>
              
              {servicesApi.loading ? (
                <ServiceHealthSkeleton />
              ) : servicesApi.error ? (
                <Alert severity="error" sx={{ mb: 2 }}>
                  Failed to load service health data: {servicesApi.error}
                </Alert>
              ) : (
                <List>
                  {servicesApi.data?.map((service: ServiceHealth, index: number) => (
                    <ListItem key={service.id} divider={index < (servicesApi.data?.length || 0) - 1}>
                      <ListItemIcon>
                        <CheckCircle color={getStatusColor(service.status) as any} />
                      </ListItemIcon>
                      <ListItemText
                        primary={service.name}
                        secondary={
                          <Box display="flex" alignItems="center" gap={2} mt={1}>
                            <Chip 
                              label={service.status} 
                              size="small" 
                              color={getStatusColor(service.status) as any}
                            />
                            <Typography variant="caption" color="text.secondary">
                              Uptime: {formatUptime(service.uptime)}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              Response: {formatResponseTime(service.responseTime)}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              v{service.version}
                            </Typography>
                          </Box>
                        }
                      />
                      <IconButton size="small">
                        <MoreVert />
                      </IconButton>
                    </ListItem>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Quick Stats */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                System Overview
              </Typography>
              
              {healthSummaryApi.loading ? (
                <Box>
                  {[1, 2, 3].map((index) => (
                    <Box key={index} mb={2}>
                      <Box display="flex" justifyContent="space-between" mb={1}>
                        <Skeleton variant="text" width="60%" />
                        <Skeleton variant="text" width="20%" />
                      </Box>
                      <Skeleton variant="rectangular" height={8} />
                    </Box>
                  ))}
                </Box>
              ) : healthSummaryApi.error ? (
                <Alert severity="error">
                  Failed to load system overview: {healthSummaryApi.error}
                </Alert>
              ) : (
                <Box>
                  <Box mb={2}>
                    <Box display="flex" justifyContent="space-between" mb={1}>
                      <Typography variant="body2">Total Services</Typography>
                      <Typography variant="body2" fontWeight="bold">
                        {healthSummaryApi.data?.totalServices || 0}
                      </Typography>
                    </Box>
                    <LinearProgress 
                      variant="determinate" 
                      value={100} 
                      color="primary" 
                    />
                  </Box>
                  
                  <Box mb={2}>
                    <Box display="flex" justifyContent="space-between" mb={1}>
                      <Typography variant="body2">Healthy Services</Typography>
                      <Typography variant="body2" fontWeight="bold">
                        {healthSummaryApi.data?.healthyServices || 0}
                      </Typography>
                    </Box>
                    <LinearProgress 
                      variant="determinate" 
                      value={healthSummaryApi.data ? (healthSummaryApi.data.healthyServices / healthSummaryApi.data.totalServices) * 100 : 0} 
                      color="success" 
                    />
                  </Box>
                  
                  <Box mb={2}>
                    <Box display="flex" justifyContent="space-between" mb={1}>
                      <Typography variant="body2">Unhealthy Services</Typography>
                      <Typography variant="body2" fontWeight="bold">
                        {healthSummaryApi.data?.unhealthyServices || 0}
                      </Typography>
                    </Box>
                    <LinearProgress 
                      variant="determinate" 
                      value={healthSummaryApi.data ? (healthSummaryApi.data.unhealthyServices / healthSummaryApi.data.totalServices) * 100 : 0} 
                      color="error" 
                    />
                  </Box>
                  
                  <Box mb={2}>
                    <Box display="flex" justifyContent="space-between" mb={1}>
                      <Typography variant="body2">Overall Health</Typography>
                      <Chip 
                        label={healthSummaryApi.data?.overallHealth || 'UNKNOWN'} 
                        size="small"
                        color={healthSummaryApi.data?.overallHealth === 'HEALTHY' ? 'success' : 
                               healthSummaryApi.data?.overallHealth === 'DEGRADED' ? 'warning' : 'error'}
                      />
                    </Box>
                  </Box>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Alerts */}
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Alerts
              </Typography>
              
              {alertsApi.loading ? (
                <List>
                  {[1, 2, 3].map((index) => (
                    <ListItem key={index} divider>
                      <ListItemIcon>
                        <Skeleton variant="circular" width={24} height={24} />
                      </ListItemIcon>
                      <ListItemText
                        primary={<Skeleton variant="text" width="80%" />}
                        secondary={<Skeleton variant="text" width="40%" />}
                      />
                      <Skeleton variant="rectangular" width={60} height={24} />
                    </ListItem>
                  ))}
                </List>
              ) : alertsApi.error ? (
                <Alert severity="error">
                  Failed to load alerts: {alertsApi.error}
                </Alert>
              ) : alertsApi.data?.length === 0 ? (
                <Typography variant="body2" color="text.secondary" textAlign="center" py={2}>
                  No recent alerts
                </Typography>
              ) : (
                <List>
                  {alertsApi.data?.slice(0, 5).map((alert) => (
                    <ListItem key={alert.id} divider>
                      <ListItemIcon>
                        {alert.severity === 'CRITICAL' && <Warning color="error" />}
                        {alert.severity === 'HIGH' && <Warning color="error" />}
                        {alert.severity === 'MEDIUM' && <Warning color="warning" />}
                        {alert.severity === 'LOW' && <TrendingUp color="info" />}
                      </ListItemIcon>
                      <ListItemText
                        primary={alert.message}
                        secondary={new Date(alert.timestamp).toLocaleString()}
                      />
                      <Chip 
                        label={alert.severity} 
                        size="small" 
                        color={getSeverityColor(alert.severity) as any}
                      />
                    </ListItem>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;