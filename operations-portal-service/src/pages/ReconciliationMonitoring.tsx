/**
 * Reconciliation Monitoring Page
 * 
 * Real-time monitoring of reconciliation batches and performance metrics.
 * Integrates with Reconciliation Service for batch management and analytics.
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
  LinearProgress,
  Alert,
  TextField,
  Skeleton,
  Tooltip,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tabs,
  Tab,
} from '@mui/material';
import { Grid } from '@mui/material';
import {
  Refresh,
  Visibility,
  Warning,
  CheckCircle,
  Error,
  TrendingUp,
  TrendingDown,
  Download,
  PlayArrow,
  Stop,
  Pause,
  Assessment,
  Timeline,
} from '@mui/icons-material';
import { useApi, usePagination } from '../hooks';
import { getReconciliationService } from '../services';
import { ReconciliationBatch, PerformanceMetric, ReconciliationException, ReconciliationBatchStatus, ReconciliationPerformanceMetric } from '../types/reconciliation';
import { useNotification } from '../contexts';
import { usePermissions } from '../hooks/usePermissions';

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
      id={`reconciliation-tabpanel-${index}`}
      aria-labelledby={`reconciliation-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const ReconciliationMonitoring: React.FC = () => {
  const { showSuccess, showError } = useNotification();
  const { canManageReconciliation, canViewReconciliation } = usePermissions();
  const [activeTab, setActiveTab] = useState(0);
  const [selectedBatch, setSelectedBatch] = useState<ReconciliationBatch | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [selectedMetric, setSelectedMetric] = useState<string>('');

  // Pagination hook
  const pagination = usePagination({
    initialPage: 0,
    initialPageSize: 20,
    totalItems: 0,
  });

  // API hooks for fetching data
  const batchesApi = useApi(
    () => getReconciliationService().getReconciliationBatches({
      page: pagination.currentPage,
      size: pagination.pageSize,
    }),
    { immediate: true, showNotifications: false }
  );

  const metricsApi = useApi(
    () => getReconciliationService().getPerformanceMetrics(),
    { immediate: true, showNotifications: false }
  );

  const exceptionsApi = useApi(
    () => getReconciliationService().getReconciliationExceptions(),
    { immediate: true, showNotifications: false }
  );

  const batchDetailsApi = useApi(
    (batchId: string) => getReconciliationService().getBatchDetails(batchId),
    { immediate: false, showNotifications: false }
  );

  // Auto-refresh data every 30 seconds
  useEffect(() => {
    const interval = setInterval(() => {
      batchesApi.execute();
      metricsApi.execute();
      exceptionsApi.execute();
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  // Handle tab change
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  // Handle batch control
  const handleBatchControl = async (batchId: string, action: 'START' | 'STOP' | 'PAUSE') => {
    if (!canManageReconciliation()) {
      showError('Access Denied', 'You do not have permission to manage reconciliation batches');
      return;
    }

    try {
      const reconciliationService = getReconciliationService();
      if (action === 'START') {
        await reconciliationService.startBatch(batchId, 'Batch started by operations team');
        showSuccess('Batch Started', 'Reconciliation batch started successfully');
      } else if (action === 'STOP') {
        await reconciliationService.stopBatch(batchId, 'Batch stopped by operations team');
        showSuccess('Batch Stopped', 'Reconciliation batch stopped successfully');
      } else if (action === 'PAUSE') {
        await reconciliationService.pauseBatch(batchId, 'Batch paused by operations team');
        showSuccess('Batch Paused', 'Reconciliation batch paused successfully');
      }
      
      batchesApi.execute();
    } catch (error: any) {
      showError('Batch Control Failed', error.message || 'Failed to control batch');
    }
  };

  // Handle batch details
  const handleViewDetails = async (batch: ReconciliationBatch) => {
    try {
      await batchDetailsApi.execute(batch.id);
      setSelectedBatch(batch);
      setDetailsDialogOpen(true);
    } catch (error: any) {
      showError('Load Failed', error.message || 'Failed to load batch details');
    }
  };

  // Handle download report
  const handleDownloadReport = async (batchId: string, format: 'CSV' | 'EXCEL' | 'PDF') => {
    try {
      const reconciliationService = getReconciliationService();
      await reconciliationService.downloadReconciliationReport(batchId, format);
      showSuccess('Download Started', 'Reconciliation report download initiated');
    } catch (error: any) {
      showError('Download Failed', error.message || 'Failed to download report');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'completed': return 'success';
      case 'processing': return 'info';
      case 'failed': return 'error';
      case 'pending': return 'warning';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'completed': return <CheckCircle color="success" />;
      case 'processing': return <CircularProgress size={16} />;
      case 'failed': return <Error color="error" />;
      case 'pending': return <Warning color="warning" />;
      default: return <Warning color="disabled" />;
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const formatDuration = (startTime: string, endTime?: string) => {
    const start = new Date(startTime);
    const end = endTime ? new Date(endTime) : new Date();
    const duration = end.getTime() - start.getTime();
    
    const hours = Math.floor(duration / (1000 * 60 * 60));
    const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((duration % (1000 * 60)) / 1000);
    
    if (hours > 0) return `${hours}h ${minutes}m`;
    if (minutes > 0) return `${minutes}m ${seconds}s`;
    return `${seconds}s`;
  };

  const formatPercentage = (value: number) => {
    return `${(value * 100).toFixed(1)}%`;
  };

  // Loading skeleton component
  const BatchTableSkeleton = () => (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell><Skeleton variant="text" width="60%" /></TableCell>
            <TableCell><Skeleton variant="rectangular" width={80} height={24} /></TableCell>
            <TableCell><Skeleton variant="text" width="40%" /></TableCell>
            <TableCell><Skeleton variant="text" width="30%" /></TableCell>
            <TableCell><Skeleton variant="text" width="50%" /></TableCell>
            <TableCell><Skeleton variant="text" width="40%" /></TableCell>
            <TableCell><Skeleton variant="rectangular" width={100} height={32} /></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {[1, 2, 3, 4, 5].map((index) => (
            <TableRow key={index}>
              <TableCell><Skeleton variant="text" width="60%" /></TableCell>
              <TableCell><Skeleton variant="rectangular" width={80} height={24} /></TableCell>
              <TableCell><Skeleton variant="text" width="40%" /></TableCell>
              <TableCell><Skeleton variant="text" width="30%" /></TableCell>
              <TableCell><Skeleton variant="text" width="50%" /></TableCell>
              <TableCell><Skeleton variant="text" width="40%" /></TableCell>
              <TableCell><Skeleton variant="rectangular" width={100} height={32} /></TableCell>
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
            Reconciliation Monitoring
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Monitor reconciliation batches and performance metrics
          </Typography>
        </Box>
        <Box display="flex" gap={1}>
          <Button
            variant="outlined"
            startIcon={<Refresh />}
            onClick={() => {
              batchesApi.execute();
              metricsApi.execute();
              exceptionsApi.execute();
            }}
            disabled={batchesApi.loading}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<Download />}
            onClick={() => {/* Implement bulk download */}}
            disabled={!canViewReconciliation()}
          >
            Download Reports
          </Button>
        </Box>
      </Box>

      {/* Performance Metrics Cards */}
      {metricsApi.data && (
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" gap={1}>
                  <Assessment color="primary" />
                  <Typography variant="h6" color="primary.main">
                    {metricsApi.data.totalBatches.toLocaleString()}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Total Batches
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" gap={1}>
                  <CheckCircle color="success" />
                  <Typography variant="h6" color="success.main">
                    {formatPercentage(metricsApi.data.successRate)}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Success Rate
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" gap={1}>
                  <Timeline color="info" />
                  <Typography variant="h6" color="info.main">
                    {formatDuration(metricsApi.data.averageProcessingTime.toString())}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Avg Processing Time
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" gap={1}>
                  <Error color="error" />
                  <Typography variant="h6" color="error.main">
                    {metricsApi.data.failedBatches.toLocaleString()}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Failed Batches
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={handleTabChange} aria-label="reconciliation tabs">
            <Tab label="Reconciliation Batches" />
            <Tab label="Performance Metrics" />
            <Tab label="Exceptions" />
          </Tabs>
        </Box>

        {/* Reconciliation Batches Tab */}
        <TabPanel value={activeTab} index={0}>
          {batchesApi.loading ? (
            <BatchTableSkeleton />
          ) : batchesApi.error ? (
            <Alert severity="error" sx={{ mb: 2 }}>
              Failed to load batches: {batchesApi.error}
            </Alert>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Batch Name</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Progress</TableCell>
                    <TableCell>Start Time</TableCell>
                    <TableCell>Duration</TableCell>
                    <TableCell>Success Rate</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {batchesApi.data?.content?.map((batch: ReconciliationBatch) => (
                    <TableRow key={batch.id}>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">
                          {batch.batchName}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box display="flex" alignItems="center" gap={1}>
                          {getStatusIcon(batch.status)}
                          <Chip 
                            label={batch.status} 
                            size="small" 
                            color={getStatusColor(batch.status) as any}
                          />
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Box>
                          <Box display="flex" justifyContent="space-between" mb={1}>
                            <Typography variant="caption">
                              {batch.processedRecords} / {batch.totalRecords}
                            </Typography>
                            <Typography variant="caption">
                              {formatPercentage(batch.successRate)}
                            </Typography>
                          </Box>
                          <LinearProgress 
                            variant="determinate" 
                            value={(batch.processedRecords / batch.totalRecords) * 100}
                            color={batch.status === ReconciliationBatchStatus.FAILED ? 'error' : 'primary'}
                          />
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {formatDate(batch.startTime)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {formatDuration(batch.startTime, batch.endTime)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {formatPercentage(batch.successRate)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box display="flex" gap={1}>
                          {batch.status === ReconciliationBatchStatus.PENDING && (
                            <Tooltip title="Start Batch">
                              <IconButton
                                size="small"
                                onClick={() => handleBatchControl(batch.id, 'START')}
                                disabled={!canManageReconciliation()}
                              >
                                <PlayArrow />
                              </IconButton>
                            </Tooltip>
                          )}
                          {batch.status === ReconciliationBatchStatus.PROCESSING && (
                            <>
                              <Tooltip title="Pause Batch">
                                <IconButton
                                  size="small"
                                  onClick={() => handleBatchControl(batch.id, 'PAUSE')}
                                  disabled={!canManageReconciliation()}
                                >
                                  <Pause />
                                </IconButton>
                              </Tooltip>
                              <Tooltip title="Stop Batch">
                                <IconButton
                                  size="small"
                                  onClick={() => handleBatchControl(batch.id, 'STOP')}
                                  disabled={!canManageReconciliation()}
                                >
                                  <Stop />
                                </IconButton>
                              </Tooltip>
                            </>
                          )}
                          <Tooltip title="View Details">
                            <IconButton
                              size="small"
                              onClick={() => handleViewDetails(batch)}
                            >
                              <Visibility />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Download Report">
                            <IconButton
                              size="small"
                              onClick={() => handleDownloadReport(batch.id, 'CSV')}
                              disabled={!canViewReconciliation()}
                            >
                              <Download />
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
        </TabPanel>

        {/* Performance Metrics Tab */}
        <TabPanel value={activeTab} index={1}>
          <Typography variant="h6" gutterBottom>
            Performance Metrics
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Detailed performance analysis and trends
          </Typography>
          
          {metricsApi.loading ? (
            <Box>
              {[1, 2, 3].map((index) => (
                <Card key={index} sx={{ mb: 2 }}>
                  <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="center">
                      <Skeleton variant="text" width="40%" />
                      <Skeleton variant="text" width="20%" />
                    </Box>
                    <Skeleton variant="rectangular" height={8} sx={{ mt: 1 }} />
                  </CardContent>
                </Card>
              ))}
            </Box>
          ) : metricsApi.error ? (
            <Alert severity="error">
              Failed to load metrics: {metricsApi.error}
            </Alert>
          ) : (
            <Grid container spacing={2}>
              {metricsApi.data?.detailedMetrics?.map((metric: PerformanceMetric) => (
                <Grid size={{ xs: 12, md: 6 }} key={metric.name}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        {metric.name}
                      </Typography>
                      <Box display="flex" alignItems="center" gap={1} mb={1}>
                        <Typography variant="h4" color="primary.main">
                          {metric.value}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {metric.unit}
                        </Typography>
                      </Box>
                      <Box display="flex" alignItems="center" gap={1}>
                        {metric.trend === 'up' ? (
                          <TrendingUp color="success" />
                        ) : metric.trend === 'down' ? (
                          <TrendingDown color="error" />
                        ) : null}
                        <Typography variant="body2" color="text.secondary">
                          {metric.description}
                        </Typography>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}
        </TabPanel>

        {/* Exceptions Tab */}
        <TabPanel value={activeTab} index={2}>
          <Typography variant="h6" gutterBottom>
            Reconciliation Exceptions
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Failed reconciliation records and exceptions
          </Typography>
          
          {exceptionsApi.loading ? (
            <BatchTableSkeleton />
          ) : exceptionsApi.error ? (
            <Alert severity="error" sx={{ mb: 2 }}>
              Failed to load exceptions: {exceptionsApi.error}
            </Alert>
          ) : !exceptionsApi.data?.content || exceptionsApi.data.content.length === 0 ? (
            <Alert severity="info">
              No reconciliation exceptions found
            </Alert>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Exception ID</TableCell>
                    <TableCell>Batch ID</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Severity</TableCell>
                    <TableCell>Message</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {exceptionsApi.data?.content?.map((exception: ReconciliationException) => (
                    <TableRow key={exception.id}>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">
                          {exception.id}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {exception.batchId}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={exception.exceptionType} 
                          size="small" 
                          color="default"
                        />
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={exception.severity} 
                          size="small" 
                          color={exception.severity === 'HIGH' ? 'error' : 
                                 exception.severity === 'MEDIUM' ? 'warning' : 'info'}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {exception.description}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {formatDate(exception.createdAt)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Tooltip title="View Details">
                          <IconButton size="small">
                            <Visibility />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </TabPanel>
      </Card>

      {/* Batch Details Dialog */}
      <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Batch Details - {selectedBatch?.batchName}
        </DialogTitle>
        <DialogContent>
          {selectedBatch && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  label="Batch ID"
                  value={selectedBatch.id}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  label="Batch Name"
                  value={selectedBatch.batchName}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  label="Status"
                  value={selectedBatch.status}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  label="Success Rate"
                  value={formatPercentage(selectedBatch.successRate)}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  label="Total Records"
                  value={selectedBatch.totalRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  label="Processed Records"
                  value={selectedBatch.processedRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  label="Failed Records"
                  value={selectedBatch.failedRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  label="Start Time"
                  value={formatDate(selectedBatch.startTime)}
                  fullWidth
                  disabled
                />
              </Grid>
              {selectedBatch.endTime && (
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="End Time"
                    value={formatDate(selectedBatch.endTime)}
                    fullWidth
                    disabled
                  />
                </Grid>
              )}
              {selectedBatch.processingTime && (
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="Processing Time"
                    value={formatDuration(selectedBatch.startTime, selectedBatch.endTime)}
                    fullWidth
                    disabled
                  />
                </Grid>
              )}
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsDialogOpen(false)}>
            Close
          </Button>
          <Button 
            variant="contained"
            onClick={() => selectedBatch && handleDownloadReport(selectedBatch.id, 'PDF')}
          >
            Download Report
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ReconciliationMonitoring;