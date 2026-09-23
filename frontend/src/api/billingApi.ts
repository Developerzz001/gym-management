import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, InvoiceResponse, InvoiceType, PageResponse, PaymentMethod } from '@/types';

export function useInvoicesQuery(client = false) {
  return useQuery({
    queryKey: ['invoices', client],
    queryFn: async () => (await axiosClient.get<ApiResponse<PageResponse<InvoiceResponse>>>(client ? '/invoices/mine' : '/invoices')).data.data,
  });
}

export function useCreateInvoiceMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (request: { clientId: number; invoiceType: InvoiceType; description: string; totalAmount?: number; tax: number; discount: number; dueDate: string;
      membershipPlanId?: number; membershipDiscountId?: number; serviceStartDate?: string; sessionCount?: number; purchaseDate?: string;
      initialPaymentAmount?: number; paymentMethod?: PaymentMethod; transactionReference?: string; paymentRemarks?: string }) =>
      (await axiosClient.post<ApiResponse<InvoiceResponse>>('/invoices', request)).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['invoices'] }),
  });
}

export function useRecordPaymentMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ invoiceId, ...request }: { invoiceId: number; amount: number; paymentMethod: PaymentMethod; transactionReference?: string; remarks?: string }) =>
      (await axiosClient.post<ApiResponse<InvoiceResponse>>(`/invoices/${invoiceId}/payments`, request)).data.data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['invoices'] }),
  });
}

export async function downloadInvoicePdf(invoice: InvoiceResponse, client = false) {
  const response = await axiosClient.get(client ? `/invoices/mine/${invoice.id}/pdf` : `/invoices/${invoice.id}/pdf`, { responseType: 'blob' });
  const url = URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${invoice.invoiceNumber}.pdf`;
  link.click();
  URL.revokeObjectURL(url);
}