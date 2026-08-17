import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface ToastState {
  open: boolean;
  message: string;
  severity: 'success' | 'error' | 'info' | 'warning';
}

interface UiState {
  toast: ToastState;
}

const initialState: UiState = {
  toast: { open: false, message: '', severity: 'success' },
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    showToast: (state, action: PayloadAction<{ message: string; severity?: ToastState['severity'] }>) => {
      state.toast = {
        open: true,
        message: action.payload.message,
        severity: action.payload.severity ?? 'success',
      };
    },
    hideToast: (state) => {
      state.toast.open = false;
    },
  },
});

export const { showToast, hideToast } = uiSlice.actions;
export default uiSlice.reducer;
