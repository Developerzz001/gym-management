import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Box,
  Typography,
  TextField,
  Button,
  Alert,
  InputAdornment,
  IconButton,
  Avatar,
} from '@mui/material';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { useLoginMutation } from '@/api/authApi';
import { useAppDispatch } from '@/app/hooks';
import { setCredentials } from '@/features/auth/authSlice';
import { extractErrorMessage } from '@/api/axiosClient';
import type { Role } from '@/types';

const validationSchema = Yup.object({
  email: Yup.string().email('Enter a valid email').required('Email is required'),
  password: Yup.string().required('Password is required'),
});

function homePathForRole(role: Role): string {
  switch (role) {
    case 'ADMIN':
      return '/admin/dashboard';
    case 'FITNESS_COACH':
      return '/coach/dashboard';
    case 'DIETICIAN':
      return '/dietician/dashboard';
    case 'CLIENT':
    default:
      return '/client/dashboard';
  }
}

function isRoleAllowedPath(path: string, role: Role): boolean {
  const roleBasePath =
    role === 'ADMIN'
      ? '/admin'
      : role === 'FITNESS_COACH'
      ? '/coach'
      : role === 'DIETICIAN'
      ? '/dietician'
      : '/client';

  return path === roleBasePath || path.startsWith(`${roleBasePath}/`);
}

export function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const loginMutation = useLoginMutation();
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();

  const formik = useFormik({
    initialValues: { email: '', password: '' },
    validationSchema,
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        const data = await loginMutation.mutateAsync(values);
        dispatch(
          setCredentials({
            accessToken: data.accessToken,
            refreshToken: data.refreshToken,
            userId: data.userId,
            firstName: data.firstName,
            lastName: data.lastName,
            email: data.email,
            role: data.role,
          })
        );
        const redirectTo = (location.state as { from?: { pathname: string } })?.from?.pathname;
        const fallbackPath = homePathForRole(data.role);
        const safeTarget =
          redirectTo && isRoleAllowedPath(redirectTo, data.role) ? redirectTo : fallbackPath;
        navigate(safeTarget, { replace: true });
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  return (
    <Box>
      <Box display="flex" flexDirection="column" alignItems="center" mb={3}>
        <Avatar sx={{ bgcolor: 'primary.main', width: 56, height: 56, mb: 1 }}>
          <FitnessCenterIcon fontSize="large" />
        </Avatar>
        <Typography variant="h5">Gym Management System</Typography>
        <Typography variant="body2" color="text.secondary">
          Sign in to continue
        </Typography>
      </Box>

      {errorMessage && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {errorMessage}
        </Alert>
      )}

      <form onSubmit={formik.handleSubmit} noValidate>
        <TextField
          fullWidth
          margin="normal"
          id="email"
          name="email"
          label="Email"
          value={formik.values.email}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={formik.touched.email && Boolean(formik.errors.email)}
          helperText={formik.touched.email && formik.errors.email}
        />
        <TextField
          fullWidth
          margin="normal"
          id="password"
          name="password"
          label="Password"
          type={showPassword ? 'text' : 'password'}
          value={formik.values.password}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={formik.touched.password && Boolean(formik.errors.password)}
          helperText={formik.touched.password && formik.errors.password}
          slotProps={{
            input: {
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton onClick={() => setShowPassword((prev) => !prev)} edge="end">
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            },
          }}
        />
        <Button
          fullWidth
          type="submit"
          variant="contained"
          size="large"
          sx={{ mt: 3 }}
          disabled={formik.isSubmitting || loginMutation.isPending}
        >
          {loginMutation.isPending ? 'Signing in...' : 'Sign In'}
        </Button>
      </form>

      <Typography variant="caption" display="block" mt={2} color="text.secondary" textAlign="center">
        Default admin: admin@gymmanagement.com / Admin@123
      </Typography>
    </Box>
  );
}
