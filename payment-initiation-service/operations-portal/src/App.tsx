import React from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Box } from '@mui/material';
import { AuthProvider, TenantProvider, NotificationProvider } from './contexts';
import { PrivateRoute, GlobalErrorBoundary } from './components';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import ServiceManagement from './pages/ServiceManagement';
import PaymentRepair from './pages/PaymentRepair';
import TransactionEnquiries from './pages/TransactionEnquiries';
import ReconciliationMonitoring from './pages/ReconciliationMonitoring';
import ChannelOnboarding from './pages/ChannelOnboarding';
import ClearingSystemOnboarding from './pages/ClearingSystemOnboarding';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
      light: '#42a5f5',
      dark: '#1565c0',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#f5f5f5',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 600,
    },
    h5: {
      fontWeight: 500,
    },
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          borderRadius: '8px',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: '6px',
        },
      },
    },
  },
});

function App() {
  return (
    <GlobalErrorBoundary>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AuthProvider>
          <TenantProvider>
            <NotificationProvider>
              <Router>
                <Routes>
                  <Route path="/login" element={<Login />} />
                  <Route path="/*" element={
                    <PrivateRoute>
                      <Box sx={{ display: 'flex', minHeight: '100vh' }}>
                        <Navbar />
                        <Box component="main" sx={{ flexGrow: 1, p: 3, ml: '240px' }}>
                          <Routes>
                            <Route path="/" element={<Dashboard />} />
                            <Route path="/services" element={<ServiceManagement />} />
                            <Route path="/payment-repair" element={<PaymentRepair />} />
                            <Route path="/transactions" element={<TransactionEnquiries />} />
                            <Route path="/reconciliation" element={<ReconciliationMonitoring />} />
                            <Route path="/channel-onboarding" element={<ChannelOnboarding />} />
                            <Route path="/clearing-onboarding" element={<ClearingSystemOnboarding />} />
                          </Routes>
                        </Box>
                      </Box>
                    </PrivateRoute>
                  } />
                </Routes>
              </Router>
            </NotificationProvider>
          </TenantProvider>
        </AuthProvider>
      </ThemeProvider>
    </GlobalErrorBoundary>
  );
}

export default App;