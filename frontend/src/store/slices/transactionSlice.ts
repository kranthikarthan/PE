import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Transaction, CreateTransactionRequest, TransactionSearchCriteria } from '../../types';
import apiService from '../../services/api';

interface TransactionState {
  items: Transaction[];
  currentTransaction: Transaction | null;
  loading: boolean;
  error: string | null;
  totalPages: number;
  currentPage: number;
  totalElements: number;
  searchCriteria: TransactionSearchCriteria | null;
}

const initialState: TransactionState = {
  items: [],
  currentTransaction: null,
  loading: false,
  error: null,
  totalPages: 0,
  currentPage: 0,
  totalElements: 0,
  searchCriteria: null,
};

// Async thunks
export const createTransaction = createAsyncThunk(
  'transactions/create',
  async (request: CreateTransactionRequest, { rejectWithValue }) => {
    try {
      const response = await apiService.createTransaction(request);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to create transaction');
    }
  }
);

export const fetchTransaction = createAsyncThunk(
  'transactions/fetchById',
  async (transactionId: string, { rejectWithValue }) => {
    try {
      const response = await apiService.getTransaction(transactionId);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to fetch transaction');
    }
  }
);

export const fetchTransactionByReference = createAsyncThunk(
  'transactions/fetchByReference',
  async (reference: string, { rejectWithValue }) => {
    try {
      const response = await apiService.getTransactionByReference(reference);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to fetch transaction');
    }
  }
);

export const searchTransactions = createAsyncThunk(
  'transactions/search',
  async (criteria: TransactionSearchCriteria, { rejectWithValue }) => {
    try {
      const response = await apiService.searchTransactions(criteria);
      return { data: response, criteria };
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to search transactions');
    }
  }
);

export const fetchTransactionsByAccount = createAsyncThunk(
  'transactions/fetchByAccount',
  async ({ accountId, page, size }: { accountId: string; page: number; size: number }, { rejectWithValue }) => {
    try {
      const response = await apiService.getTransactionsByAccount(accountId, page, size);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to fetch transactions');
    }
  }
);

export const cancelTransaction = createAsyncThunk(
  'transactions/cancel',
  async ({ transactionId, reason }: { transactionId: string; reason: string }, { rejectWithValue }) => {
    try {
      const response = await apiService.cancelTransaction(transactionId, reason);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to cancel transaction');
    }
  }
);

export const fetchTransactionStatus = createAsyncThunk(
  'transactions/fetchStatus',
  async (transactionId: string, { rejectWithValue }) => {
    try {
      const response = await apiService.getTransactionStatus(transactionId);
      return { transactionId, status: response.data };
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to fetch transaction status');
    }
  }
);

const transactionSlice = createSlice({
  name: 'transactions',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearCurrentTransaction: (state) => {
      state.currentTransaction = null;
    },
    updateTransactionStatus: (state, action: PayloadAction<{ id: string; status: string }>) => {
      const { id, status } = action.payload;
      const transaction = state.items.find(t => t.id === id);
      if (transaction) {
        transaction.status = status as any;
        transaction.updatedAt = new Date().toISOString();
      }
      if (state.currentTransaction?.id === id) {
        state.currentTransaction.status = status as any;
        state.currentTransaction.updatedAt = new Date().toISOString();
      }
    },
    setSearchCriteria: (state, action: PayloadAction<TransactionSearchCriteria>) => {
      state.searchCriteria = action.payload;
    },
    clearSearchCriteria: (state) => {
      state.searchCriteria = null;
    },
    addTransaction: (state, action: PayloadAction<Transaction>) => {
      state.items.unshift(action.payload);
      state.totalElements += 1;
    },
    updateTransaction: (state, action: PayloadAction<Transaction>) => {
      const index = state.items.findIndex(t => t.id === action.payload.id);
      if (index !== -1) {
        state.items[index] = action.payload;
      }
      if (state.currentTransaction?.id === action.payload.id) {
        state.currentTransaction = action.payload;
      }
    },
  },
  extraReducers: (builder) => {
    // Create transaction
    builder
      .addCase(createTransaction.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createTransaction.fulfilled, (state, action) => {
        state.loading = false;
        state.items.unshift(action.payload);
        state.currentTransaction = action.payload;
        state.totalElements += 1;
        state.error = null;
      })
      .addCase(createTransaction.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Fetch transaction
    builder
      .addCase(fetchTransaction.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchTransaction.fulfilled, (state, action) => {
        state.loading = false;
        state.currentTransaction = action.payload;
        state.error = null;
      })
      .addCase(fetchTransaction.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Fetch transaction by reference
    builder
      .addCase(fetchTransactionByReference.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchTransactionByReference.fulfilled, (state, action) => {
        state.loading = false;
        state.currentTransaction = action.payload;
        state.error = null;
      })
      .addCase(fetchTransactionByReference.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Search transactions
    builder
      .addCase(searchTransactions.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchTransactions.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload.data.content;
        state.totalPages = action.payload.data.totalPages;
        state.currentPage = action.payload.data.number;
        state.totalElements = action.payload.data.totalElements;
        state.searchCriteria = action.payload.criteria;
        state.error = null;
      })
      .addCase(searchTransactions.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Fetch transactions by account
    builder
      .addCase(fetchTransactionsByAccount.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchTransactionsByAccount.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload.content;
        state.totalPages = action.payload.totalPages;
        state.currentPage = action.payload.number;
        state.totalElements = action.payload.totalElements;
        state.error = null;
      })
      .addCase(fetchTransactionsByAccount.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Cancel transaction
    builder
      .addCase(cancelTransaction.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(cancelTransaction.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.items.findIndex(t => t.id === action.payload.id);
        if (index !== -1) {
          state.items[index] = action.payload;
        }
        if (state.currentTransaction?.id === action.payload.id) {
          state.currentTransaction = action.payload;
        }
        state.error = null;
      })
      .addCase(cancelTransaction.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // Fetch transaction status
    builder
      .addCase(fetchTransactionStatus.fulfilled, (state, action) => {
        const { transactionId, status } = action.payload;
        const transaction = state.items.find(t => t.id === transactionId);
        if (transaction) {
          Object.assign(transaction, status);
        }
        if (state.currentTransaction?.id === transactionId) {
          Object.assign(state.currentTransaction, status);
        }
      });
  },
});

export const {
  clearError,
  clearCurrentTransaction,
  updateTransactionStatus,
  setSearchCriteria,
  clearSearchCriteria,
  addTransaction,
  updateTransaction,
} = transactionSlice.actions;

// Selectors
export const selectTransactions = (state: { transactions: TransactionState }) => state.transactions.items;
export const selectCurrentTransaction = (state: { transactions: TransactionState }) => state.transactions.currentTransaction;
export const selectTransactionsLoading = (state: { transactions: TransactionState }) => state.transactions.loading;
export const selectTransactionsError = (state: { transactions: TransactionState }) => state.transactions.error;
export const selectTransactionsPagination = (state: { transactions: TransactionState }) => ({
  totalPages: state.transactions.totalPages,
  currentPage: state.transactions.currentPage,
  totalElements: state.transactions.totalElements,
});
export const selectSearchCriteria = (state: { transactions: TransactionState }) => state.transactions.searchCriteria;

export default transactionSlice.reducer;