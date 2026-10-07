import { useEffect, useState } from 'react';
import { Alert, Box, Button, CircularProgress, Dialog, DialogContent, FormControl, IconButton, InputLabel, Menu, MenuItem, Paper, Select, Stack, TextField, Tooltip, Typography } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import CakeOutlinedIcon from '@mui/icons-material/CakeOutlined';
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined';
import EventRepeatOutlinedIcon from '@mui/icons-material/EventRepeatOutlined';
import AutorenewOutlinedIcon from '@mui/icons-material/AutorenewOutlined';
import ScheduleOutlinedIcon from '@mui/icons-material/ScheduleOutlined';
import TaskAltOutlinedIcon from '@mui/icons-material/TaskAltOutlined';
import PaymentsOutlinedIcon from '@mui/icons-material/PaymentsOutlined';
import EventAvailableOutlinedIcon from '@mui/icons-material/EventAvailableOutlined';
import SearchIcon from '@mui/icons-material/Search';
import RefreshIcon from '@mui/icons-material/Refresh';
import DownloadIcon from '@mui/icons-material/Download';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import { extractErrorMessage } from '@/api/axiosClient';
import { downloadDashboardRows, useDashboardDetailsQuery, useOperationalDashboardSummaryQuery } from '@/api/dashboardApi';
import { useClientQuery } from '@/api/clientsApi';
import { useAppSelector } from '@/app/hooks';
import { useBranchesQuery, useOrganizationsQuery } from '@/api/multiBranchApi';
import { useOutletContext } from 'react-router-dom';
import type { DashboardCategory, DashboardRecordResponse, OperationalDashboardSummaryResponse } from '@/types';
import { InquiryFollowUpDialog } from './InquiryFollowUpDialog';

const money = (value = 0) => new Intl.NumberFormat('en-IN', {
  style: 'currency', currency: 'INR', maximumFractionDigits: 0,
}).format(value);

const cards: Array<{ key: DashboardCategory; title: string; countKey: keyof Omit<OperationalDashboardSummaryResponse, 'collection'>;
  icon: typeof CakeOutlinedIcon; accent: string; tint: string }> = [
  { key: 'member-birthdays', title: 'Member Birthday', countKey: 'memberBirthdays', icon: CakeOutlinedIcon, accent: '#087e8b', tint: '#e2f4f3' },
  { key: 'staff-birthdays', title: 'Staff Birthday', countKey: 'staffBirthdays', icon: BadgeOutlinedIcon, accent: '#368a59', tint: '#e8f4e9' },
  { key: 'inquiry-followups', title: 'Inquiry Follow Up', countKey: 'inquiryFollowups', icon: EventRepeatOutlinedIcon, accent: '#b66a08', tint: '#fff2dc' },
  { key: 'renewal-followups', title: 'Renewal Follow Up', countKey: 'renewalFollowups', icon: AutorenewOutlinedIcon, accent: '#bd4e43', tint: '#fde9e5' },
  { key: 'membership-expiring', title: 'Membership Expiring', countKey: 'membershipExpiring', icon: ScheduleOutlinedIcon, accent: '#b66a08', tint: '#fff2dc' },
  { key: 'done-followups', title: 'Done Follow Up', countKey: 'doneFollowups', icon: TaskAltOutlinedIcon, accent: '#368a59', tint: '#e8f4e9' },
  { key: 'balance-payments', title: 'Balance Payment Members', countKey: 'balancePayments', icon: PaymentsOutlinedIcon, accent: '#1769aa', tint: '#e5f0fa' },
  { key: 'appointments', title: "Today's Appointment", countKey: 'appointments', icon: EventAvailableOutlinedIcon, accent: '#bd4e43', tint: '#fde9e5' },
];

const labels: Record<DashboardCategory, string[]> = {
  'member-birthdays': ['Name', 'Mobile Number', 'Sports / Activity', 'Action'],
  'staff-birthdays': ['Staff Name', 'Mobile Number', 'Action'],
  'inquiry-followups': ['Inquiry Name', 'Mobile Number', 'Sports / Activity', 'Last Follow Up Date', 'Last Comment'],
  'renewal-followups': ['Member Name', 'Mobile Number', 'Sports / Activity', 'Membership Plan', 'Expired Date', 'Action'],
  'membership-expiring': ['Member Name', 'Mobile Number', 'Sports / Activity', 'Membership Plan', 'Expiring On', 'Remaining Days', 'Action'],
  'done-followups': ['Name', 'Mobile Number', 'Follow Up Type', 'Next Follow Up Date', 'Comment', 'Done By'],
  'balance-payments': ['Member Name', 'Mobile Number', 'Sports / Activity', 'Membership Plan', 'Pending Amount', 'Due Date', 'Action'],
  appointments: ['Name', 'Mobile Number', 'Sports / Activity', 'Appointment Time', 'Action'],
};

const dataFields: Record<DashboardCategory, Array<keyof DashboardRecordResponse | 'action'>> = {
  'member-birthdays': ['name', 'mobileNumber', 'sportActivity', 'action'],
  'staff-birthdays': ['name', 'mobileNumber', 'action'],
  'inquiry-followups': ['name', 'mobileNumber', 'sportActivity', 'date', 'comment'],
  'renewal-followups': ['name', 'mobileNumber', 'sportActivity', 'membershipPlan', 'date', 'action'],
  'membership-expiring': ['name', 'mobileNumber', 'sportActivity', 'membershipPlan', 'date', 'remainingDays', 'action'],
  'done-followups': ['name', 'mobileNumber', 'followUpType', 'nextFollowUpDate', 'comment', 'doneBy'],
  'balance-payments': ['name', 'mobileNumber', 'sportActivity', 'membershipPlan', 'pendingAmount', 'dueDate', 'action'],
  appointments: ['name', 'mobileNumber', 'sportActivity', 'appointmentTime', 'action'],
};

const formatDate = (value?: string) => value ? new Date(`${value.slice(0, 10)}T00:00:00`).toLocaleDateString('en-IN') : '-';
const formatDateTime = (value?: string) => value ? new Date(value).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }) : '-';
const displayValue = (value: unknown) => value === null || value === undefined || value === '' ? '-' : String(value);

function csvCell(value: unknown) {
  return `"${String(value ?? '').replaceAll('"', '""')}"`;
}

function downloadFile(body: string, mime: string, filename: string) {
  const url = URL.createObjectURL(new Blob([body], { type: mime }));
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}

function escapeHtml(value: unknown) {
  return String(value ?? '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;').replaceAll("'", '&#39;');
}

function exportRows(category: DashboardCategory, rows: DashboardRecordResponse[], format: 'csv' | 'excel' | 'pdf', pdfWindow?: Window | null) {
  const headings = labels[category].filter((label) => label !== 'Action');
  const fields = dataFields[category].filter((field) => field !== 'action');
  const valueFor = (row: DashboardRecordResponse, field: keyof DashboardRecordResponse) => {
    if (field === 'date') return formatDate(row.date);
    if (field === 'nextFollowUpDate') return formatDate(row.nextFollowUpDate);
    if (field === 'dueDate') return formatDate(row.dueDate);
    if (field === 'appointmentTime') return formatDateTime(row.appointmentTime);
    if (field === 'pendingAmount') return money(row.pendingAmount);
    if (field === 'remainingDays') return row.remainingDays == null ? '-' : `${row.remainingDays} days`;
    return displayValue(row[field]);
  };
  const tableRows = rows.map((row) => fields.map((field) => valueFor(row, field)));
  if (format === 'csv') {
    downloadFile([headings, ...tableRows].map((line) => line.map(csvCell).join(',')).join('\r\n'), 'text/csv;charset=utf-8', `dashboard-${category}.csv`);
  } else if (format === 'excel') {
    const html = `<table><thead><tr>${headings.map((item) => `<th>${escapeHtml(item)}</th>`).join('')}</tr></thead><tbody>${tableRows.map((line) => `<tr>${line.map((item) => `<td>${escapeHtml(item)}</td>`).join('')}</tr>`).join('')}</tbody></table>`;
    downloadFile(`\ufeff${html}`, 'application/vnd.ms-excel;charset=utf-8', `dashboard-${category}.xls`);
  } else {
    const printWindow = pdfWindow ?? window.open('', '_blank');
    if (!printWindow) return;
    const body = `<h1>${escapeHtml(cards.find((card) => card.key === category)?.title)} List</h1><table><thead><tr>${headings.map((item) => `<th>${escapeHtml(item)}</th>`).join('')}</tr></thead><tbody>${tableRows.map((line) => `<tr>${line.map((item) => `<td>${escapeHtml(item)}</td>`).join('')}</tr>`).join('')}</tbody></table>`;
      printWindow.document.write(`<html><head><title>Dashboard export</title><style>body{font:12px Arial,sans-serif;padding:24px}table{width:100%;border-collapse:collapse}th,td{border:1px solid #bbb;padding:7px;text-align:left}th{background:#eef3f5}@media print{body{padding:0}}</style></head><body>${body}<script>window.onload=()=>window.print()</script></body></html>`);
    printWindow.document.close();
  }
}

export function AdminDashboardPage() {
  const { setDashboardBranchName } = useOutletContext<{ setDashboardBranchName: (name: string) => void }>();
  const auth = useAppSelector((state) => state.auth);
  const canChooseOrganization = auth.role === 'SUPER_ADMIN';
  const canChooseBranch = canChooseOrganization || auth.role === 'ORGANIZATION_ADMIN' || auth.role === 'ADMIN';
  const [organizationId, setOrganizationId] = useState<number | ''>(auth.organizationId ?? '');
  const [branchChoiceId, setBranchChoiceId] = useState<number | ''>(auth.branchId ?? '');
  const organizations = useOrganizationsQuery('', 0, 100, canChooseOrganization);
  const effectiveBranchId = canChooseBranch ? (branchChoiceId || undefined) : (auth.branchId ?? undefined);
  const branchReady = !!effectiveBranchId;
  const branches = useBranchesQuery(organizationId || auth.organizationId || undefined, '', 0, 100,
    canChooseBranch ? (!!organizationId || auth.role === 'ADMIN') : !!effectiveBranchId);
  const [selected, setSelected] = useState<DashboardCategory>('member-birthdays');
  const [followUpClientId, setFollowUpClientId] = useState<number | null>(null);
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState('date');
  const [direction, setDirection] = useState<'asc' | 'desc'>('asc');
  const [exportAnchor, setExportAnchor] = useState<HTMLElement | null>(null);
  const [exportError, setExportError] = useState('');
  const [exporting, setExporting] = useState(false);
  const summary = useOperationalDashboardSummaryQuery(effectiveBranchId, branchReady);
  const details = useDashboardDetailsQuery(selected, keyword, page, pageSize, sort, direction, effectiveBranchId, branchReady);
  const followUpClient = useClientQuery(followUpClientId ?? undefined);
  const activeCard = cards.find((card) => card.key === selected)!;
  const selectedBranchName = branches.data?.content.find((branch) => branch.id === effectiveBranchId)?.branchName;

  useEffect(() => {
    setDashboardBranchName(selectedBranchName ?? '');
    return () => setDashboardBranchName('');
  }, [selectedBranchName, setDashboardBranchName]);

  const columns: GridColDef<DashboardRecordResponse>[] = labels[selected].map((header, index) => {
    const field = dataFields[selected][index];
    if (field === 'action') return {
      field: 'action', headerName: header, width: 105, sortable: false, filterable: false,
      renderCell: ({ row }) => {
        const digits = row.mobileNumber?.replace(/\D/g, '');
        const whatsappNumber = digits?.length === 10 ? `91${digits}` : digits;
        return <Tooltip title={digits ? 'Message on WhatsApp' : 'No mobile number available'}>
          <span><IconButton size="small" color="success" disabled={!digits} component="a"
            href={whatsappNumber ? `https://wa.me/${whatsappNumber}` : undefined} target="_blank" rel="noreferrer" aria-label="Message on WhatsApp">
            <WhatsAppIcon fontSize="small" />
          </IconButton></span>
        </Tooltip>;
      },
    };
    const sortable = ['name', 'mobileNumber', 'date', 'dueDate', 'pendingAmount', 'appointmentTime'].includes(field);
    return {
      field,
      headerName: header,
      minWidth: field === 'comment' ? 220 : 135,
      flex: field === 'name' || field === 'comment' ? 1.2 : 1,
      sortable,
      valueGetter: (_value, row) => {
        if (field === 'date') return formatDate(row.date);
        if (field === 'nextFollowUpDate' || field === 'dueDate') return formatDate(row[field]);
        if (field === 'appointmentTime') return formatDateTime(row.appointmentTime);
        if (field === 'pendingAmount') return money(row.pendingAmount);
        if (field === 'remainingDays') return row.remainingDays == null ? '-' : `${row.remainingDays} days`;
        return displayValue(row[field]);
      },
    };
  });

  const handleExport = async (format: 'csv' | 'excel' | 'pdf') => {
    setExportAnchor(null);
    setExportError('');
    setExporting(true);
    const printWindow = format === 'pdf' ? window.open('', '_blank') : null;
    if (format === 'pdf' && !printWindow) {
      setExportError('Allow pop-ups to export this list as PDF.');
      setExporting(false);
      return;
    }
    try {
      const rows = await downloadDashboardRows(selected, keyword, effectiveBranchId);
      exportRows(selected, rows, format, printWindow);
    } catch (error) {
      printWindow?.close();
      setExportError(extractErrorMessage(error));
    } finally {
      setExporting(false);
    }
  };

  return <Box>
    {(canChooseOrganization || canChooseBranch) && <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} sx={{ mb: 2 }}>
      {canChooseOrganization && <FormControl size="small" sx={{ minWidth: { xs: '100%', sm: 260 } }}>
        <InputLabel id="dashboard-organization-label">Organization</InputLabel>
        <Select labelId="dashboard-organization-label" label="Organization" value={organizationId}
          onChange={(event) => { setOrganizationId(Number(event.target.value) || ''); setBranchChoiceId(''); }}>
          <MenuItem value="">Select organization</MenuItem>
          {organizations.data?.content.map((organization) => <MenuItem key={organization.id} value={organization.id}>{organization.name}</MenuItem>)}
        </Select>
      </FormControl>}
      <FormControl size="small" sx={{ minWidth: { xs: '100%', sm: 260 } }} disabled={canChooseOrganization && !organizationId}>
        <InputLabel id="dashboard-branch-label">Branch</InputLabel>
        <Select labelId="dashboard-branch-label" label="Branch" value={branchChoiceId}
          onChange={(event) => { setBranchChoiceId(Number(event.target.value) || ''); setPage(0); setKeyword(''); }}>
          <MenuItem value="">Select branch</MenuItem>
          {branches.data?.content.map((branch) => <MenuItem key={branch.id} value={branch.id}>{branch.branchName}</MenuItem>)}
        </Select>
      </FormControl>
    </Stack>}
    {summary.error && <Alert severity="error" sx={{ mb: 2 }}>{extractErrorMessage(summary.error)}</Alert>}
    {!branchReady && <Alert severity="info" sx={{ mb: 2 }}>{canChooseOrganization
      ? 'Select an organization and branch to view branch operations.'
      : 'Select a branch to view branch operations.'}</Alert>}
    {branchReady && <>
    <Grid container spacing={1.5}>
      {cards.map((card) => {
        const Icon = card.icon;
        const active = selected === card.key;
        return <Grid key={card.key} size={{ xs: 6, sm: 6, md: 3 }}>
          <Paper sx={{ minHeight: 132, height: '100%', p: 1.8, borderRadius: 1, bgcolor: card.tint,
            border: active ? `2px solid ${card.accent}` : '1px solid transparent', transition: 'border-color 140ms ease, transform 140ms ease',
            '&:hover': { transform: 'translateY(-2px)' } }}>
            <Stack direction="row" justifyContent="space-between" alignItems="flex-start" gap={1}>
              <Box>
                <Typography variant="h4" sx={{ color: '#17262d', lineHeight: 1.1, mb: 0.8 }}>
                  {summary.data?.[card.countKey] ?? (summary.isLoading ? '—' : 0)}
                </Typography>
                <Typography variant="body2" fontWeight={600} sx={{ color: '#26383f' }}>{card.title}</Typography>
              </Box>
              <Icon sx={{ color: card.accent, fontSize: 30, flexShrink: 0 }} />
            </Stack>
            <Button size="small" onClick={() => { setSelected(card.key); setPage(0); setKeyword(''); }}
              sx={{ mt: 1, minHeight: 28, p: 0, color: card.accent, fontWeight: 700 }}>More Info</Button>
          </Paper>
        </Grid>;
      })}
    </Grid>
    <Grid container spacing={2} sx={{ mt: 0.5 }}>
      <Grid size={{ xs: 12, lg: 9 }}>
        <Paper sx={{ p: { xs: 1.5, sm: 2 }, borderRadius: 1 }}>
          <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'stretch', sm: 'center' }} gap={1.5} sx={{ mb: 1.5 }}>
            <Box><Typography variant="h6" fontWeight={700}>{activeCard.title} List</Typography>
              <Typography variant="caption" color="text.secondary">{details.data?.totalElements ?? 0} records</Typography></Box>
            <Stack direction="row" spacing={1} alignItems="center">
              <TextField size="small" placeholder="Search name or mobile" value={keyword}
                onChange={(event) => { setKeyword(event.target.value); setPage(0); }}
                InputProps={{ startAdornment: <SearchIcon fontSize="small" sx={{ mr: 1, color: 'text.secondary' }} /> }} />
              <Tooltip title="Refresh list"><span><IconButton onClick={() => details.refetch()} aria-label="Refresh list"><RefreshIcon /></IconButton></span></Tooltip>
              <Tooltip title="Export selected list"><span><Button variant="outlined" size="small" startIcon={<DownloadIcon />}
                disabled={exporting || !details.data?.totalElements} onClick={(event) => setExportAnchor(event.currentTarget)}>Export</Button></span></Tooltip>
              <Menu anchorEl={exportAnchor} open={!!exportAnchor} onClose={() => setExportAnchor(null)}>
                <MenuItem onClick={() => handleExport('excel')}>Export Excel</MenuItem>
                <MenuItem onClick={() => handleExport('csv')}>Export CSV</MenuItem>
                <MenuItem onClick={() => handleExport('pdf')}>Export PDF</MenuItem>
              </Menu>
            </Stack>
          </Stack>
          {exportError && <Alert severity="error" sx={{ mb: 1 }}>{exportError}</Alert>}
          {details.error && <Alert severity="error" sx={{ mb: 1 }}>{extractErrorMessage(details.error)}</Alert>}
          <Box sx={{ height: 470, minWidth: 0 }}>
            <DataGrid rows={details.data?.content ?? []} columns={columns} loading={details.isLoading || details.isFetching}
              rowCount={details.data?.totalElements ?? 0} paginationMode="server" sortingMode="server"
              paginationModel={{ page, pageSize }} onPaginationModelChange={(model) => { setPage(model.page); setPageSize(model.pageSize); }}
              sortModel={[{ field: sort, sort: direction }]} onSortModelChange={(model) => {
                const item = model[0];
                if (item?.sort) { setSort(item.field); setDirection(item.sort); }
              }} pageSizeOptions={[10, 20, 50]} disableRowSelectionOnClick
              onRowClick={({ row }) => {
                if (selected === 'inquiry-followups' && row.clientId) setFollowUpClientId(row.clientId);
              }}
              sx={selected === 'inquiry-followups' ? { '& .MuiDataGrid-row': { cursor: 'pointer' } } : undefined} />
          </Box>
        </Paper>
      </Grid>
      <Grid size={{ xs: 12, lg: 3 }}>
        <Paper sx={{ p: 2.2, borderRadius: 1, height: '100%', minHeight: 330, borderTop: '4px solid #087e8b' }}>
          <Typography variant="h6" fontWeight={700} sx={{ mb: 0.4 }}>Today&apos;s Collection</Typography>
          <Typography variant="caption" color="text.secondary">Payments recorded today</Typography>
          <Stack spacing={1.7} sx={{ mt: 2.4 }}>
            {[
              ['Cash', summary.data?.collection.cash], ['Card', summary.data?.collection.card],
              ['UPI', summary.data?.collection.upi], ['Bank Transfer', summary.data?.collection.bankTransfer],
              ['Cheque', summary.data?.collection.cheque], ['Other', summary.data?.collection.other],
            ].map(([label, value]) => <Stack key={String(label)} direction="row" justifyContent="space-between">
              <Typography variant="body2" color="text.secondary">{label}</Typography>
              <Typography variant="body2" fontWeight={600}>{summary.data ? money(Number(value ?? 0)) : '—'}</Typography>
            </Stack>)}
            <Box sx={{ borderTop: '1px solid', borderColor: 'divider', pt: 1.5, mt: 0.2 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="baseline">
                <Typography fontWeight={700}>Total</Typography>
                <Typography variant="h6" fontWeight={800} sx={{ color: '#087e8b' }}>
                  {summary.data ? money(summary.data.collection.total) : '—'}
                </Typography>
              </Stack>
            </Box>
          </Stack>
        </Paper>
      </Grid>
    </Grid>
    <Dialog open={!!followUpClientId && followUpClient.isLoading} onClose={() => setFollowUpClientId(null)}>
      <DialogContent sx={{ display: 'flex', justifyContent: 'center', p: 4 }}><CircularProgress size={28} /></DialogContent>
    </Dialog>
    {followUpClient.error && <Alert severity="error" sx={{ mt: 2 }} onClose={() => setFollowUpClientId(null)}>
      {extractErrorMessage(followUpClient.error)}
    </Alert>}
    <InquiryFollowUpDialog
      open={!!followUpClientId && !!followUpClient.data}
      onClose={() => setFollowUpClientId(null)}
      client={followUpClient.data ?? null}
    />
    </>}
  </Box>;
}