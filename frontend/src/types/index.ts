// Shared TypeScript types mirroring backend DTOs

export type Role = 'ADMIN' | 'FITNESS_COACH' | 'DIETICIAN' | 'CLIENT';

export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

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

export type MembershipStatus = 'ACTIVE' | 'EXPIRED';

export type NotificationType =
  | 'WORKOUT_ASSIGNED'
  | 'DIET_UPDATED'
  | 'SESSION_SCHEDULED'
  | 'MEMBERSHIP_EXPIRY';

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
  role: Role;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ClientResponse {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  active: boolean;
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
  description?: string;
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
