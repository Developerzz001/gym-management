import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, ExerciseCategory, ExerciseResponse, PageResponse } from '@/types';

export interface ExerciseRequest {
  name: string;
  category: ExerciseCategory;
  description?: string;
}

export function useExercisesQuery(category?: ExerciseCategory, name?: string) {
  return useQuery({
    queryKey: ['exercises', category, name],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<PageResponse<ExerciseResponse>>>('/exercises', {
        params: { category, name: name || undefined, page: 0, size: 200 },
      });
      return response.data.data.content;
    },
  });
}

export function useCreateExerciseMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: ExerciseRequest) => {
      const response = await axiosClient.post<ApiResponse<ExerciseResponse>>('/exercises', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['exercises'] }),
  });
}

export function useUpdateExerciseMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: ExerciseRequest }) => {
      const response = await axiosClient.put<ApiResponse<ExerciseResponse>>(`/exercises/${id}`, payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['exercises'] }),
  });
}

export function useDeleteExerciseMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await axiosClient.delete(`/exercises/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['exercises'] }),
  });
}
