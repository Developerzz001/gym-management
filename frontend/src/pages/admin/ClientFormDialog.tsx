import { useEffect, useMemo, useState } from 'react';
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
  FormControlLabel,
  Checkbox,
  Divider,
  Typography,
  Alert,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import type { ClientResponse } from '@/types';
import { useConvertInquiryMutation, useRegisterClientMutation, useRegisterInquiryMutation, useUpdateClientMutation, useUploadClientProfileImageMutation, type ClientRequest } from '@/api/clientsApi';
import { useReceptionistsQuery } from '@/api/usersApi';
import { extractErrorMessage } from '@/api/axiosClient';
import { PhotoCapture } from '@/components/common/PhotoCapture';
import { useAppSelector } from '@/app/hooks';

interface ClientFormDialogProps {
  open: boolean;
  onClose: () => void;
  client?: ClientResponse | null;
  inquiry?: boolean;
  conversion?: boolean;
}

export function ClientFormDialog({ open, onClose, client, inquiry = false, conversion = false }: ClientFormDialogProps) {
  const isEdit = !!client;
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [photoFile, setPhotoFile] = useState<File | null>(null);
  const [createdClientId, setCreatedClientId] = useState<number | null>(null);
  const [bmiResult, setBmiResult] = useState<number | null>(null);
  const registerMutation = useRegisterClientMutation();
  const inquiryMutation = useRegisterInquiryMutation();
  const updateMutation = useUpdateClientMutation();
  const convertMutation = useConvertInquiryMutation();
  const uploadImageMutation = useUploadClientProfileImageMutation();
  const { userId: currentUserId, firstName: currentFirstName, lastName: currentLastName } = useAppSelector((state) => state.auth);
  const { data: receptionists } = useReceptionistsQuery();

  const validationSchema = useMemo(
    () =>
      Yup.object({
        firstName: Yup.string().required('First name is required'),
        lastName: inquiry && !conversion ? Yup.string().notRequired() : Yup.string().required('Last name is required'),
        email: inquiry && !conversion
          ? Yup.string().email('Enter a valid email').notRequired()
          : Yup.string().email('Enter a valid email').required('Email is required'),
        password: conversion ? Yup.string().min(6, 'Minimum 6 characters').required('Password is required') : inquiry ? Yup.string().notRequired() : isEdit
          ? Yup.string().min(6, 'Minimum 6 characters')
          : Yup.string().min(6, 'Minimum 6 characters').required('Password is required'),
        contactNumber: inquiry && !conversion ? Yup.string().required('Phone number is required') : Yup.string().nullable(),
        heightCm: Yup.number().positive().nullable(),
        weightKg: Yup.number().positive().nullable(),
      }),
    [conversion, inquiry, isEdit]
  );

  const nextFollowUpDate = new Date();
  nextFollowUpDate.setDate(nextFollowUpDate.getDate() + 7);

  const formik = useFormik<ClientRequest>({
    initialValues: {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      active: !inquiry,
      gender: undefined,
      dateOfBirth: '',
      source: 'WALK_IN',
      sportActivity: 'GYM',
      executiveId: currentUserId ?? undefined,
      rating: undefined,
      comment: '',
      inquiryDate: new Date().toLocaleDateString("en-CA"),
      nextFollowUpDate: nextFollowUpDate.toLocaleDateString("en-CA"),
      heightCm: undefined,
      weightKg: undefined,
      address: '',
      contactNumber: '',
      fitnessGoal: '',
      diabetes: false,
      hypertension: false,
      asthma: false,
      allergies: '',
      injuries: '',
      medicalNotes: '',
    },
    validationSchema,
    enableReinitialize: true,
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        if (createdClientId && photoFile) {
          await uploadImageMutation.mutateAsync({ id: createdClientId, file: photoFile });
          onClose();
          return;
        }
        if (conversion && client) {
          const convertedClient = await convertMutation.mutateAsync({ id: client.id, payload: values });
          if (photoFile) {
            setCreatedClientId(convertedClient.id);
            await uploadImageMutation.mutateAsync({ id: convertedClient.id, file: photoFile });
          }
        } else if (isEdit && client) {
          await updateMutation.mutateAsync({ id: client.id, payload: values });
        } else {
          const createdClient = inquiry
            ? await inquiryMutation.mutateAsync(values)
            : await registerMutation.mutateAsync(values);
          if (photoFile) {
            setCreatedClientId(createdClient.id);
            await uploadImageMutation.mutateAsync({ id: createdClient.id, file: photoFile });
          }
        }
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  useEffect(() => {
    setPhotoFile(null);
    setCreatedClientId(null);
    setErrorMessage(null);
    if (client) {
      formik.setValues({
        firstName: client.firstName,
        lastName: client.lastName,
        email: client.email,
        password: '',
        active: client.active,
        gender: client.gender,
        source: client.source,
        sportActivity: client.sportActivity,
        executiveId: client.executiveId ?? currentUserId ?? undefined,
        rating: client.rating,
        comment: client.comment ?? '',
        dateOfBirth: client.dateOfBirth ?? '',
        inquiryDate: client.inquiryDate ?? '',
        nextFollowUpDate: client.nextFollowUpDate ?? '',
        heightCm: client.heightCm,
        weightKg: client.weightKg,
        address: client.address ?? '',
        contactNumber: client.contactNumber ?? '',
        fitnessGoal: client.fitnessGoal ?? '',
        diabetes: client.diabetes,
        hypertension: client.hypertension,
        asthma: client.asthma,
        allergies: client.allergies ?? '',
        injuries: client.injuries ?? '',
        medicalNotes: client.medicalNotes ?? '',
      });
    } else {
      formik.resetForm();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [client, open]);

  const isSubmitting = registerMutation.isPending || inquiryMutation.isPending || updateMutation.isPending
    || convertMutation.isPending || uploadImageMutation.isPending;
  const showPhotoCapture = !isEdit || conversion;
  const isNewInquiry = inquiry && !conversion;

  const handleAnalyseDetails = () => {
    const weight = Number(formik.values.weightKg);
    const heightCm = Number(formik.values.heightCm);
    if (!weight || !heightCm) {
      setBmiResult(null);
      return;
    }
    const heightM = heightCm / 100;
    setBmiResult(Number((weight / (heightM * heightM)).toFixed(1)));
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth={isNewInquiry ? 'xl' : 'lg'} fullWidth>
      <DialogTitle>{conversion ? 'Convert Inquiry to Client' : isEdit ? 'Update Client' : inquiry ? 'Register New Inquiry' : 'Register New Client'}</DialogTitle>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent dividers>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
          {isNewInquiry ? (
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 9 }}>
                <Grid container spacing={1.5}>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" type="date" label="Date" name="inquiryDate" InputLabelProps={{ shrink: true }}
                      value={formik.values.inquiryDate} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Phone No" name="contactNumber" value={formik.values.contactNumber}
                      onChange={formik.handleChange} error={formik.touched.contactNumber && !!formik.errors.contactNumber}
                      helperText={formik.touched.contactNumber && formik.errors.contactNumber} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="First Name" name="firstName" value={formik.values.firstName}
                      onChange={formik.handleChange} error={formik.touched.firstName && !!formik.errors.firstName}
                      helperText={formik.touched.firstName && formik.errors.firstName} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Last Name" name="lastName" value={formik.values.lastName}
                      onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField select fullWidth size="small" label="Gender" name="gender" value={formik.values.gender ?? ''} onChange={formik.handleChange}>
                      <MenuItem value="MALE">Male</MenuItem>
                      <MenuItem value="FEMALE">Female</MenuItem>
                      <MenuItem value="OTHER">Other</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" type="date" label="Date of Birth" name="dateOfBirth" InputLabelProps={{ shrink: true }}
                      value={formik.values.dateOfBirth} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField select fullWidth size="small" label="Source" name="source" value={formik.values.source ?? ''} onChange={formik.handleChange}>
                      <MenuItem value="WALK_IN">Walk In</MenuItem>
                      <MenuItem value="REFERRAL">Referral</MenuItem>
                      <MenuItem value="FACEBOOK">Facebook</MenuItem>
                      <MenuItem value="INSTAGRAM">Instagram</MenuItem>
                      <MenuItem value="WHATSAPP">Whatsapp</MenuItem>
                      <MenuItem value="GOOGLE">Google</MenuItem>
                      <MenuItem value="EXISTING_MEMBER">Existing Member</MenuItem>
                      <MenuItem value="OTHER">Other</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField select fullWidth size="small" label="Sports / Activity" name="sportActivity" value={formik.values.sportActivity ?? ''} onChange={formik.handleChange}>
                      <MenuItem value="GYM">Gym</MenuItem>
                      <MenuItem value="SWIMMING">Swimming</MenuItem>
                      <MenuItem value="OTHER">Other</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Goal" name="fitnessGoal" value={formik.values.fitnessGoal} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" type="date" label="Next Follow Up Date" name="nextFollowUpDate" InputLabelProps={{ shrink: true }}
                      value={formik.values.nextFollowUpDate} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField select fullWidth size="small" label="Executive" name="executiveId" value={formik.values.executiveId ?? ''} onChange={formik.handleChange}>
                      {currentUserId && !receptionists?.some((r) => r.id === currentUserId) && (
                        <MenuItem value={currentUserId}>{[currentFirstName, currentLastName].filter(Boolean).join(' ')} (You)</MenuItem>
                      )}
                      {receptionists?.map((r) => (
                        <MenuItem key={r.id} value={r.id}>
                          {r.firstName} {r.lastName}{r.id === currentUserId ? ' (You)' : ''}
                        </MenuItem>
                      ))}
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField select fullWidth size="small" label="Rating" name="rating" value={formik.values.rating ?? ''} onChange={formik.handleChange}>
                      <MenuItem value="COLD">Cold</MenuItem>
                      <MenuItem value="WARM">Warm</MenuItem>
                      <MenuItem value="HOT">Hot</MenuItem>
                      <MenuItem value="EXPECTED">Expected</MenuItem>
                      <MenuItem value="NOT_INTERESTED">Not Interested</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={12}>
                    <TextField fullWidth size="small" label="Address" name="address"
                      value={formik.values.address} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={12}>
                    <TextField fullWidth size="small" multiline rows={2} label="Any Comment" name="comment"
                      value={formik.values.comment} onChange={formik.handleChange} />
                  </Grid>
                </Grid>
              </Grid>
              <Grid
                size={{ xs: 12, md: 3 }}
                sx={{
                  pl: { md: 2 },
                  mt: { xs: 2, md: 0 },
                  borderLeft: { md: '1px solid' },
                  borderColor: { md: 'divider' },
                }}
              >
                <PhotoCapture file={photoFile} onChange={setPhotoFile} disabled={isSubmitting} height={180} />
                <Grid container spacing={1.5} sx={{ mt: 0.5 }}>
                  <Grid size={6}>
                    <TextField fullWidth size="small" type="number" label="Weight (kg)" name="weightKg" value={formik.values.weightKg ?? ''}
                      onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={6}>
                    <TextField fullWidth size="small" type="number" label="Height (cm)" name="heightCm" value={formik.values.heightCm ?? ''}
                      onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={12}>
                    <Button fullWidth variant="outlined" onClick={handleAnalyseDetails}>Analyse Details</Button>
                  </Grid>
                  {bmiResult !== null && (
                    <Grid size={12}>
                      <Typography variant="body2" color="text.secondary" textAlign="center">
                        BMI: <strong>{bmiResult}</strong>
                      </Typography>
                    </Grid>
                  )}
                </Grid>
              </Grid>
            </Grid>
          ) : (
            <>
          <Typography variant="subtitle2" color="text.secondary" gutterBottom>Basic Information</Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: showPhotoCapture ? 9 : 12 }}>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth label="First Name" name="firstName" value={formik.values.firstName}
                    onChange={formik.handleChange} error={formik.touched.firstName && !!formik.errors.firstName}
                    helperText={formik.touched.firstName && formik.errors.firstName} />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth label="Last Name" name="lastName" value={formik.values.lastName}
                    onChange={formik.handleChange} error={formik.touched.lastName && !!formik.errors.lastName}
                    helperText={formik.touched.lastName && formik.errors.lastName} />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth label="Email" name="email" value={formik.values.email}
                    onChange={formik.handleChange} error={formik.touched.email && !!formik.errors.email}
                    helperText={formik.touched.email && formik.errors.email} />
                </Grid>
                {(!inquiry || conversion) && <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth label={conversion ? 'Password' : isEdit ? 'New Password (optional)' : 'Password'} type="password" name="password"
                    value={formik.values.password} onChange={formik.handleChange}
                    error={formik.touched.password && !!formik.errors.password}
                    helperText={formik.touched.password && formik.errors.password} />
                </Grid>}
              </Grid>
            </Grid>
            {showPhotoCapture && (
              <Grid size={{ xs: 12, md: 3 }}>
                <PhotoCapture file={photoFile} onChange={setPhotoFile} disabled={isSubmitting} />
              </Grid>
            )}
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField select fullWidth label="Gender" name="gender" value={formik.values.gender ?? ''} onChange={formik.handleChange}>
                <MenuItem value="MALE">Male</MenuItem>
                <MenuItem value="FEMALE">Female</MenuItem>
                <MenuItem value="OTHER">Other</MenuItem>
              </TextField>
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField fullWidth type="date" label="Date of Birth" name="dateOfBirth" InputLabelProps={{ shrink: true }}
                value={formik.values.dateOfBirth} onChange={formik.handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField fullWidth label="Contact Number" name="contactNumber" value={formik.values.contactNumber}
                onChange={formik.handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField fullWidth type="number" label="Height (cm)" name="heightCm" value={formik.values.heightCm ?? ''}
                onChange={formik.handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField fullWidth type="number" label="Weight (kg)" name="weightKg" value={formik.values.weightKg ?? ''}
                onChange={formik.handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField fullWidth label="Fitness Goal" name="fitnessGoal" value={formik.values.fitnessGoal}
                onChange={formik.handleChange} />
            </Grid>
            <Grid size={12}>
              <TextField fullWidth label="Address" name="address" value={formik.values.address} onChange={formik.handleChange} />
            </Grid>

            <Grid size={12}><Divider sx={{ my: 1 }} /><Typography variant="subtitle2" color="text.secondary">Medical Details</Typography></Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControlLabel control={<Checkbox checked={formik.values.diabetes} name="diabetes" onChange={formik.handleChange} />} label="Diabetes" />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControlLabel control={<Checkbox checked={formik.values.hypertension} name="hypertension" onChange={formik.handleChange} />} label="Hypertension" />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControlLabel control={<Checkbox checked={formik.values.asthma} name="asthma" onChange={formik.handleChange} />} label="Asthma" />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label="Allergies" name="allergies" value={formik.values.allergies} onChange={formik.handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label="Injuries" name="injuries" value={formik.values.injuries} onChange={formik.handleChange} />
            </Grid>
            <Grid size={12}>
              <TextField fullWidth multiline rows={2} label="Medical Notes" name="medicalNotes" value={formik.values.medicalNotes}
                onChange={formik.handleChange} />
            </Grid>
          </Grid>
            </>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={isSubmitting}>
            {createdClientId ? 'Retry Photo Upload' : conversion ? 'Convert to Client' : isEdit ? 'Update Client' : inquiry ? 'Register Inquiry' : 'Register Client'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
