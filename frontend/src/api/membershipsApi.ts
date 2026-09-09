import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, MembershipPlanResponse, MembershipResponse } from '@/types';

export interface MembershipPlanRequest {
  name: string;
  durationDays: number;
  fees: number;
  description?: string;
}

export interface AssignMembershipRequest {
  clientId: number;
  membershipPlanId: number;
  startDate: string;
}

export function useMembershipPlansQuery() {
  return useQuery({
    queryKey: ['membership-plans'],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<MembershipPlanResponse[]>>('/membership-plans');
      return response.data.data;
    },
  });
}

export function useCreateMembershipPlanMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: MembershipPlanRequest) => {
      const response = await axiosClient.post<ApiResponse<MembershipPlanResponse>>('/membership-plans', payload);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['membership-plans'] }),
  });
}

export function useDeleteMembershipPlanMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await axiosClient.delete(`/membership-plans/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['membership-plans'] }),
  });
}

export function useMembershipsByClientQuery(clientId?: number) {
  return useQuery({
    queryKey: ['memberships', 'client', clientId],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<MembershipResponse[]>>(`/memberships/client/${clientId}`);
      return response.data.data;
    },
    enabled: !!clientId,
  });
}

export function useAssignMembershipMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: AssignMembershipRequest) => {
      const response = await axiosClient.post<ApiResponse<MembershipResponse>>('/memberships/assign', payload);
      return response.data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['memberships'] });
      queryClient.invalidateQueries({ queryKey: ['clients'] });
    },
  });
}

export function useRenewMembershipMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (clientId: number) => {
      const response = await axiosClient.post<ApiResponse<MembershipResponse>>(`/memberships/renew/${clientId}`);
      return response.data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['memberships'] }),
  });
}
