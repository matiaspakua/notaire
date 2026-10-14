> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`AuditAspect.isAuditableOperation` accepts only POST/PUT/PATCH/DELETE and login/logout. The generic `AuditOperationDescriber` puts any `String` argument into the detail, which for `searchPeople` would be a DNI or name.

## Goals / Non-Goals

**Goals:** traceable PII reads with no PII in the trail.
**Non-Goals:** auditing every GET; UI changes.

## Decisions

1. **Opt-in annotation, not a blanket GET rule.** The existing decision to keep list refreshes out of the trail stays; PII endpoints opt in.
2. **A dedicated describer** builds `Read of personal data ... via <URI>` from the request URI (no query string) and the first numeric id argument; string arguments are never read.
3. **One row per request**, so lists and searches are aggregated by construction.

## Riesgos / Trade-offs

- [Audit table growth on busy list screens] → one row per request; the UI already paginates; revisit with #1636.
- [Handler added later without the annotation] → the guard test lists the three Person handlers.

## Testing Strategy

`AuditPersonalDataReadTest` (unit): single, search, list, unannotated, annotation presence. Written first and observed failing.

## Regression Strategy

Full `mvn verify -pl backend-api` (existing audit tests assert writes still audited and other GETs not).

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
