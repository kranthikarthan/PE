import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Chip,
  CircularProgress,
  Autocomplete,
} from '@mui/material';
import { Cancel as CancelIcon, Search as SearchIcon } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';

import iso20022ApiService from '../../services/iso20022Api';
import { Camt055Message, CancellationReasonCode, CancellationResult } from '../../types/iso20022';

interface Iso20022CancellationFormData {
  originalEndToEndId: string;
  originalTransactionId?: string;
  reasonCode: CancellationReasonCode;
  cancellationReason: string;
  additionalInfo?: string;
}

interface Iso20022CancellationFormProps {
  onCancellationProcessed?: (result: CancellationResult[]) => void;
  onError?: (error: string) => void;
  prefilledEndToEndId?: string;
}

const Iso20022CancellationForm: React.FC<Iso20022CancellationFormProps> = ({
  onCancellationProcessed,
  onError,
  prefilledEndToEndId,
}) => {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [searchingTransaction, setSearchingTransaction] = useState(false);

  const {
    control,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<Iso20022CancellationFormData>({
    defaultValues: {
      reasonCode: CancellationReasonCode.CUST,
      originalEndToEndId: prefilledEndToEndId || '',
    },
  });

  const watchedReasonCode = watch('reasonCode');

  const onSubmit = async (data: Iso20022CancellationFormData) => {
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // Create camt.055 message from form data
      const camt055Message: Camt055Message = iso20022ApiService.createCamt055FromCancellation({
        originalEndToEndId: data.originalEndToEndId,
        reasonCode: data.reasonCode,
        reason: data.cancellationReason,
        originalAmount: 0, // This would be populated from the original transaction
        originalCurrency: 'USD', // This would be populated from the original transaction
      });

      // Add additional information if provided
      if (data.additionalInfo) {
        const txInfo = camt055Message.CstmrPmtCxlReq.Undrlyg[0].TxInf[0];
        if (txInfo.CxlRsnInf && txInfo.CxlRsnInf[0]) {
          txInfo.CxlRsnInf[0].AddtlInf = [data.additionalInfo];
        }
      }

      // Process the cancellation
      const response = await iso20022ApiService.processCamt055(camt055Message);

      const acceptedCancellations = response.cancellationResults.filter(r => r.status === 'ACCEPTED');
      const rejectedCancellations = response.cancellationResults.filter(r => r.status === 'REJECTED');

      if (acceptedCancellations.length > 0) {
        setSuccess(
          `Cancellation processed successfully. ${acceptedCancellations.length} payment(s) cancelled.`
        );
      }

      if (rejectedCancellations.length > 0) {
        setError(
          `${rejectedCancellations.length} cancellation(s) were rejected: ${
            rejectedCancellations.map(r => r.errorMessage).join(', ')
          }`
        );
      }

      if (onCancellationProcessed) {
        onCancellationProcessed(response.cancellationResults);
      }

      // Reset form if successful
      if (rejectedCancellations.length === 0) {
        reset();
      }

    } catch (err: any) {
      const errorMessage = err.message || 'Failed to process cancellation';
      setError(errorMessage);
      
      if (onError) {
        onError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const searchTransaction = async (endToEndId: string) => {
    if (!endToEndId.trim()) return;

    setSearchingTransaction(true);
    try {
      // This would search for the transaction by end-to-end ID
      // For now, just validate the format
      if (endToEndId.startsWith('E2E-')) {
        setValue('originalTransactionId', 'Found'); // Placeholder
      } else {
        setError('Invalid end-to-end ID format. Should start with E2E-');
      }
    } catch (err: any) {
      setError('Transaction not found or not eligible for cancellation');
    } finally {
      setSearchingTransaction(false);
    }
  };

  const getReasonDescription = (code: CancellationReasonCode): string => {
    switch (code) {
      case CancellationReasonCode.CUST:
        return 'General customer request';
      case CancellationReasonCode.DUPL:
        return 'Duplicate payment detected';
      case CancellationReasonCode.FRAD:
        return 'Suspected fraudulent payment';
      case CancellationReasonCode.TECH:
        return 'Technical problem or error';
      case CancellationReasonCode.UPAY:
        return 'Payment made in error';
      case CancellationReasonCode.CUTA:
        return 'Past processing cut-off time';
      case CancellationReasonCode.AGNT:
        return 'Incorrect bank or agent';
      case CancellationReasonCode.CURR:
        return 'Incorrect currency specified';
      default:
        return 'Customer requested cancellation';
    }
  };

  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <CancelIcon sx={{ mr: 2, color: 'warning.main' }} />
          <Typography variant="h5" component="h2">
            ISO 20022 Payment Cancellation
          </Typography>
          <Chip
            label="camt.055"
            color="warning"
            size="small"
            sx={{ ml: 2 }}
          />
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}

        <form onSubmit={handleSubmit(onSubmit)}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Original Payment Identification
              </Typography>
            </Grid>

            <Grid item xs={12} sm={10}>
              <Controller
                name="originalEndToEndId"
                control={control}
                rules={{ 
                  required: 'Original end-to-end ID is required',
                  pattern: {
                    value: /^E2E-.+/,
                    message: 'End-to-end ID must start with E2E-'
                  }
                }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Original End-to-End ID"
                    fullWidth
                    error={!!errors.originalEndToEndId}
                    helperText={errors.originalEndToEndId?.message || 'Enter the end-to-end ID from the original payment'}
                    placeholder="E2E-20240115-001"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={2}>
              <Button
                variant="outlined"
                fullWidth
                startIcon={searchingTransaction ? <CircularProgress size={20} /> : <SearchIcon />}
                onClick={() => searchTransaction(watch('originalEndToEndId'))}
                disabled={searchingTransaction || !watch('originalEndToEndId')}
                sx={{ height: '56px' }}
              >
                Search
              </Button>
            </Grid>

            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                Cancellation Details
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="reasonCode"
                control={control}
                rules={{ required: 'Reason code is required' }}
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.reasonCode}>
                    <InputLabel>Cancellation Reason Code</InputLabel>
                    <Select {...field} label="Cancellation Reason Code">
                      <MenuItem value={CancellationReasonCode.CUST}>CUST - Customer Request</MenuItem>
                      <MenuItem value={CancellationReasonCode.DUPL}>DUPL - Duplicate Payment</MenuItem>
                      <MenuItem value={CancellationReasonCode.FRAD}>FRAD - Fraudulent Payment</MenuItem>
                      <MenuItem value={CancellationReasonCode.TECH}>TECH - Technical Problem</MenuItem>
                      <MenuItem value={CancellationReasonCode.UPAY}>UPAY - Undue Payment</MenuItem>
                      <MenuItem value={CancellationReasonCode.CUTA}>CUTA - Cut-off Time</MenuItem>
                      <MenuItem value={CancellationReasonCode.AGNT}>AGNT - Incorrect Agent</MenuItem>
                      <MenuItem value={CancellationReasonCode.CURR}>CURR - Incorrect Currency</MenuItem>
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {getReasonDescription(watchedReasonCode)}
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Controller
                name="cancellationReason"
                control={control}
                rules={{ required: 'Cancellation reason is required' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Cancellation Reason"
                    fullWidth
                    multiline
                    rows={3}
                    error={!!errors.cancellationReason}
                    helperText={errors.cancellationReason?.message || 'Detailed explanation for the cancellation'}
                    placeholder="Customer detected duplicate payment and requests cancellation..."
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Controller
                name="additionalInfo"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Additional Information (Optional)"
                    fullWidth
                    multiline
                    rows={2}
                    placeholder="Any additional context or information..."
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                <Button
                  variant="outlined"
                  onClick={() => reset()}
                  disabled={loading}
                >
                  Reset
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  color="warning"
                  disabled={loading}
                  startIcon={loading ? <CircularProgress size={20} /> : <CancelIcon />}
                >
                  {loading ? 'Processing...' : 'Request Cancellation'}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>

        <Box sx={{ mt: 3, p: 2, backgroundColor: 'warning.50', borderRadius: 1 }}>
          <Typography variant="body2" color="text.secondary">
            <strong>ISO 20022 camt.055</strong> - Customer Payment Cancellation Request
            <br />
            Use this form to cancel payments that have not yet been completed. For completed payments, use the reversal function instead.
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
};

export default Iso20022CancellationForm;