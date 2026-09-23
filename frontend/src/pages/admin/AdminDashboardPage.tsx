import { Paper, Box, Typography } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { PageHeader } from '@/components/common/PageHeader';
import { useAdminDashboardQuery } from '@/api/dashboardApi';

const money = (value = 0) => new Intl.NumberFormat('en-IN', {
  style: 'currency', currency: 'INR', maximumFractionDigits: 0,
}).format(value);

function StatCard({ label, value }: { label: string; value: number | string }) {
  return (
    <Paper sx={{ p: 2.5, minHeight: 96 }}>
      <Typography variant="h5" fontWeight={700}>{value}</Typography>
      <Typography variant="body2" color="text.secondary">{label}</Typography>
    </Paper>
  );
}

export function AdminDashboardPage() {
  const { data } = useAdminDashboardQuery();
  const metrics: Array<[string, string | number]> = [
    ['Total Revenue', money(data?.totalRevenue)],
    ['This Month', money(data?.monthlyRevenue)],
    ['Today', money(data?.todayRevenue)],
    ['Outstanding', money(data?.outstandingPayments)],
    ['Overdue', money(data?.overduePayments)],
    ['Collection Efficiency', `${data?.collectionEfficiencyPercent ?? 0}%`],
    ['Active Members', data?.activeMembers ?? 0],
    ['New Members', data?.newMembers ?? 0],
    ['Expiring (30 days)', data?.expiringMemberships ?? 0],
    ["Today's Attendance", data?.todayAttendance ?? 0],
    ['Monthly Attendance', data?.monthlyAttendance ?? 0],
    ['Sessions Conducted', data?.sessionsConducted ?? 0],
  ];

  return (
    <Box>
      <PageHeader title="Business Dashboard" subtitle="Revenue, membership, attendance, and staff performance" />
      <Grid container spacing={2}>
        {metrics.map(([label, value]) => <Grid key={label} size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label={label} value={value} />
        </Grid>)}
      </Grid>
      <Grid container spacing={2} sx={{ mt: 1 }}>
        <Grid size={{ xs: 12, md: 6 }}><Paper sx={{ p: 2.5 }}>
          <Typography variant="h6">Top Coaches</Typography>
          {data?.topPerformingCoaches.map((item) => <Typography key={item.staffId} sx={{ py: 0.75 }}>{item.name}: {item.completedActivities} completed sessions</Typography>)}
        </Paper></Grid>
        <Grid size={{ xs: 12, md: 6 }}><Paper sx={{ p: 2.5 }}>
          <Typography variant="h6">Top Dieticians</Typography>
          {data?.topPerformingDieticians.map((item) => <Typography key={item.staffId} sx={{ py: 0.75 }}>{item.name}: {item.completedActivities} plans</Typography>)}
        </Paper></Grid>
      </Grid>
    </Box>
  );
}
