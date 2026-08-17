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
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { useClientsQuery, useDeactivateClientMutation } from '@/api/clientsApi';
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
  const [selectedClient, setSelectedClient] = useState<ClientResponse | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<ClientResponse | null>(null);
  const [assignTarget, setAssignTarget] = useState<ClientResponse | null>(null);
  const [assignCoachId, setAssignCoachId] = useState<number | ''>('');
  const [assignDieticianId, setAssignDieticianId] = useState<number | ''>('');
  const [assignError, setAssignError] = useState<string | null>(null);

  const { data, isLoading } = useClientsQuery(keyword, page, pageSize);
  const { data: coaches } = useCoachesQuery('', 0, 100);
  const { data: dieticians } = useDieticiansQuery('', 0, 100);
  const deactivateMutation = useDeactivateClientMutation();
  const assignCoachMutation = useAssignCoachMutation();
  const assignDieticianMutation = useAssignDieticianMutation();

  const columns: GridColDef<ClientResponse>[] = [
    { field: 'firstName', headerName: 'First Name', flex: 1 },
    { field: 'lastName', headerName: 'Last Name', flex: 1 },
    { field: 'email', headerName: 'Email', flex: 1.3 },
    { field: 'contactNumber', headerName: 'Contact', flex: 1 },
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
      renderCell: (params) => (
        <Chip size="small" label={params.value ? 'Active' : 'Inactive'} color={params.value ? 'success' : 'default'} />
      ),
    },
    {
      field: 'actions', headerName: 'Actions', width: 160, sortable: false,
      renderCell: (params) => (
        <Box>
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => { setSelectedClient(params.row); setFormOpen(true); }}>
              <EditIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Assign Coach/Dietician">
            <IconButton size="small" onClick={() => {
              setAssignTarget(params.row);
              setAssignCoachId(params.row.assignedCoachId ?? '');
              setAssignDieticianId(params.row.assignedDieticianId ?? '');
            }}>
              <GroupAddIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Deactivate">
            <IconButton size="small" onClick={() => setDeactivateTarget(params.row)} disabled={!params.row.active}>
              <BlockIcon fontSize="small" />
            </IconButton>
          </Tooltip>
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
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setSelectedClient(null); setFormOpen(true); }}>
            Register Client
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

      <ClientFormDialog open={formOpen} onClose={() => setFormOpen(false)} client={selectedClient} />

      <ConfirmDialog
        open={!!deactivateTarget}
        title="Deactivate Client"
        message={`Deactivate ${deactivateTarget?.firstName} ${deactivateTarget?.lastName}? They will no longer be able to log in.`}
        onClose={() => setDeactivateTarget(null)}
        confirmColor="error"
        onConfirm={() => deactivateTarget && deactivateMutation.mutate(deactivateTarget.id)}
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
