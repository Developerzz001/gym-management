import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, DieticianResponse, PageResponse } from '@/types';

export interface DieticianRequest {
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

export function useDieticiansQuery(keyword: string, page: number, size: number, enabled = true) {
  return useQuery({
    queryKey: ['dieticians', keyword, page, size],
    enabled,
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<PageResponse<DieticianResponse>>>('/dieticians', {
        params: { keyword: keyword || undefined, page, size },
      });
      return response.data.data;
    },
  });
}

export function useCreateDieticianMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: DieticianRequest) => {
      const response = await axiosClient.post<ApiResponse<DieticianResponse>>('/dieticians', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dieticians'] }),
  });
}

export function useUpdateDieticianMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: DieticianRequest }) => {
      const response = await axiosClient.put<ApiResponse<DieticianResponse>>(`/dieticians/${id}`, payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dieticians'] }),
  });
}

export function useActivateDieticianMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const response = await axiosClient.patch<ApiResponse<DieticianResponse>>(`/dieticians/${id}/activate`);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dieticians'] }),
  });
}

export function useDeactivateDieticianMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const response = await axiosClient.patch<ApiResponse<DieticianResponse>>(`/dieticians/${id}/deactivate`);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dieticians'] }),
  });
}

export function useDeleteDieticianMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await axiosClient.delete(`/dieticians/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dieticians'] }),
  });
}
