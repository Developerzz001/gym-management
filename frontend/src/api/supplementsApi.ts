import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, SupplementResponse } from '@/types';

export interface SupplementRequest {
  clientId: number;
  name: string;
  dosage?: string;
  timing?: string;
  instructions?: string;
}

export function useSupplementsByClientQuery(clientId?: number) {
  return useQuery({
    queryKey: ['supplements', 'client', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<SupplementResponse[]>>(`/supplements/client/${clientId}`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useCreateSupplementMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: SupplementRequest) => {
      const response = await axiosClient.post<ApiResponse<SupplementResponse>>('/supplements', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['supplements'] }),
  });
}

export function useDeleteSupplementMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await axiosClient.delete(`/supplements/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['supplements'] }),
  });
}
