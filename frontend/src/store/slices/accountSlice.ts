import { createSlice } from '@reduxjs/toolkit';

const accountSlice = createSlice({
  name: 'accounts',
  initialState: {
    items: [],
    loading: false,
    error: null,
  },
  reducers: {},
});

export default accountSlice.reducer;