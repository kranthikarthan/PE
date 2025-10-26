/**
 * Components Index
 * 
 * Centralized exports for all reusable components.
 */

// Data Table
export { default as DataTable } from './DataTable';
export type { Column, DataTableProps } from './DataTable';

// Error Boundary
export { default as ErrorBoundary } from './ErrorBoundary';

// Loading Spinner
export { default as LoadingSpinner } from './LoadingSpinner';
export type { LoadingSpinnerProps } from './LoadingSpinner';

// Status Chip
export { default as StatusChip } from './StatusChip';
export type { StatusChipProps, StatusType } from './StatusChip';

// Confirm Dialog
export { default as ConfirmDialog } from './ConfirmDialog';
export type { ConfirmDialogProps, ConfirmDialogType } from './ConfirmDialog';

// Search Bar
export { default as SearchBar } from './SearchBar';
export type { SearchBarProps, SearchSuggestion, SearchFilter } from './SearchBar';

// Date Range Picker
export { default as DateRangePicker } from './DateRangePicker';
export type { DateRangePickerProps, DateRange, PredefinedRange } from './DateRangePicker';

// Metric Card
export { default as MetricCard } from './MetricCard';
export { default as GlobalErrorBoundary } from './GlobalErrorBoundary';
export type { MetricCardProps, MetricTrend, MetricStatus } from './MetricCard';

// Private Route
export { default as PrivateRoute } from './PrivateRoute';

// Performance Optimization Components
export * from './LazyComponents';
export { default as SuspenseWrapper } from './SuspenseWrapper';
export { default as VirtualizedList } from './VirtualizedList';
export { default as InfiniteScrollList } from './InfiniteScrollList';
export * from './MemoizedComponent';
