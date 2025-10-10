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
  Alert,
  CircularProgress,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip
} from '@mui/material';
import {
  CloudUpload as UploadIcon,
  Security as SecurityIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { PfxImportRequest } from '../../types/certificate';
import certificateService from '../../services/certificateService';

interface CertificateImportDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess: (message: string) => void;
}

const schema = yup.object({
  password: yup.string().required('Password is required'),
  tenantId: yup.string(),
  certificateType: yup.string(),
  description: yup.string(),
  validateCertificate: yup.boolean(),
  extractPrivateKey: yup.boolean(),
  extractCertificateChain: yup.boolean()
});

const CertificateImportDialog: React.FC<CertificateImportDialogProps> = ({
  open,
  onClose,
  onSuccess
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [importedCertificate, setImportedCertificate] = useState<any>(null);
  const [dragActive, setDragActive] = useState(false);

  const {
    control,
    handleSubmit,
    formState: { errors },
    reset,
    watch
  } = useForm<PfxImportRequest>({
    resolver: yupResolver(schema),
    defaultValues: {
      password: '',
      tenantId: '',
      certificateType: 'PFX_IMPORTED',
      description: '',
      validateCertificate: true,
      extractPrivateKey: true,
      extractCertificateChain: true
    }
  });

  const watchedValues = watch();

  const handleClose = () => {
    reset();
    setError(null);
    setSelectedFile(null);
    setImportedCertificate(null);
    onClose();
  };

  const handleFileSelect = (file: File) => {
    if (file && (file.name.toLowerCase().endsWith('.pfx') || file.name.toLowerCase().endsWith('.p12'))) {
      setSelectedFile(file);
      setError(null);
    } else {
      setError('Please select a valid .pfx or .p12 file');
    }
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelect(e.dataTransfer.files[0]);
    }
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFileSelect(e.target.files[0]);
    }
  };

  const onSubmit = async (data: PfxImportRequest) => {
    if (!selectedFile) {
      setError('Please select a PFX file');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const result = await certificateService.importPfxCertificate(selectedFile, data);
      setImportedCertificate(result);
      onSuccess('PFX certificate imported successfully');
    } catch (err: any) {
      setError(err.message || 'Failed to import PFX certificate');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadCertificate = () => {
    if (importedCertificate) {
      certificateService.downloadCertificatePem(
        {
          id: importedCertificate.certificateId,
          subjectDN: importedCertificate.subjectDN,
          issuerDN: importedCertificate.issuerDN,
          serialNumber: importedCertificate.serialNumber,
          validFrom: importedCertificate.validFrom,
          validTo: importedCertificate.validTo,
          publicKeyAlgorithm: importedCertificate.publicKeyAlgorithm,
          signatureAlgorithm: importedCertificate.signatureAlgorithm,
          certificateType: 'PFX_IMPORTED',
          tenantId: '',
          status: 'ACTIVE',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        },
        importedCertificate.certificatePem
      );
    }
  };

  const handleDownloadPrivateKey = () => {
    if (importedCertificate) {
      certificateService.downloadPrivateKeyPem(
        {
          id: importedCertificate.certificateId,
          subjectDN: importedCertificate.subjectDN,
          issuerDN: importedCertificate.issuerDN,
          serialNumber: importedCertificate.serialNumber,
          validFrom: importedCertificate.validFrom,
          validTo: importedCertificate.validTo,
          publicKeyAlgorithm: importedCertificate.publicKeyAlgorithm,
          signatureAlgorithm: importedCertificate.signatureAlgorithm,
          certificateType: 'PFX_IMPORTED',
          tenantId: '',
          status: 'ACTIVE',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        },
        importedCertificate.privateKeyPem
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
      <DialogTitle>Import PFX Certificate</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {importedCertificate ? (
          <Box>
            <Alert severity="success" sx={{ mb: 2 }}>
              PFX certificate imported successfully!
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
                  {importedCertificate.subjectDN}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Issuer DN
                </Typography>
                <Typography variant="body1">
                  {importedCertificate.issuerDN}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Serial Number
                </Typography>
                <Typography variant="body1">
                  {importedCertificate.serialNumber}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Alias
                </Typography>
                <Typography variant="body1">
                  {importedCertificate.alias}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Valid From
                </Typography>
                <Typography variant="body1">
                  {new Date(importedCertificate.validFrom).toLocaleString()}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Valid To
                </Typography>
                <Typography variant="body1">
                  {new Date(importedCertificate.validTo).toLocaleString()}
                </Typography>
              </Grid>
            </Grid>

            {importedCertificate.certificateChain && importedCertificate.certificateChain.length > 0 && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Certificate Chain
                </Typography>
                <List dense>
                  {importedCertificate.certificateChain.map((cert: string, index: number) => (
                    <ListItem key={index}>
                      <ListItemIcon>
                        <SecurityIcon />
                      </ListItemIcon>
                      <ListItemText
                        primary={`Certificate ${index + 1}`}
                        secondary={`${cert.length} characters`}
                      />
                    </ListItem>
                  ))}
                </List>
              </Box>
            )}

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
            </Box>

            <Typography variant="h6" gutterBottom>
              Certificate PEM
            </Typography>
            <Box sx={{ position: 'relative' }}>
              <TextField
                multiline
                rows={8}
                fullWidth
                value={importedCertificate.certificatePem}
                InputProps={{
                  readOnly: true,
                  sx: { fontFamily: 'monospace', fontSize: '0.875rem' }
                }}
                sx={{ mb: 1 }}
              />
              <Button
                size="small"
                onClick={() => handleCopyToClipboard(importedCertificate.certificatePem)}
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
                value={importedCertificate.privateKeyPem}
                InputProps={{
                  readOnly: true,
                  sx: { fontFamily: 'monospace', fontSize: '0.875rem' }
                }}
                sx={{ mb: 1 }}
              />
              <Button
                size="small"
                onClick={() => handleCopyToClipboard(importedCertificate.privateKeyPem)}
                sx={{ position: 'absolute', top: 8, right: 8 }}
              >
                Copy
              </Button>
            </Box>
          </Box>
        ) : (
          <Box>
            {/* File Upload Area */}
            <Paper
              variant="outlined"
              sx={{
                p: 3,
                textAlign: 'center',
                border: dragActive ? '2px dashed #1976d2' : '2px dashed #ccc',
                backgroundColor: dragActive ? '#f5f5f5' : 'transparent',
                cursor: 'pointer',
                mb: 3
              }}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
              onClick={() => document.getElementById('file-input')?.click()}
            >
              <input
                id="file-input"
                type="file"
                accept=".pfx,.p12"
                onChange={handleFileInputChange}
                style={{ display: 'none' }}
              />
              
              <UploadIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
              
              <Typography variant="h6" gutterBottom>
                {selectedFile ? selectedFile.name : 'Drop PFX file here or click to select'}
              </Typography>
              
              <Typography variant="body2" color="text.secondary">
                Supported formats: .pfx, .p12
              </Typography>
              
              {selectedFile && (
                <Box sx={{ mt: 2 }}>
                  <Chip
                    icon={<CheckCircleIcon />}
                    label={`${selectedFile.name} (${(selectedFile.size / 1024).toFixed(1)} KB)`}
                    color="success"
                    variant="outlined"
                  />
                </Box>
              )}
            </Paper>

            {/* Import Form */}
            <form onSubmit={handleSubmit(onSubmit)}>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <Controller
                    name="password"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        fullWidth
                        label="PFX Password"
                        type="password"
                        error={!!errors.password}
                        helperText={errors.password?.message}
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
                          <MenuItem value="PFX_IMPORTED">PFX Imported</MenuItem>
                          <MenuItem value="CLIENT">Client</MenuItem>
                          <MenuItem value="SERVER">Server</MenuItem>
                          <MenuItem value="CA">Certificate Authority</MenuItem>
                        </Select>
                      </FormControl>
                    )}
                  />
                </Grid>

                <Grid item xs={12}>
                  <Controller
                    name="description"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        fullWidth
                        label="Description"
                        placeholder="Optional description"
                        multiline
                        rows={2}
                      />
                    )}
                  />
                </Grid>

                <Grid item xs={12}>
                  <Typography variant="subtitle2" gutterBottom>
                    Import Options
                  </Typography>
                  
                  <Grid container spacing={1}>
                    <Grid item xs={12} sm={4}>
                      <Controller
                        name="validateCertificate"
                        control={control}
                        render={({ field }) => (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <CheckCircleIcon color="primary" />
                            <Typography variant="body2">
                              Validate Certificate
                            </Typography>
                          </Box>
                        )}
                      />
                    </Grid>
                    
                    <Grid item xs={12} sm={4}>
                      <Controller
                        name="extractPrivateKey"
                        control={control}
                        render={({ field }) => (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <SecurityIcon color="primary" />
                            <Typography variant="body2">
                              Extract Private Key
                            </Typography>
                          </Box>
                        )}
                      />
                    </Grid>
                    
                    <Grid item xs={12} sm={4}>
                      <Controller
                        name="extractCertificateChain"
                        control={control}
                        render={({ field }) => (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <InfoIcon color="primary" />
                            <Typography variant="body2">
                              Extract Certificate Chain
                            </Typography>
                          </Box>
                        )}
                      />
                    </Grid>
                  </Grid>
                </Grid>
              </Grid>
            </form>
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>
          {importedCertificate ? 'Close' : 'Cancel'}
        </Button>
        {!importedCertificate && (
          <Button
            onClick={handleSubmit(onSubmit)}
            variant="contained"
            disabled={loading || !selectedFile}
            startIcon={loading ? <CircularProgress size={20} /> : null}
          >
            {loading ? 'Importing...' : 'Import Certificate'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default CertificateImportDialog;