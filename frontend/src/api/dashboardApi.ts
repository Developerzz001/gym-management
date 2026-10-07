import { useQuery } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { AdminDashboardResponse, ApiResponse, DashboardCategory, DashboardRecordResponse,
  OperationalDashboardSummaryResponse, PageResponse } from '@/types';

export function useAdminDashboardQuery() {
  return useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: async () => (await axiosClient.get<ApiResponse<AdminDashboardResponse>>('/dashboard/admin')).data.data,
    refetchInterval: 60000,
  });
}

const DETAIL_PATHS: Record<DashboardCategory, string> = {
  'member-birthdays': 'member-birthdays',
  'staff-birthdays': 'staff-birthdays',
  'inquiry-followups': 'inquiry-followups',
  'renewal-followups': 'renewal-followups',
  'membership-expiring': 'membership-expiring',
  'done-followups': 'done-followups',
  'balance-payments': 'balance-payments',
  appointments: 'appointments',
};

export function useOperationalDashboardSummaryQuery(branchId?: number, enabled = true) {
  return useQuery({
    queryKey: ['operational-dashboard-summary', branchId],
    enabled,
    queryFn: async () => (await axiosClient.get<ApiResponse<OperationalDashboardSummaryResponse>>('/dashboard/summary', { params: { branchId } })).data.data,
    refetchInterval: 60000,
  });
}

export function useDashboardDetailsQuery(category: DashboardCategory, keyword: string, page: number, pageSize: number,
  sort: string, direction: 'asc' | 'desc', branchId?: number, enabled = true) {
  return useQuery({
    queryKey: ['dashboard-details', category, branchId, keyword, page, pageSize, sort, direction],
    enabled,
    queryFn: async () => (await axiosClient.get<ApiResponse<PageResponse<DashboardRecordResponse>>>(
      `/dashboard/${DETAIL_PATHS[category]}`, { params: { branchId, keyword: keyword || undefined, page, size: pageSize, sort, direction } })).data.data,
  });
}

export async function downloadDashboardRows(category: DashboardCategory, keyword: string, branchId?: number) {
  const rows: DashboardRecordResponse[] = [];
  let page = 0;
  let last = false;
  while (!last) {
    const response = await axiosClient.get<ApiResponse<PageResponse<DashboardRecordResponse>>>(
      `/dashboard/${DETAIL_PATHS[category]}`, { params: { branchId, keyword: keyword || undefined, page, size: 100 } });
    rows.push(...response.data.data.content);
    last = response.data.data.last;
    page += 1;
  }
  return rows;
}