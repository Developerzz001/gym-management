# Gym Management System

A full-stack, role-based gym operations platform with member and staff management, memberships,
billing, attendance, coaching, nutrition, reporting, notifications, and multi-branch tenancy.

- **Backend**: Java 21, Spring Boot 3, Spring Security (JWT + Refresh Tokens, RBAC), Spring Data JPA
  (Hibernate), PostgreSQL, MapStruct, Lombok, Bean Validation, springdoc-openapi (Swagger UI), Maven.
- **Frontend**: React 18, TypeScript 6, Vite 5, Material UI 6, Redux Toolkit, TanStack React Query,
  Axios, React Router 7, Formik + Yup.
- **Database**: PostgreSQL.

For the code-grounded architecture, module inventory, end-to-end flows, current limitations, and
recommended product roadmap, see [Project Documentation](docs/PROJECT_DOCUMENTATION.md).

## Project Structure

```
gym-management/
├── backend/   # Spring Boot REST API (com.gymmanagement)
├── frontend/  # React + TypeScript SPA
└── docs/      # Architecture, rollout notes, and reviewed SQL scripts
```

## Prerequisites

| Tool             | Version   |
|------------------|-----------|
| JDK              | 21        |
| Maven            | 3.9+      |
| Node.js          | 18+ (20 recommended) |
| PostgreSQL       | 15+       |

## 1. Database Setup

Run PostgreSQL locally, e.g. via Docker:

```powershell
docker run --name gym-postgres `
  -e POSTGRES_DB=gym_management `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=12345 `
  -p 5432:5432 -d postgres:16
```

The backend uses Hibernate `ddl-auto: update`, so the core, billing, attendance, notification,
organization, and branch tables are created automatically for a fresh local database. Existing
databases should review and apply [Phase 2 SQL](docs/sql/phase2.sql) and
[Phase 2.1 multi-branch SQL](docs/sql/phase2_1_multi_branch.sql), including the documented tenant
backfill, before production deployment. Production should use versioned migrations and
`ddl-auto: validate`.

Connection settings are in [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml).
Override via environment variables or edit directly:

```yaml
spring.datasource.url: jdbc:postgresql://localhost:5432/gym_management
spring.datasource.username: postgres
spring.datasource.password: 12345
```

## 2. Run the Backend

```powershell
cd backend
mvn spring-boot:run
```

The API starts on **http://localhost:8080/api**.

- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

On first startup, only the default **SUPER_ADMIN** account is seeded automatically:

```
SUPER_ADMIN: superadmin@gymmanagement.com / SuperAdmin@123
```

Use `SUPER_ADMIN` to create organizations. Organization codes are generated automatically in
sequence (`ORG_01`, `ORG_02`, and so on), and the supplied organization email and initial password
become its `ORGANIZATION_ADMIN` login. Legacy `ADMIN` accounts can still be created through the
user management API for backward-compatible gym operations screens.

## 3. Run the Frontend

```powershell
cd frontend
npm install
npm run dev
```

The app starts on **http://localhost:5173** and proxies `/api/*` requests to the backend
(`http://localhost:8080`) — see `frontend/vite.config.ts`.

## 4. Typical Local Workflow

1. Log in as `superadmin@gymmanagement.com` and create an organization. The organization email and
  initial password can then be used to log in as its `ORGANIZATION_ADMIN`.
2. As the organization admin, create branches. Branch codes are generated per organization in
  sequence (`BR_01`, `BR_02`, and so on).
3. From Branch Managers, create one `BRANCH_MANAGER` login for each branch.
4. As a branch manager, create `COACH`, `DIETICIAN`, and `RECEPTIONIST` logins for that branch.
5. Organization admins and branch managers can transfer members and staff within their
  organization. Super admins can view all organizations, branches, dashboards, and reports but
  cannot perform transfers.

The multi-branch implementation supports platform-scoped `SUPER_ADMIN`, `ORGANIZATION_ADMIN`,
`BRANCH_MANAGER`, `RECEPTIONIST`, and `COACH` users. Tenant users require organization/branch scope
data; only the platform-level `SUPER_ADMIN` is seeded automatically.

## Authentication Flow

- `POST /api/v1/auth/login` — returns a short-lived JWT access token (15 min) and a longer-lived
  refresh token (7 days, stored server-side in `refresh_tokens`).
- `POST /api/v1/auth/refresh-token` — exchanges a valid refresh token for a new access token.
  The frontend Axios client automatically retries a request once with a refreshed token on `401`.
- `POST /api/v1/auth/logout` — revokes the given refresh token.
- `POST /api/v1/auth/change-password` — changes the authenticated user's password.

Roles: `SUPER_ADMIN`, `ORGANIZATION_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `COACH`, `DIETICIAN`,
and `CLIENT`. Legacy `ADMIN` and `FITNESS_COACH` roles remain supported during migration. Backend
method checks and tenant-aware service/repository filters are the authorization boundary.

## Backend Package Structure

```
com.gymmanagement
├── auth            # login/refresh/logout/change-password
├── security         # JWT filter, JwtService, UserDetails, SecurityConfig
├── user             # User entity/roles, admin user CRUD
├── admin            # cross-module assignment endpoints (assign/change coach & dietician)
├── organization     # organization lifecycle and tenant ownership
├── branch / audit   # branch settings, transfers, and branch audit history
├── coach / dietician # profile CRUD (creates linked User with role)
├── client            # client profile + medical details
├── workout           # Exercise master, WorkoutPlan/Detail, TrainingSession
├── diet              # DietPlan/Detail
├── supplement / medicine
├── progress          # date-wise body metrics, BMI auto-calculated
├── membership        # MembershipPlan, Membership (assign/renew)
├── billing           # invoices, payments, waivers, refunds, PDF/email delivery
├── attendance        # check-in/out and usage reports
├── dashboard         # admin, branch, and organization aggregates
├── notification      # in-app notification records
└── common            # BaseEntity, exceptions, ApiResponse/PageResponse, config (security, swagger, auditing)
```

## Implemented Operations

- **Billing:** invoice creation, partial/full payments, balance waivers, refunds, audit history,
  invoice PDF generation, optional email delivery, and client-owned invoice views.
- **Attendance:** membership-aware check-in/check-out, one active visit per member, attendance
  history, usage summaries, and peak-hour reporting.
- **Dashboards:** server-side admin, branch, and organization KPI aggregates.
- **Multi-branch:** organization/branch management, branch settings, tenant-scoped access, member and
  staff transfers, immutable branch snapshots on operational records, and branch reports/exports.

See [Project Documentation](docs/PROJECT_DOCUMENTATION.md),
[Phase 2](docs/sql/phase2.sql), and [Phase 2.1](docs/PHASE_2_1_MULTI_BRANCH.md) for API contracts and
rollout details.

## Notes on Local-Dev Optimizations

- CORS is pre-configured for `http://localhost:5173`.
- JWT secret / token TTLs are configurable via `application.yml` (`app.jwt.*`).
- `spring.jpa.hibernate.ddl-auto=update` avoids the need for manual migrations locally.

## WhatsApp Notifications and Reminders

The backend now supports WhatsApp notifications for:

- New workout plan assigned by coach -> client gets WhatsApp message.
- New diet plan assigned by dietician -> client gets WhatsApp message.
- Diet reminders at meal times (`BREAKFAST`, `MORNING_SNACK`, `LUNCH`, `EVENING_SNACK`, `DINNER`).
- Daily coach reminder to add missing workout plans for assigned clients.
- Daily dietician reminder to add missing diet plans for assigned clients.

Diet plan meal entries now support an optional `mealTime` (`HH:mm`).
If `mealTime` is not provided, defaults are used:

- BREAKFAST 09:00
- MORNING_SNACK 11:00
- LUNCH 14:00
- EVENING_SNACK 17:00
- DINNER 20:00

### Configure provider

The checked-in configuration enables the Twilio provider but leaves credentials empty. For local
development without external delivery, set `app.whatsapp.provider: log`; use environment variables
for real provider credentials.

Set these in `backend/src/main/resources/application.yml` or environment variables:

```yaml
app.whatsapp.enabled: true
app.whatsapp.provider: twilio
app.whatsapp.default-country-code: +91
app.whatsapp.twilio.account-sid: ${TWILIO_ACCOUNT_SID}
app.whatsapp.twilio.auth-token: ${TWILIO_AUTH_TOKEN}
app.whatsapp.twilio.from-number: ${TWILIO_WHATSAPP_FROM}
```

Reminder scheduler defaults:

```yaml
app.notifications.time-zone: Asia/Kolkata
app.notifications.diet-reminder-cron: 0 * * * * *
app.notifications.coach-plan-reminder-cron: 0 0 8 * * *
app.notifications.dietician-plan-reminder-cron: 0 15 8 * * *
```

## AWS Deployment Plan (Production)

This plan deploys the frontend and backend separately, with managed AWS services for security,
scalability, and operations.

### Target Architecture

- Frontend (React build): S3 static hosting + CloudFront CDN + ACM TLS certificate.
- Backend (Spring Boot): ECS Fargate service behind an Application Load Balancer (ALB).
- Database: Amazon RDS for PostgreSQL in private subnets.
- Container registry: Amazon ECR.
- Secrets and config: AWS Secrets Manager + ECS task environment variables.
- DNS: Route 53.
- Logging/metrics: CloudWatch Logs + CloudWatch alarms.

### 1. Prerequisites

1. AWS account with IAM permissions for VPC, ECS, ECR, RDS, S3, CloudFront, ALB, Route 53,
   CloudWatch, ACM, and Secrets Manager.
2. Domain name (for example: `app.yourdomain.com`, `api.yourdomain.com`).
3. Local tools: AWS CLI v2, Docker, Maven, Node.js, and optionally Terraform.

### 2. Environment Strategy

Create separate environments:

- `dev`
- `staging`
- `prod`

Use separate resources or at least separate ECS services/RDS instances and secrets per environment.

### 3. Networking and Security Baseline

1. Create a VPC with at least 2 Availability Zones.
2. Public subnets:
- ALB
- NAT gateway
3. Private subnets:
- ECS tasks
- RDS PostgreSQL
4. Security groups:
- ALB SG: allow 80/443 from internet.
- ECS SG: allow backend port (8080) only from ALB SG.
- RDS SG: allow 5432 only from ECS SG.
5. Restrict IAM roles to least privilege for ECS task execution and app access to secrets.

### 4. Database (RDS PostgreSQL)

1. Create RDS PostgreSQL in private subnets (Multi-AZ recommended for prod).
2. Create database `gym_management`.
3. Store DB credentials in Secrets Manager.
4. Update backend datasource values through environment variables.

Recommended production change:

- Replace `spring.jpa.hibernate.ddl-auto=update` with `validate` in prod.
- Add a migration tool (Flyway/Liquibase) for controlled schema changes.

### 5. Backend Deployment (ECS Fargate)

1. Create ECR repository (for example: `gym-management-backend`).
2. Build backend artifact and Docker image.
3. Push image to ECR.
4. Create ECS cluster and Fargate task definition.
5. Set environment variables/secrets in task definition:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_WHATSAPP_FROM`
6. Create ECS service behind ALB target group.
7. Configure health check endpoint (for example `/api/actuator/health` if actuator is enabled).
8. Add auto scaling based on CPU/memory and minimum healthy task count.

### 6. Frontend Deployment (S3 + CloudFront)

1. Build frontend:

```powershell
cd frontend
npm ci
npm run build
```

2. Upload `frontend/dist` to S3 bucket (private bucket recommended).
3. Create CloudFront distribution with S3 origin.
4. Configure SPA fallback: map 403/404 to `/index.html` (HTTP 200).
5. Set frontend API base URL to backend domain (`https://api.yourdomain.com/api`).
6. In backend CORS config, allow your frontend domain (CloudFront custom domain).

### 7. TLS and DNS

1. Issue ACM certificates for frontend and backend domains.
2. Attach certificate:
- CloudFront for frontend domain.
- ALB for backend domain.
3. Create Route 53 records:
- `app.yourdomain.com` -> CloudFront
- `api.yourdomain.com` -> ALB

### 8. Observability and Alerts

1. Enable CloudWatch logs for ECS tasks.
2. Create alarms for:
- ECS task CPU/memory high
- ALB 5xx errors
- RDS CPU/storage/connections
3. Add SNS notifications for alarm actions.

### 9. CI/CD Pipeline (Recommended)

Use GitHub Actions (or CodePipeline) with this flow:

1. On push to `main`:
- Run backend unit tests/build.
- Build backend image and push to ECR.
- Deploy new ECS task definition and service update.
2. Build frontend and sync to S3.
3. Invalidate CloudFront cache for changed assets/index.
4. Run smoke tests against `app` and `api` endpoints.

### 10. Go-Live Checklist

1. Admin seed login works on production URL.
2. Role-based login and route access verified for ADMIN/COACH/DIETICIAN/CLIENT.
3. Upload flows and CRUD flows verified.
4. Notification bell and unread count verified.
5. WhatsApp provider mode verified (`log` for non-prod, `twilio` for prod if required).
6. Backup and restore tested (RDS snapshots).
7. Rollback tested by redeploying previous ECS task definition revision.

### 11. Rollback Plan

1. Backend: switch ECS service to previous stable task definition revision.
2. Frontend: restore previous S3 build artifact and invalidate CloudFront.
3. Database: restore from latest safe snapshot if migration issue occurs.

This gives you a reliable production setup while keeping local development workflow unchanged.

## Ask AI (Real Model Responses)

The floating Ask AI widget can use real model responses through a backend endpoint:

- API: `POST /api/v1/ai/ask`
- Security: authenticated users only (same JWT auth as other app APIs)
- Provider: OpenAI-compatible Chat Completions API

Configure environment variables for backend:

```powershell
$env:AI_ENABLED="true"
$env:AI_PROVIDER="openai"
$env:OPENAI_API_KEY="<your-openai-api-key>"
$env:OPENAI_BASE_URL="https://api.openai.com/v1"
$env:OPENAI_MODEL="gpt-4o-mini"
$env:OPENAI_TEMPERATURE="0.3"
$env:OPENAI_MAX_TOKENS="500"
```

If `AI_ENABLED=false` or API key is missing, Ask AI returns a clear configuration error.

The current endpoint is a general assistant and is not grounded in member records. The advanced,
tenant-aware AI fitness coach described in [docs/Advance_AI_Coach_2_2.txt](docs/Advance_AI_Coach_2_2.txt)
is a roadmap specification, not an implemented feature.

Production guidance:

1. Store API keys in AWS Secrets Manager.
2. Inject secrets into ECS task environment variables.
3. Do not hardcode AI keys in `application.yml`.
