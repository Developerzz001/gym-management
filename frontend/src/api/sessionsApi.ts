import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, SessionResponse, SessionStatus } from '@/types';

export interface SessionRequest {
  clientId: number;
  sessionDateTime: string;
  notes?: string;
}

export function useSessionsByClientQuery(clientId?: number) {
  return useQuery({
    queryKey: ['sessions', 'client', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<SessionResponse[]>>(`/sessions/client/${clientId}`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useUpcomingSessionsQuery(clientId?: number) {
  return useQuery({
    queryKey: ['sessions', 'upcoming', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<SessionResponse[]>>(`/sessions/client/${clientId}/upcoming`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useMySessionsQuery() {
  return useQuery({
    queryKey: ['sessions', 'my-sessions'],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<SessionResponse[]>>('/sessions/my-sessions');
      return response.data.data;
    },
  });
}

export function useScheduleSessionMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: SessionRequest) => {
      const response = await axiosClient.post<ApiResponse<SessionResponse>>('/sessions', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sessions'] }),
  });
}

export function useUpdateSessionStatusMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, status }: { id: number; status: SessionStatus }) => {
      const response = await axiosClient.patch<ApiResponse<SessionResponse>>(`/sessions/${id}/status`, { status });
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sessions'] }),
  });
}
