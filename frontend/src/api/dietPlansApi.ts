import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, DietPlanDetailRequest, DietPlanResponse } from '@/types';

export interface DietPlanRequest {
  clientId: number;
  title: string;
  description?: string;
  details: DietPlanDetailRequest[];
}

export function useDietPlansByClientQuery(clientId?: number) {
  return useQuery({
    queryKey: ['diet-plans', 'client', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<DietPlanResponse[]>>(`/diet-plans/client/${clientId}`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useMyDietPlansQuery() {
  return useQuery({
    queryKey: ['diet-plans', 'my-plans'],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<DietPlanResponse[]>>('/diet-plans/my-plans');
      return response.data.data;
    },
  });
}

export function useCreateDietPlanMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: DietPlanRequest) => {
      const response = await axiosClient.post<ApiResponse<DietPlanResponse>>('/diet-plans', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['diet-plans'] }),
  });
}

export function useUpdateDietPlanMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: DietPlanRequest }) => {
      const response = await axiosClient.put<ApiResponse<DietPlanResponse>>(`/diet-plans/${id}`, payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['diet-plans'] }),
  });
}

export function useDeleteDietPlanMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await axiosClient.delete(`/diet-plans/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['diet-plans'] }),
  });
}
