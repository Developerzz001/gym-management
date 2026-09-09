import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, ClientResponse, CoachResponse, DieticianResponse } from '@/types';
import type { ClientRequest } from './clientsApi';
import type { CoachRequest } from './coachesApi';
import type { DieticianRequest } from './dieticiansApi';

export type ProfileResponse = ClientResponse | CoachResponse | DieticianResponse;
export type ProfileRequest = ClientRequest | CoachRequest | DieticianRequest;

export function useMyProfileQuery(role: 'CLIENT' | 'FITNESS_COACH' | 'DIETICIAN') {
  const path = role === 'CLIENT' ? '/clients/me' : role === 'FITNESS_COACH' ? '/coaches/me' : '/dieticians/me';
  return useQuery({
    queryKey: ['profile', role],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<ProfileResponse>>(path);
      return response.data.data;
    },
  });
}

export function useUpdateMyProfileMutation(role: 'CLIENT' | 'FITNESS_COACH' | 'DIETICIAN') {
  const queryClient = useQueryClient();
  const path = role === 'CLIENT' ? '/clients/me' : role === 'FITNESS_COACH' ? '/coaches/me' : '/dieticians/me';
  return useMutation({
    mutationFn: async (payload: ProfileRequest) => {
      const response = await axiosClient.put<ApiResponse<ProfileResponse>>(path, payload);
      return response.data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile', role] });
      queryClient.invalidateQueries({ queryKey: ['client', 'me'] });
    },
  });
}

export function useMyProfileImageQuery(enabled = true) {
  return useQuery({
    queryKey: ['profile-image'],
    enabled,
    queryFn: async () => {
      const response = await axiosClient.get<Blob>('/users/me/profile-image', { responseType: 'blob' });
      return URL.createObjectURL(response.data);
    },
    retry: false,
  });
}

export function useUploadMyProfileImageMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (file: File) => {
      if (file.size > 5 * 1024 * 1024) {
        throw new Error('Profile image must be 5 MB or smaller');
      }
      if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
        throw new Error('Only JPG, PNG, and WEBP images are supported');
      }
      const formData = new FormData();
      formData.append('file', file);
      await axiosClient.put('/users/me/profile-image', formData, {
        headers: { 'Content-Type': undefined },
      });
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['profile-image'] }),
  });
}