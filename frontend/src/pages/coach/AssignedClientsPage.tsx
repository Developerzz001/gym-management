import { Box, Paper, Chip } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import { PageHeader } from '@/components/common/PageHeader';
import { useAssignedClientsQuery } from '@/api/clientsApi';
import type { ClientResponse } from '@/types';

export function AssignedClientsPage() {
  const { data: clients, isLoading } = useAssignedClientsQuery('coach');

  const columns: GridColDef<ClientResponse>[] = [
    { field: 'firstName', headerName: 'First Name', flex: 1 },
    { field: 'lastName', headerName: 'Last Name', flex: 1 },
    { field: 'email', headerName: 'Email', flex: 1.3 },
    { field: 'contactNumber', headerName: 'Contact', flex: 1 },
    { field: 'fitnessGoal', headerName: 'Fitness Goal', flex: 1 },
    {
      field: 'medical', headerName: 'Medical Flags', flex: 1.2, sortable: false,
      renderCell: (params) => (
        <Box display="flex" gap={0.5}>
          {params.row.diabetes && <Chip size="small" label="Diabetes" color="warning" />}
          {params.row.hypertension && <Chip size="small" label="Hypertension" color="warning" />}
          {params.row.asthma && <Chip size="small" label="Asthma" color="warning" />}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <PageHeader title="Assigned Clients" subtitle="Clients currently assigned to you" />
      <Paper sx={{ height: 560 }}>
        <DataGrid
          rows={clients ?? []}
          columns={columns}
          loading={isLoading}
          disableRowSelectionOnClick
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        />
      </Paper>
    </Box>
  );
}
