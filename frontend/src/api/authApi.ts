import { useMutation } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, LoginResponse } from '@/types';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

export async function loginRequest(payload: LoginPayload): Promise<LoginResponse> {
  const response = await axiosClient.post<ApiResponse<LoginResponse>>('/auth/login', payload);
  return response.data.data;
}

export async function logoutRequest(refreshToken: string): Promise<void> {
  await axiosClient.post('/auth/logout', { refreshToken });
}

export async function changePasswordRequest(payload: ChangePasswordPayload): Promise<void> {
  await axiosClient.post('/auth/change-password', payload);
}

export function useLoginMutation() {
  return useMutation({ mutationFn: loginRequest });
}

export function useChangePasswordMutation() {
  return useMutation({ mutationFn: changePasswordRequest });
}
