import React, { useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Tooltip,
  Box,
  Typography,
  CircularProgress,
  Alert,
  Button,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText
} from '@mui/material';
import {
  Visibility as ViewIcon,
  Delete as DeleteIcon,
  Security as SecurityIcon,
  Download as DownloadIcon,
  MoreVert as MoreVertIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import { CertificateInfo } from '../../types/certificate';
import certificateService from '../../services/certificateService';

interface CertificateListProps {
  certificates: CertificateInfo[];
  loading: boolean;
  onView: (certificate: CertificateInfo) => void;
  onDelete: (certificate: CertificateInfo) => void;
  onValidate: (certificate: CertificateInfo) => void;
}

const CertificateList: React.FC<CertificateListProps> = ({
  certificates,
  loading,
  onView,
  onDelete,
  onValidate
}) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedCertificate, setSelectedCertificate] = useState<CertificateInfo | null>(null);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, certificate: CertificateInfo) => {
    setAnchorEl(event.currentTarget);
    setSelectedCertificate(certificate);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedCertificate(null);
  };

  const handleView = () => {
    if (selectedCertificate) {
      onView(selectedCertificate);
    }
    handleMenuClose();
  };

  const handleDelete = () => {
    if (selectedCertificate) {
      onDelete(selectedCertificate);
    }
    handleMenuClose();
  };

  const handleValidate = () => {
    if (selectedCertificate) {
      onValidate(selectedCertificate);
    }
    handleMenuClose();
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
    return new Date(dateString).toLocaleDateString();
  };

  const formatDateTime = (dateString: string) => {
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

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (certificates.length === 0) {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Alert severity="info">
          No certificates found. Generate or import a certificate to get started.
        </Alert>
      </Box>
    );
  }

  return (
    <Box>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Subject DN</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Validation</TableCell>
              <TableCell>Valid From</TableCell>
              <TableCell>Valid To</TableCell>
              <TableCell>Days Until Expiry</TableCell>
              <TableCell>Tenant ID</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {certificates.map((certificate) => {
              const daysUntilExpiry = getDaysUntilExpiry(certificate);
              const expiringSoon = isExpiringSoon(certificate);
              const expired = isExpired(certificate);

              return (
                <TableRow key={certificate.id} hover>
                  <TableCell>
                    <Box>
                      <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                        {certificate.subjectDN}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Serial: {certificate.serialNumber}
                      </Typography>
                    </Box>
                  </TableCell>
                  
                  <TableCell>
                    <Chip
                      label={certificate.certificateType}
                      size="small"
                      variant="outlined"
                    />
                  </TableCell>
                  
                  <TableCell>
                    <Chip
                      icon={getStatusIcon(certificate.status)}
                      label={certificate.status}
                      color={getStatusColor(certificate.status) as any}
                      size="small"
                    />
                  </TableCell>
                  
                  <TableCell>
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
                  </TableCell>
                  
                  <TableCell>
                    <Typography variant="body2">
                      {formatDate(certificate.validFrom)}
                    </Typography>
                  </TableCell>
                  
                  <TableCell>
                    <Typography variant="body2">
                      {formatDate(certificate.validTo)}
                    </Typography>
                  </TableCell>
                  
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography
                        variant="body2"
                        color={expired ? 'error' : expiringSoon ? 'warning.main' : 'text.primary'}
                      >
                        {expired ? 'Expired' : `${daysUntilExpiry} days`}
                      </Typography>
                      {expiringSoon && !expired && (
                        <WarningIcon color="warning" fontSize="small" />
                      )}
                      {expired && (
                        <ErrorIcon color="error" fontSize="small" />
                      )}
                    </Box>
                  </TableCell>
                  
                  <TableCell>
                    <Typography variant="body2">
                      {certificate.tenantId || 'Default'}
                    </Typography>
                  </TableCell>
                  
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <Tooltip title="View Details">
                        <IconButton
                          size="small"
                          onClick={() => onView(certificate)}
                        >
                          <ViewIcon />
                        </IconButton>
                      </Tooltip>
                      
                      <Tooltip title="More Actions">
                        <IconButton
                          size="small"
                          onClick={(e) => handleMenuOpen(e, certificate)}
                        >
                          <MoreVertIcon />
                        </IconButton>
                      </Tooltip>
                    </Box>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Action Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        <MenuItem onClick={handleView}>
          <ListItemIcon>
            <ViewIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>View Details</ListItemText>
        </MenuItem>
        
        <MenuItem onClick={handleValidate}>
          <ListItemIcon>
            <SecurityIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Validate Certificate</ListItemText>
        </MenuItem>
        
        <MenuItem onClick={handleDelete}>
          <ListItemIcon>
            <DeleteIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Delete Certificate</ListItemText>
        </MenuItem>
      </Menu>
    </Box>
  );
};

export default CertificateList;