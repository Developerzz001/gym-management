import { useState } from 'react';
import { Alert, Box, Button, CircularProgress, List, ListItem, ListItemText, Pagination, Paper, Stack } from '@mui/material';
import { useMarkNotificationReadMutation, useNotificationsQuery } from '@/api/notificationsApi';
import { extractErrorMessage } from '@/api/axiosClient';
import { PageHeader } from '@/components/common/PageHeader';

const PAGE_SIZE = 10;

export function NotificationsPage() {
  const [page, setPage] = useState(0);
  const notifications = useNotificationsQuery(page, PAGE_SIZE);
  const markAsRead = useMarkNotificationReadMutation();

  return (
    <Box>
      <PageHeader title="Notifications" subtitle="Your latest gym updates and reminders" />
      {(notifications.error || markAsRead.error) && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {extractErrorMessage(notifications.error || markAsRead.error)}
        </Alert>
      )}
      <Paper>
        {notifications.isLoading ? (
          <Stack alignItems="center" sx={{ py: 6 }}><CircularProgress /></Stack>
        ) : notifications.data?.content.length ? (
          <List disablePadding>
            {notifications.data.content.map((notification) => (
              <ListItem
                key={notification.id}
                divider
                secondaryAction={!notification.isRead ? (
                  <Button
                    size="small"
                    disabled={markAsRead.isPending}
                    onClick={() => markAsRead.mutate(notification.id)}
                  >
                    Mark as read
                  </Button>
                ) : undefined}
              >
                <ListItemText
                  primary={notification.message}
                  secondary={new Date(notification.createdAt).toLocaleString()}
                  primaryTypographyProps={{ fontWeight: notification.isRead ? 400 : 700 }}
                />
              </ListItem>
            ))}
          </List>
        ) : (
          <Alert severity="info">No notifications yet.</Alert>
        )}
      </Paper>
      {(notifications.data?.totalPages ?? 0) > 1 && (
        <Stack alignItems="center" sx={{ mt: 2 }}>
          <Pagination
            page={page + 1}
            count={notifications.data?.totalPages ?? 1}
            onChange={(_, value) => setPage(value - 1)}
          />
        </Stack>
      )}
    </Box>
  );
}