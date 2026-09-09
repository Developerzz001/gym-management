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
import { ClientFormDialog } from './ClientFormDialog';
import type { ClientResponse } from '@/types';

export function ClientsPage() {
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [formOpen, setFormOpen] = useState(false);
  const [inquiryFormOpen, setInquiryFormOpen] = useState(false);
  const [selectedClient, setSelectedClient] = useState<ClientResponse | null>(null);
  const [conversionTarget, setConversionTarget] = useState<ClientResponse | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<ClientResponse | null>(null);
  const [activateTarget, setActivateTarget] = useState<ClientResponse | null>(null);
  const [assignTarget, setAssignTarget] = useState<ClientResponse | null>(null);
  const [assignCoachId, setAssignCoachId] = useState<number | ''>('');
  const [assignDieticianId, setAssignDieticianId] = useState<number | ''>('');
  const [assignError, setAssignError] = useState<string | null>(null);

  const { data, isLoading } = useClientsQuery(keyword, page, pageSize);
  const { data: coaches } = useCoachesQuery('', 0, 100);
  const { data: dieticians } = useDieticiansQuery('', 0, 100);
  const deactivateMutation = useDeactivateClientMutation();
  const activateMutation = useActivateClientMutation();
  const assignCoachMutation = useAssignCoachMutation();
  const assignDieticianMutation = useAssignDieticianMutation();

  const columns: GridColDef<ClientResponse>[] = [
    { field: 'firstName', headerName: 'First Name', flex: 1 },
    { field: 'lastName', headerName: 'Last Name', flex: 1 },
    { field: 'email', headerName: 'Email', flex: 1.3 },
    { field: 'contactNumber', headerName: 'Contact', flex: 1 },
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
      field: 'assignedCoachName', headerName: 'Coach', flex: 1,
      renderCell: (params) => params.value || <Chip size="small" label="Unassigned" variant="outlined" />,
    },
    {
      field: 'assignedDieticianName', headerName: 'Dietician', flex: 1,
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
          {params.row.registrationType === 'INQUIRY' && (
            <Button
              size="small"
              variant="contained"
              startIcon={<PersonAddIcon />}
              onClick={() => setConversionTarget(params.row)}
            >
              Convert to Client
            </Button>
          )}
          {params.row.registrationType !== 'INQUIRY' && (
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
          {params.row.registrationType === 'INQUIRY' ? null : params.row.active ? (
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
        title="Clients"
        subtitle="Register and manage gym clients"
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setSelectedClient(null); setFormOpen(true); }}>
              Register Client
            </Button>
            <Button variant="outlined" startIcon={<ContactPageIcon />} onClick={() => setInquiryFormOpen(true)}>
              Register Inquiry
            </Button>
          </Box>
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

      <ClientFormDialog open={formOpen} onClose={() => setFormOpen(false)} client={selectedClient} />
      <ClientFormDialog open={inquiryFormOpen} onClose={() => setInquiryFormOpen(false)} inquiry />
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
