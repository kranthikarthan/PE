import React from 'react';
import {
  Tooltip,
  IconButton,
  Box,
  Typography,
  Link,
  Chip,
} from '@mui/material';
import {
  HelpOutline as HelpIcon,
  Info as InfoIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';

interface HelpTooltipProps {
  title: string;
  content?: string;
  type?: 'info' | 'warning' | 'error' | 'help';
  maxWidth?: number;
  placement?: 'top' | 'bottom' | 'left' | 'right';
  documentationUrl?: string;
  examples?: string[];
  relatedTopics?: string[];
  children?: React.ReactNode;
}

const HelpTooltip: React.FC<HelpTooltipProps> = ({
  title,
  content,
  type = 'help',
  maxWidth = 300,
  placement = 'top',
  documentationUrl,
  examples,
  relatedTopics,
  children,
}) => {
  const getIcon = () => {
    switch (type) {
      case 'info':
        return <InfoIcon fontSize="small" />;
      case 'warning':
        return <WarningIcon fontSize="small" />;
      case 'error':
        return <ErrorIcon fontSize="small" />;
      default:
        return <HelpIcon fontSize="small" />;
    }
  };

  const getColor = () => {
    switch (type) {
      case 'info':
        return 'info';
      case 'warning':
        return 'warning';
      case 'error':
        return 'error';
      default:
        return 'default';
    }
  };

  const renderTooltipContent = () => (
    <Box sx={{ maxWidth, p: 1 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
        {getIcon()}
        <Typography variant="subtitle2" fontWeight={600}>
          {title}
        </Typography>
      </Box>
      
      {content && (
        <Typography variant="body2" sx={{ mb: 1, lineHeight: 1.5 }}>
          {content}
        </Typography>
      )}
      
      {examples && examples.length > 0 && (
        <Box sx={{ mb: 1 }}>
          <Typography variant="caption" fontWeight={600} color="text.secondary">
            Examples:
          </Typography>
          {examples.map((example, index) => (
            <Typography key={index} variant="caption" display="block" sx={{ fontFamily: 'monospace', mt: 0.5 }}>
              {example}
            </Typography>
          ))}
        </Box>
      )}
      
      {relatedTopics && relatedTopics.length > 0 && (
        <Box sx={{ mb: 1 }}>
          <Typography variant="caption" fontWeight={600} color="text.secondary" sx={{ mb: 0.5, display: 'block' }}>
            Related Topics:
          </Typography>
          <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
            {relatedTopics.map((topic, index) => (
              <Chip key={index} label={topic} size="small" variant="outlined" />
            ))}
          </Box>
        </Box>
      )}
      
      {documentationUrl && (
        <Link
          href={documentationUrl}
          target="_blank"
          rel="noopener noreferrer"
          variant="caption"
          sx={{ textDecoration: 'none' }}
        >
          📖 View Documentation
        </Link>
      )}
    </Box>
  );

  if (children) {
    return (
      <Tooltip
        title={renderTooltipContent()}
        placement={placement}
        arrow
        componentsProps={{
          tooltip: {
            sx: {
              backgroundColor: (theme) => theme.palette.background.paper,
              color: (theme) => theme.palette.text.primary,
              border: (theme) => `1px solid ${theme.palette.divider}`,
              boxShadow: (theme) => theme.shadows[4],
            },
          },
        }}
      >
        <span>{children}</span>
      </Tooltip>
    );
  }

  return (
    <Tooltip
      title={renderTooltipContent()}
      placement={placement}
      arrow
      componentsProps={{
        tooltip: {
          sx: {
            backgroundColor: (theme) => theme.palette.background.paper,
            color: (theme) => theme.palette.text.primary,
            border: (theme) => `1px solid ${theme.palette.divider}`,
            boxShadow: (theme) => theme.shadows[4],
          },
        },
      }}
    >
      <IconButton size="small" color={getColor()}>
        {getIcon()}
      </IconButton>
    </Tooltip>
  );
};

export default HelpTooltip;