import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Alert,
  CircularProgress,
  Chip,
} from '@mui/material';
import {
  Send as SendIcon,
  AccountBalance as AccountIcon,
  Payment as PaymentIcon,
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import transactionService from '../../services/transactionService';
import { CreateTransactionRequest, Account, PaymentType } from '../../types/transaction';
import ModernButton from '../common/ModernButton';
import ModernCard from '../common/ModernCard';
import StatusChip from '../common/StatusChip';

interface TransactionFormProps {
  onTransactionCreated?: (transaction: any) => void;
  onError?: (error: string) => void;
}

const validationSchema = yup.object({
  fromAccountId: yup.string().optional(),
  toAccountId: yup.string().optional(),
  paymentTypeId: yup.string().required('Payment type is required'),
  amount: yup
    .number()
    .positive('Amount must be positive')
    .required('Amount is required')
    .test('min-amount', 'Amount must be at least 0.01', (value) => value >= 0.01),
  currencyCode: yup.string().required('Currency is required'),
  description: yup.string().optional(),
  externalReference: yup.string().optional(),
});

const TransactionForm: React.FC<TransactionFormProps> = ({
  onTransactionCreated,
  onError,
}) => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [paymentTypes, setPaymentTypes] = useState<PaymentType[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const {
    control,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm<CreateTransactionRequest>({
    resolver: yupResolver(validationSchema),
    defaultValues: {
      currencyCode: 'USD',
      amount: 0,
    },
  });

  const watchedFromAccount = watch('fromAccountId');
  const watchedToAccount = watch('toAccountId');

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      const [accountsData, paymentTypesData] = await Promise.all([
        transactionService.getAccounts(),
        transactionService.getPaymentTypes(),
      ]);
      setAccounts(accountsData);
      setPaymentTypes(paymentTypesData.filter(pt => pt.isActive));
    } catch (err: any) {
      setError(`Failed to load data: ${err.message}`);
    }
  };

  const onSubmit = async (data: CreateTransactionRequest) => {
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // Validate that at least one account is selected
      if (!data.fromAccountId && !data.toAccountId) {
        throw new Error('At least one account (from or to) is required');
      }

      const transaction = await transactionService.createTransaction(data);
      
      setSuccess(`Transaction created successfully: ${transaction.transactionReference}`);
      reset();
      
      if (onTransactionCreated) {
        onTransactionCreated(transaction);
      }
    } catch (err: any) {
      const errorMessage = err.message || 'Failed to create transaction';
      setError(errorMessage);
      
      if (onError) {
        onError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const getAccountDisplayName = (account: Account) => {
    return `${account.accountNumber} - ${account.currencyCode} (${account.balance.toFixed(2)})`;
  };

  const getPaymentTypeDisplayName = (paymentType: PaymentType) => {
    return `${paymentType.name} (${paymentType.code})`;
  };

  return (
    <ModernCard
      title="Create Transaction"
      subtitle="Create a new payment transaction"
      icon={<PaymentIcon />}
    >
      <CardContent>
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

        <Box component="form" onSubmit={handleSubmit(onSubmit)}>
          <Grid container spacing={3}>
            {/* From Account */}
            <Grid item xs={12} md={6}>
              <FormControl fullWidth error={!!errors.fromAccountId}>
                <InputLabel>From Account (Optional)</InputLabel>
                <Controller
                  name="fromAccountId"
                  control={control}
                  render={({ field }) => (
                    <Select
                      {...field}
                      label="From Account (Optional)"
                      value={field.value || ''}
                    >
                      <MenuItem value="">
                        <em>Select from account (optional)</em>
                      </MenuItem>
                      {accounts.map((account) => (
                        <MenuItem key={account.id} value={account.id}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <AccountIcon fontSize="small" />
                            <Box>
                              <Typography variant="body2">
                                {getAccountDisplayName(account)}
                              </Typography>
                              <StatusChip 
                                status={account.status === 'ACTIVE' ? 'active' : 'inactive'} 
                                label={account.status} 
                                size="small" 
                              />
                            </Box>
                          </Box>
                        </MenuItem>
                      ))}
                    </Select>
                  )}
                />
                {errors.fromAccountId && (
                  <Typography variant="caption" color="error">
                    {errors.fromAccountId.message}
                  </Typography>
                )}
              </FormControl>
            </Grid>

            {/* To Account */}
            <Grid item xs={12} md={6}>
              <FormControl fullWidth error={!!errors.toAccountId}>
                <InputLabel>To Account (Optional)</InputLabel>
                <Controller
                  name="toAccountId"
                  control={control}
                  render={({ field }) => (
                    <Select
                      {...field}
                      label="To Account (Optional)"
                      value={field.value || ''}
                    >
                      <MenuItem value="">
                        <em>Select to account (optional)</em>
                      </MenuItem>
                      {accounts.map((account) => (
                        <MenuItem key={account.id} value={account.id}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <AccountIcon fontSize="small" />
                            <Box>
                              <Typography variant="body2">
                                {getAccountDisplayName(account)}
                              </Typography>
                              <StatusChip 
                                status={account.status === 'ACTIVE' ? 'active' : 'inactive'} 
                                label={account.status} 
                                size="small" 
                              />
                            </Box>
                          </Box>
                        </MenuItem>
                      ))}
                    </Select>
                  )}
                />
                {errors.toAccountId && (
                  <Typography variant="caption" color="error">
                    {errors.toAccountId.message}
                  </Typography>
                )}
              </FormControl>
            </Grid>

            {/* Payment Type */}
            <Grid item xs={12} md={6}>
              <FormControl fullWidth error={!!errors.paymentTypeId}>
                <InputLabel>Payment Type *</InputLabel>
                <Controller
                  name="paymentTypeId"
                  control={control}
                  render={({ field }) => (
                    <Select
                      {...field}
                      label="Payment Type *"
                      value={field.value || ''}
                    >
                      {paymentTypes.map((paymentType) => (
                        <MenuItem key={paymentType.id} value={paymentType.id}>
                          <Box>
                            <Typography variant="body2">
                              {getPaymentTypeDisplayName(paymentType)}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {paymentType.description}
                            </Typography>
                            <Box sx={{ mt: 0.5 }}>
                              <Chip
                                label={`${paymentType.minAmount} - ${paymentType.maxAmount}`}
                                size="small"
                                variant="outlined"
                              />
                              <Chip
                                label={paymentType.isSynchronous ? 'Sync' : 'Async'}
                                size="small"
                                variant="outlined"
                                sx={{ ml: 0.5 }}
                              />
                            </Box>
                          </Box>
                        </MenuItem>
                      ))}
                    </Select>
                  )}
                />
                {errors.paymentTypeId && (
                  <Typography variant="caption" color="error">
                    {errors.paymentTypeId.message}
                  </Typography>
                )}
              </FormControl>
            </Grid>

            {/* Amount */}
            <Grid item xs={12} md={6}>
              <Controller
                name="amount"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Amount *"
                    type="number"
                    fullWidth
                    error={!!errors.amount}
                    helperText={errors.amount?.message}
                    inputProps={{ step: 0.01, min: 0.01 }}
                  />
                )}
              />
            </Grid>

            {/* Currency */}
            <Grid item xs={12} md={6}>
              <FormControl fullWidth error={!!errors.currencyCode}>
                <InputLabel>Currency *</InputLabel>
                <Controller
                  name="currencyCode"
                  control={control}
                  render={({ field }) => (
                    <Select
                      {...field}
                      label="Currency *"
                      value={field.value || 'USD'}
                    >
                      <MenuItem value="USD">USD - US Dollar</MenuItem>
                      <MenuItem value="EUR">EUR - Euro</MenuItem>
                      <MenuItem value="GBP">GBP - British Pound</MenuItem>
                      <MenuItem value="JPY">JPY - Japanese Yen</MenuItem>
                      <MenuItem value="CAD">CAD - Canadian Dollar</MenuItem>
                    </Select>
                  )}
                />
                {errors.currencyCode && (
                  <Typography variant="caption" color="error">
                    {errors.currencyCode.message}
                  </Typography>
                )}
              </FormControl>
            </Grid>

            {/* Description */}
            <Grid item xs={12} md={6}>
              <Controller
                name="description"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Description"
                    fullWidth
                    multiline
                    rows={2}
                    error={!!errors.description}
                    helperText={errors.description?.message}
                  />
                )}
              />
            </Grid>

            {/* External Reference */}
            <Grid item xs={12}>
              <Controller
                name="externalReference"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="External Reference"
                    fullWidth
                    error={!!errors.externalReference}
                    helperText={errors.externalReference?.message}
                  />
                )}
              />
            </Grid>

            {/* Submit Button */}
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                <ModernButton
                  variant="outline"
                  onClick={() => reset()}
                  disabled={loading}
                >
                  Reset
                </ModernButton>
                <ModernButton
                  variant="primary"
                  type="submit"
                  loading={loading}
                  startIcon={<SendIcon />}
                >
                  Create Transaction
                </ModernButton>
              </Box>
            </Grid>
          </Grid>
        </Box>
      </CardContent>
    </ModernCard>
  );
};

export default TransactionForm;