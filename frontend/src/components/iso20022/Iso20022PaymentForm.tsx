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
  Divider,
  Chip,
  CircularProgress,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { useForm, Controller } from 'react-hook-form';
import { Payment as PaymentIcon, Send as SendIcon } from '@mui/icons-material';

import iso20022ApiService from '../../services/iso20022Api';
import { Pain001Message, LocalInstrument, ChargeBearer, ServiceLevel } from '../../types/iso20022';

interface Iso20022PaymentFormData {
  debtorName: string;
  debtorAccountId: string;
  debtorAccountIban?: string;
  creditorName: string;
  creditorAccountId: string;
  creditorAccountIban?: string;
  amount: number;
  currency: string;
  localInstrument: LocalInstrument;
  serviceLevel?: ServiceLevel;
  chargeBearer: ChargeBearer;
  purposeCode?: string;
  remittanceInfo: string;
  urgency: 'HIGH' | 'NORM' | 'LOW';
  requestedExecutionDate: Date;
}

interface Iso20022PaymentFormProps {
  onPaymentCreated?: (pain002Response: any) => void;
  onError?: (error: string) => void;
}

const Iso20022PaymentForm: React.FC<Iso20022PaymentFormProps> = ({
  onPaymentCreated,
  onError,
}) => {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const {
    control,
    handleSubmit,
    watch,
    reset,
    formState: { errors },
  } = useForm<Iso20022PaymentFormData>({
    defaultValues: {
      currency: 'USD',
      localInstrument: LocalInstrument.RTP,
      chargeBearer: ChargeBearer.SHAR,
      urgency: 'NORM',
      requestedExecutionDate: new Date(),
    },
  });

  const watchedLocalInstrument = watch('localInstrument');

  const onSubmit = async (data: Iso20022PaymentFormData) => {
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // Create pain.001 message from form data
      const pain001Message: Pain001Message = iso20022ApiService.createPain001FromPayment({
        debtorName: data.debtorName,
        debtorAccountId: data.debtorAccountId,
        creditorName: data.creditorName,
        creditorAccountId: data.creditorAccountId,
        amount: data.amount,
        currency: data.currency,
        description: data.remittanceInfo,
        localInstrument: data.localInstrument,
      });

      // Add additional ISO 20022 fields
      pain001Message.CstmrCdtTrfInitn.PmtInf.PmtTpInf = {
        InstrPrty: data.urgency,
        LclInstrm: { Cd: data.localInstrument },
        ...(data.serviceLevel && { SvcLvl: { Cd: data.serviceLevel } }),
        ...(data.purposeCode && { CtgyPurp: { Cd: data.purposeCode } }),
      };

      pain001Message.CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.ChrgBr = data.chargeBearer;

      // Add IBAN if provided
      if (data.debtorAccountIban) {
        pain001Message.CstmrCdtTrfInitn.PmtInf.DbtrAcct.Id.IBAN = data.debtorAccountIban;
      }

      if (data.creditorAccountIban) {
        pain001Message.CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.CdtrAcct.Id.IBAN = data.creditorAccountIban;
      }

      // Process the payment
      const response = await iso20022ApiService.processPain001(pain001Message);

      setSuccess(`Payment initiated successfully. Message ID: ${pain001Message.CstmrCdtTrfInitn.GrpHdr.MsgId}`);
      
      if (onPaymentCreated) {
        onPaymentCreated(response);
      }

      // Reset form
      reset();

    } catch (err: any) {
      const errorMessage = err.message || 'Failed to process payment';
      setError(errorMessage);
      
      if (onError) {
        onError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const getPaymentMethodDescription = (instrument: LocalInstrument): string => {
    switch (instrument) {
      case LocalInstrument.RTP:
        return 'Real-Time Payment (Instant settlement)';
      case LocalInstrument.ACH:
        return 'Automated Clearing House (Next business day)';
      case LocalInstrument.WIRE:
        return 'Wire Transfer (Same day)';
      case LocalInstrument.SEPA:
        return 'Single Euro Payments Area (1-2 business days)';
      case LocalInstrument.RTGS:
        return 'Real-Time Gross Settlement (Immediate)';
      case LocalInstrument.INST:
        return 'Instant Payment (Seconds)';
      default:
        return 'Standard transfer';
    }
  };

  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <PaymentIcon sx={{ mr: 2, color: 'primary.main' }} />
          <Typography variant="h5" component="h2">
            ISO 20022 Payment Initiation
          </Typography>
          <Chip
            label="pain.001"
            color="primary"
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
            {/* Payment Details */}
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Payment Details
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="amount"
                control={control}
                rules={{ 
                  required: 'Amount is required',
                  min: { value: 0.01, message: 'Amount must be greater than 0' },
                }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Amount"
                    type="number"
                    fullWidth
                    error={!!errors.amount}
                    helperText={errors.amount?.message}
                    inputProps={{ step: '0.01', min: '0.01' }}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="currency"
                control={control}
                rules={{ required: 'Currency is required' }}
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.currency}>
                    <InputLabel>Currency</InputLabel>
                    <Select {...field} label="Currency">
                      <MenuItem value="USD">USD - US Dollar</MenuItem>
                      <MenuItem value="EUR">EUR - Euro</MenuItem>
                      <MenuItem value="GBP">GBP - British Pound</MenuItem>
                      <MenuItem value="CAD">CAD - Canadian Dollar</MenuItem>
                      <MenuItem value="AUD">AUD - Australian Dollar</MenuItem>
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="localInstrument"
                control={control}
                rules={{ required: 'Payment method is required' }}
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.localInstrument}>
                    <InputLabel>Payment Method</InputLabel>
                    <Select {...field} label="Payment Method">
                      <MenuItem value={LocalInstrument.RTP}>RTP - Real-Time Payment</MenuItem>
                      <MenuItem value={LocalInstrument.ACH}>ACH - Automated Clearing House</MenuItem>
                      <MenuItem value={LocalInstrument.WIRE}>WIRE - Wire Transfer</MenuItem>
                      <MenuItem value={LocalInstrument.SEPA}>SEPA - Single Euro Payments</MenuItem>
                      <MenuItem value={LocalInstrument.RTGS}>RTGS - Real-Time Gross Settlement</MenuItem>
                      <MenuItem value={LocalInstrument.INST}>INST - Instant Payment</MenuItem>
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="chargeBearer"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth>
                    <InputLabel>Charge Bearer</InputLabel>
                    <Select {...field} label="Charge Bearer">
                      <MenuItem value={ChargeBearer.DEBT}>DEBT - Debtor pays all charges</MenuItem>
                      <MenuItem value={ChargeBearer.CRED}>CRED - Creditor pays all charges</MenuItem>
                      <MenuItem value={ChargeBearer.SHAR}>SHAR - Charges shared</MenuItem>
                      <MenuItem value={ChargeBearer.SLEV}>SLEV - Service level agreement</MenuItem>
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Typography variant="body2" color="text.secondary">
                {getPaymentMethodDescription(watchedLocalInstrument)}
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Divider />
              <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                Debtor Information
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="debtorName"
                control={control}
                rules={{ required: 'Debtor name is required' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Debtor Name"
                    fullWidth
                    error={!!errors.debtorName}
                    helperText={errors.debtorName?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="debtorAccountId"
                control={control}
                rules={{ required: 'Debtor account is required' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Debtor Account Number"
                    fullWidth
                    error={!!errors.debtorAccountId}
                    helperText={errors.debtorAccountId?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Controller
                name="debtorAccountIban"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Debtor IBAN (Optional)"
                    fullWidth
                    placeholder="US64SVBKUS6S3300958879"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Divider />
              <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                Creditor Information
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="creditorName"
                control={control}
                rules={{ required: 'Creditor name is required' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Creditor Name"
                    fullWidth
                    error={!!errors.creditorName}
                    helperText={errors.creditorName?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="creditorAccountId"
                control={control}
                rules={{ required: 'Creditor account is required' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Creditor Account Number"
                    fullWidth
                    error={!!errors.creditorAccountId}
                    helperText={errors.creditorAccountId?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Controller
                name="creditorAccountIban"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Creditor IBAN (Optional)"
                    fullWidth
                    placeholder="US64SVBKUS6S3300958880"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Divider />
              <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                Additional Details
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="requestedExecutionDate"
                control={control}
                rules={{ required: 'Execution date is required' }}
                render={({ field }) => (
                  <DatePicker
                    {...field}
                    label="Requested Execution Date"
                    slotProps={{
                      textField: {
                        fullWidth: true,
                        error: !!errors.requestedExecutionDate,
                        helperText: errors.requestedExecutionDate?.message,
                      },
                    }}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="urgency"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth>
                    <InputLabel>Priority</InputLabel>
                    <Select {...field} label="Priority">
                      <MenuItem value="HIGH">HIGH - Urgent processing</MenuItem>
                      <MenuItem value="NORM">NORM - Normal processing</MenuItem>
                      <MenuItem value="LOW">LOW - Low priority</MenuItem>
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="purposeCode"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth>
                    <InputLabel>Purpose Code (Optional)</InputLabel>
                    <Select {...field} label="Purpose Code (Optional)">
                      <MenuItem value="">None</MenuItem>
                      <MenuItem value="CBFF">CBFF - Capital Building</MenuItem>
                      <MenuItem value="CHAR">CHAR - Charity Payment</MenuItem>
                      <MenuItem value="CORT">CORT - Trade Settlement</MenuItem>
                      <MenuItem value="SALA">SALA - Salary Payment</MenuItem>
                      <MenuItem value="TRAD">TRAD - Trade Services</MenuItem>
                      <MenuItem value="INTC">INTC - International Trade</MenuItem>
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Controller
                name="remittanceInfo"
                control={control}
                rules={{ required: 'Remittance information is required' }}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Remittance Information"
                    fullWidth
                    multiline
                    rows={3}
                    error={!!errors.remittanceInfo}
                    helperText={errors.remittanceInfo?.message || 'Payment description or reference'}
                    placeholder="Payment for services, Invoice #12345, etc."
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
                  disabled={loading}
                  startIcon={loading ? <CircularProgress size={20} /> : <SendIcon />}
                >
                  {loading ? 'Processing...' : 'Initiate Payment'}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>

        <Box sx={{ mt: 3, p: 2, backgroundColor: 'grey.50', borderRadius: 1 }}>
          <Typography variant="body2" color="text.secondary">
            <strong>ISO 20022 pain.001</strong> - Customer Credit Transfer Initiation
            <br />
            This form creates a standards-compliant ISO 20022 payment message that can be processed by any banking system.
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
};

export default Iso20022PaymentForm;