// Shared TypeScript types mirroring backend DTOs

export type Role = 'SUPER_ADMIN' | 'ORGANIZATION_ADMIN' | 'BRANCH_MANAGER' | 'COACH' |
  'RECEPTIONIST' | 'ADMIN' | 'FITNESS_COACH' | 'DIETICIAN' | 'CLIENT';

export type EntityStatus = 'ACTIVE' | 'INACTIVE';

export interface OrganizationResponse {
  id: number;
  code: string;
  name: string;
  ownerName: string;
  contactNumber?: string;
  email: string;
  address?: string;
  status: EntityStatus;
  createdDate: string;
  updatedDate?: string;
}

export interface BranchResponse {
  id: number;
  organizationId: number;
  organizationName: string;
  branchCode: string;
  branchName: string;
  address: string;
  city: string;
  state: string;
  country: string;
  pincode: string;
  contactNumber?: string;
  email?: string;
  managerId?: number;
  managerName?: string;
  status: EntityStatus;
  createdDate: string;
  updatedDate?: string;
}

export interface BranchSettingsResponse {
  branchId: number;
  workingHours: string;
  timezone: string;
  membershipRules?: string;
  notificationPreferences?: string;
  attendanceRules?: string;
}

export interface BranchDashboardResponse {
  branchId: number;
  branchName: string;
  totalMembers: number;
  activeMembers: number;
  newMembers: number;
  expiringMemberships: number;
  dailyRevenue: number;
  monthlyRevenue: number;
  attendanceToday: number;
  activeCoaches: number;
  activeDieticians: number;
}

export interface BranchPerformanceResponse {
  branchId: number;
  branchName: string;
  members: number;
  revenue: number;
  attendance: number;
  performanceScore: number;
}

export interface OrganizationDashboardResponse {
  organizationId: number;
  organizationName: string;
  totalBranches: number;
  totalMembers: number;
  totalRevenue: number;
  branches: BranchPerformanceResponse[];
}

export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export type RegistrationType = 'REGISTERED' | 'INQUIRY';

export type ExerciseCategory =
  | 'CHEST'
  | 'BACK'
  | 'SHOULDER'
  | 'ARMS'
  | 'LEGS'
  | 'CORE'
  | 'CARDIO';

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export type SessionStatus = 'SCHEDULED' | 'COMPLETED' | 'MISSED' | 'CANCELLED';

export type MealType =
  | 'BREAKFAST'
  | 'MORNING_SNACK'
  | 'LUNCH'
  | 'EVENING_SNACK'
  | 'DINNER';

export type MembershipStatus = 'ACTIVE' | 'EXPIRED' | 'SUSPENDED';

export type NotificationType =
  | 'WORKOUT_ASSIGNED'
  | 'DIET_UPDATED'
  | 'SESSION_SCHEDULED'
  | 'MEMBERSHIP_EXPIRY'
  | 'INVOICE_CREATED'
  | 'PAYMENT_RECEIVED'
  | 'PARTIAL_PAYMENT_RECEIVED'
  | 'BALANCE_PAYMENT_COMPLETED'
  | 'PAYMENT_OVERDUE'
  | 'MEMBERSHIP_RENEWED';

export type InvoiceStatus = 'PENDING' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE' | 'CANCELLED';
export type InvoiceType = 'MEMBERSHIP' | 'MEMBERSHIP_RENEWAL' | 'PERSONAL_TRAINING' | 'SUPPLEMENT_PURCHASE';
export type PaymentMethod = 'CASH' | 'UPI' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'NET_BANKING';

export interface PaymentTransactionResponse {
  id: number;
  paidAmount: number;
  transactionType: 'PAYMENT' | 'REFUND' | 'WAIVER';
  status: string;
  paymentMethod?: PaymentMethod;
  transactionReference?: string;
  remarks?: string;
  paymentDate: string;
  createdBy?: string;
}

export interface InvoiceResponse {
  id: number;
  invoiceNumber: string;
  clientId: number;
  clientName: string;
  invoiceType: InvoiceType;
  membershipPlanId?: number;
  membershipPlanName?: string;
  membershipDiscountId?: number;
  membershipDiscountName?: string;
  membershipDiscountPercentage?: number;
  generatedMembershipId?: number;
  serviceStartDate?: string;
  sessionCount?: number;
  purchaseDate?: string;
  description: string;
  totalAmount: number;
  tax: number;
  discount: number;
  finalAmount: number;
  amountPaid: number;
  balanceAmount: number;
  status: InvoiceStatus;
  dueDate: string;
  createdAt: string;
  paymentHistory: PaymentTransactionResponse[];
}

export interface AttendanceResponse {
  id: number;
  clientId: number;
  clientName: string;
  checkInAt: string;
  checkOutAt?: string;
  durationMinutes?: number;
}

export interface AdminDashboardResponse {
  totalRevenue: number;
  monthlyRevenue: number;
  todayRevenue: number;
  outstandingPayments: number;
  overduePayments: number;
  collectionEfficiencyPercent: number;
  activeMembers: number;
  newMembers: number;
  renewals: number;
  expiringMemberships: number;
  todayAttendance: number;
  monthlyAttendance: number;
  peakUsageHours: Array<{ hour: number; checkIns: number }>;
  sessionsConducted: number;
  topPerformingCoaches: Array<{ staffId: number; name: string; completedActivities: number }>;
  topPerformingDieticians: Array<{ staffId: number; name: string; completedActivities: number }>;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface LoginResponse {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  organizationId?: number;
  branchId?: number;
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface UserResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber?: string;
  shiftStartTime?: string;
  shiftEndTime?: string;
  role: Role;
  organizationId?: number;
  organizationName?: string;
  branchId?: number;
  branchName?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ClientResponse {
  id: number;
  userId: number;
  organizationId?: number;
  branchId?: number;
  branchName?: string;
  firstName: string;
  lastName: string;
  email: string;
  active: boolean;
  membershipActive: boolean;
  membershipAssigned: boolean;
  membershipStartDate?: string;
  membershipEndDate?: string;
  registrationType: RegistrationType;
  gender?: Gender;
  dateOfBirth?: string;
  heightCm?: number;
  weightKg?: number;
  address?: string;
  contactNumber?: string;
  fitnessGoal?: string;
  diabetes: boolean;
  hypertension: boolean;
  asthma: boolean;
  allergies?: string;
  injuries?: string;
  medicalNotes?: string;
  assignedCoachId?: number;
  assignedCoachName?: string;
  assignedDieticianId?: number;
  assignedDieticianName?: string;
}

export interface CoachResponse {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber?: string;
  specialization?: string;
  experienceYears?: number;
  bio?: string;
  active: boolean;
}

export interface DieticianResponse {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber?: string;
  specialization?: string;
  experienceYears?: number;
  bio?: string;
  active: boolean;
}

export interface ExerciseResponse {
  id: number;
  name: string;
  category: ExerciseCategory;
  description?: string;
}

export interface WorkoutPlanDetailRequest {
  dayOfWeek: DayOfWeek;
  exerciseId: number;
  sets?: number;
  reps?: number;
  durationMinutes?: number;
  restTimeSeconds?: number;
  notes?: string;
}

export interface WorkoutPlanDetailResponse extends WorkoutPlanDetailRequest {
  id: number;
  exerciseName: string;
  exerciseCategory: string;
}

export interface WorkoutPlanResponse {
  id: number;
  clientId: number;
  clientName: string;
  coachId: number;
  coachName: string;
  title: string;
  description?: string;
  createdAt: string;
  details: WorkoutPlanDetailResponse[];
}

export interface SessionResponse {
  id: number;
  clientId: number;
  clientName: string;
  coachId: number;
  coachName: string;
  sessionDateTime: string;
  status: SessionStatus;
  notes?: string;
}

export interface DietPlanDetailRequest {
  mealType: MealType;
  foodItem: string;
  quantity?: string;
  calories?: number;
  mealTime?: string;
}

export interface DietPlanDetailResponse extends DietPlanDetailRequest {
  id: number;
}

export interface DietPlanResponse {
  id: number;
  clientId: number;
  clientName: string;
  dieticianId: number;
  dieticianName: string;
  title: string;
  description?: string;
  createdAt: string;
  details: DietPlanDetailResponse[];
}

export interface SupplementResponse {
  id: number;
  clientId: number;
  clientName: string;
  dieticianId: number;
  name: string;
  dosage?: string;
  timing?: string;
  instructions?: string;
}

export interface MedicineResponse {
  id: number;
  clientId: number;
  clientName: string;
  dieticianId: number;
  name: string;
  dosage?: string;
  timing?: string;
  instructions?: string;
}

export interface ProgressRecordResponse {
  id: number;
  clientId: number;
  recordDate: string;
  weightKg?: number;
  bmi?: number;
  chestCm?: number;
  waistCm?: number;
  armsCm?: number;
  shoulderCm?: number;
  thighCm?: number;
}

export interface MembershipPlanResponse {
  id: number;
  name: string;
  durationDays: number;
  fees: number;
  extraDurationDays: number;
  description?: string;
}

export interface MembershipDiscountResponse {
  id: number;
  name: string;
  percentage: number;
  extraFreeDays: number;
  description?: string;
  active: boolean;
}

export interface MembershipResponse {
  id: number;
  clientId: number;
  clientName: string;
  membershipPlanId: number;
  membershipPlanName: string;
  startDate: string;
  endDate: string;
  status: MembershipStatus;
}

export interface NotificationResponse {
  id: number;
  type: NotificationType;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export interface AskAiRequest {
  question: string;
}

export interface AskAiResponse {
  answer: string;
  model: string;
}
