import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, NotificationResponse, PageResponse } from '@/types';

export function useNotificationsQuery(page = 0, size = 10) {
  return useQuery({
    queryKey: ['notifications', page, size],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<PageResponse<NotificationResponse>>>('/notifications', {
        params: { page, size },
      });
      return response.data.data;
    },
  });
}

export function useUnreadNotificationCountQuery() {
  return useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<number>>('/notifications/unread-count');
      return response.data.data;
    },
    refetchInterval: 30000,
  });
}

export function useMarkNotificationReadMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const response = await axiosClient.patch<ApiResponse<NotificationResponse>>(`/notifications/${id}/read`);
      return response.data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] });
    },
  });
}
