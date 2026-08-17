import { useState } from 'react';
import { useFormik, FieldArray, FormikProvider } from 'formik';
import * as Yup from 'yup';
import {
  Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem,
  IconButton, Box, Typography, Divider, Alert,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { useExercisesQuery } from '@/api/exercisesApi';
import { useCreateWorkoutPlanMutation, useUpdateWorkoutPlanMutation, type WorkoutPlanRequest } from '@/api/workoutPlansApi';
import { extractErrorMessage } from '@/api/axiosClient';
import type { ClientResponse, DayOfWeek, WorkoutPlanResponse } from '@/types';

const DAYS: DayOfWeek[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

interface WorkoutPlanFormDialogProps {
  open: boolean;
  onClose: () => void;
  client: ClientResponse | null;
  plan?: WorkoutPlanResponse | null;
}

export function WorkoutPlanFormDialog({ open, onClose, client, plan }: WorkoutPlanFormDialogProps) {
  const isEdit = !!plan;
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const { data: exercises } = useExercisesQuery();
  const hasExercises = Boolean(exercises && exercises.length > 0);
  const createMutation = useCreateWorkoutPlanMutation();
  const updateMutation = useUpdateWorkoutPlanMutation();

  const formik = useFormik<WorkoutPlanRequest>({
    initialValues: {
      clientId: client?.id ?? 0,
      title: plan?.title ?? '',
      description: plan?.description ?? '',
      details: plan?.details.map((d) => ({
        dayOfWeek: d.dayOfWeek, exerciseId: d.exerciseId, sets: d.sets, reps: d.reps,
        durationMinutes: d.durationMinutes, restTimeSeconds: d.restTimeSeconds, notes: d.notes,
      })) ?? [{ dayOfWeek: 'MONDAY', exerciseId: 0, sets: 3, reps: 12, durationMinutes: undefined, restTimeSeconds: 60, notes: '' }],
    },
    enableReinitialize: true,
    validationSchema: Yup.object({
      title: Yup.string().required('Title is required'),
      details: Yup.array().min(1, 'Add at least one exercise entry'),
    }),
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        if (isEdit && plan) {
          await updateMutation.mutateAsync({ id: plan.id, payload: values });
        } else {
          await createMutation.mutateAsync(values);
        }
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{isEdit ? 'Update Workout Plan' : `New Workout Plan for ${client?.firstName ?? ''}`}</DialogTitle>
      <FormikProvider value={formik}>
        <form onSubmit={formik.handleSubmit}>
          <DialogContent dividers>
            {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
            <Grid container spacing={2} mb={1}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField fullWidth label="Plan Title" name="title" value={formik.values.title} onChange={formik.handleChange}
                  error={formik.touched.title && !!formik.errors.title} helperText={formik.touched.title && (formik.errors.title as string)} />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField fullWidth label="Description" name="description" value={formik.values.description} onChange={formik.handleChange} />
              </Grid>
            </Grid>
            <Divider sx={{ mb: 2 }} />
            <Typography variant="subtitle2" gutterBottom>Weekly Exercise Schedule</Typography>
            <FieldArray name="details">
              {(arrayHelpers) => (
                <Box>
                  {formik.values.details.map((detail, index) => (
                    <Grid container spacing={1} key={index} alignItems="center" mb={1}>
                      <Grid size={{ xs: 6, sm: 1.5 }}>
                        <TextField select fullWidth size="small" label="Day" name={`details.${index}.dayOfWeek`}
                          value={detail.dayOfWeek} onChange={formik.handleChange}>
                          {DAYS.map((d) => <MenuItem key={d} value={d}>{d.slice(0, 3)}</MenuItem>)}
                        </TextField>
                      </Grid>
                      <Grid size={{ xs: 6, sm: 2.5 }}>
                        <TextField select fullWidth size="small" label="Exercise" name={`details.${index}.exerciseId`}
                          value={detail.exerciseId || ''} onChange={formik.handleChange}
                          helperText={!hasExercises ? 'No exercises found. Add from Coach -> Exercises.' : undefined}
                          SelectProps={{
                            displayEmpty: true,
                            MenuProps: {
                              PaperProps: {
                                sx: {
                                  bgcolor: 'background.paper',
                                  color: 'text.primary',
                                },
                              },
                            },
                          }}>
                          {hasExercises ? (
                            exercises?.map((ex) => <MenuItem key={ex.id} value={ex.id}>{ex.name}</MenuItem>)
                          ) : (
                            <MenuItem disabled value="">
                              No exercises available
                            </MenuItem>
                          )}
                        </TextField>
                      </Grid>
                      <Grid size={{ xs: 4, sm: 1 }}>
                        <TextField fullWidth size="small" type="number" label="Sets" name={`details.${index}.sets`}
                          value={detail.sets ?? ''} onChange={formik.handleChange} />
                      </Grid>
                      <Grid size={{ xs: 4, sm: 1 }}>
                        <TextField fullWidth size="small" type="number" label="Reps" name={`details.${index}.reps`}
                          value={detail.reps ?? ''} onChange={formik.handleChange} />
                      </Grid>
                      <Grid size={{ xs: 4, sm: 1.5 }}>
                        <TextField fullWidth size="small" type="number" label="Duration (min)" name={`details.${index}.durationMinutes`}
                          value={detail.durationMinutes ?? ''} onChange={formik.handleChange} />
                      </Grid>
                      <Grid size={{ xs: 4, sm: 1.5 }}>
                        <TextField fullWidth size="small" type="number" label="Rest (sec)" name={`details.${index}.restTimeSeconds`}
                          value={detail.restTimeSeconds ?? ''} onChange={formik.handleChange} />
                      </Grid>
                      <Grid size={{ xs: 6, sm: 2 }}>
                        <TextField fullWidth size="small" label="Notes" name={`details.${index}.notes`}
                          value={detail.notes ?? ''} onChange={formik.handleChange} />
                      </Grid>
                      <Grid size={{ xs: 2, sm: 1 }}>
                        <IconButton onClick={() => arrayHelpers.remove(index)} disabled={formik.values.details.length === 1}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Grid>
                    </Grid>
                  ))}
                  <Button
                    startIcon={<AddIcon />}
                    onClick={() => arrayHelpers.push({ dayOfWeek: 'MONDAY', exerciseId: 0, sets: 3, reps: 12, restTimeSeconds: 60, notes: '' })}
                  >
                    Add Exercise
                  </Button>
                </Box>
              )}
            </FieldArray>
          </DialogContent>
          <DialogActions sx={{ p: 2 }}>
            <Button onClick={onClose}>Cancel</Button>
            <Button type="submit" variant="contained">{isEdit ? 'Update Plan' : 'Create Plan'}</Button>
          </DialogActions>
        </form>
      </FormikProvider>
    </Dialog>
  );
}
