import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, FollowUpResponse, InquiryRating } from '@/types';

export interface FollowUpRequest {
  followUpDate?: string;
  comment?: string;
  executiveId?: number;
  nextFollowUpDate?: string;
  rating?: InquiryRating;
}

export function useFollowUpsQuery(clientId?: number) {
  return useQuery({
    queryKey: ['follow-ups', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<FollowUpResponse[]>>(`/clients/${clientId}/follow-ups`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useAddFollowUpMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ clientId, payload }: { clientId: number; payload: FollowUpRequest }) => {
      const response = await axiosClient.post<ApiResponse<FollowUpResponse>>(`/clients/${clientId}/follow-ups`, payload);
      return response.data.data;
    },
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['follow-ups', variables.clientId] });
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      queryClient.invalidateQueries({ queryKey: ['client', variables.clientId] });
      queryClient.invalidateQueries({ queryKey: ['operational-dashboard-summary'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-details'] });
    },
  });
}

export function useClientProfileImageQuery(clientId?: number) {
  return useQuery({
    queryKey: ['client-profile-image', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<Blob>(`/clients/${clientId}/profile-image`, { responseType: 'blob' });
      return URL.createObjectURL(response.data);
    },
    enabled: !!clientId,
    retry: false,
  });
}
