/**
 * Notification Context
 * 
 * React Context for managing toast notifications and alerts
 * throughout the application.
 */

import React, { createContext, useContext, useReducer, ReactNode } from 'react';

// Notification Types
export enum NotificationType {
  SUCCESS = 'success',
  ERROR = 'error',
  WARNING = 'warning',
  INFO = 'info',
}

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  duration?: number;
  persistent?: boolean;
  action?: {
    label: string;
    onClick: () => void;
  };
}

// Notification State Interface
interface NotificationState {
  notifications: Notification[];
}

// Notification Actions
type NotificationAction =
  | { type: 'ADD_NOTIFICATION'; payload: Notification }
  | { type: 'REMOVE_NOTIFICATION'; payload: string }
  | { type: 'CLEAR_ALL' };

// Initial State
const initialState: NotificationState = {
  notifications: [],
};

// Notification Reducer
function notificationReducer(state: NotificationState, action: NotificationAction): NotificationState {
  switch (action.type) {
    case 'ADD_NOTIFICATION':
      return {
        ...state,
        notifications: [...state.notifications, action.payload],
      };
    case 'REMOVE_NOTIFICATION':
      return {
        ...state,
        notifications: state.notifications.filter(n => n.id !== action.payload),
      };
    case 'CLEAR_ALL':
      return {
        ...state,
        notifications: [],
      };
    default:
      return state;
  }
}

// Notification Context Type
interface NotificationContextType {
  notifications: Notification[];
  showSuccess: (title: string, message: string, options?: Partial<Notification>) => void;
  showError: (title: string, message: string, options?: Partial<Notification>) => void;
  showWarning: (title: string, message: string, options?: Partial<Notification>) => void;
  showInfo: (title: string, message: string, options?: Partial<Notification>) => void;
  removeNotification: (id: string) => void;
  clearAll: () => void;
}

// Create Context
const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

// Notification Provider Props
interface NotificationProviderProps {
  children: ReactNode;
}

// Notification Provider Component
export function NotificationProvider({ children }: NotificationProviderProps) {
  const [state, dispatch] = useReducer(notificationReducer, initialState);

  // Generate unique ID
  const generateId = (): string => {
    return `notification_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  };

  // Add notification
  const addNotification = (notification: Omit<Notification, 'id'>) => {
    const id = generateId();
    const newNotification: Notification = {
      id,
      duration: 5000, // Default 5 seconds
      ...notification,
    };

    dispatch({ type: 'ADD_NOTIFICATION', payload: newNotification });

    // Auto-remove notification after duration (unless persistent)
    if (!newNotification.persistent && newNotification.duration) {
      setTimeout(() => {
        dispatch({ type: 'REMOVE_NOTIFICATION', payload: id });
      }, newNotification.duration);
    }
  };

  // Show success notification
  const showSuccess = (title: string, message: string, options?: Partial<Notification>) => {
    addNotification({
      type: NotificationType.SUCCESS,
      title,
      message,
      ...options,
    });
  };

  // Show error notification
  const showError = (title: string, message: string, options?: Partial<Notification>) => {
    addNotification({
      type: NotificationType.ERROR,
      title,
      message,
      persistent: true, // Errors are persistent by default
      ...options,
    });
  };

  // Show warning notification
  const showWarning = (title: string, message: string, options?: Partial<Notification>) => {
    addNotification({
      type: NotificationType.WARNING,
      title,
      message,
      ...options,
    });
  };

  // Show info notification
  const showInfo = (title: string, message: string, options?: Partial<Notification>) => {
    addNotification({
      type: NotificationType.INFO,
      title,
      message,
      ...options,
    });
  };

  // Remove notification
  const removeNotification = (id: string) => {
    dispatch({ type: 'REMOVE_NOTIFICATION', payload: id });
  };

  // Clear all notifications
  const clearAll = () => {
    dispatch({ type: 'CLEAR_ALL' });
  };

  const contextValue: NotificationContextType = {
    notifications: state.notifications,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    removeNotification,
    clearAll,
  };

  return (
    <NotificationContext.Provider value={contextValue}>
      {children}
    </NotificationContext.Provider>
  );
}

// Custom hook to use notification context
export function useNotification(): NotificationContextType {
  const context = useContext(NotificationContext);
  if (context === undefined) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
}

export default NotificationContext;
