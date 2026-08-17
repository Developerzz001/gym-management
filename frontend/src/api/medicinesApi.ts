import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, MedicineResponse } from '@/types';

export interface MedicineRequest {
  clientId: number;
  name: string;
  dosage?: string;
  timing?: string;
  instructions?: string;
}

export function useMedicinesByClientQuery(clientId?: number) {
  return useQuery({
    queryKey: ['medicines', 'client', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<MedicineResponse[]>>(`/medicines/client/${clientId}`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useCreateMedicineMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: MedicineRequest) => {
      const response = await axiosClient.post<ApiResponse<MedicineResponse>>('/medicines', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['medicines'] }),
  });
}

export function useDeleteMedicineMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await axiosClient.delete(`/medicines/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['medicines'] }),
  });
}
