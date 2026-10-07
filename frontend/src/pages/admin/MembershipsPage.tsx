import { useEffect, useMemo, useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Box, Paper, Button, TextField, Card, CardContent, Typography, IconButton, Tooltip,
  Dialog, DialogTitle, DialogContent, DialogActions, Alert, Chip, MenuItem, Tabs, Tab,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import {
  useMembershipPlansQuery, useCreateMembershipPlanMutation, useDeleteMembershipPlanMutation,
  useUpdateMembershipPlanMutation, useMembershipDiscountsQuery, useCreateMembershipDiscountMutation,
  useUpdateMembershipDiscountMutation, useMembershipDiscountStatusMutation,
  useActivitiesQuery, useCreateActivityMutation, useUpdateActivityMutation, useDeleteActivityMutation,
  type ActivityRequest, type MembershipDiscountRequest, type MembershipPlanRequest,
} from '@/api/membershipsApi';
import { extractErrorMessage } from '@/api/axiosClient';
import type { ActivityResponse, MembershipDiscountResponse, MembershipPlanResponse } from '@/types';

function ActivityFormDialog({ open, onClose, activity }: { open: boolean; onClose: () => void; activity?: ActivityResponse | null }) {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const createMutation = useCreateActivityMutation();
  const updateMutation = useUpdateActivityMutation();

  const formik = useFormik<ActivityRequest>({
    initialValues: { name: activity?.name ?? '', description: activity?.description ?? '' },
    validationSchema: Yup.object({
      name: Yup.string().required('Sport / Activity name is required'),
    }),
    enableReinitialize: true,
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        if (activity) await updateMutation.mutateAsync({ id: activity.id, payload: values });
        else await createMutation.mutateAsync(values);
        formik.resetForm();
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{activity ? 'Edit Sport / Activity' : 'Create Sport / Activity'}</DialogTitle>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
          <TextField fullWidth margin="normal" label="Name" name="name" value={formik.values.name} onChange={formik.handleChange}
            error={formik.touched.name && !!formik.errors.name} helperText={formik.touched.name && formik.errors.name} />
          <TextField fullWidth margin="normal" label="Description" name="description" value={formik.values.description} onChange={formik.handleChange} />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={createMutation.isPending || updateMutation.isPending}>{activity ? 'Save' : 'Create'}</Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

function PlanFormDialog({ open, onClose, plan }: { open: boolean; onClose: () => void; plan?: MembershipPlanResponse | null }) {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const createMutation = useCreateMembershipPlanMutation();
  const updateMutation = useUpdateMembershipPlanMutation();
  const { data: activities } = useActivitiesQuery();

  const formik = useFormik<MembershipPlanRequest>({
    initialValues: { name: plan?.name ?? '', durationDays: plan?.durationDays ?? 30, fees: plan?.fees ?? 0,
      extraDurationDays: plan?.extraDurationDays ?? 0,
      description: plan?.description ?? '',
      activityId: plan?.activityId },
    validationSchema: Yup.object({
      name: Yup.string().required('Plan name is required'),
      durationDays: Yup.number().positive().required('Duration is required'),
      fees: Yup.number().positive().required('Fees is required'),
      extraDurationDays: Yup.number().min(0).integer().required(),
    }),
    enableReinitialize: true,
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        if (plan) await updateMutation.mutateAsync({ id: plan.id, payload: values });
        else await createMutation.mutateAsync(values);
        formik.resetForm();
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{plan ? 'Edit Membership Plan' : 'Create Membership Plan'}</DialogTitle>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
          <TextField fullWidth margin="normal" label="Plan Name" name="name" value={formik.values.name} onChange={formik.handleChange}
            error={formik.touched.name && !!formik.errors.name} helperText={formik.touched.name && formik.errors.name} />
          <TextField select fullWidth margin="normal" label="Sport / Activity" name="activityId"
            value={formik.values.activityId ?? ''} onChange={formik.handleChange}>
            <MenuItem value="">-- None --</MenuItem>
            {activities?.map((activity) => (
              <MenuItem key={activity.id} value={activity.id}>{activity.name}</MenuItem>
            ))}
          </TextField>
          <TextField fullWidth margin="normal" type="number" label="Duration (days)" name="durationDays"
            value={formik.values.durationDays} onChange={formik.handleChange} />
          <TextField fullWidth margin="normal" type="number" label="Fees" name="fees" value={formik.values.fees} onChange={formik.handleChange} />
          {plan && <TextField fullWidth margin="normal" type="number" label="Extra Free Days" name="extraDurationDays"
            inputProps={{ min: 0, step: 1 }} value={formik.values.extraDurationDays} onChange={formik.handleChange} />}
          <TextField fullWidth margin="normal" label="Description" name="description" value={formik.values.description} onChange={formik.handleChange} />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={createMutation.isPending || updateMutation.isPending}>{plan ? 'Save' : 'Create'}</Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

function DiscountFormDialog({ open, onClose, discount }: { open: boolean; onClose: () => void; discount?: MembershipDiscountResponse | null }) {
  const create = useCreateMembershipDiscountMutation();
  const update = useUpdateMembershipDiscountMutation();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [discountType, setDiscountType] = useState<'PERCENTAGE' | 'FREE_DAYS'>('PERCENTAGE');
  const formik = useFormik<MembershipDiscountRequest>({
    initialValues: { name: discount?.name ?? '', percentage: discount?.percentage ?? 0,
      extraFreeDays: discount?.extraFreeDays ?? 0, description: discount?.description ?? '', active: discount?.active ?? true },
    validationSchema: Yup.object({ name: Yup.string().required('Offer name is required'),
      percentage: Yup.number().min(0).lessThan(100).required('Percentage is required'),
      extraFreeDays: Yup.number().min(0).integer().required('Extra free days is required')
    }).test('benefit-required', 'Enter a value for the selected discount type', (values) =>
      discountType === 'PERCENTAGE' ? (values.percentage ?? 0) > 0 : (values.extraFreeDays ?? 0) > 0),
    enableReinitialize: true,
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        if (discount) await update.mutateAsync({ id: discount.id, payload: values });
        else await create.mutateAsync(values);
        formik.resetForm();
        onClose();
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  useEffect(() => {
    if (open) {
      setErrorMessage(null);
      setDiscountType(discount?.extraFreeDays ? 'FREE_DAYS' : 'PERCENTAGE');
    }
  }, [discount, open]);

  return <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth><DialogTitle>{discount ? 'Edit Discount' : 'New Discount'}</DialogTitle>
    <form onSubmit={formik.handleSubmit}><DialogContent>{errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}
      <TextField fullWidth margin="normal" label="Offer Name" name="name" value={formik.values.name} onChange={formik.handleChange} />
      <TextField select fullWidth margin="normal" label="Discount Type" value={discountType} onChange={(event) => {
        const type = event.target.value as 'PERCENTAGE' | 'FREE_DAYS';
        setDiscountType(type);
        formik.setFieldValue(type === 'PERCENTAGE' ? 'extraFreeDays' : 'percentage', 0);
      }}>
        <MenuItem value="PERCENTAGE">Percentage</MenuItem>
        <MenuItem value="FREE_DAYS">Extra Free Days</MenuItem>
      </TextField>
      {discountType === 'PERCENTAGE' ? <TextField fullWidth margin="normal" type="number" label="Discount (%)" name="percentage" inputProps={{ min: 0.01, max: 99.99, step: 0.01 }}
        value={formik.values.percentage} onChange={formik.handleChange} />
        : <TextField fullWidth margin="normal" type="number" label="Extra Free Days" name="extraFreeDays" inputProps={{ min: 1, step: 1 }}
          value={formik.values.extraFreeDays} onChange={formik.handleChange} />}
      <TextField fullWidth margin="normal" label="Description" name="description" value={formik.values.description} onChange={formik.handleChange} />
    </DialogContent><DialogActions><Button onClick={onClose}>Cancel</Button>
      <Button type="submit" variant="contained" disabled={create.isPending || update.isPending}>{discount ? 'Save' : 'Create'}</Button>
    </DialogActions></form></Dialog>;
}

const TAB_LABELS = ['Sport / Activity', 'Membership Plans', 'Discounts'] as const;

export function MembershipsPage() {
  const [tab, setTab] = useState(0);
  const [keyword, setKeyword] = useState('');

  const { data: activities } = useActivitiesQuery();
  const { data: plans } = useMembershipPlansQuery();
  const { data: discounts } = useMembershipDiscountsQuery();
  const deleteMutation = useDeleteMembershipPlanMutation();
  const deleteActivityMutation = useDeleteActivityMutation();
  const discountStatus = useMembershipDiscountStatusMutation();

  const [activityFormOpen, setActivityFormOpen] = useState(false);
  const [activityTarget, setActivityTarget] = useState<ActivityResponse | null>(null);
  const [deleteActivityTarget, setDeleteActivityTarget] = useState<ActivityResponse | null>(null);
  const [planFormOpen, setPlanFormOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<MembershipPlanResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<MembershipPlanResponse | null>(null);
  const [discountFormOpen, setDiscountFormOpen] = useState(false);
  const [discountTarget, setDiscountTarget] = useState<MembershipDiscountResponse | null>(null);

  const filteredActivities = useMemo(
    () => activities?.filter((a) => a.name.toLowerCase().includes(keyword.trim().toLowerCase())),
    [activities, keyword]
  );
  const filteredPlans = useMemo(
    () => plans?.filter((p) => p.name.toLowerCase().includes(keyword.trim().toLowerCase())),
    [plans, keyword]
  );
  const filteredDiscounts = useMemo(
    () => discounts?.filter((d) => d.name.toLowerCase().includes(keyword.trim().toLowerCase())),
    [discounts, keyword]
  );

  return (
    <Box>
      <PageHeader
        title="Memberships"
        subtitle="Manage sports/activities, membership plans, and invoice discount offers"
        action={
          <Box display="flex" gap={1} flexWrap="wrap">
            <Button variant="outlined" startIcon={<AddIcon />} onClick={() => { setActivityTarget(null); setActivityFormOpen(true); }}>New Sport / Activity</Button>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditTarget(null); setPlanFormOpen(true); }}>New Membership Plan</Button>
            <Button variant="outlined" startIcon={<AddIcon />} onClick={() => { setDiscountTarget(null); setDiscountFormOpen(true); }}>New Discount</Button>
          </Box>
        }
      />

      <Paper sx={{ p: 2, mb: 2 }}>
        <TextField
          size="small"
          placeholder="Tab wise search by name"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          sx={{ width: 320 }}
        />
      </Paper>

      <Tabs value={tab} onChange={(_e, value) => setTab(value)} sx={{ mb: 2, borderBottom: 1, borderColor: 'divider' }}>
        {TAB_LABELS.map((label) => <Tab key={label} label={label} />)}
      </Tabs>

      {tab === 0 && (
        <Grid container spacing={2}>
          {filteredActivities?.map((activity) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={activity.id}>
              <Card>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                    <Typography variant="h6">{activity.name}</Typography>
                    <Box>
                      <Tooltip title="Edit sport/activity"><IconButton size="small" onClick={() => { setActivityTarget(activity); setActivityFormOpen(true); }}>
                        <EditIcon fontSize="small" /></IconButton></Tooltip>
                      <Tooltip title="Delete sport/activity"><IconButton size="small" onClick={() => setDeleteActivityTarget(activity)}>
                        <DeleteIcon fontSize="small" /></IconButton></Tooltip>
                    </Box>
                  </Box>
                  {activity.description && (
                    <Typography variant="body2" color="text.secondary" mt={1}>{activity.description}</Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
          ))}
          {!filteredActivities?.length && (
            <Grid size={12}>
              <Paper sx={{ p: 4, textAlign: 'center' }}>
                <Typography color="text.secondary">No sports/activities found. Create one to get started.</Typography>
              </Paper>
            </Grid>
          )}
        </Grid>
      )}

      {tab === 1 && (
        <Grid container spacing={2}>
          {filteredPlans?.map((plan) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={plan.id}>
              <Card>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                    <Typography variant="h6">{plan.name}</Typography>
                    <Box><Tooltip title="Edit plan"><IconButton size="small" onClick={() => { setEditTarget(plan); setPlanFormOpen(true); }}>
                      <EditIcon fontSize="small" /></IconButton></Tooltip>
                      <Tooltip title="Delete plan"><IconButton size="small" onClick={() => setDeleteTarget(plan)}>
                        <DeleteIcon fontSize="small" /></IconButton></Tooltip></Box>
                  </Box>
                  {plan.activityName && <Chip size="small" label={plan.activityName} color="info" sx={{ mr: 1, mt: 1 }} />}
                  <Chip size="small" label={`${plan.durationDays} days`} sx={{ mr: 1, mt: 1 }} />
                  <Chip size="small" label={`₹ ${plan.fees}`} color="primary" sx={{ mt: 1 }} />
                  {plan.extraDurationDays > 0 && <Chip size="small" label={`+${plan.extraDurationDays} free days`} color="secondary" sx={{ ml: 1, mt: 1 }} />}
                  {plan.description && (
                    <Typography variant="body2" color="text.secondary" mt={1}>{plan.description}</Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
          ))}
          {!filteredPlans?.length && (
            <Grid size={12}>
              <Paper sx={{ p: 4, textAlign: 'center' }}>
                <Typography color="text.secondary">No membership plans found. Create one to get started.</Typography>
              </Paper>
            </Grid>
          )}
        </Grid>
      )}

      {tab === 2 && (
        <Grid container spacing={2}>
          {filteredDiscounts?.map((discount) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={discount.id}>
              <Card><CardContent><Box display="flex" justifyContent="space-between" alignItems="flex-start">
                <Typography variant="h6">{discount.name}</Typography><Tooltip title="Edit discount"><IconButton size="small"
                  onClick={() => { setDiscountTarget(discount); setDiscountFormOpen(true); }}><EditIcon fontSize="small" /></IconButton></Tooltip>
              </Box>{discount.percentage > 0 && <Chip size="small" label={`${discount.percentage}% off`} color="success" sx={{ mt: 1, mr: 1 }} />}
                {discount.extraFreeDays > 0 && <Chip size="small" label={`+${discount.extraFreeDays} free days`} color="secondary" sx={{ mt: 1, mr: 1 }} />}
                <Chip size="small" label={discount.active ? 'Active' : 'Inactive'} variant="outlined" sx={{ mt: 1 }} />
                {discount.description && <Typography variant="body2" color="text.secondary" mt={1}>{discount.description}</Typography>}
                <Button size="small" sx={{ mt: 1 }} onClick={() => discountStatus.mutate({ id: discount.id, active: !discount.active })}>
                  {discount.active ? 'Deactivate' : 'Activate'}
                </Button></CardContent></Card>
            </Grid>
          ))}
          {!filteredDiscounts?.length && (
            <Grid size={12}>
              <Paper sx={{ p: 4, textAlign: 'center' }}>
                <Typography color="text.secondary">No discounts found. Create one to get started.</Typography>
              </Paper>
            </Grid>
          )}
        </Grid>
      )}

      <ActivityFormDialog open={activityFormOpen} activity={activityTarget} onClose={() => setActivityFormOpen(false)} />
      <PlanFormDialog open={planFormOpen} plan={editTarget} onClose={() => setPlanFormOpen(false)} />
      <DiscountFormDialog open={discountFormOpen} discount={discountTarget} onClose={() => setDiscountFormOpen(false)} />
      <ConfirmDialog
        open={!!deleteActivityTarget}
        title="Delete Sport / Activity"
        message={`Delete '${deleteActivityTarget?.name}'? This cannot be undone.`}
        onClose={() => setDeleteActivityTarget(null)}
        confirmColor="error"
        onConfirm={() => deleteActivityTarget && deleteActivityMutation.mutate(deleteActivityTarget.id)}
      />
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
