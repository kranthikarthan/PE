/**
 * Payment Repair Page
 * 
 * Comprehensive payment repair and management interface.
 * Integrates with Payment Initiation Service for retry, cancel, and repair operations.
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
  Checkbox,
  FormControlLabel,
  Alert,
  Tabs,
  Tab,
  Grid2 as Grid,
  Skeleton,
  Tooltip,
  CircularProgress,
  LinearProgress,
} from '@mui/material';
import {
  Refresh,
  Build,
  Cancel,
  Visibility,
  MoreVert,
  Search,
  FilterList,
  Retry,
  Delete,
  Download,
  Warning,
  CheckCircle,
  Error,
  Info,
} from '@mui/icons-material';
import { useApi, usePagination } from '@hooks';
import { getPaymentInitiationService } from '@services';
import { Payment, PaymentStatus, PaymentRepairLog } from '@types/payment';
import { useNotification } from '@contexts';
import { usePermissions } from '@hooks/usePermissions';

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
      id={`payment-tabpanel-${index}`}
      aria-labelledby={`payment-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const PaymentRepair: React.FC = () => {
  const { showSuccess, showError } = useNotification();
  const { canManagePayments, canViewPayments } = usePermissions();
  const [activeTab, setActiveTab] = useState(0);
  const [selectedPayments, setSelectedPayments] = useState<string[]>([]);
  const [repairDialogOpen, setRepairDialogOpen] = useState(false);
  const [repairAction, setRepairAction] = useState<'RETRY' | 'CANCEL' | 'REFUND'>('RETRY');
  const [repairReason, setRepairReason] = useState('');
  const [forceRepair, setForceRepair] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<PaymentStatus | 'ALL'>('ALL');

  // API hooks for fetching data
  const failedPaymentsApi = useApi(
    () => getPaymentInitiationService().getFailedPayments({ page: 0, size: 20 }),
    { immediate: true, showNotifications: false }
  );

  const repairHistoryApi = useApi(
    () => getPaymentInitiationService().getPaymentRepairHistory(''),
    { immediate: false, showNotifications: false }
  );

  const bulkRetryApi = useApi(
    (paymentIds: string[], reason: string) => 
      getPaymentInitiationService().bulkRetryPayments(paymentIds, reason),
    { immediate: false, showNotifications: true }
  );

  const bulkCancelApi = useApi(
    (paymentIds: string[], reason: string) => 
      getPaymentInitiationService().bulkCancelPayments(paymentIds, reason),
    { immediate: false, showNotifications: true }
  );

  // Handle tab change
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  // Handle payment selection
  const handlePaymentSelect = (paymentId: string, selected: boolean) => {
    if (selected) {
      setSelectedPayments(prev => [...prev, paymentId]);
    } else {
      setSelectedPayments(prev => prev.filter(id => id !== paymentId);
    }
  };

  const handleSelectAll = (selected: boolean) => {
    if (selected) {
      setSelectedPayments(failedPaymentsApi.data?.content?.map(p => p.id) || []);
    } else {
      setSelectedPayments([]);
    }
  };

  // Handle repair action
  const handleRepairAction = (action: 'RETRY' | 'CANCEL' | 'REFUND') => {
    if (selectedPayments.length === 0) {
      showError('No Selection', 'Please select at least one payment');
      return;
    }

    if (!canManagePayments()) {
      showError('Access Denied', 'You do not have permission to repair payments');
      return;
    }

    setRepairAction(action);
    setRepairDialogOpen(true);
  };

  const confirmRepairAction = async () => {
    if (selectedPayments.length === 0) return;

    try {
      if (repairAction === 'RETRY') {
        await bulkRetryApi.execute(selectedPayments, repairReason);
        showSuccess('Bulk Retry', `${selectedPayments.length} payments queued for retry`);
      } else if (repairAction === 'CANCEL') {
        await bulkCancelApi.execute(selectedPayments, repairReason);
        showSuccess('Bulk Cancel', `${selectedPayments.length} payments cancelled`);
      }

      setRepairDialogOpen(false);
      setRepairReason('');
      setForceRepair(false);
      setSelectedPayments([]);
      
      // Refresh data
      failedPaymentsApi.execute();
    } catch (error: any) {
      showError('Repair Action Failed', error.message || 'Failed to perform repair action');
    }
  };

  // Handle individual payment repair
  const handleIndividualRepair = async (paymentId: string, action: 'RETRY' | 'CANCEL') => {
    if (!canManagePayments()) {
      showError('Access Denied', 'You do not have permission to repair payments');
      return;
    }

    try {
      const paymentService = getPaymentInitiationService();
      if (action === 'RETRY') {
        await paymentService.retryPayment(paymentId, 'Retry initiated by operations team');
        showSuccess('Payment Retry', 'Payment retry initiated successfully');
      } else {
        await paymentService.cancelPayment(paymentId, 'Cancelled by operations team');
        showSuccess('Payment Cancel', 'Payment cancelled successfully');
      }
      
      failedPaymentsApi.execute();
    } catch (error: any) {
      showError('Payment Repair Failed', error.message || 'Failed to repair payment');
    }
  };

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    // Implement search logic here
  };

  // Handle status filter
  const handleStatusFilter = (status: PaymentStatus | 'ALL') => {
    setStatusFilter(status);
    // Implement filter logic here
  };

  const getStatusColor = (status: PaymentStatus) => {
    switch (status) {
      case PaymentStatus.FAILED: return 'error';
      case PaymentStatus.TIMEOUT: return 'warning';
      case PaymentStatus.RETRY_INITIATED: return 'info';
      case PaymentStatus.COMPLETED: return 'success';
      case PaymentStatus.PENDING: return 'default';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: PaymentStatus) => {
    switch (status) {
      case PaymentStatus.FAILED: return <Error color="error" />;
      case PaymentStatus.TIMEOUT: return <Warning color="warning" />;
      case PaymentStatus.RETRY_INITIATED: return <Refresh color="info" />;
      case PaymentStatus.COMPLETED: return <CheckCircle color="success" />;
      case PaymentStatus.PENDING: return <Info color="info" />;
      default: return <Info color="disabled" />;
    }
  };

  const formatAmount = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  // Loading skeleton component
  const PaymentTableSkeleton = () => (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell padding="checkbox"><Skeleton variant="rectangular" width={20} height={20} /></TableCell>
            <TableCell><Skeleton variant="text" width="60%" /></TableCell>
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
              <TableCell padding="checkbox"><Skeleton variant="rectangular" width={20} height={20} /></TableCell>
              <TableCell><Skeleton variant="text" width="60%" /></TableCell>
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
            Payment Repair
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Manage and repair failed payment transactions
          </Typography>
        </Box>
        <Box display="flex" gap={1}>
          <Button
            variant="outlined"
            startIcon={<Refresh />}
            onClick={() => failedPaymentsApi.execute()}
            disabled={failedPaymentsApi.loading}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<Build />}
            onClick={() => handleRepairAction('RETRY')}
            disabled={selectedPayments.length === 0 || !canManagePayments()}
          >
            Bulk Retry ({selectedPayments.length})
          </Button>
          <Button
            variant="contained"
            color="error"
            startIcon={<Cancel />}
            onClick={() => handleRepairAction('CANCEL')}
            disabled={selectedPayments.length === 0 || !canManagePayments()}
          >
            Bulk Cancel ({selectedPayments.length})
          </Button>
        </Box>
      </Box>

      {/* Search and Filter Bar */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField
                fullWidth
                placeholder="Search payments by ID, reference, or account..."
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
                InputProps={{
                  startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />,
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <FormControl fullWidth>
                <InputLabel>Status Filter</InputLabel>
                <Select
                  value={statusFilter}
                  onChange={(e) => handleStatusFilter(e.target.value as PaymentStatus | 'ALL')}
                  label="Status Filter"
                >
                  <MenuItem value="ALL">All Statuses</MenuItem>
                  <MenuItem value={PaymentStatus.FAILED}>Failed</MenuItem>
                  <MenuItem value={PaymentStatus.TIMEOUT}>Timeout</MenuItem>
                  <MenuItem value={PaymentStatus.RETRY_INITIATED}>Retry Initiated</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, md: 5 }}>
              <Box display="flex" gap={1}>
                <Button
                  variant="outlined"
                  startIcon={<FilterList />}
                  onClick={() => {/* Implement advanced filters */}}
                >
                  Advanced Filters
                </Button>
                <Button
                  variant="outlined"
                  startIcon={<Download />}
                  onClick={() => {/* Implement export */}}
                >
                  Export
                </Button>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={handleTabChange} aria-label="payment repair tabs">
            <Tab label="Failed Payments" />
            <Tab label="Repair History" />
            <Tab label="Bulk Operations" />
          </Tabs>
        </Box>

        {/* Failed Payments Tab */}
        <TabPanel value={activeTab} index={0}>
          {failedPaymentsApi.loading ? (
            <PaymentTableSkeleton />
          ) : failedPaymentsApi.error ? (
            <Alert severity="error" sx={{ mb: 2 }}>
              Failed to load payments: {failedPaymentsApi.error}
            </Alert>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox
                        indeterminate={selectedPayments.length > 0 && selectedPayments.length < (failedPaymentsApi.data?.content?.length || 0)}
                        checked={selectedPayments.length === (failedPaymentsApi.data?.content?.length || 0) && selectedPayments.length > 0}
                        onChange={(e) => handleSelectAll(e.target.checked)}
                      />
                    </TableCell>
                    <TableCell>Payment ID</TableCell>
                    <TableCell>Amount</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Reference</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {failedPaymentsApi.data?.content?.map((payment: Payment) => (
                    <TableRow key={payment.id}>
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedPayments.includes(payment.id)}
                          onChange={(e) => handlePaymentSelect(payment.id, e.target.checked)}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">
                          {payment.id}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {formatAmount(payment.amount, payment.currency)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box display="flex" alignItems="center" gap={1}>
                          {getStatusIcon(payment.status)}
                          <Chip 
                            label={payment.status} 
                            size="small" 
                            color={getStatusColor(payment.status) as any}
                          />
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {payment.reference}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {formatDate(payment.createdAt)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box display="flex" gap={1}>
                          <Tooltip title="Retry Payment">
                            <IconButton
                              size="small"
                              onClick={() => handleIndividualRepair(payment.id, 'RETRY')}
                              disabled={!canManagePayments()}
                            >
                              <Retry />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Cancel Payment">
                            <IconButton
                              size="small"
                              onClick={() => handleIndividualRepair(payment.id, 'CANCEL')}
                              disabled={!canManagePayments()}
                            >
                              <Cancel />
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
        </TabPanel>

        {/* Repair History Tab */}
        <TabPanel value={activeTab} index={1}>
          <Typography variant="h6" gutterBottom>
            Repair History
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            View repair actions performed on payments
          </Typography>
          {/* Implement repair history table */}
          <Alert severity="info">
            Repair history feature coming soon
          </Alert>
        </TabPanel>

        {/* Bulk Operations Tab */}
        <TabPanel value={activeTab} index={2}>
          <Typography variant="h6" gutterBottom>
            Bulk Operations
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Perform bulk operations on selected payments
          </Typography>
          
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Selected Payments: {selectedPayments.length}
                  </Typography>
                  <Box display="flex" flexDirection="column" gap={2}>
                    <Button
                      variant="contained"
                      startIcon={<Retry />}
                      onClick={() => handleRepairAction('RETRY')}
                      disabled={selectedPayments.length === 0 || !canManagePayments()}
                      fullWidth
                    >
                      Bulk Retry Selected
                    </Button>
                    <Button
                      variant="contained"
                      color="error"
                      startIcon={<Cancel />}
                      onClick={() => handleRepairAction('CANCEL')}
                      disabled={selectedPayments.length === 0 || !canManagePayments()}
                      fullWidth
                    >
                      Bulk Cancel Selected
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Operation Status
                  </Typography>
                  {bulkRetryApi.loading || bulkCancelApi.loading ? (
                    <Box>
                      <LinearProgress sx={{ mb: 1 }} />
                      <Typography variant="body2" color="text.secondary">
                        Processing bulk operation...
                      </Typography>
                    </Box>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      No operations in progress
                    </Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>
      </Card>

      {/* Repair Action Dialog */}
      <Dialog open={repairDialogOpen} onClose={() => setRepairDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {repairAction} {selectedPayments.length} Payment{selectedPayments.length > 1 ? 's' : ''}
        </DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <Alert severity="warning">
              This action will {repairAction.toLowerCase()} {selectedPayments.length} payment{selectedPayments.length > 1 ? 's' : ''}. 
              This action cannot be undone.
            </Alert>
            <TextField
              label="Reason (Required)"
              value={repairReason}
              onChange={(e) => setRepairReason(e.target.value)}
              fullWidth
              multiline
              rows={3}
              placeholder="Enter reason for this action..."
              required
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={forceRepair}
                  onChange={(e) => setForceRepair(e.target.checked)}
                />
              }
              label="Force Action (Skip Safety Checks)"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRepairDialogOpen(false)}>
            Cancel
          </Button>
          <Button 
            onClick={confirmRepairAction} 
            variant="contained"
            color={repairAction === 'CANCEL' ? 'error' : 'primary'}
            disabled={!repairReason.trim()}
          >
            {repairAction} {selectedPayments.length} Payment{selectedPayments.length > 1 ? 's' : ''}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PaymentRepair;