import React, { useState } from 'react';
import {
  Box,
  Typography,
  Tabs,
  Tab,
  Grid,
  Card,
  CardContent,
  Alert,
  Chip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
} from '@mui/material';
import {
  Payment as PaymentIcon,
  Cancel as CancelIcon,
  Assessment as ReportIcon,
  Info as InfoIcon,
  CheckCircle as CheckIcon,
  Error as ErrorIcon,
  Schedule as ScheduleIcon,
} from '@mui/icons-material';

import Iso20022PaymentForm from '../components/iso20022/Iso20022PaymentForm';
import Iso20022CancellationForm from '../components/iso20022/Iso20022CancellationForm';
import Iso20022MessageList from '../components/iso20022/Iso20022MessageList';

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
      id={`iso20022-tabpanel-${index}`}
      aria-labelledby={`iso20022-tab-${index}`}
    >
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
};

const Iso20022Page: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [helpDialogOpen, setHelpDialogOpen] = useState(false);
  const [recentActivity, setRecentActivity] = useState<string[]>([]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handlePaymentCreated = (pain002Response: any) => {
    const activity = `Payment initiated - Message ID: ${pain002Response.CstmrPmtStsRpt?.GrpHdr?.MsgId}`;
    setRecentActivity(prev => [activity, ...prev.slice(0, 4)]);
  };

  const handleCancellationProcessed = (results: any[]) => {
    const acceptedCount = results.filter(r => r.status === 'ACCEPTED').length;
    const activity = `Cancellation processed - ${acceptedCount} payment(s) cancelled`;
    setRecentActivity(prev => [activity, ...prev.slice(0, 4)]);
  };

  const messageTypeInfo = [
    {
      type: 'pain.001',
      name: 'Customer Credit Transfer Initiation',
      description: 'Initiate payments from customers',
      icon: <PaymentIcon color="primary" />,
      direction: 'Customer → Bank',
    },
    {
      type: 'pain.002',
      name: 'Customer Payment Status Report',
      description: 'Report payment status to customers',
      icon: <CheckIcon color="success" />,
      direction: 'Bank → Customer',
    },
    {
      type: 'pain.007',
      name: 'Customer Payment Reversal',
      description: 'Reverse completed payments',
      icon: <ErrorIcon color="error" />,
      direction: 'Customer → Bank',
    },
    {
      type: 'camt.055',
      name: 'Customer Payment Cancellation Request',
      description: 'Cancel pending payments',
      icon: <CancelIcon color="warning" />,
      direction: 'Customer → Bank',
    },
    {
      type: 'pacs.008',
      name: 'FI to FI Customer Credit Transfer',
      description: 'Process payments from schemes',
      icon: <ScheduleIcon color="info" />,
      direction: 'Scheme → Bank',
    },
    {
      type: 'camt.053',
      name: 'Bank to Customer Statement',
      description: 'Generate account statements',
      icon: <ReportIcon color="secondary" />,
      direction: 'Bank → Customer',
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">
          ISO 20022 Banking Messages
        </Typography>
        <Button
          variant="outlined"
          startIcon={<InfoIcon />}
          onClick={() => setHelpDialogOpen(true)}
        >
          Help & Standards
        </Button>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} lg={9}>
          <Card>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
              <Tabs value={tabValue} onChange={handleTabChange} variant="fullWidth">
                <Tab
                  label="Payment Initiation"
                  icon={<PaymentIcon />}
                  iconPosition="start"
                />
                <Tab
                  label="Payment Cancellation"
                  icon={<CancelIcon />}
                  iconPosition="start"
                />
                <Tab
                  label="Message History"
                  icon={<ReportIcon />}
                  iconPosition="start"
                />
              </Tabs>
            </Box>

            <TabPanel value={tabValue} index={0}>
              <Iso20022PaymentForm
                onPaymentCreated={handlePaymentCreated}
                onError={(error) => console.error('Payment error:', error)}
              />
            </TabPanel>

            <TabPanel value={tabValue} index={1}>
              <Iso20022CancellationForm
                onCancellationProcessed={handleCancellationProcessed}
                onError={(error) => console.error('Cancellation error:', error)}
              />
            </TabPanel>

            <TabPanel value={tabValue} index={2}>
              <Iso20022MessageList />
            </TabPanel>
          </Card>
        </Grid>

        <Grid item xs={12} lg={3}>
          {/* Recent Activity */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Activity
              </Typography>
              {recentActivity.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  No recent activity
                </Typography>
              ) : (
                <List dense>
                  {recentActivity.map((activity, index) => (
                    <ListItem key={index}>
                      <ListItemText
                        primary={activity}
                        secondary={`${index === 0 ? 'Just now' : `${index} action${index > 1 ? 's' : ''} ago`}`}
                      />
                    </ListItem>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>

          {/* Quick Stats */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                ISO 20022 Status
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Chip label="11 Message Types Supported" color="success" />
                <Chip label="Banking Standards Compliant" color="primary" />
                <Chip label="Real-time Processing" color="info" />
                <Chip label="Bulk Processing Ready" color="secondary" />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Help Dialog */}
      <Dialog
        open={helpDialogOpen}
        onClose={() => setHelpDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          ISO 20022 Banking Standards
        </DialogTitle>
        <DialogContent>
          <Typography variant="body1" paragraph>
            ISO 20022 is the international standard for financial messaging. This system supports
            the complete suite of ISO 20022 messages for modern banking operations.
          </Typography>

          <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
            Supported Message Types
          </Typography>

          <List>
            {messageTypeInfo.map((info) => (
              <ListItem key={info.type}>
                <ListItemIcon>{info.icon}</ListItemIcon>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography variant="subtitle2">{info.type}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        - {info.name}
                      </Typography>
                    </Box>
                  }
                  secondary={
                    <Box>
                      <Typography variant="body2">{info.description}</Typography>
                      <Typography variant="caption" color="primary">
                        {info.direction}
                      </Typography>
                    </Box>
                  }
                />
              </ListItem>
            ))}
          </List>

          <Alert severity="info" sx={{ mt: 3 }}>
            <Typography variant="body2">
              <strong>Standards Compliance:</strong> All messages follow ISO 20022 specifications
              and are compatible with SWIFT networks, payment schemes, and core banking systems.
            </Typography>
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setHelpDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Iso20022Page;