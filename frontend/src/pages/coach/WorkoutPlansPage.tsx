import { useState } from 'react';
import {
  Box, Paper, TextField, MenuItem, Card, CardContent, CardActions, Button, Typography,
  Chip, Stack, IconButton, Tooltip,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { useAssignedClientsQuery } from '@/api/clientsApi';
import { useWorkoutPlansByClientQuery, useDeleteWorkoutPlanMutation } from '@/api/workoutPlansApi';
import { WorkoutPlanFormDialog } from './WorkoutPlanFormDialog';
import type { WorkoutPlanResponse } from '@/types';

export function WorkoutPlansPage() {
  const { data: clients } = useAssignedClientsQuery('coach');
  const [selectedClientId, setSelectedClientId] = useState<number | ''>('');
  const { data: plans } = useWorkoutPlansByClientQuery(selectedClientId || undefined);
  const deleteMutation = useDeleteWorkoutPlanMutation();
  const [formOpen, setFormOpen] = useState(false);
  const [editingPlan, setEditingPlan] = useState<WorkoutPlanResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<WorkoutPlanResponse | null>(null);

  const selectedClient = clients?.find((c) => c.id === selectedClientId) ?? null;

  return (
    <Box>
      <PageHeader
        title="Workout Plans"
        subtitle="Create and manage weekly workout plans for your assigned clients"
        action={
          <Button variant="contained" startIcon={<AddIcon />} disabled={!selectedClientId}
            onClick={() => { setEditingPlan(null); setFormOpen(true); }}>
            New Plan
          </Button>
        }
      />

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          select size="small" label="Select Client" sx={{ width: 320 }}
          value={selectedClientId} onChange={(e) => setSelectedClientId(e.target.value ? Number(e.target.value) : '')}
        >
          {clients?.map((c) => <MenuItem key={c.id} value={c.id}>{c.firstName} {c.lastName}</MenuItem>)}
        </TextField>
      </Paper>

      {!selectedClientId && <Typography color="text.secondary">Select a client to view or create workout plans.</Typography>}

      <Grid container spacing={2}>
        {plans?.map((plan) => (
          <Grid size={{ xs: 12, md: 6 }} key={plan.id}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between">
                  <Typography variant="h6">{plan.title}</Typography>
                </Box>
                {plan.description && <Typography variant="body2" color="text.secondary" mb={1}>{plan.description}</Typography>}
                <Stack direction="row" flexWrap="wrap" gap={0.5}>
                  {plan.details.map((d) => (
                    <Chip key={d.id} size="small" label={`${d.dayOfWeek.slice(0, 3)}: ${d.exerciseName}`} />
                  ))}
                </Stack>
              </CardContent>
              <CardActions>
                <Tooltip title="Edit">
                  <IconButton size="small" onClick={() => { setEditingPlan(plan); setFormOpen(true); }}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Delete">
                  <IconButton size="small" onClick={() => setDeleteTarget(plan)}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      <WorkoutPlanFormDialog open={formOpen} onClose={() => setFormOpen(false)} client={selectedClient} plan={editingPlan} />
      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Workout Plan"
        message={`Delete plan '${deleteTarget?.title}'?`}
        onClose={() => setDeleteTarget(null)}
        confirmColor="error"
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
      />
    </Box>
  );
}
