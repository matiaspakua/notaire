> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #838 exists, labeled, and linked to CU87
- [ ] 1.2 Use Case documentation (CU87) exists and is accurate
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/folio-vinculacion-escritura/spec.md`, `specs/copia-validacion-testimonio-inscripto/spec.md`
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 ADR: n/a — sigue el layering existente (validación en la capa `api`, como ya hace `FolioController` con `ESTADO_UTILIZADO`), no introduce un patrón arquitectónico nuevo (design.md — Decisions)
- [ ] 1.6 Move Issue #838 to IN PROGRESS (`gh issue edit 838 --add-label "in-progress"`)

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/838_folio-vinculacion-escritura`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases from the Requirement coverage table in `traceability.md` (happy path, edge cases, error paths)
- [ ] 3.2 Write `FolioTest#shouldRoundTripEscrituraThroughDto`, `FolioTest#shouldReturnNullEscrituraWhenNotLinked` (unit)
- [ ] 3.3 Write `FolioControllerTest#shouldLinkFolioToEscrituraOnCreate`, `#shouldLinkFolioToEscrituraOnUpdate`, `#shouldCreateFolioWithoutEscritura`, `#shouldRejectLinkingFolioAlreadyUtilizadoByAnotherEscritura`, `#shouldAllowReSavingFolioWithSameEscritura` (integration)
- [ ] 3.4 Write `CopiaControllerTest#shouldRejectCopiaWhenTestimonioHasInscriptaMovimiento`, `#shouldCreateCopiaWhenTestimonioHasNoInscriptaMovimiento`, `#shouldCreateCopiaWhenTestimonioHasNoMovimientos` (integration)
- [ ] 3.5 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=FolioTest,FolioControllerTest,CopiaControllerTest`
- [ ] 3.6 Confirm every `#### Scenario:` in the two delta specs maps to at least one test (cross-check against `traceability.md` — Requirement coverage)

## 4. Implementación

- [ ] 4.1 `Folio.setAtributos(DtoFolio)`: leer `unDtoFolio.getEscritura()` y, si no es nulo, resolver la `Escritura` por su ID y asignarla a `fkIdEscritura`
- [ ] 4.2 `Folio.getDto()`: si `fkIdEscritura` no es nulo, poblar `miDtoFolio.setEscritura(...)` con su DTO
- [ ] 4.3 `FolioController.FolioRequest`: agregar campo opcional `escrituraId`
- [ ] 4.4 `FolioController.create`: si `escrituraId` está presente, resolver la escritura (400 si no existe); si el folio destino a vincular ya tiene `estado = "Utilizado"` con una `fkIdEscritura` distinta, devolver 409; si no, vincular y setear `estado = "Utilizado"`
- [ ] 4.5 `FolioController.update`: misma validación 4.4 aplicada a la edición de un folio existente, permitiendo re-guardar el mismo `escrituraId` sin conflicto (idempotente)
- [ ] 4.6 `CopiaController.create`: antes de guardar, resolver el `Testimonio` de origen y consultar sus `MovimientoTestimonio` vía `MovimientoTestimonio.findByInscripta` (o equivalente); si existe alguno con `inscripta = true`, devolver 409
- [ ] 4.7 Confirmar en Swagger UI que `POST/PUT /api/v1/folio` documenta `escrituraId` y la respuesta 409, y que `POST /api/v1/copia` documenta la respuesta 409

## 5. Actualizar tests existentes

- [ ] 5.1 Confirmar que los tests existentes de `FolioControllerTest` (alta/edición/borrado sin `escrituraId`) no cambian de comportamiento — este cambio es aditivo (design.md — Regression Strategy)
- [ ] 5.2 Confirmar que el test existente de `CopiaControllerTest` (alta de copia con testimonio sin movimientos inscriptos) sigue pasando sin modificar su aserción
- [ ] 5.3 n/a — ningún test queda obsoleto

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add `frontend/tests/e2e/folios-vinculacion.spec.ts` (design.md — Playwright Strategy)
- [ ] 7.2 Frontend: `frontend/src/types/index.ts` — corregir `Escritura.folio?: Folio` por `Escritura.folios?: Folio[]`
- [ ] 7.3 Frontend: `administracion/folios/page.tsx` — agregar selector de escritura (`estado = "Firmada"` sin folio vinculado) al alta/edición de folio
- [ ] 7.4 Frontend: `escrituras/page.tsx` — corregir la columna de folio para leer `e.folios` en lugar de `e.folio`
- [ ] 7.5 `cd frontend && npx playwright test` — all green
- [ ] 7.6 Verify the changed screens at 320px, 768px and 1024px
- [ ] 7.7 Golden path + edge/error paths from design.md — Playwright Strategy covered

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `docs/100-business/102-use-cases/CU87 – Vincular Escritura a Folio y Copia a Testimonio.md`, `CU28 – Ingresar nuevos folios.md` (proposal.md — Documentation Impact)
- [ ] 8.2 Update OpenAPI/Swagger annotations for `FolioController` and `CopiaController` and verify in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — vincular folio a escritura; validar copia de testimonio ya inscripto
- [ ] 8.4 Archive superseded documents into `docs/archive/` — n/a, no document is superseded by this change
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #838`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/838_folio-vinculacion-escritura`
- [ ] 10.2 Open the PR titled `[#838] feat: vincular escritura a folio y validar copia de testimonio ya inscripto`, referencing Issue #838 and CU87
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test from design.md — Deployment Strategy (crear folio vinculado a una escritura firmada vía UI, confirmar `Utilizado` y visibilidad del folio en la escritura; `GET /actuator/health` en verde)
- [ ] 12.2 Verify the rollback path described in design.md — Rollback Strategy is still available
- [ ] 12.3 Close GitHub Issue #838, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive folio-vinculacion-escritura`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
