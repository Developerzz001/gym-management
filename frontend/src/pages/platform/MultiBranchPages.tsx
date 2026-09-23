import { useState } from 'react';
import { Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControl, IconButton, InputLabel, MenuItem, Paper, Select, Stack, TextField, Tooltip, Typography } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddBusinessIcon from '@mui/icons-material/AddBusiness';
import DownloadIcon from '@mui/icons-material/Download';
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import SaveIcon from '@mui/icons-material/Save';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { PageHeader } from '@/components/common/PageHeader';
import { extractErrorMessage } from '@/api/axiosClient';
import { useAppSelector } from '@/app/hooks';
import { useBranchesQuery, useBranchDashboardQuery, useBranchSettingsQuery, useBranchStatusMutation,
  useOrganizationDashboardQuery, useOrganizationsQuery, useOrganizationStatusMutation, useSaveBranchMutation,
  useSaveBranchSettingsMutation, useSaveOrganizationMutation, useTransferMutation } from '@/api/multiBranchApi';
import type { BranchPerformanceResponse, BranchResponse, OrganizationResponse } from '@/types';

const money = (value = 0) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);

function ErrorState({ error }: { error: unknown }) {
  return error ? <Alert severity="error" sx={{ mb: 2 }}>{extractErrorMessage(error)}</Alert> : null;
}

function ScopeSelectors({ organizationId, branchId, onOrganization, onBranch, branches = true }: {
  organizationId: number | ''; branchId?: number | ''; onOrganization: (id: number | '') => void;
  onBranch?: (id: number | '') => void; branches?: boolean;
}) {
  const role = useAppSelector((state) => state.auth.role);
  const { data: organizations } = useOrganizationsQuery('', 0, 100, role === 'SUPER_ADMIN');
  const { data: branchPage } = useBranchesQuery(organizationId || undefined, '', 0, 100, branches && !!organizationId);
  return <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} mb={2}>
    {role === 'SUPER_ADMIN' && <FormControl size="small" sx={{ minWidth: 240 }}>
      <InputLabel>Organization</InputLabel><Select label="Organization" value={organizationId}
        onChange={(event) => onOrganization(Number(event.target.value) || '')}>
        <MenuItem value="">Select organization</MenuItem>
        {organizations?.content.map((item) => <MenuItem key={item.id} value={item.id}>{item.name}</MenuItem>)}
      </Select>
    </FormControl>}
    {branches && <FormControl size="small" sx={{ minWidth: 240 }}>
      <InputLabel>Branch</InputLabel><Select label="Branch" value={branchId ?? ''}
        onChange={(event) => onBranch?.(Number(event.target.value) || '')}>
        <MenuItem value="">Select branch</MenuItem>
        {branchPage?.content.map((item) => <MenuItem key={item.id} value={item.id}>{item.branchName}</MenuItem>)}
      </Select>
    </FormControl>}
  </Stack>;
}

export function OrganizationManagementPage() {
  const [keyword, setKeyword] = useState(''); const [page, setPage] = useState(0); const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<OrganizationResponse | null>(null);
  const [statusTarget, setStatusTarget] = useState<OrganizationResponse | null>(null);
  const [form, setForm] = useState({ name: '', ownerName: '', email: '', adminPassword: '', contactNumber: '', address: '' });
  const { data, isLoading, error } = useOrganizationsQuery(keyword, page);
  const save = useSaveOrganizationMutation(editing?.id); const status = useOrganizationStatusMutation();
  const columns: GridColDef<OrganizationResponse>[] = [
    { field: 'code', headerName: 'Code', width: 120 }, { field: 'name', headerName: 'Organization', flex: 1 },
    { field: 'ownerName', headerName: 'Owner', flex: 1 }, { field: 'email', headerName: 'Email', flex: 1 },
    { field: 'status', headerName: 'Status', width: 110 },
    { field: 'actions', headerName: 'Actions', width: 110, sortable: false, filterable: false,
      renderCell: ({ row }) => <Stack direction="row">
        <Tooltip title="Edit organization"><IconButton size="small" onClick={() => {
          setEditing(row); setForm({ name: row.name, ownerName: row.ownerName, email: row.email,
            adminPassword: '', contactNumber: row.contactNumber ?? '', address: row.address ?? '' }); setOpen(true);
        }}><EditIcon fontSize="small" /></IconButton></Tooltip>
        <Tooltip title={row.status === 'ACTIVE' ? 'Deactivate organization' : 'Reactivate organization'}>
          <IconButton size="small" color={row.status === 'ACTIVE' ? 'error' : 'success'} onClick={() => setStatusTarget(row)}>
            {row.status === 'ACTIVE' ? <BlockIcon fontSize="small" /> : <CheckCircleOutlineIcon fontSize="small" />}
          </IconButton>
        </Tooltip>
      </Stack> },
  ];
  const submit = async () => { if (!form.name || !form.ownerName || !form.email
    || (!editing && (form.adminPassword.length < 6 || !form.contactNumber || !form.address))) return;
    const payload = editing ? { ...form, adminPassword: undefined } : form;
    await save.mutateAsync(payload); setOpen(false); setEditing(null);
    setForm({ name: '', ownerName: '', email: '', adminPassword: '', contactNumber: '', address: '' }); };
  return <Box><PageHeader title="Organizations" subtitle="Manage organizations and ownership"
    action={<Button variant="contained" startIcon={<AddBusinessIcon />} onClick={() => { setEditing(null);
      setForm({ name: '', ownerName: '', email: '', adminPassword: '', contactNumber: '', address: '' }); setOpen(true);
    }}>New Organization</Button>} />
    <ErrorState error={error || save.error || status.error} /><Paper sx={{ p: 2, mb: 2 }}><TextField size="small" label="Search"
      value={keyword} onChange={(event) => { setKeyword(event.target.value); setPage(0); }} /></Paper>
    <Paper sx={{ height: 520 }}><DataGrid rows={data?.content ?? []} columns={columns} loading={isLoading}
      paginationMode="server" rowCount={data?.totalElements ?? 0} paginationModel={{ page, pageSize: 20 }}
      onPaginationModelChange={(model) => setPage(model.page)} /></Paper>
    <ConfirmDialog open={!!statusTarget} title={`${statusTarget?.status === 'ACTIVE' ? 'Deactivate' : 'Reactivate'} Organization`}
      message={statusTarget?.status === 'ACTIVE'
        ? `Deactivate ${statusTarget?.name}? All owners, branch managers, staff, and clients will be unable to log in until it is reactivated.`
        : `Reactivate ${statusTarget?.name}? Active accounts in this organization will be able to log in again.`}
      onClose={() => setStatusTarget(null)} confirmColor={statusTarget?.status === 'ACTIVE' ? 'error' : 'primary'}
      onConfirm={() => statusTarget && status.mutate({ id: statusTarget.id, active: statusTarget.status !== 'ACTIVE' })} />
    <Dialog open={open} onClose={() => { setOpen(false); setEditing(null); }} fullWidth maxWidth="sm">
      <DialogTitle>{editing ? 'Edit organization' : 'Create organization'}</DialogTitle>
      <DialogContent><Grid container spacing={2} sx={{ mt: 0 }}>
        {Object.keys(form).filter((key) => !editing || key !== 'adminPassword').map((key) => <Grid key={key} size={{ xs: 12, sm: 6 }}><TextField fullWidth type={key === 'adminPassword' ? 'password' : 'text'} required={['name','ownerName','email'].includes(key) || (!editing && ['adminPassword','contactNumber','address'].includes(key))}
          label={key.replace(/([A-Z])/g, ' $1')} value={form[key as keyof typeof form]}
          onChange={(event) => setForm({ ...form, [key]: event.target.value })} /></Grid>)}
      </Grid><ErrorState error={save.error} /></DialogContent><DialogActions><Button onClick={() => { setOpen(false); setEditing(null); }}>Cancel</Button>
        <Button variant="contained" disabled={save.isPending || !form.name || !form.ownerName || !form.email
          || (!editing && (form.adminPassword.length < 6 || !form.contactNumber || !form.address))}
          onClick={submit}>{editing ? 'Save' : 'Create'}</Button></DialogActions></Dialog>
  </Box>;
}

export function BranchManagementPage() {
  const auth = useAppSelector((state) => state.auth); const [organizationId, setOrganizationId] = useState<number | ''>(auth.organizationId ?? '');
  const [keyword, setKeyword] = useState(''); const [page, setPage] = useState(0); const [open, setOpen] = useState(false);
  const [statusTarget, setStatusTarget] = useState<BranchResponse | null>(null);
  const empty = { organizationId: Number(organizationId), branchName: '', address: '', city: '', state: '', country: 'India', pincode: '', contactNumber: '', email: '', managerId: undefined };
  const organizationSelected = !!organizationId;
  const [form, setForm] = useState(empty); const { data, isLoading, error } = useBranchesQuery(organizationId || undefined, keyword, page, 20, organizationSelected);
  const save = useSaveBranchMutation(); const status = useBranchStatusMutation();
  const columns: GridColDef<BranchResponse>[] = [
    { field: 'branchCode', headerName: 'Code', width: 110 }, { field: 'branchName', headerName: 'Branch', flex: 1 },
    { field: 'organizationName', headerName: 'Organization', flex: 1 }, { field: 'city', headerName: 'City', width: 130 },
    { field: 'managerName', headerName: 'Manager', flex: 1 }, { field: 'status', headerName: 'Status', width: 100 },
  ];
  if (auth.role === 'ORGANIZATION_ADMIN') columns.push({ field: 'actions', headerName: '', width: 120, sortable: false,
    renderCell: ({ row }) => <Button size="small" onClick={() => setStatusTarget(row)}>
      {row.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
    </Button> });
  return <Box><PageHeader title="Branches" subtitle="Onboard, configure, and activate gym locations"
    action={auth.role === 'ORGANIZATION_ADMIN' ? <Button variant="contained" startIcon={<AddBusinessIcon />} disabled={!organizationId} onClick={() => {
      setForm({ ...empty, organizationId: Number(organizationId) }); setOpen(true);
    }}>New Branch</Button> : undefined} />
    <ScopeSelectors organizationId={organizationId} onOrganization={(id) => { setOrganizationId(id); setPage(0); }} branches={false} />
    <ErrorState error={error || save.error || status.error} />{organizationSelected ? <><Paper sx={{ p: 2, mb: 2 }}><TextField size="small" label="Search branches"
      value={keyword} onChange={(event) => setKeyword(event.target.value)} /></Paper>
    <Paper sx={{ height: 520 }}><DataGrid rows={data?.content ?? []} columns={columns} loading={isLoading}
      paginationMode="server" rowCount={data?.totalElements ?? 0} paginationModel={{ page, pageSize: 20 }} onPaginationModelChange={(model) => setPage(model.page)} /></Paper></>
      : <Alert severity={auth.role === 'ORGANIZATION_ADMIN' ? 'error' : 'info'}>
        {auth.role === 'ORGANIZATION_ADMIN'
          ? 'Your account is not assigned to an organization.'
          : 'Select an organization to view its branches.'}
      </Alert>}
    <ConfirmDialog open={!!statusTarget} title={`${statusTarget?.status === 'ACTIVE' ? 'Deactivate' : 'Activate'} Branch`}
      message={`${statusTarget?.status === 'ACTIVE' ? 'Deactivate' : 'Activate'} ${statusTarget?.branchName}?`}
      onClose={() => setStatusTarget(null)} confirmColor={statusTarget?.status === 'ACTIVE' ? 'error' : 'primary'}
      onConfirm={() => statusTarget && status.mutate({ id: statusTarget.id, active: statusTarget.status !== 'ACTIVE' })} />
    <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="md"><DialogTitle>Create branch</DialogTitle><DialogContent>
      <Grid container spacing={2} sx={{ mt: 0 }}>{Object.keys(form).filter((key) => !['organizationId','managerId'].includes(key)).map((key) =>
        <Grid key={key} size={{ xs: 12, sm: 6 }}><TextField fullWidth label={key.replace(/([A-Z])/g, ' $1')}
          required={['branchCode','branchName','address','city','state','country','pincode'].includes(key)} value={String(form[key as keyof typeof form] ?? '')}
          onChange={(event) => setForm({ ...form, [key]: event.target.value })} /></Grid>)}</Grid></DialogContent>
      <DialogActions><Button onClick={() => setOpen(false)}>Cancel</Button><Button variant="contained" disabled={save.isPending || !form.organizationId || !form.branchName}
        onClick={async () => { await save.mutateAsync(form); setOpen(false); }}>Create</Button></DialogActions></Dialog>
  </Box>;
}

function StatCards({ metrics }: { metrics: Array<[string, string | number]> }) {
  return <Grid container spacing={2}>{metrics.map(([label, value]) => <Grid key={label} size={{ xs: 12, sm: 6, lg: 3 }}>
    <Paper sx={{ p: 2.5, minHeight: 96 }}><Typography variant="h5" fontWeight={700}>{value}</Typography>
      <Typography color="text.secondary" variant="body2">{label}</Typography></Paper></Grid>)}</Grid>;
}

export function BranchDashboardPage() {
  const auth = useAppSelector((state) => state.auth); const [organizationId, setOrganizationId] = useState<number | ''>(auth.organizationId ?? '');
  const [branchId, setBranchId] = useState<number | ''>(auth.branchId ?? ''); const query = useBranchDashboardQuery(branchId || undefined);
  const data = query.data; return <Box><PageHeader title="Branch Dashboard" subtitle="Live operational performance for one branch" />
    <ScopeSelectors organizationId={organizationId} branchId={branchId} onOrganization={(id) => { setOrganizationId(id); setBranchId(''); }} onBranch={setBranchId} />
    <ErrorState error={query.error} />{query.isLoading ? <CircularProgress /> : data && <StatCards metrics={[
      ['Total Members', data.totalMembers], ['Active Members', data.activeMembers], ['New Members', data.newMembers],
      ['Expiring Memberships', data.expiringMemberships], ['Daily Revenue', money(data.dailyRevenue)],
      ['Monthly Revenue', money(data.monthlyRevenue)], ['Attendance Today', data.attendanceToday],
      ['Active Coaches', data.activeCoaches], ['Active Dieticians', data.activeDieticians]]} />}</Box>;
}

export function OrganizationDashboardPage() {
  const auth = useAppSelector((state) => state.auth); const [organizationId, setOrganizationId] = useState<number | ''>(auth.organizationId ?? '');
  const [from, setFrom] = useState(''); const [to, setTo] = useState(''); const query = useOrganizationDashboardQuery(organizationId || undefined, from || undefined, to || undefined);
  const max = Math.max(...(query.data?.branches.map((item) => item.revenue) ?? [1]));
  return <Box><PageHeader title="Organization Dashboard" subtitle="Consolidated performance across every branch" />
    <ScopeSelectors organizationId={organizationId} onOrganization={setOrganizationId} branches={false} />
    <Stack direction="row" spacing={2} mb={2}><TextField type="date" label="From" InputLabelProps={{ shrink: true }} value={from} onChange={(e) => setFrom(e.target.value)} />
      <TextField type="date" label="To" InputLabelProps={{ shrink: true }} value={to} onChange={(e) => setTo(e.target.value)} /></Stack>
    <ErrorState error={query.error} />{query.data && <><StatCards metrics={[["Total Branches", query.data.totalBranches], ["Total Members", query.data.totalMembers], ["Total Revenue", money(query.data.totalRevenue)]]} />
      <Paper sx={{ p: 3, mt: 2 }}><Typography variant="h6" mb={2}>Branch Revenue Comparison</Typography>{query.data.branches.map((item) =>
        <Box key={item.branchId} sx={{ mb: 2 }}><Stack direction="row" justifyContent="space-between"><Typography>{item.branchName}</Typography><Typography>{money(item.revenue)}</Typography></Stack>
          <Box sx={{ height: 12, bgcolor: 'grey.200', mt: 0.5 }}><Box sx={{ height: 12, width: `${Math.max(2, item.revenue / max * 100)}%`, bgcolor: 'primary.main' }} /></Box></Box>)}</Paper></>}</Box>;
}

export function TransfersPage() {
  const auth = useAppSelector((state) => state.auth); const [organizationId, setOrganizationId] = useState<number | ''>(auth.organizationId ?? '');
  const [branchId, setBranchId] = useState<number | ''>(''); const [kind, setKind] = useState<'members' | 'staff'>('members');
  const [subjectId, setSubjectId] = useState(''); const [reason, setReason] = useState(''); const transfer = useTransferMutation(kind, Number(subjectId));
  return <Box><PageHeader title="Branch Transfers" subtitle="Move members or staff while preserving historical records" />
    <ScopeSelectors organizationId={organizationId} branchId={branchId} onOrganization={setOrganizationId} onBranch={setBranchId} />
    <Paper sx={{ p: 3, maxWidth: 680 }}><Stack spacing={2}><FormControl><InputLabel>Transfer type</InputLabel><Select label="Transfer type" value={kind} onChange={(e) => setKind(e.target.value as typeof kind)}>
      <MenuItem value="members">Member</MenuItem><MenuItem value="staff">Staff</MenuItem></Select></FormControl>
      <TextField label={kind === 'members' ? 'Member ID' : 'Staff user ID'} type="number" value={subjectId} onChange={(e) => setSubjectId(e.target.value)} />
      <TextField label="Reason" multiline minRows={3} value={reason} onChange={(e) => setReason(e.target.value)} />
      <ErrorState error={transfer.error} />{transfer.isSuccess && <Alert severity="success">Transfer completed and audited.</Alert>}
      <Button variant="contained" startIcon={<SwapHorizIcon />} disabled={!subjectId || !branchId || !reason || transfer.isPending}
        onClick={() => transfer.mutate({ destinationBranchId: Number(branchId), reason })}>Transfer</Button></Stack></Paper></Box>;
}

function downloadReport(rows: BranchPerformanceResponse[], format: 'csv' | 'xls') {
  const headings = ['Branch', 'Members', 'Revenue', 'Attendance', 'Score'];
  const values = rows.map((row) => [row.branchName, row.members, row.revenue, row.attendance, row.performanceScore]);
  const separator = format === 'csv' ? ',' : '\t'; const body = [headings, ...values].map((row) => row.join(separator)).join('\n');
  const anchor = document.createElement('a'); anchor.href = URL.createObjectURL(new Blob([body], { type: format === 'csv' ? 'text/csv' : 'application/vnd.ms-excel' }));
  anchor.download = `branch-performance.${format}`; anchor.click(); URL.revokeObjectURL(anchor.href);
}

export function BranchReportsPage() {
  const auth = useAppSelector((state) => state.auth); const [organizationId, setOrganizationId] = useState<number | ''>(auth.organizationId ?? '');
  const [from, setFrom] = useState(''); const [to, setTo] = useState(''); const query = useOrganizationDashboardQuery(organizationId || undefined, from || undefined, to || undefined);
  const columns: GridColDef<BranchPerformanceResponse>[] = [{ field: 'branchName', headerName: 'Branch', flex: 1 },
    { field: 'members', headerName: 'Members', width: 120 }, { field: 'revenue', headerName: 'Revenue', width: 160, valueFormatter: (value) => money(Number(value)) },
    { field: 'attendance', headerName: 'Attendance', width: 130 }, { field: 'performanceScore', headerName: 'Score', width: 120 }];
  return <Box><PageHeader title="Branch Reports" subtitle="Revenue, membership, attendance, growth, comparison, transfer, and staff reporting" />
    <ScopeSelectors organizationId={organizationId} onOrganization={setOrganizationId} branches={false} /><Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} mb={2}>
      <TextField size="small" type="date" label="From" InputLabelProps={{ shrink: true }} value={from} onChange={(e) => setFrom(e.target.value)} />
      <TextField size="small" type="date" label="To" InputLabelProps={{ shrink: true }} value={to} onChange={(e) => setTo(e.target.value)} />
      <Button startIcon={<DownloadIcon />} onClick={() => downloadReport(query.data?.branches ?? [], 'csv')}>CSV</Button>
      <Button startIcon={<DownloadIcon />} onClick={() => downloadReport(query.data?.branches ?? [], 'xls')}>Excel</Button>
      <Button startIcon={<DownloadIcon />} onClick={() => window.print()}>PDF</Button></Stack><ErrorState error={query.error} />
    <Paper sx={{ height: 520 }}><DataGrid rows={query.data?.branches ?? []} getRowId={(row) => row.branchId} columns={columns} loading={query.isLoading} /></Paper></Box>;
}

export function BranchSettingsPage() {
  const auth = useAppSelector((state) => state.auth); const [organizationId, setOrganizationId] = useState<number | ''>(auth.organizationId ?? '');
  const [branchId, setBranchId] = useState<number | ''>(auth.branchId ?? ''); const query = useBranchSettingsQuery(branchId || undefined);
  const save = useSaveBranchSettingsMutation(branchId || undefined);
  return <Box><PageHeader title="Branch Settings" subtitle="Working hours, timezone, membership, notification, and attendance policies" />
    <ScopeSelectors organizationId={organizationId} branchId={branchId} onOrganization={setOrganizationId} onBranch={setBranchId} />
    <Paper sx={{ p: 3, maxWidth: 820 }}>{query.data && <SettingsForm key={query.data.branchId} initial={query.data}
      saving={save.isPending} error={save.error} saved={save.isSuccess} onSave={(value) => save.mutate(value)} />}</Paper></Box>;
}

function SettingsForm({ initial, saving, error, saved, onSave }: { initial: import('@/types').BranchSettingsResponse;
  saving: boolean; error: unknown; saved: boolean; onSave: (value: Omit<import('@/types').BranchSettingsResponse, 'branchId'>) => void }) {
  const [form, setForm] = useState({ workingHours: initial.workingHours, timezone: initial.timezone,
    membershipRules: initial.membershipRules ?? '', notificationPreferences: initial.notificationPreferences ?? '', attendanceRules: initial.attendanceRules ?? '' });
  return <Stack spacing={2}>{Object.keys(form).map((key) => <TextField key={key} label={key.replace(/([A-Z])/g, ' $1')}
    multiline={key !== 'timezone'} minRows={key === 'timezone' ? undefined : 2} value={form[key as keyof typeof form]}
    onChange={(event) => setForm({ ...form, [key]: event.target.value })} />)}<ErrorState error={error} />
    {saved && <Alert severity="success">Settings saved.</Alert>}<Button variant="contained" startIcon={<SaveIcon />}
      disabled={!form.workingHours || !form.timezone || saving} onClick={() => onSave(form)}>Save settings</Button></Stack>;
}