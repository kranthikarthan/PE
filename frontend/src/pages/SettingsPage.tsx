import React from 'react';
import { Box, Typography } from '@mui/material';

const SettingsPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>
        Settings
      </Typography>
      <Typography variant="body1">
        System settings interface coming soon...
      </Typography>
    </Box>
  );
};

export default SettingsPage;