/**
 * Lazy-loaded components for code splitting
 */

import { lazy } from 'react';

// Lazy load page components
export const LazyDashboard = lazy(() => import('../pages/Dashboard'));
export const LazyServiceManagement = lazy(() => import('../pages/ServiceManagement'));
export const LazyPaymentRepair = lazy(() => import('../pages/PaymentRepair'));
export const LazyTransactionEnquiries = lazy(() => import('../pages/TransactionEnquiries'));
export const LazyReconciliationMonitoring = lazy(() => import('../pages/ReconciliationMonitoring'));
export const LazyChannelOnboarding = lazy(() => import('../pages/ChannelOnboarding'));
export const LazyClearingSystemOnboarding = lazy(() => import('../pages/ClearingSystemOnboarding'));

// Lazy load heavy components
export const LazyDataTable = lazy(() => import('./DataTable'));
export const LazyDateRangePicker = lazy(() => import('./DateRangePicker'));
export const LazyMetricCard = lazy(() => import('./MetricCard'));

// Lazy load utility components
export const LazyErrorBoundary = lazy(() => import('./ErrorBoundary'));
export const LazyLoadingSpinner = lazy(() => import('./LoadingSpinner'));
