import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  IconButton,
  Tooltip,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Button,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  Visibility as VisibilityIcon,
  Refresh as RefreshIcon,
  FilterList as FilterIcon,
  GetApp as DownloadIcon,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { format } from 'date-fns';

import iso20022ApiService from '../../services/iso20022Api';
import { Iso20022MessageStatus, Iso20022MessageType } from '../../types/iso20022';

interface Iso20022MessageListProps {
  accountId?: string;
  showFilters?: boolean;
  maxHeight?: number;
}

const Iso20022MessageList: React.FC<Iso20022MessageListProps> = ({
  accountId,
  showFilters = true,
  maxHeight = 600,
}) => {
  const [messages, setMessages] = useState<Iso20022MessageStatus[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [totalCount, setTotalCount] = useState(0);
  const [selectedMessage, setSelectedMessage] = useState<Iso20022MessageStatus | null>(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);

  // Filter states
  const [filters, setFilters] = useState({
    messageType: '',
    status: '',
    direction: '',
    fromDate: null as Date | null,
    toDate: null as Date | null,
    endToEndId: '',
    messageId: '',
  });

  useEffect(() => {
    loadMessages();
  }, [page, rowsPerPage, filters]);

  const loadMessages = async () => {
    setLoading(true);
    setError(null);

    try {
      // Mock data for now - in real implementation, this would call the API
      const mockMessages: Iso20022MessageStatus[] = [
        {
          messageId: 'MSG-20240115-001',
          messageType: 'pain001',
          direction: 'INBOUND',
          status: 'PROCESSED',
          endToEndId: 'E2E-20240115-001',
          correlationId: 'CORR-001',
          transactionReference: 'TXN-1705312200000-A1B2C3D4',
          amount: 1000.00,
          currencyCode: 'USD',
          createdAt: '2024-01-15T10:30:00.000Z',
          processedAt: '2024-01-15T10:30:15.000Z',
          processingTimeMs: 15000,
        },
        {
          messageId: 'CANCEL-20240115-001',
          messageType: 'camt055',
          direction: 'INBOUND',
          status: 'PROCESSED',
          endToEndId: 'E2E-20240115-001',
          correlationId: 'CORR-002',
          createdAt: '2024-01-15T11:00:00.000Z',
          processedAt: '2024-01-15T11:00:05.000Z',
          processingTimeMs: 5000,
        },
        {
          messageId: 'SCHEME-20240115-001',
          messageType: 'pacs008',
          direction: 'INBOUND',
          status: 'PROCESSED',
          endToEndId: 'E2E-SCHEME-001',
          transactionReference: 'TXN-SCHEME-001',
          amount: 2500.00,
          currencyCode: 'USD',
          createdAt: '2024-01-15T12:30:00.000Z',
          processedAt: '2024-01-15T12:30:10.000Z',
          processingTimeMs: 10000,
        },
        {
          messageId: 'STMT-20240115-001',
          messageType: 'camt053',
          direction: 'OUTBOUND',
          status: 'PROCESSED',
          correlationId: 'CORR-003',
          createdAt: '2024-01-15T23:59:00.000Z',
          processedAt: '2024-01-15T23:59:05.000Z',
          processingTimeMs: 5000,
        },
      ];

      // Apply filters
      let filteredMessages = mockMessages;
      
      if (filters.messageType) {
        filteredMessages = filteredMessages.filter(m => m.messageType === filters.messageType);
      }
      if (filters.status) {
        filteredMessages = filteredMessages.filter(m => m.status === filters.status);
      }
      if (filters.direction) {
        filteredMessages = filteredMessages.filter(m => m.direction === filters.direction);
      }
      if (filters.endToEndId) {
        filteredMessages = filteredMessages.filter(m => 
          m.endToEndId?.toLowerCase().includes(filters.endToEndId.toLowerCase())
        );
      }
      if (filters.messageId) {
        filteredMessages = filteredMessages.filter(m => 
          m.messageId.toLowerCase().includes(filters.messageId.toLowerCase())
        );
      }

      setMessages(filteredMessages.slice(page * rowsPerPage, (page + 1) * rowsPerPage));
      setTotalCount(filteredMessages.length);

    } catch (err: any) {
      setError(err.message || 'Failed to load ISO 20022 messages');
    } finally {
      setLoading(false);
    }
  };

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleViewDetails = (message: Iso20022MessageStatus) => {
    setSelectedMessage(message);
    setDetailDialogOpen(true);
  };

  const getStatusColor = (status: string): 'success' | 'warning' | 'error' | 'info' | 'default' => {
    switch (status) {
      case 'PROCESSED': return 'success';
      case 'PROCESSING': return 'warning';
      case 'FAILED': case 'REJECTED': return 'error';
      case 'VALIDATED': return 'info';
      default: return 'default';
    }
  };

  const getMessageTypeColor = (type: string): 'primary' | 'secondary' | 'warning' | 'info' => {
    if (type.startsWith('pain')) return 'primary';
    if (type.startsWith('pacs')) return 'secondary';
    if (type.startsWith('camt')) return 'info';
    return 'primary';
  };

  const formatProcessingTime = (timeMs?: number): string => {
    if (!timeMs) return 'N/A';
    if (timeMs < 1000) return `${timeMs}ms`;
    if (timeMs < 60000) return `${(timeMs / 1000).toFixed(1)}s`;
    return `${(timeMs / 60000).toFixed(1)}m`;
  };

  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h5" component="h2">
            ISO 20022 Messages
          </Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={loadMessages}
              disabled={loading}
            >
              Refresh
            </Button>
            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              onClick={() => {/* Export functionality */}}
            >
              Export
            </Button>
          </Box>
        </Box>

        {showFilters && (
          <Paper sx={{ p: 2, mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              <FilterIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Filters
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6} md={3}>
                <FormControl fullWidth size="small">
                  <InputLabel>Message Type</InputLabel>
                  <Select
                    value={filters.messageType}
                    label="Message Type"
                    onChange={(e) => setFilters({ ...filters, messageType: e.target.value })}
                  >
                    <MenuItem value="">All</MenuItem>
                    <MenuItem value="pain001">pain.001 - Payment Initiation</MenuItem>
                    <MenuItem value="pain007">pain.007 - Payment Reversal</MenuItem>
                    <MenuItem value="pacs008">pacs.008 - Scheme Payment</MenuItem>
                    <MenuItem value="camt053">camt.053 - Account Statement</MenuItem>
                    <MenuItem value="camt055">camt.055 - Payment Cancellation</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <FormControl fullWidth size="small">
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={filters.status}
                    label="Status"
                    onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                  >
                    <MenuItem value="">All</MenuItem>
                    <MenuItem value="RECEIVED">Received</MenuItem>
                    <MenuItem value="VALIDATED">Validated</MenuItem>
                    <MenuItem value="PROCESSING">Processing</MenuItem>
                    <MenuItem value="PROCESSED">Processed</MenuItem>
                    <MenuItem value="FAILED">Failed</MenuItem>
                    <MenuItem value="REJECTED">Rejected</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <FormControl fullWidth size="small">
                  <InputLabel>Direction</InputLabel>
                  <Select
                    value={filters.direction}
                    label="Direction"
                    onChange={(e) => setFilters({ ...filters, direction: e.target.value })}
                  >
                    <MenuItem value="">All</MenuItem>
                    <MenuItem value="INBOUND">Inbound</MenuItem>
                    <MenuItem value="OUTBOUND">Outbound</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <TextField
                  label="End-to-End ID"
                  size="small"
                  fullWidth
                  value={filters.endToEndId}
                  onChange={(e) => setFilters({ ...filters, endToEndId: e.target.value })}
                  placeholder="E2E-..."
                />
              </Grid>
            </Grid>
          </Paper>
        )}

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <TableContainer sx={{ maxHeight }}>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell>Message ID</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Direction</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>End-to-End ID</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Created</TableCell>
                <TableCell>Processing Time</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={9} align="center">
                    <CircularProgress />
                  </TableCell>
                </TableRow>
              ) : messages.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} align="center">
                    No ISO 20022 messages found
                  </TableCell>
                </TableRow>
              ) : (
                messages.map((message) => (
                  <TableRow key={message.messageId} hover>
                    <TableCell>
                      <Typography variant="body2" fontFamily="monospace">
                        {message.messageId}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={message.messageType}
                        color={getMessageTypeColor(message.messageType)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={message.direction}
                        variant="outlined"
                        size="small"
                        color={message.direction === 'INBOUND' ? 'primary' : 'secondary'}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={message.status}
                        color={getStatusColor(message.status)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontFamily="monospace">
                        {message.endToEndId || 'N/A'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {message.amount && message.currencyCode ? (
                        <Typography variant="body2">
                          {message.currencyCode} {message.amount.toLocaleString()}
                        </Typography>
                      ) : (
                        'N/A'
                      )}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {format(new Date(message.createdAt), 'MMM dd, HH:mm')}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {formatProcessingTime(message.processingTimeMs)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Tooltip title="View Details">
                        <IconButton
                          size="small"
                          onClick={() => handleViewDetails(message)}
                        >
                          <VisibilityIcon />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <TablePagination
          component="div"
          count={totalCount}
          page={page}
          onPageChange={handleChangePage}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          rowsPerPageOptions={[10, 25, 50, 100]}
        />

        {/* Message Detail Dialog */}
        <Dialog
          open={detailDialogOpen}
          onClose={() => setDetailDialogOpen(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>
            ISO 20022 Message Details
            {selectedMessage && (
              <Chip
                label={selectedMessage.messageType}
                color={getMessageTypeColor(selectedMessage.messageType)}
                size="small"
                sx={{ ml: 2 }}
              />
            )}
          </DialogTitle>
          <DialogContent>
            {selectedMessage && (
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" gutterBottom>Message ID</Typography>
                  <Typography variant="body2" fontFamily="monospace" gutterBottom>
                    {selectedMessage.messageId}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" gutterBottom>Status</Typography>
                  <Chip
                    label={selectedMessage.status}
                    color={getStatusColor(selectedMessage.status)}
                    size="small"
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" gutterBottom>End-to-End ID</Typography>
                  <Typography variant="body2" fontFamily="monospace">
                    {selectedMessage.endToEndId || 'N/A'}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" gutterBottom>Transaction Reference</Typography>
                  <Typography variant="body2" fontFamily="monospace">
                    {selectedMessage.transactionReference || 'N/A'}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" gutterBottom>Amount</Typography>
                  <Typography variant="body2">
                    {selectedMessage.amount && selectedMessage.currencyCode
                      ? `${selectedMessage.currencyCode} ${selectedMessage.amount.toLocaleString()}`
                      : 'N/A'
                    }
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" gutterBottom>Processing Time</Typography>
                  <Typography variant="body2">
                    {formatProcessingTime(selectedMessage.processingTimeMs)}
                  </Typography>
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="subtitle2" gutterBottom>Created At</Typography>
                  <Typography variant="body2">
                    {format(new Date(selectedMessage.createdAt), 'PPpp')}
                  </Typography>
                </Grid>
                {selectedMessage.processedAt && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" gutterBottom>Processed At</Typography>
                    <Typography variant="body2">
                      {format(new Date(selectedMessage.processedAt), 'PPpp')}
                    </Typography>
                  </Grid>
                )}
                {selectedMessage.errorCode && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" gutterBottom>Error</Typography>
                    <Alert severity="error">
                      <Typography variant="body2">
                        <strong>{selectedMessage.errorCode}</strong>: {selectedMessage.errorMessage}
                      </Typography>
                    </Alert>
                  </Grid>
                )}
              </Grid>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDetailDialogOpen(false)}>Close</Button>
          </DialogActions>
        </Dialog>
      </CardContent>
    </Card>
  );
};

export default Iso20022MessageList;