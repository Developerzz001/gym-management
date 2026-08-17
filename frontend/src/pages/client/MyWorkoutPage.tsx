import { Box, Paper, Typography, Card, CardContent, Chip, Stack } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { PageHeader } from '@/components/common/PageHeader';
import { useMyClientProfileQuery } from '@/api/clientsApi';
import { useWorkoutPlansByClientQuery } from '@/api/workoutPlansApi';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

export function MyWorkoutPage() {
  const { data: profile } = useMyClientProfileQuery();
  const { data: plans, isLoading } = useWorkoutPlansByClientQuery(profile?.id);

  return (
    <Box>
      <PageHeader title="My Workout" subtitle="Your weekly workout plans assigned by your coach" />
      {!isLoading && !plans?.length && (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">No workout plans assigned yet.</Typography>
        </Paper>
      )}
      <Grid container spacing={2}>
        {plans?.map((plan) => (
          <Grid size={12} key={plan.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{plan.title}</Typography>
                <Typography variant="body2" color="text.secondary" mb={2}>Coach: {plan.coachName}</Typography>
                {DAYS.map((day) => {
                  const dayExercises = plan.details.filter((d) => d.dayOfWeek === day);
                  if (dayExercises.length === 0) return null;
                  return (
                    <Box key={day} mb={1.5}>
                      <Typography variant="subtitle2" color="primary">{day}</Typography>
                      <Stack direction="row" flexWrap="wrap" gap={1} mt={0.5}>
                        {dayExercises.map((d) => (
                          <Chip
                            key={d.id}
                            label={`${d.exerciseName} - ${d.sets ?? '-'}x${d.reps ?? '-'}`}
                            variant="outlined"
                          />
                        ))}
                      </Stack>
                    </Box>
                  );
                })}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
