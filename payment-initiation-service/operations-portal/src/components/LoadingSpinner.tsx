/**
 * LoadingSpinner Component
 * 
 * Consistent loading indicator component with various sizes and styles.
 * Provides standardized loading states across the application.
 */

import React from 'react';
import {
  Box,
  CircularProgress,
  LinearProgress,
  Typography,
  Skeleton,
  Fade,
} from '@mui/material';

export interface LoadingSpinnerProps {
  size?: number | string;
  color?: 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' | 'info';
  variant?: 'circular' | 'linear' | 'skeleton';
  message?: string;
  fullScreen?: boolean;
  overlay?: boolean;
  thickness?: number;
  disableShrink?: boolean;
  sx?: any;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 40,
  color = 'primary',
  variant = 'circular',
  message,
  fullScreen = false,
  overlay = false,
  thickness = 3.6,
  disableShrink = false,
  sx,
}) => {
  const spinnerContent = (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
        ...(fullScreen && {
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          zIndex: 9999,
          backgroundColor: 'rgba(255, 255, 255, 0.8)',
        }),
        ...(overlay && {
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          zIndex: 1,
          backgroundColor: 'rgba(255, 255, 255, 0.8)',
        }),
        ...sx,
      }}
    >
      {variant === 'circular' && (
        <CircularProgress
          size={size}
          color={color}
          thickness={thickness}
          disableShrink={disableShrink}
        />
      )}
      
      {variant === 'linear' && (
        <LinearProgress
          color={color}
          sx={{ width: '100%', maxWidth: 300 }}
        />
      )}
      
      {variant === 'skeleton' && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, width: '100%' }}>
          <Skeleton variant="rectangular" width="100%" height={40} />
          <Skeleton variant="text" width="80%" />
          <Skeleton variant="text" width="60%" />
        </Box>
      )}
      
      {message && (
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{ textAlign: 'center' }}
        >
          {message}
        </Typography>
      )}
    </Box>
  );

  if (fullScreen || overlay) {
    return (
      <Fade in timeout={300}>
        {spinnerContent}
      </Fade>
    );
  }

  return spinnerContent;
};

export default LoadingSpinner;
