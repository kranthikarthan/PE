import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  LinearProgress,
  Alert,
  Tabs,
  Tab,
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
  Grid,
} from '@mui/material';
import {
  Refresh,
  PlayArrow,
  Pause,
  Stop,
  Visibility,
  Download,
  Warning,
  CheckCircle,
  Error,
} from '@mui/icons-material';

interface ReconciliationBatch {
  id: string;
  name: string;
  status: 'running' | 'completed' | 'failed' | 'paused';
  startTime: string;
  endTime?: string;
  totalRecords: number;
  processedRecords: number;
  matchedRecords: number;
  unmatchedRecords: number;
  errorRecords: number;
  progress: number;
}

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
  const [tabValue, setTabValue] = useState(0);
  const [selectedBatch, setSelectedBatch] = useState<ReconciliationBatch | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);

  const [reconciliationBatches] = useState<ReconciliationBatch[]>([
    {
      id: 'BATCH-001',
      name: 'Daily Reconciliation - 2025-10-20',
      status: 'running',
      startTime: '2025-10-20 02:00:00',
      totalRecords: 10000,
      processedRecords: 7500,
      matchedRecords: 7200,
      unmatchedRecords: 250,
      errorRecords: 50,
      progress: 75,
    },
    {
      id: 'BATCH-002',
      name: 'Hourly Reconciliation - 2025-10-20 10:00',
      status: 'completed',
      startTime: '2025-10-20 10:00:00',
      endTime: '2025-10-20 10:15:00',
      totalRecords: 5000,
      processedRecords: 5000,
      matchedRecords: 4950,
      unmatchedRecords: 40,
      errorRecords: 10,
      progress: 100,
    },
    {
      id: 'BATCH-003',
      name: 'Daily Reconciliation - 2025-10-19',
      status: 'failed',
      startTime: '2025-10-19 02:00:00',
      endTime: '2025-10-19 02:30:00',
      totalRecords: 8000,
      processedRecords: 3000,
      matchedRecords: 2800,
      unmatchedRecords: 150,
      errorRecords: 50,
      progress: 37.5,
    },
  ]);

  const [exceptions] = useState([
    {
      id: '1',
      batchId: 'BATCH-001',
      type: 'Amount Mismatch',
      description: 'Payment amount differs between systems',
      severity: 'high',
      count: 15,
      lastOccurrence: '2025-10-20 11:30:00',
    },
    {
      id: '2',
      batchId: 'BATCH-001',
      type: 'Missing Transaction',
      description: 'Transaction found in source but not in target',
      severity: 'medium',
      count: 8,
      lastOccurrence: '2025-10-20 11:25:00',
    },
    {
      id: '3',
      batchId: 'BATCH-002',
      type: 'Duplicate Transaction',
      description: 'Same transaction processed multiple times',
      severity: 'low',
      count: 3,
      lastOccurrence: '2025-10-20 10:10:00',
    },
  ]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'running': return 'info';
      case 'completed': return 'success';
      case 'failed': return 'error';
      case 'paused': return 'warning';
      default: return 'default';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'high': return 'error';
      case 'medium': return 'warning';
      case 'low': return 'info';
      default: return 'default';
    }
  };

  const handleBatchAction = (batchId: string, action: string) => {
    console.log(`Action ${action} on batch ${batchId}`);
  };

  const handleViewDetails = (batch: ReconciliationBatch) => {
    setSelectedBatch(batch);
    setDetailsDialogOpen(true);
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Reconciliation & Monitoring
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Monitor reconciliation processes and exception handling
          </Typography>
        </Box>
        <Box display="flex" gap={2}>
          <Button
            variant="outlined"
            startIcon={<Download />}
          >
            Export Report
          </Button>
          <Button
            variant="contained"
            startIcon={<Refresh />}
            onClick={() => window.location.reload()}
          >
            Refresh
          </Button>
        </Box>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <CheckCircle color="success" sx={{ mr: 1 }} />
                <Typography variant="h6">
                  {reconciliationBatches.filter(b => b.status === 'completed').length}
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
                <PlayArrow color="info" sx={{ mr: 1 }} />
                <Typography variant="h6">
                  {reconciliationBatches.filter(b => b.status === 'running').length}
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Running Batches
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" mb={1}>
                <Error color="error" sx={{ mr: 1 }} />
                <Typography variant="h6">
                  {reconciliationBatches.filter(b => b.status === 'failed').length}
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
                <Warning color="warning" sx={{ mr: 1 }} />
                <Typography variant="h6">
                  {exceptions.reduce((sum, ex) => sum + ex.count, 0)}
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Total Exceptions
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={handleTabChange}>
            <Tab label="Reconciliation Batches" />
            <Tab label="Exception Monitoring" />
            <Tab label="Performance Metrics" />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={0}>
          <Typography variant="h6" gutterBottom>
            Reconciliation Batches
          </Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Batch ID</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Progress</TableCell>
                  <TableCell>Total Records</TableCell>
                  <TableCell>Matched</TableCell>
                  <TableCell>Unmatched</TableCell>
                  <TableCell>Errors</TableCell>
                  <TableCell>Start Time</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {reconciliationBatches.map((batch) => (
                  <TableRow key={batch.id}>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        {batch.id}
                      </Typography>
                    </TableCell>
                    <TableCell>{batch.name}</TableCell>
                    <TableCell>
                      <Chip 
                        label={batch.status.toUpperCase()} 
                        color={getStatusColor(batch.status) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Box display="flex" alignItems="center" gap={1}>
                        <LinearProgress 
                          variant="determinate" 
                          value={batch.progress} 
                          sx={{ width: 100 }}
                        />
                        <Typography variant="caption">
                          {batch.progress}%
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>{batch.totalRecords.toLocaleString()}</TableCell>
                    <TableCell>
                      <Typography color="success.main" fontWeight="bold">
                        {batch.matchedRecords.toLocaleString()}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography color="warning.main" fontWeight="bold">
                        {batch.unmatchedRecords.toLocaleString()}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography color="error.main" fontWeight="bold">
                        {batch.errorRecords.toLocaleString()}
                      </Typography>
                    </TableCell>
                    <TableCell>{batch.startTime}</TableCell>
                    <TableCell>
                      <Box display="flex" gap={1}>
                        {batch.status === 'running' && (
                          <IconButton
                            size="small"
                            onClick={() => handleBatchAction(batch.id, 'pause')}
                            color="warning"
                          >
                            <Pause />
                          </IconButton>
                        )}
                        {batch.status === 'paused' && (
                          <IconButton
                            size="small"
                            onClick={() => handleBatchAction(batch.id, 'resume')}
                            color="success"
                          >
                            <PlayArrow />
                          </IconButton>
                        )}
                        {batch.status === 'running' && (
                          <IconButton
                            size="small"
                            onClick={() => handleBatchAction(batch.id, 'stop')}
                            color="error"
                          >
                            <Stop />
                          </IconButton>
                        )}
                        <IconButton
                          size="small"
                          onClick={() => handleViewDetails(batch)}
                        >
                          <Visibility />
                        </IconButton>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <Typography variant="h6" gutterBottom>
            Exception Monitoring
          </Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Exception Type</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>Severity</TableCell>
                  <TableCell>Count</TableCell>
                  <TableCell>Last Occurrence</TableCell>
                  <TableCell>Batch ID</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {exceptions.map((exception) => (
                  <TableRow key={exception.id}>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        {exception.type}
                      </Typography>
                    </TableCell>
                    <TableCell>{exception.description}</TableCell>
                    <TableCell>
                      <Chip 
                        label={exception.severity.toUpperCase()} 
                        color={getSeverityColor(exception.severity) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Typography fontWeight="bold">
                        {exception.count}
                      </Typography>
                    </TableCell>
                    <TableCell>{exception.lastOccurrence}</TableCell>
                    <TableCell>{exception.batchId}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          <Typography variant="h6" gutterBottom>
            Performance Metrics
          </Typography>
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Processing Speed
                  </Typography>
                  <Typography variant="h4" color="primary">
                    1,250 records/min
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Average processing rate
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Match Rate
                  </Typography>
                  <Typography variant="h4" color="success.main">
                    98.5%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Average match rate
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Error Rate
                  </Typography>
                  <Typography variant="h4" color="error.main">
                    0.8%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Average error rate
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Average Processing Time
                  </Typography>
                  <Typography variant="h4" color="info.main">
                    12.5 min
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Per batch
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>
      </Card>

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
              <Grid item xs={12} md={6}>
                <TextField
                  label="Total Records"
                  value={selectedBatch.totalRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Processed Records"
                  value={selectedBatch.processedRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Matched Records"
                  value={selectedBatch.matchedRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Unmatched Records"
                  value={selectedBatch.unmatchedRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Error Records"
                  value={selectedBatch.errorRecords.toLocaleString()}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Progress"
                  value={`${selectedBatch.progress}%`}
                  fullWidth
                  disabled
                />
              </Grid>
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
