import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Button,
  IconButton,
  Tooltip,
  Alert,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Badge,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ArrowForward as ArrowForwardIcon,
  ArrowBack as ArrowBackIcon,
  SwapHoriz as SwapHorizIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  Settings as SettingsIcon,
  PlayArrow as PlayArrowIcon,
  Stop as StopIcon,
} from '@mui/icons-material';
import { Iso20022MessageType, FlowDirection } from '../../types/clearingSystem';

interface MessageFlowConfigProps {
  tenantId: string;
  paymentType: string;
  localInstrumentCode?: string;
}

interface MessageFlow {
  id: string;
  name: string;
  description: string;
  clientMessage: Iso20022MessageType;
  bankProcessing: string;
  clearingSystemMessage: Iso20022MessageType;
  clearingSystemResponse: Iso20022MessageType;
  bankResponse: Iso20022MessageType;
  clientResponse: Iso20022MessageType;
  flowDirection: FlowDirection;
  isActive: boolean;
  isConfigured: boolean;
  endpoints: {
    clientToBank: string;
    bankToClearing: string;
    clearingToBank: string;
    bankToClient: string;
  };
  configuration: {
    messageFormat: 'JSON' | 'XML';
    responseMode: 'IMMEDIATE' | 'ASYNC' | 'KAFKA' | 'WEBHOOK';
    timeoutMs: number;
    retryAttempts: number;
    authenticationType: string;
  };
}

const MessageFlowConfig: React.FC<MessageFlowConfigProps> = ({
  tenantId,
  paymentType,
  localInstrumentCode,
}) => {
  const [flows, setFlows] = useState<MessageFlow[]>([]);
  const [expandedFlows, setExpandedFlows] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Mock data - in production, this would come from the API
    const mockFlows: MessageFlow[] = [
      {
        id: 'pain001-flow',
        name: 'PAIN.001 Payment Flow',
        description: 'Complete payment initiation flow from client to clearing system',
        clientMessage: 'pain001',
        bankProcessing: 'Transform to PACS.008',
        clearingSystemMessage: 'pacs008',
        clearingSystemResponse: 'pacs002',
        bankResponse: 'Transform to PAIN.002',
        clientResponse: 'pain002',
        flowDirection: 'CLIENT_TO_CLEARING',
        isActive: true,
        isConfigured: true,
        endpoints: {
          clientToBank: '/api/v1/iso20022/comprehensive/pain001-to-clearing-system',
          bankToClearing: 'https://clearing-system.com/api/pacs008',
          clearingToBank: 'https://clearing-system.com/webhook/pacs002',
          bankToClient: '/api/v1/iso20022/comprehensive/pain002-response',
        },
        configuration: {
          messageFormat: 'JSON',
          responseMode: 'IMMEDIATE',
          timeoutMs: 30000,
          retryAttempts: 3,
          authenticationType: 'API_KEY',
        },
      },
      {
        id: 'camt055-flow',
        name: 'CAMT.055 Cancellation Flow',
        description: 'Payment cancellation request flow',
        clientMessage: 'camt055',
        bankProcessing: 'Transform to PACS.007',
        clearingSystemMessage: 'pacs007',
        clearingSystemResponse: 'pacs002',
        bankResponse: 'Transform to CAMT.029',
        clientResponse: 'camt029',
        flowDirection: 'CLIENT_TO_CLEARING',
        isActive: true,
        isConfigured: true,
        endpoints: {
          clientToBank: '/api/v1/iso20022/comprehensive/camt055-to-clearing-system',
          bankToClearing: 'https://clearing-system.com/api/pacs007',
          clearingToBank: 'https://clearing-system.com/webhook/pacs002',
          bankToClient: '/api/v1/iso20022/comprehensive/camt029-response',
        },
        configuration: {
          messageFormat: 'JSON',
          responseMode: 'IMMEDIATE',
          timeoutMs: 30000,
          retryAttempts: 3,
          authenticationType: 'API_KEY',
        },
      },
      {
        id: 'camt056-flow',
        name: 'CAMT.056 Status Request Flow',
        description: 'Payment status request flow',
        clientMessage: 'camt056',
        bankProcessing: 'Transform to PACS.028',
        clearingSystemMessage: 'pacs028',
        clearingSystemResponse: 'pacs002',
        bankResponse: 'Transform to CAMT.056',
        clientResponse: 'camt056',
        flowDirection: 'CLIENT_TO_CLEARING',
        isActive: true,
        isConfigured: true,
        endpoints: {
          clientToBank: '/api/v1/iso20022/comprehensive/camt056-to-clearing-system',
          bankToClearing: 'https://clearing-system.com/api/pacs028',
          clearingToBank: 'https://clearing-system.com/webhook/pacs002',
          bankToClient: '/api/v1/iso20022/comprehensive/camt056-response',
        },
        configuration: {
          messageFormat: 'JSON',
          responseMode: 'IMMEDIATE',
          timeoutMs: 30000,
          retryAttempts: 3,
          authenticationType: 'API_KEY',
        },
      },
      {
        id: 'pacs008-incoming-flow',
        name: 'PACS.008 Incoming Flow',
        description: 'Incoming payment from clearing system',
        clientMessage: 'pacs008',
        bankProcessing: 'Process incoming payment',
        clearingSystemMessage: 'pacs008',
        clearingSystemResponse: 'pacs002',
        bankResponse: 'Generate PACS.002 acknowledgment',
        clientResponse: 'pacs002',
        flowDirection: 'CLEARING_TO_CLIENT',
        isActive: true,
        isConfigured: true,
        endpoints: {
          clientToBank: 'https://clearing-system.com/webhook/pacs008',
          bankToClearing: '/api/v1/iso20022/comprehensive/pacs008-from-clearing-system',
          clearingToBank: 'https://clearing-system.com/api/pacs002',
          bankToClient: '/api/v1/iso20022/comprehensive/pacs002-response',
        },
        configuration: {
          messageFormat: 'JSON',
          responseMode: 'ASYNC',
          timeoutMs: 30000,
          retryAttempts: 3,
          authenticationType: 'API_KEY',
        },
      },
      {
        id: 'pacs004-return-flow',
        name: 'PACS.004 Return Flow',
        description: 'Payment return from clearing system',
        clientMessage: 'pacs004',
        bankProcessing: 'Process payment return',
        clearingSystemMessage: 'pacs004',
        clearingSystemResponse: 'pacs002',
        bankResponse: 'Transform to PAIN.002',
        clientResponse: 'pain002',
        flowDirection: 'CLEARING_TO_CLIENT',
        isActive: true,
        isConfigured: true,
        endpoints: {
          clientToBank: 'https://clearing-system.com/webhook/pacs004',
          bankToClearing: '/api/v1/iso20022/comprehensive/pacs004-from-clearing-system',
          clearingToBank: 'https://clearing-system.com/api/pacs002',
          bankToClient: '/api/v1/iso20022/comprehensive/pain002-response',
        },
        configuration: {
          messageFormat: 'JSON',
          responseMode: 'ASYNC',
          timeoutMs: 30000,
          retryAttempts: 3,
          authenticationType: 'API_KEY',
        },
      },
      {
        id: 'camt054-notification-flow',
        name: 'CAMT.054 Notification Flow',
        description: 'Account notification from clearing system',
        clientMessage: 'camt054',
        bankProcessing: 'Process account notification',
        clearingSystemMessage: 'camt054',
        clearingSystemResponse: 'camt029',
        bankResponse: 'Transform to CAMT.053',
        clientResponse: 'camt053',
        flowDirection: 'CLEARING_TO_CLIENT',
        isActive: true,
        isConfigured: true,
        endpoints: {
          clientToBank: 'https://clearing-system.com/webhook/camt054',
          bankToClearing: '/api/v1/iso20022/comprehensive/camt054-from-clearing-system',
          clearingToBank: 'https://clearing-system.com/api/camt029',
          bankToClient: '/api/v1/iso20022/comprehensive/camt053-response',
        },
        configuration: {
          messageFormat: 'JSON',
          responseMode: 'ASYNC',
          timeoutMs: 30000,
          retryAttempts: 3,
          authenticationType: 'API_KEY',
        },
      },
    ];

    setFlows(mockFlows);
    setLoading(false);
  }, [tenantId, paymentType, localInstrumentCode]);

  const handleFlowToggle = (flowId: string) => {
    setExpandedFlows(prev => 
      prev.includes(flowId) 
        ? prev.filter(id => id !== flowId)
        : [...prev, flowId]
    );
  };

  const getFlowDirectionIcon = (direction: FlowDirection) => {
    switch (direction) {
      case 'CLIENT_TO_CLEARING':
        return <ArrowForwardIcon color="primary" />;
      case 'CLEARING_TO_CLIENT':
        return <ArrowBackIcon color="secondary" />;
      case 'BIDIRECTIONAL':
        return <SwapHorizIcon color="info" />;
      default:
        return <InfoIcon />;
    }
  };

  const getFlowDirectionColor = (direction: FlowDirection) => {
    switch (direction) {
      case 'CLIENT_TO_CLEARING':
        return 'primary';
      case 'CLEARING_TO_CLIENT':
        return 'secondary';
      case 'BIDIRECTIONAL':
        return 'info';
      default:
        return 'default';
    }
  };

  const getStatusChip = (isActive: boolean, isConfigured: boolean) => {
    if (isActive && isConfigured) {
      return <Chip label="Active & Configured" color="success" size="small" />;
    } else if (isConfigured) {
      return <Chip label="Configured" color="warning" size="small" />;
    } else {
      return <Chip label="Not Configured" color="error" size="small" />;
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <Typography>Loading message flows...</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        ISO 20022 Message Flow Configuration
      </Typography>
      
      <Alert severity="info" sx={{ mb: 3 }}>
        Complete configuration of all ISO 20022 message flows between clients and clearing systems.
        Each flow is fully configurable with REST API endpoints, JSON/XML formats, and synchronous/asynchronous processing.
      </Alert>

      <Grid container spacing={2}>
        <Grid item xs={12} md={8}>
          <Typography variant="h6" gutterBottom>
            Message Flows
          </Typography>
          
          {flows.map((flow) => (
            <Accordion 
              key={flow.id}
              expanded={expandedFlows.includes(flow.id)}
              onChange={() => handleFlowToggle(flow.id)}
              sx={{ mb: 2 }}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Box display="flex" alignItems="center" width="100%">
                  <Box display="flex" alignItems="center" flex={1}>
                    {getFlowDirectionIcon(flow.flowDirection)}
                    <Typography variant="h6" sx={{ ml: 1 }}>
                      {flow.name}
                    </Typography>
                  </Box>
                  <Box display="flex" alignItems="center" gap={1}>
                    {getStatusChip(flow.isActive, flow.isConfigured)}
                    <Chip 
                      label={flow.flowDirection.replace('_', ' ')} 
                      color={getFlowDirectionColor(flow.flowDirection)}
                      size="small"
                    />
                  </Box>
                </Box>
              </AccordionSummary>
              <AccordionDetails>
                <Grid container spacing={3}>
                  <Grid item xs={12}>
                    <Typography variant="body2" color="textSecondary" gutterBottom>
                      {flow.description}
                    </Typography>
                  </Grid>
                  
                  {/* Flow Diagram */}
                  <Grid item xs={12}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          Message Flow
                        </Typography>
                        <Box display="flex" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2}>
                          <Box textAlign="center">
                            <Typography variant="caption" color="textSecondary">Client</Typography>
                            <Box p={1} border={1} borderColor="primary.main" borderRadius={1}>
                              <Typography variant="body2" fontWeight="bold">
                                {flow.clientMessage.toUpperCase()}
                              </Typography>
                            </Box>
                          </Box>
                          
                          <ArrowForwardIcon color="action" />
                          
                          <Box textAlign="center">
                            <Typography variant="caption" color="textSecondary">Bank</Typography>
                            <Box p={1} border={1} borderColor="secondary.main" borderRadius={1}>
                              <Typography variant="body2" fontWeight="bold">
                                {flow.bankProcessing}
                              </Typography>
                            </Box>
                          </Box>
                          
                          <ArrowForwardIcon color="action" />
                          
                          <Box textAlign="center">
                            <Typography variant="caption" color="textSecondary">Clearing System</Typography>
                            <Box p={1} border={1} borderColor="info.main" borderRadius={1}>
                              <Typography variant="body2" fontWeight="bold">
                                {flow.clearingSystemMessage.toUpperCase()}
                              </Typography>
                            </Box>
                          </Box>
                          
                          <ArrowForwardIcon color="action" />
                          
                          <Box textAlign="center">
                            <Typography variant="caption" color="textSecondary">Response</Typography>
                            <Box p={1} border={1} borderColor="success.main" borderRadius={1}>
                              <Typography variant="body2" fontWeight="bold">
                                {flow.clearingSystemResponse.toUpperCase()}
                              </Typography>
                            </Box>
                          </Box>
                          
                          <ArrowForwardIcon color="action" />
                          
                          <Box textAlign="center">
                            <Typography variant="caption" color="textSecondary">Client</Typography>
                            <Box p={1} border={1} borderColor="primary.main" borderRadius={1}>
                              <Typography variant="body2" fontWeight="bold">
                                {flow.clientResponse.toUpperCase()}
                              </Typography>
                            </Box>
                          </Box>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                  
                  {/* Endpoints Configuration */}
                  <Grid item xs={12} md={6}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          Endpoints
                        </Typography>
                        <List dense>
                          <ListItem>
                            <ListItemIcon>
                              <ArrowForwardIcon color="primary" />
                            </ListItemIcon>
                            <ListItemText 
                              primary="Client to Bank"
                              secondary={flow.endpoints.clientToBank}
                            />
                          </ListItem>
                          <ListItem>
                            <ListItemIcon>
                              <ArrowForwardIcon color="secondary" />
                            </ListItemIcon>
                            <ListItemText 
                              primary="Bank to Clearing"
                              secondary={flow.endpoints.bankToClearing}
                            />
                          </ListItem>
                          <ListItem>
                            <ListItemIcon>
                              <ArrowBackIcon color="info" />
                            </ListItemIcon>
                            <ListItemText 
                              primary="Clearing to Bank"
                              secondary={flow.endpoints.clearingToBank}
                            />
                          </ListItem>
                          <ListItem>
                            <ListItemIcon>
                              <ArrowBackIcon color="primary" />
                            </ListItemIcon>
                            <ListItemText 
                              primary="Bank to Client"
                              secondary={flow.endpoints.bankToClient}
                            />
                          </ListItem>
                        </List>
                      </CardContent>
                    </Card>
                  </Grid>
                  
                  {/* Configuration Details */}
                  <Grid item xs={12} md={6}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          Configuration
                        </Typography>
                        <List dense>
                          <ListItem>
                            <ListItemText 
                              primary="Message Format"
                              secondary={flow.configuration.messageFormat}
                            />
                          </ListItem>
                          <ListItem>
                            <ListItemText 
                              primary="Response Mode"
                              secondary={flow.configuration.responseMode}
                            />
                          </ListItem>
                          <ListItem>
                            <ListItemText 
                              primary="Timeout"
                              secondary={`${flow.configuration.timeoutMs}ms`}
                            />
                          </ListItem>
                          <ListItem>
                            <ListItemText 
                              primary="Retry Attempts"
                              secondary={flow.configuration.retryAttempts}
                            />
                          </ListItem>
                          <ListItem>
                            <ListItemText 
                              primary="Authentication"
                              secondary={flow.configuration.authenticationType}
                            />
                          </ListItem>
                        </List>
                      </CardContent>
                    </Card>
                  </Grid>
                  
                  {/* Action Buttons */}
                  <Grid item xs={12}>
                    <Box display="flex" gap={2}>
                      <Button
                        variant="outlined"
                        startIcon={<SettingsIcon />}
                        onClick={() => {/* Open configuration dialog */}}
                      >
                        Configure Flow
                      </Button>
                      <Button
                        variant="outlined"
                        startIcon={<PlayArrowIcon />}
                        onClick={() => {/* Test flow */}}
                      >
                        Test Flow
                      </Button>
                      <Button
                        variant="outlined"
                        startIcon={flow.isActive ? <StopIcon /> : <PlayArrowIcon />}
                        onClick={() => {/* Toggle flow status */}}
                      >
                        {flow.isActive ? 'Deactivate' : 'Activate'}
                      </Button>
                    </Box>
                  </Grid>
                </Grid>
              </AccordionDetails>
            </Accordion>
          ))}
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Flow Statistics
              </Typography>
              <List dense>
                <ListItem>
                  <ListItemText 
                    primary="Total Flows"
                    secondary={flows.length}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText 
                    primary="Active Flows"
                    secondary={flows.filter(f => f.isActive).length}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText 
                    primary="Configured Flows"
                    secondary={flows.filter(f => f.isConfigured).length}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText 
                    primary="Client to Clearing"
                    secondary={flows.filter(f => f.flowDirection === 'CLIENT_TO_CLEARING').length}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText 
                    primary="Clearing to Client"
                    secondary={flows.filter(f => f.flowDirection === 'CLEARING_TO_CLIENT').length}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText 
                    primary="Bidirectional"
                    secondary={flows.filter(f => f.flowDirection === 'BIDIRECTIONAL').length}
                  />
                </ListItem>
              </List>
            </CardContent>
          </Card>
          
          <Card sx={{ mt: 2 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Tenant Configuration
              </Typography>
              <List dense>
                <ListItem>
                  <ListItemText 
                    primary="Tenant ID"
                    secondary={tenantId}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText 
                    primary="Payment Type"
                    secondary={paymentType}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText 
                    primary="Local Instrument"
                    secondary={localInstrumentCode || 'Not specified'}
                  />
                </ListItem>
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default MessageFlowConfig;