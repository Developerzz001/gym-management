import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, ClientResponse, Gender, PageResponse, RegistrationType } from '@/types';

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

export function useClientsQuery(keyword: string, page: number, size: number, registrationType?: RegistrationType) {
  return useQuery({
    queryKey: ['clients', keyword, page, size, registrationType],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<PageResponse<ClientResponse>>>('/clients', {
        params: { keyword: keyword || undefined, registrationType, page, size },
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

export function useRegisterInquiryMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: ClientRequest) => {
      const response = await axiosClient.post<ApiResponse<ClientResponse>>('/clients/inquiries', payload);
      return response.data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      queryClient.invalidateQueries({ queryKey: ['inquiries'] });
    },
  });
}

export function useUploadClientProfileImageMutation() {
  return useMutation({
    mutationFn: async ({ id, file }: { id: number; file: File }) => {
      const formData = new FormData();
      formData.append('file', file);
      await axiosClient.put(`/clients/${id}/profile-image`, formData, {
        headers: { 'Content-Type': undefined },
      });
    },
  });
}

export function useConvertInquiryMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: ClientRequest }) => {
      const response = await axiosClient.post<ApiResponse<ClientResponse>>(`/clients/${id}/convert`, payload);
      return response.data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inquiries'] });
      queryClient.invalidateQueries({ queryKey: ['clients'] });
    },
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

export function useActivateClientMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const response = await axiosClient.patch<ApiResponse<ClientResponse>>(`/clients/${id}/activate`);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  });
}
