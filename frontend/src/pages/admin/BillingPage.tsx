import { useState } from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Paper, TextField, Typography } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import PaymentsIcon from '@mui/icons-material/Payments';
import DownloadIcon from '@mui/icons-material/Download';
import { PageHeader } from '@/components/common/PageHeader';
import { useClientsQuery } from '@/api/clientsApi';
import { downloadInvoicePdf, useCreateInvoiceMutation, useInvoicesQuery, useRecordPaymentMutation } from '@/api/billingApi';
import { useMembershipDiscountsQuery, useMembershipPlansQuery } from '@/api/membershipsApi';
import type { InvoiceResponse, InvoiceType, PaymentMethod } from '@/types';

const money = (value: number) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(value);

export function BillingPage() {
  const { data } = useInvoicesQuery();
  const { data: clients } = useClientsQuery('', 0, 100);
  const { data: membershipPlans } = useMembershipPlansQuery();
  const { data: membershipDiscounts } = useMembershipDiscountsQuery(true);
  const createInvoice = useCreateInvoiceMutation();
  const recordPayment = useRecordPaymentMutation();
  const [createOpen, setCreateOpen] = useState(false);
  const [paymentInvoice, setPaymentInvoice] = useState<InvoiceResponse | null>(null);
  const today = new Date().toISOString().slice(0, 10);
  const [form, setForm] = useState({ clientId: '', invoiceType: 'MEMBERSHIP' as InvoiceType, description: '', totalAmount: '', tax: '0', discount: '0', dueDate: '',
    membershipPlanId: '', membershipDiscountId: '', serviceStartDate: today, sessionCount: '', purchaseDate: today });
  const [initialPaymentType, setInitialPaymentType] = useState<'UNPAID' | 'FULL' | 'PARTIAL'>('UNPAID');
  const [initialPayment, setInitialPayment] = useState({ amount: '', paymentMethod: 'CASH' as PaymentMethod, transactionReference: '', remarks: '' });
  const [payment, setPayment] = useState({ amount: '', paymentMethod: 'CASH' as PaymentMethod, transactionReference: '', remarks: '' });
  const isMembership = form.invoiceType === 'MEMBERSHIP' || form.invoiceType === 'MEMBERSHIP_RENEWAL';
  const isPersonalTraining = form.invoiceType === 'PERSONAL_TRAINING';
  const isSupplement = form.invoiceType === 'SUPPLEMENT_PURCHASE';
  const selectedPlan = membershipPlans?.find((plan) => plan.id === Number(form.membershipPlanId));
  const selectedDiscount = membershipDiscounts?.find((discount) => discount.id === Number(form.membershipDiscountId));
  const registeredClients = clients?.content.filter((client) => client.registrationType === 'REGISTERED');
  const subtotal = isMembership ? selectedPlan?.fees ?? 0 : Number(form.totalAmount || 0);
  const discount = isMembership ? subtotal * (selectedDiscount?.percentage ?? 0) / 100 : Number(form.discount || 0);
  const finalAmount = subtotal + Number(form.tax || 0) - discount;
  const columns: GridColDef<InvoiceResponse>[] = [
    { field: 'invoiceNumber', headerName: 'Invoice', width: 190 }, { field: 'clientName', headerName: 'Client', minWidth: 130, flex: 1 },
    { field: 'invoiceType', headerName: 'Type', width: 170 }, { field: 'finalAmount', headerName: 'Total', width: 125, valueFormatter: (value) => money(Number(value)) },
    { field: 'balanceAmount', headerName: 'Outstanding', width: 140, valueFormatter: (value) => money(Number(value)) },
    { field: 'actions', headerName: 'Actions', width: 165, sortable: false, renderCell: ({ row }) => <Box>
      {(row.status === 'PENDING' || row.status === 'PARTIALLY_PAID') && row.balanceAmount > 0
        && <Button size="small" variant="outlined" startIcon={<PaymentsIcon />} onClick={() => { setPaymentInvoice(row); setPayment({ ...payment, amount: String(row.balanceAmount) }); }}>Pay</Button>}
      <Button size="small" aria-label="Download invoice" onClick={() => downloadInvoicePdf(row)}><DownloadIcon /></Button>
    </Box> },
    { field: 'dueDate', headerName: 'Due', width: 120 }, { field: 'status', headerName: 'Status', width: 145 },
  ];
  return <Box><PageHeader title="Billing & Invoices" subtitle="Create invoices, collect balances, and review payment history" action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)}>New Invoice</Button>} />
    <Paper sx={{ height: 590 }}><DataGrid rows={data?.content ?? []} columns={columns} loading={!data} disableRowSelectionOnClick /></Paper>
    <Dialog open={createOpen} onClose={() => setCreateOpen(false)} fullWidth maxWidth="sm"><DialogTitle>Create invoice</DialogTitle><DialogContent>
      <TextField select fullWidth margin="normal" label="Client" value={form.clientId} onChange={(e) => setForm({ ...form, clientId: e.target.value })}>{registeredClients?.map((client) => <MenuItem key={client.id} value={client.id}>{client.firstName} {client.lastName}</MenuItem>)}</TextField>
      <TextField select fullWidth margin="normal" label="Invoice type" value={form.invoiceType} onChange={(e) => setForm({ ...form, invoiceType: e.target.value as InvoiceType })}>{['MEMBERSHIP', 'MEMBERSHIP_RENEWAL', 'PERSONAL_TRAINING', 'SUPPLEMENT_PURCHASE'].map((type) => <MenuItem key={type} value={type}>{type.replaceAll('_', ' ')}</MenuItem>)}</TextField>
      {isMembership && <TextField select fullWidth margin="normal" label="Membership Plan" value={form.membershipPlanId}
        onChange={(e) => setForm({ ...form, membershipPlanId: e.target.value })}>
        {membershipPlans?.map((plan) => <MenuItem key={plan.id} value={plan.id}>{plan.name} · {plan.durationDays} days
          {plan.extraDurationDays > 0 ? ` + ${plan.extraDurationDays} free` : ''}</MenuItem>)}</TextField>}
      {isMembership && <TextField select fullWidth margin="normal" label="Discount Offer (optional)" value={form.membershipDiscountId}
        onChange={(e) => setForm({ ...form, membershipDiscountId: e.target.value })}>
        <MenuItem value="">No discount</MenuItem>
        {membershipDiscounts?.map((offer) => <MenuItem key={offer.id} value={offer.id}>
          {offer.name}{offer.percentage > 0 ? ` · ${offer.percentage}% off` : ''}{offer.extraFreeDays > 0 ? ` · +${offer.extraFreeDays} free days` : ''}
        </MenuItem>)}
      </TextField>}
      <TextField fullWidth margin="normal" label="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 2 }}><TextField margin="normal" type="number" label="Subtotal" disabled={isMembership} value={isMembership ? subtotal : form.totalAmount} onChange={(e) => setForm({ ...form, totalAmount: e.target.value })} /><TextField margin="normal" type="number" label="Tax" value={form.tax} onChange={(e) => setForm({ ...form, tax: e.target.value })} /><TextField margin="normal" type="number" label="Discount" disabled={isMembership} value={isMembership ? discount.toFixed(2) : form.discount} onChange={(e) => setForm({ ...form, discount: e.target.value })} /></Box>
      <Typography variant="body2" color="text.secondary">Invoice total: {money(finalAmount)}</Typography>
      {(isMembership || isPersonalTraining) && <TextField fullWidth margin="normal" type="date"
        label={form.invoiceType === 'MEMBERSHIP_RENEWAL' ? 'Requested Start Date (renewal begins after current expiry)' : 'Start Date'}
        slotProps={{ inputLabel: { shrink: true } }} value={form.serviceStartDate} onChange={(e) => setForm({ ...form, serviceStartDate: e.target.value })} />}
      {isPersonalTraining && <TextField fullWidth margin="normal" type="number" label="Session Count" inputProps={{ min: 1 }}
        value={form.sessionCount} onChange={(e) => setForm({ ...form, sessionCount: e.target.value })} />}
      {isSupplement && <TextField fullWidth margin="normal" type="date" label="Purchase Date"
        slotProps={{ inputLabel: { shrink: true } }} value={form.purchaseDate} onChange={(e) => setForm({ ...form, purchaseDate: e.target.value })} />}
      <TextField fullWidth margin="normal" type="date" label="Due date" slotProps={{ inputLabel: { shrink: true } }} value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
      <TextField select fullWidth margin="normal" label="Payment" value={initialPaymentType}
        onChange={(e) => setInitialPaymentType(e.target.value as 'UNPAID' | 'FULL' | 'PARTIAL')}>
        <MenuItem value="UNPAID">Unpaid</MenuItem><MenuItem value="FULL">Paid in full</MenuItem><MenuItem value="PARTIAL">Partial payment</MenuItem>
      </TextField>
      {initialPaymentType !== 'UNPAID' && <>
        {initialPaymentType === 'PARTIAL' && <TextField fullWidth margin="normal" type="number" label="Amount Paid" inputProps={{ min: 0.01, max: finalAmount, step: 0.01 }}
          value={initialPayment.amount} onChange={(e) => setInitialPayment({ ...initialPayment, amount: e.target.value })} />}
        <TextField select fullWidth margin="normal" label="Payment Method" value={initialPayment.paymentMethod}
          onChange={(e) => setInitialPayment({ ...initialPayment, paymentMethod: e.target.value as PaymentMethod })}>
          {['CASH', 'UPI', 'CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING'].map((method) => <MenuItem key={method} value={method}>{method.replaceAll('_', ' ')}</MenuItem>)}
        </TextField>
        <TextField fullWidth margin="normal" label="Transaction Reference" value={initialPayment.transactionReference}
          onChange={(e) => setInitialPayment({ ...initialPayment, transactionReference: e.target.value })} />
        <TextField fullWidth margin="normal" label="Payment Remarks" value={initialPayment.remarks}
          onChange={(e) => setInitialPayment({ ...initialPayment, remarks: e.target.value })} />
      </>}
    </DialogContent><DialogActions><Button onClick={() => setCreateOpen(false)}>Cancel</Button><Button variant="contained"
      disabled={!form.clientId || !form.description || !form.dueDate || (isMembership ? !form.membershipPlanId || !form.serviceStartDate : !form.totalAmount)
        || (isPersonalTraining && (!form.serviceStartDate || !form.sessionCount)) || (isSupplement && !form.purchaseDate)
        || (initialPaymentType === 'PARTIAL' && (!initialPayment.amount || Number(initialPayment.amount) <= 0 || Number(initialPayment.amount) >= finalAmount))
        || createInvoice.isPending}
      onClick={async () => { await createInvoice.mutateAsync({ clientId: Number(form.clientId), invoiceType: form.invoiceType, description: form.description,
        totalAmount: isMembership ? undefined : Number(form.totalAmount), tax: Number(form.tax), discount: isMembership ? discount : Number(form.discount), dueDate: form.dueDate,
        membershipPlanId: isMembership ? Number(form.membershipPlanId) : undefined,
        membershipDiscountId: isMembership && form.membershipDiscountId ? Number(form.membershipDiscountId) : undefined,
        serviceStartDate: isMembership || isPersonalTraining ? form.serviceStartDate : undefined,
        sessionCount: isPersonalTraining ? Number(form.sessionCount) : undefined, purchaseDate: isSupplement ? form.purchaseDate : undefined,
        initialPaymentAmount: initialPaymentType === 'FULL' ? finalAmount : initialPaymentType === 'PARTIAL' ? Number(initialPayment.amount) : undefined,
        paymentMethod: initialPaymentType !== 'UNPAID' ? initialPayment.paymentMethod : undefined,
        transactionReference: initialPayment.transactionReference || undefined, paymentRemarks: initialPayment.remarks || undefined }); setCreateOpen(false); }}>Create</Button></DialogActions></Dialog>
    <Dialog open={!!paymentInvoice} onClose={() => setPaymentInvoice(null)} fullWidth maxWidth="xs"><DialogTitle>Record payment</DialogTitle><DialogContent>
      <Typography variant="body2" color="text.secondary">Outstanding: {money(paymentInvoice?.balanceAmount ?? 0)}</Typography>
      <TextField fullWidth margin="normal" type="number" label="Amount" value={payment.amount} onChange={(e) => setPayment({ ...payment, amount: e.target.value })} />
      <TextField select fullWidth margin="normal" label="Method" value={payment.paymentMethod} onChange={(e) => setPayment({ ...payment, paymentMethod: e.target.value as PaymentMethod })}>{['CASH', 'UPI', 'CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING'].map((method) => <MenuItem key={method} value={method}>{method.replaceAll('_', ' ')}</MenuItem>)}</TextField>
      <TextField fullWidth margin="normal" label="Transaction reference" value={payment.transactionReference} onChange={(e) => setPayment({ ...payment, transactionReference: e.target.value })} /><TextField fullWidth margin="normal" label="Remarks" value={payment.remarks} onChange={(e) => setPayment({ ...payment, remarks: e.target.value })} />
    </DialogContent><DialogActions><Button onClick={() => setPaymentInvoice(null)}>Cancel</Button><Button variant="contained" disabled={!payment.amount || recordPayment.isPending} onClick={async () => { if (!paymentInvoice) return; await recordPayment.mutateAsync({ invoiceId: paymentInvoice.id, ...payment, amount: Number(payment.amount) }); setPaymentInvoice(null); }}>Record</Button></DialogActions></Dialog>
  </Box>;
}