import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Chip,
  LinearProgress,
  Alert,
  Button,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  Security as SecurityIcon,
  Settings as SettingsIcon,
  Storage as StorageIcon,
  CloudQueue as CloudQueueIcon,
  Monitor as MonitorIcon,
} from '@mui/icons-material';
import { authApi } from '../services/authApi';
import { configApi } from '../services/configApi';

interface ServiceStatus {
  name: string;
  status: 'healthy' | 'degraded' | 'unhealthy';
  responseTime: number;
  lastCheck: string;
  version?: string;
  uptime?: string;
}

interface MicroservicesDashboardProps {
  // Add any props if needed
}

const MicroservicesDashboard: React.FC<MicroservicesDashboardProps> = () => {
  const [services, setServices] = useState<ServiceStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastRefresh, setLastRefresh] = useState<Date>(new Date());

  const serviceConfigs = [
    {
      name: 'Authentication Service',
      icon: <SecurityIcon />,
      color: 'primary',
      url: process.env.REACT_APP_AUTH_SERVICE_URL || 'http://localhost:8080/api/v1/auth',
    },
    {
      name: 'Configuration Service',
      icon: <SettingsIcon />,
      color: 'secondary',
      url: process.env.REACT_APP_CONFIG_SERVICE_URL || 'http://localhost:8080/api/v1/config',
    },
    {
      name: 'Middleware Service',
      icon: <StorageIcon />,
      color: 'success',
      url: process.env.REACT_APP_MIDDLEWARE_SERVICE_URL || 'http://localhost:8080/api/v1/iso20022',
    },
    {
      name: 'Core Banking Service',
      icon: <StorageIcon />,
      color: 'info',
      url: process.env.REACT_APP_CORE_BANKING_SERVICE_URL || 'http://localhost:8080/api/v1/banking',
    },
    {
      name: 'API Gateway',
      icon: <CloudQueueIcon />,
      color: 'warning',
      url: process.env.REACT_APP_GATEWAY_SERVICE_URL || 'http://localhost:8080',
    },
    {
      name: 'Monitoring Service',
      icon: <MonitorIcon />,
      color: 'error',
      url: process.env.REACT_APP_MONITORING_SERVICE_URL || 'http://localhost:8080/actuator',
    },
  ];

  const checkServiceHealth = async (service: any): Promise<ServiceStatus> => {
    const startTime = Date.now();
    try {
      const response = await fetch(`${service.url}/health`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        signal: AbortSignal.timeout(5000), // 5 second timeout
      });

      const responseTime = Date.now() - startTime;
      
      if (response.ok) {
        return {
          name: service.name,
          status: 'healthy',
          responseTime,
          lastCheck: new Date().toISOString(),
          version: '1.0.0', // This would come from the actual response
          uptime: '99.9%', // This would come from the actual response
        };
      } else {
        return {
          name: service.name,
          status: 'degraded',
          responseTime,
          lastCheck: new Date().toISOString(),
        };
      }
    } catch (error) {
      const responseTime = Date.now() - startTime;
      return {
        name: service.name,
        status: 'unhealthy',
        responseTime,
        lastCheck: new Date().toISOString(),
      };
    }
  };

  const loadServices = async () => {
    try {
      setLoading(true);
      setError(null);

      const servicePromises = serviceConfigs.map(service => checkServiceHealth(service));
      const serviceStatuses = await Promise.all(servicePromises);
      
      setServices(serviceStatuses);
      setLastRefresh(new Date());
    } catch (err: any) {
      setError(err.message || 'Failed to load service status');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadServices();
    
    // Set up auto-refresh every 30 seconds
    const interval = setInterval(loadServices, 30000);
    return () => clearInterval(interval);
  }, []);

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'healthy':
        return <CheckCircleIcon color="success" />;
      case 'degraded':
        return <WarningIcon color="warning" />;
      case 'unhealthy':
        return <ErrorIcon color="error" />;
      default:
        return <InfoIcon color="info" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'healthy':
        return 'success';
      case 'degraded':
        return 'warning';
      case 'unhealthy':
        return 'error';
      default:
        return 'default';
    }
  };

  const getOverallStatus = () => {
    const healthyCount = services.filter(s => s.status === 'healthy').length;
    const degradedCount = services.filter(s => s.status === 'degraded').length;
    const unhealthyCount = services.filter(s => s.status === 'unhealthy').length;

    if (unhealthyCount > 0) return 'unhealthy';
    if (degradedCount > 0) return 'degraded';
    return 'healthy';
  };

  const overallStatus = getOverallStatus();
  const healthyCount = services.filter(s => s.status === 'healthy').length;
  const totalCount = services.length;

  return (
    <Box sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1">
          Microservices Dashboard
        </Typography>
        <Box display="flex" alignItems="center" gap={2}>
          <Typography variant="body2" color="text.secondary">
            Last updated: {lastRefresh.toLocaleTimeString()}
          </Typography>
          <Tooltip title="Refresh Services">
            <IconButton onClick={loadServices} disabled={loading}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Overall Status Card */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Box>
              <Typography variant="h6" gutterBottom>
                Overall System Status
              </Typography>
              <Box display="flex" alignItems="center" gap={2}>
                {getStatusIcon(overallStatus)}
                <Chip
                  label={overallStatus.toUpperCase()}
                  color={getStatusColor(overallStatus) as any}
                  size="large"
                />
              </Box>
            </Box>
            <Box textAlign="right">
              <Typography variant="h4" color="primary">
                {healthyCount}/{totalCount}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Services Healthy
              </Typography>
            </Box>
          </Box>
          <LinearProgress
            variant="determinate"
            value={(healthyCount / totalCount) * 100}
            sx={{ mt: 2 }}
          />
        </CardContent>
      </Card>

      {/* Services Grid */}
      <Grid container spacing={3}>
        {services.map((service, index) => (
          <Grid item xs={12} sm={6} md={4} key={service.name}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                  <Box display="flex" alignItems="center" gap={1}>
                    {serviceConfigs[index].icon}
                    <Typography variant="h6" component="h2">
                      {service.name}
                    </Typography>
                  </Box>
                  {getStatusIcon(service.status)}
                </Box>

                <Box mb={2}>
                  <Chip
                    label={service.status.toUpperCase()}
                    color={getStatusColor(service.status) as any}
                    size="small"
                  />
                </Box>

                <Box display="flex" justifyContent="space-between" mb={1}>
                  <Typography variant="body2" color="text.secondary">
                    Response Time:
                  </Typography>
                  <Typography variant="body2">
                    {service.responseTime}ms
                  </Typography>
                </Box>

                {service.version && (
                  <Box display="flex" justifyContent="space-between" mb={1}>
                    <Typography variant="body2" color="text.secondary">
                      Version:
                    </Typography>
                    <Typography variant="body2">
                      {service.version}
                    </Typography>
                  </Box>
                )}

                {service.uptime && (
                  <Box display="flex" justifyContent="space-between" mb={1}>
                    <Typography variant="body2" color="text.secondary">
                      Uptime:
                    </Typography>
                    <Typography variant="body2">
                      {service.uptime}
                    </Typography>
                  </Box>
                )}

                <Box display="flex" justifyContent="space-between">
                  <Typography variant="body2" color="text.secondary">
                    Last Check:
                  </Typography>
                  <Typography variant="body2">
                    {new Date(service.lastCheck).toLocaleTimeString()}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Service Details */}
      <Card sx={{ mt: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Service Details
          </Typography>
          <Box display="flex" flexWrap="wrap" gap={2}>
            {services.map((service) => (
              <Card key={service.name} variant="outlined" sx={{ minWidth: 200 }}>
                <CardContent>
                  <Typography variant="subtitle1" gutterBottom>
                    {service.name}
                  </Typography>
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    {getStatusIcon(service.status)}
                    <Chip
                      label={service.status}
                      color={getStatusColor(service.status) as any}
                      size="small"
                    />
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    Response Time: {service.responseTime}ms
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Last Check: {new Date(service.lastCheck).toLocaleString()}
                  </Typography>
                </CardContent>
              </Card>
            ))}
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default MicroservicesDashboard;