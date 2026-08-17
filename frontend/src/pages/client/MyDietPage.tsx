import { Box, Paper, Typography, Card, CardContent, List, ListItem, ListItemText, Chip } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { PageHeader } from '@/components/common/PageHeader';
import { useMyClientProfileQuery } from '@/api/clientsApi';
import { useDietPlansByClientQuery } from '@/api/dietPlansApi';
import { useSupplementsByClientQuery } from '@/api/supplementsApi';
import { useMedicinesByClientQuery } from '@/api/medicinesApi';

const MEAL_ORDER = ['BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'EVENING_SNACK', 'DINNER'];

export function MyDietPage() {
  const { data: profile } = useMyClientProfileQuery();
  const { data: plans, isLoading } = useDietPlansByClientQuery(profile?.id);
  const { data: supplements } = useSupplementsByClientQuery(profile?.id);
  const { data: medicines } = useMedicinesByClientQuery(profile?.id);

  return (
    <Box>
      <PageHeader title="My Diet" subtitle="Diet plan, supplements and medicines assigned by your dietician" />
      {!isLoading && !plans?.length && (
        <Paper sx={{ p: 4, textAlign: 'center', mb: 3 }}>
          <Typography color="text.secondary">No diet plans assigned yet.</Typography>
        </Paper>
      )}
      <Grid container spacing={2} mb={3}>
        {plans?.map((plan) => (
          <Grid size={12} key={plan.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{plan.title}</Typography>
                <Typography variant="body2" color="text.secondary" mb={2}>Dietician: {plan.dieticianName}</Typography>
                {MEAL_ORDER.map((meal) => {
                  const items = plan.details.filter((d) => d.mealType === meal);
                  if (items.length === 0) return null;
                  return (
                    <Box key={meal} mb={1.5}>
                      <Typography variant="subtitle2" color="primary">{meal.replace('_', ' ')}</Typography>
                      <List dense disablePadding>
                        {items.map((d) => (
                          <ListItem key={d.id} disableGutters>
                            <ListItemText primary={d.foodItem} secondary={`${d.quantity ?? ''} ${d.calories ? `- ${d.calories} kcal` : ''}`} />
                          </ListItem>
                        ))}
                      </List>
                    </Box>
                  );
                })}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={2}>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>Supplements</Typography>
            <Box display="flex" flexWrap="wrap" gap={1}>
              {supplements?.map((s) => <Chip key={s.id} label={`${s.name} - ${s.dosage ?? ''}`} color="success" />)}
              {!supplements?.length && <Typography color="text.secondary">None assigned.</Typography>}
            </Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>Medicines</Typography>
            <Box display="flex" flexWrap="wrap" gap={1}>
              {medicines?.map((m) => <Chip key={m.id} label={`${m.name} - ${m.dosage ?? ''}`} color="warning" />)}
              {!medicines?.length && <Typography color="text.secondary">None assigned.</Typography>}
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
