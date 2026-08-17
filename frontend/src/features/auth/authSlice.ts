import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import { getStoredAuth, setStoredAuth, type StoredAuth } from '@/api/axiosClient';
import type { Role } from '@/types';

interface AuthState {
  isAuthenticated: boolean;
  userId: number | null;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  role: Role | null;
}

function buildInitialState(): AuthState {
  const stored = getStoredAuth();
  if (!stored) {
    return {
      isAuthenticated: false,
      userId: null,
      firstName: null,
      lastName: null,
      email: null,
      role: null,
    };
  }
  return {
    isAuthenticated: true,
    userId: stored.userId,
    firstName: stored.firstName,
    lastName: stored.lastName,
    email: stored.email,
    role: stored.role as Role,
  };
}

const authSlice = createSlice({
  name: 'auth',
  initialState: buildInitialState(),
  reducers: {
    setCredentials: (state, action: PayloadAction<StoredAuth>) => {
      const auth = action.payload;
      setStoredAuth(auth);
      state.isAuthenticated = true;
      state.userId = auth.userId;
      state.firstName = auth.firstName;
      state.lastName = auth.lastName;
      state.email = auth.email;
      state.role = auth.role as Role;
    },
    clearCredentials: (state) => {
      setStoredAuth(null);
      state.isAuthenticated = false;
      state.userId = null;
      state.firstName = null;
      state.lastName = null;
      state.email = null;
      state.role = null;
    },
  },
});

export const { setCredentials, clearCredentials } = authSlice.actions;
export default authSlice.reducer;
