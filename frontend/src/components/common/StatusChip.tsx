import React from 'react';
import {
  Chip,
  ChipProps,
  Box,
  Tooltip,
} from '@mui/material';
import {
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  Schedule as ScheduleIcon,
  Pause as PauseIcon,
  PlayArrow as PlayIcon,
  Stop as StopIcon,
} from '@mui/icons-material';

export type StatusType = 
  | 'success' 
  | 'error' 
  | 'warning' 
  | 'info' 
  | 'pending' 
  | 'active' 
  | 'inactive' 
  | 'paused' 
  | 'running' 
  | 'stopped';

interface StatusChipProps extends Omit<ChipProps, 'color'> {
  status: StatusType;
  label?: string;
  showIcon?: boolean;
  tooltip?: string;
  size?: 'small' | 'medium';
}

const StatusChip: React.FC<StatusChipProps> = ({
  status,
  label,
  showIcon = true,
  tooltip,
  size = 'small',
  sx,
  ...props
}) => {
  const getStatusConfig = () => {
    switch (status) {
      case 'success':
        return {
          color: 'success' as const,
          icon: <CheckCircleIcon fontSize="small" />,
          defaultLabel: 'Success',
        };
      case 'error':
        return {
          color: 'error' as const,
          icon: <ErrorIcon fontSize="small" />,
          defaultLabel: 'Error',
        };
      case 'warning':
        return {
          color: 'warning' as const,
          icon: <WarningIcon fontSize="small" />,
          defaultLabel: 'Warning',
        };
      case 'info':
        return {
          color: 'info' as const,
          icon: <InfoIcon fontSize="small" />,
          defaultLabel: 'Info',
        };
      case 'pending':
        return {
          color: 'default' as const,
          icon: <ScheduleIcon fontSize="small" />,
          defaultLabel: 'Pending',
        };
      case 'active':
        return {
          color: 'success' as const,
          icon: <PlayIcon fontSize="small" />,
          defaultLabel: 'Active',
        };
      case 'inactive':
        return {
          color: 'default' as const,
          icon: <StopIcon fontSize="small" />,
          defaultLabel: 'Inactive',
        };
      case 'paused':
        return {
          color: 'warning' as const,
          icon: <PauseIcon fontSize="small" />,
          defaultLabel: 'Paused',
        };
      case 'running':
        return {
          color: 'info' as const,
          icon: <PlayIcon fontSize="small" />,
          defaultLabel: 'Running',
        };
      case 'stopped':
        return {
          color: 'error' as const,
          icon: <StopIcon fontSize="small" />,
          defaultLabel: 'Stopped',
        };
      default:
        return {
          color: 'default' as const,
          icon: <InfoIcon fontSize="small" />,
          defaultLabel: 'Unknown',
        };
    }
  };

  const config = getStatusConfig();
  const displayLabel = label || config.defaultLabel;

  const chip = (
    <Chip
      icon={showIcon ? config.icon : undefined}
      label={displayLabel}
      color={config.color}
      size={size}
      variant="filled"
      sx={{
        fontWeight: 500,
        borderRadius: 2,
        '& .MuiChip-icon': {
          fontSize: '1rem',
        },
        ...sx,
      }}
      {...props}
    />
  );

  if (tooltip) {
    return (
      <Tooltip title={tooltip} arrow>
        <span>{chip}</span>
      </Tooltip>
    );
  }

  return chip;
};

export default StatusChip;