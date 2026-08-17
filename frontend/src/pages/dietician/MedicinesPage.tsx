import { useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Box, Paper, TextField, MenuItem, Button, Dialog, DialogTitle, DialogContent, DialogActions, Alert, IconButton, Tooltip,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { useAssignedClientsQuery } from '@/api/clientsApi';
import { useMedicinesByClientQuery, useCreateMedicineMutation, useDeleteMedicineMutation } from '@/api/medicinesApi';
import { extractErrorMessage } from '@/api/axiosClient';
import type { MedicineResponse } from '@/types';

function MedicineFormDialog({ open, onClose, clientId }: { open: boolean; onClose: () => void; clientId: number }) {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const createMutation = useCreateMedicineMutation();

  const formik = useFormik({
    initialValues: { name: '', dosage: '', timing: '', instructions: '' },
    validationSchema: Yup.object({ name: Yup.string().required('Name is required') }),
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        await createMutation.mutateAsync({ clientId, ...values });
        formik.resetForm();
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Add Medicine</DialogTitle>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
          <TextField fullWidth margin="normal" label="Name" name="name" value={formik.values.name} onChange={formik.handleChange}
            error={formik.touched.name && !!formik.errors.name} helperText={formik.touched.name && formik.errors.name} />
          <TextField fullWidth margin="normal" label="Dosage" name="dosage" value={formik.values.dosage} onChange={formik.handleChange} />
          <TextField fullWidth margin="normal" label="Timing" name="timing" value={formik.values.timing} onChange={formik.handleChange} />
          <TextField fullWidth margin="normal" label="Instructions" name="instructions" value={formik.values.instructions} onChange={formik.handleChange} />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained">Add</Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

export function MedicinesPage() {
  const { data: clients } = useAssignedClientsQuery('dietician');
  const [selectedClientId, setSelectedClientId] = useState<number | ''>('');
  const { data: medicines, isLoading } = useMedicinesByClientQuery(selectedClientId || undefined);
  const deleteMutation = useDeleteMedicineMutation();
  const [formOpen, setFormOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<MedicineResponse | null>(null);

  const columns: GridColDef<MedicineResponse>[] = [
    { field: 'name', headerName: 'Name', flex: 1 },
    { field: 'dosage', headerName: 'Dosage', flex: 1 },
    { field: 'timing', headerName: 'Timing', flex: 1 },
    { field: 'instructions', headerName: 'Instructions', flex: 1.5 },
    {
      field: 'actions', headerName: '', width: 80, sortable: false,
      renderCell: (params) => (
        <Tooltip title="Delete">
          <IconButton size="small" onClick={() => setDeleteTarget(params.row)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      ),
    },
  ];

  return (
    <Box>
      <PageHeader
        title="Medicines"
        subtitle="Manage medicine prescriptions for your assigned clients"
        action={<Button variant="contained" startIcon={<AddIcon />} disabled={!selectedClientId} onClick={() => setFormOpen(true)}>Add Medicine</Button>}
      />
      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField select size="small" label="Select Client" sx={{ width: 320 }}
          value={selectedClientId} onChange={(e) => setSelectedClientId(e.target.value ? Number(e.target.value) : '')}>
          {clients?.map((c) => <MenuItem key={c.id} value={c.id}>{c.firstName} {c.lastName}</MenuItem>)}
        </TextField>
      </Paper>
      <Paper sx={{ height: 480 }}>
        <DataGrid rows={medicines ?? []} columns={columns} loading={isLoading} disableRowSelectionOnClick
          pageSizeOptions={[10, 25]} initialState={{ pagination: { paginationModel: { pageSize: 10 } } }} />
      </Paper>
      {selectedClientId && <MedicineFormDialog open={formOpen} onClose={() => setFormOpen(false)} clientId={Number(selectedClientId)} />}
      <ConfirmDialog open={!!deleteTarget} title="Delete Medicine" message={`Delete '${deleteTarget?.name}'?`}
        onClose={() => setDeleteTarget(null)} confirmColor="error" onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)} />
    </Box>
  );
}
