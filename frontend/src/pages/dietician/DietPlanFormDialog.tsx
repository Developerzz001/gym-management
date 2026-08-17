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
import { useCreateDietPlanMutation, useUpdateDietPlanMutation, type DietPlanRequest } from '@/api/dietPlansApi';
import { extractErrorMessage } from '@/api/axiosClient';
import type { ClientResponse, DietPlanResponse, MealType } from '@/types';

const MEAL_TYPES: MealType[] = ['BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'EVENING_SNACK', 'DINNER'];

interface DietPlanFormDialogProps {
  open: boolean;
  onClose: () => void;
  client: ClientResponse | null;
  plan?: DietPlanResponse | null;
}

export function DietPlanFormDialog({ open, onClose, client, plan }: DietPlanFormDialogProps) {
  const isEdit = !!plan;
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const createMutation = useCreateDietPlanMutation();
  const updateMutation = useUpdateDietPlanMutation();

  const formik = useFormik<DietPlanRequest>({
    initialValues: {
      clientId: client?.id ?? 0,
      title: plan?.title ?? '',
      description: plan?.description ?? '',
      details: plan?.details.map((d) => ({
        mealType: d.mealType,
        foodItem: d.foodItem,
        quantity: d.quantity,
        calories: d.calories,
        mealTime: d.mealTime,
      })) ?? [
        { mealType: 'BREAKFAST', foodItem: '', quantity: '', calories: undefined, mealTime: '09:00' },
      ],
    },
    enableReinitialize: true,
    validationSchema: Yup.object({
      title: Yup.string().required('Title is required'),
      details: Yup.array().min(1, 'Add at least one meal entry'),
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
      <DialogTitle>{isEdit ? 'Update Diet Plan' : `New Diet Plan for ${client?.firstName ?? ''}`}</DialogTitle>
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
            <Typography variant="subtitle2" gutterBottom>Meals</Typography>
            <FieldArray name="details">
              {(arrayHelpers) => (
                <Box>
                  {formik.values.details.map((detail, index) => (
                    <Grid container spacing={1} key={index} alignItems="center" mb={1}>
                      <Grid size={{ xs: 6, sm: 2 }}>
                        <TextField select fullWidth size="small" label="Meal" name={`details.${index}.mealType`}
                          value={detail.mealType} onChange={formik.handleChange}>
                          {MEAL_TYPES.map((m) => <MenuItem key={m} value={m}>{m.replace('_', ' ')}</MenuItem>)}
                        </TextField>
                      </Grid>
                      <Grid size={{ xs: 6, sm: 3 }}>
                        <TextField fullWidth size="small" label="Food Item" name={`details.${index}.foodItem`}
                          value={detail.foodItem} onChange={formik.handleChange} />
                      </Grid>
                      <Grid size={{ xs: 6, sm: 2 }}>
                        <TextField fullWidth size="small" label="Quantity" name={`details.${index}.quantity`}
                          value={detail.quantity ?? ''} onChange={formik.handleChange} />
                      </Grid>
                      <Grid size={{ xs: 6, sm: 2 }}>
                        <TextField fullWidth size="small" type="time" label="Time" name={`details.${index}.mealTime`}
                          value={detail.mealTime ?? ''} onChange={formik.handleChange}
                          slotProps={{ inputLabel: { shrink: true } }} />
                      </Grid>
                      <Grid size={{ xs: 4, sm: 2 }}>
                        <TextField fullWidth size="small" type="number" label="Calories" name={`details.${index}.calories`}
                          value={detail.calories ?? ''} onChange={formik.handleChange} />
                      </Grid>
                      <Grid size={{ xs: 2, sm: 1 }}>
                        <IconButton onClick={() => arrayHelpers.remove(index)} disabled={formik.values.details.length === 1}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Grid>
                    </Grid>
                  ))}
                  <Button startIcon={<AddIcon />} onClick={() => arrayHelpers.push({ mealType: 'BREAKFAST', foodItem: '', quantity: '', calories: undefined, mealTime: '09:00' })}>
                    Add Meal Item
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
