import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  TextField,
  Button,
  Grid,
  Alert,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControlLabel,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Settings as SettingsIcon,
  Topic as TopicIcon,
  Speed as SpeedIcon,
} from '@mui/icons-material';

interface PaymentTypeResponseConfig {
  paymentTypeCode: string;
  paymentTypeName: string;
  responseMode: 'SYNCHRONOUS' | 'ASYNCHRONOUS' | 'KAFKA_TOPIC';
  kafkaResponseConfig: {
    enabled: boolean;
    usePaymentTypeSpecificTopic: boolean;
    topicPattern: string;
    explicitTopicName?: string;
    includeOriginalMessage: boolean;
    priority: 'HIGH' | 'NORMAL' | 'LOW';
    targetSystems: string[];
    retryPolicy: {
      maxRetries: number;
      backoffMs: number;
    };
  };
}

interface PaymentResponseConfigManagerProps {
  tenantId: string;
  onConfigChange?: (config: PaymentTypeResponseConfig) => void;
}

const PaymentResponseConfigManager: React.FC<PaymentResponseConfigManagerProps> = ({
  tenantId,
  onConfigChange,
}) => {
  const [paymentTypes, setPaymentTypes] = useState<PaymentTypeResponseConfig[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editingConfig, setEditingConfig] = useState<PaymentTypeResponseConfig | null>(null);

  useEffect(() => {
    loadPaymentTypeConfigs();
  }, [tenantId]);

  const loadPaymentTypeConfigs = async () => {
    setLoading(true);
    try {
      // Mock data - in real implementation, this would call the API
      const mockConfigs: PaymentTypeResponseConfig[] = [
        {
          paymentTypeCode: 'RTP',
          paymentTypeName: 'Real-Time Payment',
          responseMode: 'SYNCHRONOUS',
          kafkaResponseConfig: {
            enabled: false,
            usePaymentTypeSpecificTopic: true,
            topicPattern: 'payment-engine.{tenantId}.responses.{paymentType}.pain002',
            includeOriginalMessage: true,
            priority: 'HIGH',
            targetSystems: ['core-banking'],
            retryPolicy: { maxRetries: 3, backoffMs: 1000 },
          },
        },
        {
          paymentTypeCode: 'ACH_CREDIT',
          paymentTypeName: 'ACH Credit Transfer',
          responseMode: 'KAFKA_TOPIC',
          kafkaResponseConfig: {
            enabled: true,
            usePaymentTypeSpecificTopic: true,
            topicPattern: 'payment-engine.{tenantId}.responses.{paymentType}.pain002',
            includeOriginalMessage: false,
            priority: 'NORMAL',
            targetSystems: ['ach-processor', 'notification-service'],
            retryPolicy: { maxRetries: 5, backoffMs: 2000 },
          },
        },
        {
          paymentTypeCode: 'WIRE_TRANSFER',
          paymentTypeName: 'Wire Transfer',
          responseMode: 'KAFKA_TOPIC',
          kafkaResponseConfig: {
            enabled: true,
            usePaymentTypeSpecificTopic: false,
            topicPattern: 'payment-engine.{tenantId}.responses.{paymentType}.pain002',
            explicitTopicName: 'high-value-wire-responses',
            includeOriginalMessage: true,
            priority: 'HIGH',
            targetSystems: ['wire-processor', 'compliance-service', 'audit-service'],
            retryPolicy: { maxRetries: 3, backoffMs: 1000 },
          },
        },
      ];
      
      setPaymentTypes(mockConfigs);
    } catch (err: any) {
      setError(err.message || 'Failed to load payment type configurations');
    } finally {
      setLoading(false);
    }
  };

  const handleEditConfig = (config: PaymentTypeResponseConfig) => {
    setEditingConfig({ ...config });
    setEditDialogOpen(true);
  };

  const handleSaveConfig = async () => {
    if (!editingConfig) return;

    try {
      // API call to update response configuration
      // await apiClient.updatePaymentTypeResponseConfig(tenantId, editingConfig.paymentTypeCode, editingConfig);
      
      // Update local state
      setPaymentTypes(prev =>
        prev.map(config =>
          config.paymentTypeCode === editingConfig.paymentTypeCode ? editingConfig : config
        )
      );
      
      setEditDialogOpen(false);
      setEditingConfig(null);
      
      if (onConfigChange) {
        onConfigChange(editingConfig);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to update configuration');
    }
  };

  const getResponseModeColor = (mode: string) => {
    switch (mode) {
      case 'SYNCHRONOUS': return 'primary';
      case 'ASYNCHRONOUS': return 'warning';
      case 'KAFKA_TOPIC': return 'success';
      default: return 'default';
    }
  };

  const getTopicName = (config: PaymentTypeResponseConfig) => {
    if (config.kafkaResponseConfig.explicitTopicName) {
      return config.kafkaResponseConfig.explicitTopicName;
    }
    
    if (config.kafkaResponseConfig.usePaymentTypeSpecificTopic) {
      return config.kafkaResponseConfig.topicPattern
        .replace('{tenantId}', tenantId)
        .replace('{paymentType}', config.paymentTypeCode.toLowerCase());
    }
    
    return `payment-engine.${tenantId}.responses.pain002`;
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Payment Response Configuration - {tenantId}
      </Typography>
      
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Configure how pain.002 responses are delivered for each payment type:
        <br />
        • <strong>SYNCHRONOUS</strong>: Immediate API response
        • <strong>ASYNCHRONOUS</strong>: Callback-based delivery
        • <strong>KAFKA_TOPIC</strong>: Published to payment-type-specific Kafka topics
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <TableContainer component={Card}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Payment Type</TableCell>
              <TableCell>Response Mode</TableCell>
              <TableCell>Kafka Topic</TableCell>
              <TableCell>Target Systems</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paymentTypes.map((config) => (
              <TableRow key={config.paymentTypeCode}>
                <TableCell>
                  <Box>
                    <Typography variant="body2" fontWeight="bold">
                      {config.paymentTypeName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {config.paymentTypeCode}
                    </Typography>
                  </Box>
                </TableCell>
                <TableCell>
                  <Chip
                    label={config.responseMode}
                    color={getResponseModeColor(config.responseMode) as any}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  {config.responseMode === 'KAFKA_TOPIC' ? (
                    <Box>
                      <Typography variant="body2" fontFamily="monospace" fontSize="0.8rem">
                        {getTopicName(config)}
                      </Typography>
                      <Chip
                        label={config.kafkaResponseConfig.priority}
                        size="small"
                        color={config.kafkaResponseConfig.priority === 'HIGH' ? 'error' : 'default'}
                      />
                    </Box>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      N/A
                    </Typography>
                  )}
                </TableCell>
                <TableCell>
                  {config.responseMode === 'KAFKA_TOPIC' && config.kafkaResponseConfig.targetSystems.length > 0 ? (
                    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                      {config.kafkaResponseConfig.targetSystems.map((system) => (
                        <Chip key={system} label={system} size="small" variant="outlined" />
                      ))}
                    </Box>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      N/A
                    </Typography>
                  )}
                </TableCell>
                <TableCell>
                  <Button
                    size="small"
                    startIcon={<SettingsIcon />}
                    onClick={() => handleEditConfig(config)}
                  >
                    Configure
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Edit Configuration Dialog */}
      <Dialog
        open={editDialogOpen}
        onClose={() => setEditDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          Configure Response for {editingConfig?.paymentTypeName}
        </DialogTitle>
        <DialogContent>
          {editingConfig && (
            <Grid container spacing={3} sx={{ mt: 1 }}>
              <Grid item xs={12}>
                <FormControl fullWidth>
                  <InputLabel>Response Mode</InputLabel>
                  <Select
                    value={editingConfig.responseMode}
                    label="Response Mode"
                    onChange={(e) =>
                      setEditingConfig({
                        ...editingConfig,
                        responseMode: e.target.value as any,
                      })
                    }
                  >
                    <MenuItem value="SYNCHRONOUS">
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <SpeedIcon />
                        Synchronous (Immediate API Response)
                      </Box>
                    </MenuItem>
                    <MenuItem value="ASYNCHRONOUS">
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <SettingsIcon />
                        Asynchronous (Callback-based)
                      </Box>
                    </MenuItem>
                    <MenuItem value="KAFKA_TOPIC">
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <TopicIcon />
                        Kafka Topic (Event-driven)
                      </Box>
                    </MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              {editingConfig.responseMode === 'KAFKA_TOPIC' && (
                <>
                  <Grid item xs={12}>
                    <Accordion>
                      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6">Kafka Configuration</Typography>
                      </AccordionSummary>
                      <AccordionDetails>
                        <Grid container spacing={2}>
                          <Grid item xs={12}>
                            <FormControlLabel
                              control={
                                <Switch
                                  checked={editingConfig.kafkaResponseConfig.enabled}
                                  onChange={(e) =>
                                    setEditingConfig({
                                      ...editingConfig,
                                      kafkaResponseConfig: {
                                        ...editingConfig.kafkaResponseConfig,
                                        enabled: e.target.checked,
                                      },
                                    })
                                  }
                                />
                              }
                              label="Enable Kafka Response Publishing"
                            />
                          </Grid>

                          <Grid item xs={12}>
                            <FormControlLabel
                              control={
                                <Switch
                                  checked={editingConfig.kafkaResponseConfig.usePaymentTypeSpecificTopic}
                                  onChange={(e) =>
                                    setEditingConfig({
                                      ...editingConfig,
                                      kafkaResponseConfig: {
                                        ...editingConfig.kafkaResponseConfig,
                                        usePaymentTypeSpecificTopic: e.target.checked,
                                      },
                                    })
                                  }
                                />
                              }
                              label="Use Payment Type Specific Topic"
                            />
                          </Grid>

                          <Grid item xs={12}>
                            <TextField
                              fullWidth
                              label="Topic Pattern"
                              value={editingConfig.kafkaResponseConfig.topicPattern}
                              onChange={(e) =>
                                setEditingConfig({
                                  ...editingConfig,
                                  kafkaResponseConfig: {
                                    ...editingConfig.kafkaResponseConfig,
                                    topicPattern: e.target.value,
                                  },
                                })
                              }
                              helperText="Use {tenantId} and {paymentType} placeholders"
                            />
                          </Grid>

                          <Grid item xs={12}>
                            <TextField
                              fullWidth
                              label="Explicit Topic Name (Optional)"
                              value={editingConfig.kafkaResponseConfig.explicitTopicName || ''}
                              onChange={(e) =>
                                setEditingConfig({
                                  ...editingConfig,
                                  kafkaResponseConfig: {
                                    ...editingConfig.kafkaResponseConfig,
                                    explicitTopicName: e.target.value,
                                  },
                                })
                              }
                              helperText="Override topic pattern with explicit name"
                            />
                          </Grid>

                          <Grid item xs={6}>
                            <FormControl fullWidth>
                              <InputLabel>Priority</InputLabel>
                              <Select
                                value={editingConfig.kafkaResponseConfig.priority}
                                label="Priority"
                                onChange={(e) =>
                                  setEditingConfig({
                                    ...editingConfig,
                                    kafkaResponseConfig: {
                                      ...editingConfig.kafkaResponseConfig,
                                      priority: e.target.value as any,
                                    },
                                  })
                                }
                              >
                                <MenuItem value="HIGH">High Priority</MenuItem>
                                <MenuItem value="NORMAL">Normal Priority</MenuItem>
                                <MenuItem value="LOW">Low Priority</MenuItem>
                              </Select>
                            </FormControl>
                          </Grid>

                          <Grid item xs={6}>
                            <FormControlLabel
                              control={
                                <Switch
                                  checked={editingConfig.kafkaResponseConfig.includeOriginalMessage}
                                  onChange={(e) =>
                                    setEditingConfig({
                                      ...editingConfig,
                                      kafkaResponseConfig: {
                                        ...editingConfig.kafkaResponseConfig,
                                        includeOriginalMessage: e.target.checked,
                                      },
                                    })
                                  }
                                />
                              }
                              label="Include Original Message"
                            />
                          </Grid>

                          <Grid item xs={6}>
                            <TextField
                              fullWidth
                              label="Max Retries"
                              type="number"
                              value={editingConfig.kafkaResponseConfig.retryPolicy.maxRetries}
                              onChange={(e) =>
                                setEditingConfig({
                                  ...editingConfig,
                                  kafkaResponseConfig: {
                                    ...editingConfig.kafkaResponseConfig,
                                    retryPolicy: {
                                      ...editingConfig.kafkaResponseConfig.retryPolicy,
                                      maxRetries: parseInt(e.target.value) || 3,
                                    },
                                  },
                                })
                              }
                            />
                          </Grid>

                          <Grid item xs={6}>
                            <TextField
                              fullWidth
                              label="Backoff (ms)"
                              type="number"
                              value={editingConfig.kafkaResponseConfig.retryPolicy.backoffMs}
                              onChange={(e) =>
                                setEditingConfig({
                                  ...editingConfig,
                                  kafkaResponseConfig: {
                                    ...editingConfig.kafkaResponseConfig,
                                    retryPolicy: {
                                      ...editingConfig.kafkaResponseConfig.retryPolicy,
                                      backoffMs: parseInt(e.target.value) || 1000,
                                    },
                                  },
                                })
                              }
                            />
                          </Grid>

                          <Grid item xs={12}>
                            <TextField
                              fullWidth
                              label="Target Systems (comma-separated)"
                              value={editingConfig.kafkaResponseConfig.targetSystems.join(', ')}
                              onChange={(e) =>
                                setEditingConfig({
                                  ...editingConfig,
                                  kafkaResponseConfig: {
                                    ...editingConfig.kafkaResponseConfig,
                                    targetSystems: e.target.value
                                      .split(',')
                                      .map(s => s.trim())
                                      .filter(s => s.length > 0),
                                  },
                                })
                              }
                              helperText="Systems that will consume the Kafka messages"
                            />
                          </Grid>

                          <Grid item xs={12}>
                            <Alert severity="info">
                              <Typography variant="body2">
                                <strong>Resulting Topic:</strong> {getTopicName(editingConfig)}
                              </Typography>
                            </Alert>
                          </Grid>
                        </Grid>
                      </AccordionDetails>
                    </Accordion>
                  </Grid>
                </>
              )}
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSaveConfig}>
            Save Configuration
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PaymentResponseConfigManager;