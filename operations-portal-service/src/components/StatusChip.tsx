/**
 * StatusChip Component
 * 
 * Consistent status display component with predefined status types and colors.
 * Provides standardized status indicators across the application.
 */

import React from 'react';
import {
  Chip,
  ChipProps,
  Tooltip,
} from '@mui/material';
import {
  CheckCircle,
  Error,
  Warning,
  Info,
  Pending,
  Schedule,
  Cancel,
  PlayArrow,
  Pause,
  Stop,
} from '@mui/icons-material';

export type StatusType = 
  | 'success' | 'error' | 'warning' | 'info' | 'pending'
  | 'completed' | 'failed' | 'running' | 'stopped' | 'paused'
  | 'active' | 'inactive' | 'enabled' | 'disabled'
  | 'up' | 'down' | 'degraded' | 'unknown'
  | 'approved' | 'rejected' | 'cancelled'
  | 'processing' | 'queued' | 'timeout';

export interface StatusChipProps extends Omit<ChipProps, 'color' | 'icon'> {
  status: StatusType | string;
  showIcon?: boolean;
  tooltip?: string;
  size?: 'small' | 'medium';
}

const StatusChip: React.FC<StatusChipProps> = ({
  status,
  showIcon = true,
  tooltip,
  size = 'small',
  ...chipProps
}) => {
  // Get status configuration
  const getStatusConfig = (status: StatusType | string) => {
    const normalizedStatus = status.toLowerCase() as StatusType;
    
    switch (normalizedStatus) {
      case 'success':
      case 'completed':
      case 'active':
      case 'enabled':
      case 'up':
      case 'approved':
        return {
          color: 'success' as const,
          icon: <CheckCircle />,
          label: 'Success',
        };
      
      case 'error':
      case 'failed':
      case 'inactive':
      case 'disabled':
      case 'down':
      case 'rejected':
      case 'cancelled':
        return {
          color: 'error' as const,
          icon: <Error />,
          label: 'Error',
        };
      
      case 'warning':
      case 'degraded':
      case 'timeout':
        return {
          color: 'warning' as const,
          icon: <Warning />,
          label: 'Warning',
        };
      
      case 'info':
      case 'unknown':
        return {
          color: 'info' as const,
          icon: <Info />,
          label: 'Info',
        };
      
      case 'pending':
      case 'queued':
        return {
          color: 'default' as const,
          icon: <Pending />,
          label: 'Pending',
        };
      
      case 'running':
      case 'processing':
        return {
          color: 'primary' as const,
          icon: <PlayArrow />,
          label: 'Running',
        };
      
      case 'stopped':
        return {
          color: 'default' as const,
          icon: <Stop />,
          label: 'Stopped',
        };
      
      case 'paused':
        return {
          color: 'warning' as const,
          icon: <Pause />,
          label: 'Paused',
        };
      
      default:
        return {
          color: 'default' as const,
          icon: <Info />,
          label: status,
        };
    }
  };

  const config = getStatusConfig(status);
  
  const chip = (
    <Chip
      icon={showIcon ? config.icon : undefined}
      label={config.label}
      color={config.color}
      size={size}
      variant="filled"
      {...chipProps}
    />
  );

  if (tooltip) {
    return (
      <Tooltip title={tooltip}>
        {chip}
      </Tooltip>
    );
  }

  return chip;
};

export default StatusChip;
