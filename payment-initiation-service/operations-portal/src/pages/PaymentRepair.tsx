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
  Checkbox,
  FormControlLabel,
  Alert,
  Tabs,
  Tab,
  Grid,
} from '@mui/material';
import {
  Refresh,
  Build,
  Cancel,
  Visibility,
  MoreVert,
  Search,
  FilterList,
} from '@mui/icons-material';

interface Payment {
  id: string;
  amount: string;
  currency: string;
  status: 'failed' | 'timeout' | 'error' | 'retry_initiated';
  sourceAccount: string;
  destinationAccount: string;
  reference: string;
  createdAt: string;
  lastUpdated: string;
  failureReason?: string;
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
      id={`payment-tabpanel-${index}`}
      aria-labelledby={`payment-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const PaymentRepair: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [selectedPayments, setSelectedPayments] = useState<string[]>([]);
  const [repairDialogOpen, setRepairDialogOpen] = useState(false);
  const [selectedPayment, setSelectedPayment] = useState<Payment | null>(null);
  const [repairReason, setRepairReason] = useState('');
  const [forceRetry, setForceRetry] = useState(false);

  const [failedPayments] = useState<Payment[]>([
    {
      id: 'PAY-001',
      amount: '1,250.00',
      currency: 'USD',
      status: 'failed',
      sourceAccount: '****1234',
      destinationAccount: '****5678',
      reference: 'INV-2025-001',
      createdAt: '2025-10-20 10:30:00',
      lastUpdated: '2025-10-20 10:35:00',
      failureReason: 'Insufficient funds',
    },
    {
      id: 'PAY-002',
      amount: '5,000.00',
      currency: 'EUR',
      status: 'timeout',
      sourceAccount: '****9876',
      destinationAccount: '****5432',
      reference: 'INV-2025-002',
      createdAt: '2025-10-20 09:15:00',
      lastUpdated: '2025-10-20 09:20:00',
      failureReason: 'Network timeout',
    },
    {
      id: 'PAY-003',
      amount: '750.50',
      currency: 'GBP',
      status: 'error',
      sourceAccount: '****1111',
      destinationAccount: '****2222',
      reference: 'INV-2025-003',
      createdAt: '2025-10-20 08:45:00',
      lastUpdated: '2025-10-20 08:50:00',
      failureReason: 'Invalid account number',
    },
  ]);

  const [repairHistory] = useState([
    {
      id: '1',
      paymentId: 'PAY-001',
      action: 'RETRY',
      performedBy: 'ops-user-1',
      reason: 'System error resolved',
      result: 'SUCCESS',
      timestamp: '2025-10-20 11:00:00',
    },
    {
      id: '2',
      paymentId: 'PAY-002',
      action: 'CANCEL',
      performedBy: 'ops-user-2',
      reason: 'Customer requested cancellation',
      result: 'SUCCESS',
      timestamp: '2025-10-20 10:45:00',
    },
  ]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'failed': return 'error';
      case 'timeout': return 'warning';
      case 'error': return 'error';
      case 'retry_initiated': return 'info';
      default: return 'default';
    }
  };

  const handleSelectPayment = (paymentId: string) => {
    setSelectedPayments(prev => 
      prev.includes(paymentId) 
        ? prev.filter(id => id !== paymentId)
        : [...prev, paymentId]
    );
  };

  const handleSelectAll = () => {
    setSelectedPayments(
      selectedPayments.length === failedPayments.length 
        ? [] 
        : failedPayments.map(p => p.id)
    );
  };

  const handleRepairPayment = (payment: Payment) => {
    setSelectedPayment(payment);
    setRepairDialogOpen(true);
  };

  const handleBulkRetry = () => {
    // Implement bulk retry logic
    console.log('Bulk retry for payments:', selectedPayments);
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

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
        <Box display="flex" gap={2}>
          <Button
            variant="outlined"
            startIcon={<Search />}
          >
            Search
          </Button>
          <Button
            variant="outlined"
            startIcon={<FilterList />}
          >
            Filter
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

      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={handleTabChange}>
            <Tab label="Failed Payments" />
            <Tab label="Repair History" />
            <Tab label="Bulk Operations" />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={0}>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="h6">
              Failed Payments ({failedPayments.length})
            </Typography>
            <Box display="flex" gap={1}>
              <Button
                variant="outlined"
                startIcon={<Refresh />}
                disabled={selectedPayments.length === 0}
                onClick={handleBulkRetry}
              >
                Bulk Retry ({selectedPayments.length})
              </Button>
            </Box>
          </Box>

          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell padding="checkbox">
                    <Checkbox
                      checked={selectedPayments.length === failedPayments.length}
                      indeterminate={selectedPayments.length > 0 && selectedPayments.length < failedPayments.length}
                      onChange={handleSelectAll}
                    />
                  </TableCell>
                  <TableCell>Payment ID</TableCell>
                  <TableCell>Amount</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Source Account</TableCell>
                  <TableCell>Destination Account</TableCell>
                  <TableCell>Reference</TableCell>
                  <TableCell>Created At</TableCell>
                  <TableCell>Failure Reason</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {failedPayments.map((payment) => (
                  <TableRow key={payment.id}>
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={selectedPayments.includes(payment.id)}
                        onChange={() => handleSelectPayment(payment.id)}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight="bold">
                        {payment.id}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {payment.amount} {payment.currency}
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={payment.status.toUpperCase().replace('_', ' ')} 
                        color={getStatusColor(payment.status) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{payment.sourceAccount}</TableCell>
                    <TableCell>{payment.destinationAccount}</TableCell>
                    <TableCell>{payment.reference}</TableCell>
                    <TableCell>{payment.createdAt}</TableCell>
                    <TableCell>
                      <Typography variant="caption" color="error">
                        {payment.failureReason}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Box display="flex" gap={1}>
                        <IconButton
                          size="small"
                          onClick={() => handleRepairPayment(payment)}
                          color="primary"
                        >
                          <Build />
                        </IconButton>
                        <IconButton
                          size="small"
                          onClick={() => handleRepairPayment(payment)}
                          color="secondary"
                        >
                          <Cancel />
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
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <Typography variant="h6" gutterBottom>
            Repair History
          </Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Payment ID</TableCell>
                  <TableCell>Action</TableCell>
                  <TableCell>Performed By</TableCell>
                  <TableCell>Reason</TableCell>
                  <TableCell>Result</TableCell>
                  <TableCell>Timestamp</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {repairHistory.map((history) => (
                  <TableRow key={history.id}>
                    <TableCell>{history.paymentId}</TableCell>
                    <TableCell>
                      <Chip 
                        label={history.action} 
                        color="primary" 
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{history.performedBy}</TableCell>
                    <TableCell>{history.reason}</TableCell>
                    <TableCell>
                      <Chip 
                        label={history.result} 
                        color={history.result === 'SUCCESS' ? 'success' : 'error'} 
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{history.timestamp}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          <Typography variant="h6" gutterBottom>
            Bulk Operations
          </Typography>
          <Alert severity="info" sx={{ mb: 2 }}>
            Select multiple payments from the Failed Payments tab to perform bulk operations.
          </Alert>
          <Box display="flex" gap={2}>
            <Button
              variant="contained"
              startIcon={<Refresh />}
              disabled={selectedPayments.length === 0}
            >
              Bulk Retry ({selectedPayments.length} selected)
            </Button>
            <Button
              variant="outlined"
              startIcon={<Cancel />}
              disabled={selectedPayments.length === 0}
            >
              Bulk Cancel ({selectedPayments.length} selected)
            </Button>
          </Box>
        </TabPanel>
      </Card>

      {/* Repair Dialog */}
      <Dialog open={repairDialogOpen} onClose={() => setRepairDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          Repair Payment - {selectedPayment?.id}
        </DialogTitle>
        <DialogContent>
          {selectedPayment && (
            <Box sx={{ mt: 2 }}>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <TextField
                    label="Payment ID"
                    value={selectedPayment.id}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <TextField
                    label="Amount"
                    value={`${selectedPayment.amount} ${selectedPayment.currency}`}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <TextField
                    label="Status"
                    value={selectedPayment.status}
                    fullWidth
                    disabled
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    label="Repair Reason"
                    value={repairReason}
                    onChange={(e) => setRepairReason(e.target.value)}
                    fullWidth
                    multiline
                    rows={3}
                    placeholder="Enter reason for repair action..."
                  />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={forceRetry}
                        onChange={(e) => setForceRetry(e.target.checked)}
                      />
                    }
                    label="Force retry (ignore validation checks)"
                  />
                </Grid>
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRepairDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => setRepairDialogOpen(false)}>
            Execute Repair
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PaymentRepair;
