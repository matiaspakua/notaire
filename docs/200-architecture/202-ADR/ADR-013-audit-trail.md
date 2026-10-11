# ADR-013: Cross-cutting audit with Spring AOP

**Status:** Accepted
**Date:** 2026-05-12
**Deciders:** Matías Miguez
**Related:** ADR-001 (Microservices Architecture), ADR-008 (Security & Authentication), ADR-010 (Error Handling)

## Context

A legal requirement (see `.claude/rules/general.md` and the "Legal Constraints" section of the [SAD](../201-SAD/sad.md#2-constraints)) says every create, update, and delete on notarial domain entities must be recorded immutably, together with the authenticated user who performed it. Recording that by hand in each controller (31 `*Controller` classes) would be repetitive, easy to skip, and hard to keep consistent.

An earlier design incident (issue #555) found that trusting a client-supplied header (`X-Notaire-User`) for authorship let a malicious client forge the audited user.

## Decision

Implement auditing as a **cross-cutting concern** with **Spring AOP**, centralized in `AuditAspect` (`com.licensis.notaire.audit`):

- **Single pointcut**: every public method of a `*Controller` in `com.licensis.notaire.api` or `com.licensis.notaire.adapter.in.web`. `AuditRecordController` is excluded so audit reads do not audit themselves.
- **`@AfterReturning` advice**: a record is written only when the operation succeeds (failed writes are not audited, except login and logout).
- **Auditable-operation filter** (`isAuditableOperation`): methods annotated with `@PostMapping`, `@PutMapping`, `@PatchMapping`, or `@DeleteMapping`, or whose name contains `login` or `logout`. `GET` (read) operations are **not audited**.
- **User attribution**: the actor comes only from `SecurityContextHolder`, filled by `JwtAuthenticationFilter` from a valid JWT — never from a client-supplied header. Every write under `/api/**` requires authentication, so an authenticated user is present when the aspect runs.
- **Persistence**: each record is stored in `audit_records` through `AuditRecordService` → `AuditRecordRepository`.
- **Exposure**: readable through `AuditRecordController` (`GET /api/v1/audit-log`) and in the frontend at `/dashboard/audit`.

### Why AOP (not per-service audit code, and not database triggers)

| Criterion | Spring AOP (chosen) | Manual audit in each service | PostgreSQL triggers |
|----------|----------------------|-------------------------------------|----------------------|
| Guaranteed coverage | Yes — one pointcut covers current and future controllers | No — each developer must remember to add it | Yes, but at row level, without HTTP or JWT user context |
| Access to the authenticated user | Direct, through `SecurityContextHolder` | Direct, but duplicated in each service | No access to the application context |
| Coupling to business logic | None — the aspect lives outside the services | High — audit mixed with business logic | None, but opaque to the application team |
| Maintainability | One place to change | N changes (one per service) | A SQL migration per table |

## Consequences

### Positive

- New write endpoints are audited automatically, with no extra code, as long as they follow the `*Controller` convention and a write HTTP verb.
- The client cannot forge the audited user (issue #555).
- Audit logic is fully decoupled from the business services (single responsibility).

### Negative

- The name-based pointcut needs discipline: a controller outside `adapter.in.web` (or the legacy `api` package) or without the `Controller` suffix is not audited.
- Changes made directly in the database, outside the application, are not audited (out of scope: only `backend-api` is assumed to access PostgreSQL; see ADR-001).

## Related ADRs

- ADR-001: Microservices Architecture (only the backend accesses the database)
- ADR-008: Security & Authentication (JWT is the only identity source)
- ADR-010: Error Handling
