import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, PageResponse, Role, UserResponse } from '@/types';

export interface UserPayload {
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber?: string;
  shiftStartTime?: string;
  shiftEndTime?: string;
  password: string;
  role: Role;
  organizationId?: number;
  branchId?: number;
  active: boolean;
}

export function useUsersQuery(page: number, size = 20) {
  return useQuery({ queryKey: ['tenant-users', page, size], queryFn: async () =>
    (await axiosClient.get<ApiResponse<PageResponse<UserResponse>>>('/users', { params: { page, size } })).data.data });
}

export function useCreateUserMutation() {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: async (payload: UserPayload) =>
    (await axiosClient.post<ApiResponse<UserResponse>>('/users', payload)).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tenant-users'] }) });
}