import { Paper, Box, Typography, List, ListItem, ListItemText, Chip } from '@mui/material';
import Grid from '@mui/material/Grid2';
import PeopleIcon from '@mui/icons-material/People';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import EventNoteIcon from '@mui/icons-material/EventNote';
import { PageHeader } from '@/components/common/PageHeader';
import { useAssignedClientsQuery } from '@/api/clientsApi';
import { useMyWorkoutPlansQuery } from '@/api/workoutPlansApi';
import { useMySessionsQuery } from '@/api/sessionsApi';

export function CoachDashboardPage() {
  const { data: clients } = useAssignedClientsQuery('coach');
  const { data: plans } = useMyWorkoutPlansQuery();
  const { data: sessions } = useMySessionsQuery();

  const upcoming = sessions?.filter((s) => s.status === 'SCHEDULED').slice(0, 5) ?? [];

  return (
    <Box>
      <PageHeader title="Coach Dashboard" subtitle="Your assigned clients, plans and sessions" />
      <Grid container spacing={3} mb={3}>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Paper sx={{ p: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
            <PeopleIcon color="primary" fontSize="large" />
            <Box><Typography variant="h5">{clients?.length ?? 0}</Typography><Typography variant="body2" color="text.secondary">Assigned Clients</Typography></Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Paper sx={{ p: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
            <FitnessCenterIcon color="success" fontSize="large" />
            <Box><Typography variant="h5">{plans?.length ?? 0}</Typography><Typography variant="body2" color="text.secondary">Workout Plans</Typography></Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Paper sx={{ p: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
            <EventNoteIcon color="warning" fontSize="large" />
            <Box><Typography variant="h5">{upcoming.length}</Typography><Typography variant="body2" color="text.secondary">Upcoming Sessions</Typography></Box>
          </Paper>
        </Grid>
      </Grid>

      <Paper sx={{ p: 2 }}>
        <Typography variant="h6" mb={1}>Upcoming Sessions</Typography>
        <List>
          {upcoming.map((s) => (
            <ListItem key={s.id} divider>
              <ListItemText primary={s.clientName} secondary={new Date(s.sessionDateTime).toLocaleString()} />
              <Chip label={s.status} size="small" color="info" />
            </ListItem>
          ))}
          {upcoming.length === 0 && <Typography color="text.secondary">No upcoming sessions scheduled.</Typography>}
        </List>
      </Paper>
    </Box>
  );
}
