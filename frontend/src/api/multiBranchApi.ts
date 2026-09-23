import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, BranchDashboardResponse, BranchResponse, BranchSettingsResponse,
  OrganizationDashboardResponse, OrganizationResponse, PageResponse } from '@/types';

export type OrganizationPayload = Pick<OrganizationResponse, 'name' | 'ownerName' | 'email'> & { adminPassword?: string } &
  Partial<Pick<OrganizationResponse, 'contactNumber' | 'address'>>;
export type BranchPayload = Omit<BranchResponse, 'id' | 'branchCode' | 'organizationName' | 'managerName' | 'status' | 'createdDate' | 'updatedDate'>;

export function useOrganizationsQuery(keyword: string, page: number, size = 20, enabled = true) {
  return useQuery({ queryKey: ['organizations', keyword, page, size], enabled, queryFn: async () =>
    (await axiosClient.get<ApiResponse<PageResponse<OrganizationResponse>>>('/organizations', { params: { keyword, page, size } })).data.data });
}

export function useSaveOrganizationMutation(id?: number) {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: async (payload: OrganizationPayload) =>
    (await (id ? axiosClient.put<ApiResponse<OrganizationResponse>>(`/organizations/${id}`, payload)
      : axiosClient.post<ApiResponse<OrganizationResponse>>('/organizations', payload))).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['organizations'] }) });
}

export function useOrganizationStatusMutation() {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: async ({ id, active }: { id: number; active: boolean }) =>
    (await axiosClient.patch<ApiResponse<OrganizationResponse>>(`/organizations/${id}/status`, null, { params: { active } })).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['organizations'] }) });
}

export function useBranchesQuery(organizationId?: number, keyword = '', page = 0, size = 20, enabled = true) {
  return useQuery({ queryKey: ['branches', organizationId, keyword, page, size], enabled, queryFn: async () =>
    (await axiosClient.get<ApiResponse<PageResponse<BranchResponse>>>('/branches', { params: { organizationId, keyword, page, size } })).data.data });
}

export function useSaveBranchMutation(id?: number) {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: async (payload: BranchPayload) =>
    (await (id ? axiosClient.put<ApiResponse<BranchResponse>>(`/branches/${id}`, payload)
      : axiosClient.post<ApiResponse<BranchResponse>>('/branches', payload))).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['branches'] }) });
}

export function useBranchStatusMutation() {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: async ({ id, active }: { id: number; active: boolean }) =>
    (await axiosClient.patch<ApiResponse<BranchResponse>>(`/branches/${id}/status`, null, { params: { active } })).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['branches'] }) });
}

export function useBranchDashboardQuery(branchId?: number) {
  return useQuery({ queryKey: ['branch-dashboard', branchId], enabled: !!branchId, refetchInterval: 60000,
    queryFn: async () => (await axiosClient.get<ApiResponse<BranchDashboardResponse>>(`/dashboards/branches/${branchId}`)).data.data });
}

export function useOrganizationDashboardQuery(organizationId?: number, from?: string, to?: string) {
  return useQuery({ queryKey: ['organization-dashboard', organizationId, from, to], enabled: !!organizationId,
    queryFn: async () => (await axiosClient.get<ApiResponse<OrganizationDashboardResponse>>(
      `/dashboards/organizations/${organizationId}`, { params: { from, to } })).data.data });
}

export function useBranchSettingsQuery(branchId?: number) {
  return useQuery({ queryKey: ['branch-settings', branchId], enabled: !!branchId,
    queryFn: async () => (await axiosClient.get<ApiResponse<BranchSettingsResponse>>(`/branches/${branchId}/settings`)).data.data });
}

export function useSaveBranchSettingsMutation(branchId?: number) {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: async (payload: Omit<BranchSettingsResponse, 'branchId'>) =>
    (await axiosClient.put<ApiResponse<BranchSettingsResponse>>(`/branches/${branchId}/settings`, payload)).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['branch-settings', branchId] }) });
}

export function useTransferMutation(kind: 'members' | 'staff', subjectId: number) {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: async (payload: { destinationBranchId: number; reason: string }) =>
    (await axiosClient.post(`/branches/${kind}/${subjectId}/transfer`, payload)).data,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['clients'] }); queryClient.invalidateQueries({ queryKey: ['branches'] }); } });
}