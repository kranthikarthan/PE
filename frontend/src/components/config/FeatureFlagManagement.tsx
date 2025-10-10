import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Alert,
  CircularProgress,
  Tooltip,
  Grid,
  Switch,
  FormControlLabel,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  ToggleOn as ToggleOnIcon,
  ToggleOff as ToggleOffIcon,
  Refresh as RefreshIcon,
  Flag as FlagIcon,
} from '@mui/icons-material';
import { configApi, FeatureFlag, FeatureFlagRequest, Tenant } from '../../services/configApi';

const FeatureFlagManagement: React.FC = () => {
  const [featureFlags, setFeatureFlags] = useState<FeatureFlag[]>([]);
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingFlag, setEditingFlag] = useState<FeatureFlag | null>(null);
  const [formData, setFormData] = useState<FeatureFlagRequest>({
    flagName: '',
    flagDescription: '',
    flagValue: false,
    tenantId: '',
    environment: 'PRODUCTION',
    rolloutPercentage: 0,
    targetUsers: '',
  });

  useEffect(() => {
    loadFeatureFlags();
    loadTenants();
  }, []);

  const loadFeatureFlags = async () => {
    try {
      setLoading(true);
      const flagList = await configApi.getFeatureFlags();
      setFeatureFlags(flagList);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load feature flags');
    } finally {
      setLoading(false);
    }
  };

  const loadTenants = async () => {
    try {
      const tenantList = await configApi.getTenants();
      setTenants(tenantList);
    } catch (err: any) {
      console.error('Failed to load tenants:', err);
    }
  };

  const handleOpenDialog = (flag?: FeatureFlag) => {
    if (flag) {
      setEditingFlag(flag);
      setFormData({
        flagName: flag.flagName,
        flagDescription: flag.flagDescription || '',
        flagValue: flag.flagValue,
        tenantId: flag.tenantId || '',
        environment: flag.environment,
        rolloutPercentage: flag.rolloutPercentage,
        targetUsers: flag.targetUsers || '',
      });
    } else {
      setEditingFlag(null);
      setFormData({
        flagName: '',
        flagDescription: '',
        flagValue: false,
        tenantId: '',
        environment: 'PRODUCTION',
        rolloutPercentage: 0,
        targetUsers: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingFlag(null);
    setFormData({
      flagName: '',
      flagDescription: '',
      flagValue: false,
      tenantId: '',
      environment: 'PRODUCTION',
      rolloutPercentage: 0,
      targetUsers: '',
    });
  };

  const handleSubmit = async () => {
    try {
      if (editingFlag) {
        await configApi.updateFeatureFlag(editingFlag.flagName, formData);
      } else {
        await configApi.createFeatureFlag(formData);
      }
      handleCloseDialog();
      loadFeatureFlags();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save feature flag');
    }
  };

  const handleToggleFlag = async (flagName: string) => {
    try {
      await configApi.toggleFeatureFlag(flagName);
      loadFeatureFlags();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to toggle feature flag');
    }
  };

  const handleDeleteFlag = async (flagName: string) => {
    if (window.confirm('Are you sure you want to delete this feature flag?')) {
      try {
        await configApi.deleteFeatureFlag(flagName);
        loadFeatureFlags();
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to delete feature flag');
      }
    }
  };

  const getEnvironmentColor = (environment: string) => {
    switch (environment) {
      case 'PRODUCTION':
        return 'error';
      case 'STAGING':
        return 'warning';
      case 'TESTING':
        return 'info';
      case 'DEVELOPMENT':
        return 'success';
      default:
        return 'default';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Card>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Typography variant="h5" component="h1">
              Feature Flag Management
            </Typography>
            <Box>
              <Button
                variant="outlined"
                startIcon={<RefreshIcon />}
                onClick={loadFeatureFlags}
                sx={{ mr: 1 }}
              >
                Refresh
              </Button>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => handleOpenDialog()}
              >
                Add Feature Flag
              </Button>
            </Box>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
              {error}
            </Alert>
          )}

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Flag Name</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>Value</TableCell>
                  <TableCell>Environment</TableCell>
                  <TableCell>Tenant</TableCell>
                  <TableCell>Rollout %</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Created</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {featureFlags.map((flag) => (
                  <TableRow key={flag.id}>
                    <TableCell>
                      <Box display="flex" alignItems="center">
                        <FlagIcon sx={{ mr: 1, color: 'primary.main' }} />
                        {flag.flagName}
                      </Box>
                    </TableCell>
                    <TableCell>{flag.flagDescription || '-'}</TableCell>
                    <TableCell>
                      <Chip
                        label={flag.flagValue ? 'Enabled' : 'Disabled'}
                        color={flag.flagValue ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={flag.environment}
                        color={getEnvironmentColor(flag.environment) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      {flag.tenantId ? (
                        tenants.find(t => t.id === flag.tenantId)?.name || flag.tenantId
                      ) : (
                        'Global'
                      )}
                    </TableCell>
                    <TableCell>{flag.rolloutPercentage}%</TableCell>
                    <TableCell>
                      <Chip
                        label={flag.isActive ? 'Active' : 'Inactive'}
                        color={flag.isActive ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{formatDate(flag.createdAt)}</TableCell>
                    <TableCell>
                      <Tooltip title="Edit Feature Flag">
                        <IconButton
                          size="small"
                          onClick={() => handleOpenDialog(flag)}
                        >
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
                      
                      <Tooltip title={flag.flagValue ? 'Disable Flag' : 'Enable Flag'}>
                        <IconButton
                          size="small"
                          onClick={() => handleToggleFlag(flag.flagName)}
                        >
                          {flag.flagValue ? <ToggleOffIcon /> : <ToggleOnIcon />}
                        </IconButton>
                      </Tooltip>
                      
                      <Tooltip title="Delete Feature Flag">
                        <IconButton
                          size="small"
                          onClick={() => handleDeleteFlag(flag.flagName)}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Feature Flag Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingFlag ? 'Edit Feature Flag' : 'Add New Feature Flag'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 1 }}>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Flag Name"
                  value={formData.flagName}
                  onChange={(e) => setFormData({ ...formData, flagName: e.target.value })}
                  margin="normal"
                  required
                  disabled={!!editingFlag}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Environment"
                  value={formData.environment}
                  onChange={(e) => setFormData({ ...formData, environment: e.target.value as any })}
                  margin="normal"
                >
                  <MenuItem value="DEVELOPMENT">Development</MenuItem>
                  <MenuItem value="TESTING">Testing</MenuItem>
                  <MenuItem value="STAGING">Staging</MenuItem>
                  <MenuItem value="PRODUCTION">Production</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Description"
                  multiline
                  rows={2}
                  value={formData.flagDescription}
                  onChange={(e) => setFormData({ ...formData, flagDescription: e.target.value })}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Tenant"
                  value={formData.tenantId}
                  onChange={(e) => setFormData({ ...formData, tenantId: e.target.value })}
                  margin="normal"
                >
                  <MenuItem value="">Global</MenuItem>
                  {tenants.map((tenant) => (
                    <MenuItem key={tenant.id} value={tenant.id}>
                      {tenant.name}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Rollout Percentage"
                  type="number"
                  value={formData.rolloutPercentage}
                  onChange={(e) => setFormData({ ...formData, rolloutPercentage: parseInt(e.target.value) || 0 })}
                  margin="normal"
                  inputProps={{ min: 0, max: 100 }}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Target Users"
                  multiline
                  rows={2}
                  value={formData.targetUsers}
                  onChange={(e) => setFormData({ ...formData, targetUsers: e.target.value })}
                  margin="normal"
                  placeholder="Comma-separated list of user IDs or email addresses"
                />
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.flagValue}
                      onChange={(e) => setFormData({ ...formData, flagValue: e.target.checked })}
                    />
                  }
                  label="Enable Feature Flag"
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSubmit} variant="contained">
            {editingFlag ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default FeatureFlagManagement;