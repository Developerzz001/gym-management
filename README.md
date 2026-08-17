# Gym Management System

A full-stack Gym Management System built for **local development**.

- **Backend**: Java 21, Spring Boot 3, Spring Security (JWT + Refresh Tokens, RBAC), Spring Data JPA
  (Hibernate), SQL Server, MapStruct, Lombok, Bean Validation, springdoc-openapi (Swagger UI), Maven.
- **Frontend**: React 18, TypeScript, Vite, Material UI, Redux Toolkit, TanStack React Query, Axios,
  React Router v6, Formik + Yup.
- **Database**: SQL Server.

## Project Structure

```
gym-management/
├── backend/   # Spring Boot REST API (com.gymmanagement)
└── frontend/  # React + TypeScript SPA
```

## Prerequisites

| Tool             | Version   |
|------------------|-----------|
| JDK              | 21        |
| Maven            | 3.9+      |
| Node.js          | 18+ (20 recommended) |
| SQL Server       | 2019+ (or SQL Server container) |

## 1. Database Setup

Run SQL Server locally, e.g. via Docker:

```powershell
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourStrong@Passw0rd" `
  -p 1433:1433 --name gym-sqlserver -d mcr.microsoft.com/mssql/server:2022-latest
```

Then create the database (any SQL client, e.g. `sqlcmd` or Azure Data Studio):

```sql
CREATE DATABASE gym_management;
```

The backend uses Hibernate `ddl-auto: update`, so all tables (`users`, `clients`, `fitness_coaches`,
`dieticians`, `membership_plans`, `memberships`, `exercises`, `workout_plans`,
`workout_plan_details`, `training_sessions`, `diet_plans`, `diet_plan_details`, `supplements`,
`medicines`, `progress_records`, `notifications`, `refresh_tokens`) are created automatically on
first run — no manual schema scripts required for local dev.

Connection settings are in [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml).
Override via environment variables or edit directly:

```yaml
spring.datasource.url: jdbc:sqlserver://localhost:1433;databaseName=gym_management;encrypt=true;trustServerCertificate=true
spring.datasource.username: sa
spring.datasource.password: YourStrong@Passw0rd
```

## 2. Run the Backend

```powershell
cd backend
mvn spring-boot:run
```

The API starts on **http://localhost:8080/api**.

- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

On first startup, a default **ADMIN** account is seeded automatically:

```
email:    admin@gymmanagement.com
password: Admin@123
```

Use this account to log in and create coaches, dieticians, and clients from the Admin UI.

## 3. Run the Frontend

```powershell
cd frontend
npm install
npm run dev
```

The app starts on **http://localhost:5173** and proxies `/api/*` requests to the backend
(`http://localhost:8080`) — see `frontend/vite.config.ts`.

## 4. Typical Local Workflow

1. Log in as `admin@gymmanagement.com`.
2. Create Fitness Coaches and Dieticians (Admin → Coaches / Dieticians).
3. Register Clients (Admin → Clients) and assign a coach/dietician to each.
4. Log out and log in as a coach/dietician (password set during creation, default `Coach@123` /
   `Diet@123` if left blank) to create workout plans, diet plans, supplements, medicines and
   schedule sessions.
5. Log in as a client (password set during registration, default `Client@123` if left blank) to view
   the dashboard, workout, diet and progress pages.

## Authentication Flow

- `POST /api/v1/auth/login` — returns a short-lived JWT access token (15 min) and a longer-lived
  refresh token (7 days, stored server-side in `refresh_tokens`).
- `POST /api/v1/auth/refresh-token` — exchanges a valid refresh token for a new access token.
  The frontend Axios client automatically retries a request once with a refreshed token on `401`.
- `POST /api/v1/auth/logout` — revokes the given refresh token.
- `POST /api/v1/auth/change-password` — changes the authenticated user's password.

Roles: `ADMIN`, `FITNESS_COACH`, `DIETICIAN`, `CLIENT` (enforced via `@PreAuthorize` on every
controller endpoint).

## Backend Package Structure

```
com.gymmanagement
├── auth            # login/refresh/logout/change-password
├── security         # JWT filter, JwtService, UserDetails, SecurityConfig
├── user             # User entity/roles, admin user CRUD
├── admin             # cross-module assignment endpoints (assign/change coach & dietician)
├── coach / dietician # profile CRUD (creates linked User with role)
├── client            # client profile + medical details
├── workout           # Exercise master, WorkoutPlan/Detail, TrainingSession
├── diet              # DietPlan/Detail
├── supplement / medicine
├── progress          # date-wise body metrics, BMI auto-calculated
├── membership        # MembershipPlan, Membership (assign/renew)
├── notification      # in-app notification records
└── common            # BaseEntity, exceptions, ApiResponse/PageResponse, config (security, swagger, auditing)
```

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

By default, WhatsApp is in log mode (`provider: log`) so local development works without external
credentials.

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
- Database: Amazon RDS for SQL Server in private subnets.
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
- RDS SQL Server
4. Security groups:
- ALB SG: allow 80/443 from internet.
- ECS SG: allow backend port (8080) only from ALB SG.
- RDS SG: allow 1433 only from ECS SG.
5. Restrict IAM roles to least privilege for ECS task execution and app access to secrets.

### 4. Database (RDS SQL Server)

1. Create RDS SQL Server in private subnets (Multi-AZ recommended for prod).
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

Production guidance:

1. Store API keys in AWS Secrets Manager.
2. Inject secrets into ECS task environment variables.
3. Do not hardcode AI keys in `application.yml`.
