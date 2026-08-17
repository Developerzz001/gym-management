import { useMutation } from '@tanstack/react-query';
import { axiosClient } from './axiosClient';
import type { ApiResponse, AskAiRequest, AskAiResponse } from '@/types';

export function useAskAiMutation() {
  return useMutation({
    mutationFn: async (payload: AskAiRequest) => {
      const response = await axiosClient.post<ApiResponse<AskAiResponse>>('/ai/ask', payload);
      return response.data.data;
    },
  });
}
