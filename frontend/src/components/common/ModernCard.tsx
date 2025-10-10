import React from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardProps,
  Box,
  Typography,
  IconButton,
  Tooltip,
  Fade,
} from '@mui/material';
import {
  InfoOutlined as InfoIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
} from '@mui/icons-material';

interface ModernCardProps extends Omit<CardProps, 'title'> {
  title?: string;
  subtitle?: string;
  description?: string;
  icon?: React.ReactNode;
  action?: React.ReactNode;
  collapsible?: boolean;
  defaultExpanded?: boolean;
  tooltip?: string;
  gradient?: boolean;
  elevation?: 'low' | 'medium' | 'high';
  children: React.ReactNode;
}

const ModernCard: React.FC<ModernCardProps> = ({
  title,
  subtitle,
  description,
  icon,
  action,
  collapsible = false,
  defaultExpanded = true,
  tooltip,
  gradient = false,
  elevation = 'medium',
  children,
  sx,
  ...props
}) => {
  const [expanded, setExpanded] = React.useState(defaultExpanded);

  const getElevation = () => {
    switch (elevation) {
      case 'low': return 1;
      case 'high': return 8;
      default: return 3;
    }
  };

  const handleToggle = () => {
    if (collapsible) {
      setExpanded(!expanded);
    }
  };

  return (
    <Card
      sx={{
        position: 'relative',
        overflow: 'hidden',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: (theme) => 
            elevation === 'high' 
              ? theme.shadows[12] 
              : theme.shadows[6],
        },
        ...(gradient && {
          background: (theme) => theme.palette.primary.main,
          color: (theme) => theme.palette.primary.contrastText,
          '& .MuiCardHeader-title': {
            color: 'inherit',
          },
          '& .MuiCardHeader-subheader': {
            color: 'inherit',
            opacity: 0.8,
          },
        }),
        ...sx,
      }}
      elevation={getElevation()}
      {...props}
    >
      {(title || subtitle || description || icon || action) && (
        <CardHeader
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {icon && (
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  {icon}
                </Box>
              )}
              <Box sx={{ flex: 1 }}>
                {title && (
                  <Typography variant="h6" component="h3" fontWeight={600}>
                    {title}
                  </Typography>
                )}
                {subtitle && (
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                    {subtitle}
                  </Typography>
                )}
              </Box>
              {tooltip && (
                <Tooltip title={tooltip} arrow>
                  <IconButton size="small" sx={{ opacity: 0.7 }}>
                    <InfoIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              )}
              {collapsible && (
                <IconButton 
                  size="small" 
                  onClick={handleToggle}
                  sx={{ 
                    transition: 'transform 0.2s',
                    transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
                  }}
                >
                  <ExpandMoreIcon />
                </IconButton>
              )}
            </Box>
          }
          action={action}
          sx={{
            pb: description ? 1 : 2,
            '& .MuiCardHeader-content': {
              overflow: 'hidden',
            },
          }}
        />
      )}
      
      {description && (
        <Box sx={{ px: 2, pb: 1 }}>
          <Typography variant="body2" color="text.secondary">
            {description}
          </Typography>
        </Box>
      )}

      <Fade in={expanded} timeout={300}>
        <CardContent sx={{ pt: title || subtitle || description ? 1 : 2 }}>
          {children}
        </CardContent>
      </Fade>
    </Card>
  );
};

export default ModernCard;