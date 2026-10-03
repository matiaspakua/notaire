> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1067 linked (open Gate 1 issue; fallout #1124/#1068)
- [x] 1.2 Use Case CU39 exists
- [x] 1.3 Acceptance Criteria as scenarios in delta spec
- [x] 1.4 Impact Analysis in `proposal.md`
- [x] 1.5 ADR n/a — bugfix of DTO→entity hydration
- [x] 1.6 Hotfix restores main; no separate in-progress issue required

## 2. Crear branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 Branch `cursor/fix-main-ci-1124-fallout-69d3` created
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Failing CI on main reproduced (BudgetTemplate create 500 / AuditAspect NoSuchMethod)
- [x] 3.2 Integration expectations already present; controller fix makes them pass
- [x] 3.3 Integration tests for template create / duplicate / concept-in-use
- [x] 3.4 Local AuditAspectTest green; template suites re-run after fix
- [x] 3.5 Spec scenarios mapped to integration / Bruno tests

## 4. Implementación

- [x] 4.1 Inject ConceptRepository + ProcedureTypeRepository into BudgetTemplateController
- [x] 4.2 setConcept/setProcedureType via getReferenceById before JpaController.create
- [x] 4.3 existsById → 400 when FK missing
- [x] 4.4 Align AuditAspectTest + integration helpers to DTO contracts
- [x] 4.5 ManagementRequest @NotBlank encabezado
- [x] 4.6 Add this OpenSpec change folder

## 5. Actualizar tests existentes

- [x] 5.1 Integration helpers use notaryPersonId / substitutePersonId / testimonyId / fkId*
- [x] 5.2 Assertions updated for DTO response shapes
- [x] 5.3 No obsolete tests removed without cause

## 6. Ejecutar regresión

- [x] 6.1 Unit: AuditAspectTest
- [ ] 6.2 Integration: BudgetTemplateSerializationIntegrationTest (+ related)
- [ ] 6.3 Coverage Gate via CI
- [ ] 6.4 Bruno budget-templates via CI
- [x] 6.5 No @Disabled introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface (see design.md)
- [x] 7.2 n/a
- [x] 7.3 n/a
- [x] 7.4 Recorded n/a in design.md

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Hotfix — OpenSpec change is the process record
- [x] 8.2 n/a — endpoint contract unchanged (DTO already shipped in #1124)
- [x] 8.3 CHANGELOG optional for CI restore
- [x] 8.4 n/a archive
- [x] 8.5 No doc duplication
- [ ] 8.6 validate-sdlc-plan + Process Checks on push

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 No invented Closes for wrong issue (fallout #1124 / #1068 in body)
- [x] 9.3 No secrets
- [ ] 9.4 SHAs in traceability after push

## 10. Pull Request y validación CI

- [x] 10.1 Branch pushed
- [x] 10.2 PR #1127 open
- [ ] 10.3 Wait for required workflows
- [ ] 10.4 Gate 4 — CI green then merge
- [ ] 10.5 Ready for review when green

## 11. Gate 4 readiness checklist

- [ ] 11.1 Unit + Integration + Coverage green on PR
- [ ] 11.2 Process Checks green (OpenSpec path present)
- [ ] 11.3 Bruno budget-templates create 201
- [ ] 11.4 Merged to main
- [x] 11.5 No production secrets committed
- [x] 11.6 Traceability chain filled as gates complete

## Definition of Done

- [ ] Unit Tests + Integration Tests + Coverage Gate green on #1127
- [ ] Process Checks green (this OpenSpec folder in the PR diff)
- [ ] Bruno `budget-templates` create returns 201
- [ ] PR merged to `main` and main CI restores green
- [x] No secrets committed; no wrongful `Closes`

## 12. Cierre

- [ ] 12.1 Merge #1127
- [ ] 12.2 Confirm main CI green
- [x] 12.3 Status file updated under agent store
- [x] 12.4 No wrongful Closes
