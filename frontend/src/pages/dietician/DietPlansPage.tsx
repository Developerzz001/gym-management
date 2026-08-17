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
import { useDietPlansByClientQuery, useDeleteDietPlanMutation } from '@/api/dietPlansApi';
import { DietPlanFormDialog } from './DietPlanFormDialog';
import type { DietPlanResponse } from '@/types';

export function DietPlansPage() {
  const { data: clients } = useAssignedClientsQuery('dietician');
  const [selectedClientId, setSelectedClientId] = useState<number | ''>('');
  const { data: plans } = useDietPlansByClientQuery(selectedClientId || undefined);
  const deleteMutation = useDeleteDietPlanMutation();
  const [formOpen, setFormOpen] = useState(false);
  const [editingPlan, setEditingPlan] = useState<DietPlanResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<DietPlanResponse | null>(null);

  const selectedClient = clients?.find((c) => c.id === selectedClientId) ?? null;

  return (
    <Box>
      <PageHeader
        title="Diet Plans"
        subtitle="Create and manage diet plans for your assigned clients"
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

      {!selectedClientId && <Typography color="text.secondary">Select a client to view or create diet plans.</Typography>}

      <Grid container spacing={2}>
        {plans?.map((plan) => (
          <Grid size={{ xs: 12, md: 6 }} key={plan.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{plan.title}</Typography>
                {plan.description && <Typography variant="body2" color="text.secondary" mb={1}>{plan.description}</Typography>}
                <Stack direction="row" flexWrap="wrap" gap={0.5}>
                  {plan.details.map((d) => (
                    <Chip
                      key={d.id}
                      size="small"
                      label={`${d.mealType.replace('_', ' ')}${d.mealTime ? ` (${d.mealTime})` : ''}: ${d.foodItem}`}
                    />
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

      <DietPlanFormDialog open={formOpen} onClose={() => setFormOpen(false)} client={selectedClient} plan={editingPlan} />
      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Diet Plan"
        message={`Delete plan '${deleteTarget?.title}'?`}
        onClose={() => setDeleteTarget(null)}
        confirmColor="error"
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
      />
    </Box>
  );
}
