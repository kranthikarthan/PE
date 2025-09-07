import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Alert,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Tooltip,
  Badge,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Switch,
  FormControlLabel,
  Autocomplete,
} from '@mui/material';
import {
  Transform as TransformIcon,
  Code as CodeIcon,
  Settings as SettingsIcon,
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  Refresh as RefreshIcon,
  Search as SearchIcon,
  FilterList as FilterIcon,
  Assessment as AssessmentIcon,
  Schema as SchemaIcon,
  Functions as FunctionsIcon,
  AutoAwesome as AutoAwesomeIcon,
  Conditional as ConditionalIcon,
  Assignment as AssignmentIcon,
  ExpandMore as ExpandMoreIcon,
  PlayArrow as PlayIcon,
  Stop as StopIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
} from '@mui/icons-material';
import { useForm, Controller, useFieldArray } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import JSONEditor from 'react-json-editor-ajrm';
import locale from 'react-json-editor-ajrm/locale/en';

// Types
interface AdvancedPayloadMapping {
  id?: string;
  mappingName: string;
  tenantId: string;
  paymentType?: string;
  localInstrumentationCode?: string;
  clearingSystemCode?: string;
  mappingType: string;
  direction: string;
  sourceSchema?: any;
  targetSchema?: any;
  fieldMappings?: any;
  valueAssignments?: any;
  conditionalMappings?: any;
  derivedValueRules?: any;
  autoGenerationRules?: any;
  transformationRules?: any;
  validationRules?: any;
  defaultValues?: any;
  arrayHandlingConfig?: any;
  nestedObjectConfig?: any;
  errorHandlingConfig?: any;
  performanceConfig?: any;
  version: string;
  priority: number;
  isActive: boolean;
  description?: string;
}

interface MappingStatistics {
  totalMappings: number;
  fieldMappings: number;
  valueAssignmentMappings: number;
  derivedValueMappings: number;
  autoGenerationMappings: number;
  conditionalMappings: number;
  transformationMappings: number;
  requestMappings: number;
  responseMappings: number;
  bidirectionalMappings: number;
  paymentTypeSpecific: number;
  instrumentSpecific: number;
  clearingSystemSpecific: number;
  averagePriority: number;
  lastMappingCreated?: string;
  lastMappingUpdated?: string;
}

// Validation schema
const mappingSchema = yup.object({
  mappingName: yup.string().required('Mapping name is required'),
  tenantId: yup.string().required('Tenant ID is required'),
  mappingType: yup.string().required('Mapping type is required'),
  direction: yup.string().required('Direction is required'),
  version: yup.string().required('Version is required'),
  priority: yup.number().min(1).max(100),
});

const AdvancedPayloadMapping: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [mappings, setMappings] = useState<AdvancedPayloadMapping[]>([]);
  const [statistics, setStatistics] = useState<MappingStatistics | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [mappingDialogOpen, setMappingDialogOpen] = useState(false);
  const [editingMapping, setEditingMapping] = useState<AdvancedPayloadMapping | null>(null);
  const [filterType, setFilterType] = useState<string>('all');
  const [filterDirection, setFilterDirection] = useState<string>('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedTenant, setSelectedTenant] = useState<string>('tenant1');

  const {
    control,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isDirty },
  } = useForm<AdvancedPayloadMapping>({
    resolver: yupResolver(mappingSchema),
    defaultValues: {
      mappingType: 'FIELD_MAPPING',
      direction: 'REQUEST',
      version: '1.0',
      priority: 1,
      isActive: true,
    },
  });

  // Load data on component mount
  useEffect(() => {
    loadMappings();
    loadStatistics();
  }, [selectedTenant, filterType, filterDirection, searchTerm]);

  const loadMappings = async () => {
    try {
      setLoading(true);
      // Mock data for now
      const mockMappings: AdvancedPayloadMapping[] = [
        {
          id: '1',
          mappingName: 'PAIN001_TO_PACS008_MAPPING',
          tenantId: 'tenant1',
          paymentType: 'TRANSFER',
          localInstrumentationCode: 'LOCAL_INSTRUMENT_001',
          mappingType: 'TRANSFORMATION_MAPPING',
          direction: 'REQUEST',
          fieldMappings: {
            transactionReference: 'transactionReference',
            fromAccountNumber: 'fromAccountNumber',
            toAccountNumber: 'toAccountNumber',
            amount: 'amount',
            currency: 'currency',
          },
          valueAssignments: {
            messageId: 'PAIN001-{{uuid()}}',
            creationDateTime: '{{timestamp()}}',
            messageType: 'pain.001',
            version: '2013',
            source: 'payment-engine',
          },
          derivedValueRules: {
            totalAmount: {
              expression: '${source.amount}',
              type: 'NUMBER',
            },
            formattedAmount: {
              expression: '${source.amount} * 100',
              type: 'NUMBER',
            },
          },
          autoGenerationRules: {
            messageId: { type: 'UUID' },
            creationDateTime: { type: 'TIMESTAMP' },
            transactionId: {
              type: 'SEQUENTIAL',
              prefix: 'TXN-',
              suffix: '-PAIN001',
              length: 15,
            },
          },
          conditionalMappings: {
            'paymentType == "TRANSFER"': {
              target: 'paymentTypeCode',
              source: 'TRA',
            },
            'amount > 10000': {
              target: 'requiresApproval',
              source: 'true',
            },
          },
          transformationRules: {
            transactionReference: 'uppercase',
            fromAccountNumber: 'uppercase',
            toAccountNumber: 'uppercase',
            currency: 'uppercase',
          },
          defaultValues: {
            processingMode: 'IMMEDIATE',
            priority: 'NORMAL',
            channel: 'API',
          },
          version: '1.0',
          priority: 1,
          isActive: true,
          description: 'Advanced mapping for PAIN.001 to PACS.008 transformation',
        },
        {
          id: '2',
          mappingName: 'CLEARING_SYSTEM_MAPPING',
          tenantId: 'tenant1',
          paymentType: 'TRANSFER',
          localInstrumentationCode: 'LOCAL_INSTRUMENT_002',
          mappingType: 'VALUE_ASSIGNMENT_MAPPING',
          direction: 'REQUEST',
          fieldMappings: {
            transactionReference: 'transactionReference',
            amount: 'amount',
            currency: 'currency',
          },
          valueAssignments: {
            clearingSystemCode: 'CLEARING_001',
            routingCode: 'ROUTE_001',
            institutionId: 'INST_001',
            messageFormat: 'ISO20022',
            protocol: 'REST',
            endpoint: '/api/v1/clearing/process',
          },
          derivedValueRules: {
            clearingReference: {
              expression: 'CLEARING_001-${source.transactionReference}',
              type: 'STRING',
            },
            routingInfo: {
              expression: '${source.currency}-${source.amount}',
              type: 'STRING',
            },
          },
          autoGenerationRules: {
            clearingId: { type: 'UUID' },
            timestamp: { type: 'TIMESTAMP' },
          },
          conditionalMappings: {
            'currency == "USD"': {
              target: 'clearingSystemCode',
              source: 'CLEARING_USD',
            },
            'currency == "EUR"': {
              target: 'clearingSystemCode',
              source: 'CLEARING_EUR',
            },
          },
          transformationRules: {
            transactionReference: 'uppercase',
            currency: 'uppercase',
          },
          defaultValues: {
            timeout: 30000,
            retryAttempts: 3,
          },
          version: '1.0',
          priority: 2,
          isActive: true,
          description: 'Clearing system specific mapping with value assignments',
        },
      ];
      
      setMappings(mockMappings);
    } catch (err) {
      setError('Failed to load mappings');
    } finally {
      setLoading(false);
    }
  };

  const loadStatistics = async () => {
    try {
      // Mock statistics
      setStatistics({
        totalMappings: 2,
        fieldMappings: 1,
        valueAssignmentMappings: 1,
        derivedValueMappings: 2,
        autoGenerationMappings: 2,
        conditionalMappings: 2,
        transformationMappings: 2,
        requestMappings: 2,
        responseMappings: 0,
        bidirectionalMappings: 0,
        paymentTypeSpecific: 2,
        instrumentSpecific: 2,
        clearingSystemSpecific: 0,
        averagePriority: 1.5,
        lastMappingCreated: '2024-01-15T10:30:00Z',
        lastMappingUpdated: '2024-01-15T10:30:00Z',
      });
    } catch (err) {
      setError('Failed to load statistics');
    }
  };

  const onSubmit = async (data: AdvancedPayloadMapping) => {
    try {
      setLoading(true);
      setError(null);

      if (editingMapping) {
        // Update existing mapping
        setMappings(prev =>
          prev.map(mapping => mapping.id === editingMapping.id ? { ...data, id: editingMapping.id } : mapping)
        );
        setSuccess('Mapping updated successfully');
      } else {
        // Create new mapping
        const newMapping = { ...data, id: Date.now().toString() };
        setMappings(prev => [...prev, newMapping]);
        setSuccess('Mapping created successfully');
      }

      setMappingDialogOpen(false);
      setEditingMapping(null);
      reset();
      loadStatistics();
    } catch (err) {
      setError('Failed to save mapping');
    } finally {
      setLoading(false);
    }
  };

  const handleEditMapping = (mapping: AdvancedPayloadMapping) => {
    setEditingMapping(mapping);
    reset(mapping);
    setMappingDialogOpen(true);
  };

  const handleDeleteMapping = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this mapping?')) {
      try {
        setMappings(prev => prev.filter(mapping => mapping.id !== id));
        setSuccess('Mapping deleted successfully');
        loadStatistics();
      } catch (err) {
        setError('Failed to delete mapping');
      }
    }
  };

  const getMappingTypeColor = (type: string) => {
    const colors: Record<string, 'primary' | 'secondary' | 'success' | 'warning' | 'error'> = {
      'FIELD_MAPPING': 'primary',
      'VALUE_ASSIGNMENT_MAPPING': 'secondary',
      'DERIVED_VALUE_MAPPING': 'success',
      'AUTO_GENERATION_MAPPING': 'warning',
      'CONDITIONAL_MAPPING': 'error',
      'TRANSFORMATION_MAPPING': 'primary',
    };
    return colors[type] || 'default';
  };

  const getDirectionColor = (direction: string) => {
    const colors: Record<string, 'primary' | 'secondary' | 'success'> = {
      'REQUEST': 'primary',
      'RESPONSE': 'secondary',
      'BIDIRECTIONAL': 'success',
    };
    return colors[direction] || 'default';
  };

  const filteredMappings = mappings.filter(mapping => {
    const matchesType = filterType === 'all' || mapping.mappingType === filterType;
    const matchesDirection = filterDirection === 'all' || mapping.direction === filterDirection;
    const matchesSearch = searchTerm === '' || 
      mapping.mappingName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      mapping.description?.toLowerCase().includes(searchTerm.toLowerCase());
    
    return matchesType && matchesDirection && matchesSearch;
  });

  const renderStatistics = () => (
    <Grid container spacing={3} sx={{ mb: 3 }}>
      <Grid item xs={12} sm={6} md={3}>
        <Card>
          <CardContent>
            <Box display="flex" alignItems="center">
              <AssessmentIcon sx={{ mr: 2, color: 'primary.main' }} />
              <Box>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Total Mappings
                </Typography>
                <Typography variant="h4">
                  {statistics?.totalMappings || 0}
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Grid>
      
      <Grid item xs={12} sm={6} md={3}>
        <Card>
          <CardContent>
            <Box display="flex" alignItems="center">
              <AssignmentIcon sx={{ mr: 2, color: 'secondary.main' }} />
              <Box>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Value Assignments
                </Typography>
                <Typography variant="h4">
                  {statistics?.valueAssignmentMappings || 0}
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Grid>
      
      <Grid item xs={12} sm={6} md={3}>
        <Card>
          <CardContent>
            <Box display="flex" alignItems="center">
              <FunctionsIcon sx={{ mr: 2, color: 'success.main' }} />
              <Box>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Derived Values
                </Typography>
                <Typography variant="h4">
                  {statistics?.derivedValueMappings || 0}
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Grid>
      
      <Grid item xs={12} sm={6} md={3}>
        <Card>
          <CardContent>
            <Box display="flex" alignItems="center">
              <AutoAwesomeIcon sx={{ mr: 2, color: 'warning.main' }} />
              <Box>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Auto Generated
                </Typography>
                <Typography variant="h4">
                  {statistics?.autoGenerationMappings || 0}
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );

  const renderMappingsList = () => (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Advanced Payload Mappings</Typography>
        <Box display="flex" gap={2}>
          <TextField
            size="small"
            placeholder="Search mappings..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
            }}
          />
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setMappingDialogOpen(true)}
          >
            Add Mapping
          </Button>
        </Box>
      </Box>

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth size="small">
            <InputLabel>Mapping Type</InputLabel>
            <Select
              value={filterType}
              label="Mapping Type"
              onChange={(e) => setFilterType(e.target.value)}
            >
              <MenuItem value="all">All Types</MenuItem>
              <MenuItem value="FIELD_MAPPING">Field Mapping</MenuItem>
              <MenuItem value="VALUE_ASSIGNMENT_MAPPING">Value Assignment</MenuItem>
              <MenuItem value="DERIVED_VALUE_MAPPING">Derived Value</MenuItem>
              <MenuItem value="AUTO_GENERATION_MAPPING">Auto Generation</MenuItem>
              <MenuItem value="CONDITIONAL_MAPPING">Conditional</MenuItem>
              <MenuItem value="TRANSFORMATION_MAPPING">Transformation</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth size="small">
            <InputLabel>Direction</InputLabel>
            <Select
              value={filterDirection}
              label="Direction"
              onChange={(e) => setFilterDirection(e.target.value)}
            >
              <MenuItem value="all">All Directions</MenuItem>
              <MenuItem value="REQUEST">Request</MenuItem>
              <MenuItem value="RESPONSE">Response</MenuItem>
              <MenuItem value="BIDIRECTIONAL">Bidirectional</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {filteredMappings.map((mapping) => (
          <Grid item xs={12} md={6} key={mapping.id}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                  <Box>
                    <Typography variant="h6" gutterBottom>
                      {mapping.mappingName}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      v{mapping.version}
                    </Typography>
                  </Box>
                  <Box>
                    <Chip
                      label={mapping.mappingType.replace('_', ' ')}
                      color={getMappingTypeColor(mapping.mappingType)}
                      size="small"
                    />
                    <Chip
                      label={mapping.direction}
                      color={getDirectionColor(mapping.direction)}
                      size="small"
                      sx={{ ml: 1 }}
                    />
                    <Chip
                      label={mapping.isActive ? 'Active' : 'Inactive'}
                      color={mapping.isActive ? 'success' : 'default'}
                      size="small"
                      sx={{ ml: 1 }}
                    />
                  </Box>
                </Box>

                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Tenant:</strong> {mapping.tenantId}
                  </Typography>
                  {mapping.paymentType && (
                    <Typography variant="body2" color="text.secondary">
                      <strong>Payment Type:</strong> {mapping.paymentType}
                    </Typography>
                  )}
                  {mapping.localInstrumentationCode && (
                    <Typography variant="body2" color="text.secondary">
                      <strong>Local Instrument:</strong> {mapping.localInstrumentationCode}
                    </Typography>
                  )}
                  <Typography variant="body2" color="text.secondary">
                    <strong>Priority:</strong> {mapping.priority}
                  </Typography>
                </Box>

                {mapping.description && (
                  <Typography variant="body2" color="text.secondary" mb={2}>
                    {mapping.description}
                  </Typography>
                )}

                <Box display="flex" gap={1}>
                  <Button
                    size="small"
                    startIcon={<EditIcon />}
                    onClick={() => handleEditMapping(mapping)}
                  >
                    Edit
                  </Button>
                  <Button
                    size="small"
                    startIcon={<ViewIcon />}
                    onClick={() => {
                      // Show mapping details
                    }}
                  >
                    View
                  </Button>
                  <Button
                    size="small"
                    color="error"
                    startIcon={<DeleteIcon />}
                    onClick={() => handleDeleteMapping(mapping.id!)}
                  >
                    Delete
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Advanced Payload Mapping
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Configure flexible payload mappings with static values, derived values, conditional logic, and auto-generated IDs.
      </Typography>

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

      {renderStatistics()}

      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={(e, newValue) => setActiveTab(newValue)}>
            <Tab label="Mappings" icon={<TransformIcon />} />
            <Tab label="Statistics" icon={<AssessmentIcon />} />
          </Tabs>
        </Box>

        <Box sx={{ p: 3 }}>
          {activeTab === 0 && renderMappingsList()}
          {activeTab === 1 && (
            <Box>
              <Typography variant="h6" gutterBottom>
                Detailed Statistics
              </Typography>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Mapping Types
                      </Typography>
                      <List>
                        <ListItem>
                          <ListItemIcon>
                            <SchemaIcon color="primary" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Field Mappings"
                            secondary={`${statistics?.fieldMappings || 0} mappings`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <AssignmentIcon color="secondary" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Value Assignments"
                            secondary={`${statistics?.valueAssignmentMappings || 0} mappings`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <FunctionsIcon color="success" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Derived Values"
                            secondary={`${statistics?.derivedValueMappings || 0} mappings`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <AutoAwesomeIcon color="warning" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Auto Generated"
                            secondary={`${statistics?.autoGenerationMappings || 0} mappings`}
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
                        Directions & Specificity
                      </Typography>
                      <List>
                        <ListItem>
                          <ListItemIcon>
                            <PlayIcon color="primary" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Request Mappings"
                            secondary={`${statistics?.requestMappings || 0} mappings`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <StopIcon color="secondary" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Response Mappings"
                            secondary={`${statistics?.responseMappings || 0} mappings`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <ConditionalIcon color="success" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Conditional Mappings"
                            secondary={`${statistics?.conditionalMappings || 0} mappings`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemIcon>
                            <TransformIcon color="warning" />
                          </ListItemIcon>
                          <ListItemText
                            primary="Transformation Mappings"
                            secondary={`${statistics?.transformationMappings || 0} mappings`}
                          />
                        </ListItem>
                      </List>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            </Box>
          )}
        </Box>
      </Card>

      {/* Mapping Dialog */}
      <Dialog open={mappingDialogOpen} onClose={() => setMappingDialogOpen(false)} maxWidth="lg" fullWidth>
        <DialogTitle>
          {editingMapping ? 'Edit Advanced Payload Mapping' : 'Add Advanced Payload Mapping'}
        </DialogTitle>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogContent>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="mappingName"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Mapping Name"
                      fullWidth
                      error={!!errors.mappingName}
                      helperText={errors.mappingName?.message}
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
                      label="Tenant ID"
                      fullWidth
                      error={!!errors.tenantId}
                      helperText={errors.tenantId?.message}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="mappingType"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!errors.mappingType}>
                      <InputLabel>Mapping Type</InputLabel>
                      <Select {...field} label="Mapping Type">
                        <MenuItem value="FIELD_MAPPING">Field Mapping</MenuItem>
                        <MenuItem value="VALUE_ASSIGNMENT_MAPPING">Value Assignment</MenuItem>
                        <MenuItem value="DERIVED_VALUE_MAPPING">Derived Value</MenuItem>
                        <MenuItem value="AUTO_GENERATION_MAPPING">Auto Generation</MenuItem>
                        <MenuItem value="CONDITIONAL_MAPPING">Conditional</MenuItem>
                        <MenuItem value="TRANSFORMATION_MAPPING">Transformation</MenuItem>
                        <MenuItem value="CUSTOM_MAPPING">Custom</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="direction"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth error={!!errors.direction}>
                      <InputLabel>Direction</InputLabel>
                      <Select {...field} label="Direction">
                        <MenuItem value="REQUEST">Request</MenuItem>
                        <MenuItem value="RESPONSE">Response</MenuItem>
                        <MenuItem value="BIDIRECTIONAL">Bidirectional</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <Controller
                  name="paymentType"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Payment Type"
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <Controller
                  name="localInstrumentationCode"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Local Instrumentation Code"
                      fullWidth
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <Controller
                  name="clearingSystemCode"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Clearing System Code"
                      fullWidth
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="version"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Version"
                      fullWidth
                      error={!!errors.version}
                      helperText={errors.version?.message}
                    />
                  )}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <Controller
                  name="priority"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Priority"
                      type="number"
                      fullWidth
                      error={!!errors.priority}
                      helperText={errors.priority?.message}
                    />
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
                      label="Description"
                      fullWidth
                      multiline
                      rows={3}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Value Assignments (JSON)
                </Typography>
                <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 1 }}>
                  <JSONEditor
                    id="valueAssignments"
                    placeholder={{
                      "messageId": "PAIN001-{{uuid()}}",
                      "creationDateTime": "{{timestamp()}}",
                      "messageType": "pain.001",
                      "version": "2013",
                      "source": "payment-engine"
                    }}
                    locale={locale}
                    height="200px"
                    width="100%"
                  />
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Derived Value Rules (JSON)
                </Typography>
                <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 1 }}>
                  <JSONEditor
                    id="derivedValueRules"
                    placeholder={{
                      "totalAmount": {
                        "expression": "${source.amount}",
                        "type": "NUMBER"
                      },
                      "formattedAmount": {
                        "expression": "${source.amount} * 100",
                        "type": "NUMBER"
                      }
                    }}
                    locale={locale}
                    height="200px"
                    width="100%"
                  />
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Auto Generation Rules (JSON)
                </Typography>
                <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 1 }}>
                  <JSONEditor
                    id="autoGenerationRules"
                    placeholder={{
                      "messageId": {
                        "type": "UUID"
                      },
                      "creationDateTime": {
                        "type": "TIMESTAMP"
                      },
                      "transactionId": {
                        "type": "SEQUENTIAL",
                        "prefix": "TXN-",
                        "suffix": "-PAIN001",
                        "length": 15
                      }
                    }}
                    locale={locale}
                    height="200px"
                    width="100%"
                  />
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Conditional Mappings (JSON)
                </Typography>
                <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 1 }}>
                  <JSONEditor
                    id="conditionalMappings"
                    placeholder={{
                      "paymentType == \"TRANSFER\"": {
                        "target": "paymentTypeCode",
                        "source": "TRA"
                      },
                      "amount > 10000": {
                        "target": "requiresApproval",
                        "source": "true"
                      }
                    }}
                    locale={locale}
                    height="200px"
                    width="100%"
                  />
                </Box>
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setMappingDialogOpen(false)} startIcon={<CancelIcon />}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              startIcon={<SaveIcon />}
              disabled={loading}
            >
              {editingMapping ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default AdvancedPayloadMapping;