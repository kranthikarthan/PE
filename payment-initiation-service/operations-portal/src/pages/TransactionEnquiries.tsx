/**
 * Transaction Enquiries Page
 * 
 * Advanced transaction search and analysis interface.
 * Integrates with Transaction Processing Service for comprehensive transaction queries.
 */

import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  InputAdornment,
  Grid2 as Grid,
  Skeleton,
  Alert,
  Tooltip,
  Pagination,
  CircularProgress,
  LinearProgress,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import {
  Search,
  Download,
  Visibility,
  FilterList,
  Refresh,
  TrendingUp,
  TrendingDown,
  CheckCircle,
  Error,
  Warning,
  Info,
} from '@mui/icons-material';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { useApi, usePagination, useSearchApi } from '@hooks';
import { getTransactionProcessingService } from '@services';
import { Transaction, TransactionStatus, TransactionType, TransactionSearchCriteria } from '@types/transaction';
import { useNotification } from '@contexts';
import { usePermissions } from '@hooks/usePermissions';

const TransactionEnquiries: React.FC = () => {
  const { showSuccess, showError } = useNotification();
  const { canViewTransactions, canSearchTransactions, canExport } = usePermissions();
  const [searchCriteria, setSearchCriteria] = useState<TransactionSearchCriteria>({});
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [exportDialogOpen, setExportDialogOpen] = useState(false);
  const [exportFormat, setExportFormat] = useState<'CSV' | 'EXCEL' | 'PDF'>('CSV');
  const [dateFrom, setDateFrom] = useState<Date | null>(null);
  const [dateTo, setDateTo] = useState<Date | null>(null);

  // Pagination hook
  const pagination = usePagination({
    initialPage: 0,
    initialPageSize: 20,
    totalItems: 0,
  });

  // Search API hook
  const searchApi = useSearchApi(
    (query: string) => getTransactionProcessingService().searchTransactions(
      { query, ...searchCriteria },
      { page: pagination.currentPage, size: pagination.size }
    ),
    {
      debounceMs: 500,
      minQueryLength: 2,
    }
  );

  // Transaction metrics API
  const metricsApi = useApi(
    () => getTransactionProcessingService().getTransactionMetrics(),
    { immediate: true, showNotifications: false }
  );

  // Export API
  const exportApi = useApi(
    (format: string) => getTransactionProcessingService().exportTransactions({
      format: format as 'CSV' | 'EXCEL' | 'PDF',
      criteria: searchCriteria,
      includeMetadata: true,
      includeRelatedTransactions: false,
    }),
    { immediate: false, showNotifications: true }
  );

  // Handle search
  const handleSearch = () => {
    const criteria: TransactionSearchCriteria = {
      ...searchCriteria,
      dateFrom: dateFrom?.toISOString(),
      dateTo: dateTo?.toISOString(),
    };
    
    setSearchCriteria(criteria);
    searchApi.search(JSON.stringify(criteria));
  };

  // Handle export
  const handleExport = async () => {
    if (!canExport()) {
      showError('Access Denied', 'You do not have permission to export transactions');
      return;
    }

    try {
      await exportApi.execute(exportFormat);
      showSuccess('Export Started', 'Transaction export has been initiated. You will be notified when ready.');
      setExportDialogOpen(false);
    } catch (error: any) {
      showError('Export Failed', error.message || 'Failed to export transactions');
    }
  };

  // Handle transaction details
  const handleViewDetails = async (transactionId: string) => {
    try {
      const transaction = await getTransactionProcessingService().getTransactionById(transactionId);
      setSelectedTransaction(transaction);
      setDetailsDialogOpen(true);
    } catch (error: any) {
      showError('Load Failed', error.message || 'Failed to load transaction details');
    }
  };

  // Clear search
  const handleClearSearch = () => {
    setSearchCriteria({});
    setDateFrom(null);
    setDateTo(null);
    searchApi.setQuery('');
  };

  const getStatusColor = (status: TransactionStatus) => {
    switch (status) {
      case TransactionStatus.COMPLETED: return 'success';
      case TransactionStatus.FAILED: return 'error';
      case TransactionStatus.PENDING: return 'warning';
      case TransactionStatus.PROCESSING: return 'info';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: TransactionStatus) => {
    switch (status) {
      case TransactionStatus.COMPLETED: return <CheckCircle color="success" />;
      case TransactionStatus.FAILED: return <Error color="error" />;
      case TransactionStatus.PENDING: return <Warning color="warning" />;
      case TransactionStatus.PROCESSING: return <Info color="info" />;
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

  const formatProcessingTime = (time?: number) => {
    if (!time) return 'N/A';
    if (time < 1000) return `${time}ms`;
    return `${(time / 1000).toFixed(2)}s`;
  };

  // Loading skeleton component
  const TransactionTableSkeleton = () => (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell><Skeleton variant="text" width="60%" /></TableCell>
            <TableCell><Skeleton variant="text" width="40%" /></TableCell>
            <TableCell><Skeleton variant="text" width="30%" /></TableCell>
            <TableCell><Skeleton variant="text" width="50%" /></TableCell>
            <TableCell><Skeleton variant="text" width="40%" /></TableCell>
            <TableCell><Skeleton variant="text" width="30%" /></TableCell>
            <TableCell><Skeleton variant="rectangular" width={100} height={32} /></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {[1, 2, 3, 4, 5].map((index) => (
            <TableRow key={index}>
              <TableCell><Skeleton variant="text" width="60%" /></TableCell>
              <TableCell><Skeleton variant="text" width="40%" /></TableCell>
              <TableCell><Skeleton variant="rectangular" width={80} height={24} /></TableCell>
              <TableCell><Skeleton variant="text" width="50%" /></TableCell>
              <TableCell><Skeleton variant="text" width="40%" /></TableCell>
              <TableCell><Skeleton variant="text" width="30%" /></TableCell>
              <TableCell><Skeleton variant="rectangular" width={100} height={32} /></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Box>
            <Typography variant="h4" gutterBottom>
              Transaction Enquiries
            </Typography>
            <Typography variant="subtitle1" color="text.secondary">
              Search and analyze payment transactions
            </Typography>
          </Box>
          <Box display="flex" gap={1}>
            <Button
              variant="outlined"
              startIcon={<Refresh />}
              onClick={() => {
                searchApi.execute();
                metricsApi.execute();
              }}
              disabled={searchApi.loading}
            >
              Refresh
            </Button>
            <Button
              variant="contained"
              startIcon={<Download />}
              onClick={() => setExportDialogOpen(true)}
              disabled={!canExport()}
            >
              Export
            </Button>
          </Box>
        </Box>

        {/* Search and Filter Section */}
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Search & Filter
            </Typography>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 3 }}>
                <TextField
                  fullWidth
                  label="Transaction ID"
                  value={searchCriteria.transactionId || ''}
                  onChange={(e) => setSearchCriteria(prev => ({ ...prev, transactionId: e.target.value }))}
                  InputProps={{
                    startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />,
                  }}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 3 }}>
                <TextField
                  fullWidth
                  label="Reference"
                  value={searchCriteria.reference || ''}
                  onChange={(e) => setSearchCriteria(prev => ({ ...prev, reference: e.target.value }))}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 3 }}>
                <FormControl fullWidth>
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={searchCriteria.status || ''}
                    onChange={(e) => setSearchCriteria(prev => ({ ...prev, status: e.target.value as TransactionStatus }))}
                    label="Status"
                  >
                    <MenuItem value="">All Statuses</MenuItem>
                    <MenuItem value={TransactionStatus.COMPLETED}>Completed</MenuItem>
                    <MenuItem value={TransactionStatus.PENDING}>Pending</MenuItem>
                    <MenuItem value={TransactionStatus.FAILED}>Failed</MenuItem>
                    <MenuItem value={TransactionStatus.PROCESSING}>Processing</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 12, md: 3 }}>
                <FormControl fullWidth>
                  <InputLabel>Type</InputLabel>
                  <Select
                    value={searchCriteria.type || ''}
                    onChange={(e) => setSearchCriteria(prev => ({ ...prev, type: e.target.value as TransactionType }))}
                    label="Type"
                  >
                    <MenuItem value="">All Types</MenuItem>
                    <MenuItem value={TransactionType.PAYMENT}>Payment</MenuItem>
                    <MenuItem value={TransactionType.REFUND}>Refund</MenuItem>
                    <MenuItem value={TransactionType.REVERSAL}>Reversal</MenuItem>
                    <MenuItem value={TransactionType.ADJUSTMENT}>Adjustment</MenuItem>
                    <MenuItem value={TransactionType.FEE}>Fee</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 12, md: 3 }}>
                <DatePicker
                  label="From Date"
                  value={dateFrom}
                  onChange={(newValue) => setDateFrom(newValue)}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 3 }}>
                <DatePicker
                  label="To Date"
                  value={dateTo}
                  onChange={(newValue) => setDateTo(newValue)}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 3 }}>
                <TextField
                  fullWidth
                  label="Amount From"
                  type="number"
                  value={searchCriteria.amountFrom || ''}
                  onChange={(e) => setSearchCriteria(prev => ({ ...prev, amountFrom: parseFloat(e.target.value) }))}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 3 }}>
                <TextField
                  fullWidth
                  label="Amount To"
                  type="number"
                  value={searchCriteria.amountTo || ''}
                  onChange={(e) => setSearchCriteria(prev => ({ ...prev, amountTo: parseFloat(e.target.value) }))}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 12 }}>
                <Box display="flex" gap={1}>
                  <Button
                    variant="contained"
                    startIcon={<Search />}
                    onClick={handleSearch}
                    disabled={searchApi.loading}
                  >
                    Search
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<FilterList />}
                    onClick={() => {/* Implement advanced filters */}}
                  >
                    Advanced Filters
                  </Button>
                  <Button
                    variant="outlined"
                    onClick={handleClearSearch}
                  >
                    Clear
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        {/* Metrics Cards */}
        {metricsApi.data && (
          <Grid container spacing={2} sx={{ mb: 3 }}>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <TrendingUp color="success" />
                    <Typography variant="h6" color="success.main">
                      {metricsApi.data.totalTransactions.toLocaleString()}
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    Total Transactions
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
                      {metricsApi.data.successRate.toFixed(1)}%
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
                    <TrendingDown color="error" />
                    <Typography variant="h6" color="error.main">
                      {metricsApi.data.failedTransactions.toLocaleString()}
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    Failed Transactions
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <Info color="info" />
                    <Typography variant="h6" color="info.main">
                      {formatProcessingTime(metricsApi.data.averageProcessingTime)}
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    Avg Processing Time
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        )}

        {/* Transactions Table */}
        <Card>
          <CardContent>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">
                Transaction Results
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {searchApi.data?.length || 0} transactions found
              </Typography>
            </Box>

            {searchApi.loading ? (
              <TransactionTableSkeleton />
            ) : searchApi.error ? (
              <Alert severity="error" sx={{ mb: 2 }}>
                Failed to load transactions: {searchApi.error}
              </Alert>
            ) : searchApi.data?.length === 0 ? (
              <Alert severity="info">
                No transactions found matching your criteria
              </Alert>
            ) : (
              <>
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Transaction ID</TableCell>
                        <TableCell>Amount</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Reference</TableCell>
                        <TableCell>Created</TableCell>
                        <TableCell>Processing Time</TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {searchApi.data?.map((transaction: Transaction) => (
                        <TableRow key={transaction.id}>
                          <TableCell>
                            <Typography variant="body2" fontWeight="medium">
                              {transaction.id}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {formatAmount(transaction.amount, transaction.currency)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Box display="flex" alignItems="center" gap={1}>
                              {getStatusIcon(transaction.status)}
                              <Chip 
                                label={transaction.status} 
                                size="small" 
                                color={getStatusColor(transaction.status) as any}
                              />
                            </Box>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {transaction.reference}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {formatDate(transaction.createdAt)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {formatProcessingTime(transaction.processingTime)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Tooltip title="View Details">
                              <IconButton
                                size="small"
                                onClick={() => handleViewDetails(transaction.id)}
                              >
                                <Visibility />
                              </IconButton>
                            </Tooltip>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>

                {/* Pagination */}
                {pagination.totalPages > 1 && (
                  <Box display="flex" justifyContent="center" mt={2}>
                    <Pagination
                      count={pagination.totalPages}
                      page={pagination.currentPage + 1}
                      onChange={(event, page) => pagination.goToPage(page - 1)}
                      color="primary"
                    />
                  </Box>
                )}
              </>
            )}
          </CardContent>
        </Card>

        {/* Transaction Details Dialog */}
        <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="md" fullWidth>
          <DialogTitle>
            Transaction Details - {selectedTransaction?.id}
          </DialogTitle>
          <DialogContent>
            {selectedTransaction && (
              <Grid container spacing={2} sx={{ mt: 1 }}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="Transaction ID"
                    value={selectedTransaction.id}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="Type"
                    value={selectedTransaction.type}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="Amount"
                    value={formatAmount(selectedTransaction.amount, selectedTransaction.currency)}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="Status"
                    value={selectedTransaction.status}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="Source Account"
                    value={selectedTransaction.sourceAccount}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="Destination Account"
                    value={selectedTransaction.destinationAccount}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="Reference"
                    value={selectedTransaction.reference}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    label="Created At"
                    value={formatDate(selectedTransaction.createdAt)}
                    fullWidth
                    disabled
                  />
                </Grid>
                {selectedTransaction.completedAt && (
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      label="Completed At"
                      value={formatDate(selectedTransaction.completedAt)}
                      fullWidth
                      disabled
                    />
                  </Grid>
                )}
                {selectedTransaction.processingTime && (
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      label="Processing Time"
                      value={formatProcessingTime(selectedTransaction.processingTime)}
                      fullWidth
                      disabled
                    />
                  </Grid>
                )}
                {selectedTransaction.failureReason && (
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      label="Failure Reason"
                      value={selectedTransaction.failureReason}
                      fullWidth
                      multiline
                      rows={3}
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
          </DialogActions>
        </Dialog>

        {/* Export Dialog */}
        <Dialog open={exportDialogOpen} onClose={() => setExportDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>
            Export Transactions
          </DialogTitle>
          <DialogContent>
            <Box display="flex" flexDirection="column" gap={2} mt={1}>
              <FormControl fullWidth>
                <InputLabel>Export Format</InputLabel>
                <Select
                  value={exportFormat}
                  onChange={(e) => setExportFormat(e.target.value as 'CSV' | 'EXCEL' | 'PDF')}
                  label="Export Format"
                >
                  <MenuItem value="CSV">CSV</MenuItem>
                  <MenuItem value="EXCEL">Excel</MenuItem>
                  <MenuItem value="PDF">PDF</MenuItem>
                </Select>
              </FormControl>
              <Alert severity="info">
                Export will include all transactions matching your current search criteria.
              </Alert>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setExportDialogOpen(false)}>
              Cancel
            </Button>
            <Button 
              onClick={handleExport} 
              variant="contained"
              disabled={exportApi.loading}
            >
              {exportApi.loading ? <CircularProgress size={20} /> : 'Export'}
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
};

export default TransactionEnquiries;