import { useState } from 'react';
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, FormControl,
  InputLabel, MenuItem, Paper, Select, Stack, TextField } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { PageHeader } from '@/components/common/PageHeader';
import { useAppSelector } from '@/app/hooks';
import { extractErrorMessage } from '@/api/axiosClient';
import { useBranchesQuery } from '@/api/multiBranchApi';
import { useCreateUserMutation, useUsersQuery, type UserPayload } from '@/api/usersApi';
import type { Role, UserResponse } from '@/types';

const STAFF_ROLES: Role[] = ['COACH', 'DIETICIAN', 'RECEPTIONIST'];

export function UserManagementPage() {
  const auth = useAppSelector((state) => state.auth);
  const isOrganizationAdmin = auth.role === 'ORGANIZATION_ADMIN';
  const allowedRoles = isOrganizationAdmin ? ['BRANCH_MANAGER' as Role] : STAFF_ROLES;
  const [page, setPage] = useState(0);
  const [open, setOpen] = useState(false);
  const emptyForm: UserPayload = { firstName: '', lastName: '', email: '', mobileNumber: '', password: '',
    shiftStartTime: '', shiftEndTime: '',
    role: allowedRoles[0], organizationId: auth.organizationId ?? undefined,
    branchId: isOrganizationAdmin ? undefined : auth.branchId ?? undefined, active: true };
  const [form, setForm] = useState<UserPayload>(emptyForm);
  const users = useUsersQuery(page);
  const branches = useBranchesQuery(auth.organizationId ?? undefined, '', 0, 100);
  const create = useCreateUserMutation();
  const columns: GridColDef<UserResponse>[] = [
    { field: 'firstName', headerName: 'First name', flex: 1 },
    { field: 'lastName', headerName: 'Last name', flex: 1 },
    { field: 'email', headerName: 'Email', flex: 1.5 },
    { field: 'role', headerName: 'Role', width: 170 },
    { field: 'branchName', headerName: 'Branch', flex: 1 },
    { field: 'shift', headerName: 'Shift', width: 150,
      valueGetter: (_value, row) => row.shiftStartTime && row.shiftEndTime
        ? `${row.shiftStartTime.slice(0, 5)} - ${row.shiftEndTime.slice(0, 5)}` : '-' },
    { field: 'active', headerName: 'Status', width: 100, valueFormatter: (value) => value ? 'Active' : 'Inactive' },
  ];
  const submit = async () => {
    await create.mutateAsync(form);
    setOpen(false);
    setForm(emptyForm);
  };

  return <Box><PageHeader title={isOrganizationAdmin ? 'Branch Manager' : 'Branch Staff'}
    subtitle={isOrganizationAdmin ? 'Create managers for branches in your organization' : 'Create coaches, dieticians, and receptionists for your branch'}
    action={<Button variant="contained" startIcon={<PersonAddIcon />} onClick={() => setOpen(true)}>{isOrganizationAdmin ? 'New Manager' : 'New User'}</Button>} />
    {users.error && <Alert severity="error" sx={{ mb: 2 }}>{extractErrorMessage(users.error)}</Alert>}
    <Paper sx={{ height: 520 }}><DataGrid rows={users.data?.content ?? []} columns={columns} loading={users.isLoading}
      paginationMode="server" rowCount={users.data?.totalElements ?? 0} paginationModel={{ page, pageSize: 20 }}
      onPaginationModelChange={(model) => setPage(model.page)} /></Paper>
    <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm"><DialogTitle>{isOrganizationAdmin ? 'Create Branch Manager' : 'Create User'}</DialogTitle>
      <DialogContent><Stack spacing={2} sx={{ mt: 1 }}>
        {create.error && <Alert severity="error">{extractErrorMessage(create.error)}</Alert>}
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}><TextField required fullWidth label="First name" value={form.firstName}
            onChange={(event) => setForm({ ...form, firstName: event.target.value })} /></Grid>
          <Grid size={{ xs: 12, sm: 6 }}><TextField required fullWidth label="Last name" value={form.lastName}
            onChange={(event) => setForm({ ...form, lastName: event.target.value })} /></Grid>
          <Grid size={{ xs: 12, sm: 6 }}><TextField required fullWidth type="email" label="Email" value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })} /></Grid>
          <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Mobile number" value={form.mobileNumber}
            onChange={(event) => setForm({ ...form, mobileNumber: event.target.value })} /></Grid>
          <Grid size={{ xs: 12, sm: 6 }}><TextField required fullWidth type="password" label="Initial password" value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })} /></Grid>
          <Grid size={{ xs: 12, sm: 6 }}><FormControl fullWidth><InputLabel>Role</InputLabel><Select label="Role" value={form.role}
            onChange={(event) => setForm({ ...form, role: event.target.value as Role })}>
            {allowedRoles.map((role) => <MenuItem key={role} value={role}>{role.replaceAll('_', ' ')}</MenuItem>)}
          </Select></FormControl></Grid>
          <Grid size={{ xs: 12, sm: 6 }}><TextField required fullWidth type="time" label="Shift start"
            slotProps={{ inputLabel: { shrink: true } }} value={form.shiftStartTime}
            onChange={(event) => setForm({ ...form, shiftStartTime: event.target.value })} /></Grid>
          <Grid size={{ xs: 12, sm: 6 }}><TextField required fullWidth type="time" label="Shift end"
            slotProps={{ inputLabel: { shrink: true } }} value={form.shiftEndTime}
            onChange={(event) => setForm({ ...form, shiftEndTime: event.target.value })} /></Grid>
          {isOrganizationAdmin && <Grid size={12}><FormControl required fullWidth><InputLabel>Branch</InputLabel>
            <Select label="Branch" value={form.branchId ?? ''} onChange={(event) => setForm({ ...form, branchId: Number(event.target.value) })}>
              {branches.data?.content.map((branch) => <MenuItem key={branch.id} value={branch.id}>{branch.branchName}</MenuItem>)}
            </Select></FormControl></Grid>}
        </Grid>
      </Stack></DialogContent><DialogActions><Button onClick={() => setOpen(false)}>Cancel</Button>
        <Button variant="contained" disabled={create.isPending || !form.firstName || !form.lastName || !form.email || form.password.length < 6
          || !form.branchId || !form.shiftStartTime || !form.shiftEndTime}
          onClick={submit}>Create</Button></DialogActions></Dialog>
  </Box>;
}