import React from 'react';
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
} from '@mui/material';
import { GridLegacy as Grid } from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Warning,
  CheckCircle,
  Refresh,
  MoreVert,
} from '@mui/icons-material';

const Dashboard: React.FC = () => {
  const systemHealth = [
    { service: 'Payment Initiation', status: 'Healthy', uptime: '99.9%', responseTime: '45ms' },
    { service: 'Saga Orchestrator', status: 'Healthy', uptime: '99.8%', responseTime: '120ms' },
    { service: 'Analytics Service', status: 'Warning', uptime: '98.5%', responseTime: '200ms' },
    { service: 'Reconciliation Service', status: 'Healthy', uptime: '99.7%', responseTime: '80ms' },
  ];

  const recentAlerts = [
    { id: 1, message: 'High error rate detected in Analytics Service', severity: 'warning', time: '2 minutes ago' },
    { id: 2, message: 'Payment processing delay in Saga Orchestrator', severity: 'info', time: '15 minutes ago' },
    { id: 3, message: 'Reconciliation batch completed successfully', severity: 'success', time: '1 hour ago' },
  ];

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'healthy': return 'success';
      case 'warning': return 'warning';
      case 'error': return 'error';
      default: return 'default';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'error': return 'error';
      case 'warning': return 'warning';
      case 'info': return 'info';
      case 'success': return 'success';
      default: return 'default';
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Operations Dashboard
      </Typography>
      <Typography variant="subtitle1" color="text.secondary" sx={{ mb: 3 }}>
        Real-time monitoring and management of payment processing systems
      </Typography>

      <Grid container spacing={3}>
        {/* System Health Overview */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">System Health Overview</Typography>
                <IconButton size="small">
                  <Refresh />
                </IconButton>
              </Box>
              <List>
                {systemHealth.map((service, index) => (
                  <ListItem key={index} divider={index < systemHealth.length - 1}>
                    <ListItemIcon>
                      <CheckCircle color={getStatusColor(service.status) as any} />
                    </ListItemIcon>
                    <ListItemText
                      primary={service.service}
                      secondary={
                        <Box display="flex" alignItems="center" gap={2} mt={1}>
                          <Chip 
                            label={service.status} 
                            size="small" 
                            color={getStatusColor(service.status) as any}
                          />
                          <Typography variant="caption" color="text.secondary">
                            Uptime: {service.uptime}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            Response: {service.responseTime}
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
            </CardContent>
          </Card>
        </Grid>

        {/* Quick Stats */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Quick Stats
              </Typography>
              <Box mb={2}>
                <Box display="flex" justifyContent="space-between" mb={1}>
                  <Typography variant="body2">Total Payments Today</Typography>
                  <Typography variant="body2" fontWeight="bold">12,847</Typography>
                </Box>
                <LinearProgress variant="determinate" value={75} />
              </Box>
              <Box mb={2}>
                <Box display="flex" justifyContent="space-between" mb={1}>
                  <Typography variant="body2">Success Rate</Typography>
                  <Typography variant="body2" fontWeight="bold">99.2%</Typography>
                </Box>
                <LinearProgress variant="determinate" value={99.2} color="success" />
              </Box>
              <Box mb={2}>
                <Box display="flex" justifyContent="space-between" mb={1}>
                  <Typography variant="body2">Failed Payments</Typography>
                  <Typography variant="body2" fontWeight="bold">103</Typography>
                </Box>
                <LinearProgress variant="determinate" value={15} color="error" />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Alerts */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Alerts
              </Typography>
              <List>
                {recentAlerts.map((alert) => (
                  <ListItem key={alert.id} divider>
                    <ListItemIcon>
                      {alert.severity === 'error' && <Warning color="error" />}
                      {alert.severity === 'warning' && <Warning color="warning" />}
                      {alert.severity === 'info' && <TrendingUp color="info" />}
                      {alert.severity === 'success' && <CheckCircle color="success" />}
                    </ListItemIcon>
                    <ListItemText
                      primary={alert.message}
                      secondary={alert.time}
                    />
                    <Chip 
                      label={alert.severity.toUpperCase()} 
                      size="small" 
                      color={getSeverityColor(alert.severity) as any}
                    />
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;