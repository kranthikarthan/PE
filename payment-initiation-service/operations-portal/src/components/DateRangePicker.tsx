/**
 * DateRangePicker Component
 * 
 * Reusable date range picker component with predefined ranges and validation.
 * Provides consistent date range selection across the application.
 */

import React, { useState, useCallback } from 'react';
import {
  Box,
  TextField,
  Button,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
  Typography,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  DateRange,
  CalendarToday,
  Clear,
  Today,
  DateRange as DateRangeIcon,
  AccessTime,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

export interface DateRange {
  start: Date | null;
  end: Date | null;
}

export interface PredefinedRange {
  label: string;
  value: string;
  getRange: () => { start: Date; end: Date };
  icon?: React.ReactNode;
}

export interface DateRangePickerProps {
  value?: DateRange;
  onChange?: (range: DateRange) => void;
  onClear?: () => void;
  placeholder?: string;
  disabled?: boolean;
  minDate?: Date;
  maxDate?: Date;
  predefinedRanges?: PredefinedRange[];
  showPredefinedRanges?: boolean;
  showClearButton?: boolean;
  showTodayButton?: boolean;
  format?: string;
  size?: 'small' | 'medium';
  variant?: 'outlined' | 'filled' | 'standard';
  fullWidth?: boolean;
  sx?: any;
}

const DateRangePicker: React.FC<DateRangePickerProps> = ({
  value = { start: null, end: null },
  onChange,
  onClear,
  placeholder = 'Select date range',
  disabled = false,
  minDate,
  maxDate,
  predefinedRanges = [],
  showPredefinedRanges = true,
  showClearButton = true,
  showTodayButton = true,
  format = 'MM/dd/yyyy',
  size = 'medium',
  variant = 'outlined',
  fullWidth = true,
  sx,
}) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [tempRange, setTempRange] = useState<DateRange>(value);

  // Default predefined ranges
  const defaultRanges: PredefinedRange[] = [
    {
      label: 'Today',
      value: 'today',
      getRange: () => {
        const today = new Date();
        return { start: today, end: today };
      },
      icon: <Today />,
    },
    {
      label: 'Yesterday',
      value: 'yesterday',
      getRange: () => {
        const yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        return { start: yesterday, end: yesterday };
      },
      icon: <Today />,
    },
    {
      label: 'Last 7 days',
      value: 'last7days',
      getRange: () => {
        const end = new Date();
        const start = new Date();
        start.setDate(start.getDate() - 6);
        return { start, end };
      },
      icon: <DateRangeIcon />,
    },
    {
      label: 'Last 30 days',
      value: 'last30days',
      getRange: () => {
        const end = new Date();
        const start = new Date();
        start.setDate(start.getDate() - 29);
        return { start, end };
      },
      icon: <DateRangeIcon />,
    },
    {
      label: 'This month',
      value: 'thismonth',
      getRange: () => {
        const now = new Date();
        const start = new Date(now.getFullYear(), now.getMonth(), 1);
        const end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
        return { start, end };
      },
      icon: <CalendarToday />,
    },
    {
      label: 'Last month',
      value: 'lastmonth',
      getRange: () => {
        const now = new Date();
        const start = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        const end = new Date(now.getFullYear(), now.getMonth(), 0);
        return { start, end };
      },
      icon: <CalendarToday />,
    },
  ];

  const ranges = predefinedRanges.length > 0 ? predefinedRanges : defaultRanges;

  // Format date for display
  const formatDate = useCallback((date: Date | null) => {
    if (!date) return '';
    return date.toLocaleDateString('en-US', {
      month: '2-digit',
      day: '2-digit',
      year: 'numeric',
    });
  }, []);

  // Get display text
  const getDisplayText = useCallback(() => {
    if (!value.start && !value.end) return placeholder;
    
    if (value.start && value.end) {
      return `${formatDate(value.start)} - ${formatDate(value.end)}`;
    }
    
    if (value.start) {
      return `From ${formatDate(value.start)}`;
    }
    
    if (value.end) {
      return `Until ${formatDate(value.end)}`;
    }
    
    return placeholder;
  }, [value, formatDate, placeholder]);

  // Handle range change
  const handleRangeChange = useCallback((newRange: DateRange) => {
    setTempRange(newRange);
    onChange?.(newRange);
  }, [onChange]);

  // Handle predefined range select
  const handlePredefinedRangeSelect = useCallback((range: PredefinedRange) => {
    const newRange = range.getRange();
    handleRangeChange(newRange);
    setAnchorEl(null);
  }, [handleRangeChange]);

  // Handle clear
  const handleClear = useCallback(() => {
    const emptyRange = { start: null, end: null };
    setTempRange(emptyRange);
    onChange?.(emptyRange);
    onClear?.();
  }, [onChange, onClear]);

  // Handle today
  const handleToday = useCallback(() => {
    const today = new Date();
    const todayRange = { start: today, end: today };
    handleRangeChange(todayRange);
  }, [handleRangeChange]);

  // Handle menu open
  const handleMenuOpen = useCallback((event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  }, []);

  // Handle menu close
  const handleMenuClose = useCallback(() => {
    setAnchorEl(null);
  }, []);

  // Check if range is valid
  const isValidRange = value.start && value.end && value.start <= value.end;

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ ...sx }}>
        {/* Date Range Display */}
        <Box display="flex" alignItems="center" gap={1}>
          <TextField
            fullWidth={fullWidth}
            value={getDisplayText()}
            placeholder={placeholder}
            disabled={disabled}
            size={size}
            variant={variant}
            InputProps={{
              readOnly: true,
              startAdornment: <DateRange />,
              endAdornment: (
                <Box display="flex" alignItems="center">
                  {showClearButton && (value.start || value.end) && (
                    <Tooltip title="Clear">
                      <IconButton onClick={handleClear} size="small">
                        <Clear />
                      </IconButton>
                    </Tooltip>
                  )}
                  {showPredefinedRanges && (
                    <Tooltip title="Quick ranges">
                      <IconButton onClick={handleMenuOpen} size="small">
                        <AccessTime />
                      </IconButton>
                    </Tooltip>
                  )}
                </Box>
              ),
            }}
            onClick={handleMenuOpen}
            sx={{ cursor: 'pointer' }}
          />
        </Box>

        {/* Predefined Ranges Menu */}
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
          PaperProps={{
            sx: { minWidth: 200 }
          }}
        >
          {showTodayButton && (
            <MenuItem onClick={handleToday}>
              <ListItemIcon>
                <Today />
              </ListItemIcon>
              <ListItemText primary="Today" />
            </MenuItem>
          )}
          
          {ranges.map((range) => (
            <MenuItem
              key={range.value}
              onClick={() => handlePredefinedRangeSelect(range)}
            >
              {range.icon && (
                <ListItemIcon>
                  {range.icon}
                </ListItemIcon>
              )}
              <ListItemText primary={range.label} />
            </MenuItem>
          ))}
        </Menu>

        {/* Individual Date Pickers */}
        <Box display="flex" gap={2} mt={2}>
          <DatePicker
            label="Start Date"
            value={tempRange.start}
            onChange={(date) => handleRangeChange({ ...tempRange, start: date })}
            minDate={minDate}
            maxDate={maxDate || tempRange.end}
            disabled={disabled}
            slotProps={{
              textField: {
                size,
                variant,
                fullWidth: true,
              },
            }}
          />
          
          <DatePicker
            label="End Date"
            value={tempRange.end}
            onChange={(date) => handleRangeChange({ ...tempRange, end: date })}
            minDate={tempRange.start || minDate}
            maxDate={maxDate}
            disabled={disabled}
            slotProps={{
              textField: {
                size,
                variant,
                fullWidth: true,
              },
            }}
          />
        </Box>

        {/* Range Info */}
        {value.start && value.end && (
          <Box mt={2}>
            <Typography variant="body2" color="text.secondary">
              {isValidRange ? (
                <>
                  <Chip
                    label={`${Math.ceil((value.end.getTime() - value.start.getTime()) / (1000 * 60 * 60 * 24)) + 1} days`}
                    size="small"
                    color="primary"
                    variant="outlined"
                  />
                </>
              ) : (
                <Chip
                  label="Invalid range"
                  size="small"
                  color="error"
                  variant="outlined"
                />
              )}
            </Typography>
          </Box>
        )}
      </Box>
    </LocalizationProvider>
  );
};

export default DateRangePicker;
