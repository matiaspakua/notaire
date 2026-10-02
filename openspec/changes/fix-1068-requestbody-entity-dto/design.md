> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1068 (audit-2026-09, priority:high, security) lists 13 controllers that
bind JPA entities as `@RequestBody`. Project rule forbids entities in the API
layer. `UserController` / `FolioController` / `RoleController` already show the
preferred pattern: nested request records + `@Valid` + explicit mapping.
`AuditRecordController.create` is also the subject of #1060 (forge audit rows).

## Goals / Non-Goals

**Goals:**

- Eliminate mass assignment on all listed write endpoints via request DTOs.
- Apply `@Valid` on every new request body.
- Prove with tests that client-supplied id/version (and audit forgery) cannot
  stick.
- Remove public audit-log create (read-only API).
- Keep client-writable JSON field names stable for the Next.js frontend.

**Non-Goals:**

- Rewriting every GET list to a separate OpenAPI model if response records
  already cover the fields (lists may return the same response record type).
- Broad RBAC redesign (#559) or global exception formatting (#579).
- Flyway / schema changes.
- Touching `local-ai/`.

## Decisions

- **Nested records in controllers** over new shared `Dto*` types in
  `notaire-shared`: matches Folio/User, avoids bloating the legacy DTO package,
  and keeps validation next to the endpoint.
- **FK associations as Integer ids** in requests (e.g. `personId`,
  `identificationTypeId`), resolved in the controller/service — never accept
  nested entity graphs from the client.
- **Path id + loaded `@Version` always win** on update; request DTOs omit id and
  version entirely so Jackson cannot bind them.
- **AuditRecord POST deleted** rather than DTO-wrapped: wrapping would still
  allow forging audit content; #1060 AC requires removal.
- **Response records** for create/get/update payloads; OpenAPI documents them.

## Riesgos / Trade-offs

- [Risk] Frontend or Bruno still POSTs full entity graphs → Mitigation: keep
  writable field names; ignore unknown properties (default Jackson); update
  Bruno; integration tests cover happy path.
- [Risk] ProcedureTemplate / BudgetTemplate use composite PKs → Mitigation:
  request carries the FK pair; path variables remain source of truth on update.
- [Risk] ManagementController also has `CompleteCaseRequest` paths already on
  DTOs — only the raw `DeedManagement` create/update change → Mitigation:
  narrow edit; leave complete-case flow untouched.
- [Risk] Cannot apply `in-progress` label via this agent's `gh` token →
  Mitigation: record in traceability Exceptions.

## Testing Strategy

TDD: write mass-assignment / validation integration tests first; observe FAIL
(entity still bound or audit POST still 200); then implement; observe PASS.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Write endpoints reject entity-shaped id/version mass assignment | integration | `RequestBodyDtoBindingIntegrationTest` |
| `@Valid` rejects blank required fields on Person / IdentificationType | integration | existing `PersonRequestValidationIntegrationTest` + new cases |
| Audit-log POST returns 405/404 | integration | `AuditRecordMutationDisabledIntegrationTest` |
| Happy-path create still 201 with DTO body | integration | `RequestBodyDtoBindingIntegrationTest` |

- New unit tests: mapper helpers if extracted
- New integration tests: above
- Coverage impact: should hold JaCoCo ratchet (new code paths tested)

## Regression Strategy

- Existing tests affected: any MockMvc/Bruno that POST full entities including
  id/version; Person validation tests (still 400 on blanks); RemainingControllers
  / ApiH2 tests that create via entity JSON.
- Full suite: `mvn verify -pl backend-api`
- HTTP/Bruno: update bodies then `bash testing/scripts/test.sh` or
  `integration-test/scripts/test.sh` when stack is up
- Legacy `jpa` / Swing: out of scope

## Playwright Strategy

- n/a — no UI surface; API contract only. Frontend already sends field-level
  JSON; writable names preserved. Record "n/a — no UI surface" in tasks §7.

## Deployment Strategy

- Flyway migration required: no
- Deployment order: normal backend image roll
- Configuration / `.env`: none
- Feature flag: no
- Smoke test: `GET /actuator/health`; `POST /api/v1/people` with DTO; confirm
  `POST /api/v1/audit-log` is 405

## Rollback Strategy

- Revert safe: yes (no schema change); reverts restore entity binding (known
  vulnerability — only roll back for emergency availability)
- Database rollback: none needed
- Data written under new behavior: normal domain rows; no forged-audit rows
  after deploy
- Blast radius if delayed: clients still using audit POST fail until they stop

## Migration Plan

1. Gate 1 artifacts + failing tests
2. Convert controllers one family at a time (catalog → people → budgets → deeds
   → management → templates → remove audit POST)
3. Update Bruno / CHANGELOG
4. Preflight, PR, CI, merge

## Open Questions

None — AuditRecord handling resolved as remove-POST per #1060 complementarity.
