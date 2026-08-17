import { useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  MenuItem,
  Alert,
} from '@mui/material';
import {
  useCreateExerciseMutation,
  useUpdateExerciseMutation,
  type ExerciseRequest,
} from '@/api/exercisesApi';
import { extractErrorMessage } from '@/api/axiosClient';
import type { ExerciseCategory, ExerciseResponse } from '@/types';

const CATEGORIES: ExerciseCategory[] = ['CHEST', 'BACK', 'SHOULDER', 'ARMS', 'LEGS', 'CORE', 'CARDIO'];

interface ExerciseFormDialogProps {
  open: boolean;
  onClose: () => void;
  exercise?: ExerciseResponse | null;
}

export function ExerciseFormDialog({ open, onClose, exercise }: ExerciseFormDialogProps) {
  const isEdit = !!exercise;
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const createMutation = useCreateExerciseMutation();
  const updateMutation = useUpdateExerciseMutation();

  const formik = useFormik<ExerciseRequest>({
    initialValues: {
      name: exercise?.name ?? '',
      category: exercise?.category ?? 'CHEST',
      description: exercise?.description ?? '',
    },
    enableReinitialize: true,
    validationSchema: Yup.object({
      name: Yup.string().trim().required('Exercise name is required'),
      category: Yup.string().required('Category is required'),
      description: Yup.string().max(1000, 'Description is too long'),
    }),
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        if (isEdit && exercise) {
          await updateMutation.mutateAsync({ id: exercise.id, payload: values });
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
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Update Exercise' : 'Add Exercise'}</DialogTitle>
      <form onSubmit={formik.handleSubmit} noValidate>
        <DialogContent dividers>
          {errorMessage && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {errorMessage}
            </Alert>
          )}
          <TextField
            fullWidth
            margin="normal"
            label="Exercise Name"
            name="name"
            value={formik.values.name}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.name && Boolean(formik.errors.name)}
            helperText={formik.touched.name && formik.errors.name}
          />
          <TextField
            fullWidth
            select
            margin="normal"
            label="Category"
            name="category"
            value={formik.values.category}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.category && Boolean(formik.errors.category)}
            helperText={formik.touched.category && formik.errors.category}
          >
            {CATEGORIES.map((category) => (
              <MenuItem key={category} value={category}>
                {category}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            fullWidth
            multiline
            minRows={3}
            margin="normal"
            label="Description"
            name="description"
            value={formik.values.description ?? ''}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.description && Boolean(formik.errors.description)}
            helperText={formik.touched.description && formik.errors.description}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button
            type="submit"
            variant="contained"
            disabled={formik.isSubmitting || createMutation.isPending || updateMutation.isPending}
          >
            {isEdit ? 'Update' : 'Add'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
