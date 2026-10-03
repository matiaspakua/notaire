> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See proposal.md — Objetivo. On `main` after #1124, `AuditRecordController` is
GET-only; `AuditRecordMutationDisabledIntegrationTest` covers POST→405;
unit tests cover POST and DELETE→405. Gaps: explicit PUT tests, Bruno
negatives, stale OpenAPI `@Tag` (“consultar y administrar”).

## Goals / Non-Goals

**Goals:**
- Lock mutation deny to **405** for POST, PUT, and DELETE.
- Close #1060 AC with Bruno + OpenAPI + threat-model note.
- Keep legitimate `AuditAspect` writer unchanged.

**Non-Goals:**
- RBAC on GET (#559), DB immutability (SR-09), aspect `save` visibility hardening.

## Decisions

1. **Status code 405 over 404** — Spring already returns 405 when no handler
   exists; AC allows either; lock 405 for consistency with existing tests and
   OpenAPI expectations. Alternative (404 via security filter) rejected as more
   invasive and inconsistent with current mapping-absence behavior.
2. **No controller code change for mutations** — handlers remain absent;
   work is tests + docs + OpenAPI tag. Alternative (explicit `@RequestMapping`
   returning 405) rejected as noise.
3. **Bruno negatives under `audit-records/`** — same folder as the GET list;
   use JWT from `00-auth`. Alternative (security-folder) rejected to keep
   resource co-location.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Clients still POSTing audit rows break | Intentional since #1124; frontend never POSTed; CHANGELOG already notes |
| Integration test MockMvc without security filters | Keep unit/integration mapping tests for 405; unauthenticated 401 remains separate security-chain concern |
| Label `in-progress` unavailable via token | Proceed; PR still closes #1060 |

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| POST audit-log is rejected | unit + integration | `AuditRecordControllerTest#shouldRejectCreateOfAuditRecords`; `AuditRecordMutationDisabledIntegrationTest#shouldRejectAuditLogPost` (existing) |
| PUT audit-log is rejected | unit + integration | `AuditRecordControllerTest#shouldRejectUpdateOfAuditRecords` (new); `AuditRecordMutationDisabledIntegrationTest#shouldRejectAuditLogPut` (new) |
| DELETE audit-log is rejected | unit + integration | `AuditRecordControllerTest#shouldRejectDeleteOfAuditRecords` (existing); `AuditRecordMutationDisabledIntegrationTest#shouldRejectAuditLogDelete` (new) |
| OpenAPI tag is consult-only | unit / static | Assert controller `@Tag` description (unit) or manual Swagger check in Gate 3 |
| Bruno rejects audit-log mutations | Bruno | `api-test/audit-records/02-post-denied.yml`, `03-put-denied.yml`, `04-delete-denied.yml` |

- New unit tests (`src/test/java/.../unit/`): PUT rejection on `AuditRecordControllerTest`
- New integration tests (`src/test/java/.../integration/`): PUT + DELETE on `AuditRecordMutationDisabledIntegrationTest`
- Coverage impact (JaCoCo ratchet floor; 80% target): neutral/slight increase (tests only + tag string)

TDD: add failing PUT unit/integration assertions first if any implementation were needed; for tag text, assert expected description then update the annotation.

## Regression Strategy

- Existing tests affected: `AuditRecordControllerTest`, `AuditRecordMutationDisabledIntegrationTest` (extend, do not weaken)
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh` (or `bru run` from `backend-api/api-test`)
- Legacy paths at risk: none — legacy `AuditRecordJpaController` is non-REST

## Playwright Strategy

n/a — no UI surface. Frontend already uses GET-only hooks; no E2E mutation path.

- Specs to add/update under `frontend/tests/e2e/`: none
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: n/a

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: standard backend image roll; no migration
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `GET /api/v1/audit-log` with JWT → 200; `POST /api/v1/audit-log` → 405

## Rollback Strategy

- Revert safe: yes — reverts tests/docs/tag only; POST remains absent from #1124 on main unless that commit is also reverted
- Database rollback: none needed
- Data written under the new behavior after revert: none (deny-only)
- Blast radius if rollback is delayed: documentation/test drift only

## Migration Plan

1. Land tests + Bruno + OpenAPI tag + threat-model note on this branch.
2. Merge PR with `Closes #1060`.
3. Smoke on target: mutation deny 405.

## Open Questions

None — status code locked to 405 per prep survey and existing tests.
