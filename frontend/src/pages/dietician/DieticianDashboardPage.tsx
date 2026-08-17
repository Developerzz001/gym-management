import { Box, Paper, Typography } from '@mui/material';
import Grid from '@mui/material/Grid2';
import PeopleIcon from '@mui/icons-material/People';
import RestaurantIcon from '@mui/icons-material/Restaurant';
import { PageHeader } from '@/components/common/PageHeader';
import { useAssignedClientsQuery } from '@/api/clientsApi';
import { useMyDietPlansQuery } from '@/api/dietPlansApi';

export function DieticianDashboardPage() {
  const { data: clients } = useAssignedClientsQuery('dietician');
  const { data: plans } = useMyDietPlansQuery();

  return (
    <Box>
      <PageHeader title="Dietician Dashboard" subtitle="Your assigned clients and diet plans" />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Paper sx={{ p: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
            <PeopleIcon color="primary" fontSize="large" />
            <Box><Typography variant="h5">{clients?.length ?? 0}</Typography><Typography variant="body2" color="text.secondary">Assigned Clients</Typography></Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Paper sx={{ p: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
            <RestaurantIcon color="success" fontSize="large" />
            <Box><Typography variant="h5">{plans?.length ?? 0}</Typography><Typography variant="body2" color="text.secondary">Diet Plans Created</Typography></Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
