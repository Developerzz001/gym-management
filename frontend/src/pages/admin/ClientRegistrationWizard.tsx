import { useEffect, useMemo, useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem,
  Typography, Alert, Avatar, Stepper, Step, StepLabel, Divider,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import { useNextMemberCodeQuery, useUploadClientProfileImageMutation } from '@/api/clientsApi';
import { useRegisterClientWithEnrollmentMutation, type ClientEnrollmentRequest } from '@/api/enrollmentApi';
import { useActivitiesQuery, useMembershipDiscountsQuery, useMembershipPlansQuery } from '@/api/membershipsApi';
import { useReceptionistsQuery } from '@/api/usersApi';
import { downloadInvoicePdf } from '@/api/billingApi';
import { extractErrorMessage } from '@/api/axiosClient';
import { PhotoCapture } from '@/components/common/PhotoCapture';
import { useAppSelector } from '@/app/hooks';
import type { EnrollmentResponse, Gender, PaymentMethod } from '@/types';

interface ClientRegistrationWizardProps {
  open: boolean;
  onClose: () => void;
}

interface WizardValues {
  firstName: string;
  lastName: string;
  email: string;
  contactNumber: string;
  alternateContactNumber: string;
  gender: Gender | '';
  dateOfBirth: string;
  address: string;
  executiveId: number | '';
  fitnessGoal: string;
  heightCm: number | '';
  weightKg: number | '';
  allergies: string;
  injuries: string;
  medicalNotes: string;
  receiptNumber: string;
  receiptDate: string;
  activityId: number | '';
  membershipPlanId: number | '';
  startDate: string;
  membershipDiscountId: number | '';
  taxPercent: number;
  description: string;
  paymentType: 'FULL' | 'PARTIAL';
  amountPaid: number;
  paymentMethod: PaymentMethod | '';
  transactionReference: string;
  remarks: string;
}

const STEP1_FIELDS: Array<keyof WizardValues> = ['firstName', 'lastName', 'contactNumber', 'gender'];
const REFERENCE_REQUIRED_METHODS: PaymentMethod[] = ['UPI', 'CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING'];

export function ClientRegistrationWizard({ open, onClose }: ClientRegistrationWizardProps) {
  const [activeStep, setActiveStep] = useState(0);
  const [photoFile, setPhotoFile] = useState<File | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successResult, setSuccessResult] = useState<EnrollmentResponse | null>(null);
  const [bmiResult, setBmiResult] = useState<number | null>(null);

  const { userId: currentUserId, firstName: currentFirstName, lastName: currentLastName } = useAppSelector((state) => state.auth);
  const { data: memberCode } = useNextMemberCodeQuery(open);
  const { data: receptionists } = useReceptionistsQuery();
  const { data: activities } = useActivitiesQuery();
  const { data: discounts } = useMembershipDiscountsQuery(true);
  const enrollMutation = useRegisterClientWithEnrollmentMutation();
  const uploadImageMutation = useUploadClientProfileImageMutation();

  const formik = useFormik<WizardValues>({
    initialValues: {
      firstName: '', lastName: '', email: '', contactNumber: '', alternateContactNumber: '',
      gender: '', dateOfBirth: '', address: '', executiveId: currentUserId ?? '', fitnessGoal: '',
      heightCm: '', weightKg: '', allergies: '', injuries: '', medicalNotes: '',
      receiptNumber: '', receiptDate: new Date().toLocaleDateString('en-CA'),
      activityId: '', membershipPlanId: '', startDate: new Date().toLocaleDateString('en-CA'),
      membershipDiscountId: '', taxPercent: 0, description: '',
      paymentType: 'FULL', amountPaid: 0, paymentMethod: '', transactionReference: '', remarks: '',
    },
    validationSchema: Yup.object({
      firstName: Yup.string().required('First name is required'),
      lastName: Yup.string().required('Last name is required'),
      contactNumber: Yup.string().required('Mobile number is required'),
      gender: Yup.string().required('Gender is required'),
      activityId: Yup.number().required('Sport / Activity is required'),
      membershipPlanId: Yup.number().required('Membership plan is required'),
      startDate: Yup.string().required('Start date is required'),
      paymentMethod: Yup.string().required('Payment method is required'),
      amountPaid: Yup.number().positive('Amount paid must be greater than zero').required('Amount paid is required'),
      transactionReference: Yup.string().when('paymentMethod', {
        is: (method: string) => REFERENCE_REQUIRED_METHODS.includes(method as PaymentMethod),
        then: (schema) => schema.required('Transaction reference is required for this payment method'),
      }),
    }),
    onSubmit: async (values) => {
      setErrorMessage(null);
      try {
        const payload: ClientEnrollmentRequest = {
          firstName: values.firstName,
          lastName: values.lastName,
          email: values.email || undefined,
          contactNumber: values.contactNumber,
          alternateContactNumber: values.alternateContactNumber || undefined,
          gender: values.gender as Gender,
          dateOfBirth: values.dateOfBirth || undefined,
          address: values.address || undefined,
          executiveId: values.executiveId === '' ? undefined : Number(values.executiveId),
          fitnessGoal: values.fitnessGoal || undefined,
          heightCm: values.heightCm === '' ? undefined : Number(values.heightCm),
          weightKg: values.weightKg === '' ? undefined : Number(values.weightKg),
          allergies: values.allergies || undefined,
          injuries: values.injuries || undefined,
          medicalNotes: values.medicalNotes || undefined,
          activityId: Number(values.activityId),
          membershipPlanId: Number(values.membershipPlanId),
          startDate: values.startDate,
          membershipDiscountId: values.membershipDiscountId === '' ? undefined : Number(values.membershipDiscountId),
          tax: Number(taxAmount.toFixed(2)),
          description: values.description || undefined,
          amountPaid: Number(values.amountPaid),
          paymentMethod: values.paymentMethod as PaymentMethod,
          transactionReference: values.transactionReference || undefined,
          remarks: [
            values.receiptNumber && `Receipt No: ${values.receiptNumber}`,
            values.receiptDate && `Receipt Date: ${values.receiptDate}`,
            values.remarks,
          ].filter(Boolean).join(' | ') || undefined,
        };
        const result = await enrollMutation.mutateAsync(payload);
        if (photoFile) {
          await uploadImageMutation.mutateAsync({ id: result.client.id, file: photoFile });
        }
        setSuccessResult(result);
      } catch (error) {
        setErrorMessage(extractErrorMessage(error));
      }
    },
  });

  useEffect(() => {
    if (open) {
      setActiveStep(0);
      setPhotoFile(null);
      setErrorMessage(null);
      setSuccessResult(null);
      formik.resetForm();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  const { data: plans } = useMembershipPlansQuery(
    formik.values.activityId === '' ? undefined : Number(formik.values.activityId)
  );

  const selectedPlan = plans?.find((p) => p.id === formik.values.membershipPlanId);
  const selectedDiscount = discounts?.find((d) => d.id === formik.values.membershipDiscountId);

  const subtotal = selectedPlan?.fees ?? 0;
  const taxAmount = useMemo(() => (subtotal * (formik.values.taxPercent || 0)) / 100, [subtotal, formik.values.taxPercent]);
  const discountAmount = selectedDiscount ? (subtotal * Number(selectedDiscount.percentage)) / 100 : 0;
  const finalAmount = Math.max(0, subtotal + taxAmount - discountAmount);
  const pendingAmount = Math.max(0, finalAmount - (formik.values.amountPaid || 0));

  const endDatePreview = useMemo(() => {
    if (!selectedPlan || !formik.values.startDate) return '';
    const durationDays = selectedPlan.durationDays + selectedPlan.extraDurationDays + (selectedDiscount?.extraFreeDays ?? 0);
    const end = new Date(formik.values.startDate);
    end.setDate(end.getDate() + durationDays);
    return end.toLocaleDateString('en-CA');
  }, [selectedPlan, selectedDiscount, formik.values.startDate]);

  useEffect(() => {
    if (formik.values.paymentType === 'FULL') {
      formik.setFieldValue('amountPaid', Number(finalAmount.toFixed(2)));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [formik.values.paymentType, finalAmount]);

  const handleNext = async () => {
    const errors = await formik.validateForm();
    const touched: Record<string, boolean> = {};
    STEP1_FIELDS.forEach((field) => { touched[field] = true; });
    formik.setTouched({ ...formik.touched, ...touched });
    const hasStepError = STEP1_FIELDS.some((field) => !!errors[field]);
    if (!hasStepError) setActiveStep(1);
  };

  const handleAnalyseDetails = () => {
    const weight = Number(formik.values.weightKg);
    const heightCm = Number(formik.values.heightCm);
    if (!weight || !heightCm) {
      setBmiResult(null);
      return;
    }
    const heightM = heightCm / 100;
    setBmiResult(Number((weight / (heightM * heightM)).toFixed(1)));
  };

  const isSubmitting = enrollMutation.isPending || uploadImageMutation.isPending;

  if (successResult) {
    return (
      <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
        <DialogTitle>Success</DialogTitle>
        <DialogContent>
          <Alert severity="success">Client created successfully.</Alert>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Member ID: <strong>{successResult.client.memberCode}</strong>
          </Typography>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Close</Button>
          <Button variant="contained" startIcon={<ReceiptLongIcon />} onClick={() => downloadInvoicePdf(successResult.invoice)}>
            Print Receipt
          </Button>
        </DialogActions>
      </Dialog>
    );
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xl" fullWidth>
      <DialogTitle>Register New Client - Step {activeStep + 1}</DialogTitle>
      <Stepper activeStep={activeStep} sx={{ px: 3, pb: 2 }}>
        <Step><StepLabel>Client Information</StepLabel></Step>
        <Step><StepLabel>Membership Enrollment</StepLabel></Step>
      </Stepper>
      <form onSubmit={formik.handleSubmit}>
        <DialogContent dividers>
          {errorMessage && <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>}

          {activeStep === 0 ? (
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 9 }}>
                <Grid container spacing={1.5}>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" type="date" label="Registration Date" value={new Date().toLocaleDateString('en-CA')}
                      InputLabelProps={{ shrink: true }} disabled />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Member Id" value={memberCode ?? ''} InputProps={{ readOnly: true }} disabled />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="First Name" name="firstName" value={formik.values.firstName}
                      onChange={formik.handleChange} error={formik.touched.firstName && !!formik.errors.firstName}
                      helperText={formik.touched.firstName && formik.errors.firstName} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Last Name" name="lastName" value={formik.values.lastName}
                      onChange={formik.handleChange} error={formik.touched.lastName && !!formik.errors.lastName}
                      helperText={formik.touched.lastName && formik.errors.lastName} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Mobile Number" name="contactNumber" value={formik.values.contactNumber}
                      onChange={formik.handleChange} error={formik.touched.contactNumber && !!formik.errors.contactNumber}
                      helperText={formik.touched.contactNumber && formik.errors.contactNumber} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Alternate Mobile Number" name="alternateContactNumber"
                      value={formik.values.alternateContactNumber} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Email" name="email" value={formik.values.email} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField select fullWidth size="small" label="Gender" name="gender" value={formik.values.gender}
                      onChange={formik.handleChange} error={formik.touched.gender && !!formik.errors.gender}
                      helperText={formik.touched.gender && formik.errors.gender}>
                      <MenuItem value="MALE">Male</MenuItem>
                      <MenuItem value="FEMALE">Female</MenuItem>
                      <MenuItem value="OTHER">Other</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" type="date" label="Date of Birth" name="dateOfBirth" InputLabelProps={{ shrink: true }}
                      value={formik.values.dateOfBirth} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField select fullWidth size="small" label="Executive" name="executiveId" value={formik.values.executiveId}
                      onChange={formik.handleChange}>
                      {currentUserId && !receptionists?.some((r) => r.id === currentUserId) && (
                        <MenuItem value={currentUserId}>{[currentFirstName, currentLastName].filter(Boolean).join(' ')} (You)</MenuItem>
                      )}
                      {receptionists?.map((r) => (
                        <MenuItem key={r.id} value={r.id}>{r.firstName} {r.lastName}{r.id === currentUserId ? ' (You)' : ''}</MenuItem>
                      ))}
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 8 }}>
                    <TextField fullWidth size="small" label="Goal" name="fitnessGoal" value={formik.values.fitnessGoal} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={12}>
                    <TextField fullWidth size="small" label="Address" name="address" value={formik.values.address} onChange={formik.handleChange} />
                  </Grid>

                  <Grid size={12}><Divider sx={{ my: 1 }} /><Typography variant="subtitle2" color="text.secondary">Medical Information</Typography></Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Allergies" name="allergies" value={formik.values.allergies} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Injuries" name="injuries" value={formik.values.injuries} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField fullWidth size="small" label="Medical Notes" name="medicalNotes" value={formik.values.medicalNotes} onChange={formik.handleChange} />
                  </Grid>
                </Grid>
              </Grid>
              <Grid size={{ xs: 12, md: 3 }} sx={{ pl: { md: 2 }, mt: { xs: 2, md: 0 }, borderLeft: { md: '1px solid' }, borderColor: { md: 'divider' } }}>
                <PhotoCapture file={photoFile} onChange={setPhotoFile} disabled={isSubmitting} height={180} />
                <Grid container spacing={1.5} sx={{ mt: 0.5 }}>
                  <Grid size={6}>
                    <TextField fullWidth size="small" type="number" label="Weight (kg)" name="weightKg" value={formik.values.weightKg}
                      onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={6}>
                    <TextField fullWidth size="small" type="number" label="Height (cm)" name="heightCm" value={formik.values.heightCm}
                      onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={12}>
                    <Button fullWidth variant="outlined" onClick={handleAnalyseDetails}>Analyse Details</Button>
                  </Grid>
                  {bmiResult !== null && (
                    <Grid size={12}>
                      <Typography variant="body2" color="text.secondary" textAlign="center">
                        BMI: <strong>{bmiResult}</strong>
                      </Typography>
                    </Grid>
                  )}
                </Grid>
              </Grid>
            </Grid>
          ) : (
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 7 }} sx={{ pr: { md: 3 } }}>
                <Grid container spacing={1.5} alignItems="center">
                  <Grid size={{ xs: 12, sm: 3 }} sx={{ textAlign: 'center' }}>
                    <Avatar src={photoFile ? URL.createObjectURL(photoFile) : undefined} sx={{ width: 96, height: 96, mx: 'auto' }} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <Typography variant="body2" color="text.secondary">Member Id</Typography>
                    <Typography variant="subtitle2">{memberCode}</Typography>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <Typography variant="body2" color="text.secondary">Name</Typography>
                    <Typography variant="subtitle2">{formik.values.firstName} {formik.values.lastName}</Typography>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <Typography variant="body2" color="text.secondary">Contact No</Typography>
                    <Typography variant="subtitle2">{formik.values.contactNumber}</Typography>
                  </Grid>

                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField fullWidth size="small" label="Receipt No" name="receiptNumber" value={formik.values.receiptNumber}
                      onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField fullWidth size="small" type="date" label="Receipt Date" name="receiptDate" InputLabelProps={{ shrink: true }}
                      value={formik.values.receiptDate} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField select fullWidth size="small" label="Sport / Activity" name="activityId" value={formik.values.activityId}
                      onChange={(event) => { formik.setFieldValue('activityId', event.target.value); formik.setFieldValue('membershipPlanId', ''); }}
                      error={formik.touched.activityId && !!formik.errors.activityId} helperText={formik.touched.activityId && formik.errors.activityId}>
                      {activities?.map((activity) => <MenuItem key={activity.id} value={activity.id}>{activity.name}</MenuItem>)}
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField select fullWidth size="small" label="Membership Plan" name="membershipPlanId" value={formik.values.membershipPlanId}
                      onChange={formik.handleChange} disabled={!formik.values.activityId}
                      error={formik.touched.membershipPlanId && !!formik.errors.membershipPlanId}
                      helperText={formik.touched.membershipPlanId && formik.errors.membershipPlanId}>
                      {plans?.map((plan) => <MenuItem key={plan.id} value={plan.id}>{plan.name} (₹{plan.fees})</MenuItem>)}
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField fullWidth size="small" type="date" label="Start Date" name="startDate" InputLabelProps={{ shrink: true }}
                      value={formik.values.startDate} onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField fullWidth size="small" type="date" label="End Date" value={endDatePreview} InputLabelProps={{ shrink: true }}
                      InputProps={{ readOnly: true }} disabled />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField select fullWidth size="small" label="Discount Offer (optional)" name="membershipDiscountId"
                      value={formik.values.membershipDiscountId} onChange={formik.handleChange}>
                      <MenuItem value="">-- None --</MenuItem>
                      {discounts?.map((discount) => <MenuItem key={discount.id} value={discount.id}>{discount.name}</MenuItem>)}
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField fullWidth size="small" label="Description" name="description" value={formik.values.description} onChange={formik.handleChange} />
                  </Grid>
                </Grid>
              </Grid>

              <Grid
                size={{ xs: 12, md: 5 }}
                sx={{ pl: { md: 3 }, mt: { xs: 2, md: 0 }, borderLeft: { md: '1px solid' }, borderColor: { md: 'divider' } }}
              >
                <Grid container spacing={1.5}>
                  <Grid size={4}>
                    <TextField fullWidth size="small" label="Subtotal" value={subtotal} InputProps={{ readOnly: true }} disabled />
                  </Grid>
                  <Grid size={4}>
                    <TextField fullWidth size="small" type="number" label="Tax (%)" name="taxPercent" value={formik.values.taxPercent}
                      onChange={formik.handleChange} />
                  </Grid>
                  <Grid size={4}>
                    <TextField fullWidth size="small" label="Discount" value={discountAmount.toFixed(2)} InputProps={{ readOnly: true }} disabled />
                  </Grid>
                  <Grid size={12}>
                    <Typography variant="body2" color="text.secondary">Invoice total: ₹{finalAmount.toFixed(2)}</Typography>
                  </Grid>

                  <Grid size={12}>
                    <TextField select fullWidth size="small" label="Payment" value={formik.values.paymentType}
                      onChange={(event) => formik.setFieldValue('paymentType', event.target.value)}>
                      <MenuItem value="FULL">Full payment</MenuItem>
                      <MenuItem value="PARTIAL">Partial payment</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={12}>
                    <TextField fullWidth size="small" type="number" label="Amount Paid" name="amountPaid" value={formik.values.amountPaid}
                      onChange={formik.handleChange} disabled={formik.values.paymentType === 'FULL'}
                      error={formik.touched.amountPaid && !!formik.errors.amountPaid}
                      helperText={(formik.touched.amountPaid && formik.errors.amountPaid) || `Pending: ₹${pendingAmount.toFixed(2)}`} />
                  </Grid>
                  <Grid size={12}>
                    <TextField select fullWidth size="small" label="Payment Method" name="paymentMethod" value={formik.values.paymentMethod}
                      onChange={formik.handleChange} error={formik.touched.paymentMethod && !!formik.errors.paymentMethod}
                      helperText={formik.touched.paymentMethod && formik.errors.paymentMethod}>
                      <MenuItem value="CASH">Cash</MenuItem>
                      <MenuItem value="UPI">UPI</MenuItem>
                      <MenuItem value="CREDIT_CARD">Credit Card</MenuItem>
                      <MenuItem value="DEBIT_CARD">Debit Card</MenuItem>
                      <MenuItem value="NET_BANKING">Bank Transfer</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={12}>
                    <TextField fullWidth size="small" label="Transaction Reference" name="transactionReference"
                      value={formik.values.transactionReference} onChange={formik.handleChange}
                      error={formik.touched.transactionReference && !!formik.errors.transactionReference}
                      helperText={formik.touched.transactionReference && formik.errors.transactionReference} />
                  </Grid>
                  <Grid size={12}>
                    <TextField fullWidth size="small" label="Remarks" name="remarks" value={formik.values.remarks} onChange={formik.handleChange} />
                  </Grid>
                </Grid>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          {activeStep === 0 ? (
            <Button variant="contained" onClick={handleNext}>Next</Button>
          ) : (
            <>
              <Button onClick={() => setActiveStep(0)}>Back</Button>
              <Button type="submit" variant="contained" disabled={isSubmitting}>Submit</Button>
            </>
          )}
        </DialogActions>
      </form>
    </Dialog>
  );
}
