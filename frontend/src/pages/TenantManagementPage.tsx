import React from 'react';
import { Box, Typography } from '@mui/material';
import TenantCloningManagement from '../components/TenantManagement';

const TenantManagementPage: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Tenant Management
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Manage tenant configurations, cloning, versioning, and migration across environments.
      </Typography>
      <TenantCloningManagement />
    </Box>
  );
};

export default TenantManagementPage;