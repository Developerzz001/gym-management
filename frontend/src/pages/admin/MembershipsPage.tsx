import { useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Box, Paper, Button, TextField, Card, CardContent, Typography, IconButton, Tooltip,
  Dialog, DialogTitle, DialogContent, DialogActions, MenuItem, Alert, Chip,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import AssignmentIcon from '@mui/icons-material/Assignment';
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import {
  useMembershipPlansQuery, useCreateMembershipPlanMutation, useDeleteMembershipPlanMutation,
  useAssignMembershipMutation, type MembershipPlanRequest,
} from '@/api/membershipsApi';
import { useClientsQuery } from '@/api/clientsApi';
import { extractErrorMessage } from '@/api/axiosClient';
import type { MembershipPlanResponse } from '@/types';

function PlanFormDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const createMutation = useCreateMembershipPlanMutation();

  const formik = useFormik<MembershipPlanRequest>({
    initialValues: { name: '', durationDays: 30, fees: 0, description: '' },
    validationSchema: Yup.object({
      name: Yup.string().required('Plan name is required'),
      durationDays: Yup.number().positive().required('Duration is required'),
      fees: Yup.number().positive().required('Fees is required'),
    }),
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        await createMutation.mutateAsync(values);
        formik.resetForm();
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Create Membership Plan</DialogTitle>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
          <TextField fullWidth margin="normal" label="Plan Name" name="name" value={formik.values.name} onChange={formik.handleChange}
            error={formik.touched.name && !!formik.errors.name} helperText={formik.touched.name && formik.errors.name} />
          <TextField fullWidth margin="normal" type="number" label="Duration (days)" name="durationDays"
            value={formik.values.durationDays} onChange={formik.handleChange} />
          <TextField fullWidth margin="normal" type="number" label="Fees" name="fees" value={formik.values.fees} onChange={formik.handleChange} />
          <TextField fullWidth margin="normal" label="Description" name="description" value={formik.values.description} onChange={formik.handleChange} />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained">Create</Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

function AssignMembershipDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const { data: clients } = useClientsQuery('', 0, 200);
  const { data: plans } = useMembershipPlansQuery();
  const assignMutation = useAssignMembershipMutation();
  const registeredActiveClients = clients?.content.filter(
    (client) => client.registrationType === 'REGISTERED'
  ) ?? [];

  const formik = useFormik({
    initialValues: { clientId: '', membershipPlanId: '', startDate: new Date().toISOString().slice(0, 10) },
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        await assignMutation.mutateAsync({
          clientId: Number(values.clientId),
          membershipPlanId: Number(values.membershipPlanId),
          startDate: values.startDate,
        });
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Assign Membership</DialogTitle>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
          <TextField select fullWidth margin="normal" label="Client" name="clientId" value={formik.values.clientId} onChange={formik.handleChange}>
            {registeredActiveClients.map((c) => (
              <MenuItem key={c.id} value={c.id}>{c.firstName} {c.lastName}</MenuItem>
            ))}
          </TextField>
          <TextField select fullWidth margin="normal" label="Membership Plan" name="membershipPlanId" value={formik.values.membershipPlanId} onChange={formik.handleChange}>
            {plans?.map((p) => <MenuItem key={p.id} value={p.id}>{p.name} ({p.durationDays} days)</MenuItem>)}
          </TextField>
          <TextField fullWidth margin="normal" type="date" label="Start Date" name="startDate" InputLabelProps={{ shrink: true }}
            value={formik.values.startDate} onChange={formik.handleChange} />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained">Assign</Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

export function MembershipsPage() {
  const { data: plans } = useMembershipPlansQuery();
  const deleteMutation = useDeleteMembershipPlanMutation();
  const [planFormOpen, setPlanFormOpen] = useState(false);
  const [assignOpen, setAssignOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<MembershipPlanResponse | null>(null);

  return (
    <Box>
      <PageHeader
        title="Memberships"
        subtitle="Manage membership plans and assign memberships to clients"
        action={
          <Box display="flex" gap={1}>
            <Button variant="outlined" startIcon={<AssignmentIcon />} onClick={() => setAssignOpen(true)}>Assign Membership</Button>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setPlanFormOpen(true)}>New Plan</Button>
          </Box>
        }
      />

      <Grid container spacing={2}>
        {plans?.map((plan) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={plan.id}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                  <Typography variant="h6">{plan.name}</Typography>
                  <Tooltip title="Delete plan">
                    <IconButton size="small" onClick={() => setDeleteTarget(plan)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </Box>
                <Chip size="small" label={`${plan.durationDays} days`} sx={{ mr: 1, mt: 1 }} />
                <Chip size="small" label={`₹ ${plan.fees}`} color="primary" sx={{ mt: 1 }} />
                {plan.description && (
                  <Typography variant="body2" color="text.secondary" mt={1}>{plan.description}</Typography>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
        {!plans?.length && (
          <Grid size={12}>
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <Typography color="text.secondary">No membership plans yet. Create one to get started.</Typography>
            </Paper>
          </Grid>
        )}
      </Grid>

      <PlanFormDialog open={planFormOpen} onClose={() => setPlanFormOpen(false)} />
      <AssignMembershipDialog open={assignOpen} onClose={() => setAssignOpen(false)} />
      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Membership Plan"
        message={`Delete plan '${deleteTarget?.name}'? This cannot be undone.`}
        onClose={() => setDeleteTarget(null)}
        confirmColor="error"
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
      />
    </Box>
  );
}
