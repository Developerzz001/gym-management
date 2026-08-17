---
description: "Use when reviewing backend Spring Boot or frontend React changes in gym-management. Covers review focus for auth, API contracts, validation, persistence, and UI state handling."
---
# Gym Management Review Rules

- For backend changes, verify controller-service-repository boundaries, DTO validation, entity mapping, transaction safety, and role enforcement.
- For security-related changes, verify JWT handling, protected routes, role checks, and unauthorized flows.
- For frontend changes, verify API typings, request and response shape alignment, form validation, loading states, error states, and route protection.
- For cross-stack changes, verify that frontend payloads and backend DTOs still match.
- Flag missing tests whenever behavior changes.
- Prefer high-signal findings over broad style commentary.