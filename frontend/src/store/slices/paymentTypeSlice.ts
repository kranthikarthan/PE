import { createSlice } from '@reduxjs/toolkit';

const paymentTypeSlice = createSlice({
  name: 'paymentTypes',
  initialState: {
    items: [],
    loading: false,
    error: null,
  },
  reducers: {},
});

export default paymentTypeSlice.reducer;