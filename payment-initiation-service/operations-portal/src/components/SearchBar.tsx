/**
 * SearchBar Component
 * 
 * Reusable search input component with debouncing and advanced features.
 * Provides consistent search functionality across the application.
 */

import React, { useState, useEffect, useCallback } from 'react';
import {
  TextField,
  InputAdornment,
  IconButton,
  Box,
  Chip,
  Typography,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Select,
} from '@mui/material';
import {
  Search,
  Clear,
  FilterList,
  History,
  TrendingUp,
} from '@mui/icons-material';

export interface SearchSuggestion {
  id: string;
  label: string;
  value: string;
  category?: string;
  icon?: React.ReactNode;
}

export interface SearchFilter {
  id: string;
  label: string;
  value: string;
  options: Array<{ value: string; label: string }>;
}

export interface SearchBarProps {
  placeholder?: string;
  value?: string;
  onChange?: (value: string) => void;
  onSearch?: (query: string) => void;
  onClear?: () => void;
  debounceMs?: number;
  minLength?: number;
  maxLength?: number;
  suggestions?: SearchSuggestion[];
  onSuggestionSelect?: (suggestion: SearchSuggestion) => void;
  filters?: SearchFilter[];
  onFilterChange?: (filterId: string, value: string) => void;
  showFilters?: boolean;
  showHistory?: boolean;
  history?: string[];
  onHistorySelect?: (query: string) => void;
  onHistoryClear?: () => void;
  showTrending?: boolean;
  trending?: string[];
  onTrendingSelect?: (trend: string) => void;
  disabled?: boolean;
  fullWidth?: boolean;
  size?: 'small' | 'medium';
  variant?: 'outlined' | 'filled' | 'standard';
  sx?: any;
}

const SearchBar: React.FC<SearchBarProps> = ({
  placeholder = 'Search...',
  value = '',
  onChange,
  onSearch,
  onClear,
  debounceMs = 300,
  minLength = 2,
  maxLength = 100,
  suggestions = [],
  onSuggestionSelect,
  filters = [],
  onFilterChange,
  showFilters = false,
  showHistory = false,
  history = [],
  onHistorySelect,
  onHistoryClear,
  showTrending = false,
  trending = [],
  onTrendingSelect,
  disabled = false,
  fullWidth = true,
  size = 'medium',
  variant = 'outlined',
  sx,
}) => {
  const [searchValue, setSearchValue] = useState(value);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [showFiltersMenu, setShowFiltersMenu] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [activeFilters, setActiveFilters] = useState<Record<string, string>>({});

  // Debounced search effect
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchValue.length >= minLength) {
        onSearch?.(searchValue);
      }
    }, debounceMs);

    return () => clearTimeout(timer);
  }, [searchValue, debounceMs, minLength, onSearch]);

  // Update internal value when prop changes
  useEffect(() => {
    setSearchValue(value);
  }, [value]);

  // Handle input change
  const handleInputChange = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = event.target.value;
    
    if (maxLength && newValue.length > maxLength) {
      return;
    }
    
    setSearchValue(newValue);
    onChange?.(newValue);
    
    // Show suggestions if there are any
    if (suggestions.length > 0 && newValue.length >= minLength) {
      setShowSuggestions(true);
    } else {
      setShowSuggestions(false);
    }
  }, [onChange, maxLength, suggestions.length, minLength]);

  // Handle search
  const handleSearch = useCallback(() => {
    if (searchValue.length >= minLength) {
      onSearch?.(searchValue);
      setShowSuggestions(false);
    }
  }, [searchValue, minLength, onSearch]);

  // Handle clear
  const handleClear = useCallback(() => {
    setSearchValue('');
    onChange?.('');
    onClear?.();
    setShowSuggestions(false);
  }, [onChange, onClear]);

  // Handle suggestion select
  const handleSuggestionSelect = useCallback((suggestion: SearchSuggestion) => {
    setSearchValue(suggestion.value);
    onChange?.(suggestion.value);
    onSuggestionSelect?.(suggestion);
    setShowSuggestions(false);
  }, [onChange, onSuggestionSelect]);

  // Handle history select
  const handleHistorySelect = useCallback((query: string) => {
    setSearchValue(query);
    onChange?.(query);
    onHistorySelect?.(query);
    setShowSuggestions(false);
  }, [onChange, onHistorySelect]);

  // Handle trending select
  const handleTrendingSelect = useCallback((trend: string) => {
    setSearchValue(trend);
    onChange?.(trend);
    onTrendingSelect?.(trend);
    setShowSuggestions(false);
  }, [onChange, onTrendingSelect]);

  // Handle filter change
  const handleFilterChange = useCallback((filterId: string, filterValue: string) => {
    setActiveFilters(prev => ({
      ...prev,
      [filterId]: filterValue,
    }));
    onFilterChange?.(filterId, filterValue);
  }, [onFilterChange]);

  // Handle key press
  const handleKeyPress = useCallback((event: React.KeyboardEvent) => {
    if (event.key === 'Enter') {
      handleSearch();
    } else if (event.key === 'Escape') {
      setShowSuggestions(false);
    }
  }, [handleSearch]);

  // Handle focus
  const handleFocus = useCallback(() => {
    if (suggestions.length > 0 || history.length > 0 || trending.length > 0) {
      setShowSuggestions(true);
    }
  }, [suggestions.length, history.length, trending.length]);

  // Handle blur
  const handleBlur = useCallback(() => {
    // Delay hiding suggestions to allow clicking on them
    setTimeout(() => {
      setShowSuggestions(false);
    }, 200);
  }, []);

  // Get filtered suggestions
  const filteredSuggestions = suggestions.filter(suggestion =>
    suggestion.label.toLowerCase().includes(searchValue.toLowerCase()) ||
    suggestion.value.toLowerCase().includes(searchValue.toLowerCase())
  );

  // Get active filter chips
  const activeFilterChips = Object.entries(activeFilters)
    .filter(([_, value]) => value)
    .map(([filterId, value]) => {
      const filter = filters.find(f => f.id === filterId);
      const option = filter?.options.find(o => o.value === value);
      return {
        id: filterId,
        label: option?.label || value,
        onDelete: () => handleFilterChange(filterId, ''),
      };
    });

  return (
    <Box sx={{ position: 'relative', ...sx }}>
      <TextField
        fullWidth={fullWidth}
        placeholder={placeholder}
        value={searchValue}
        onChange={handleInputChange}
        onKeyPress={handleKeyPress}
        onFocus={handleFocus}
        onBlur={handleBlur}
        disabled={disabled}
        size={size}
        variant={variant}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <Search />
            </InputAdornment>
          ),
          endAdornment: (
            <InputAdornment position="end">
              {searchValue && (
                <IconButton onClick={handleClear} size="small">
                  <Clear />
                </IconButton>
              )}
              {showFilters && (
                <IconButton
                  onClick={(e) => {
                    setAnchorEl(e.currentTarget);
                    setShowFiltersMenu(true);
                  }}
                  size="small"
                >
                  <FilterList />
                </IconButton>
              )}
            </InputAdornment>
          ),
        }}
      />

      {/* Active Filters */}
      {activeFilterChips.length > 0 && (
        <Box sx={{ mt: 1, display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
          {activeFilterChips.map(chip => (
            <Chip
              key={chip.id}
              label={chip.label}
              onDelete={chip.onDelete}
              size="small"
              color="primary"
              variant="outlined"
            />
          ))}
        </Box>
      )}

      {/* Suggestions Menu */}
      {showSuggestions && (
        <Box
          sx={{
            position: 'absolute',
            top: '100%',
            left: 0,
            right: 0,
            zIndex: 1000,
            backgroundColor: 'background.paper',
            border: 1,
            borderColor: 'divider',
            borderRadius: 1,
            boxShadow: 2,
            maxHeight: 300,
            overflow: 'auto',
          }}
        >
          {/* Suggestions */}
          {filteredSuggestions.length > 0 && (
            <>
              <Box sx={{ p: 1 }}>
                <Typography variant="caption" color="text.secondary">
                  Suggestions
                </Typography>
              </Box>
              {filteredSuggestions.map(suggestion => (
                <MenuItem
                  key={suggestion.id}
                  onClick={() => handleSuggestionSelect(suggestion)}
                >
                  {suggestion.icon && (
                    <ListItemIcon>
                      {suggestion.icon}
                    </ListItemIcon>
                  )}
                  <ListItemText
                    primary={suggestion.label}
                    secondary={suggestion.category}
                  />
                </MenuItem>
              ))}
            </>
          )}

          {/* History */}
          {showHistory && history.length > 0 && (
            <>
              {filteredSuggestions.length > 0 && <Divider />}
              <Box sx={{ p: 1 }}>
                <Typography variant="caption" color="text.secondary">
                  Recent Searches
                </Typography>
              </Box>
              {history.map((query, index) => (
                <MenuItem
                  key={index}
                  onClick={() => handleHistorySelect(query)}
                >
                  <ListItemIcon>
                    <History />
                  </ListItemIcon>
                  <ListItemText primary={query} />
                </MenuItem>
              ))}
            </>
          )}

          {/* Trending */}
          {showTrending && trending.length > 0 && (
            <>
              {(filteredSuggestions.length > 0 || history.length > 0) && <Divider />}
              <Box sx={{ p: 1 }}>
                <Typography variant="caption" color="text.secondary">
                  Trending
                </Typography>
              </Box>
              {trending.map((trend, index) => (
                <MenuItem
                  key={index}
                  onClick={() => handleTrendingSelect(trend)}
                >
                  <ListItemIcon>
                    <TrendingUp />
                  </ListItemIcon>
                  <ListItemText primary={trend} />
                </MenuItem>
              ))}
            </>
          )}
        </Box>
      )}

      {/* Filters Menu */}
      <Menu
        anchorEl={anchorEl}
        open={showFiltersMenu}
        onClose={() => setShowFiltersMenu(false)}
      >
        {filters.map(filter => (
          <MenuItem key={filter.id}>
            <ListItemText primary={filter.label} />
            <Select
              value={activeFilters[filter.id] || ''}
              onChange={(e) => handleFilterChange(filter.id, e.target.value)}
              size="small"
              sx={{ minWidth: 120 }}
            >
              <MenuItem value="">All</MenuItem>
              {filter.options.map(option => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </MenuItem>
        ))}
      </Menu>
    </Box>
  );
};

export default SearchBar;
