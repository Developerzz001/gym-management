import { useState } from 'react';
import { Box, Paper, TextField, Button, Chip, IconButton, Tooltip } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { useCoachesQuery, useDeleteCoachMutation } from '@/api/coachesApi';
import { CoachFormDialog } from './CoachFormDialog';
import type { CoachResponse } from '@/types';

export function CoachesPage() {
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [formOpen, setFormOpen] = useState(false);
  const [selected, setSelected] = useState<CoachResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<CoachResponse | null>(null);

  const { data, isLoading } = useCoachesQuery(keyword, page, pageSize);
  const deleteMutation = useDeleteCoachMutation();

  const columns: GridColDef<CoachResponse>[] = [
    { field: 'firstName', headerName: 'First Name', flex: 1 },
    { field: 'lastName', headerName: 'Last Name', flex: 1 },
    { field: 'email', headerName: 'Email', flex: 1.3 },
    { field: 'specialization', headerName: 'Specialization', flex: 1 },
    { field: 'experienceYears', headerName: 'Experience (yrs)', width: 140 },
    {
      field: 'active', headerName: 'Status', width: 110,
      renderCell: (params) => <Chip size="small" label={params.value ? 'Active' : 'Inactive'} color={params.value ? 'success' : 'default'} />,
    },
    {
      field: 'actions', headerName: 'Actions', width: 110, sortable: false,
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
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <PageHeader
        title="Fitness Coaches"
        subtitle="Manage fitness coaches"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setSelected(null); setFormOpen(true); }}>Add Coach</Button>}
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
      <CoachFormDialog open={formOpen} onClose={() => setFormOpen(false)} coach={selected} />
      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Coach"
        message={`Delete ${deleteTarget?.firstName} ${deleteTarget?.lastName}? This will remove their login access.`}
        onClose={() => setDeleteTarget(null)}
        confirmColor="error"
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
      />
    </Box>
  );
}
