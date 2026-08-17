import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, WorkoutPlanDetailRequest, WorkoutPlanResponse } from '@/types';

export interface WorkoutPlanRequest {
  clientId: number;
  title: string;
  description?: string;
  details: WorkoutPlanDetailRequest[];
}

export function useWorkoutPlansByClientQuery(clientId?: number) {
  return useQuery({
    queryKey: ['workout-plans', 'client', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<WorkoutPlanResponse[]>>(`/workout-plans/client/${clientId}`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useTodaysWorkoutQuery(clientId?: number) {
  return useQuery({
    queryKey: ['workout-plans', 'today', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<WorkoutPlanResponse | null>>(`/workout-plans/client/${clientId}/today`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useMyWorkoutPlansQuery() {
  return useQuery({
    queryKey: ['workout-plans', 'my-plans'],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<WorkoutPlanResponse[]>>('/workout-plans/my-plans');
      return response.data.data;
    },
  });
}

export function useCreateWorkoutPlanMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: WorkoutPlanRequest) => {
      const response = await axiosClient.post<ApiResponse<WorkoutPlanResponse>>('/workout-plans', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['workout-plans'] }),
  });
}

export function useUpdateWorkoutPlanMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: WorkoutPlanRequest }) => {
      const response = await axiosClient.put<ApiResponse<WorkoutPlanResponse>>(`/workout-plans/${id}`, payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['workout-plans'] }),
  });
}

export function useDeleteWorkoutPlanMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await axiosClient.delete(`/workout-plans/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['workout-plans'] }),
  });
}
