import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Grid,
  Box,
  Typography,
  Chip,
  Divider,
  Alert,
  CircularProgress,
  TextField,
  IconButton,
  Tooltip,
  Tabs,
  Tab,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon
} from '@mui/material';
import {
  Close as CloseIcon,
  Download as DownloadIcon,
  ContentCopy as CopyIcon,
  Security as SecurityIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import { CertificateInfo, CertificateValidationResult } from '../../types/certificate';
import certificateService from '../../services/certificateService';

interface CertificateDetailsDialogProps {
  open: boolean;
  certificate: CertificateInfo | null;
  onClose: () => void;
  onValidate: (certificate: CertificateInfo) => void;
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
      id={`certificate-tabpanel-${index}`}
      aria-labelledby={`certificate-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

const CertificateDetailsDialog: React.FC<CertificateDetailsDialogProps> = ({
  open,
  certificate,
  onClose,
  onValidate
}) => {
  const [activeTab, setActiveTab] = useState(0);
  const [validationResult, setValidationResult] = useState<CertificateValidationResult | null>(null);
  const [validating, setValidating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  const handleValidate = async () => {
    if (!certificate) return;

    setValidating(true);
    setError(null);

    try {
      const result = await certificateService.validateCertificate(certificate.id);
      setValidationResult(result);
    } catch (err: any) {
      setError(err.message || 'Failed to validate certificate');
    } finally {
      setValidating(false);
    }
  };

  const handleCopyToClipboard = async (text: string) => {
    try {
      await certificateService.copyToClipboard(text);
    } catch (err) {
      setError('Failed to copy to clipboard');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'EXPIRED':
        return 'error';
      case 'ROTATED':
        return 'warning';
      case 'REVOKED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <CheckCircleIcon />;
      case 'EXPIRED':
        return <ErrorIcon />;
      case 'ROTATED':
        return <WarningIcon />;
      case 'REVOKED':
        return <ErrorIcon />;
      default:
        return <InfoIcon />;
    }
  };

  const getValidationStatusColor = (status?: string) => {
    switch (status) {
      case 'VALID':
        return 'success';
      case 'INVALID':
        return 'error';
      case 'PENDING':
        return 'warning';
      case 'EXPIRED':
        return 'error';
      case 'REVOKED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getValidationStatusIcon = (status?: string) => {
    switch (status) {
      case 'VALID':
        return <CheckCircleIcon />;
      case 'INVALID':
        return <ErrorIcon />;
      case 'PENDING':
        return <WarningIcon />;
      case 'EXPIRED':
        return <ErrorIcon />;
      case 'REVOKED':
        return <ErrorIcon />;
      default:
        return <InfoIcon />;
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const getDaysUntilExpiry = (certificate: CertificateInfo) => {
    const validTo = new Date(certificate.validTo);
    const now = new Date();
    const daysUntilExpiry = Math.ceil((validTo.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    return daysUntilExpiry;
  };

  const isExpiringSoon = (certificate: CertificateInfo) => {
    const daysUntilExpiry = getDaysUntilExpiry(certificate);
    return daysUntilExpiry <= 30 && daysUntilExpiry > 0;
  };

  const isExpired = (certificate: CertificateInfo) => {
    const validTo = new Date(certificate.validTo);
    const now = new Date();
    return validTo < now;
  };

  if (!certificate) return null;

  const daysUntilExpiry = getDaysUntilExpiry(certificate);
  const expiringSoon = isExpiringSoon(certificate);
  const expired = isExpired(certificate);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">
            Certificate Details
          </Typography>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {/* Status Alerts */}
        {expired && (
          <Alert severity="error" sx={{ mb: 2 }}>
            This certificate has expired.
          </Alert>
        )}
        
        {expiringSoon && !expired && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            This certificate is expiring in {daysUntilExpiry} days.
          </Alert>
        )}

        {/* Certificate Overview */}
        <Paper sx={{ p: 2, mb: 2 }}>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Subject DN
              </Typography>
              <Typography variant="body1" sx={{ wordBreak: 'break-all' }}>
                {certificate.subjectDN}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Issuer DN
              </Typography>
              <Typography variant="body1" sx={{ wordBreak: 'break-all' }}>
                {certificate.issuerDN}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Serial Number
              </Typography>
              <Typography variant="body1">
                {certificate.serialNumber}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Certificate Type
              </Typography>
              <Chip
                label={certificate.certificateType}
                size="small"
                variant="outlined"
              />
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Status
              </Typography>
              <Chip
                icon={getStatusIcon(certificate.status)}
                label={certificate.status}
                color={getStatusColor(certificate.status) as any}
                size="small"
              />
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Validation Status
              </Typography>
              {certificate.validationStatus ? (
                <Chip
                  icon={getValidationStatusIcon(certificate.validationStatus)}
                  label={certificate.validationStatus}
                  color={getValidationStatusColor(certificate.validationStatus) as any}
                  size="small"
                />
              ) : (
                <Chip
                  label="Not Validated"
                  size="small"
                  variant="outlined"
                />
              )}
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Valid From
              </Typography>
              <Typography variant="body1">
                {formatDate(certificate.validFrom)}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Valid To
              </Typography>
              <Typography variant="body1">
                {formatDate(certificate.validTo)}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Days Until Expiry
              </Typography>
              <Typography
                variant="body1"
                color={expired ? 'error' : expiringSoon ? 'warning.main' : 'text.primary'}
              >
                {expired ? 'Expired' : `${daysUntilExpiry} days`}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Tenant ID
              </Typography>
              <Typography variant="body1">
                {certificate.tenantId || 'Default'}
              </Typography>
            </Grid>
          </Grid>
        </Paper>

        {/* Tabs */}
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={handleTabChange}>
            <Tab label="Technical Details" />
            <Tab label="Validation" />
            <Tab label="History" />
          </Tabs>
        </Box>

        <TabPanel value={activeTab} index={0}>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Public Key Algorithm
              </Typography>
              <Typography variant="body1">
                {certificate.publicKeyAlgorithm}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Key Size
              </Typography>
              <Typography variant="body1">
                {certificate.keySize ? `${certificate.keySize} bits` : 'N/A'}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Signature Algorithm
              </Typography>
              <Typography variant="body1">
                {certificate.signatureAlgorithm}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Alias
              </Typography>
              <Typography variant="body1">
                {certificate.alias || 'N/A'}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Created At
              </Typography>
              <Typography variant="body1">
                {formatDate(certificate.createdAt)}
              </Typography>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Typography variant="body2" color="text.secondary">
                Updated At
              </Typography>
              <Typography variant="body1">
                {formatDate(certificate.updatedAt)}
              </Typography>
            </Grid>
            
            {certificate.rotatedTo && (
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Rotated To
                </Typography>
                <Typography variant="body1">
                  {certificate.rotatedTo}
                </Typography>
              </Grid>
            )}
            
            {certificate.rotatedAt && (
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Rotated At
                </Typography>
                <Typography variant="body1">
                  {formatDate(certificate.rotatedAt)}
                </Typography>
              </Grid>
            )}
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={1}>
          <Box sx={{ mb: 2 }}>
            <Button
              variant="contained"
              startIcon={validating ? <CircularProgress size={20} /> : <SecurityIcon />}
              onClick={handleValidate}
              disabled={validating}
            >
              {validating ? 'Validating...' : 'Validate Certificate'}
            </Button>
          </Box>

          {validationResult && (
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Validation Result
              </Typography>
              
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Status
                  </Typography>
                  <Chip
                    icon={getValidationStatusIcon(validationResult.status)}
                    label={validationResult.status}
                    color={getValidationStatusColor(validationResult.status) as any}
                    size="small"
                  />
                </Grid>
                
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Message
                  </Typography>
                  <Typography variant="body1">
                    {validationResult.message}
                  </Typography>
                </Grid>
                
                {validationResult.validationDetails && (
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary">
                      Details
                    </Typography>
                    <Typography variant="body1">
                      {validationResult.validationDetails}
                    </Typography>
                  </Grid>
                )}
              </Grid>
            </Paper>
          )}

          {certificate.validationMessage && (
            <Paper sx={{ p: 2, mt: 2 }}>
              <Typography variant="h6" gutterBottom>
                Last Validation
              </Typography>
              <Typography variant="body1">
                {certificate.validationMessage}
              </Typography>
              {certificate.lastValidated && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  Last validated: {formatDate(certificate.lastValidated)}
                </Typography>
              )}
            </Paper>
          )}
        </TabPanel>

        <TabPanel value={activeTab} index={2}>
          <List>
            <ListItem>
              <ListItemIcon>
                <InfoIcon />
              </ListItemIcon>
              <ListItemText
                primary="Certificate Created"
                secondary={formatDate(certificate.createdAt)}
              />
            </ListItem>
            
            <ListItem>
              <ListItemIcon>
                <InfoIcon />
              </ListItemIcon>
              <ListItemText
                primary="Last Updated"
                secondary={formatDate(certificate.updatedAt)}
              />
            </ListItem>
            
            {certificate.lastValidated && (
              <ListItem>
                <ListItemIcon>
                  <SecurityIcon />
                </ListItemIcon>
                <ListItemText
                  primary="Last Validated"
                  secondary={formatDate(certificate.lastValidated)}
                />
              </ListItem>
            )}
            
            {certificate.rotatedAt && (
              <ListItem>
                <ListItemIcon>
                  <RefreshIcon />
                </ListItemIcon>
                <ListItemText
                  primary="Certificate Rotated"
                  secondary={formatDate(certificate.rotatedAt)}
                />
              </ListItem>
            )}
          </List>
        </TabPanel>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default CertificateDetailsDialog;