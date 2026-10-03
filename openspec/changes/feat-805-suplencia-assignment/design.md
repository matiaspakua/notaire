> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

\#836 already resolves the effective notary in
`ManagementController.applyManagementFields` (complete-case) via
`ManagementSubstitutionService.resolverNotary`. Residual bypass:
`applyManagementRequest` (plain create/update) still does
`entity.setFkIdNotaryPerson(notary)` with no substitution lookup. Entity and
service names on current `main` are English (`Substitution`, `DeedManagement`,
`ManagementController`); some Spanish method/record names remain in the service.

## Goals / Non-Goals

**Goals:**

- Close the plain POST/PUT notary-assignment bypass by reusing
  `ManagementSubstitutionService`.
- Append redirection notes the same way complete-case does.
- Prove with failing-then-green integration tests (TDD).
- Englishize Spanish identifiers/comments/strings in touched code.

**Non-Goals:**

- Rebuilding #836 Substitution CRUD, complete-case happy path, or TS-0092 UI.
- Schema / Flyway changes.
- Renaming Spanish URL segments (`/gestiones`, `/suplencia`).
- New frontend forms for plain management CRUD.

## Decisions

1. **Reuse `ManagementSubstitutionService` in `applyManagementRequest`** —
   after `dateStart` is applied, if `notaryPersonId` is present, call
   `resolveNotary(requested, dateStart)` and set `fkIdNotaryPerson` to the
   resolved notary. Do not duplicate repository queries in the controller.
2. **Reuse note-building** — extract or call the same note-append logic used by
   complete-case (`buildNotes` / `redirectionNote`) so plain and complete-case
   produce identical redirection text.
3. **Englishize on touch** — rename `NotaryAsignado` → `AssignedNotary`,
   `resolverNotary` → `resolveNotary`, `observacionRedireccion` →
   `redirectionNote`, `substitutionAplicada` → `appliedSubstitution`; translate
   the redirection message string and Spanish comments/Javadoc in touched files.
   Update unit/integration call sites in the same change.
4. **No UI work** — TS-0092 already covers complete-case toast; plain CRUD is
   API residual only for this issue.

## Riesgos / Trade-offs

- [Clients that relied on assigning a substituted notary via plain POST/PUT]
  → Correct RF-115 behavior; document in CHANGELOG as a fix, not BREAKING API
  contract change.
- [English rename of public service methods] → Service is internal Spring bean;
  only test call sites update. No OpenAPI DTO rename.
- [Date boundary edge cases] → Existing repository query
  `dateStart ≤ date ≤ dateEnd` reused unchanged from #836.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Creación sin suplencia activa | unit | `ManagementSubstitutionServiceTest` (existing) |
| Creación/edición complete-case con suplencia | integration | `ManagementControllerIntegrationTest#shouldRedirectToSuplenteWhenUpdatingManagementNotary` (existing) |
| Plain POST create redirects under active substitution | integration | `ManagementControllerIntegrationTest` (new) |
| Plain PUT update redirects under active substitution | integration | `ManagementControllerIntegrationTest` (new) |
| Observaciones / plain path notes record redirection | integration | same new tests |
| TS-0092 complete-case UI toast | E2E | existing `TS-0092-gestion-suplencia-redirect.spec.ts` (regression; no UI change) |

- New unit tests: update existing `ManagementSubstitutionServiceTest` for
  English method/record names; behavior assertions unchanged.
- New integration tests: two methods on `ManagementControllerIntegrationTest`.
- Coverage impact: small controller branch coverage gain; ratchet floor held.

## Regression Strategy

- Existing tests affected: `ManagementSubstitutionServiceTest` (rename),
  `AdditionalControllersTest` (constructor still injects service),
  `ManagementControllerIntegrationTest` complete-case redirect (must stay green).
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: only if gestiones create assertions pin notary id under
  active substitution (unlikely).
- Legacy paths at risk: none.

## Playwright Strategy

- Specs to add/update: none (no UI surface for plain POST/PUT).
- Golden path covered: existing TS-0092 (complete-case) remains the E2E proof
  for CU22 redirection UX.
- Edge / error paths covered: n/a for this change.
- Viewports: n/a — no UI surface.
- Command: `cd frontend && npx playwright test TS-0092-gestion-suplencia-redirect.spec.ts`
  (optional smoke; coordinator CI runs full Playwright).
- Record: "n/a — no UI surface" for new E2E; regression only.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: code-only deploy
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): create active substitution; plain
  `POST /api/v1/gestiones` with substituted notary → response/entity shows
  substitute id; notes contain redirection text.

## Rollback Strategy

- Revert safe: yes — code-only
- Database rollback: none needed
- Data written under the new behavior after revert: gestiones already redirected
  keep substitute assignment and notes (correct business data)
- Blast radius if rollback is delayed: low

## Migration Plan

No staged rollout. Pre-existing gestiones assigned to a substituted notary
without redirection are not backfilled; only new plain writes apply the rule.

## Open Questions

None — scope fixed by issue #805 and coordinator brief (residual plain-path
bypass after #836).
