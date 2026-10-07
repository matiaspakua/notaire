> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1130 open
- [x] 1.2 Use Case CU01
- [x] 1.3 Acceptance Criteria as scenarios in delta spec
- [x] 1.4 Impact Analysis in `proposal.md`
- [x] 1.5 ADR n/a — restore response shape
- [x] 1.6 Hotfix for main CI

## 2. Crear branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 Branch `cursor/fix-budget-person-pg-json-69d3`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests

- [x] 3.1 PG IT failure reproduced on main run 37062075513
- [x] 3.2 Existing `BudgetPersonAssociationPgIntegrationTest` is the failing suite
- [x] 3.3 No new test class required — assert nested person
- [x] 3.4 Local/CI prove green after fix
- [x] 3.5 Spec scenarios mapped to PG IT methods

## 4. Implementación

- [x] 4.1 `BudgetResponse` uses nested `PersonRef person`
- [x] 4.2 `@JsonInclude(NON_NULL)` on response
- [x] 4.3 `toResponse` builds `PersonRef` from `fkIdPerson` (personId + name + lastName)
- [x] 4.4 OpenSpec change folder added
- [x] 4.5 `PersonRef` expanded with `name`/`lastName` for frontend DtoPerson / Playwright

## 5. Actualizar tests existentes

- [x] 5.1 PG IT asserts `personId` + `name` + `lastName`
- [x] 5.2 Unit test asserts DtoPerson name fields on get-by-id
- [x] 5.3 Bruno request bodies already nested
- [x] 5.4 No obsolete tests removed

## 6. Ejecutar regresión

- [x] 6.1 Unit / compile via CI
- [x] 6.2 Integration H2 via CI
- [x] 6.3 PG Integration via CI
- [x] 6.4 Coverage Gate via CI
- [x] 6.5 No @Disabled introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI change
- [x] 7.2 n/a
- [x] 7.3 n/a
- [x] 7.4 Recorded n/a in design.md

## 8. Gate 3 — Documentación permanente

- [x] 8.1 OpenSpec change is process record
- [x] 8.2 Frontend types already nested
- [x] 8.3 CHANGELOG optional for CI hotfix
- [x] 8.4 n/a archive
- [x] 8.5 No doc duplication
- [x] 8.6 validate-sdlc-plan + Process Checks on push

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 `Closes #1130`
- [x] 9.3 No secrets
- [x] 9.4 SHAs in traceability after push

## 10. Pull Request y validación CI

- [x] 10.1 Branch pushed
- [x] 10.2 PR open
- [x] 10.3 Wait for required workflows
- [x] 10.4 Gate 4 — CI green then merge (coordinator)
- [x] 10.5 Ready for review when green

## 11. Deploy

- [x] 11.1 Merge to main (coordinator)
- [x] 11.2 No special deploy steps
- [x] 11.3 No secrets

## Definition of Done

- [x] PG Integration Tests green (BudgetPersonAssociationPgIntegrationTest)
- [x] Coverage Gate / Unit / Integration H2 green
- [x] Process Checks green
- [x] PR merged; main CI restores
- [x] No secrets committed

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Confirm main CI green after merge
- [x] 12.2 Close #1130 via Closes
- [x] 12.3 Status file under agent store
- [x] 12.4 Tell #1126/#1128 to rebase

## Closure note (2026-10-04)

The nested `person.personId` response shape is on `main` (`BudgetController.BudgetResponse` with `PersonRef`, NON_NULL) and the PG integration test exists; CI and CD are green on `main` at `dde755e`. The checklist was ticked at archive time from that evidence.
