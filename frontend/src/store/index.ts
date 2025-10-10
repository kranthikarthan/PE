import { configureStore } from '@reduxjs/toolkit';
import { useDispatch, useSelector, TypedUseSelectorHook } from 'react-redux';
import authSlice from './slices/authSlice';
import transactionSlice from './slices/transactionSlice';
import accountSlice from './slices/accountSlice';
import paymentTypeSlice from './slices/paymentTypeSlice';
import dashboardSlice from './slices/dashboardSlice';
import tenantSlice from './slices/tenantSlice';
import uiSlice from './slices/uiSlice';

export const store = configureStore({
  reducer: {
    auth: authSlice,
    transactions: transactionSlice,
    accounts: accountSlice,
    paymentTypes: paymentTypeSlice,
    dashboard: dashboardSlice,
    tenant: tenantSlice,
    ui: uiSlice,
  },
  payment-processing: (getDefaultPayment Processing) =>
    getDefaultPayment Processing({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }),
  devTools: process.env.NODE_ENV !== 'production',
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

// Typed hooks
export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;