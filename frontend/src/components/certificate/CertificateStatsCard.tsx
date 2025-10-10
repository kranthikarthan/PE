import React from 'react';
import {
  Card,
  CardContent,
  Box,
  Typography,
  Avatar
} from '@mui/material';
import { SvgIconComponent } from '@mui/icons-material';

interface CertificateStatsCardProps {
  title: string;
  value: number;
  color: 'primary' | 'secondary' | 'success' | 'error' | 'warning' | 'info';
  icon: React.ReactElement<SvgIconComponent>;
  subtitle?: string;
  trend?: {
    value: number;
    direction: 'up' | 'down' | 'neutral';
  };
}

const CertificateStatsCard: React.FC<CertificateStatsCardProps> = ({
  title,
  value,
  color,
  icon,
  subtitle,
  trend
}) => {
  const getColorValue = (color: string) => {
    switch (color) {
      case 'primary':
        return '#1976d2';
      case 'secondary':
        return '#dc004e';
      case 'success':
        return '#2e7d32';
      case 'error':
        return '#d32f2f';
      case 'warning':
        return '#ed6c02';
      case 'info':
        return '#0288d1';
      default:
        return '#1976d2';
    }
  };

  const getTrendColor = (direction: string) => {
    switch (direction) {
      case 'up':
        return '#2e7d32';
      case 'down':
        return '#d32f2f';
      case 'neutral':
        return '#666';
      default:
        return '#666';
    }
  };

  const getTrendIcon = (direction: string) => {
    switch (direction) {
      case 'up':
        return '↗';
      case 'down':
        return '↘';
      case 'neutral':
        return '→';
      default:
        return '→';
    }
  };

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Avatar
            sx={{
              backgroundColor: getColorValue(color),
              mr: 2,
              width: 56,
              height: 56
            }}
          >
            {icon}
          </Avatar>
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
              {value.toLocaleString()}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {title}
            </Typography>
          </Box>
        </Box>
        
        {subtitle && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            {subtitle}
          </Typography>
        )}
        
        {trend && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography
              variant="body2"
              sx={{
                color: getTrendColor(trend.direction),
                fontWeight: 'medium'
              }}
            >
              {getTrendIcon(trend.direction)} {Math.abs(trend.value)}%
            </Typography>
            <Typography variant="body2" color="text.secondary">
              vs last period
            </Typography>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default CertificateStatsCard;