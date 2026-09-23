import { useState } from 'react';
import { Box, Button, MenuItem, Paper, TextField } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import LoginIcon from '@mui/icons-material/Login';
import LogoutIcon from '@mui/icons-material/Logout';
import { PageHeader } from '@/components/common/PageHeader';
import { useClientsQuery } from '@/api/clientsApi';
import { useAttendanceActionMutation, useAttendanceReportQuery } from '@/api/attendanceApi';
import type { AttendanceResponse } from '@/types';

const today = new Date().toISOString().slice(0, 10);
export function AttendancePage() {
  const [from, setFrom] = useState(today); const [to, setTo] = useState(today); const [clientId, setClientId] = useState<number | ''>('');
  const { data: clients } = useClientsQuery('', 0, 100); const { data, isLoading } = useAttendanceReportQuery(from, to);
  const checkIn = useAttendanceActionMutation('check-in'); const checkOut = useAttendanceActionMutation('check-out');
  const columns: GridColDef<AttendanceResponse>[] = [{ field: 'clientName', headerName: 'Member', flex: 1 }, { field: 'checkInAt', headerName: 'Check in', width: 210, valueFormatter: (value) => new Date(value).toLocaleString() }, { field: 'checkOutAt', headerName: 'Check out', width: 210, valueFormatter: (value) => value ? new Date(value).toLocaleString() : 'Active' }, { field: 'durationMinutes', headerName: 'Duration (min)', width: 140 }];
  return <Box><PageHeader title="Attendance" subtitle="Check members in and out, and review usage" /><Paper sx={{ p: 2, mb: 2, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
    <TextField select size="small" label="Member" sx={{ minWidth: 260 }} value={clientId} onChange={(e) => setClientId(Number(e.target.value))}>{clients?.content.map((client) => <MenuItem key={client.id} value={client.id}>{client.firstName} {client.lastName}</MenuItem>)}</TextField>
    <Button variant="contained" startIcon={<LoginIcon />} disabled={!clientId || checkIn.isPending} onClick={() => clientId && checkIn.mutate(clientId)}>Check In</Button><Button variant="outlined" startIcon={<LogoutIcon />} disabled={!clientId || checkOut.isPending} onClick={() => clientId && checkOut.mutate(clientId)}>Check Out</Button><Box sx={{ flexGrow: 1 }} />
    <TextField size="small" type="date" label="From" slotProps={{ inputLabel: { shrink: true } }} value={from} onChange={(e) => setFrom(e.target.value)} /><TextField size="small" type="date" label="To" slotProps={{ inputLabel: { shrink: true } }} value={to} onChange={(e) => setTo(e.target.value)} />
  </Paper><Paper sx={{ height: 540 }}><DataGrid rows={data ?? []} columns={columns} loading={isLoading} /></Paper></Box>;
}