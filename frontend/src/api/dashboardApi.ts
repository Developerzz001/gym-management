import { useQuery } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { AdminDashboardResponse, ApiResponse } from '@/types';

export function useAdminDashboardQuery() {
  return useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: async () => (await axiosClient.get<ApiResponse<AdminDashboardResponse>>('/dashboard/admin')).data.data,
    refetchInterval: 60000,
  });
}