import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  Box,
  Divider,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  Settings as SettingsIcon,
  Build as BuildIcon,
  Search as SearchIcon,
  Assessment as AssessmentIcon,
  AccountTree as AccountTreeIcon,
  Storage as StorageIcon,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';

const drawerWidth = 240;

const menuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/' },
  { text: 'Service Management', icon: <SettingsIcon />, path: '/services' },
  { text: 'Payment Repair', icon: <BuildIcon />, path: '/payment-repair' },
  { text: 'Transaction Enquiries', icon: <SearchIcon />, path: '/transactions' },
  { text: 'Reconciliation & Monitoring', icon: <AssessmentIcon />, path: '/reconciliation' },
  { text: 'Channel Onboarding', icon: <AccountTreeIcon />, path: '/channel-onboarding' },
  { text: 'Clearing System Onboarding', icon: <StorageIcon />, path: '/clearing-onboarding' },
];

const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: drawerWidth,
          boxSizing: 'border-box',
          backgroundColor: '#1e3a8a',
          color: 'white',
        },
      }}
    >
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Typography variant="h5" component="div" sx={{ fontWeight: 'bold' }}>
          Payments Engine
        </Typography>
        <Typography variant="subtitle2" sx={{ opacity: 0.8, mt: 1 }}>
          Operations Portal
        </Typography>
      </Box>
      <Divider sx={{ backgroundColor: 'rgba(255,255,255,0.2)' }} />
      <List sx={{ pt: 2 }}>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              onClick={() => navigate(item.path)}
              sx={{
                mx: 1,
                borderRadius: 1,
                backgroundColor: location.pathname === item.path ? 'rgba(255,255,255,0.1)' : 'transparent',
                '&:hover': {
                  backgroundColor: 'rgba(255,255,255,0.05)',
                },
              }}
            >
              <ListItemIcon sx={{ color: 'white', minWidth: 40 }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText 
                primary={item.text} 
                sx={{ 
                  '& .MuiListItemText-primary': { 
                    fontSize: '0.9rem',
                    fontWeight: location.pathname === item.path ? 600 : 400,
                  }
                }}
              />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </Drawer>
  );
};

export default Navbar;
