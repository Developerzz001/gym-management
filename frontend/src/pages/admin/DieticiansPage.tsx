import { useState } from 'react';
import { Box, Paper, TextField, Button, Chip, IconButton, Tooltip } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { useActivateDieticianMutation, useDeactivateDieticianMutation, useDieticiansQuery, useDeleteDieticianMutation } from '@/api/dieticiansApi';
import { DieticianFormDialog } from './DieticianFormDialog';
import type { DieticianResponse } from '@/types';

export function DieticiansPage() {
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [formOpen, setFormOpen] = useState(false);
  const [selected, setSelected] = useState<DieticianResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<DieticianResponse | null>(null);
  const [activateTarget, setActivateTarget] = useState<DieticianResponse | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<DieticianResponse | null>(null);

  const { data, isLoading } = useDieticiansQuery(keyword, page, pageSize);
  const deleteMutation = useDeleteDieticianMutation();
  const activateMutation = useActivateDieticianMutation();
  const deactivateMutation = useDeactivateDieticianMutation();

  const columns: GridColDef<DieticianResponse>[] = [
    { field: 'firstName', headerName: 'First Name', flex: 1 },
    { field: 'lastName', headerName: 'Last Name', flex: 1 },
    { field: 'email', headerName: 'Email', flex: 1.3 },
    { field: 'specialization', headerName: 'Specialization', flex: 1 },
    { field: 'experienceYears', headerName: 'Experience (yrs)', width: 140 },
    {
      field: 'active', headerName: 'Status', width: 110,
      valueGetter: (_value, row) => row.active ? 'Active' : 'Inactive',
      renderCell: (params) => <Chip size="small" label={params.row.active ? 'Active' : 'Inactive'} color={params.row.active ? 'success' : 'default'} />,
    },
    {
      field: 'actions', headerName: 'Actions', width: 145, sortable: false,
      renderCell: (params) => (
        <Box>
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => { setSelected(params.row); setFormOpen(true); }}>
              <EditIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Delete">
            <IconButton size="small" onClick={() => setDeleteTarget(params.row)}>
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          {params.row.active ? (
            <Tooltip title="Deactivate">
              <IconButton size="small" onClick={() => setDeactivateTarget(params.row)}>
                <BlockIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          ) : (
            <Tooltip title="Activate">
              <IconButton size="small" onClick={() => setActivateTarget(params.row)}>
                <CheckCircleIcon fontSize="small" color="success" />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <PageHeader
        title="Dieticians"
        subtitle="Manage dieticians"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setSelected(null); setFormOpen(true); }}>Add Dietician</Button>}
      />
      <Paper sx={{ p: 2, mb: 2 }}>
        <TextField size="small" placeholder="Search by name or email" value={keyword}
          onChange={(e) => { setKeyword(e.target.value); setPage(0); }} sx={{ width: 320 }} />
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
      <DieticianFormDialog open={formOpen} onClose={() => setFormOpen(false)} dietician={selected} />
      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Dietician"
        message={`Delete ${deleteTarget?.firstName} ${deleteTarget?.lastName}? This will remove their login access.`}
        onClose={() => setDeleteTarget(null)}
        confirmColor="error"
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
      />
      <ConfirmDialog
        open={!!deactivateTarget}
        title="Deactivate Dietician"
        message={`Deactivate ${deactivateTarget?.firstName} ${deactivateTarget?.lastName}? They will no longer be able to log in.`}
        onClose={() => setDeactivateTarget(null)}
        confirmColor="error"
        onConfirm={() => deactivateTarget && deactivateMutation.mutate(deactivateTarget.id)}
      />
      <ConfirmDialog
        open={!!activateTarget}
        title="Activate Dietician"
        message={`Activate ${activateTarget?.firstName} ${activateTarget?.lastName}? They will be able to log in again.`}
        onClose={() => setActivateTarget(null)}
        confirmColor="primary"
        onConfirm={() => activateTarget && activateMutation.mutate(activateTarget.id)}
      />
    </Box>
  );
}
