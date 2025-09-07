import React from 'react';
import {
  Button,
  ButtonProps,
  CircularProgress,
  Box,
  Tooltip,
} from '@mui/material';
import {
  Check as CheckIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';

interface ModernButtonProps extends Omit<ButtonProps, 'variant'> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'gradient';
  loading?: boolean;
  success?: boolean;
  error?: boolean;
  icon?: React.ReactNode;
  iconPosition?: 'start' | 'end';
  tooltip?: string;
  fullWidth?: boolean;
  size?: 'small' | 'medium' | 'large';
}

const ModernButton: React.FC<ModernButtonProps> = ({
  variant = 'primary',
  loading = false,
  success = false,
  error = false,
  icon,
  iconPosition = 'start',
  tooltip,
  fullWidth = false,
  size = 'medium',
  children,
  disabled,
  sx,
  ...props
}) => {
  const getVariantProps = () => {
    switch (variant) {
      case 'primary':
        return { variant: 'contained' as const };
      case 'secondary':
        return { variant: 'contained' as const, color: 'secondary' as const };
      case 'outline':
        return { variant: 'outlined' as const };
      case 'ghost':
        return { variant: 'text' as const };
      case 'gradient':
        return { variant: 'contained' as const };
      default:
        return { variant: 'contained' as const };
    }
  };

  const getSizeProps = () => {
    switch (size) {
      case 'small':
        return { size: 'small' as const };
      case 'large':
        return { size: 'large' as const };
      default:
        return { size: 'medium' as const };
    }
  };

  const getIcon = () => {
    if (loading) {
      return <CircularProgress size={16} color="inherit" />;
    }
    if (success) {
      return <CheckIcon fontSize="small" />;
    }
    if (error) {
      return <ErrorIcon fontSize="small" />;
    }
    return icon;
  };

  const buttonContent = (
    <Button
      {...getVariantProps()}
      {...getSizeProps()}
      disabled={disabled || loading}
      startIcon={iconPosition === 'start' ? getIcon() : undefined}
      endIcon={iconPosition === 'end' ? getIcon() : undefined}
      fullWidth={fullWidth}
      sx={{
        position: 'relative',
        overflow: 'hidden',
        borderRadius: 2,
        textTransform: 'none',
        fontWeight: 500,
        transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
        ...(variant === 'gradient' && {
          background: (theme) => theme.palette.primary.main,
          '&:hover': {
            background: (theme) => theme.palette.primary.dark,
            transform: 'translateY(-1px)',
            boxShadow: (theme) => theme.shadows[4],
          },
        }),
        ...(variant === 'ghost' && {
          '&:hover': {
            backgroundColor: (theme) => `${theme.palette.primary.main}08`,
          },
        }),
        ...(success && {
          backgroundColor: (theme) => theme.palette.success.main,
          '&:hover': {
            backgroundColor: (theme) => theme.palette.success.dark,
          },
        }),
        ...(error && {
          backgroundColor: (theme) => theme.palette.error.main,
          '&:hover': {
            backgroundColor: (theme) => theme.palette.error.dark,
          },
        }),
        ...sx,
      }}
      {...props}
    >
      {children}
    </Button>
  );

  if (tooltip) {
    return (
      <Tooltip title={tooltip} arrow>
        <span>{buttonContent}</span>
      </Tooltip>
    );
  }

  return buttonContent;
};

export default ModernButton;