/**
 * MetricCard Component
 * 
 * Reusable metric display card component with trend indicators and formatting.
 * Provides consistent metric visualization across the application.
 */

import React from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  Chip,
  IconButton,
  Tooltip,
  LinearProgress,
  Skeleton,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  TrendingFlat,
  Info,
  Refresh,
  Warning,
  Error,
  CheckCircle,
} from '@mui/icons-material';

export type MetricTrend = 'up' | 'down' | 'flat' | 'unknown';

export type MetricStatus = 'success' | 'warning' | 'error' | 'info' | 'neutral';

export interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  trend?: MetricTrend;
  trendValue?: string | number;
  status?: MetricStatus;
  loading?: boolean;
  error?: string;
  icon?: React.ReactNode;
  color?: 'primary' | 'secondary' | 'success' | 'error' | 'warning' | 'info';
  size?: 'small' | 'medium' | 'large';
  showTrend?: boolean;
  showStatus?: boolean;
  showProgress?: boolean;
  progress?: number;
  maxProgress?: number;
  onClick?: () => void;
  onRefresh?: () => void;
  tooltip?: string;
  sx?: any;
}

const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtitle,
  trend = 'unknown',
  trendValue,
  status = 'neutral',
  loading = false,
  error,
  icon,
  color = 'primary',
  size = 'medium',
  showTrend = true,
  showStatus = true,
  showProgress = false,
  progress = 0,
  maxProgress = 100,
  onClick,
  onRefresh,
  tooltip,
  sx,
}) => {
  // Get trend icon and color
  const getTrendIcon = () => {
    switch (trend) {
      case 'up':
        return <TrendingUp color="success" />;
      case 'down':
        return <TrendingDown color="error" />;
      case 'flat':
        return <TrendingFlat color="info" />;
      default:
        return <TrendingFlat color="disabled" />;
    }
  };

  // Get status icon and color
  const getStatusIcon = () => {
    switch (status) {
      case 'success':
        return <CheckCircle color="success" />;
      case 'warning':
        return <Warning color="warning" />;
      case 'error':
        return <Error color="error" />;
      case 'info':
        return <Info color="info" />;
      default:
        return null;
    }
  };

  // Get trend color
  const getTrendColor = () => {
    switch (trend) {
      case 'up':
        return 'success';
      case 'down':
        return 'error';
      case 'flat':
        return 'info';
      default:
        return 'default';
    }
  };

  // Get status color
  const getStatusColor = () => {
    switch (status) {
      case 'success':
        return 'success';
      case 'warning':
        return 'warning';
      case 'error':
        return 'error';
      case 'info':
        return 'info';
      default:
        return 'default';
    }
  };

  // Get size styles
  const getSizeStyles = () => {
    switch (size) {
      case 'small':
        return {
          padding: 1,
          minHeight: 80,
        };
      case 'large':
        return {
          padding: 3,
          minHeight: 120,
        };
      default:
        return {
          padding: 2,
          minHeight: 100,
        };
    }
  };

  // Format value
  const formatValue = (val: string | number) => {
    if (typeof val === 'number') {
      return val.toLocaleString();
    }
    return val;
  };

  // Get progress percentage
  const getProgressPercentage = () => {
    if (maxProgress === 0) return 0;
    return Math.min((progress / maxProgress) * 100, 100);
  };

  // Loading skeleton
  if (loading) {
    return (
      <Card sx={{ ...getSizeStyles(), ...sx }}>
        <CardContent>
          <Box display="flex" alignItems="center" justifyContent="space-between" mb={1}>
            <Skeleton variant="text" width="60%" height={24} />
            <Skeleton variant="circular" width={24} height={24} />
          </Box>
          <Skeleton variant="text" width="40%" height={32} />
          <Skeleton variant="text" width="80%" height={20} />
        </CardContent>
      </Card>
    );
  }

  // Error state
  if (error) {
    return (
      <Card sx={{ ...getSizeStyles(), ...sx }}>
        <CardContent>
          <Box display="flex" alignItems="center" justifyContent="space-between" mb={1}>
            <Typography variant="body2" color="text.secondary">
              {title}
            </Typography>
            <Error color="error" />
          </Box>
          <Typography variant="body2" color="error">
            {error}
          </Typography>
        </CardContent>
      </Card>
    );
  }

  const cardContent = (
    <Card
      sx={{
        ...getSizeStyles(),
        cursor: onClick ? 'pointer' : 'default',
        '&:hover': onClick ? { boxShadow: 2 } : {},
        ...sx,
      }}
      onClick={onClick}
    >
      <CardContent sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        {/* Header */}
        <Box display="flex" alignItems="center" justifyContent="space-between" mb={1}>
          <Typography
            variant={size === 'large' ? 'h6' : 'body2'}
            color="text.secondary"
            sx={{ fontWeight: 500 }}
          >
            {title}
          </Typography>
          <Box display="flex" alignItems="center" gap={1}>
            {showStatus && getStatusIcon()}
            {onRefresh && (
              <Tooltip title="Refresh">
                <IconButton
                  size="small"
                  onClick={(e) => {
                    e.stopPropagation();
                    onRefresh();
                  }}
                >
                  <Refresh />
                </IconButton>
              </Tooltip>
            )}
          </Box>
        </Box>

        {/* Value */}
        <Box display="flex" alignItems="center" gap={1} mb={1}>
          {icon && (
            <Box display="flex" alignItems="center">
              {icon}
            </Box>
          )}
          <Typography
            variant={size === 'large' ? 'h3' : size === 'small' ? 'h5' : 'h4'}
            color={`${color}.main`}
            sx={{ fontWeight: 'bold' }}
          >
            {formatValue(value)}
          </Typography>
        </Box>

        {/* Trend */}
        {showTrend && trend !== 'unknown' && (
          <Box display="flex" alignItems="center" gap={1} mb={1}>
            {getTrendIcon()}
            <Typography
              variant="body2"
              color={`${getTrendColor()}.main`}
              sx={{ fontWeight: 500 }}
            >
              {trendValue && `${trendValue}`}
            </Typography>
          </Box>
        )}

        {/* Subtitle */}
        {subtitle && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            {subtitle}
          </Typography>
        )}

        {/* Progress Bar */}
        {showProgress && (
          <Box mt="auto">
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={0.5}>
              <Typography variant="caption" color="text.secondary">
                Progress
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {progress} / {maxProgress}
              </Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={getProgressPercentage()}
              color={color as any}
              sx={{ height: 6, borderRadius: 3 }}
            />
          </Box>
        )}

        {/* Status Chip */}
        {showStatus && status !== 'neutral' && (
          <Box mt={1}>
            <Chip
              label={status.toUpperCase()}
              size="small"
              color={getStatusColor() as any}
              variant="outlined"
            />
          </Box>
        )}
      </CardContent>
    </Card>
  );

  if (tooltip) {
    return (
      <Tooltip title={tooltip}>
        {cardContent}
      </Tooltip>
    );
  }

  return cardContent;
};

export default MetricCard;
