import { Box, Paper, Typography, Chip, List, ListItem, ListItemText, Divider } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { PageHeader } from '@/components/common/PageHeader';
import { useMyClientProfileQuery } from '@/api/clientsApi';
import { useTodaysWorkoutQuery } from '@/api/workoutPlansApi';
import { useDietPlansByClientQuery } from '@/api/dietPlansApi';
import { useSupplementsByClientQuery } from '@/api/supplementsApi';
import { useMedicinesByClientQuery } from '@/api/medicinesApi';
import { useUpcomingSessionsQuery } from '@/api/sessionsApi';
import { useProgressByClientQuery } from '@/api/progressApi';

export function ClientDashboardPage() {
  const { data: profile } = useMyClientProfileQuery();
  const clientId = profile?.id;
  const { data: todaysWorkout } = useTodaysWorkoutQuery(clientId);
  const { data: dietPlans } = useDietPlansByClientQuery(clientId);
  const { data: supplements } = useSupplementsByClientQuery(clientId);
  const { data: medicines } = useMedicinesByClientQuery(clientId);
  const { data: upcomingSessions } = useUpcomingSessionsQuery(clientId);
  const { data: progress } = useProgressByClientQuery(clientId);

  const todaysDiet = dietPlans?.[0];
  const latestProgress = progress?.[0];

  return (
    <Box>
      <PageHeader title={`Welcome, ${profile?.firstName ?? ''}`} subtitle="Here's your fitness overview for today" />

      <Grid container spacing={3} mb={3}>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="subtitle2" color="text.secondary">Assigned Coach</Typography>
            <Typography variant="h6">{profile?.assignedCoachName ?? 'Not assigned yet'}</Typography>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="subtitle2" color="text.secondary">Assigned Dietician</Typography>
            <Typography variant="h6">{profile?.assignedDieticianName ?? 'Not assigned yet'}</Typography>
          </Paper>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>Today's Workout</Typography>
            {todaysWorkout ? (
              <List dense>
                {todaysWorkout.details
                  .filter((d) => d.dayOfWeek === new Date().toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase())
                  .map((d) => (
                    <ListItem key={d.id}><ListItemText primary={d.exerciseName} secondary={`${d.sets ?? '-'} sets x ${d.reps ?? '-'} reps`} /></ListItem>
                  ))}
              </List>
            ) : (
              <Typography color="text.secondary">No workout scheduled for today.</Typography>
            )}
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>Diet Plan Overview</Typography>
            {todaysDiet ? (
              <List dense>
                {todaysDiet.details.map((d) => (
                  <ListItem key={d.id}><ListItemText primary={`${d.mealType.replace('_', ' ')}: ${d.foodItem}`} secondary={`${d.quantity ?? ''} ${d.calories ? `- ${d.calories} kcal` : ''}`} /></ListItem>
                ))}
              </List>
            ) : (
              <Typography color="text.secondary">No diet plan assigned yet.</Typography>
            )}
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>Supplements & Medicines</Typography>
            <Box display="flex" flexWrap="wrap" gap={1} mb={1}>
              {supplements?.map((s) => <Chip key={s.id} label={s.name} color="success" size="small" />)}
              {medicines?.map((m) => <Chip key={m.id} label={m.name} color="warning" size="small" />)}
              {!supplements?.length && !medicines?.length && <Typography color="text.secondary">None assigned yet.</Typography>}
            </Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>Upcoming Sessions</Typography>
            <List dense>
              {upcomingSessions?.map((s) => (
                <ListItem key={s.id} divider>
                  <ListItemText primary={new Date(s.sessionDateTime).toLocaleString()} secondary={s.notes} />
                </ListItem>
              ))}
              {!upcomingSessions?.length && <Typography color="text.secondary">No upcoming sessions.</Typography>}
            </List>
          </Paper>
        </Grid>
        <Grid size={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>Latest Progress</Typography>
            {latestProgress ? (
              <Box display="flex" gap={3} flexWrap="wrap">
                <Typography>Weight: <b>{latestProgress.weightKg ?? '-'} kg</b></Typography>
                <Divider orientation="vertical" flexItem />
                <Typography>BMI: <b>{latestProgress.bmi ?? '-'}</b></Typography>
                <Divider orientation="vertical" flexItem />
                <Typography>Chest: <b>{latestProgress.chestCm ?? '-'} cm</b></Typography>
                <Divider orientation="vertical" flexItem />
                <Typography>Waist: <b>{latestProgress.waistCm ?? '-'} cm</b></Typography>
              </Box>
            ) : (
              <Typography color="text.secondary">No progress records yet.</Typography>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
