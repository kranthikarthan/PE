import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Checkbox,
  Chip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  LinearProgress,
  Tooltip,
  Divider,
} from '@mui/material';
import {
  CheckCircle as CheckCircleIcon,
  RadioButtonUnchecked as RadioButtonUncheckedIcon,
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  PlayArrow as PlayIcon,
  Pause as PauseIcon,
  Schedule as ScheduleIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  Business as BusinessIcon,
  Settings as SettingsIcon,
  Security as SecurityIcon,
  Payment as PaymentIcon,
  CloudUpload as CloudUploadIcon,
} from '@mui/icons-material';
import ModernButton from '../common/ModernButton';
import ModernCard from '../common/ModernCard';
import StatusChip from '../common/StatusChip';

export interface TodoItem {
  id: string;
  title: string;
  description: string;
  category: 'setup' | 'configuration' | 'security' | 'payment' | 'deployment' | 'maintenance';
  priority: 'low' | 'medium' | 'high' | 'critical';
  status: 'pending' | 'in-progress' | 'completed' | 'blocked';
  dueDate?: Date;
  assignedTo?: string;
  tenantId?: string;
  estimatedTime?: number; // in minutes
  completedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}

interface TenantTodoListProps {
  tenantId?: string;
  onTodoUpdate?: (todo: TodoItem) => void;
  onTodoComplete?: (todo: TodoItem) => void;
}

const TenantTodoList: React.FC<TenantTodoListProps> = ({ 
  tenantId, 
  onTodoUpdate, 
  onTodoComplete 
}) => {
  const [todos, setTodos] = useState<TodoItem[]>([]);
  const [filter, setFilter] = useState<'all' | 'pending' | 'in-progress' | 'completed'>('all');
  const [categoryFilter, setCategoryFilter] = useState<string>('all');
  const [priorityFilter, setPriorityFilter] = useState<string>('all');
  const [openDialog, setOpenDialog] = useState(false);
  const [editingTodo, setEditingTodo] = useState<TodoItem | null>(null);

  // Sample data for demonstration
  useEffect(() => {
    const sampleTodos: TodoItem[] = [
      {
        id: '1',
        title: 'Set up basic tenant information',
        description: 'Configure tenant ID, name, and environment settings',
        category: 'setup',
        priority: 'high',
        status: 'completed',
        tenantId: tenantId || 'tenant-001',
        estimatedTime: 15,
        completedAt: new Date(Date.now() - 86400000),
        createdAt: new Date(Date.now() - 172800000),
        updatedAt: new Date(Date.now() - 86400000),
      },
      {
        id: '2',
        title: 'Configure database connections',
        description: 'Set up PostgreSQL database connection and test connectivity',
        category: 'configuration',
        priority: 'critical',
        status: 'in-progress',
        tenantId: tenantId || 'tenant-001',
        estimatedTime: 30,
        createdAt: new Date(Date.now() - 86400000),
        updatedAt: new Date(Date.now() - 3600000),
      },
      {
        id: '3',
        title: 'Set up authentication and authorization',
        description: 'Configure JWT tokens, user roles, and permissions',
        category: 'security',
        priority: 'high',
        status: 'pending',
        tenantId: tenantId || 'tenant-001',
        estimatedTime: 45,
        createdAt: new Date(Date.now() - 86400000),
        updatedAt: new Date(Date.now() - 86400000),
      },
      {
        id: '4',
        title: 'Configure payment processing settings',
        description: 'Set up payment types, currencies, and transaction limits',
        category: 'payment',
        priority: 'medium',
        status: 'pending',
        tenantId: tenantId || 'tenant-001',
        estimatedTime: 20,
        createdAt: new Date(Date.now() - 86400000),
        updatedAt: new Date(Date.now() - 86400000),
      },
      {
        id: '5',
        title: 'Deploy tenant to production',
        description: 'Deploy configured tenant to production environment',
        category: 'deployment',
        priority: 'critical',
        status: 'blocked',
        tenantId: tenantId || 'tenant-001',
        estimatedTime: 60,
        createdAt: new Date(Date.now() - 86400000),
        updatedAt: new Date(Date.now() - 86400000),
      },
    ];
    setTodos(sampleTodos);
  }, [tenantId]);

  const getCategoryIcon = (category: TodoItem['category']) => {
    switch (category) {
      case 'setup': return <BusinessIcon />;
      case 'configuration': return <SettingsIcon />;
      case 'security': return <SecurityIcon />;
      case 'payment': return <PaymentIcon />;
      case 'deployment': return <CloudUploadIcon />;
      case 'maintenance': return <SettingsIcon />;
      default: return <InfoIcon />;
    }
  };

  const getPriorityColor = (priority: TodoItem['priority']) => {
    switch (priority) {
      case 'critical': return 'error';
      case 'high': return 'warning';
      case 'medium': return 'info';
      case 'low': return 'default';
      default: return 'default';
    }
  };

  const getStatusColor = (status: TodoItem['status']) => {
    switch (status) {
      case 'completed': return 'success';
      case 'in-progress': return 'info';
      case 'blocked': return 'error';
      case 'pending': return 'default';
      default: return 'default';
    }
  };

  const filteredTodos = todos.filter(todo => {
    const statusMatch = filter === 'all' || todo.status === filter;
    const categoryMatch = categoryFilter === 'all' || todo.category === categoryFilter;
    const priorityMatch = priorityFilter === 'all' || todo.priority === priorityFilter;
    const tenantMatch = !tenantId || todo.tenantId === tenantId;
    
    return statusMatch && categoryMatch && priorityMatch && tenantMatch;
  });

  const handleTodoToggle = (todo: TodoItem) => {
    const updatedTodo = {
      ...todo,
      status: todo.status === 'completed' ? 'pending' : 'completed',
      completedAt: todo.status === 'completed' ? undefined : new Date(),
      updatedAt: new Date(),
    };
    
    setTodos(prev => prev.map(t => t.id === todo.id ? updatedTodo : t));
    
    if (updatedTodo.status === 'completed' && onTodoComplete) {
      onTodoComplete(updatedTodo);
    }
    
    if (onTodoUpdate) {
      onTodoUpdate(updatedTodo);
    }
  };

  const handleStatusChange = (todo: TodoItem, newStatus: TodoItem['status']) => {
    const updatedTodo = {
      ...todo,
      status: newStatus,
      updatedAt: new Date(),
    };
    
    setTodos(prev => prev.map(t => t.id === todo.id ? updatedTodo : t));
    
    if (onTodoUpdate) {
      onTodoUpdate(updatedTodo);
    }
  };

  const getProgressPercentage = () => {
    const completedTodos = todos.filter(todo => todo.status === 'completed').length;
    return todos.length > 0 ? (completedTodos / todos.length) * 100 : 0;
  };

  const getCompletionStats = () => {
    const total = todos.length;
    const completed = todos.filter(todo => todo.status === 'completed').length;
    const inProgress = todos.filter(todo => todo.status === 'in-progress').length;
    const pending = todos.filter(todo => todo.status === 'pending').length;
    const blocked = todos.filter(todo => todo.status === 'blocked').length;
    
    return { total, completed, inProgress, pending, blocked };
  };

  const stats = getCompletionStats();

  return (
    <Box>
      {/* Header with Progress */}
      <ModernCard 
        title="Tenant Setup Progress" 
        subtitle={`${stats.completed} of ${stats.total} tasks completed`}
        elevation="low"
        sx={{ mb: 3 }}
      >
        <Box sx={{ mb: 2 }}>
          <LinearProgress 
            variant="determinate" 
            value={getProgressPercentage()} 
            sx={{ height: 8, borderRadius: 4, mb: 2 }}
          />
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              {Math.round(getProgressPercentage())}% Complete
            </Typography>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <StatusChip status="success" label={`${stats.completed} Done`} size="small" />
              <StatusChip status="info" label={`${stats.inProgress} In Progress`} size="small" />
              <StatusChip status="pending" label={`${stats.pending} Pending`} size="small" />
              {stats.blocked > 0 && (
                <StatusChip status="error" label={`${stats.blocked} Blocked`} size="small" />
              )}
            </Box>
          </Box>
        </Box>
      </ModernCard>

      {/* Filters */}
      <ModernCard elevation="low" sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Status</InputLabel>
            <Select
              value={filter}
              onChange={(e) => setFilter(e.target.value as any)}
              label="Status"
            >
              <MenuItem value="all">All</MenuItem>
              <MenuItem value="pending">Pending</MenuItem>
              <MenuItem value="in-progress">In Progress</MenuItem>
              <MenuItem value="completed">Completed</MenuItem>
            </Select>
          </FormControl>
          
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Category</InputLabel>
            <Select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              label="Category"
            >
              <MenuItem value="all">All</MenuItem>
              <MenuItem value="setup">Setup</MenuItem>
              <MenuItem value="configuration">Configuration</MenuItem>
              <MenuItem value="security">Security</MenuItem>
              <MenuItem value="payment">Payment</MenuItem>
              <MenuItem value="deployment">Deployment</MenuItem>
              <MenuItem value="maintenance">Maintenance</MenuItem>
            </Select>
          </FormControl>
          
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Priority</InputLabel>
            <Select
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value)}
              label="Priority"
            >
              <MenuItem value="all">All</MenuItem>
              <MenuItem value="critical">Critical</MenuItem>
              <MenuItem value="high">High</MenuItem>
              <MenuItem value="medium">Medium</MenuItem>
              <MenuItem value="low">Low</MenuItem>
            </Select>
          </FormControl>
          
          <Box sx={{ flex: 1 }} />
          
          <ModernButton
            variant="primary"
            startIcon={<AddIcon />}
            onClick={() => setOpenDialog(true)}
          >
            Add Task
          </ModernButton>
        </Box>
      </ModernCard>

      {/* Todo List */}
      <ModernCard elevation="low">
        <List>
          {filteredTodos.map((todo, index) => (
            <React.Fragment key={todo.id}>
              <ListItem
                sx={{
                  py: 2,
                  '&:hover': {
                    backgroundColor: 'action.hover',
                  },
                }}
              >
                <ListItemIcon>
                  <Checkbox
                    checked={todo.status === 'completed'}
                    onChange={() => handleTodoToggle(todo)}
                    icon={<RadioButtonUncheckedIcon />}
                    checkedIcon={<CheckCircleIcon />}
                    color="primary"
                  />
                </ListItemIcon>
                
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                      <Typography 
                        variant="subtitle1" 
                        sx={{ 
                          textDecoration: todo.status === 'completed' ? 'line-through' : 'none',
                          opacity: todo.status === 'completed' ? 0.7 : 1,
                        }}
                      >
                        {todo.title}
                      </Typography>
                      <Chip
                        icon={getCategoryIcon(todo.category)}
                        label={todo.category}
                        size="small"
                        variant="outlined"
                      />
                      <StatusChip 
                        status={getPriorityColor(todo.priority) as any} 
                        label={todo.priority} 
                        size="small" 
                      />
                    </Box>
                  }
                  secondary={
                    <Box>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {todo.description}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                        <StatusChip 
                          status={getStatusColor(todo.status) as any} 
                          label={todo.status.replace('-', ' ')} 
                          size="small" 
                        />
                        {todo.estimatedTime && (
                          <Chip
                            icon={<ScheduleIcon />}
                            label={`${todo.estimatedTime}m`}
                            size="small"
                            variant="outlined"
                          />
                        )}
                        {todo.dueDate && (
                          <Chip
                            label={`Due: ${todo.dueDate.toLocaleDateString()}`}
                            size="small"
                            variant="outlined"
                            color={todo.dueDate < new Date() ? 'error' : 'default'}
                          />
                        )}
                      </Box>
                    </Box>
                  }
                />
                
                <ListItemSecondaryAction>
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <FormControl size="small" sx={{ minWidth: 120 }}>
                      <Select
                        value={todo.status}
                        onChange={(e) => handleStatusChange(todo, e.target.value as TodoItem['status'])}
                        variant="outlined"
                      >
                        <MenuItem value="pending">Pending</MenuItem>
                        <MenuItem value="in-progress">In Progress</MenuItem>
                        <MenuItem value="completed">Completed</MenuItem>
                        <MenuItem value="blocked">Blocked</MenuItem>
                      </Select>
                    </FormControl>
                    
                    <Tooltip title="Edit">
                      <IconButton size="small" onClick={() => setEditingTodo(todo)}>
                        <EditIcon />
                      </IconButton>
                    </Tooltip>
                    
                    <Tooltip title="Delete">
                      <IconButton size="small" color="error">
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </ListItemSecondaryAction>
              </ListItem>
              {index < filteredTodos.length - 1 && <Divider />}
            </React.Fragment>
          ))}
        </List>
        
        {filteredTodos.length === 0 && (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="body1" color="text.secondary">
              No tasks found matching your filters.
            </Typography>
          </Box>
        )}
      </ModernCard>
    </Box>
  );
};

export default TenantTodoList;