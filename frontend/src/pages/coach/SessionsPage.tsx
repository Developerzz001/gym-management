import { useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Box, Paper, Button, TextField, MenuItem, Dialog, DialogTitle, DialogContent, DialogActions, Alert, Chip,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import { PageHeader } from '@/components/common/PageHeader';
import { useAssignedClientsQuery } from '@/api/clientsApi';
import { useMySessionsQuery, useScheduleSessionMutation, useUpdateSessionStatusMutation } from '@/api/sessionsApi';
import { extractErrorMessage } from '@/api/axiosClient';
import type { SessionResponse, SessionStatus } from '@/types';

const STATUS_COLORS: Record<SessionStatus, 'info' | 'success' | 'error' | 'default'> = {
  SCHEDULED: 'info', COMPLETED: 'success', MISSED: 'error', CANCELLED: 'default',
};

function ScheduleSessionDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { data: clients } = useAssignedClientsQuery('coach');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const scheduleMutation = useScheduleSessionMutation();

  const formik = useFormik({
    initialValues: { clientId: '', sessionDateTime: '', notes: '' },
    validationSchema: Yup.object({
      clientId: Yup.string().required('Client is required'),
      sessionDateTime: Yup.string().required('Date/time is required'),
    }),
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        await scheduleMutation.mutateAsync({
          clientId: Number(values.clientId),
          sessionDateTime: values.sessionDateTime,
          notes: values.notes,
        });
        formik.resetForm();
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Schedule Personal Training Session</DialogTitle>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
          <TextField select fullWidth margin="normal" label="Client" name="clientId" value={formik.values.clientId} onChange={formik.handleChange}
            error={formik.touched.clientId && !!formik.errors.clientId} helperText={formik.touched.clientId && formik.errors.clientId}>
            {clients?.map((c) => <MenuItem key={c.id} value={c.id}>{c.firstName} {c.lastName}</MenuItem>)}
          </TextField>
          <TextField fullWidth margin="normal" type="datetime-local" label="Session Date/Time" name="sessionDateTime"
            InputLabelProps={{ shrink: true }} value={formik.values.sessionDateTime} onChange={formik.handleChange}
            error={formik.touched.sessionDateTime && !!formik.errors.sessionDateTime} helperText={formik.touched.sessionDateTime && formik.errors.sessionDateTime} />
          <TextField fullWidth margin="normal" label="Notes" name="notes" value={formik.values.notes} onChange={formik.handleChange} />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained">Schedule</Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

export function SessionsPage() {
  const { data: sessions, isLoading } = useMySessionsQuery();
  const updateStatusMutation = useUpdateSessionStatusMutation();
  const [formOpen, setFormOpen] = useState(false);

  const columns: GridColDef<SessionResponse>[] = [
    { field: 'clientName', headerName: 'Client', flex: 1 },
    {
      field: 'sessionDateTime', headerName: 'Date/Time', flex: 1,
      renderCell: (params) => new Date(params.value).toLocaleString(),
    },
    { field: 'notes', headerName: 'Notes', flex: 1 },
    {
      field: 'status', headerName: 'Status', width: 140,
      renderCell: (params) => <Chip size="small" label={params.value} color={STATUS_COLORS[params.value as SessionStatus]} />,
    },
    {
      field: 'actions', headerName: 'Update Status', width: 260, sortable: false,
      renderCell: (params) => (
        <TextField
          select size="small" value="" SelectProps={{ displayEmpty: true }} sx={{ width: 220 }}
          onChange={(e) => updateStatusMutation.mutate({ id: params.row.id, status: e.target.value as SessionStatus })}
        >
          <MenuItem value="" disabled>Change status...</MenuItem>
          <MenuItem value="SCHEDULED">Scheduled</MenuItem>
          <MenuItem value="COMPLETED">Completed</MenuItem>
          <MenuItem value="MISSED">Missed</MenuItem>
          <MenuItem value="CANCELLED">Cancelled</MenuItem>
        </TextField>
      ),
    },
  ];

  return (
    <Box>
      <PageHeader
        title="Training Sessions"
        subtitle="Schedule and track personal training sessions"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setFormOpen(true)}>Schedule Session</Button>}
      />
      <Paper sx={{ height: 560 }}>
        <DataGrid
          rows={sessions ?? []}
          columns={columns}
          loading={isLoading}
          disableRowSelectionOnClick
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        />
      </Paper>
      <ScheduleSessionDialog open={formOpen} onClose={() => setFormOpen(false)} />
    </Box>
  );
}
