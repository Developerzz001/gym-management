import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, MembershipDiscountResponse, MembershipPlanResponse, MembershipResponse } from '@/types';

export interface MembershipPlanRequest {
  name: string;
  durationDays: number;
  fees: number;
  extraDurationDays: number;
  description?: string;
}

export interface MembershipDiscountRequest {
  name: string;
  percentage: number;
  extraFreeDays: number;
  description?: string;
  active: boolean;
}

export function useMembershipDiscountsQuery(activeOnly = false) {
  return useQuery({
    queryKey: ['membership-discounts', activeOnly],
    queryFn: async () => {
      const response = await axiosClient.get<ApiResponse<MembershipDiscountResponse[]>>('/membership-discounts', {
        params: { activeOnly },
      });
      return response.data.data;
    },
  });
}

export function useCreateMembershipDiscountMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: MembershipDiscountRequest) =>
      (await axiosClient.post<ApiResponse<MembershipDiscountResponse>>('/membership-discounts', payload)).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['membership-discounts'] }),
  });
}

export function useUpdateMembershipDiscountMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: MembershipDiscountRequest }) =>
      (await axiosClient.put<ApiResponse<MembershipDiscountResponse>>(`/membership-discounts/${id}`, payload)).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['membership-discounts'] }),
  });
}

export function useMembershipDiscountStatusMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, active }: { id: number; active: boolean }) =>
      (await axiosClient.patch<ApiResponse<MembershipDiscountResponse>>(`/membership-discounts/${id}/status`, null, {
        params: { active },
      })).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['membership-discounts'] }),
  });
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

export function useUpdateMembershipPlanMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: MembershipPlanRequest }) => {
      const response = await axiosClient.put<ApiResponse<MembershipPlanResponse>>(`/membership-plans/${id}`, payload);
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
