import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, AttendanceResponse } from '@/types';

export function useAttendanceReportQuery(from: string, to: string) {
  return useQuery({
    queryKey: ['attendance', from, to],
    queryFn: async () => (await axiosClient.get<ApiResponse<AttendanceResponse[]>>('/attendance/reports', { params: { from, to } })).data.data,
  });
}

export function useAttendanceActionMutation(action: 'check-in' | 'check-out') {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (clientId: number) => (await axiosClient.post<ApiResponse<AttendanceResponse>>(`/attendance/clients/${clientId}/${action}`)).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['attendance'] }),
  });
}