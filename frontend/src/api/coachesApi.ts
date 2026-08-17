import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, CoachResponse, PageResponse } from '@/types';

export interface CoachRequest {
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber?: string;
  password?: string;
  specialization?: string;
  experienceYears?: number;
  bio?: string;
  active?: boolean;
}

export function useCoachesQuery(keyword: string, page: number, size: number) {
  return useQuery({
    queryKey: ['coaches', keyword, page, size],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<PageResponse<CoachResponse>>>('/coaches', {
        params: { keyword: keyword || undefined, page, size },
      });
      return response.data.data;
    },
  });
}

export function useCreateCoachMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: CoachRequest) => {
      const response = await axiosClient.post<ApiResponse<CoachResponse>>('/coaches', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['coaches'] }),
  });
}

export function useUpdateCoachMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: CoachRequest }) => {
      const response = await axiosClient.put<ApiResponse<CoachResponse>>(`/coaches/${id}`, payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['coaches'] }),
  });
}

export function useDeleteCoachMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await axiosClient.delete(`/coaches/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['coaches'] }),
  });
}
