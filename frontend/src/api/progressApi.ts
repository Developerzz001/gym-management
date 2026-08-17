import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, ProgressRecordResponse } from '@/types';

export interface ProgressRecordRequest {
  clientId: number;
  recordDate: string;
  weightKg?: number;
  chestCm?: number;
  waistCm?: number;
  armsCm?: number;
  shoulderCm?: number;
  thighCm?: number;
}

export function useProgressByClientQuery(clientId?: number) {
  return useQuery({
    queryKey: ['progress', 'client', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<ProgressRecordResponse[]>>(`/progress/client/${clientId}`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useAddProgressMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: ProgressRecordRequest) => {
      const response = await axiosClient.post<ApiResponse<ProgressRecordResponse>>('/progress', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['progress'] }),
  });
}
