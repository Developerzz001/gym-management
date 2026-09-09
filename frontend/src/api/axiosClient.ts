import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse, LoginResponse } from '@/types';

const LEGACY_AUTH_STORAGE_KEY = 'gym_auth';

export interface StoredAuth {
  accessToken: string;
  refreshToken: string;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
}

let currentAuth: StoredAuth | null = null;

if (typeof localStorage !== 'undefined') {
  localStorage.removeItem(LEGACY_AUTH_STORAGE_KEY);
}

export function getStoredAuth(): StoredAuth | null {
  return currentAuth;
}

export function setStoredAuth(auth: StoredAuth | null) {
  currentAuth = auth;
}

export const axiosClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const auth = getStoredAuth();
  if (auth?.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`;
  }
  return config;
});

let isRefreshing = false;
let refreshQueue: Array<(token: string | null) => void> = [];

function subscribeTokenRefresh(cb: (token: string | null) => void) {
  refreshQueue.push(cb);
}

function onTokenRefreshed(token: string | null) {
  refreshQueue.forEach((cb) => cb(token));
  refreshQueue = [];
}

axiosClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry &&
        !originalRequest.url?.includes('/auth/login') && !originalRequest.url?.includes('/auth/refresh-token')) {
      const auth = getStoredAuth();
      if (!auth?.refreshToken) {
        setStoredAuth(null);
        window.location.href = '/login';
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          subscribeTokenRefresh((token) => {
            if (token) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
              resolve(axiosClient(originalRequest));
            } else {
              reject(error);
            }
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const response = await axios.post<ApiResponse<LoginResponse>>('/api/v1/auth/refresh-token', {
          refreshToken: auth.refreshToken,
        });
        const data = response.data.data;
        setStoredAuth({
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          userId: data.userId,
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
          role: data.role,
        });
        isRefreshing = false;
        onTokenRefreshed(data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        isRefreshing = false;
        onTokenRefreshed(null);
        setStoredAuth(null);
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; validationErrors?: Record<string, string> } | undefined;
    if (data?.validationErrors) {
      return Object.values(data.validationErrors).join(', ');
    }
    return data?.message || error.message || 'Something went wrong';
  }
  return 'Something went wrong';
}
