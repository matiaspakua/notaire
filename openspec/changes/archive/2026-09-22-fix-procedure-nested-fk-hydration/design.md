> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See `proposal.md` — Objetivo. `ProcedureController` (`/api/v1/tramites`)
accepts the raw `Procedure` JPA entity as its request body for `POST`/`PUT`.
`RegistrationDraftController` (`/api/v1/minutas-inscripcion`) already
demonstrates the target pattern in this codebase: a plain-field request
`record`, resolved against real repositories before the service persists
anything (`GenerateRequest(Integer idDeed)`).

## Goals / Non-Goals

**Goals:**
- `POST`/`PUT /api/v1/tramites` always persist and return real, currently-
  persisted association state.
- Match the existing `RegistrationDraftController` request-record pattern.

**Non-Goals:**
- Introducing `DeedRepositoryPort`/`DeedManagementRepositoryPort` hexagonal
  ports (existing plain `DeedRepository`/`DeedManagementRepository` reused).
- Changing the response shape (still the raw `Procedure` entity).
- Fixing any other controller with the same raw-entity pattern.

## Decisions

- **New `ProcedureRequest` record, not the legacy `DtoProcedure`.**
  `DtoProcedure` (in `notaire-shared`) nests full sub-DTOs
  (`DtoDeed deed`, `DtoProperty property`, ...) — using it would just move
  the same "trust client-supplied nested object" problem one layer down.
  `RegistrationDraftController.GenerateRequest`'s plain-id pattern is the
  one proven in this codebase not to have this bug; `ProcedureRequest`
  follows it exactly: `idProcedureType`, `idProperty`, `idDeed`,
  `idManagement`, `idBudget`, `notes` — all plain ids, no nested objects.
- **Controller resolves references directly, no new service class.**
  `ProcedureController` has no dedicated `ProcedureService`/use-case class
  today (unlike `RegistrationDraftController`); adding one is a larger
  refactor than this bug fix needs. The controller already injects
  `ProcedureRepositoryPort`; this change adds `PropertyRepositoryPort`,
  `BudgetRepositoryPort`, `ProcedureTypeRepositoryPort` (existing hex
  ports) plus `DeedRepository`/`DeedManagementRepository` (existing plain
  Spring Data repositories) as constructor dependencies.
- **`idProcedureType` required, others optional.** Matches the DB
  constraint (`fk_id_procedure_type NOT NULL`) and the entity's own
  `@ManyToOne(optional = false)`; the other four associations are all
  `optional = true`/nullable in the entity today.
- **404 for an unresolvable provided id, 400 for a missing required
  field.** Matches the existing convention in this controller's other
  endpoints (`getById` returns 404; no endpoint currently returns 400, so
  this introduces that pattern, matching
  `RegistrationDraftController`'s `BusinessValidationException` → 400).

## Riesgos / Trade-offs

- **BREAKING contract change** → mitigated: no production UI caller exists
  for this endpoint yet (verified via repo-wide grep of `frontend/src`);
  the only callers are test fixtures, all updated in this same change
  (Bruno, Playwright E2E, backend unit/integration tests).
- **`SimpleControllersTest.ProcedureControllerTests` constructs the
  controller directly with a single-arg constructor and serializes a raw
  `Procedure` entity as the request body** → this test must be rewritten
  to mock the new dependencies and send `ProcedureRequest`-shaped JSON;
  done as part of this change (Constitution: never weaken an assertion to
  force green — this is a legitimate contract change, so the old
  expectation is now wrong, not the new code).
- **Mixing hex ports (`PropertyRepositoryPort`, etc.) with plain Spring
  Data repositories (`DeedRepository`, `DeedManagementRepository`) in one
  controller** → declared, not silently accepted: documented in
  `proposal.md` — Architecture review as matching existing codebase
  convention (P10 — adapt, don't replace); introducing two new hex ports
  for a bug fix would be disproportionate scope creep.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Creating a Procedure with a real Deed id returns the Deed's actual state | integration | `ProcedureControllerFkHydrationIntegrationTest` (new) |
| A subsequent GET reflects the same real association state | integration | same |
| idProcedureType is required | integration | same |
| A non-existent referenced id is rejected | integration | same |
| Updating a Procedure with a real Deed id returns the Deed's actual state | integration | same |

- New unit tests: `SimpleControllersTest.ProcedureControllerTests` rewritten
  (not new, but materially changed — mocks the new dependencies, posts
  `ProcedureRequest` JSON instead of a raw entity).
- New integration tests: `ProcedureControllerFkHydrationIntegrationTest`
  (new file) — reproduces the exact bug from issue #981 (create a Deed
  with real `status`/`number`, `POST` a Procedure referencing it by plain
  id, assert both the `POST` response and a follow-up `GET` reflect the
  real Deed).
- Coverage impact: net-new integration test class; existing
  `ProcedureSerializationIntegrationTest`'s two helper methods
  (`createProcedureForBudget`) updated to the new flat-id request shape —
  behavior asserted (no cyclic-recursion in the JSON) is unchanged.

## Regression Strategy

- Existing tests affected:
  - `SimpleControllersTest.ProcedureControllerTests` (rewritten — mocks
    new dependencies, new request shape)
  - `ProcedureSerializationIntegrationTest` (`createProcedureForBudget`
    helper's request body updated to flat ids)
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `backend-api/api-test/tramites/01-create.yml`,
  `04-update.yml` — request bodies updated to flat ids
- Legacy paths at risk: none — `jpa` package does not expose this
  endpoint (it's already migrated to `adapter/in/web`)

## Playwright Strategy

- Specs to update: `TS-0082-minuta-inscripcion-feature.spec.ts` (line
  ~53-55, `apiPost(page, "/tramites", {fkIdProcedureType: {...}, fkIdDeed:
  {...}})` → flat `{idProcedureType, idDeed}`); `setup/api-helpers.ts`'s
  `seedProcedure` helper (used by several other specs — TS-0020, TS-0043,
  TS-0071, TS-0090, TS-0060, TS-0042, TS-0070, TS-0024, TS-0022 — via the
  shared helper, not directly)
- Golden path covered: `TS-0082` — "Golden path: generar minuta, presentar
  e inscribir en forma definitiva" must pass after this fix
- Edge / error paths covered: `TS-0082`'s two related edge-case tests
  (per issue #981's own description)
- Viewports: n/a — this is an API contract fix, not a UI change
- Command: `cd frontend && npx playwright test TS-0082`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — single-service change, no
  migration/code ordering concern
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `POST /api/v1/tramites` with a real
  `idDeed`, confirm the response embeds the real Deed state

## Rollback Strategy

- Revert safe: yes — pure application-code change, no schema/data migration
- Database rollback: none needed
- Data written under the new behavior after revert: any `Procedure` rows
  created via the new flat-id contract retain their correctly-resolved
  associations regardless of a later code revert (data, not code, holds
  the fix's effect) — reverting only stops *future* creates from
  benefiting
- Blast radius if rollback is delayed: low — affects only Procedure
  create/update via this one endpoint

## Open Questions

None — the pattern to follow (`RegistrationDraftController`'s request
record) and the scope (this one controller) are both fully determined by
the issue's own technical notes and the existing codebase.
