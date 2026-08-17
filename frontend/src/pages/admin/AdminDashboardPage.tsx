import { Paper, Box, Typography } from '@mui/material';
import Grid from '@mui/material/Grid2';
import PeopleIcon from '@mui/icons-material/People';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import RestaurantIcon from '@mui/icons-material/Restaurant';
import CardMembershipIcon from '@mui/icons-material/CardMembership';
import { PageHeader } from '@/components/common/PageHeader';
import { useClientsQuery } from '@/api/clientsApi';
import { useCoachesQuery } from '@/api/coachesApi';
import { useDieticiansQuery } from '@/api/dieticiansApi';
import { useMembershipPlansQuery } from '@/api/membershipsApi';

function StatCard({ label, value, icon, color }: { label: string; value: number | string; icon: React.ReactNode; color: string }) {
  return (
    <Paper sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
      <Box
        sx={{
          width: 56,
          height: 56,
          borderRadius: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: `${color}.light`,
          color: `${color}.dark`,
        }}
      >
        {icon}
      </Box>
      <Box>
        <Typography variant="h5" fontWeight={700}>
          {value}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {label}
        </Typography>
      </Box>
    </Paper>
  );
}

export function AdminDashboardPage() {
  const { data: clients } = useClientsQuery('', 0, 1);
  const { data: coaches } = useCoachesQuery('', 0, 1);
  const { data: dieticians } = useDieticiansQuery('', 0, 1);
  const { data: plans } = useMembershipPlansQuery();

  return (
    <Box>
      <PageHeader title="Admin Dashboard" subtitle="Overview of the gym management system" />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="Total Clients" value={clients?.totalElements ?? 0} icon={<PeopleIcon />} color="primary" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="Fitness Coaches" value={coaches?.totalElements ?? 0} icon={<FitnessCenterIcon />} color="success" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="Dieticians" value={dieticians?.totalElements ?? 0} icon={<RestaurantIcon />} color="warning" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="Membership Plans" value={plans?.length ?? 0} icon={<CardMembershipIcon />} color="info" />
        </Grid>
      </Grid>
    </Box>
  );
}
