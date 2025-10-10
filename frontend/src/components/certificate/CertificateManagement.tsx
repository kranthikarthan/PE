import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Tabs,
  Tab,
  Card,
  CardContent,
  Grid,
  Button,
  Chip,
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
  Alert,
  Snackbar,
  Tooltip,
  Fab,
  SpeedDial,
  SpeedDialAction,
  SpeedDialIcon
} from '@mui/material';
import {
  Add as AddIcon,
  Upload as UploadIcon,
  Refresh as RefreshIcon,
  Download as DownloadIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  Visibility as ViewIcon,
  Security as SecurityIcon,
  Warning as WarningIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon
} from '@mui/icons-material';
import { useTheme } from '@mui/material/styles';
import { CertificateInfo, CertificateStats } from '../../types/certificate';
import certificateService from '../../services/certificateService';
import CertificateList from './CertificateList';
import CertificateGenerationDialog from './CertificateGenerationDialog';
import CertificateImportDialog from './CertificateImportDialog';
import CertificateDetailsDialog from './CertificateDetailsDialog';
import CertificateStatsCard from './CertificateStatsCard';

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

const CertificateManagement: React.FC = () => {
  const theme = useTheme();
  const [activeTab, setActiveTab] = useState(0);
  const [certificates, setCertificates] = useState<CertificateInfo[]>([]);
  const [stats, setStats] = useState<CertificateStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  // Dialog states
  const [generateDialogOpen, setGenerateDialogOpen] = useState(false);
  const [importDialogOpen, setImportDialogOpen] = useState(false);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [selectedCertificate, setSelectedCertificate] = useState<CertificateInfo | null>(null);
  
  // Filter states
  const [filter, setFilter] = useState({
    tenantId: '',
    certificateType: '',
    status: '',
    showExpired: false,
    showRotated: false
  });

  useEffect(() => {
    loadCertificates();
    loadStats();
  }, [filter]);

  const loadCertificates = async () => {
    setLoading(true);
    try {
      const data = await certificateService.getAllCertificates({
        tenantId: filter.tenantId || undefined,
        certificateType: filter.certificateType || undefined,
        status: filter.status || undefined,
        includeExpired: filter.showExpired,
        includeRotated: filter.showRotated
      });
      setCertificates(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load certificates');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const data = await certificateService.getCertificateStats();
      setStats(data);
    } catch (err: any) {
      console.error('Failed to load stats:', err);
    }
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  const handleGenerateCertificate = () => {
    setGenerateDialogOpen(true);
  };

  const handleImportCertificate = () => {
    setImportDialogOpen(true);
  };

  const handleViewCertificate = (certificate: CertificateInfo) => {
    setSelectedCertificate(certificate);
    setDetailsDialogOpen(true);
  };

  const handleDeleteCertificate = async (certificate: CertificateInfo) => {
    if (window.confirm(`Are you sure you want to delete certificate "${certificate.subjectDN}"?`)) {
      try {
        await certificateService.deleteCertificate(certificate.id);
        setSuccess('Certificate deleted successfully');
        loadCertificates();
        loadStats();
      } catch (err: any) {
        setError(err.message || 'Failed to delete certificate');
      }
    }
  };

  const handleValidateCertificate = async (certificate: CertificateInfo) => {
    try {
      await certificateService.validateCertificate(certificate.id);
      setSuccess('Certificate validation completed');
      loadCertificates();
    } catch (err: any) {
      setError(err.message || 'Failed to validate certificate');
    }
  };

  const handleRefresh = () => {
    loadCertificates();
    loadStats();
  };

  const handleFilterChange = (field: string, value: string | boolean) => {
    setFilter(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleCloseDialogs = () => {
    setGenerateDialogOpen(false);
    setImportDialogOpen(false);
    setDetailsDialogOpen(false);
    setSelectedCertificate(null);
  };

  const handleSuccess = (message: string) => {
    setSuccess(message);
    loadCertificates();
    loadStats();
    handleCloseDialogs();
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

  const speedDialActions = [
    {
      icon: <AddIcon />,
      name: 'Generate Certificate',
      onClick: handleGenerateCertificate
    },
    {
      icon: <UploadIcon />,
      name: 'Import PFX Certificate',
      onClick: handleImportCertificate
    },
    {
      icon: <RefreshIcon />,
      name: 'Refresh',
      onClick: handleRefresh
    }
  ];

  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <SecurityIcon />
          Certificate Management
        </Typography>
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={handleRefresh}
          disabled={loading}
        >
          Refresh
        </Button>
      </Box>

      {/* Stats Cards */}
      {stats && (
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={6} md={3}>
            <CertificateStatsCard
              title="Total Certificates"
              value={stats.totalCertificates}
              color="primary"
              icon={<SecurityIcon />}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <CertificateStatsCard
              title="Active Certificates"
              value={stats.activeCertificates}
              color="success"
              icon={<CheckCircleIcon />}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <CertificateStatsCard
              title="Expiring Soon"
              value={stats.expiringSoon}
              color="warning"
              icon={<WarningIcon />}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <CertificateStatsCard
              title="Expired Certificates"
              value={stats.expiredCertificates}
              color="error"
              icon={<ErrorIcon />}
            />
          </Grid>
        </Grid>
      )}

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Filters
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Tenant ID"
                value={filter.tenantId}
                onChange={(e) => handleFilterChange('tenantId', e.target.value)}
                size="small"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Certificate Type</InputLabel>
                <Select
                  value={filter.certificateType}
                  label="Certificate Type"
                  onChange={(e) => handleFilterChange('certificateType', e.target.value)}
                >
                  <MenuItem value="">All Types</MenuItem>
                  <MenuItem value="GENERATED">Generated</MenuItem>
                  <MenuItem value="PFX_IMPORTED">PFX Imported</MenuItem>
                  <MenuItem value="TRUSTED_CA">Trusted CA</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Status</InputLabel>
                <Select
                  value={filter.status}
                  label="Status"
                  onChange={(e) => handleFilterChange('status', e.target.value)}
                >
                  <MenuItem value="">All Statuses</MenuItem>
                  <MenuItem value="ACTIVE">Active</MenuItem>
                  <MenuItem value="EXPIRED">Expired</MenuItem>
                  <MenuItem value="ROTATED">Rotated</MenuItem>
                  <MenuItem value="REVOKED">Revoked</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', height: '100%' }}>
                <Button
                  variant={filter.showExpired ? 'contained' : 'outlined'}
                  size="small"
                  onClick={() => handleFilterChange('showExpired', !filter.showExpired)}
                >
                  Show Expired
                </Button>
                <Button
                  variant={filter.showRotated ? 'contained' : 'outlined'}
                  size="small"
                  onClick={() => handleFilterChange('showRotated', !filter.showRotated)}
                >
                  Show Rotated
                </Button>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={handleTabChange} aria-label="certificate management tabs">
            <Tab label="All Certificates" />
            <Tab label="Expiring Soon" />
            <Tab label="By Type" />
            <Tab label="By Status" />
          </Tabs>
        </Box>

        <TabPanel value={activeTab} index={0}>
          <CertificateList
            certificates={certificates}
            loading={loading}
            onView={handleViewCertificate}
            onDelete={handleDeleteCertificate}
            onValidate={handleValidateCertificate}
          />
        </TabPanel>

        <TabPanel value={activeTab} index={1}>
          <CertificateList
            certificates={certificates.filter(cert => certificateService.isExpiringSoon(cert, 30))}
            loading={loading}
            onView={handleViewCertificate}
            onDelete={handleDeleteCertificate}
            onValidate={handleValidateCertificate}
          />
        </TabPanel>

        <TabPanel value={activeTab} index={2}>
          <CertificateList
            certificates={certificates.filter(cert => cert.certificateType === 'GENERATED')}
            loading={loading}
            onView={handleViewCertificate}
            onDelete={handleDeleteCertificate}
            onValidate={handleValidateCertificate}
          />
        </TabPanel>

        <TabPanel value={activeTab} index={3}>
          <CertificateList
            certificates={certificates.filter(cert => cert.status === 'ACTIVE')}
            loading={loading}
            onView={handleViewCertificate}
            onDelete={handleDeleteCertificate}
            onValidate={handleValidateCertificate}
          />
        </TabPanel>
      </Card>

      {/* Speed Dial */}
      <SpeedDial
        ariaLabel="Certificate management actions"
        sx={{ position: 'fixed', bottom: 16, right: 16 }}
        icon={<SpeedDialIcon />}
      >
        {speedDialActions.map((action) => (
          <SpeedDialAction
            key={action.name}
            icon={action.icon}
            tooltipTitle={action.name}
            onClick={action.onClick}
          />
        ))}
      </SpeedDial>

      {/* Dialogs */}
      <CertificateGenerationDialog
        open={generateDialogOpen}
        onClose={handleCloseDialogs}
        onSuccess={handleSuccess}
      />

      <CertificateImportDialog
        open={importDialogOpen}
        onClose={handleCloseDialogs}
        onSuccess={handleSuccess}
      />

      <CertificateDetailsDialog
        open={detailsDialogOpen}
        certificate={selectedCertificate}
        onClose={handleCloseDialogs}
        onValidate={handleValidateCertificate}
      />

      {/* Snackbars */}
      <Snackbar
        open={!!error}
        autoHideDuration={6000}
        onClose={() => setError(null)}
      >
        <Alert onClose={() => setError(null)} severity="error">
          {error}
        </Alert>
      </Snackbar>

      <Snackbar
        open={!!success}
        autoHideDuration={6000}
        onClose={() => setSuccess(null)}
      >
        <Alert onClose={() => setSuccess(null)} severity="success">
          {success}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default CertificateManagement;