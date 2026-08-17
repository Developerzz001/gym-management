import { Box, Paper } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import { PageHeader } from '@/components/common/PageHeader';
import { useMyClientProfileQuery } from '@/api/clientsApi';
import { useProgressByClientQuery } from '@/api/progressApi';
import type { ProgressRecordResponse } from '@/types';

export function MyProgressPage() {
  const { data: profile } = useMyClientProfileQuery();
  const { data: progress, isLoading } = useProgressByClientQuery(profile?.id);

  const columns: GridColDef<ProgressRecordResponse>[] = [
    { field: 'recordDate', headerName: 'Date', flex: 1 },
    { field: 'weightKg', headerName: 'Weight (kg)', flex: 1 },
    { field: 'bmi', headerName: 'BMI', flex: 1 },
    { field: 'chestCm', headerName: 'Chest (cm)', flex: 1 },
    { field: 'waistCm', headerName: 'Waist (cm)', flex: 1 },
    { field: 'armsCm', headerName: 'Arms (cm)', flex: 1 },
    { field: 'shoulderCm', headerName: 'Shoulder (cm)', flex: 1 },
    { field: 'thighCm', headerName: 'Thigh (cm)', flex: 1 },
  ];

  return (
    <Box>
      <PageHeader title="My Progress" subtitle="Date-wise body metrics tracked by your fitness coach" />
      <Paper sx={{ height: 560 }}>
        <DataGrid
          rows={progress ?? []}
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
