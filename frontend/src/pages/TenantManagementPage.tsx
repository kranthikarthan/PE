import React, { useState } from 'react';
import { Box, Typography, Tabs, Tab, Paper } from '@mui/material';
import TenantCloningManagement from '../components/TenantManagement';
import TenantAuthConfiguration from '../components/tenant/TenantAuthConfiguration';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`tenant-tabpanel-${index}`}
      aria-labelledby={`tenant-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const TenantManagementPage: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [selectedTenantId, setSelectedTenantId] = useState<string>('');

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleTenantSelect = (tenantId: string) => {
    setSelectedTenantId(tenantId);
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Tenant Management
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Manage tenant configurations, cloning, versioning, migration, and authentication across environments.
      </Typography>
      
      <Paper sx={{ width: '100%' }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={handleTabChange} aria-label="tenant management tabs">
            <Tab label="Tenant Cloning" />
            <Tab label="Authentication Configuration" />
          </Tabs>
        </Box>
        
        <TabPanel value={tabValue} index={0}>
          <TenantCloningManagement onTenantSelect={handleTenantSelect} />
        </TabPanel>
        
        <TabPanel value={tabValue} index={1}>
          {selectedTenantId ? (
            <TenantAuthConfiguration 
              tenantId={selectedTenantId}
              onConfigurationChange={() => {
                // Refresh tenant list or other relevant data
              }}
            />
          ) : (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="h6" color="text.secondary">
                Please select a tenant from the Tenant Cloning tab to configure authentication
              </Typography>
            </Box>
          )}
        </TabPanel>
      </Paper>
    </Box>
  );
};

export default TenantManagementPage;