import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Alert,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Tooltip,
  Badge,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  LinearProgress,
  Avatar,
  Stack,
  FormControlLabel,
  Switch,
  Autocomplete,
} from '@mui/material';
import {
  Build as BuildIcon,
  Assignment as AssignmentIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Schedule as ScheduleIcon,
  Person as PersonIcon,
  Refresh as RefreshIcon,
  PlayArrow as PlayArrowIcon,
  Stop as StopIcon,
  Visibility as ViewIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Add as AddIcon,
  FilterList as FilterIcon,
  Search as SearchIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Timeline as TimelineIcon,
  Assessment as AssessmentIcon,
  Settings as SettingsIcon,
  Security as SecurityIcon,
  AccountBalance as AccountBalanceIcon,
  Payment as PaymentIcon,
  Receipt as ReceiptIcon,
  History as HistoryIcon,
  Notifications as NotificationsIcon,
  ExpandMore as ExpandMoreIcon,
  ArrowUpward as ArrowUpwardIcon,
  ArrowDownward as ArrowDownwardIcon,
  Pause as PauseIcon,
  PlayCircle as PlayCircleIcon,
  Cancel as CancelIcon,
  Done as DoneIcon,
  Flag as FlagIcon,
  PriorityHigh as PriorityHighIcon,
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import JSONEditor from 'react-json-editor-ajrm';
import locale from 'react-json-editor-ajrm/locale/en';

// Types
interface TransactionRepair {
  id: string;
  transactionReference: string;
  parentTransactionId?: string;
  tenantId: string;
  repairType: string;
  repairStatus: string;
  failureReason?: string;
  errorCode?: string;
  errorMessage?: string;
  fromAccountNumber?: string;
  toAccountNumber?: string;
  amount?: number;
  currency?: string;
  paymentType?: string;
  debitStatus?: string;
  creditStatus?: string;
  debitReference?: string;
  creditReference?: string;
  debitResponse?: any;
  creditResponse?: any;
  originalRequest?: any;
  retryCount: number;
  maxRetries: number;
  nextRetryAt?: string;
  timeoutAt?: string;
  priority: number;
  assignedTo?: string;
  correctiveAction?: string;
  correctiveActionDetails?: any;
  resolutionNotes?: string;
  resolvedBy?: string;
  resolvedAt?: string;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

interface CorrectiveAction {
  value: string;
  label: string;
  description: string;
  icon: React.ReactNode;
  color: 'primary' | 'secondary' | 'success' | 'warning' | 'error';
}

interface RepairStatistics {
  total: number;
  pending: number;
  assigned: number;
  inProgress: number;
  resolved: number;
  failed: number;
  cancelled: number;
  readyForRetry: number;
  timedOut: number;
  highPriority: number;
  needingReview: number;
}

// Validation schema
const correctiveActionSchema = yup.object({
  correctiveAction: yup.string().required('Corrective action is required'),
  correctiveActionDetails: yup.object(),
  resolutionNotes: yup.string().when('correctiveAction', {
    is: (val: string) => val === 'NO_ACTION' || val === 'CANCEL_TRANSACTION',
    then: (schema) => schema.required('Resolution notes are required'),
    otherwise: (schema) => schema.optional(),
  }),
});

const TransactionRepairManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [repairs, setRepairs] = useState<TransactionRepair[]>([]);
  const [statistics, setStatistics] = useState<RepairStatistics | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [selectedRepair, setSelectedRepair] = useState<TransactionRepair | null>(null);
  const [actionDialogOpen, setActionDialogOpen] = useState(false);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [filterStatus, setFilterStatus] = useState<string>('all');
  const [filterType, setFilterType] = useState<string>('all');
  const [filterPriority, setFilterPriority] = useState<string>('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<string>('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

  const {
    control: actionControl,
    handleSubmit: handleActionSubmit,
    reset: resetAction,
    watch: watchAction,
    formState: { errors: actionErrors },
  } = useForm({
    resolver: yupResolver(correctiveActionSchema),
    defaultValues: {
      correctiveAction: '',
      correctiveActionDetails: {},
      resolutionNotes: '',
    },
  });

  // Load data on component mount
  useEffect(() => {
    loadRepairs();
    loadStatistics();
  }, [filterStatus, filterType, filterPriority, searchTerm, sortBy, sortOrder]);

  const loadRepairs = async () => {
    try {
      setLoading(true);
      // Mock data for now
      const mockRepairs: TransactionRepair[] = [
        {
          id: '1',
          transactionReference: 'TXN-001-REPAIR',
          tenantId: 'tenant1',
          repairType: 'CREDIT_FAILED',
          repairStatus: 'PENDING',
          failureReason: 'Credit operation failed after successful debit',
          errorCode: 'CREDIT_TIMEOUT',
          errorMessage: 'Credit operation timed out after 60 seconds',
          fromAccountNumber: 'ACC-001',
          toAccountNumber: 'ACC-002',
          amount: 1000.00,
          currency: 'USD',
          paymentType: 'TRANSFER',
          debitStatus: 'SUCCESS',
          creditStatus: 'FAILED',
          retryCount: 0,
          maxRetries: 3,
          priority: 5,
          assignedTo: 'admin@example.com',
          correctiveAction: 'RETRY_CREDIT',
          createdAt: '2024-01-15T10:30:00Z',
          updatedAt: '2024-01-15T10:30:00Z',
        },
        {
          id: '2',
          transactionReference: 'TXN-002-REPAIR',
          tenantId: 'tenant1',
          repairType: 'DEBIT_FAILED',
          repairStatus: 'ASSIGNED',
          failureReason: 'Debit operation failed - insufficient funds',
          errorCode: 'INSUFFICIENT_FUNDS',
          errorMessage: 'Account balance is insufficient for the requested amount',
          fromAccountNumber: 'ACC-003',
          toAccountNumber: 'ACC-004',
          amount: 2500.00,
          currency: 'USD',
          paymentType: 'TRANSFER',
          debitStatus: 'FAILED',
          creditStatus: 'PENDING',
          retryCount: 1,
          maxRetries: 3,
          priority: 8,
          assignedTo: 'support@example.com',
          correctiveAction: 'MANUAL_CREDIT',
          createdAt: '2024-01-15T09:15:00Z',
          updatedAt: '2024-01-15T09:45:00Z',
        },
        {
          id: '3',
          transactionReference: 'TXN-003-REPAIR',
          tenantId: 'tenant2',
          repairType: 'DEBIT_TIMEOUT',
          repairStatus: 'PENDING',
          failureReason: 'Debit operation timed out',
          errorCode: 'DEBIT_TIMEOUT',
          errorMessage: 'Debit operation timed out after 60 seconds',
          fromAccountNumber: 'ACC-005',
          toAccountNumber: 'ACC-006',
          amount: 500.00,
          currency: 'EUR',
          paymentType: 'PAYMENT',
          debitStatus: 'TIMEOUT',
          creditStatus: 'PENDING',
          retryCount: 0,
          maxRetries: 3,
          priority: 3,
          correctiveAction: 'RETRY_DEBIT',
          nextRetryAt: '2024-01-15T11:00:00Z',
          createdAt: '2024-01-15T08:45:00Z',
          updatedAt: '2024-01-15T08:45:00Z',
        },
        {
          id: '4',
          transactionReference: 'TXN-004-REPAIR',
          tenantId: 'tenant1',
          repairType: 'MANUAL_REVIEW',
          repairStatus: 'IN_PROGRESS',
          failureReason: 'Transaction requires manual review due to unusual activity',
          errorCode: 'MANUAL_REVIEW',
          errorMessage: 'Transaction flagged for manual review',
          fromAccountNumber: 'ACC-007',
          toAccountNumber: 'ACC-008',
          amount: 10000.00,
          currency: 'USD',
          paymentType: 'TRANSFER',
          debitStatus: 'SUCCESS',
          creditStatus: 'SUCCESS',
          retryCount: 0,
          maxRetries: 3,
          priority: 10,
          assignedTo: 'compliance@example.com',
          correctiveAction: 'ESCALATE',
          createdAt: '2024-01-15T07:30:00Z',
          updatedAt: '2024-01-15T10:15:00Z',
        },
        {
          id: '5',
          transactionReference: 'TXN-005-REPAIR',
          tenantId: 'tenant2',
          repairType: 'SYSTEM_ERROR',
          repairStatus: 'RESOLVED',
          failureReason: 'System error during transaction processing',
          errorCode: 'SYSTEM_ERROR',
          errorMessage: 'Database connection timeout during transaction processing',
          fromAccountNumber: 'ACC-009',
          toAccountNumber: 'ACC-010',
          amount: 750.00,
          currency: 'GBP',
          paymentType: 'PAYMENT',
          debitStatus: 'PENDING',
          creditStatus: 'PENDING',
          retryCount: 2,
          maxRetries: 3,
          priority: 6,
          resolvedBy: 'admin@example.com',
          resolvedAt: '2024-01-15T06:00:00Z',
          resolutionNotes: 'System error resolved - transaction reprocessed successfully',
          correctiveAction: 'RETRY_BOTH',
          createdAt: '2024-01-15T05:30:00Z',
          updatedAt: '2024-01-15T06:00:00Z',
        },
      ];
      
      setRepairs(mockRepairs);
    } catch (err) {
      setError('Failed to load transaction repairs');
    } finally {
      setLoading(false);
    }
  };

  const loadStatistics = async () => {
    try {
      // Mock statistics
      setStatistics({
        total: 5,
        pending: 2,
        assigned: 1,
        inProgress: 1,
        resolved: 1,
        failed: 0,
        cancelled: 0,
        readyForRetry: 1,
        timedOut: 0,
        highPriority: 2,
        needingReview: 1,
      });
    } catch (err) {
      setError('Failed to load statistics');
    }
  };

  const handleApplyCorrectiveAction = async (data: any) => {
    try {
      setLoading(true);
      setError(null);

      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 2000));

      // Update repair status
      setRepairs(prev =>
        prev.map(repair =>
          repair.id === selectedRepair?.id
            ? {
                ...repair,
                repairStatus: 'IN_PROGRESS',
                correctiveAction: data.correctiveAction,
                correctiveActionDetails: data.correctiveActionDetails,
                resolutionNotes: data.resolutionNotes,
                updatedAt: new Date().toISOString(),
              }
            : repair
        )
      );

      setSuccess('Corrective action applied successfully');
      setActionDialogOpen(false);
      setSelectedRepair(null);
      resetAction();
      loadStatistics();
    } catch (err) {
      setError('Failed to apply corrective action');
    } finally {
      setLoading(false);
    }
  };

  const handleAssignRepair = async (repairId: string, assignedTo: string) => {
    try {
      setRepairs(prev =>
        prev.map(repair =>
          repair.id === repairId
            ? {
                ...repair,
                assignedTo,
                repairStatus: 'ASSIGNED',
                updatedAt: new Date().toISOString(),
              }
            : repair
        )
      );
      setSuccess('Repair assigned successfully');
    } catch (err) {
      setError('Failed to assign repair');
    }
  };

  const handleResolveRepair = async (repairId: string, resolutionNotes: string) => {
    try {
      setRepairs(prev =>
        prev.map(repair =>
          repair.id === repairId
            ? {
                ...repair,
                repairStatus: 'RESOLVED',
                resolvedBy: 'current-user@example.com',
                resolvedAt: new Date().toISOString(),
                resolutionNotes,
                updatedAt: new Date().toISOString(),
              }
            : repair
        )
      );
      setSuccess('Repair resolved successfully');
      loadStatistics();
    } catch (err) {
      setError('Failed to resolve repair');
    }
  };

  const getRepairTypeColor = (type: string) => {
    const colors: Record<string, 'primary' | 'secondary' | 'success' | 'warning' | 'error'> = {
      'DEBIT_FAILED': 'error',
      'CREDIT_FAILED': 'error',
      'DEBIT_TIMEOUT': 'warning',
      'CREDIT_TIMEOUT': 'warning',
      'MANUAL_REVIEW': 'secondary',
      'SYSTEM_ERROR': 'error',
      'PARTIAL_SUCCESS': 'warning',
    };
    return colors[type] || 'default';
  };

  const getRepairStatusColor = (status: string) => {
    const colors: Record<string, 'primary' | 'secondary' | 'success' | 'warning' | 'error'> = {
      'PENDING': 'warning',
      'ASSIGNED': 'primary',
      'IN_PROGRESS': 'secondary',
      'RESOLVED': 'success',
      'FAILED': 'error',
      'CANCELLED': 'default',
    };
    return colors[status] || 'default';
  };

  const getPriorityColor = (priority: number) => {
    if (priority >= 8) return 'error';
    if (priority >= 5) return 'warning';
    return 'success';
  };

  const getCorrectiveActions = (): CorrectiveAction[] => [
    {
      value: 'RETRY_DEBIT',
      label: 'Retry Debit',
      description: 'Retry the failed debit operation',
      icon: <RefreshIcon />,
      color: 'primary',
    },
    {
      value: 'RETRY_CREDIT',
      label: 'Retry Credit',
      description: 'Retry the failed credit operation',
      icon: <RefreshIcon />,
      color: 'primary',
    },
    {
      value: 'RETRY_BOTH',
      label: 'Retry Both',
      description: 'Retry both debit and credit operations',
      icon: <RefreshIcon />,
      color: 'primary',
    },
    {
      value: 'REVERSE_DEBIT',
      label: 'Reverse Debit',
      description: 'Reverse the successful debit operation',
      icon: <ArrowDownwardIcon />,
      color: 'warning',
    },
    {
      value: 'REVERSE_CREDIT',
      label: 'Reverse Credit',
      description: 'Reverse the successful credit operation',
      icon: <ArrowDownwardIcon />,
      color: 'warning',
    },
    {
      value: 'REVERSE_BOTH',
      label: 'Reverse Both',
      description: 'Reverse both debit and credit operations',
      icon: <ArrowDownwardIcon />,
      color: 'warning',
    },
    {
      value: 'MANUAL_CREDIT',
      label: 'Manual Credit',
      description: 'Process credit manually',
      icon: <PaymentIcon />,
      color: 'secondary',
    },
    {
      value: 'MANUAL_DEBIT',
      label: 'Manual Debit',
      description: 'Process debit manually',
      icon: <PaymentIcon />,
      color: 'secondary',
    },
    {
      value: 'MANUAL_BOTH',
      label: 'Manual Both',
      description: 'Process both operations manually',
      icon: <PaymentIcon />,
      color: 'secondary',
    },
    {
      value: 'CANCEL_TRANSACTION',
      label: 'Cancel Transaction',
      description: 'Cancel the entire transaction',
      icon: <CancelIcon />,
      color: 'error',
    },
    {
      value: 'ESCALATE',
      label: 'Escalate',
      description: 'Escalate for higher level review',
      icon: <FlagIcon />,
      color: 'error',
    },
    {
      value: 'NO_ACTION',
      label: 'No Action',
      description: 'No corrective action needed',
      icon: <DoneIcon />,
      color: 'success',
    },
  ];

  const filteredRepairs = repairs.filter(repair => {
    const matchesStatus = filterStatus === 'all' || repair.repairStatus === filterStatus;
    const matchesType = filterType === 'all' || repair.repairType === filterType;
    const matchesPriority = filterPriority === 'all' || 
      (filterPriority === 'high' && repair.priority >= 8) ||
      (filterPriority === 'medium' && repair.priority >= 5 && repair.priority < 8) ||
      (filterPriority === 'low' && repair.priority < 5);
    const matchesSearch = searchTerm === '' || 
      repair.transactionReference.toLowerCase().includes(searchTerm.toLowerCase()) ||
      repair.fromAccountNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      repair.toAccountNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      repair.errorMessage?.toLowerCase().includes(searchTerm.toLowerCase());
    
    return matchesStatus && matchesType && matchesPriority && matchesSearch;
  });

  const renderStatistics = () => (
    <Grid container spacing={3} sx={{ mb: 3 }}>
      <Grid item xs={12} sm={6} md={3}>
        <Card>
          <CardContent>
            <Box display="flex" alignItems="center">
              <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                <AssessmentIcon />
              </Avatar>
              <Box>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Total Repairs
                </Typography>
                <Typography variant="h4">
                  {statistics?.total || 0}
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Grid>
      
      <Grid item xs={12} sm={6} md={3}>
        <Card>
          <CardContent>
            <Box display="flex" alignItems="center">
              <Avatar sx={{ bgcolor: 'warning.main', mr: 2 }}>
                <ScheduleIcon />
              </Avatar>
              <Box>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Pending
                </Typography>
                <Typography variant="h4">
                  {statistics?.pending || 0}
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Grid>
      
      <Grid item xs={12} sm={6} md={3}>
        <Card>
          <CardContent>
            <Box display="flex" alignItems="center">
              <Avatar sx={{ bgcolor: 'error.main', mr: 2 }}>
                <PriorityHighIcon />
              </Avatar>
              <Box>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  High Priority
                </Typography>
                <Typography variant="h4">
                  {statistics?.highPriority || 0}
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Grid>
      
      <Grid item xs={12} sm={6} md={3}>
        <Card>
          <CardContent>
            <Box display="flex" alignItems="center">
              <Avatar sx={{ bgcolor: 'success.main', mr: 2 }}>
                <CheckCircleIcon />
              </Avatar>
              <Box>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Resolved
                </Typography>
                <Typography variant="h4">
                  {statistics?.resolved || 0}
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );

  const renderRepairsList = () => (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Transaction Repairs</Typography>
        <Box display="flex" gap={2}>
          <TextField
            size="small"
            placeholder="Search repairs..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
            }}
          />
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={loadRepairs}
          >
            Refresh
          </Button>
        </Box>
      </Box>

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={4}>
          <FormControl fullWidth size="small">
            <InputLabel>Status</InputLabel>
            <Select
              value={filterStatus}
              label="Status"
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <MenuItem value="all">All Statuses</MenuItem>
              <MenuItem value="PENDING">Pending</MenuItem>
              <MenuItem value="ASSIGNED">Assigned</MenuItem>
              <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
              <MenuItem value="RESOLVED">Resolved</MenuItem>
              <MenuItem value="FAILED">Failed</MenuItem>
              <MenuItem value="CANCELLED">Cancelled</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        
        <Grid item xs={12} sm={4}>
          <FormControl fullWidth size="small">
            <InputLabel>Type</InputLabel>
            <Select
              value={filterType}
              label="Type"
              onChange={(e) => setFilterType(e.target.value)}
            >
              <MenuItem value="all">All Types</MenuItem>
              <MenuItem value="DEBIT_FAILED">Debit Failed</MenuItem>
              <MenuItem value="CREDIT_FAILED">Credit Failed</MenuItem>
              <MenuItem value="DEBIT_TIMEOUT">Debit Timeout</MenuItem>
              <MenuItem value="CREDIT_TIMEOUT">Credit Timeout</MenuItem>
              <MenuItem value="MANUAL_REVIEW">Manual Review</MenuItem>
              <MenuItem value="SYSTEM_ERROR">System Error</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        
        <Grid item xs={12} sm={4}>
          <FormControl fullWidth size="small">
            <InputLabel>Priority</InputLabel>
            <Select
              value={filterPriority}
              label="Priority"
              onChange={(e) => setFilterPriority(e.target.value)}
            >
              <MenuItem value="all">All Priorities</MenuItem>
              <MenuItem value="high">High (8-10)</MenuItem>
              <MenuItem value="medium">Medium (5-7)</MenuItem>
              <MenuItem value="low">Low (1-4)</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      </Grid>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Transaction Reference</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Priority</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Assigned To</TableCell>
              <TableCell>Created</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredRepairs.map((repair) => (
              <TableRow key={repair.id}>
                <TableCell>
                  <Typography variant="body2" fontWeight="medium">
                    {repair.transactionReference}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {repair.fromAccountNumber} → {repair.toAccountNumber}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Chip
                    label={repair.repairType.replace('_', ' ')}
                    color={getRepairTypeColor(repair.repairType)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={repair.repairStatus.replace('_', ' ')}
                    color={getRepairStatusColor(repair.repairStatus)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={repair.priority}
                    color={getPriorityColor(repair.priority)}
                    size="small"
                    icon={repair.priority >= 8 ? <PriorityHighIcon /> : undefined}
                  />
                </TableCell>
                <TableCell>
                  <Typography variant="body2">
                    {repair.currency} {repair.amount?.toFixed(2)}
                  </Typography>
                </TableCell>
                <TableCell>
                  {repair.assignedTo ? (
                    <Box display="flex" alignItems="center">
                      <PersonIcon sx={{ mr: 1, fontSize: 16 }} />
                      <Typography variant="body2">
                        {repair.assignedTo}
                      </Typography>
                    </Box>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      Unassigned
                    </Typography>
                  )}
                </TableCell>
                <TableCell>
                  <Typography variant="body2">
                    {new Date(repair.createdAt).toLocaleDateString()}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(repair.createdAt).toLocaleTimeString()}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Box display="flex" gap={1}>
                    <Tooltip title="View Details">
                      <IconButton
                        size="small"
                        onClick={() => {
                          setSelectedRepair(repair);
                          setDetailsDialogOpen(true);
                        }}
                      >
                        <ViewIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Apply Corrective Action">
                      <IconButton
                        size="small"
                        onClick={() => {
                          setSelectedRepair(repair);
                          setActionDialogOpen(true);
                        }}
                        disabled={repair.repairStatus === 'RESOLVED'}
                      >
                        <BuildIcon />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Transaction Repair Management
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Manage and resolve failed debit/credit operations with corrective actions.
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

      {renderStatistics()}

      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={(e, newValue) => setActiveTab(newValue)}>
            <Tab label="Repairs" icon={<BuildIcon />} />
            <Tab label="Statistics" icon={<AssessmentIcon />} />
          </Tabs>
        </Box>

        <Box sx={{ p: 3 }}>
          {activeTab === 0 && renderRepairsList()}
          {activeTab === 1 && (
            <Box>
              <Typography variant="h6" gutterBottom>
                Detailed Statistics
              </Typography>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Status Breakdown
                      </Typography>
                      <List>
                        <ListItem>
                          <ListItemIcon>
                            <ScheduleIcon color="warning" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Pending"
                            secondary={`${statistics?.pending || 0} repairs`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <PersonIcon color="primary" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Assigned"
                            secondary={`${statistics?.assigned || 0} repairs`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <BuildIcon color="secondary" />
                          </ListItemIcon>
                          <ListItemText
                            primary="In Progress"
                            secondary={`${statistics?.inProgress || 0} repairs`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <CheckCircleIcon color="success" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Resolved"
                            secondary={`${statistics?.resolved || 0} repairs`}
                          />
                        </ListItem>
                      </List>
                    </CardContent>
                  </Card>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Priority & Alerts
                      </Typography>
                      <List>
                        <ListItem>
                          <ListItemIcon>
                            <PriorityHighIcon color="error" />
                          </ListItemIcon>
                          <ListItemText
                            primary="High Priority"
                            secondary={`${statistics?.highPriority || 0} repairs`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <RefreshIcon color="primary" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Ready for Retry"
                            secondary={`${statistics?.readyForRetry || 0} repairs`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <ScheduleIcon color="warning" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Timed Out"
                            secondary={`${statistics?.timedOut || 0} repairs`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <SecurityIcon color="secondary" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Needs Manual Review"
                            secondary={`${statistics?.needingReview || 0} repairs`}
                          />
                        </ListItem>
                      </List>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            </Box>
          )}
        </Box>
      </Card>

      {/* Corrective Action Dialog */}
      <Dialog open={actionDialogOpen} onClose={() => setActionDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Apply Corrective Action - {selectedRepair?.transactionReference}
        </DialogTitle>
        <form onSubmit={handleActionSubmit(handleApplyCorrectiveAction)}>
          <DialogContent>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Controller
                  name="correctiveAction"
                  control={actionControl}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!actionErrors.correctiveAction}>
                      <InputLabel>Corrective Action</InputLabel>
                      <Select {...field} label="Corrective Action">
                        {getCorrectiveActions().map((action) => (
                          <MenuItem key={action.value} value={action.value}>
                            <Box display="flex" alignItems="center">
                              {action.icon}
                              <Box sx={{ ml: 2 }}>
                                <Typography variant="body1">{action.label}</Typography>
                                <Typography variant="caption" color="text.secondary">
                                  {action.description}
                                </Typography>
                              </Box>
                            </Box>
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Action Details (JSON)
                </Typography>
                <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 1 }}>
                  <JSONEditor
                    id="correctiveActionDetails"
                    placeholder={{
                      "retryReason": "Retry with extended timeout",
                      "timeoutSeconds": 120,
                      "notes": "Additional notes for the action"
                    }}
                    locale={locale}
                    height="200px"
                    width="100%"
                  />
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Controller
                  name="resolutionNotes"
                  control={actionControl}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Resolution Notes"
                      fullWidth
                      multiline
                      rows={3}
                      error={!!actionErrors.resolutionNotes}
                      helperText={actionErrors.resolutionNotes?.message}
                    />
                  )}
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setActionDialogOpen(false)}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={loading}
            >
              Apply Action
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Details Dialog */}
      <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="lg" fullWidth>
        <DialogTitle>
          Transaction Repair Details - {selectedRepair?.transactionReference}
        </DialogTitle>
        <DialogContent>
          {selectedRepair && (
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Basic Information
                    </Typography>
                    <List>
                      <ListItem>
                        <ListItemText
                          primary="Transaction Reference"
                          secondary={selectedRepair.transactionReference}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Repair Type"
                          secondary={selectedRepair.repairType}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Status"
                          secondary={selectedRepair.repairStatus}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Priority"
                          secondary={selectedRepair.priority}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Amount"
                          secondary={`${selectedRepair.currency} ${selectedRepair.amount?.toFixed(2)}`}
                        />
                      </ListItem>
                    </List>
                  </CardContent>
                </Card>
              </Grid>
              
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Operation Status
                    </Typography>
                    <List>
                      <ListItem>
                        <ListItemText
                          primary="Debit Status"
                          secondary={selectedRepair.debitStatus || 'N/A'}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Credit Status"
                          secondary={selectedRepair.creditStatus || 'N/A'}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Retry Count"
                          secondary={`${selectedRepair.retryCount}/${selectedRepair.maxRetries}`}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Error Code"
                          secondary={selectedRepair.errorCode || 'N/A'}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Failure Reason"
                          secondary={selectedRepair.failureReason || 'N/A'}
                        />
                      </ListItem>
                    </List>
                  </CardContent>
                </Card>
              </Grid>
              
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Error Details
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {selectedRepair.errorMessage || 'No error message available'}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsDialogOpen(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TransactionRepairManagement;