import { Box, Button, Paper } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import DownloadIcon from '@mui/icons-material/Download';
import { PageHeader } from '@/components/common/PageHeader';
import { downloadInvoicePdf, useInvoicesQuery } from '@/api/billingApi';
import type { InvoiceResponse } from '@/types';

const money = (value: number) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(value);
export function MyInvoicesPage() {
  const { data, isLoading } = useInvoicesQuery(true);
  const columns: GridColDef<InvoiceResponse>[] = [{ field: 'invoiceNumber', headerName: 'Invoice', width: 190 }, { field: 'description', headerName: 'Description', flex: 1 }, { field: 'finalAmount', headerName: 'Total', width: 130, valueFormatter: (value) => money(Number(value)) }, { field: 'balanceAmount', headerName: 'Outstanding', width: 140, valueFormatter: (value) => money(Number(value)) }, { field: 'dueDate', headerName: 'Due', width: 120 }, { field: 'status', headerName: 'Status', width: 150 }, { field: 'download', headerName: '', width: 90, sortable: false, renderCell: ({ row }) => <Button aria-label="Download invoice" onClick={() => downloadInvoicePdf(row, true)}><DownloadIcon /></Button> }];
  return <Box><PageHeader title="My Invoices" subtitle="Payments, balances, and receipts" /><Paper sx={{ height: 590 }}><DataGrid rows={data?.content ?? []} columns={columns} loading={isLoading} disableRowSelectionOnClick /></Paper></Box>;
}