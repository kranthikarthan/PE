import React, { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Paper,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  AccountBalance as AccountBalanceIcon,
  Payment as PaymentIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Schedule as ScheduleIcon,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
} from 'recharts';
import { format, subDays } from 'date-fns';

import { useAppDispatch, useAppSelector } from '../store';
import { DashboardStats, ChartData } from '../types';
import apiService from '../services/api';

// Mock data for charts - in real app, this would come from API
const mockVolumeData: ChartData[] = [
  { name: '6 days ago', value: 245000, date: format(subDays(new Date(), 6), 'yyyy-MM-dd') },
  { name: '5 days ago', value: 312000, date: format(subDays(new Date(), 5), 'yyyy-MM-dd') },
  { name: '4 days ago', value: 198000, date: format(subDays(new Date(), 4), 'yyyy-MM-dd') },
  { name: '3 days ago', value: 456000, date: format(subDays(new Date(), 3), 'yyyy-MM-dd') },
  { name: '2 days ago', value: 389000, date: format(subDays(new Date(), 2), 'yyyy-MM-dd') },
  { name: 'Yesterday', value: 523000, date: format(subDays(new Date(), 1), 'yyyy-MM-dd') },
  { name: 'Today', value: 678000, date: format(new Date(), 'yyyy-MM-dd') },
];

const mockStatusData = [
  { name: 'Completed', value: 1245, color: '#4caf50' },
  { name: 'Pending', value: 89, color: '#ff9800' },
  { name: 'Failed', value: 23, color: '#f44336' },
  { name: 'Cancelled', value: 12, color: '#9e9e9e' },
];

const mockPaymentTypeData = [
  { name: 'ACH Credit', value: 456, amount: 2340000 },
  { name: 'Wire Transfer', value: 234, amount: 5670000 },
  { name: 'RTP', value: 567, amount: 1230000 },
  { name: 'Zelle', value: 123, amount: 456000 },
  { name: 'Card Payment', value: 789, amount: 890000 },
];

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactElement;
  color: 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info';
  trend?: {
    value: number;
    isPositive: boolean;
  };
}

const StatCard: React.FC<StatCardProps> = ({ title, value, subtitle, icon, color, trend }) => {
  const formatValue = (val: string | number): string => {
    if (typeof val === 'number') {
      if (val >= 1000000) {
        return `$${(val / 1000000).toFixed(1)}M`;
      } else if (val >= 1000) {
        return `$${(val / 1000).toFixed(0)}K`;
      }
      return val.toLocaleString();
    }
    return val;
  };

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 48,
              height: 48,
              borderRadius: 2,
              backgroundColor: `${color}.light`,
              color: `${color}.main`,
              mr: 2,
            }}
          >
            {icon}
          </Box>
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6" component="div" color="text.secondary" gutterBottom>
              {title}
            </Typography>
          </Box>
        </Box>
        
        <Typography variant="h4" component="div" sx={{ mb: 1 }}>
          {formatValue(value)}
        </Typography>
        
        {subtitle && (
          <Typography variant="body2" color="text.secondary">
            {subtitle}
          </Typography>
        )}
        
        {trend && (
          <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
            <TrendingUpIcon
              sx={{
                fontSize: 16,
                color: trend.isPositive ? 'success.main' : 'error.main',
                transform: trend.isPositive ? 'none' : 'rotate(180deg)',
                mr: 0.5,
              }}
            />
            <Typography
              variant="body2"
              sx={{
                color: trend.isPositive ? 'success.main' : 'error.main',
                fontWeight: 'medium',
              }}
            >
              {trend.value}% from last week
            </Typography>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        // In a real app, this would fetch from the API
        // const response = await apiService.getDashboardStats();
        // setStats(response.data);
        
        // Mock data for now
        setTimeout(() => {
          setStats({
            totalTransactions: 1369,
            totalAmount: 10456789,
            successfulTransactions: 1245,
            failedTransactions: 23,
            pendingTransactions: 89,
            averageTransactionAmount: 7634,
            transactionVolumeToday: 678000,
            activeAccounts: 2456,
            totalCustomers: 1234,
          });
          setLoading(false);
        }, 1000);
      } catch (err: any) {
        setError(err.message || 'Failed to load dashboard data');
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        {error}
      </Alert>
    );
  }

  if (!stats) {
    return (
      <Alert severity="warning" sx={{ mb: 2 }}>
        No dashboard data available
      </Alert>
    );
  }

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>
        Dashboard
      </Typography>

      {/* Key Metrics */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Transactions"
            value={stats.totalTransactions}
            subtitle="Last 30 days"
            icon={<PaymentIcon />}
            color="primary"
            trend={{ value: 12.5, isPositive: true }}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Transaction Volume"
            value={stats.totalAmount}
            subtitle="Total processed"
            icon={<TrendingUpIcon />}
            color="success"
            trend={{ value: 8.2, isPositive: true }}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Success Rate"
            value={`${((stats.successfulTransactions / stats.totalTransactions) * 100).toFixed(1)}%`}
            subtitle={`${stats.successfulTransactions} successful`}
            icon={<CheckCircleIcon />}
            color="success"
            trend={{ value: 2.1, isPositive: true }}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active Accounts"
            value={stats.activeAccounts}
            subtitle={`${stats.totalCustomers} customers`}
            icon={<AccountBalanceIcon />}
            color="info"
            trend={{ value: 5.4, isPositive: true }}
          />
        </Grid>
      </Grid>

      {/* Secondary Metrics */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={4}>
          <StatCard
            title="Pending Transactions"
            value={stats.pendingTransactions}
            subtitle="Awaiting processing"
            icon={<ScheduleIcon />}
            color="warning"
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <StatCard
            title="Failed Transactions"
            value={stats.failedTransactions}
            subtitle="Require attention"
            icon={<ErrorIcon />}
            color="error"
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <StatCard
            title="Average Amount"
            value={stats.averageTransactionAmount}
            subtitle="Per transaction"
            icon={<TrendingUpIcon />}
            color="secondary"
          />
        </Grid>
      </Grid>

      {/* Charts */}
      <Grid container spacing={3}>
        {/* Transaction Volume Chart */}
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ mb: 3 }}>
              Transaction Volume (Last 7 Days)
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={mockVolumeData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis tickFormatter={(value) => `$${(value / 1000).toFixed(0)}K`} />
                <Tooltip
                  formatter={(value: number) => [`$${value.toLocaleString()}`, 'Volume']}
                  labelFormatter={(label) => `Date: ${label}`}
                />
                <Line
                  type="monotone"
                  dataKey="value"
                  stroke="#1976d2"
                  strokeWidth={3}
                  dot={{ fill: '#1976d2', strokeWidth: 2, r: 4 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Transaction Status Pie Chart */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ mb: 3 }}>
              Transaction Status
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={mockStatusData}
                  cx="50%"
                  cy="50%"
                  outerRadius={80}
                  dataKey="value"
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                >
                  {mockStatusData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Payment Types Chart */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ mb: 3 }}>
              Payment Types Performance
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={mockPaymentTypeData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis yAxisId="left" orientation="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip
                  formatter={(value: number, name: string) => [
                    name === 'value' ? `${value} transactions` : `$${value.toLocaleString()}`,
                    name === 'value' ? 'Count' : 'Amount',
                  ]}
                />
                <Bar yAxisId="left" dataKey="value" fill="#1976d2" name="value" />
                <Bar yAxisId="right" dataKey="amount" fill="#dc004e" name="amount" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DashboardPage;