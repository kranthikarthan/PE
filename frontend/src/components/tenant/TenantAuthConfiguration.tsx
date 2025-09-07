import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Button,
  Alert,
  Chip,
  Divider,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Tooltip
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  Security as SecurityIcon,
  Key as KeyIcon,
  Http as HttpIcon
} from '@mui/icons-material';
import { tenantAuthApi } from '../../services/tenantAuthApi';
import {
  TenantAuthConfiguration,
  TenantAuthConfigurationRequest,
  AuthMethod,
  AUTH_METHOD_OPTIONS,
  JWS_ALGORITHM_OPTIONS
} from '../../types/tenantAuth';

interface TenantAuthConfigurationProps {
  tenantId: string;
  onConfigurationChange?: () => void;
}

const TenantAuthConfigurationComponent: React.FC<TenantAuthConfigurationProps> = ({
  tenantId,
  onConfigurationChange
}) => {
  const [configurations, setConfigurations] = useState<TenantAuthConfiguration[]>([]);
  const [activeConfiguration, setActiveConfiguration] = useState<TenantAuthConfiguration | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingConfig, setEditingConfig] = useState<TenantAuthConfiguration | null>(null);
  const [formData, setFormData] = useState<TenantAuthConfigurationRequest>({
    tenantId,
    authMethod: AuthMethod.JWT,
    clientIdHeaderName: 'X-Client-ID',
    clientSecretHeaderName: 'X-Client-Secret',
    authHeaderName: 'Authorization',
    authHeaderPrefix: 'Bearer',
    jwsAlgorithm: 'HS256',
    includeClientHeaders: false
  });

  useEffect(() => {
    loadConfigurations();
  }, [tenantId]);

  const loadConfigurations = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [configs, active] = await Promise.all([
        tenantAuthApi.getConfigurations(tenantId),
        tenantAuthApi.getActiveConfiguration(tenantId)
      ]);
      
      setConfigurations(configs);
      setActiveConfiguration(active);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load configurations');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);
      setSuccess(null);

      const configData = {
        ...formData,
        createdBy: 'current-user', // This should come from auth context
        updatedBy: 'current-user'
      };

      await tenantAuthApi.createOrUpdateConfiguration(configData);
      setSuccess('Configuration saved successfully');
      setDialogOpen(false);
      resetForm();
      loadConfigurations();
      onConfigurationChange?.();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (config: TenantAuthConfiguration) => {
    setEditingConfig(config);
    setFormData({
      tenantId: config.tenantId,
      authMethod: config.authMethod,
      clientId: config.clientId,
      clientIdHeaderName: config.clientIdHeaderName,
      clientSecretHeaderName: config.clientSecretHeaderName,
      authHeaderName: config.authHeaderName,
      authHeaderPrefix: config.authHeaderPrefix,
      tokenEndpoint: config.tokenEndpoint,
      publicKeyEndpoint: config.publicKeyEndpoint,
      jwsPublicKey: config.jwsPublicKey,
      jwsAlgorithm: config.jwsAlgorithm,
      jwsIssuer: config.jwsIssuer,
      includeClientHeaders: config.includeClientHeaders,
      description: config.description,
      updatedBy: 'current-user'
    });
    setDialogOpen(true);
  };

  const handleActivate = async (id: string) => {
    try {
      setLoading(true);
      setError(null);
      await tenantAuthApi.activateConfiguration(id, 'current-user');
      setSuccess('Configuration activated successfully');
      loadConfigurations();
      onConfigurationChange?.();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to activate configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleDeactivate = async (id: string) => {
    try {
      setLoading(true);
      setError(null);
      await tenantAuthApi.deactivateConfiguration(id, 'current-user');
      setSuccess('Configuration deactivated successfully');
      loadConfigurations();
      onConfigurationChange?.();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to deactivate configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this configuration?')) {
      try {
        setLoading(true);
        setError(null);
        await tenantAuthApi.deleteConfiguration(id);
        setSuccess('Configuration deleted successfully');
        loadConfigurations();
        onConfigurationChange?.();
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to delete configuration');
      } finally {
        setLoading(false);
      }
    }
  };

  const resetForm = () => {
    setFormData({
      tenantId,
      authMethod: AuthMethod.JWT,
      clientIdHeaderName: 'X-Client-ID',
      clientSecretHeaderName: 'X-Client-Secret',
      authHeaderName: 'Authorization',
      authHeaderPrefix: 'Bearer',
      jwsAlgorithm: 'HS256',
      includeClientHeaders: false
    });
    setEditingConfig(null);
  };

  const handleDialogClose = () => {
    setDialogOpen(false);
    resetForm();
  };

  const getAuthMethodDescription = (method: AuthMethod) => {
    const option = AUTH_METHOD_OPTIONS.find(opt => opt.value === method);
    return option?.description || '';
  };

  const getJwsAlgorithmDescription = (algorithm: string) => {
    const option = JWS_ALGORITHM_OPTIONS.find(opt => opt.value === algorithm);
    return option?.description || '';
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h2">
          <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
          Authentication Configuration
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setDialogOpen(true)}
          disabled={loading}
        >
          Add Configuration
        </Button>
      </Box>

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

      {/* Active Configuration */}
      {activeConfiguration && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Box display="flex" alignItems="center" mb={2}>
              <CheckCircleIcon color="success" sx={{ mr: 1 }} />
              <Typography variant="h6">Active Configuration</Typography>
            </Box>
            <Grid container spacing={2}>
              <Grid item xs={12} md={3}>
                <Typography variant="subtitle2" color="textSecondary">
                  Authentication Method
                </Typography>
                <Chip 
                  label={activeConfiguration.authMethod} 
                  color="primary" 
                  size="small"
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <Typography variant="subtitle2" color="textSecondary">
                  Client Headers
                </Typography>
                <Chip 
                  label={activeConfiguration.includeClientHeaders ? 'Enabled' : 'Disabled'} 
                  color={activeConfiguration.includeClientHeaders ? 'success' : 'default'} 
                  size="small"
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <Typography variant="subtitle2" color="textSecondary">
                  Client ID
                </Typography>
                <Typography variant="body2">
                  {activeConfiguration.clientId || 'Not configured'}
                </Typography>
              </Grid>
              <Grid item xs={12} md={3}>
                <Typography variant="subtitle2" color="textSecondary">
                  JWS Algorithm
                </Typography>
                <Typography variant="body2">
                  {activeConfiguration.jwsAlgorithm}
                </Typography>
              </Grid>
            </Grid>
            {activeConfiguration.description && (
              <Box mt={2}>
                <Typography variant="subtitle2" color="textSecondary">
                  Description
                </Typography>
                <Typography variant="body2">
                  {activeConfiguration.description}
                </Typography>
              </Box>
            )}
          </CardContent>
        </Card>
      )}

      {/* All Configurations */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            All Configurations
          </Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Method</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Client Headers</TableCell>
                  <TableCell>Algorithm</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {configurations.map((config) => (
                  <TableRow key={config.id}>
                    <TableCell>
                      <Box display="flex" alignItems="center">
                        <SecurityIcon sx={{ mr: 1, fontSize: 20 }} />
                        <Box>
                          <Typography variant="body2" fontWeight="medium">
                            {config.authMethod}
                          </Typography>
                          <Typography variant="caption" color="textSecondary">
                            {getAuthMethodDescription(config.authMethod)}
                          </Typography>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        icon={config.isActive ? <CheckCircleIcon /> : <CancelIcon />}
                        label={config.isActive ? 'Active' : 'Inactive'}
                        color={config.isActive ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={config.includeClientHeaders ? 'Enabled' : 'Disabled'}
                        color={config.includeClientHeaders ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {config.jwsAlgorithm}
                      </Typography>
                      <Typography variant="caption" color="textSecondary">
                        {getJwsAlgorithmDescription(config.jwsAlgorithm)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {config.description || 'No description'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Box display="flex" gap={1}>
                        <Tooltip title="Edit">
                          <IconButton
                            size="small"
                            onClick={() => handleEdit(config)}
                            disabled={loading}
                          >
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        {config.isActive ? (
                          <Tooltip title="Deactivate">
                            <IconButton
                              size="small"
                              onClick={() => handleDeactivate(config.id)}
                              disabled={loading}
                            >
                              <CancelIcon />
                            </IconButton>
                          </Tooltip>
                        ) : (
                          <Tooltip title="Activate">
                            <IconButton
                              size="small"
                              onClick={() => handleActivate(config.id)}
                              disabled={loading}
                            >
                              <CheckCircleIcon />
                            </IconButton>
                          </Tooltip>
                        )}
                        <Tooltip title="Delete">
                          <IconButton
                            size="small"
                            onClick={() => handleDelete(config.id)}
                            disabled={loading}
                            color="error"
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Configuration Dialog */}
      <Dialog open={dialogOpen} onClose={handleDialogClose} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingConfig ? 'Edit Authentication Configuration' : 'Add Authentication Configuration'}
        </DialogTitle>
        <form onSubmit={handleSubmit}>
          <DialogContent>
            <Grid container spacing={2}>
              {/* Basic Configuration */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                  Basic Configuration
                </Typography>
              </Grid>
              
              <Grid item xs={12} md={6}>
                <FormControl fullWidth required>
                  <InputLabel>Authentication Method</InputLabel>
                  <Select
                    value={formData.authMethod}
                    onChange={(e) => setFormData({ ...formData, authMethod: e.target.value as AuthMethod })}
                    label="Authentication Method"
                  >
                    {AUTH_METHOD_OPTIONS.map((option) => (
                      <MenuItem key={option.value} value={option.value}>
                        <Box>
                          <Typography variant="body2" fontWeight="medium">
                            {option.label}
                          </Typography>
                          <Typography variant="caption" color="textSecondary">
                            {option.description}
                          </Typography>
                        </Box>
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Description"
                  value={formData.description || ''}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  multiline
                  rows={2}
                />
              </Grid>

              {/* Client Configuration */}
              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Typography variant="h6" gutterBottom>
                  <KeyIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                  Client Configuration
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.includeClientHeaders || false}
                      onChange={(e) => setFormData({ ...formData, includeClientHeaders: e.target.checked })}
                    />
                  }
                  label="Include Client Headers in Outgoing Requests"
                />
              </Grid>

              {formData.includeClientHeaders && (
                <>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Client ID"
                      value={formData.clientId || ''}
                      onChange={(e) => setFormData({ ...formData, clientId: e.target.value })}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Client Secret"
                      type="password"
                      value={formData.clientSecret || ''}
                      onChange={(e) => setFormData({ ...formData, clientSecret: e.target.value })}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Client ID Header Name"
                      value={formData.clientIdHeaderName || 'X-Client-ID'}
                      onChange={(e) => setFormData({ ...formData, clientIdHeaderName: e.target.value })}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Client Secret Header Name"
                      value={formData.clientSecretHeaderName || 'X-Client-Secret'}
                      onChange={(e) => setFormData({ ...formData, clientSecretHeaderName: e.target.value })}
                    />
                  </Grid>
                </>
              )}

              {/* JWS Configuration */}
              {formData.authMethod === AuthMethod.JWS && (
                <>
                  <Grid item xs={12}>
                    <Divider sx={{ my: 2 }} />
                    <Typography variant="h6" gutterBottom>
                      JWS Configuration
                    </Typography>
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <FormControl fullWidth>
                      <InputLabel>JWS Algorithm</InputLabel>
                      <Select
                        value={formData.jwsAlgorithm || 'HS256'}
                        onChange={(e) => setFormData({ ...formData, jwsAlgorithm: e.target.value })}
                        label="JWS Algorithm"
                      >
                        {JWS_ALGORITHM_OPTIONS.map((option) => (
                          <MenuItem key={option.value} value={option.value}>
                            <Box>
                              <Typography variant="body2" fontWeight="medium">
                                {option.label}
                              </Typography>
                              <Typography variant="caption" color="textSecondary">
                                {option.description}
                              </Typography>
                            </Box>
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="JWS Issuer"
                      value={formData.jwsIssuer || ''}
                      onChange={(e) => setFormData({ ...formData, jwsIssuer: e.target.value })}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="JWS Public Key"
                      value={formData.jwsPublicKey || ''}
                      onChange={(e) => setFormData({ ...formData, jwsPublicKey: e.target.value })}
                      multiline
                      rows={3}
                      placeholder="Paste the JWS public key here..."
                    />
                  </Grid>
                </>
              )}

              {/* Endpoint Configuration */}
              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Typography variant="h6" gutterBottom>
                  <HttpIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                  Endpoint Configuration
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Token Endpoint"
                  value={formData.tokenEndpoint || ''}
                  onChange={(e) => setFormData({ ...formData, tokenEndpoint: e.target.value })}
                  placeholder="https://auth.example.com/token"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Public Key Endpoint"
                  value={formData.publicKeyEndpoint || ''}
                  onChange={(e) => setFormData({ ...formData, publicKeyEndpoint: e.target.value })}
                  placeholder="https://auth.example.com/public-key"
                />
              </Grid>

              {/* Header Configuration */}
              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Typography variant="h6" gutterBottom>
                  Header Configuration
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Auth Header Name"
                  value={formData.authHeaderName || 'Authorization'}
                  onChange={(e) => setFormData({ ...formData, authHeaderName: e.target.value })}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Auth Header Prefix"
                  value={formData.authHeaderPrefix || 'Bearer'}
                  onChange={(e) => setFormData({ ...formData, authHeaderPrefix: e.target.value })}
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleDialogClose} disabled={loading}>
              Cancel
            </Button>
            <Button type="submit" variant="contained" disabled={loading}>
              {editingConfig ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default TenantAuthConfigurationComponent;