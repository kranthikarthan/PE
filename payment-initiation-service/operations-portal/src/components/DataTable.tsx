/**
 * DataTable Component
 * 
 * Enhanced table component with sorting, filtering, pagination, and selection.
 * Reusable across all pages for consistent data display.
 */

import React, { useState, useMemo } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TableSortLabel,
  Checkbox,
  Box,
  Typography,
  IconButton,
  Tooltip,
  Chip,
  Skeleton,
  CircularProgress,
  TablePagination,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Menu,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  Search,
  FilterList,
  MoreVert,
  Download,
  Refresh,
  Visibility,
  Edit,
  Delete,
  ArrowUpward,
  ArrowDownward,
} from '@mui/icons-material';

export interface Column<T = any> {
  id: string;
  label: string;
  minWidth?: number;
  align?: 'left' | 'right' | 'center';
  sortable?: boolean;
  filterable?: boolean;
  render?: (value: any, row: T) => React.ReactNode;
  getValue?: (row: T) => any;
}

export interface DataTableProps<T = any> {
  columns: Column<T>[];
  data: T[];
  loading?: boolean;
  error?: string;
  sortable?: boolean;
  selectable?: boolean;
  pagination?: boolean;
  page?: number;
  pageSize?: number;
  totalCount?: number;
  onPageChange?: (page: number) => void;
  onPageSizeChange?: (pageSize: number) => void;
  onSort?: (field: string, direction: 'asc' | 'desc') => void;
  onSelectionChange?: (selectedRows: T[]) => void;
  onRowClick?: (row: T) => void;
  actions?: Array<{
    label: string;
    icon: React.ReactNode;
    onClick: (row: T) => void;
    disabled?: (row: T) => boolean;
    color?: 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success';
  }>;
  bulkActions?: Array<{
    label: string;
    icon: React.ReactNode;
    onClick: (selectedRows: T[]) => void;
    disabled?: (selectedRows: T[]) => boolean;
    color?: 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success';
  }>;
  searchable?: boolean;
  onSearch?: (query: string) => void;
  filterable?: boolean;
  filters?: Array<{
    field: string;
    label: string;
    options: Array<{ value: string; label: string }>;
    value: string;
    onChange: (value: string) => void;
  }>;
  emptyMessage?: string;
  dense?: boolean;
  stickyHeader?: boolean;
  maxHeight?: number;
}

function DataTable<T = any>({
  columns,
  data,
  loading = false,
  error,
  sortable = true,
  selectable = false,
  pagination = true,
  page = 0,
  pageSize = 10,
  totalCount = 0,
  onPageChange,
  onPageSizeChange,
  onSort,
  onSelectionChange,
  onRowClick,
  actions = [],
  bulkActions = [],
  searchable = false,
  onSearch,
  filterable = false,
  filters = [],
  emptyMessage = 'No data available',
  dense = false,
  stickyHeader = false,
  maxHeight,
}: DataTableProps<T>) {
  const [sortField, setSortField] = useState<string>('');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [selectedRows, setSelectedRows] = useState<T[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedRow, setSelectedRow] = useState<T | null>(null);

  // Handle sorting
  const handleSort = (field: string) => {
    if (!sortable) return;

    const newDirection = sortField === field && sortDirection === 'asc' ? 'desc' : 'asc';
    setSortField(field);
    setSortDirection(newDirection);
    onSort?.(field, newDirection);
  };

  // Handle selection
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedRows([...data]);
      onSelectionChange?.(data);
    } else {
      setSelectedRows([]);
      onSelectionChange?.([]);
    }
  };

  const handleSelectRow = (row: T, checked: boolean) => {
    let newSelectedRows;
    if (checked) {
      newSelectedRows = [...selectedRows, row];
    } else {
      newSelectedRows = selectedRows.filter(r => r !== row);
    }
    setSelectedRows(newSelectedRows);
    onSelectionChange?.(newSelectedRows);
  };

  const isSelected = (row: T) => selectedRows.includes(row);

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    onSearch?.(query);
  };

  // Handle row click
  const handleRowClick = (row: T) => {
    onRowClick?.(row);
  };

  // Handle action menu
  const handleActionMenuOpen = (event: React.MouseEvent<HTMLElement>, row: T) => {
    setAnchorEl(event.currentTarget);
    setSelectedRow(row);
  };

  const handleActionMenuClose = () => {
    setAnchorEl(null);
    setSelectedRow(null);
  };

  // Handle action click
  const handleActionClick = (action: any) => {
    if (selectedRow) {
      action.onClick(selectedRow);
    }
    handleActionMenuClose();
  };

  // Handle bulk action click
  const handleBulkActionClick = (action: any) => {
    action.onClick(selectedRows);
  };

  // Render cell content
  const renderCellContent = (column: Column<T>, row: T) => {
    const value = column.getValue ? column.getValue(row) : (row as any)[column.id];
    
    if (column.render) {
      return column.render(value, row);
    }
    
    return value;
  };

  // Loading skeleton
  const LoadingSkeleton = () => (
    <TableBody>
      {Array.from({ length: pageSize }).map((_, index) => (
        <TableRow key={index}>
          {selectable && (
            <TableCell padding="checkbox">
              <Skeleton variant="rectangular" width={20} height={20} />
            </TableCell>
          )}
          {columns.map((column) => (
            <TableCell key={column.id}>
              <Skeleton variant="text" width="80%" />
            </TableCell>
          ))}
          {actions.length > 0 && (
            <TableCell>
              <Skeleton variant="rectangular" width={40} height={32} />
            </TableCell>
          )}
        </TableRow>
      ))}
    </TableBody>
  );

  return (
    <Box>
      {/* Search and Filter Bar */}
      {(searchable || filterable) && (
        <Box display="flex" gap={2} mb={2} alignItems="center">
          {searchable && (
            <TextField
              placeholder="Search..."
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
              size="small"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search />
                  </InputAdornment>
                ),
              }}
              sx={{ minWidth: 200 }}
            />
          )}
          
          {filterable && filters.map((filter) => (
            <FormControl key={filter.field} size="small" sx={{ minWidth: 120 }}>
              <InputLabel>{filter.label}</InputLabel>
              <Select
                value={filter.value}
                onChange={(e) => filter.onChange(e.target.value)}
                label={filter.label}
              >
                <MenuItem value="">All</MenuItem>
                {filter.options.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          ))}
        </Box>
      )}

      {/* Bulk Actions */}
      {bulkActions.length > 0 && selectedRows.length > 0 && (
        <Box display="flex" gap={1} mb={2} alignItems="center">
          <Typography variant="body2" color="text.secondary">
            {selectedRows.length} selected
          </Typography>
          {bulkActions.map((action, index) => (
            <Button
              key={index}
              size="small"
              startIcon={action.icon}
              onClick={() => handleBulkActionClick(action)}
              disabled={action.disabled?.(selectedRows)}
              color={action.color}
            >
              {action.label}
            </Button>
          ))}
        </Box>
      )}

      {/* Table */}
      <TableContainer 
        component={Paper} 
        sx={{ 
          maxHeight: maxHeight || 'auto',
          position: stickyHeader ? 'sticky' : 'static',
        }}
      >
        <Table stickyHeader={stickyHeader} size={dense ? 'small' : 'medium'}>
          <TableHead>
            <TableRow>
              {selectable && (
                <TableCell padding="checkbox">
                  <Checkbox
                    indeterminate={selectedRows.length > 0 && selectedRows.length < data.length}
                    checked={data.length > 0 && selectedRows.length === data.length}
                    onChange={(e) => handleSelectAll(e.target.checked)}
                  />
                </TableCell>
              )}
              {columns.map((column) => (
                <TableCell
                  key={column.id}
                  align={column.align}
                  style={{ minWidth: column.minWidth }}
                >
                  {sortable && column.sortable !== false ? (
                    <TableSortLabel
                      active={sortField === column.id}
                      direction={sortField === column.id ? sortDirection : 'asc'}
                      onClick={() => handleSort(column.id)}
                    >
                      {column.label}
                    </TableSortLabel>
                  ) : (
                    column.label
                  )}
                </TableCell>
              ))}
              {actions.length > 0 && (
                <TableCell align="right">Actions</TableCell>
              )}
            </TableRow>
          </TableHead>
          
          {loading ? (
            <LoadingSkeleton />
          ) : error ? (
            <TableBody>
              <TableRow>
                <TableCell colSpan={columns.length + (selectable ? 1 : 0) + (actions.length > 0 ? 1 : 0)}>
                  <Box display="flex" justifyContent="center" alignItems="center" py={4}>
                    <Typography color="error">{error}</Typography>
                  </Box>
                </TableCell>
              </TableRow>
            </TableBody>
          ) : data.length === 0 ? (
            <TableBody>
              <TableRow>
                <TableCell colSpan={columns.length + (selectable ? 1 : 0) + (actions.length > 0 ? 1 : 0)}>
                  <Box display="flex" justifyContent="center" alignItems="center" py={4}>
                    <Typography color="text.secondary">{emptyMessage}</Typography>
                  </Box>
                </TableCell>
              </TableRow>
            </TableBody>
          ) : (
            <TableBody>
              {data.map((row, index) => (
                <TableRow
                  key={index}
                  hover
                  selected={isSelected(row)}
                  onClick={() => handleRowClick(row)}
                  sx={{ cursor: onRowClick ? 'pointer' : 'default' }}
                >
                  {selectable && (
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={isSelected(row)}
                        onChange={(e) => handleSelectRow(row, e.target.checked)}
                        onClick={(e) => e.stopPropagation()}
                      />
                    </TableCell>
                  )}
                  {columns.map((column) => (
                    <TableCell key={column.id} align={column.align}>
                      {renderCellContent(column, row)}
                    </TableCell>
                  ))}
                  {actions.length > 0 && (
                    <TableCell align="right">
                      <IconButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleActionMenuOpen(e, row);
                        }}
                      >
                        <MoreVert />
                      </IconButton>
                    </TableCell>
                  )}
                </TableRow>
              ))}
            </TableBody>
          )}
        </Table>
      </TableContainer>

      {/* Pagination */}
      {pagination && (
        <TablePagination
          rowsPerPageOptions={[5, 10, 25, 50, 100]}
          component="div"
          count={totalCount}
          rowsPerPage={pageSize}
          page={page}
          onPageChange={(_, newPage) => onPageChange?.(newPage)}
          onRowsPerPageChange={(e) => onPageSizeChange?.(parseInt(e.target.value))}
        />
      )}

      {/* Action Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleActionMenuClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        {actions.map((action, index) => (
          <MenuItem
            key={index}
            onClick={() => handleActionClick(action)}
            disabled={action.disabled?.(selectedRow!)}
          >
            <ListItemIcon>{action.icon}</ListItemIcon>
            <ListItemText>{action.label}</ListItemText>
          </MenuItem>
        ))}
      </Menu>
    </Box>
  );
}

export default DataTable;
