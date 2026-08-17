import { useEffect, useMemo, useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Alert } from '@mui/material';
import Grid from '@mui/material/Grid2';
import type { CoachResponse } from '@/types';
import { useCreateCoachMutation, useUpdateCoachMutation, type CoachRequest } from '@/api/coachesApi';
import { extractErrorMessage } from '@/api/axiosClient';

interface CoachFormDialogProps {
  open: boolean;
  onClose: () => void;
  coach?: CoachResponse | null;
}

export function CoachFormDialog({ open, onClose, coach }: CoachFormDialogProps) {
  const isEdit = !!coach;
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const createMutation = useCreateCoachMutation();
  const updateMutation = useUpdateCoachMutation();

  const validationSchema = useMemo(
    () =>
      Yup.object({
        firstName: Yup.string().required('First name is required'),
        lastName: Yup.string().required('Last name is required'),
        email: Yup.string().email('Enter a valid email').required('Email is required'),
        password: isEdit ? Yup.string().min(6) : Yup.string().min(6).required('Password is required'),
        experienceYears: Yup.number().min(0).nullable(),
      }),
    [isEdit]
  );

  const formik = useFormik<CoachRequest>({
    initialValues: {
      firstName: '', lastName: '', email: '', mobileNumber: '', password: '',
      specialization: '', experienceYears: undefined, bio: '', active: true,
    },
    validationSchema,
    enableReinitialize: true,
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        if (isEdit && coach) {
          await updateMutation.mutateAsync({ id: coach.id, payload: values });
        } else {
          await createMutation.mutateAsync(values);
        }
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  useEffect(() => {
    if (coach) {
      formik.setValues({
        firstName: coach.firstName, lastName: coach.lastName, email: coach.email,
        mobileNumber: coach.mobileNumber ?? '', password: '',
        specialization: coach.specialization ?? '', experienceYears: coach.experienceYears,
        bio: coach.bio ?? '', active: coach.active,
      });
    } else {
      formik.resetForm();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [coach, open]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Update Coach' : 'Add Fitness Coach'}</DialogTitle>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent dividers>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label="First Name" name="firstName" value={formik.values.firstName} onChange={formik.handleChange}
                error={formik.touched.firstName && !!formik.errors.firstName} helperText={formik.touched.firstName && formik.errors.firstName} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label="Last Name" name="lastName" value={formik.values.lastName} onChange={formik.handleChange}
                error={formik.touched.lastName && !!formik.errors.lastName} helperText={formik.touched.lastName && formik.errors.lastName} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label="Email" name="email" value={formik.values.email} onChange={formik.handleChange}
                error={formik.touched.email && !!formik.errors.email} helperText={formik.touched.email && formik.errors.email} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label="Mobile Number" name="mobileNumber" value={formik.values.mobileNumber} onChange={formik.handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth type="password" label={isEdit ? 'New Password (optional)' : 'Password'} name="password"
                value={formik.values.password} onChange={formik.handleChange}
                error={formik.touched.password && !!formik.errors.password} helperText={formik.touched.password && formik.errors.password} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth type="number" label="Experience (years)" name="experienceYears" value={formik.values.experienceYears ?? ''} onChange={formik.handleChange} />
            </Grid>
            <Grid size={12}>
              <TextField fullWidth label="Specialization" name="specialization" value={formik.values.specialization} onChange={formik.handleChange} />
            </Grid>
            <Grid size={12}>
              <TextField fullWidth multiline rows={2} label="Bio" name="bio" value={formik.values.bio} onChange={formik.handleChange} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained">{isEdit ? 'Update' : 'Add Coach'}</Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
