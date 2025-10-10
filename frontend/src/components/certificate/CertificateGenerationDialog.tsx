import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Box,
  Typography,
  Chip,
  FormControlLabel,
  Checkbox,
  Alert,
  CircularProgress
} from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { CertificateGenerationRequest } from '../../types/certificate';
import certificateService from '../../services/certificateService';

interface CertificateGenerationDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess: (message: string) => void;
}

const schema = yup.object({
  subjectDN: yup.string().required('Subject DN is required'),
  tenantId: yup.string(),
  certificateType: yup.string(),
  validityDays: yup.number().min(1, 'Validity days must be at least 1').max(3650, 'Validity days cannot exceed 10 years'),
  description: yup.string()
});

const CertificateGenerationDialog: React.FC<CertificateGenerationDialogProps> = ({
  open,
  onClose,
  onSuccess
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [generatedCertificate, setGeneratedCertificate] = useState<any>(null);

  const {
    control,
    handleSubmit,
    formState: { errors },
    reset,
    watch
  } = useForm<CertificateGenerationRequest>({
    resolver: yupResolver(schema),
    defaultValues: {
      subjectDN: '',
      tenantId: '',
      certificateType: 'GENERATED',
      validityDays: 365,
      keyUsage: ['digitalSignature', 'keyEncipherment'],
      extendedKeyUsage: ['serverAuth', 'clientAuth'],
      description: '',
      includePrivateKey: true,
      includePublicKey: true
    }
  });

  const watchedValues = watch();

  const handleClose = () => {
    reset();
    setError(null);
    setGeneratedCertificate(null);
    onClose();
  };

  const onSubmit = async (data: CertificateGenerationRequest) => {
    setLoading(true);
    setError(null);

    try {
      const result = await certificateService.generateCertificate(data);
      setGeneratedCertificate(result);
      onSuccess('Certificate generated successfully');
    } catch (err: any) {
      setError(err.message || 'Failed to generate certificate');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadCertificate = () => {
    if (generatedCertificate) {
      certificateService.downloadCertificatePem(
        {
          id: generatedCertificate.certificateId,
          subjectDN: generatedCertificate.subjectDN,
          issuerDN: generatedCertificate.issuerDN,
          serialNumber: generatedCertificate.serialNumber,
          validFrom: generatedCertificate.validFrom,
          validTo: generatedCertificate.validTo,
          publicKeyAlgorithm: generatedCertificate.publicKeyAlgorithm,
          keySize: generatedCertificate.keySize,
          signatureAlgorithm: generatedCertificate.signatureAlgorithm,
          certificateType: 'GENERATED',
          tenantId: '',
          status: 'ACTIVE',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        },
        generatedCertificate.certificatePem
      );
    }
  };

  const handleDownloadPrivateKey = () => {
    if (generatedCertificate) {
      certificateService.downloadPrivateKeyPem(
        {
          id: generatedCertificate.certificateId,
          subjectDN: generatedCertificate.subjectDN,
          issuerDN: generatedCertificate.issuerDN,
          serialNumber: generatedCertificate.serialNumber,
          validFrom: generatedCertificate.validFrom,
          validTo: generatedCertificate.validTo,
          publicKeyAlgorithm: generatedCertificate.publicKeyAlgorithm,
          keySize: generatedCertificate.keySize,
          signatureAlgorithm: generatedCertificate.signatureAlgorithm,
          certificateType: 'GENERATED',
          tenantId: '',
          status: 'ACTIVE',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        },
        generatedCertificate.privateKeyPem
      );
    }
  };

  const handleDownloadPublicKey = () => {
    if (generatedCertificate && generatedCertificate.publicKeyPem) {
      certificateService.downloadPublicKeyPem(
        {
          id: generatedCertificate.certificateId,
          subjectDN: generatedCertificate.subjectDN,
          issuerDN: generatedCertificate.issuerDN,
          serialNumber: generatedCertificate.serialNumber,
          validFrom: generatedCertificate.validFrom,
          validTo: generatedCertificate.validTo,
          publicKeyAlgorithm: generatedCertificate.publicKeyAlgorithm,
          keySize: generatedCertificate.keySize,
          signatureAlgorithm: generatedCertificate.signatureAlgorithm,
          certificateType: 'GENERATED',
          tenantId: '',
          status: 'ACTIVE',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        },
        generatedCertificate.publicKeyPem
      );
    }
  };

  const handleCopyToClipboard = async (text: string) => {
    try {
      await certificateService.copyToClipboard(text);
      onSuccess('Copied to clipboard');
    } catch (err) {
      setError('Failed to copy to clipboard');
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>Generate New Certificate</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {generatedCertificate ? (
          <Box>
            <Alert severity="success" sx={{ mb: 2 }}>
              Certificate generated successfully!
            </Alert>
            
            <Typography variant="h6" gutterBottom>
              Certificate Details
            </Typography>
            
            <Grid container spacing={2} sx={{ mb: 2 }}>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Subject DN
                </Typography>
                <Typography variant="body1">
                  {generatedCertificate.subjectDN}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Issuer DN
                </Typography>
                <Typography variant="body1">
                  {generatedCertificate.issuerDN}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Serial Number
                </Typography>
                <Typography variant="body1">
                  {generatedCertificate.serialNumber}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Key Size
                </Typography>
                <Typography variant="body1">
                  {generatedCertificate.keySize} bits
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Valid From
                </Typography>
                <Typography variant="body1">
                  {new Date(generatedCertificate.validFrom).toLocaleString()}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Valid To
                </Typography>
                <Typography variant="body1">
                  {new Date(generatedCertificate.validTo).toLocaleString()}
                </Typography>
              </Grid>
            </Grid>

            <Typography variant="h6" gutterBottom>
              Download Options
            </Typography>
            
            <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
              <Button
                variant="outlined"
                onClick={handleDownloadCertificate}
                size="small"
              >
                Download Certificate
              </Button>
              <Button
                variant="outlined"
                onClick={handleDownloadPrivateKey}
                size="small"
              >
                Download Private Key
              </Button>
              {generatedCertificate.publicKeyPem && (
                <Button
                  variant="outlined"
                  onClick={handleDownloadPublicKey}
                  size="small"
                >
                  Download Public Key
                </Button>
              )}
            </Box>

            <Typography variant="h6" gutterBottom>
              Certificate PEM
            </Typography>
            <Box sx={{ position: 'relative' }}>
              <TextField
                multiline
                rows={8}
                fullWidth
                value={generatedCertificate.certificatePem}
                InputProps={{
                  readOnly: true,
                  sx: { fontFamily: 'monospace', fontSize: '0.875rem' }
                }}
                sx={{ mb: 1 }}
              />
              <Button
                size="small"
                onClick={() => handleCopyToClipboard(generatedCertificate.certificatePem)}
                sx={{ position: 'absolute', top: 8, right: 8 }}
              >
                Copy
              </Button>
            </Box>

            <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
              Private Key PEM
            </Typography>
            <Box sx={{ position: 'relative' }}>
              <TextField
                multiline
                rows={8}
                fullWidth
                value={generatedCertificate.privateKeyPem}
                InputProps={{
                  readOnly: true,
                  sx: { fontFamily: 'monospace', fontSize: '0.875rem' }
                }}
                sx={{ mb: 1 }}
              />
              <Button
                size="small"
                onClick={() => handleCopyToClipboard(generatedCertificate.privateKeyPem)}
                sx={{ position: 'absolute', top: 8, right: 8 }}
              >
                Copy
              </Button>
            </Box>
          </Box>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Controller
                  name="subjectDN"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      fullWidth
                      label="Subject DN"
                      placeholder="CN=example.com, O=Organization, C=US"
                      error={!!errors.subjectDN}
                      helperText={errors.subjectDN?.message}
                      required
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="tenantId"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      fullWidth
                      label="Tenant ID"
                      placeholder="Optional tenant ID"
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="certificateType"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Certificate Type</InputLabel>
                      <Select {...field} label="Certificate Type">
                        <MenuItem value="GENERATED">Generated</MenuItem>
                        <MenuItem value="CLIENT">Client</MenuItem>
                        <MenuItem value="SERVER">Server</MenuItem>
                        <MenuItem value="CA">Certificate Authority</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="validityDays"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      fullWidth
                      label="Validity Days"
                      type="number"
                      error={!!errors.validityDays}
                      helperText={errors.validityDays?.message}
                      required
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="description"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      fullWidth
                      label="Description"
                      placeholder="Optional description"
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  Key Usage
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {['digitalSignature', 'keyEncipherment', 'dataEncipherment', 'keyAgreement', 'keyCertSign', 'cRLSign'].map((usage) => (
                    <Chip
                      key={usage}
                      label={usage}
                      color={watchedValues.keyUsage?.includes(usage) ? 'primary' : 'default'}
                      onClick={() => {
                        const currentUsage = watchedValues.keyUsage || [];
                        const newUsage = currentUsage.includes(usage)
                          ? currentUsage.filter((u: string) => u !== usage)
                          : [...currentUsage, usage];
                        // Update the form value
                      }}
                    />
                  ))}
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  Extended Key Usage
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {['serverAuth', 'clientAuth', 'codeSigning', 'emailProtection', 'timeStamping'].map((usage) => (
                    <Chip
                      key={usage}
                      label={usage}
                      color={watchedValues.extendedKeyUsage?.includes(usage) ? 'primary' : 'default'}
                      onClick={() => {
                        const currentUsage = watchedValues.extendedKeyUsage || [];
                        const newUsage = currentUsage.includes(usage)
                          ? currentUsage.filter((u: string) => u !== usage)
                          : [...currentUsage, usage];
                        // Update the form value
                      }}
                    />
                  ))}
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Controller
                  name="includePrivateKey"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Checkbox {...field} checked={field.value} />}
                      label="Include Private Key"
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Controller
                  name="includePublicKey"
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Checkbox {...field} checked={field.value} />}
                      label="Include Public Key"
                    />
                  )}
                />
              </Grid>
            </Grid>
          </form>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>
          {generatedCertificate ? 'Close' : 'Cancel'}
        </Button>
        {!generatedCertificate && (
          <Button
            onClick={handleSubmit(onSubmit)}
            variant="contained"
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : null}
          >
            {loading ? 'Generating...' : 'Generate Certificate'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default CertificateGenerationDialog;