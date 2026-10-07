import { useMutation, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, EnrollmentResponse, Gender, PaymentMethod } from '@/types';

export interface ClientEnrollmentRequest {
  branchId?: number;
  firstName: string;
  lastName: string;
  email?: string;
  contactNumber: string;
  alternateContactNumber?: string;
  gender: Gender;
  dateOfBirth?: string;
  address?: string;
  executiveId?: number;
  fitnessGoal?: string;
  heightCm?: number;
  weightKg?: number;
  allergies?: string;
  injuries?: string;
  medicalNotes?: string;
  activityId: number;
  membershipPlanId: number;
  startDate: string;
  membershipDiscountId?: number;
  tax: number;
  description?: string;
  amountPaid: number;
  paymentMethod: PaymentMethod;
  transactionReference?: string;
  remarks?: string;
}

export function useRegisterClientWithEnrollmentMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: ClientEnrollmentRequest) => {
      const response = await axiosClient.post<ApiResponse<EnrollmentResponse>>('/enrollments/clients', payload);
      return response.data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
      queryClient.invalidateQueries({ queryKey: ['next-member-code'] });
    },
  });
}
