import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Button,
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
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  LinearProgress,
  Tabs,
  Tab,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Tooltip,
  Badge,
  Switch,
  FormControlLabel,
  FormGroup,
  Checkbox,
  Autocomplete
} from '@mui/material';
import {
  ContentCopy as CloneIcon,
  Download as ExportIcon,
  Upload as ImportIcon,
  History as HistoryIcon,
  Compare as CompareIcon,
  Settings as SettingsIcon,
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Refresh as RefreshIcon,
  Visibility as ViewIcon,
  GetApp as DownloadIcon,
  CloudUpload as UploadIcon,
  Timeline as TimelineIcon,
  Assessment as StatisticsIcon,
  Template as TemplateIcon,
  Rollback as RollbackIcon
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import axios from 'axios';

interface TenantConfiguration {
  id: string;
  tenantId: string;
  version: string;
  name: string;
  description: string;
  isActive: boolean;
  isDefault: boolean;
  environment: string;
  sourceTenantId?: string;
  sourceVersion?: string;
  clonedBy?: string;
  clonedAt?: string;
  configurationData: Record<string, string>;
  metadata: Record<string, string>;
  changeLog?: string;
  createdBy: string;
  updatedBy: string;
  createdAt: string;
  updatedAt: string;
}

interface TenantCloneRequest {
  sourceTenantId: string;
  sourceVersion?: string;
  targetTenantId: string;
  targetEnvironment: string;
  targetVersion?: string;
  name?: string;
  description?: string;
  changeLog?: string;
  clonedBy?: string;
  activateAfterClone?: boolean;
  copyMetadata?: boolean;
  copyConfigurationData?: boolean;
  configurationOverrides?: Record<string, string>;
  metadataOverrides?: Record<string, string>;
  additionalMetadata?: Record<string, string>;
}

interface TenantExportRequest {
  tenantId: string;
  version?: string;
  includeVersions?: string[];
  environment?: string;
  includeConfigurationData?: boolean;
  includeMetadata?: boolean;
  includeHistory?: boolean;
  includeRelatedConfigurations?: boolean;
  exportFormat?: string;
  exportPath?: string;
  compress?: boolean;
  exportedBy?: string;
  exportReason?: string;
}

interface TenantImportRequest {
  importData: string;
  importFormat: string;
  targetTenantId: string;
  targetEnvironment?: string;
  targetVersion?: string;
  name?: string;
  description?: string;
  changeLog?: string;
  importedBy?: string;
  activateAfterImport?: boolean;
  validateBeforeImport?: boolean;
  overwriteExisting?: boolean;
  preserveSourceMetadata?: boolean;
  configurationOverrides?: Record<string, string>;
  metadataOverrides?: Record<string, string>;
  additionalMetadata?: Record<string, string>;
}

const TenantCloningManagement: React.FC = () => {
  const [tenants, setTenants] = useState<string[]>([]);
  const [selectedTenant, setSelectedTenant] = useState<string>('');
  const [tenantVersions, setTenantVersions] = useState<string[]>([]);
  const [tenantHistory, setTenantHistory] = useState<TenantConfiguration[]>([]);
  const [statistics, setStatistics] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [selectedTab, setSelectedTab] = useState(0);
  
  // Dialog states
  const [cloneDialogOpen, setCloneDialogOpen] = useState(false);
  const [exportDialogOpen, setExportDialogOpen] = useState(false);
  const [importDialogOpen, setImportDialogOpen] = useState(false);
  const [compareDialogOpen, setCompareDialogOpen] = useState(false);
  const [templateDialogOpen, setTemplateDialogOpen] = useState(false);
  const [historyDialogOpen, setHistoryDialogOpen] = useState(false);
  
  // Form controls
  const { control: cloneControl, handleSubmit: handleCloneSubmit, reset: resetClone } = useForm<TenantCloneRequest>();
  const { control: exportControl, handleSubmit: handleExportSubmit, reset: resetExport } = useForm<TenantExportRequest>();
  const { control: importControl, handleSubmit: handleImportSubmit, reset: resetImport } = useForm<TenantImportRequest>();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadTenants(),
        loadStatistics()
      ]);
    } catch (error) {
      console.error('Error loading tenant data:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadTenants = async () => {
    try {
      const response = await axios.get('/api/tenant-management/tenants');
      setTenants(response.data);
    } catch (error) {
      console.error('Error loading tenants:', error);
    }
  };

  const loadTenantVersions = async (tenantId: string) => {
    try {
      const response = await axios.get(`/api/tenant-management/tenants/${tenantId}/versions`);
      setTenantVersions(response.data);
    } catch (error) {
      console.error('Error loading tenant versions:', error);
    }
  };

  const loadTenantHistory = async (tenantId: string) => {
    try {
      const response = await axios.get(`/api/tenant-management/tenants/${tenantId}/history`);
      setTenantHistory(response.data);
    } catch (error) {
      console.error('Error loading tenant history:', error);
    }
  };

  const loadStatistics = async () => {
    try {
      const response = await axios.get('/api/tenant-management/statistics');
      setStatistics(response.data);
    } catch (error) {
      console.error('Error loading statistics:', error);
    }
  };

  const handleCloneTenant = async (data: TenantCloneRequest) => {
    try {
      const response = await axios.post('/api/tenant-management/clone', data);
      if (response.data.success) {
        alert('Tenant cloned successfully!');
        setCloneDialogOpen(false);
        resetClone();
        loadData();
      } else {
        alert('Failed to clone tenant: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error cloning tenant:', error);
      alert('Error cloning tenant');
    }
  };

  const handleExportTenant = async (data: TenantExportRequest) => {
    try {
      const response = await axios.post('/api/tenant-management/export', data);
      if (response.data.success) {
        // Download the exported file
        const downloadUrl = response.data.downloadUrl;
        window.open(downloadUrl, '_blank');
        alert('Tenant exported successfully!');
        setExportDialogOpen(false);
        resetExport();
      } else {
        alert('Failed to export tenant: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error exporting tenant:', error);
      alert('Error exporting tenant');
    }
  };

  const handleImportTenant = async (data: TenantImportRequest) => {
    try {
      const response = await axios.post('/api/tenant-management/import', data);
      if (response.data.success) {
        alert('Tenant imported successfully!');
        setImportDialogOpen(false);
        resetImport();
        loadData();
      } else {
        alert('Failed to import tenant: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error importing tenant:', error);
      alert('Error importing tenant');
    }
  };

  const handleFileImport = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);
    formData.append('targetTenantId', 'new-tenant-' + Date.now());
    formData.append('importedBy', 'admin');

    try {
      const response = await axios.post('/api/tenant-management/import-file', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      
      if (response.data.success) {
        alert('Tenant imported from file successfully!');
        loadData();
      } else {
        alert('Failed to import tenant from file: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error importing tenant from file:', error);
      alert('Error importing tenant from file');
    }
  };

  const handleRollbackTenant = async (tenantId: string, version: string) => {
    if (!confirm(`Are you sure you want to rollback ${tenantId} to version ${version}?`)) {
      return;
    }

    try {
      const response = await axios.post(`/api/tenant-management/rollback/${tenantId}/${version}`);
      if (response.data.success) {
        alert('Tenant rolled back successfully!');
        loadData();
      } else {
        alert('Failed to rollback tenant: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error rolling back tenant:', error);
      alert('Error rolling back tenant');
    }
  };

  const handleCreateTemplate = async (tenantId: string, version: string, templateName: string) => {
    try {
      const response = await axios.post('/api/tenant-management/templates', null, {
        params: { tenantId, version, templateName }
      });
      
      if (response.data.success) {
        alert('Template created successfully!');
        setTemplateDialogOpen(false);
      } else {
        alert('Failed to create template: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error creating template:', error);
      alert('Error creating template');
    }
  };

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString();
  };

  const getEnvironmentColor = (environment: string) => {
    switch (environment) {
      case 'DEVELOPMENT': return 'primary';
      case 'INTEGRATION': return 'secondary';
      case 'USER_ACCEPTANCE': return 'warning';
      case 'PRODUCTION': return 'error';
      default: return 'default';
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Tenant Management
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
            startIcon={<CloneIcon />}
            onClick={() => setCloneDialogOpen(true)}
          >
            Clone Tenant
          </Button>
          <Button
            variant="outlined"
            startIcon={<ExportIcon />}
            onClick={() => setExportDialogOpen(true)}
          >
            Export
          </Button>
          <Button
            variant="outlined"
            startIcon={<ImportIcon />}
            onClick={() => setImportDialogOpen(true)}
          >
            Import
          </Button>
          <Button
            variant="outlined"
            startIcon={<StatisticsIcon />}
            onClick={loadStatistics}
          >
            Refresh Stats
          </Button>
        </Box>
      </Box>

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      <Tabs value={selectedTab} onChange={(e, newValue) => setSelectedTab(newValue)} sx={{ mb: 3 }}>
        <Tab label="Tenants Overview" icon={<SettingsIcon />} />
        <Tab label="Statistics" icon={<StatisticsIcon />} />
        <Tab label="Templates" icon={<TemplateIcon />} />
      </Tabs>

      {selectedTab === 0 && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Available Tenants
                </Typography>
                <List>
                  {tenants.map((tenant) => (
                    <ListItem
                      key={tenant}
                      button
                      selected={selectedTenant === tenant}
                      onClick={() => {
                        setSelectedTenant(tenant);
                        loadTenantVersions(tenant);
                        loadTenantHistory(tenant);
                      }}
                    >
                      <ListItemText primary={tenant} />
                      <IconButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          setHistoryDialogOpen(true);
                        }}
                      >
                        <HistoryIcon />
                      </IconButton>
                    </ListItem>
                  ))}
                </List>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={8}>
            {selectedTenant && (
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="h6">
                      {selectedTenant} - Versions
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <Button
                        size="small"
                        startIcon={<CloneIcon />}
                        onClick={() => setCloneDialogOpen(true)}
                      >
                        Clone
                      </Button>
                      <Button
                        size="small"
                        startIcon={<ExportIcon />}
                        onClick={() => setExportDialogOpen(true)}
                      >
                        Export
                      </Button>
                      <Button
                        size="small"
                        startIcon={<TemplateIcon />}
                        onClick={() => setTemplateDialogOpen(true)}
                      >
                        Create Template
                      </Button>
                    </Box>
                  </Box>

                  <TableContainer component={Paper}>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>Version</TableCell>
                          <TableCell>Environment</TableCell>
                          <TableCell>Status</TableCell>
                          <TableCell>Created</TableCell>
                          <TableCell>Actions</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {tenantHistory.map((config) => (
                          <TableRow key={config.id}>
                            <TableCell>{config.version}</TableCell>
                            <TableCell>
                              <Chip
                                label={config.environment}
                                color={getEnvironmentColor(config.environment) as any}
                                size="small"
                              />
                            </TableCell>
                            <TableCell>
                              <Chip
                                label={config.isActive ? 'ACTIVE' : 'INACTIVE'}
                                color={config.isActive ? 'success' : 'default'}
                                size="small"
                              />
                            </TableCell>
                            <TableCell>{formatDateTime(config.createdAt)}</TableCell>
                            <TableCell>
                              <Box sx={{ display: 'flex', gap: 1 }}>
                                <Tooltip title="View Details">
                                  <IconButton size="small">
                                    <ViewIcon />
                                  </IconButton>
                                </Tooltip>
                                <Tooltip title="Compare">
                                  <IconButton size="small">
                                    <CompareIcon />
                                  </IconButton>
                                </Tooltip>
                                {!config.isActive && (
                                  <Tooltip title="Rollback">
                                    <IconButton
                                      size="small"
                                      onClick={() => handleRollbackTenant(config.tenantId, config.version)}
                                    >
                                      <RollbackIcon />
                                    </IconButton>
                                  </Tooltip>
                                )}
                              </Box>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </CardContent>
              </Card>
            )}
          </Grid>
        </Grid>
      )}

      {selectedTab === 1 && statistics && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Configuration Statistics
                </Typography>
                <List>
                  <ListItem>
                    <ListItemText
                      primary="Total Configurations"
                      secondary={statistics.totalConfigurations}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="Template Tenants"
                      secondary={statistics.templateTenantCount}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="Recent Configurations (1 week)"
                      secondary={statistics.recentConfigurations}
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Configurations by Environment
                </Typography>
                <List>
                  {Object.entries(statistics.configurationsByEnvironment || {}).map(([env, count]) => (
                    <ListItem key={env}>
                      <ListItemText
                        primary={env}
                        secondary={`${count} configurations`}
                      />
                    </ListItem>
                  ))}
                </List>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {selectedTab === 2 && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Template Management
            </Typography>
            <Alert severity="info">
              Templates allow you to create reusable tenant configurations that can be applied to new tenants.
            </Alert>
            <Box sx={{ mt: 2 }}>
              <Button
                variant="contained"
                startIcon={<TemplateIcon />}
                onClick={() => setTemplateDialogOpen(true)}
              >
                Create New Template
              </Button>
            </Box>
          </CardContent>
        </Card>
      )}

      {/* Clone Dialog */}
      <Dialog open={cloneDialogOpen} onClose={() => setCloneDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Clone Tenant Configuration</DialogTitle>
        <form onSubmit={handleCloneSubmit(handleCloneTenant)}>
          <DialogContent>
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} md={6}>
                <Controller
                  name="sourceTenantId"
                  control={cloneControl}
                  defaultValue=""
                  render={({ field }) => (
                    <Autocomplete
                      {...field}
                      options={tenants}
                      renderInput={(params) => (
                        <TextField {...params} label="Source Tenant" required />
                      )}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="targetTenantId"
                  control={cloneControl}
                  defaultValue=""
                  render={({ field }) => (
                    <TextField {...field} label="Target Tenant" required fullWidth />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="targetEnvironment"
                  control={cloneControl}
                  defaultValue="DEVELOPMENT"
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Target Environment</InputLabel>
                      <Select {...field} label="Target Environment">
                        <MenuItem value="DEVELOPMENT">Development</MenuItem>
                        <MenuItem value="INTEGRATION">Integration Testing</MenuItem>
                        <MenuItem value="USER_ACCEPTANCE">User Acceptance Testing</MenuItem>
                        <MenuItem value="PRODUCTION">Production</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="sourceVersion"
                  control={cloneControl}
                  defaultValue=""
                  render={({ field }) => (
                    <TextField {...field} label="Source Version (optional)" fullWidth />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="description"
                  control={cloneControl}
                  defaultValue=""
                  render={({ field }) => (
                    <TextField {...field} label="Description" fullWidth multiline rows={3} />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="changeLog"
                  control={cloneControl}
                  defaultValue=""
                  render={({ field }) => (
                    <TextField {...field} label="Change Log" fullWidth multiline rows={2} />
                  )}
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setCloneDialogOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained">Clone Tenant</Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Export Dialog */}
      <Dialog open={exportDialogOpen} onClose={() => setExportDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Export Tenant Configuration</DialogTitle>
        <form onSubmit={handleExportSubmit(handleExportTenant)}>
          <DialogContent>
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} md={6}>
                <Controller
                  name="tenantId"
                  control={exportControl}
                  defaultValue={selectedTenant}
                  render={({ field }) => (
                    <Autocomplete
                      {...field}
                      options={tenants}
                      renderInput={(params) => (
                        <TextField {...params} label="Tenant ID" required />
                      )}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Controller
                  name="exportFormat"
                  control={exportControl}
                  defaultValue="JSON"
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Export Format</InputLabel>
                      <Select {...field} label="Export Format">
                        <MenuItem value="JSON">JSON</MenuItem>
                        <MenuItem value="YAML">YAML</MenuItem>
                        <MenuItem value="XML">XML</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="exportReason"
                  control={exportControl}
                  defaultValue=""
                  render={({ field }) => (
                    <TextField {...field} label="Export Reason" fullWidth />
                  )}
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setExportDialogOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained">Export Tenant</Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Import Dialog */}
      <Dialog open={importDialogOpen} onClose={() => setImportDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Import Tenant Configuration</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <Typography variant="h6" gutterBottom>
              Import from File
            </Typography>
            <input
              type="file"
              accept=".json,.yaml,.yml,.xml"
              onChange={handleFileImport}
              style={{ marginBottom: 16 }}
            />
          </Box>
          <Divider sx={{ my: 2 }} />
          <Typography variant="h6" gutterBottom>
            Import from Data
          </Typography>
          <form onSubmit={handleImportSubmit(handleImportTenant)}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Controller
                  name="targetTenantId"
                  control={importControl}
                  defaultValue=""
                  render={({ field }) => (
                    <TextField {...field} label="Target Tenant ID" required fullWidth />
                  )}
                />
              </Grid>
              <Grid item xs={12}>
                <Controller
                  name="importData"
                  control={importControl}
                  defaultValue=""
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Import Data (JSON/YAML/XML)"
                      fullWidth
                      multiline
                      rows={10}
                      required
                    />
                  )}
                />
              </Grid>
            </Grid>
            <DialogActions>
              <Button onClick={() => setImportDialogOpen(false)}>Cancel</Button>
              <Button type="submit" variant="contained">Import Tenant</Button>
            </DialogActions>
          </form>
        </DialogContent>
      </Dialog>

      {/* Template Dialog */}
      <Dialog open={templateDialogOpen} onClose={() => setTemplateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create Template</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                label="Template Name"
                fullWidth
                id="templateName"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Autocomplete
                options={tenants}
                renderInput={(params) => (
                  <TextField {...params} label="Source Tenant" />
                )}
                id="templateSourceTenant"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Source Version"
                fullWidth
                id="templateSourceVersion"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTemplateDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={() => {
              const templateName = (document.getElementById('templateName') as HTMLInputElement)?.value;
              const sourceTenant = (document.getElementById('templateSourceTenant') as HTMLInputElement)?.value;
              const sourceVersion = (document.getElementById('templateSourceVersion') as HTMLInputElement)?.value;
              
              if (templateName && sourceTenant && sourceVersion) {
                handleCreateTemplate(sourceTenant, sourceVersion, templateName);
              }
            }}
          >
            Create Template
          </Button>
        </DialogActions>
      </Dialog>

      {/* History Dialog */}
      <Dialog open={historyDialogOpen} onClose={() => setHistoryDialogOpen(false)} maxWidth="lg" fullWidth>
        <DialogTitle>Tenant History - {selectedTenant}</DialogTitle>
        <DialogContent>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Version</TableCell>
                  <TableCell>Environment</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Created By</TableCell>
                  <TableCell>Created At</TableCell>
                  <TableCell>Change Log</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {tenantHistory.map((config) => (
                  <TableRow key={config.id}>
                    <TableCell>{config.version}</TableCell>
                    <TableCell>
                      <Chip
                        label={config.environment}
                        color={getEnvironmentColor(config.environment) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={config.isActive ? 'ACTIVE' : 'INACTIVE'}
                        color={config.isActive ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{config.createdBy}</TableCell>
                    <TableCell>{formatDateTime(config.createdAt)}</TableCell>
                    <TableCell>{config.changeLog || '-'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setHistoryDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TenantCloningManagement;