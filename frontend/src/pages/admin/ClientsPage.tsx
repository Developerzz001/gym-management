import { useState } from 'react';
import {
  Box,
  Paper,
  TextField,
  Button,
  Chip,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  MenuItem,
  Alert,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import GroupAddIcon from '@mui/icons-material/GroupAdd';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import ContactPageIcon from '@mui/icons-material/ContactPage';
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { useActivateClientMutation, useClientsQuery, useDeactivateClientMutation } from '@/api/clientsApi';
import { useCoachesQuery } from '@/api/coachesApi';
import { useDieticiansQuery } from '@/api/dieticiansApi';
import { useAssignCoachMutation, useAssignDieticianMutation } from '@/api/assignmentsApi';
import { extractErrorMessage } from '@/api/axiosClient';
import { useAppSelector } from '@/app/hooks';
import { ClientFormDialog } from './ClientFormDialog';
import type { ClientResponse, RegistrationType } from '@/types';

function ClientManagementPage({ registrationType }: { registrationType: RegistrationType }) {
  const isInquiryPage = registrationType === 'INQUIRY';
  const role = useAppSelector((state) => state.auth.role);
  const canManageAssignments = role === 'ADMIN';
  const canConvertInquiries = role === 'ADMIN' || role === 'BRANCH_MANAGER' || role === 'RECEPTIONIST';
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [formOpen, setFormOpen] = useState(false);
  const [selectedClient, setSelectedClient] = useState<ClientResponse | null>(null);
  const [conversionTarget, setConversionTarget] = useState<ClientResponse | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<ClientResponse | null>(null);
  const [activateTarget, setActivateTarget] = useState<ClientResponse | null>(null);
  const [assignTarget, setAssignTarget] = useState<ClientResponse | null>(null);
  const [assignCoachId, setAssignCoachId] = useState<number | ''>('');
  const [assignDieticianId, setAssignDieticianId] = useState<number | ''>('');
  const [assignError, setAssignError] = useState<string | null>(null);

  const { data, isLoading } = useClientsQuery(keyword, page, pageSize, registrationType);
  const { data: coaches } = useCoachesQuery('', 0, 100, canManageAssignments);
  const { data: dieticians } = useDieticiansQuery('', 0, 100, canManageAssignments);
  const deactivateMutation = useDeactivateClientMutation();
  const activateMutation = useActivateClientMutation();
  const assignCoachMutation = useAssignCoachMutation();
  const assignDieticianMutation = useAssignDieticianMutation();

  const columns: GridColDef<ClientResponse>[] = [
    { field: 'firstName', headerName: 'First Name', width: 140 },
    { field: 'lastName', headerName: 'Last Name', width: 140 },
    { field: 'email', headerName: 'Email', width: 240 },
    { field: 'contactNumber', headerName: 'Contact', width: 150 },
    {
      field: 'membershipAssigned', headerName: 'Membership', width: 145,
      renderCell: (params) => (
        <Chip
          size="small"
          label={params.row.membershipAssigned ? 'Assigned' : 'Not Assigned'}
          color={params.row.membershipAssigned ? 'primary' : 'default'}
          variant="outlined"
        />
      ),
    },
    {
      field: 'membershipStartDate', headerName: 'Started', width: 125,
      valueGetter: (_value, row) => row.membershipStartDate ?? '-',
    },
    {
      field: 'membershipEndDate', headerName: 'Expires', width: 125,
      valueGetter: (_value, row) => row.membershipEndDate ?? '-',
    },
    {
      field: 'assignedCoachName', headerName: 'Coach', width: 170,
      renderCell: (params) => params.value || <Chip size="small" label="Unassigned" variant="outlined" />,
    },
    {
      field: 'assignedDieticianName', headerName: 'Dietician', width: 170,
      renderCell: (params) => params.value || <Chip size="small" label="Unassigned" variant="outlined" />,
    },
    {
      field: 'active', headerName: 'Status', width: 110,
      valueGetter: (_value, row) => row.membershipActive ? 'Active' : 'Inactive',
      renderCell: (params) => (
        <Chip size="small" label={params.row.membershipActive ? 'Active' : 'Inactive'} color={params.row.membershipActive ? 'success' : 'default'} />
      ),
    },
    {
      field: 'registrationType', headerName: 'Type', width: 120,
      valueGetter: (_value, row) => row.registrationType === 'INQUIRY' ? 'Inquiry' : 'Client',
      renderCell: (params) => (
        <Chip size="small" label={params.row.registrationType === 'INQUIRY' ? 'Inquiry' : 'Client'}
          color={params.row.registrationType === 'INQUIRY' ? 'warning' : 'success'} variant="outlined" />
      ),
    },
    {
      field: 'actions', headerName: 'Actions', width: 160, sortable: false,
      renderCell: (params) => (
        <Box>
          {params.row.registrationType !== 'INQUIRY' && (
            <Tooltip title="Edit">
              <IconButton
                size="small"
                disabled={!params.row.active}
                onClick={() => { setSelectedClient(params.row); setFormOpen(true); }}
              >
                <EditIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          {canConvertInquiries && params.row.registrationType === 'INQUIRY' && (
            <Button
              size="small"
              variant="contained"
              startIcon={<PersonAddIcon />}
              onClick={() => setConversionTarget(params.row)}
            >
              Convert to Client
            </Button>
          )}
          {canManageAssignments && params.row.registrationType !== 'INQUIRY' && (
            <Tooltip title="Assign Coach/Dietician">
              <IconButton size="small" disabled={!params.row.active} onClick={() => {
                setAssignTarget(params.row);
                setAssignCoachId(params.row.assignedCoachId ?? '');
                setAssignDieticianId(params.row.assignedDieticianId ?? '');
              }}>
                <GroupAddIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          {!canManageAssignments || params.row.registrationType === 'INQUIRY' ? null : params.row.active ? (
            <Tooltip title="Deactivate">
              <IconButton size="small" onClick={() => setDeactivateTarget(params.row)}>
                <BlockIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          ) : (
            <Tooltip title="Activate client">
              <IconButton size="small" onClick={() => setActivateTarget(params.row)}>
                <CheckCircleIcon fontSize="small" color="success" />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      ),
    },
  ];

  const handleAssignSave = async () => {
    if (!assignTarget) return;
    setAssignError(null);
    try {
      if (assignCoachId) {
        await assignCoachMutation.mutateAsync({ clientId: assignTarget.id, coachId: Number(assignCoachId) });
      }
      if (assignDieticianId) {
        await assignDieticianMutation.mutateAsync({ clientId: assignTarget.id, dieticianId: Number(assignDieticianId) });
      }
      setAssignTarget(null);
    } catch (error) {
      setAssignError(extractErrorMessage(error));
    }
  };

  return (
    <Box>
      <PageHeader
        title={isInquiryPage ? 'Inquiries' : 'Clients'}
        subtitle={isInquiryPage ? 'Register and convert prospective client inquiries' : 'Register and manage gym clients'}
        action={
          <Button
            variant="contained"
            startIcon={isInquiryPage ? <ContactPageIcon /> : <AddIcon />}
            onClick={() => { setSelectedClient(null); setFormOpen(true); }}
          >
            {isInquiryPage ? 'Register Inquiry' : 'Register Client'}
          </Button>
        }
      />

      <Paper sx={{ p: 2, mb: 2 }}>
        <TextField
          size="small"
          placeholder="Search by name or email"
          value={keyword}
          onChange={(e) => { setKeyword(e.target.value); setPage(0); }}
          sx={{ width: 320 }}
        />
      </Paper>

      <Paper sx={{ height: 560 }}>
        <DataGrid
          rows={data?.content ?? []}
          columns={columns}
          loading={isLoading}
          paginationMode="server"
          rowCount={data?.totalElements ?? 0}
          paginationModel={{ page, pageSize }}
          onPaginationModelChange={(model) => { setPage(model.page); setPageSize(model.pageSize); }}
          pageSizeOptions={[5, 10, 25, 50]}
          disableRowSelectionOnClick
        />
      </Paper>

      <ClientFormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        client={selectedClient}
        inquiry={isInquiryPage}
      />
      <ClientFormDialog
        open={!!conversionTarget}
        onClose={() => setConversionTarget(null)}
        client={conversionTarget}
        conversion
      />

      <ConfirmDialog
        open={!!deactivateTarget}
        title="Deactivate Client"
        message={`Deactivate ${deactivateTarget?.firstName} ${deactivateTarget?.lastName}? They will no longer be able to log in.`}
        onClose={() => setDeactivateTarget(null)}
        confirmColor="error"
        onConfirm={() => deactivateTarget && deactivateMutation.mutate(deactivateTarget.id)}
      />

      <ConfirmDialog
        open={!!activateTarget}
        title="Activate Client"
        message={`Activate ${activateTarget?.firstName} ${activateTarget?.lastName}? They will be able to log in again.`}
        onClose={() => setActivateTarget(null)}
        confirmColor="primary"
        onConfirm={() => activateTarget && activateMutation.mutate(activateTarget.id)}
      />

      <Dialog open={!!assignTarget} onClose={() => setAssignTarget(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Assign Coach / Dietician</DialogTitle>
        <DialogContent>
          {assignError && <Alert severity="error" sx={{ mb: 2 }}>{assignError}</Alert>}
          <TextField
            select fullWidth margin="normal" label="Fitness Coach"
            value={assignCoachId} onChange={(e) => setAssignCoachId(e.target.value ? Number(e.target.value) : '')}
          >
            <MenuItem value="">-- None --</MenuItem>
            {coaches?.content.map((c) => (
              <MenuItem key={c.id} value={c.id}>{c.firstName} {c.lastName}</MenuItem>
            ))}
          </TextField>
          <TextField
            select fullWidth margin="normal" label="Dietician"
            value={assignDieticianId} onChange={(e) => setAssignDieticianId(e.target.value ? Number(e.target.value) : '')}
          >
            <MenuItem value="">-- None --</MenuItem>
            {dieticians?.content.map((d) => (
              <MenuItem key={d.id} value={d.id}>{d.firstName} {d.lastName}</MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setAssignTarget(null)}>Cancel</Button>
          <Button variant="contained" onClick={handleAssignSave}>Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export function InquiriesPage() {
  return <ClientManagementPage registrationType="INQUIRY" />;
}

export function ClientsPage() {
  return <ClientManagementPage registrationType="REGISTERED" />;
}
