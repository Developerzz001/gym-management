import { Snackbar, Alert } from '@mui/material';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { hideToast } from '@/features/ui/uiSlice';

export function GlobalToast() {
  const { toast } = useAppSelector((state) => state.ui);
  const dispatch = useAppDispatch();

  return (
    <Snackbar
      open={toast.open}
      autoHideDuration={4000}
      onClose={() => dispatch(hideToast())}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
    >
      <Alert severity={toast.severity} onClose={() => dispatch(hideToast())} variant="filled" sx={{ width: '100%' }}>
        {toast.message}
      </Alert>
    </Snackbar>
  );
}
