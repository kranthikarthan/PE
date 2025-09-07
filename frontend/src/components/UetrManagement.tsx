import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  Alert,
  CircularProgress,
  Tabs,
  Tab,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Tooltip,
  Divider
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Visibility as ViewIcon,
  Timeline as TimelineIcon,
  Assessment as StatsIcon,
  Generate as GenerateIcon,
  CheckCircle as ValidIcon,
  Error as InvalidIcon
} from '@mui/icons-material';
import axios from 'axios';

interface UetrTrackingRecord {
  id: string;
  uetr: string;
  messageType: string;
  tenantId: string;
  transactionReference: string;
  direction: string;
  status: string;
  statusReason?: string;
  processingSystem?: string;
  createdAt: string;
  updatedAt: string;
}

interface UetrJourney {
  originalUetr: string;
  timestamp: string;
  systemId: string;
  messageType: string;
  journeySteps: UetrTrackingRecord[];
}

interface UetrStatistics {
  tenantId: string;
  dateFrom?: string;
  dateTo?: string;
  totalUetrs: number;
  completedUetrs: number;
  failedUetrs: number;
  pendingUetrs: number;
  averageProcessingTimeMs: number;
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
      id={`uetr-tabpanel-${index}`}
      aria-labelledby={`uetr-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

const UetrManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Search state
  const [searchUetr, setSearchUetr] = useState('');
  const [searchResults, setSearchResults] = useState<UetrTrackingRecord[]>([]);
  const [selectedUetr, setSelectedUetr] = useState<UetrTrackingRecord | null>(null);
  const [uetrJourney, setUetrJourney] = useState<UetrJourney | null>(null);

  // Generate state
  const [generateMessageType, setGenerateMessageType] = useState('PAIN001');
  const [generateTenantId, setGenerateTenantId] = useState('');
  const [generatedUetr, setGeneratedUetr] = useState<string | null>(null);

  // Statistics state
  const [statistics, setStatistics] = useState<UetrStatistics | null>(null);
  const [statsTenantId, setStatsTenantId] = useState('');
  const [statsDateFrom, setStatsDateFrom] = useState('');
  const [statsDateTo, setStatsDateTo] = useState('');

  // Validation state
  const [validateUetr, setValidateUetr] = useState('');
  const [validationResult, setValidationResult] = useState<any>(null);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  const handleSearch = async () => {
    if (!searchUetr.trim()) {
      setError('Please enter a UETR to search');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await axios.get(`/api/v1/uetr/track/${searchUetr}`);
      if (response.data.found) {
        setSelectedUetr(response.data.trackingRecord);
        setSearchResults([response.data.trackingRecord]);
      } else {
        setError('UETR not found');
        setSelectedUetr(null);
        setSearchResults([]);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to search UETR');
      setSelectedUetr(null);
      setSearchResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleViewJourney = async (uetr: string) => {
    setLoading(true);
    setError(null);

    try {
      const response = await axios.get(`/api/v1/uetr/journey/${uetr}`);
      setUetrJourney(response.data.journey);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to retrieve UETR journey');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateUetr = async () => {
    if (!generateMessageType || !generateTenantId.trim()) {
      setError('Please select message type and enter tenant ID');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await axios.post('/api/v1/uetr/generate', null, {
        params: {
          messageType: generateMessageType,
          tenantId: generateTenantId
        }
      });
      setGeneratedUetr(response.data.uetr);
      setSuccess('UETR generated successfully');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to generate UETR');
    } finally {
      setLoading(false);
    }
  };

  const handleValidateUetr = async () => {
    if (!validateUetr.trim()) {
      setError('Please enter a UETR to validate');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await axios.get(`/api/v1/uetr/validate/${validateUetr}`);
      setValidationResult(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to validate UETR');
    } finally {
      setLoading(false);
    }
  };

  const handleGetStatistics = async () => {
    setLoading(true);
    setError(null);

    try {
      const params: any = {};
      if (statsTenantId.trim()) params.tenantId = statsTenantId;
      if (statsDateFrom.trim()) params.dateFrom = statsDateFrom;
      if (statsDateTo.trim()) params.dateTo = statsDateTo;

      const response = await axios.get('/api/v1/uetr/statistics', { params });
      setStatistics(response.data.statistics);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to retrieve statistics');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED': return 'success';
      case 'FAILED': return 'error';
      case 'REJECTED': return 'error';
      case 'PENDING': return 'warning';
      case 'PROCESSING': return 'info';
      case 'MANUAL_REVIEW': return 'warning';
      default: return 'default';
    }
  };

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString();
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        UETR Management
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Manage Unique End-to-End Transaction References for payment tracking and reconciliation
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={handleTabChange} aria-label="UETR management tabs">
            <Tab label="Search & Track" icon={<SearchIcon />} />
            <Tab label="Generate" icon={<GenerateIcon />} />
            <Tab label="Validate" icon={<ValidIcon />} />
            <Tab label="Statistics" icon={<StatsIcon />} />
          </Tabs>
        </Box>

        <TabPanel value={activeTab} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Search UETR
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                    <TextField
                      fullWidth
                      label="UETR"
                      value={searchUetr}
                      onChange={(e) => setSearchUetr(e.target.value)}
                      placeholder="Enter UETR to search"
                      variant="outlined"
                    />
                    <Button
                      variant="contained"
                      onClick={handleSearch}
                      disabled={loading}
                      startIcon={loading ? <CircularProgress size={20} /> : <SearchIcon />}
                    >
                      Search
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={6}>
              {selectedUetr && (
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      UETR Details
                    </Typography>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        UETR: {selectedUetr.uetr}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Status: <Chip label={selectedUetr.status} color={getStatusColor(selectedUetr.status) as any} size="small" />
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Message Type: {selectedUetr.messageType}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Tenant: {selectedUetr.tenantId}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Transaction: {selectedUetr.transactionReference}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Direction: {selectedUetr.direction}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Created: {formatDateTime(selectedUetr.createdAt)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Updated: {formatDateTime(selectedUetr.updatedAt)}
                      </Typography>
                    </Box>
                    <Button
                      variant="outlined"
                      startIcon={<TimelineIcon />}
                      onClick={() => handleViewJourney(selectedUetr.uetr)}
                      disabled={loading}
                    >
                      View Journey
                    </Button>
                  </CardContent>
                </Card>
              )}
            </Grid>

            {uetrJourney && (
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      UETR Journey: {uetrJourney.originalUetr}
                    </Typography>
                    <TableContainer component={Paper}>
                      <Table>
                        <TableHead>
                          <TableRow>
                            <TableCell>Step</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>System</TableCell>
                            <TableCell>Message Type</TableCell>
                            <TableCell>Timestamp</TableCell>
                            <TableCell>Reason</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {uetrJourney.journeySteps.map((step, index) => (
                            <TableRow key={step.id}>
                              <TableCell>{index + 1}</TableCell>
                              <TableCell>
                                <Chip label={step.status} color={getStatusColor(step.status) as any} size="small" />
                              </TableCell>
                              <TableCell>{step.processingSystem}</TableCell>
                              <TableCell>{step.messageType}</TableCell>
                              <TableCell>{formatDateTime(step.updatedAt)}</TableCell>
                              <TableCell>{step.statusReason}</TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={1}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Generate New UETR
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel>Message Type</InputLabel>
                    <Select
                      value={generateMessageType}
                      onChange={(e) => setGenerateMessageType(e.target.value)}
                      label="Message Type"
                    >
                      <MenuItem value="PAIN001">PAIN.001</MenuItem>
                      <MenuItem value="PACS008">PACS.008</MenuItem>
                      <MenuItem value="PACS002">PACS.002</MenuItem>
                      <MenuItem value="PAIN002">PAIN.002</MenuItem>
                      <MenuItem value="CAMT054">CAMT.054</MenuItem>
                      <MenuItem value="CAMT055">CAMT.055</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Tenant ID"
                    value={generateTenantId}
                    onChange={(e) => setGenerateTenantId(e.target.value)}
                    placeholder="Enter tenant ID"
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    onClick={handleGenerateUetr}
                    disabled={loading}
                    startIcon={loading ? <CircularProgress size={20} /> : <GenerateIcon />}
                  >
                    Generate UETR
                  </Button>
                </Grid>
                {generatedUetr && (
                  <Grid item xs={12}>
                    <Alert severity="success">
                      <Typography variant="h6">Generated UETR:</Typography>
                      <Typography variant="body1" sx={{ fontFamily: 'monospace', mt: 1 }}>
                        {generatedUetr}
                      </Typography>
                    </Alert>
                  </Grid>
                )}
              </Grid>
            </CardContent>
          </Card>
        </TabPanel>

        <TabPanel value={activeTab} index={2}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Validate UETR Format
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                <TextField
                  fullWidth
                  label="UETR"
                  value={validateUetr}
                  onChange={(e) => setValidateUetr(e.target.value)}
                  placeholder="Enter UETR to validate"
                  variant="outlined"
                />
                <Button
                  variant="contained"
                  onClick={handleValidateUetr}
                  disabled={loading}
                  startIcon={loading ? <CircularProgress size={20} /> : <ValidIcon />}
                >
                  Validate
                </Button>
              </Box>
              {validationResult && (
                <Alert severity={validationResult.isValid ? "success" : "error"}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {validationResult.isValid ? <ValidIcon /> : <InvalidIcon />}
                    <Typography variant="body1">
                      UETR is {validationResult.isValid ? 'valid' : 'invalid'}
                    </Typography>
                  </Box>
                  {validationResult.isValid && (
                    <Box sx={{ mt: 2 }}>
                      <Typography variant="body2">
                        Timestamp: {validationResult.timestamp}
                      </Typography>
                      <Typography variant="body2">
                        System ID: {validationResult.systemId}
                      </Typography>
                      <Typography variant="body2">
                        Message Type: {validationResult.messageTypeId}
                      </Typography>
                    </Box>
                  )}
                </Alert>
              )}
            </CardContent>
          </Card>
        </TabPanel>

        <TabPanel value={activeTab} index={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                UETR Statistics
              </Typography>
              <Grid container spacing={2} sx={{ mb: 2 }}>
                <Grid item xs={12} md={4}>
                  <TextField
                    fullWidth
                    label="Tenant ID (Optional)"
                    value={statsTenantId}
                    onChange={(e) => setStatsTenantId(e.target.value)}
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12} md={4}>
                  <TextField
                    fullWidth
                    label="Date From (Optional)"
                    type="datetime-local"
                    value={statsDateFrom}
                    onChange={(e) => setStatsDateFrom(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12} md={4}>
                  <TextField
                    fullWidth
                    label="Date To (Optional)"
                    type="datetime-local"
                    value={statsDateTo}
                    onChange={(e) => setStatsDateTo(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                    variant="outlined"
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    onClick={handleGetStatistics}
                    disabled={loading}
                    startIcon={loading ? <CircularProgress size={20} /> : <StatsIcon />}
                  >
                    Get Statistics
                  </Button>
                </Grid>
              </Grid>
              {statistics && (
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6} md={3}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h4" color="primary">
                          {statistics.totalUetrs}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Total UETRs
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h4" color="success.main">
                          {statistics.completedUetrs}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Completed
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h4" color="error.main">
                          {statistics.failedUetrs}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Failed
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h4" color="warning.main">
                          {statistics.pendingUetrs}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Pending
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          Average Processing Time
                        </Typography>
                        <Typography variant="h4" color="info.main">
                          {statistics.averageProcessingTimeMs.toFixed(2)} ms
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              )}
            </CardContent>
          </Card>
        </TabPanel>
      </Card>
    </Box>
  );
};

export default UetrManagement;