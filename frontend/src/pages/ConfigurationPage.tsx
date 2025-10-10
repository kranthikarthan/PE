import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Tabs,
  Tab,
  Grid,
  Card,
  CardContent,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Switch,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Chip,
  Alert,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Settings as SettingsIcon,
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Refresh as RefreshIcon,
  Security as SecurityIcon,
  Speed as SpeedIcon,
  Flag as FlagIcon,
  Topic as TopicIcon,
  Api as ApiIcon,
  AccountTree as AccountTreeIcon,
  SwapHoriz as SwapHorizIcon,
} from '@mui/icons-material';
import PaymentResponseConfigManager from '../components/configuration/PaymentResponseConfigManager';
import SchemeConfigManager from '../components/configuration/SchemeConfigManager';
import ClearingSystemManager from '../components/configuration/ClearingSystemManager';
import MessageFlowConfig from '../components/configuration/MessageFlowConfig';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`config-tabpanel-${index}`}
    >
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
};

interface ConfigItem {
  key: string;
  value: string;
  category: string;
  description?: string;
  type: 'string' | 'number' | 'boolean' | 'json';
  sensitive?: boolean;
}

interface FeatureFlag {
  name: string;
  enabled: boolean;
  description: string;
  rolloutPercentage: number;
  environment: string;
}

interface RateLimit {
  endpoint: string;
  method: string;
  requestsPerMinute: number;
  burstCapacity: number;
  windowSizeSeconds: number;
}

const ConfigurationPage: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [currentTenant, setCurrentTenant] = useState('default');
  const [configurations, setConfigurations] = useState<ConfigItem[]>([]);
  const [featureFlags, setFeatureFlags] = useState<FeatureFlag[]>([]);
  const [rateLimits, setRateLimits] = useState<RateLimit[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<any>(null);

  useEffect(() => {
    loadConfigurations();
    loadFeatureFlags();
    loadRateLimits();
  }, [currentTenant]);

  const loadConfigurations = async () => {
    setLoading(true);
    try {
      // Mock configuration data
      const mockConfigs: ConfigItem[] = [
        {
          key: 'payment.default_currency',
          value: 'USD',
          category: 'Payment Processing',
          description: 'Default currency for payments',
          type: 'string',
        },
        {
          key: 'payment.max_daily_limit',
          value: '50000.00',
          category: 'Payment Processing',
          description: 'Maximum daily transaction limit per account',
          type: 'number',
        },
        {
          key: 'security.jwt_expiry_minutes',
          value: '60',
          category: 'Security',
          description: 'JWT token expiry time in minutes',
          type: 'number',
        },
        {
          key: 'kafka.batch_size',
          value: '16384',
          category: 'Messaging',
          description: 'Kafka producer batch size',
          type: 'number',
        },
        {
          key: 'iso20022.validation_strict',
          value: 'true',
          category: 'ISO 20022',
          description: 'Enable strict ISO 20022 message validation',
          type: 'boolean',
        },
      ];
      
      setConfigurations(mockConfigs);
    } catch (err: any) {
      setError(err.message || 'Failed to load configurations');
    } finally {
      setLoading(false);
    }
  };

  const loadFeatureFlags = async () => {
    try {
      // Mock feature flag data
      const mockFlags: FeatureFlag[] = [
        {
          name: 'iso20022_processing',
          enabled: true,
          description: 'Enable ISO 20022 message processing',
          rolloutPercentage: 100,
          environment: 'production',
        },
        {
          name: 'bulk_processing',
          enabled: true,
          description: 'Enable bulk payment processing',
          rolloutPercentage: 100,
          environment: 'production',
        },
        {
          name: 'advanced_fraud_detection',
          enabled: false,
          description: 'Enable ML-based fraud detection',
          rolloutPercentage: 0,
          environment: 'production',
        },
        {
          name: 'real_time_notifications',
          enabled: true,
          description: 'Enable real-time customer notifications',
          rolloutPercentage: 75,
          environment: 'production',
        },
      ];
      
      setFeatureFlags(mockFlags);
    } catch (err: any) {
      setError(err.message || 'Failed to load feature flags');
    }
  };

  const loadRateLimits = async () => {
    try {
      // Mock rate limit data
      const mockLimits: RateLimit[] = [
        {
          endpoint: '/api/v1/transactions',
          method: 'POST',
          requestsPerMinute: 100,
          burstCapacity: 150,
          windowSizeSeconds: 60,
        },
        {
          endpoint: '/api/v1/iso20022/pain001',
          method: 'POST',
          requestsPerMinute: 100,
          burstCapacity: 150,
          windowSizeSeconds: 60,
        },
        {
          endpoint: '/api/v1/iso20022/camt055',
          method: 'POST',
          requestsPerMinute: 50,
          burstCapacity: 75,
          windowSizeSeconds: 60,
        },
        {
          endpoint: '/api/v1/accounts/*',
          method: 'GET',
          requestsPerMinute: 1000,
          burstCapacity: 1500,
          windowSizeSeconds: 60,
        },
      ];
      
      setRateLimits(mockLimits);
    } catch (err: any) {
      setError(err.message || 'Failed to load rate limits');
    }
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleEditConfiguration = (config: ConfigItem) => {
    setEditingItem(config);
    setEditDialogOpen(true);
  };

  const handleToggleFeatureFlag = async (flagName: string, enabled: boolean) => {
    try {
      // API call to toggle feature flag
      console.log(`Toggling feature flag ${flagName} to ${enabled}`);
      
      // Update local state
      setFeatureFlags(prev => 
        prev.map(flag => 
          flag.name === flagName ? { ...flag, enabled } : flag
        )
      );
    } catch (err: any) {
      setError(err.message || 'Failed to update feature flag');
    }
  };

  const getConfigValueDisplay = (config: ConfigItem) => {
    if (config.sensitive) {
      return '••••••••';
    }
    
    if (config.type === 'boolean') {
      return config.value === 'true' ? 'Enabled' : 'Disabled';
    }
    
    if (config.type === 'json') {
      return 'JSON Object';
    }
    
    return config.value;
  };

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'Payment Processing': return 'primary';
      case 'Security': return 'error';
      case 'Messaging': return 'info';
      case 'ISO 20022': return 'success';
      default: return 'default';
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">
          System Configuration
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Tenant</InputLabel>
            <Select
              value={currentTenant}
              label="Tenant"
              onChange={(e) => setCurrentTenant(e.target.value)}
            >
              <MenuItem value="default">Default</MenuItem>
              <MenuItem value="demo-bank">Demo Bank</MenuItem>
              <MenuItem value="fintech-corp">FinTech Corp</MenuItem>
            </Select>
          </FormControl>
          
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={() => {
              loadConfigurations();
              loadFeatureFlags();
              loadRateLimits();
            }}
          >
            Refresh
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={handleTabChange}>
            <Tab
              label="System Configuration"
              icon={<SettingsIcon />}
              iconPosition="start"
            />
            <Tab
              label="Feature Flags"
              icon={<FlagIcon />}
              iconPosition="start"
            />
            <Tab
              label="Rate Limits"
              icon={<SpeedIcon />}
              iconPosition="start"
            />
            <Tab
              label="Response Configuration"
              icon={<TopicIcon />}
              iconPosition="start"
            />
            <Tab
              label="Scheme Configuration"
              icon={<ApiIcon />}
              iconPosition="start"
            />
            <Tab
              label="Clearing Systems"
              icon={<AccountTreeIcon />}
              iconPosition="start"
            />
            <Tab
              label="Message Flows"
              icon={<SwapHorizIcon />}
              iconPosition="start"
            />
            <Tab
              label="Security Settings"
              icon={<SecurityIcon />}
              iconPosition="start"
            />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={0}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6">
                System Configuration - {currentTenant}
              </Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => {
                  setEditingItem(null);
                  setEditDialogOpen(true);
                }}
              >
                Add Configuration
              </Button>
            </Box>

            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Key</TableCell>
                    <TableCell>Value</TableCell>
                    <TableCell>Category</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {configurations.map((config) => (
                    <TableRow key={config.key}>
                      <TableCell>
                        <Typography variant="body2" fontFamily="monospace">
                          {config.key}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {getConfigValueDisplay(config)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={config.category}
                          color={getCategoryColor(config.category) as any}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">
                          {config.description}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Tooltip title="Edit">
                          <IconButton
                            size="small"
                            onClick={() => handleEditConfiguration(config)}
                          >
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6">
                Feature Flags - {currentTenant}
              </Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => {
                  // Add new feature flag
                }}
              >
                Add Feature Flag
              </Button>
            </Box>

            <Grid container spacing={3}>
              {featureFlags.map((flag) => (
                <Grid item xs={12} md={6} key={flag.name}>
                  <Card variant="outlined">
                    <CardContent>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                        <Box>
                          <Typography variant="h6" gutterBottom>
                            {flag.name.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {flag.description}
                          </Typography>
                        </Box>
                        <Switch
                          checked={flag.enabled}
                          onChange={(e) => handleToggleFeatureFlag(flag.name, e.target.checked)}
                        />
                      </Box>
                      
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        <Chip
                          label={flag.enabled ? 'Enabled' : 'Disabled'}
                          color={flag.enabled ? 'success' : 'default'}
                          size="small"
                        />
                        <Chip
                          label={`${flag.rolloutPercentage}% rollout`}
                          color="info"
                          size="small"
                        />
                        <Chip
                          label={flag.environment}
                          color="secondary"
                          size="small"
                        />
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </CardContent>
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6">
                Rate Limits - {currentTenant}
              </Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => {
                  // Add new rate limit
                }}
              >
                Add Rate Limit
              </Button>
            </Box>

            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Endpoint</TableCell>
                    <TableCell>Method</TableCell>
                    <TableCell>Requests/Min</TableCell>
                    <TableCell>Burst Capacity</TableCell>
                    <TableCell>Window (sec)</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {rateLimits.map((limit, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        <Typography variant="body2" fontFamily="monospace">
                          {limit.endpoint}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip label={limit.method} size="small" />
                      </TableCell>
                      <TableCell>{limit.requestsPerMinute}</TableCell>
                      <TableCell>{limit.burstCapacity}</TableCell>
                      <TableCell>{limit.windowSizeSeconds}</TableCell>
                      <TableCell>
                        <Tooltip title="Edit">
                          <IconButton size="small">
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete">
                          <IconButton size="small" color="error">
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
        </TabPanel>

        <TabPanel value={tabValue} index={3}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Payment Response Configuration - {currentTenant}
            </Typography>
            
            <PaymentResponseConfigManager
              tenantId={currentTenant}
              onConfigChange={(config) => {
                console.log('Response configuration changed:', config);
              }}
            />
          </CardContent>
        </TabPanel>

        <TabPanel value={tabValue} index={4}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Scheme Configuration - {currentTenant}
            </Typography>
            
            <SchemeConfigManager />
          </CardContent>
        </TabPanel>

        <TabPanel value={tabValue} index={5}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Clearing Systems - {currentTenant}
            </Typography>
            
            <ClearingSystemManager />
          </CardContent>
        </TabPanel>

        <TabPanel value={tabValue} index={6}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Message Flows - {currentTenant}
            </Typography>
            
            <MessageFlowConfig 
              tenantId={currentTenant}
              paymentType="RTP"
              localInstrumentCode="RTP"
            />
          </CardContent>
        </TabPanel>

        <TabPanel value={tabValue} index={7}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Security Settings - {currentTenant}
            </Typography>
            
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Authentication
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="body2">JWT Token Expiry</Typography>
                        <Typography variant="body2" fontFamily="monospace">60 minutes</Typography>
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="body2">Max Login Attempts</Typography>
                        <Typography variant="body2" fontFamily="monospace">5</Typography>
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="body2">Account Lockout Duration</Typography>
                        <Typography variant="body2" fontFamily="monospace">30 minutes</Typography>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} md={6}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Transaction Security
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="body2">Fraud Detection</Typography>
                        <Switch checked={false} />
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="body2">High Value Monitoring</Typography>
                        <Switch checked={true} />
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="body2">Velocity Checking</Typography>
                        <Switch checked={true} />
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </CardContent>
        </TabPanel>
      </Card>

      {/* Edit Configuration Dialog */}
      <Dialog
        open={editDialogOpen}
        onClose={() => setEditDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {editingItem ? 'Edit Configuration' : 'Add Configuration'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                label="Configuration Key"
                fullWidth
                defaultValue={editingItem?.key || ''}
                placeholder="payment.max_amount"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Value"
                fullWidth
                defaultValue={editingItem?.value || ''}
                placeholder="10000.00"
              />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Category</InputLabel>
                <Select
                  defaultValue={editingItem?.category || ''}
                  label="Category"
                >
                  <MenuItem value="Payment Processing">Payment Processing</MenuItem>
                  <MenuItem value="Security">Security</MenuItem>
                  <MenuItem value="Messaging">Messaging</MenuItem>
                  <MenuItem value="ISO 20022">ISO 20022</MenuItem>
                  <MenuItem value="Monitoring">Monitoring</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Description"
                fullWidth
                multiline
                rows={3}
                defaultValue={editingItem?.description || ''}
                placeholder="Description of what this configuration controls"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => setEditDialogOpen(false)}>
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ConfigurationPage;