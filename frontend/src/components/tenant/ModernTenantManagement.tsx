import React, { useState } from 'react';
import {
  Box,
  Typography,
  Tabs,
  Tab,
  Grid,
  Card,
  CardContent,
  IconButton,
  Tooltip,
  Fab,
  SpeedDial,
  SpeedDialAction,
  SpeedDialIcon,
  Alert,
  Chip,
  Avatar,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  ListItemSecondaryAction,
  Divider,
} from '@mui/material';
import {
  Business as BusinessIcon,
  Settings as SettingsIcon,
  Security as SecurityIcon,
  Payment as PaymentIcon,
  CloudUpload as CloudUploadIcon,
  ContentCopy as CloneIcon,
  Download as ExportIcon,
  Upload as ImportIcon,
  Palette as ThemeIcon,
  Help as HelpIcon,
  Add as AddIcon,
  Dashboard as DashboardIcon,
  Timeline as TimelineIcon,
  Assessment as AnalyticsIcon,
  Notifications as NotificationsIcon,
} from '@mui/icons-material';
import ModernCard from '../common/ModernCard';
import ModernButton from '../common/ModernButton';
import StatusChip from '../common/StatusChip';
import HelpTooltip from '../common/HelpTooltip';
import HelpSystem from '../common/HelpSystem';
import ThemeSelector from '../common/ThemeSelector';
import EnhancedTenantSetupWizard from './EnhancedTenantSetupWizard';
import TenantTodoList from './TenantTodoList';
import MultiLevelAuthConfigurationManager from '../multiLevelAuth/MultiLevelAuthConfigurationManager';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div hidden={value !== index}>
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

const ModernTenantManagement: React.FC = () => {
  const [selectedTab, setSelectedTab] = useState(0);
  const [helpSystemOpen, setHelpSystemOpen] = useState(false);
  const [themeSelectorOpen, setThemeSelectorOpen] = useState(false);
  const [speedDialOpen, setSpeedDialOpen] = useState(false);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setSelectedTab(newValue);
  };

  const speedDialActions = [
    {
      icon: <AddIcon />,
      name: 'New Tenant (Enhanced)',
      tooltip: 'Set up a new tenant with multi-level authentication',
      onClick: () => setSelectedTab(1), // Enhanced Setup Wizard tab
    },
    {
      icon: <SecurityIcon />,
      name: 'Multi-Level Auth',
      tooltip: 'Manage multi-level authentication configuration',
      onClick: () => setSelectedTab(2), // Multi-Level Auth tab
    },
    {
      icon: <CloneIcon />,
      name: 'Clone Tenant',
      tooltip: 'Clone an existing tenant configuration',
      onClick: () => console.log('Clone tenant'),
    },
    {
      icon: <ExportIcon />,
      name: 'Export Configuration',
      tooltip: 'Export tenant configuration',
      onClick: () => console.log('Export configuration'),
    },
    {
      icon: <ImportIcon />,
      name: 'Import Configuration',
      tooltip: 'Import tenant configuration',
      onClick: () => console.log('Import configuration'),
    },
  ];

  const renderDashboard = () => (
    <Box>
      {/* Welcome Section */}
      <ModernCard
        title="Welcome to Tenant Management"
        subtitle="Manage your tenant configurations with ease"
        gradient
        sx={{ mb: 3 }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Avatar sx={{ bgcolor: 'primary.main', width: 56, height: 56 }}>
            <BusinessIcon fontSize="large" />
          </Avatar>
          <Box>
            <Typography variant="h6" fontWeight={600}>
              Tenant Management Dashboard
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Configure, clone, and manage tenant settings across environments
            </Typography>
          </Box>
        </Box>
      </ModernCard>

      {/* Quick Stats */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <ModernCard elevation="low">
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar sx={{ bgcolor: 'success.main' }}>
                <BusinessIcon />
              </Avatar>
              <Box>
                <Typography variant="h4" fontWeight={600}>
                  12
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Active Tenants
                </Typography>
              </Box>
            </Box>
          </ModernCard>
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <ModernCard elevation="low">
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar sx={{ bgcolor: 'info.main' }}>
                <SettingsIcon />
              </Avatar>
              <Box>
                <Typography variant="h4" fontWeight={600}>
                  8
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Configurations
                </Typography>
              </Box>
            </Box>
          </ModernCard>
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <ModernCard elevation="low">
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar sx={{ bgcolor: 'warning.main' }}>
                <SecurityIcon />
              </Avatar>
              <Box>
                <Typography variant="h4" fontWeight={600}>
                  5
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Security Policies
                </Typography>
              </Box>
            </Box>
          </ModernCard>
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <ModernCard elevation="low">
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar sx={{ bgcolor: 'error.main' }}>
                <PaymentIcon />
              </Avatar>
              <Box>
                <Typography variant="h4" fontWeight={600}>
                  3
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Payment Types
                </Typography>
              </Box>
            </Box>
          </ModernCard>
        </Grid>
      </Grid>

      {/* Recent Activity */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <ModernCard
            title="Recent Activity"
            subtitle="Latest tenant management activities"
            tooltip="Shows recent changes and operations performed on tenants"
          >
            <List>
              <ListItem>
                <ListItemAvatar>
                  <Avatar sx={{ bgcolor: 'success.main' }}>
                    <CloneIcon />
                  </Avatar>
                </ListItemAvatar>
                <ListItemText
                  primary="Tenant cloned successfully"
                  secondary="tenant-001 → tenant-002 (2 hours ago)"
                />
                <ListItemSecondaryAction>
                  <StatusChip status="success" label="Completed" size="small" />
                </ListItemSecondaryAction>
              </ListItem>
              
              <Divider />
              
              <ListItem>
                <ListItemAvatar>
                  <Avatar sx={{ bgcolor: 'info.main' }}>
                    <SettingsIcon />
                  </Avatar>
                </ListItemAvatar>
                <ListItemText
                  primary="Configuration updated"
                  secondary="tenant-003 security settings (4 hours ago)"
                />
                <ListItemSecondaryAction>
                  <StatusChip status="info" label="Updated" size="small" />
                </ListItemSecondaryAction>
              </ListItem>
              
              <Divider />
              
              <ListItem>
                <ListItemAvatar>
                  <Avatar sx={{ bgcolor: 'warning.main' }}>
                    <ExportIcon />
                  </Avatar>
                </ListItemAvatar>
                <ListItemText
                  primary="Configuration exported"
                  secondary="tenant-001 to JSON format (1 day ago)"
                />
                <ListItemSecondaryAction>
                  <StatusChip status="success" label="Exported" size="small" />
                </ListItemSecondaryAction>
              </ListItem>
            </List>
          </ModernCard>
        </Grid>
        
        <Grid item xs={12} md={6}>
          <ModernCard
            title="Quick Actions"
            subtitle="Common tenant management tasks"
          >
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <ModernButton
                  variant="outline"
                  fullWidth
                  startIcon={<AddIcon />}
                  onClick={() => setSelectedTab(1)}
                >
                  New Tenant
                </ModernButton>
              </Grid>
              <Grid item xs={6}>
                <ModernButton
                  variant="outline"
                  fullWidth
                  startIcon={<CloneIcon />}
                >
                  Clone Tenant
                </ModernButton>
              </Grid>
              <Grid item xs={6}>
                <ModernButton
                  variant="outline"
                  fullWidth
                  startIcon={<ExportIcon />}
                >
                  Export Config
                </ModernButton>
              </Grid>
              <Grid item xs={6}>
                <ModernButton
                  variant="outline"
                  fullWidth
                  startIcon={<ImportIcon />}
                >
                  Import Config
                </ModernButton>
              </Grid>
            </Grid>
          </ModernCard>
        </Grid>
      </Grid>
    </Box>
  );


  const renderTodoList = () => (
    <TenantTodoList />
  );

  const renderAnalytics = () => (
    <Box>
      <Alert severity="info" sx={{ mb: 3 }}>
        Analytics and reporting features are coming soon. Track tenant usage, performance metrics, and configuration changes.
      </Alert>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <ModernCard
            title="Tenant Usage Statistics"
            subtitle="Usage patterns and performance metrics"
          >
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <AnalyticsIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                Analytics Dashboard
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Coming soon - Track tenant performance and usage
              </Typography>
            </Box>
          </ModernCard>
        </Grid>
        
        <Grid item xs={12} md={6}>
          <ModernCard
            title="Configuration History"
            subtitle="Track changes and modifications over time"
          >
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <TimelineIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                Change History
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Coming soon - View configuration change timeline
              </Typography>
            </Box>
          </ModernCard>
        </Grid>
      </Grid>
    </Box>
  );

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* Header */}
      <Box sx={{ 
        bgcolor: 'background.paper', 
        borderBottom: 1, 
        borderColor: 'divider',
        px: 3,
        py: 2,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <BusinessIcon color="primary" sx={{ fontSize: 32 }} />
          <Box>
            <Typography variant="h5" fontWeight={600}>
              Tenant Management
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Professional tenant configuration and management system
            </Typography>
          </Box>
        </Box>
        
        <Box sx={{ display: 'flex', gap: 1 }}>
          <HelpTooltip
            title="Help & Documentation"
            content="Access comprehensive help articles, tutorials, and documentation for tenant management."
            documentationUrl="/docs/tenant-management"
          >
            <IconButton onClick={() => setHelpSystemOpen(true)}>
              <HelpIcon />
            </IconButton>
          </HelpTooltip>
          
          <HelpTooltip
            title="Theme Customization"
            content="Customize the interface theme and color palette to match your bank's branding."
          >
            <IconButton onClick={() => setThemeSelectorOpen(true)}>
              <ThemeIcon />
            </IconButton>
          </HelpTooltip>
        </Box>
      </Box>

      {/* Main Content */}
      <Box sx={{ flexGrow: 1, display: 'flex' }}>
        {/* Navigation Tabs */}
        <Box sx={{ 
          width: 240, 
          bgcolor: 'background.paper', 
          borderRight: 1, 
          borderColor: 'divider',
          pt: 2
        }}>
          <Tabs
            orientation="vertical"
            value={selectedTab}
            onChange={handleTabChange}
            sx={{
              '& .MuiTab-root': {
                alignItems: 'flex-start',
                textAlign: 'left',
                minHeight: 48,
                px: 3,
              },
            }}
          >
            <Tab
              icon={<DashboardIcon />}
              label="Dashboard"
              iconPosition="start"
            />
            <Tab
              icon={<AddIcon />}
              label="Enhanced Setup"
              iconPosition="start"
            />
            <Tab
              icon={<SecurityIcon />}
              label="Multi-Level Auth"
              iconPosition="start"
            />
            <Tab
              icon={<TimelineIcon />}
              label="Todo List"
              iconPosition="start"
            />
            <Tab
              icon={<AnalyticsIcon />}
              label="Analytics"
              iconPosition="start"
            />
          </Tabs>
        </Box>

        {/* Content Area */}
        <Box sx={{ flexGrow: 1 }}>
          <TabPanel value={selectedTab} index={0}>
            {renderDashboard()}
          </TabPanel>
          <TabPanel value={selectedTab} index={1}>
            <EnhancedTenantSetupWizard />
          </TabPanel>
          <TabPanel value={selectedTab} index={2}>
            <MultiLevelAuthConfigurationManager />
          </TabPanel>
          <TabPanel value={selectedTab} index={3}>
            {renderTodoList()}
          </TabPanel>
          <TabPanel value={selectedTab} index={4}>
            {renderAnalytics()}
          </TabPanel>
        </Box>
      </Box>

      {/* Speed Dial for Quick Actions */}
      <SpeedDial
        ariaLabel="Quick Actions"
        sx={{ position: 'fixed', bottom: 16, right: 16 }}
        icon={<SpeedDialIcon />}
        onClose={() => setSpeedDialOpen(false)}
        onOpen={() => setSpeedDialOpen(true)}
        open={speedDialOpen}
      >
        {speedDialActions.map((action) => (
          <SpeedDialAction
            key={action.name}
            icon={action.icon}
            tooltipTitle={action.tooltip}
            onClick={action.onClick}
          />
        ))}
      </SpeedDial>

      {/* Help System */}
      <HelpSystem
        open={helpSystemOpen}
        onClose={() => setHelpSystemOpen(false)}
      />

      {/* Theme Selector */}
      <ThemeSelector
        open={themeSelectorOpen}
        onClose={() => setThemeSelectorOpen(false)}
      />
    </Box>
  );
};

export default ModernTenantManagement;