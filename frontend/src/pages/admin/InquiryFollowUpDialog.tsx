import { useEffect, useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  Button,
  TextField,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Divider,
  Typography,
  Avatar,
  Alert,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import PersonIcon from '@mui/icons-material/Person';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import type { ClientResponse, FollowUpResponse } from '@/types';
import { useAddFollowUpMutation, useClientProfileImageQuery, useFollowUpsQuery, type FollowUpRequest } from '@/api/followUpsApi';
import { useReceptionistsQuery } from '@/api/usersApi';
import { extractErrorMessage } from '@/api/axiosClient';
import { useAppSelector } from '@/app/hooks';

interface InquiryFollowUpDialogProps {
  open: boolean;
  onClose: () => void;
  client: ClientResponse | null;
}

const RATING_OPTIONS: Array<{ value: string; label: string }> = [
  { value: 'COLD', label: 'Cold' },
  { value: 'WARM', label: 'Warm' },
  { value: 'HOT', label: 'Hot' },
  { value: 'EXPECTED', label: 'Expected' },
  { value: 'NOT_INTERESTED', label: 'Not Interested' },
];

export function InquiryFollowUpDialog({ open, onClose, client }: InquiryFollowUpDialogProps) {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const { userId: currentUserId, firstName: currentFirstName, lastName: currentLastName } = useAppSelector((state) => state.auth);
  const { data: receptionists } = useReceptionistsQuery();
  const { data: photoUrl } = useClientProfileImageQuery(client?.id);
  const { data: history } = useFollowUpsQuery(client?.id);
  const addFollowUpMutation = useAddFollowUpMutation();

  const formik = useFormik<FollowUpRequest & { noFurtherFollowUp: boolean }>({
    initialValues: {
      comment: '',
      executiveId: client?.executiveId ?? currentUserId ?? undefined,
      nextFollowUpDate: client?.nextFollowUpDate ?? '',
      rating: client?.rating,
      noFurtherFollowUp: false,
    },
    validationSchema: Yup.object({
      rating: Yup.string().required('Rating is required'),
    }),
    enableReinitialize: true,
    onSubmit: async (values) => {
      if (!client) return;
      setErrorMessage(null);
      try {
        const { noFurtherFollowUp, ...payload } = values;
        await addFollowUpMutation.mutateAsync({
          clientId: client.id,
          payload: { ...payload, nextFollowUpDate: noFurtherFollowUp ? undefined : payload.nextFollowUpDate },
        });
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  useEffect(() => {
    setErrorMessage(null);
  }, [client, open]);

  const historyColumns: GridColDef<FollowUpResponse>[] = [
    { field: 'followUpDate', headerName: 'Follow Up Date', width: 130 },
    { field: 'comment', headerName: 'Comment', flex: 1, minWidth: 200 },
    { field: 'executiveName', headerName: 'Executive', width: 160, valueGetter: (_v, row) => row.executiveName ?? '-' },
    { field: 'nextFollowUpDate', headerName: 'Next Follow Up Date', width: 160, valueGetter: (_v, row) => row.nextFollowUpDate ?? '-' },
    {
      field: 'rating', headerName: 'Rating', width: 130,
      valueGetter: (_v, row) => row.rating ? row.rating.replace('_', ' ') : '-',
    },
  ];

  if (!client) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Follow Up - {client.firstName} {client.lastName}</DialogTitle>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent dividers>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, sm: 3 }} sx={{ display: 'flex', justifyContent: 'center' }}>
              <Avatar src={photoUrl} sx={{ width: 120, height: 120 }}>
                <PersonIcon sx={{ fontSize: 56 }} />
              </Avatar>
            </Grid>
            <Grid size={{ xs: 12, sm: 9 }}>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="body2" color="text.secondary">Name</Typography>
                  <Typography variant="subtitle1">{client.firstName} {client.lastName}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="body2" color="text.secondary">Contact</Typography>
                  <Typography variant="subtitle1">{client.contactNumber ?? '-'}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth type="date" label="Date" value={new Date().toLocaleDateString('en-CA')}
                    InputLabelProps={{ shrink: true }} InputProps={{ readOnly: true }} disabled />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField select fullWidth label="Rating" name="rating" value={formik.values.rating ?? ''} onChange={formik.handleChange}
                    error={formik.touched.rating && !!formik.errors.rating} helperText={formik.touched.rating && formik.errors.rating}>
                    {RATING_OPTIONS.map((option) => (
                      <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
                    ))}
                  </TextField>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField select fullWidth label="Executive" name="executiveId" value={formik.values.executiveId ?? ''} onChange={formik.handleChange}>
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
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth type="date" label="Next Follow Up Date" name="nextFollowUpDate" InputLabelProps={{ shrink: true }}
                    value={formik.values.nextFollowUpDate ?? ''} onChange={formik.handleChange} disabled={formik.values.noFurtherFollowUp} />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }} sx={{ display: 'flex', alignItems: 'center' }}>
                  <FormControlLabel
                    control={<Checkbox checked={formik.values.noFurtherFollowUp} name="noFurtherFollowUp" onChange={formik.handleChange} />}
                    label="No further follow-up required"
                  />
                </Grid>
                <Grid size={12}>
                  <TextField fullWidth multiline rows={3} label="Comment" name="comment"
                    value={formik.values.comment} onChange={formik.handleChange} />
                </Grid>
                <Grid size={12} sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                  <Button onClick={onClose}>Cancel</Button>
                  <Button type="submit" variant="contained" disabled={addFollowUpMutation.isPending}>Submit</Button>
                </Grid>
              </Grid>
            </Grid>
          </Grid>

          <Divider sx={{ my: 3 }} />
          <Typography variant="subtitle2" color="text.secondary" gutterBottom>History of Follow Ups</Typography>
          <div style={{ height: 260, width: '100%' }}>
            <DataGrid
              rows={history ?? []}
              columns={historyColumns}
              hideFooterSelectedRowCount
              pageSizeOptions={[5, 10]}
              initialState={{ pagination: { paginationModel: { pageSize: 5 } } }}
            />
          </div>
        </DialogContent>
      </form>
    </Dialog>
  );
}
