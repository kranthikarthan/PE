import React, { useState } from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
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
  DatePicker,
} from '@mui/material';
import {
  Search,
  Download,
  Visibility,
  FilterList,
  Refresh,
} from '@mui/icons-material';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

interface Transaction {
  id: string;
  amount: string;
  currency: string;
  status: 'completed' | 'pending' | 'failed' | 'processing';
  sourceAccount: string;
  destinationAccount: string;
  reference: string;
  createdAt: string;
  completedAt?: string;
  processingTime?: string;
}

const TransactionEnquiries: React.FC = () => {
  const [searchCriteria, setSearchCriteria] = useState({
    paymentId: '',
    reference: '',
    status: '',
    dateFrom: null,
    dateTo: null,
    amountFrom: '',
    amountTo: '',
  });
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);

  const [transactions] = useState<Transaction[]>([
    {
      id: 'TXN-001',
      amount: '1,250.00',
      currency: 'USD',
      status: 'completed',
      sourceAccount: '****1234',
      destinationAccount: '****5678',
      reference: 'INV-2025-001',
      createdAt: '2025-10-20 10:30:00',
      completedAt: '2025-10-20 10:32:15',
      processingTime: '2m 15s',
    },
    {
      id: 'TXN-002',
      amount: '5,000.00',
      currency: 'EUR',
      status: 'processing',
      sourceAccount: '****9876',
      destinationAccount: '****5432',
      reference: 'INV-2025-002',
      createdAt: '2025-10-20 09:15:00',
    },
    {
      id: 'TXN-003',
      amount: '750.50',
      currency: 'GBP',
      status: 'failed',
      sourceAccount: '****1111',
      destinationAccount: '****2222',
      reference: 'INV-2025-003',
      createdAt: '2025-10-20 08:45:00',
    },
    {
      id: 'TXN-004',
      amount: '2,100.00',
      currency: 'USD',
      status: 'pending',
      sourceAccount: '****3333',
      destinationAccount: '****4444',
      reference: 'INV-2025-004',
      createdAt: '2025-10-20 11:20:00',
    },
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

  const handleSearch = () => {
    // Implement search logic
    console.log('Search criteria:', searchCriteria);
  };

  const handleViewDetails = (transaction: Transaction) => {
    setSelectedTransaction(transaction);
    setDetailsDialogOpen(true);
  };

  const handleExport = () => {
    // Implement export logic
    console.log('Export transactions');
  };

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
          <Box display="flex" gap={2}>
            <Button
              variant="outlined"
              startIcon={<Download />}
              onClick={handleExport}
            >
              Export
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

        {/* Search Criteria */}
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Search Criteria
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} md={3}>
                <TextField
                  label="Payment ID"
                  value={searchCriteria.paymentId}
                  onChange={(e) => setSearchCriteria({...searchCriteria, paymentId: e.target.value})}
                  fullWidth
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <Search />
                      </InputAdornment>
                    ),
                  }}
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField
                  label="Reference"
                  value={searchCriteria.reference}
                  onChange={(e) => setSearchCriteria({...searchCriteria, reference: e.target.value})}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <FormControl fullWidth>
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={searchCriteria.status}
                    label="Status"
                    onChange={(e) => setSearchCriteria({...searchCriteria, status: e.target.value})}
                  >
                    <MenuItem value="">All</MenuItem>
                    <MenuItem value="completed">Completed</MenuItem>
                    <MenuItem value="processing">Processing</MenuItem>
                    <MenuItem value="failed">Failed</MenuItem>
                    <MenuItem value="pending">Pending</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} md={3}>
                <Box display="flex" gap={1}>
                  <Button
                    variant="contained"
                    onClick={handleSearch}
                    sx={{ flex: 1 }}
                  >
                    Search
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<FilterList />}
                  >
                    Filters
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        {/* Search Results */}
        <Card>
          <CardContent>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">
                Search Results ({transactions.length} transactions)
              </Typography>
              <Box display="flex" gap={1}>
                <Button
                  variant="outlined"
                  startIcon={<Download />}
                  onClick={handleExport}
                >
                  Export Results
                </Button>
              </Box>
            </Box>

            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Transaction ID</TableCell>
                    <TableCell>Amount</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Source Account</TableCell>
                    <TableCell>Destination Account</TableCell>
                    <TableCell>Reference</TableCell>
                    <TableCell>Created At</TableCell>
                    <TableCell>Processing Time</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {transactions.map((transaction) => (
                    <TableRow key={transaction.id}>
                      <TableCell>
                        <Typography variant="subtitle2" fontWeight="bold">
                          {transaction.id}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        {transaction.amount} {transaction.currency}
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={transaction.status.toUpperCase()} 
                          color={getStatusColor(transaction.status) as any}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{transaction.sourceAccount}</TableCell>
                      <TableCell>{transaction.destinationAccount}</TableCell>
                      <TableCell>{transaction.reference}</TableCell>
                      <TableCell>{transaction.createdAt}</TableCell>
                      <TableCell>
                        {transaction.processingTime || '-'}
                      </TableCell>
                      <TableCell>
                        <IconButton
                          size="small"
                          onClick={() => handleViewDetails(transaction)}
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

        {/* Transaction Details Dialog */}
        <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="md" fullWidth>
          <DialogTitle>
            Transaction Details - {selectedTransaction?.id}
          </DialogTitle>
          <DialogContent>
            {selectedTransaction && (
              <Grid container spacing={2} sx={{ mt: 1 }}>
                <Grid item xs={6}>
                  <TextField
                    label="Transaction ID"
                    value={selectedTransaction.id}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label="Status"
                    value={selectedTransaction.status}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label="Amount"
                    value={`${selectedTransaction.amount} ${selectedTransaction.currency}`}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label="Reference"
                    value={selectedTransaction.reference}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label="Source Account"
                    value={selectedTransaction.sourceAccount}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label="Destination Account"
                    value={selectedTransaction.destinationAccount}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label="Created At"
                    value={selectedTransaction.createdAt}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label="Completed At"
                    value={selectedTransaction.completedAt || '-'}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField
                    label="Processing Time"
                    value={selectedTransaction.processingTime || '-'}
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
    </LocalizationProvider>
  );
};

export default TransactionEnquiries;
