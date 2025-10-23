/**
 * Global error boundary for catching and handling React errors
 */

import React, { Component, ErrorInfo, ReactNode } from 'react';
import { 
  Box, 
  Typography, 
  Button, 
  Alert, 
  AlertTitle,
  Paper,
  Container,
  Stack
} from '@mui/material';
import { Refresh, BugReport, Home } from '@mui/icons-material';
import { errorHandler, AppError, ErrorType, ErrorSeverity } from '../utils/errorTypes';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: AppError) => void;
}

interface State {
  hasError: boolean;
  error: AppError | null;
  errorInfo: ErrorInfo | null;
}

class GlobalErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null
    };
  }

  static getDerivedStateFromError(error: Error): Partial<State> {
    return {
      hasError: true,
      error: errorHandler.handleError(error, {
        source: 'GlobalErrorBoundary'
      })
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    const appError = errorHandler.handleError(error, {
      source: 'GlobalErrorBoundary',
      details: {
        componentStack: errorInfo.componentStack,
        errorBoundary: true
      }
    });

    this.setState({
      error: appError,
      errorInfo
    });

    // Call custom error handler if provided
    if (this.props.onError) {
      this.props.onError(appError);
    }
  }

  handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null
    });
  };

  handleReload = () => {
    window.location.reload();
  };

  handleGoHome = () => {
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      // Use custom fallback if provided
      if (this.props.fallback) {
        return this.props.fallback;
      }

      const { error } = this.state;
      const isCritical = error?.severity === ErrorSeverity.CRITICAL;
      const isRetryable = error?.retryable ?? false;

      return (
        <Container maxWidth="md" sx={{ mt: 4 }}>
          <Paper elevation={3} sx={{ p: 4 }}>
            <Stack spacing={3}>
              <Alert 
                severity={isCritical ? 'error' : 'warning'}
                icon={<BugReport />}
              >
                <AlertTitle>
                  {isCritical ? 'Critical Error' : 'Application Error'}
                </AlertTitle>
                <Typography variant="body1">
                  {error?.userMessage || 'An unexpected error occurred. Please try again.'}
                </Typography>
              </Alert>

              <Box>
                <Typography variant="h6" gutterBottom>
                  Error Details
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Error Type: {error?.type || 'Unknown'}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Severity: {error?.severity || 'Unknown'}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Time: {error?.timestamp?.toLocaleString() || 'Unknown'}
                </Typography>
                {error?.message && (
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Message: {error.message}
                  </Typography>
                )}
              </Box>

              <Stack direction="row" spacing={2} justifyContent="center">
                {isRetryable && (
                  <Button
                    variant="contained"
                    startIcon={<Refresh />}
                    onClick={this.handleRetry}
                    color="primary"
                  >
                    Try Again
                  </Button>
                )}
                
                <Button
                  variant="outlined"
                  startIcon={<Home />}
                  onClick={this.handleGoHome}
                >
                  Go Home
                </Button>
                
                <Button
                  variant="outlined"
                  onClick={this.handleReload}
                >
                  Reload Page
                </Button>
              </Stack>

              {process.env.NODE_ENV === 'development' && this.state.errorInfo && (
                <Box sx={{ mt: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Development Information
                  </Typography>
                  <Box 
                    component="pre" 
                    sx={{ 
                      backgroundColor: 'grey.100', 
                      p: 2, 
                      borderRadius: 1,
                      overflow: 'auto',
                      fontSize: '0.875rem'
                    }}
                  >
                    {this.state.errorInfo.componentStack}
                  </Box>
                </Box>
              )}
            </Stack>
          </Paper>
        </Container>
      );
    }

    return this.props.children;
  }
}

export default GlobalErrorBoundary;
