import { useMutation, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, ClientResponse } from '@/types';

export function useAssignCoachMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: { clientId: number; coachId: number }) => {
      const response = await axiosClient.post<ApiResponse<ClientResponse>>('/admin/assignments/coach', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  });
}

export function useAssignDieticianMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: { clientId: number; dieticianId: number }) => {
      const response = await axiosClient.post<ApiResponse<ClientResponse>>('/admin/assignments/dietician', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  });
}
