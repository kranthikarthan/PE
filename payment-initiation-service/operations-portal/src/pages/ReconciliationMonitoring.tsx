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
  LinearProgress,
  Alert,
  TextField,
} from '@mui/material';
import { GridLegacy as Grid } from '@mui/material';
import {
  Refresh,
  Visibility,
  Warning,
  CheckCircle,
  Error,
  TrendingUp,
  TrendingDown,
} from '@mui/icons-material';

interface ReconciliationBatch {
  id: string;
  batchName: string;
  status: 'completed' | 'processing' | 'failed' | 'pending';
  startTime: string;
  endTime?: string;
  totalRecords: number;
  processedRecords: number;
  failedRecords: number;
  successRate: number;
  processingTime?: string;
}

interface PerformanceMetric {
  name: string;
  value: string;
  trend: 'up' | 'down' | 'stable';
  change: string;
}

const ReconciliationMonitoring: React.FC = () => {
  const [selectedBatch, setSelectedBatch] = useState<ReconciliationBatch | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);

  const [batches] = useState<ReconciliationBatch[]>([
    {
      id: 'BATCH-001',
      batchName: 'Daily Reconciliation - 2025-10-20',
      status: 'completed',
      startTime: '2025-10-20 00:00:00',
      endTime: '2025-10-20 02:30:00',
      totalRecords: 15000,
      processedRecords: 14985,
      failedRecords: 15,
      successRate: 99.9,
      processingTime: '2h 30m',
    },
    {
      id: 'BATCH-002',
      batchName: 'Hourly Reconciliation - 2025-10-20 10:00',
      status: 'processing',
      startTime: '2025-10-20 10:00:00',
      totalRecords: 5000,
      processedRecords: 3200,
      failedRecords: 0,
      successRate: 100,
    },
    {
      id: 'BATCH-003',
      batchName: 'Daily Reconciliation - 2025-10-19',
      status: 'failed',
      startTime: '2025-10-19 00:00:00',
      endTime: '2025-10-19 00:45:00',
      totalRecords: 12000,
      processedRecords: 8500,
      failedRecords: 3500,
      successRate: 70.8,
      processingTime: '45m',
    },
  ]);

  const [performanceMetrics] = useState<PerformanceMetric[]>([
    { name: 'Average Processing Time', value: '1h 45m', trend: 'down', change: '-15%' },
    { name: 'Success Rate', value: '99.2%', trend: 'up', change: '+0.3%' },
    { name: 'Records per Hour', value: '8,500', trend: 'up', change: '+12%' },
    { name: 'Error Rate', value: '0.8%', trend: 'down', change: '-0.2%' },
  ]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed': return 'success';
      case 'processing': return 'info';
      case 'failed': return 'error';
      case 'pending': return 'warning';
      default: return 'default';
    }
  };

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'up': return <TrendingUp color="success" />;
      case 'down': return <TrendingDown color="error" />;
      default: return <CheckCircle color="info" />;
    }
  };

  const handleViewDetails = (batch: ReconciliationBatch) => {
    setSelectedBatch(batch);
    setDetailsDialogOpen(true);
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Reconciliation Monitoring
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Monitor reconciliation processes and handle exceptions
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Refresh />}
          onClick={() => window.location.reload()}
        >
          Refresh
        </Button>
      </Box>

      <Grid container spacing={3}>
        {/* Performance Metrics */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Performance Metrics
              </Typography>
              <Grid container spacing={2}>
                {performanceMetrics.map((metric, index) => (
                  <Grid item xs={12} sm={6} md={3} key={index}>
                    <Box textAlign="center" p={2}>
                      <Box display="flex" alignItems="center" justifyContent="center" mb={1}>
                        {getTrendIcon(metric.trend)}
                      </Box>
                      <Typography variant="h6" fontWeight="bold">
                        {metric.value}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {metric.name}
                      </Typography>
                      <Typography 
                        variant="caption" 
                        color={metric.trend === 'up' ? 'success.main' : metric.trend === 'down' ? 'error.main' : 'text.secondary'}
                      >
                        {metric.change}
                      </Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Summary Cards */}
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <CheckCircle color="success" />
                <Typography variant="h6" sx={{ ml: 1 }}>
                  {batches.filter(b => b.status === 'completed').length}
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Completed Batches
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <Warning color="warning" />
                <Typography variant="h6" sx={{ ml: 1 }}>
                  {batches.filter(b => b.status === 'processing').length}
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Processing Batches
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <Error color="error" />
                <Typography variant="h6" sx={{ ml: 1 }}>
                  {batches.filter(b => b.status === 'failed').length}
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Failed Batches
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <CheckCircle color="info" />
                <Typography variant="h6" sx={{ ml: 1 }}>
                  {batches.reduce((sum, b) => sum + b.totalRecords, 0).toLocaleString()}
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Total Records
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Reconciliation Batches */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Reconciliation Batches
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Batch ID</TableCell>
                      <TableCell>Batch Name</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Start Time</TableCell>
                      <TableCell>End Time</TableCell>
                      <TableCell>Total Records</TableCell>
                      <TableCell>Processed</TableCell>
                      <TableCell>Failed</TableCell>
                      <TableCell>Success Rate</TableCell>
                      <TableCell>Processing Time</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {batches.map((batch) => (
                      <TableRow key={batch.id}>
                        <TableCell>
                          <Typography variant="subtitle2" fontWeight="bold">
                            {batch.id}
                          </Typography>
                        </TableCell>
                        <TableCell>{batch.batchName}</TableCell>
                        <TableCell>
                          <Chip 
                            label={batch.status.toUpperCase()} 
                            color={getStatusColor(batch.status) as any}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>{batch.startTime}</TableCell>
                        <TableCell>{batch.endTime || '-'}</TableCell>
                        <TableCell>{batch.totalRecords.toLocaleString()}</TableCell>
                        <TableCell>{batch.processedRecords.toLocaleString()}</TableCell>
                        <TableCell>
                          <Typography color={batch.failedRecords > 0 ? 'error' : 'text.secondary'}>
                            {batch.failedRecords.toLocaleString()}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1}>
                            <Typography variant="body2">
                              {batch.successRate.toFixed(1)}%
                            </Typography>
                            <LinearProgress 
                              variant="determinate" 
                              value={batch.successRate} 
                              sx={{ width: 50, height: 4 }}
                              color={batch.successRate >= 95 ? 'success' : batch.successRate >= 80 ? 'warning' : 'error'}
                            />
                          </Box>
                        </TableCell>
                        <TableCell>{batch.processingTime || '-'}</TableCell>
                        <TableCell>
                          <IconButton
                            size="small"
                            onClick={() => handleViewDetails(batch)}
                          >
                            <Visibility />
                          </IconButton>
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

      {/* Batch Details Dialog */}
      <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Batch Details - {selectedBatch?.id}
        </DialogTitle>
        <DialogContent>
          {selectedBatch && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Batch ID"
                  value={selectedBatch.id}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Status"
                  value={selectedBatch.status}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Batch Name"
                  value={selectedBatch.batchName}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Start Time"
                  value={selectedBatch.startTime}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="End Time"
                  value={selectedBatch.endTime || '-'}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField
                  label="Total Records"
                  value={selectedBatch.totalRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField
                  label="Processed Records"
                  value={selectedBatch.processedRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField
                  label="Failed Records"
                  value={selectedBatch.failedRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Success Rate"
                  value={`${selectedBatch.successRate.toFixed(1)}%`}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Processing Time"
                  value={selectedBatch.processingTime || '-'}
                  fullWidth
                  disabled
                />
              </Grid>
              {selectedBatch.failedRecords > 0 && (
                <Grid item xs={12}>
                  <Alert severity="warning">
                    This batch has {selectedBatch.failedRecords} failed records that require attention.
                  </Alert>
                </Grid>
              )}
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsDialogOpen(false)}>Close</Button>
          <Button variant="contained" onClick={() => setDetailsDialogOpen(false)}>
            Export Details
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ReconciliationMonitoring;