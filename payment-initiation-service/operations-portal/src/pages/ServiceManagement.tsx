import React, { useState } from 'react';
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
} from '@mui/material';
import {
  PlayArrow,
  Pause,
  Stop,
  Refresh,
  Settings,
  Visibility,
  MoreVert,
} from '@mui/icons-material';

interface Service {
  id: string;
  name: string;
  status: 'running' | 'stopped' | 'paused' | 'error';
  uptime: string;
  responseTime: string;
  instances: number;
  lastDeployment: string;
  version: string;
}

const ServiceManagement: React.FC = () => {
  const [services, setServices] = useState<Service[]>([
    {
      id: '1',
      name: 'Payment Initiation Service',
      status: 'running',
      uptime: '99.9%',
      responseTime: '45ms',
      instances: 3,
      lastDeployment: '2025-10-20 10:30:00',
      version: 'v1.2.3',
    },
    {
      id: '2',
      name: 'Saga Orchestrator',
      status: 'running',
      uptime: '99.8%',
      responseTime: '120ms',
      instances: 2,
      lastDeployment: '2025-10-20 09:15:00',
      version: 'v1.1.5',
    },
    {
      id: '3',
      name: 'Analytics Service',
      status: 'error',
      uptime: '98.5%',
      responseTime: '200ms',
      instances: 1,
      lastDeployment: '2025-10-19 16:45:00',
      version: 'v1.0.8',
    },
    {
      id: '4',
      name: 'Reconciliation Service',
      status: 'running',
      uptime: '99.7%',
      responseTime: '80ms',
      instances: 2,
      lastDeployment: '2025-10-20 08:20:00',
      version: 'v1.3.1',
    },
  ]);

  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'running': return 'success';
      case 'stopped': return 'error';
      case 'paused': return 'warning';
      case 'error': return 'error';
      default: return 'default';
    }
  };

  const handleServiceAction = (serviceId: string, action: string) => {
    setServices(services.map(service => 
      service.id === serviceId 
        ? { ...service, status: action as any }
        : service
    ));
  };

  const handleViewDetails = (service: Service) => {
    setSelectedService(service);
    setDialogOpen(true);
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Service Management
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Monitor and manage microservices health and performance
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Refresh />}
          onClick={() => window.location.reload()}
        >
          Refresh All
        </Button>
      </Box>

      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
        {/* Service Overview Cards */}
        <Box sx={{ flex: '1 1 300px', minWidth: '300px' }}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="success.main">
                {services.filter(s => s.status === 'running').length}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Running Services
              </Typography>
            </CardContent>
          </Card>
        </Box>
        <Box sx={{ flex: '1 1 300px', minWidth: '300px' }}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="error.main">
                {services.filter(s => s.status === 'error').length}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Error Services
              </Typography>
            </CardContent>
          </Card>
        </Box>
        <Box sx={{ flex: '1 1 300px', minWidth: '300px' }}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="warning.main">
                {services.filter(s => s.status === 'paused').length}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Paused Services
              </Typography>
            </CardContent>
          </Card>
        </Box>
        <Box sx={{ flex: '1 1 300px', minWidth: '300px' }}>
          <Card>
            <CardContent>
              <Typography variant="h6">
                {services.reduce((sum, s) => sum + s.instances, 0)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Total Instances
              </Typography>
            </CardContent>
          </Card>
        </Box>

        {/* Services Table */}
        <Box sx={{ flex: '1 1 100%', minWidth: '100%' }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Service Details
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Service Name</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Uptime</TableCell>
                      <TableCell>Response Time</TableCell>
                      <TableCell>Instances</TableCell>
                      <TableCell>Version</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {services.map((service) => (
                      <TableRow key={service.id}>
                        <TableCell>
                          <Typography variant="subtitle2" fontWeight="bold">
                            {service.name}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip 
                            label={service.status.toUpperCase()} 
                            color={getStatusColor(service.status) as any}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>{service.uptime}</TableCell>
                        <TableCell>{service.responseTime}</TableCell>
                        <TableCell>{service.instances}</TableCell>
                        <TableCell>{service.version}</TableCell>
                        <TableCell>
                          <Box display="flex" gap={1}>
                            <IconButton
                              size="small"
                              onClick={() => handleServiceAction(service.id, 'running')}
                              disabled={service.status === 'running'}
                            >
                              <PlayArrow />
                            </IconButton>
                            <IconButton
                              size="small"
                              onClick={() => handleServiceAction(service.id, 'paused')}
                              disabled={service.status === 'paused'}
                            >
                              <Pause />
                            </IconButton>
                            <IconButton
                              size="small"
                              onClick={() => handleServiceAction(service.id, 'stopped')}
                              disabled={service.status === 'stopped'}
                            >
                              <Stop />
                            </IconButton>
                            <IconButton
                              size="small"
                              onClick={() => handleViewDetails(service)}
                            >
                              <Visibility />
                            </IconButton>
                            <IconButton size="small">
                              <MoreVert />
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
        </Box>

      {/* Service Details Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Service Details - {selectedService?.name}
        </DialogTitle>
        <DialogContent>
          {selectedService && (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, mt: 1 }}>
              <Box sx={{ flex: '1 1 200px', minWidth: '200px' }}>
                <TextField
                  label="Service Name"
                  value={selectedService.name}
                  fullWidth
                  disabled
                />
              </Box>
              <Box sx={{ flex: '1 1 200px', minWidth: '200px' }}>
                <FormControl fullWidth>
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={selectedService.status}
                    label="Status"
                    onChange={(e) => setSelectedService({
                      ...selectedService,
                      status: e.target.value as any
                    })}
                  >
                    <MenuItem value="running">Running</MenuItem>
                    <MenuItem value="paused">Paused</MenuItem>
                    <MenuItem value="stopped">Stopped</MenuItem>
                    <MenuItem value="error">Error</MenuItem>
                  </Select>
                </FormControl>
              </Box>
              <Box sx={{ flex: '1 1 200px', minWidth: '200px' }}>
                <TextField
                  label="Uptime"
                  value={selectedService.uptime}
                  fullWidth
                  disabled
                />
              </Box>
              <Box sx={{ flex: '1 1 200px', minWidth: '200px' }}>
                <TextField
                  label="Response Time"
                  value={selectedService.responseTime}
                  fullWidth
                  disabled
                />
              </Box>
              <Box sx={{ flex: '1 1 200px', minWidth: '200px' }}>
                <TextField
                  label="Instances"
                  value={selectedService.instances}
                  fullWidth
                  type="number"
                  onChange={(e) => setSelectedService({
                    ...selectedService,
                    instances: parseInt(e.target.value)
                  })}
                />
              </Box>
              <Box sx={{ flex: '1 1 200px', minWidth: '200px' }}>
                <TextField
                  label="Version"
                  value={selectedService.version}
                  fullWidth
                  disabled
                />
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => setDialogOpen(false)}>
            Save Changes
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ServiceManagement;
