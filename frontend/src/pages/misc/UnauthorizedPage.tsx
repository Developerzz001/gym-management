import { Box, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';

export function UnauthorizedPage() {
  const navigate = useNavigate();
  return (
    <Box display="flex" flexDirection="column" alignItems="center" justifyContent="center" minHeight="100vh" gap={2}>
      <Typography variant="h3">403</Typography>
      <Typography variant="h6">You do not have permission to view this page</Typography>
      <Button variant="contained" onClick={() => navigate(-1)}>Go Back</Button>
    </Box>
  );
}
