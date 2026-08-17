import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, ClientResponse, Gender, PageResponse } from '@/types';

export interface ClientRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  active?: boolean;
  gender?: Gender;
  dateOfBirth?: string;
  heightCm?: number;
  weightKg?: number;
  address?: string;
  contactNumber?: string;
  fitnessGoal?: string;
  diabetes: boolean;
  hypertension: boolean;
  asthma: boolean;
  allergies?: string;
  injuries?: string;
  medicalNotes?: string;
}

export function useClientsQuery(keyword: string, page: number, size: number) {
  return useQuery({
    queryKey: ['clients', keyword, page, size],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<PageResponse<ClientResponse>>>('/clients', {
        params: { keyword: keyword || undefined, page, size },
      });
      return response.data.data;
    },
  });
}

export function useClientQuery(id?: number) {
  return useQuery({
    queryKey: ['client', id],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<ClientResponse>>(`/clients/${id}`);
      return response.data.data;
    },
    enabled: !!id,
  });
}

export function useMyClientProfileQuery() {
  return useQuery({
    queryKey: ['client', 'me'],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<ClientResponse>>('/clients/me');
      return response.data.data;
    },
  });
}

export function useAssignedClientsQuery(role: 'coach' | 'dietician') {
  const path = role === 'coach' ? '/clients/assigned-coach' : '/clients/assigned-dietician';
  return useQuery({
    queryKey: ['clients', 'assigned', role],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<ClientResponse[]>>(path);
      return response.data.data;
    },
  });
}

export function useRegisterClientMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: ClientRequest) => {
      const response = await axiosClient.post<ApiResponse<ClientResponse>>('/clients', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  });
}

export function useUpdateClientMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: ClientRequest }) => {
      const response = await axiosClient.put<ApiResponse<ClientResponse>>(`/clients/${id}`, payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  });
}

export function useDeactivateClientMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const response = await axiosClient.patch<ApiResponse<ClientResponse>>(`/clients/${id}/deactivate`);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  });
}
