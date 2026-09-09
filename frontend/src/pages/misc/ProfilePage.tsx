import { useMemo, useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { Alert, Avatar, Box, Button, Checkbox, Divider, FormControlLabel, MenuItem, Paper, TextField, Typography } from '@mui/material';
import PhotoCameraIcon from '@mui/icons-material/PhotoCamera';
import Grid from '@mui/material/Grid2';
import { PageHeader } from '@/components/common/PageHeader';
import { extractErrorMessage } from '@/api/axiosClient';
import { useMyProfileImageQuery, useMyProfileQuery, useUpdateMyProfileMutation, useUploadMyProfileImageMutation } from '@/api/profileApi';
import { useAppSelector } from '@/app/hooks';
import type { ClientRequest } from '@/api/clientsApi';
import type { CoachRequest } from '@/api/coachesApi';
import type { DieticianRequest } from '@/api/dieticiansApi';
import type { ClientResponse, CoachResponse, DieticianResponse } from '@/types';

type ProfileValues = Omit<ClientRequest, 'active'> | Omit<CoachRequest, 'active'> | Omit<DieticianRequest, 'active'>;

export function ProfilePage() {
  const role = useAppSelector((state) => state.auth.role) as 'CLIENT' | 'FITNESS_COACH' | 'DIETICIAN';
  const { data: profile, isLoading } = useMyProfileQuery(role);
  const updateMutation = useUpdateMyProfileMutation(role);
  const { data: profileImage } = useMyProfileImageQuery(true);
  const uploadImageMutation = useUploadMyProfileImageMutation();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const isClient = role === 'CLIENT';

  const validationSchema = useMemo(() => Yup.object({
    firstName: Yup.string().required('First name is required'),
    lastName: Yup.string().required('Last name is required'),
    email: Yup.string().email('Enter a valid email').required('Email is required'),
    password: Yup.string().min(6, 'Minimum 6 characters').notRequired(),
  }), []);

  const initialValues: ProfileValues = isClient ? {
      firstName: '', lastName: '', email: '', password: '', gender: undefined, dateOfBirth: '',
      heightCm: undefined, weightKg: undefined, address: '', contactNumber: '', fitnessGoal: '',
      diabetes: false, hypertension: false, asthma: false, allergies: '', injuries: '', medicalNotes: '',
    } : {
      firstName: '', lastName: '', email: '', mobileNumber: '', password: '', specialization: '',
      experienceYears: undefined, bio: '',
    };

  if (profile && isClient) {
    const client = profile as ClientResponse;
    Object.assign(initialValues, {
      firstName: client.firstName, lastName: client.lastName, email: client.email,
      gender: client.gender, dateOfBirth: client.dateOfBirth ?? '', heightCm: client.heightCm,
      weightKg: client.weightKg, address: client.address ?? '', contactNumber: client.contactNumber ?? '',
      fitnessGoal: client.fitnessGoal ?? '', diabetes: client.diabetes, hypertension: client.hypertension,
      asthma: client.asthma, allergies: client.allergies ?? '', injuries: client.injuries ?? '',
      medicalNotes: client.medicalNotes ?? '',
    });
  } else if (profile) {
    const professional = profile as CoachResponse | DieticianResponse;
    Object.assign(initialValues, {
      firstName: professional.firstName, lastName: professional.lastName, email: professional.email,
      mobileNumber: professional.mobileNumber ?? '', specialization: professional.specialization ?? '',
      experienceYears: professional.experienceYears, bio: professional.bio ?? '',
    });
  }

  const formik = useFormik<ProfileValues>({
    initialValues,
    enableReinitialize: true,
    validationSchema,
    onSubmit: async (values) => {
      setErrorMessage(null);
      setSuccessMessage(null);
      try {
        await updateMutation.mutateAsync(values);
        setSuccessMessage('Profile updated successfully.');
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  if (isLoading || !profile) return <Typography>Loading profile...</Typography>;

  const setField = (name: string, value: unknown) => formik.setFieldValue(name, value);
  const value = (name: string) => (formik.values as Record<string, unknown>)[name] ?? '';
  const error = (name: string) => {
    const touched = (formik.touched as Record<string, boolean>)[name];
    const message = (formik.errors as Record<string, string>)[name];
    return touched && message ? message : undefined;
  };

  const profileImageUrl = profileImage;

  return (
    <Box>
      <PageHeader title="My Profile" subtitle="Update your personal information" />
      <Paper sx={{ p: 3, maxWidth: 900 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
          <Avatar src={profileImageUrl} sx={{ width: 88, height: 88, bgcolor: 'primary.main', fontSize: 32 }}>
            {profile.firstName[0]}{profile.lastName[0]}
          </Avatar>
          <Box>
            <Typography variant="subtitle1">Profile photo</Typography>
            <Button component="label" variant="outlined" size="small" startIcon={<PhotoCameraIcon />} disabled={uploadImageMutation.isPending}>
              Upload Image
              <input hidden type="file" accept="image/jpeg,image/png,image/webp" onChange={(event) => {
                const file = event.target.files?.[0];
                if (file) uploadImageMutation.mutate(file);
                event.target.value = '';
              }} />
            </Button>
          </Box>
        </Box>
        {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
        {uploadImageMutation.isError && <Alert severity="error" sx={{ mb: 2 }}>{extractErrorMessage(uploadImageMutation.error)}</Alert>}
        {successMessage && <Alert severity="success" sx={{ mb: 2 }}>{successMessage}</Alert>}
        <form onSubmit={formik.handleSubmit}>
          <Typography variant="subtitle2" color="text.secondary" gutterBottom>Account Information</Typography>
          <Grid container spacing={2}>
            {['firstName', 'lastName', 'email'].map((field) => (
              <Grid key={field} size={{ xs: 12, sm: 6 }}>
                <TextField fullWidth label={field === 'firstName' ? 'First Name' : field === 'lastName' ? 'Last Name' : 'Email'}
                  name={field} value={value(field)} onChange={formik.handleChange} error={!!error(field)} helperText={error(field)} />
              </Grid>
            ))}
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth type="password" label="New Password (optional)" name="password" value={value('password')}
                onChange={formik.handleChange} error={!!error('password')} helperText={error('password')} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label={isClient ? 'Contact Number' : 'Mobile Number'} name={isClient ? 'contactNumber' : 'mobileNumber'}
                value={value(isClient ? 'contactNumber' : 'mobileNumber')} onChange={formik.handleChange} />
            </Grid>
          </Grid>

          {isClient ? (
            <>
              <Divider sx={{ my: 3 }} />
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>Personal and Health Details</Typography>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField select fullWidth label="Gender" name="gender" value={value('gender')} onChange={formik.handleChange}>
                    <MenuItem value="">Not specified</MenuItem><MenuItem value="MALE">Male</MenuItem><MenuItem value="FEMALE">Female</MenuItem><MenuItem value="OTHER">Other</MenuItem>
                  </TextField>
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}><TextField fullWidth type="date" label="Date of Birth" name="dateOfBirth" InputLabelProps={{ shrink: true }} value={value('dateOfBirth')} onChange={formik.handleChange} /></Grid>
                <Grid size={{ xs: 12, sm: 4 }}><TextField fullWidth label="Fitness Goal" name="fitnessGoal" value={value('fitnessGoal')} onChange={formik.handleChange} /></Grid>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth type="number" label="Height (cm)" name="heightCm" value={value('heightCm')} onChange={formik.handleChange} /></Grid>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth type="number" label="Weight (kg)" name="weightKg" value={value('weightKg')} onChange={formik.handleChange} /></Grid>
                <Grid size={12}><TextField fullWidth label="Address" name="address" value={value('address')} onChange={formik.handleChange} /></Grid>
                {['diabetes', 'hypertension', 'asthma'].map((field) => <Grid key={field} size={{ xs: 12, sm: 4 }}><FormControlLabel control={<Checkbox checked={Boolean(value(field))} onChange={(event) => setField(field, event.target.checked)} />} label={field[0].toUpperCase() + field.slice(1)} /></Grid>)}
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Allergies" name="allergies" value={value('allergies')} onChange={formik.handleChange} /></Grid>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Injuries" name="injuries" value={value('injuries')} onChange={formik.handleChange} /></Grid>
                <Grid size={12}><TextField fullWidth multiline rows={3} label="Medical Notes" name="medicalNotes" value={value('medicalNotes')} onChange={formik.handleChange} /></Grid>
              </Grid>
            </>
          ) : (
            <>
              <Divider sx={{ my: 3 }} />
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>Professional Details</Typography>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Specialization" name="specialization" value={value('specialization')} onChange={formik.handleChange} /></Grid>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth type="number" label="Experience (years)" name="experienceYears" value={value('experienceYears')} onChange={formik.handleChange} /></Grid>
                <Grid size={12}><TextField fullWidth multiline rows={3} label="Bio" name="bio" value={value('bio')} onChange={formik.handleChange} /></Grid>
              </Grid>
            </>
          )}
          <Box sx={{ mt: 3 }}><Button type="submit" variant="contained" disabled={updateMutation.isPending}>Save Changes</Button></Box>
        </form>
      </Paper>
    </Box>
  );
}