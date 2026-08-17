import { useMemo, useState } from 'react';
import { Box, Paper, TextField, MenuItem, Button, IconButton, Tooltip } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { useExercisesQuery, useDeleteExerciseMutation } from '@/api/exercisesApi';
import { ExerciseFormDialog } from './ExerciseFormDialog';
import type { ExerciseCategory, ExerciseResponse } from '@/types';

const CATEGORIES: Array<ExerciseCategory | 'ALL'> = ['ALL', 'CHEST', 'BACK', 'SHOULDER', 'ARMS', 'LEGS', 'CORE', 'CARDIO'];

export function ExercisesPage() {
  const [category, setCategory] = useState<ExerciseCategory | 'ALL'>('ALL');
  const [keyword, setKeyword] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [selected, setSelected] = useState<ExerciseResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ExerciseResponse | null>(null);

  const { data: exercises, isLoading } = useExercisesQuery(category === 'ALL' ? undefined : category, keyword || undefined);
  const deleteMutation = useDeleteExerciseMutation();

  const rows = useMemo(() => exercises ?? [], [exercises]);

  const columns: GridColDef<ExerciseResponse>[] = [
    { field: 'name', headerName: 'Exercise', flex: 1.1, minWidth: 180 },
    { field: 'category', headerName: 'Category', width: 160 },
    { field: 'description', headerName: 'Description', flex: 1.6, minWidth: 260 },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 120,
      sortable: false,
      filterable: false,
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
        title="Exercise Master"
        subtitle="Create and manage exercises used in workout plans"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setSelected(null); setFormOpen(true); }}>Add Exercise</Button>}
      />

      <Paper sx={{ p: 2, mb: 2, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <TextField
          size="small"
          select
          label="Category"
          value={category}
          onChange={(e) => setCategory(e.target.value as ExerciseCategory | 'ALL')}
          sx={{ width: 220 }}
        >
          {CATEGORIES.map((value) => (
            <MenuItem key={value} value={value}>
              {value}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          size="small"
          label="Search Exercise"
          placeholder="Type exercise name"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          sx={{ width: 320 }}
        />
      </Paper>

      <Paper sx={{ height: 560 }}>
        <DataGrid
          rows={rows}
          columns={columns}
          loading={isLoading}
          disableRowSelectionOnClick
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10, page: 0 } } }}
        />
      </Paper>

      <ExerciseFormDialog open={formOpen} onClose={() => setFormOpen(false)} exercise={selected} />

      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Exercise"
        message={`Delete exercise '${deleteTarget?.name}'?`}
        onClose={() => setDeleteTarget(null)}
        confirmColor="error"
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
      />
    </Box>
  );
}
