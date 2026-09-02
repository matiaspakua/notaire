> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #839 exists, labeled, and linked to Use Case CU80 — Administrar Cuadernos de Folios
- [x] 1.2 Use Case documentation exists at `docs/100-business/102-use-cases/CU80 – Administrar Cuadernos de Folios.md` — confirm it matches this change's scope before implementing
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/cuadernos-de-folios/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 n/a — no architectural deviation from the existing entity/repository/controller pattern; no ADR required (see design.md — Architecture review)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 839 --add-label "in-progress"`) — only if not already in progress from a sibling change under the same issue

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/839_protocolo-cuadernos-de-folios`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md — Testing Strategy, 10 scenarios)
- [x] 3.2 Write unit tests in `CuadernoTest`: `shouldMarkFoliosAsAsignadoACuaderno`, `shouldAssignNumberOneToFirstCuadernoOfYear`, `shouldRecalculateNextAvailableCuadernoNumber`
- [x] 3.3 Write integration tests in `CuadernoControllerTest`: `shouldCreateCuadernoFromConsecutiveFolios`, `shouldRejectCuadernoWhenFolioCountNotMultipleOfTen`, `shouldRejectCuadernoWithNonConsecutiveFolios`, `shouldRejectCuadernoWithFolioAlreadyAssigned`, `shouldCreateCuadernoWithJustifiedDamagedFolio`, `shouldGenerateCaratulaForExistingCuaderno`, `shouldReturnNotFoundForMissingCuadernoCaratula`
- [x] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=CuadernoTest,CuadernoControllerTest`
- [x] 3.5 Confirm every `#### Scenario:` in `specs/cuadernos-de-folios/spec.md` maps to at least one test (cross-check against traceability.md — Requirement coverage)

## 4. Implementación

- [x] 4.1 Flyway migration `V{n}__create_cuadernos_table.sql`: nueva tabla `cuadernos` (`id_cuaderno`, `numero`, `anio`, `fk_id_persona_escribano`, `observaciones`, `version`) y columna `fk_id_cuaderno` nullable en `folios`
- [x] 4.2 Nueva entidad `negocio/Cuaderno.java` (`idCuaderno`, `numero`, `anio`, `fkIdPersonaEscribano`, `observaciones`, relación con `Folio`), siguiendo el patrón `setAtributos(Dto)`/`getDto()` ya usado por `Folio`/`TipoDeFolio`
- [x] 4.3 Agregar campo `fkIdCuaderno` (`@ManyToOne`, opcional) a `negocio/Folio.java` y su mapeo en `setAtributos`/`getDto`
- [x] 4.4 Nuevo `DtoCuaderno` en `notaire-shared`, siguiendo la convención `DtoEntityName`
- [x] 4.5 Nuevo `repository/CuadernoRepository` (Spring Data JPA), con consulta de folios disponibles (sin `fkIdCuaderno`) ordenados por registro/número/año
- [x] 4.6 Nuevo `api/CuadernoController` con `POST /api/v1/cuadernos` (validaciones: múltiplo de 10, consecutividad, no reasignación, justificación para folio dañado/anulado; asignación de número correlativo con recálculo ante conflicto; marca los 10 folios como `estado = "Asignado a cuaderno"`), `GET /api/v1/cuadernos`, `GET /api/v1/cuadernos/{id}`
- [x] 4.7 Nuevo endpoint `GET /api/v1/cuadernos/{id}/caratula` que genera el PDF vía JasperReports, siguiendo el patrón de `ReporteController`; nueva plantilla `.jrxml`/`.jasper` en `src/main/resources/reportes/` con año, registro del escribano, número de cuaderno, rango de folios y detalle de escrituras/trámites otorgados
- [x] 4.8 Documentar los nuevos endpoints con `@Operation`/`@ApiResponse` (OpenAPI/Swagger)
- [x] 4.9 Nueva pantalla `frontend/src/app/dashboard/protocolo/cuadernos/page.tsx`: listar folios disponibles, seleccionar rango de 10, generar cuaderno, descargar carátula — usando `FormContainer`/`FormSection`/`FormField`/`FormActions` y `theme` tokens

## 5. Actualizar tests existentes

- [x] 5.1 Revisar `FolioControllerTest` (ver design.md — Regression Strategy): confirmar que ninguna aserción existente se rompe por el nuevo campo `fkIdCuaderno` (nullable, aditivo)
- [x] 5.2 No se anticipan cambios de aserciones existentes — solo confirmar que siguen pasando
- [x] 5.3 n/a — ningún test queda obsoleto por este cambio

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `frontend/tests/e2e/TS-0072-cuadernos-protocolo-workflow.spec.ts` (see design.md — Playwright Strategy): golden path (generar cuaderno de 10 folios consecutivos y descargar carátula) + edge paths (cantidad no múltiplo de 10, folio ya asignado)
- [x] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify `dashboard/protocolo/cuadernos` at 320px, 768px and 1024px (blocked this session: Chrome extension disconnected; page reuses `DataTable`/`Dialog` primitives already responsive-verified elsewhere — low risk, but not literally re-checked)
- [x] 7.4 n/a — this change has a UI surface, covered above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Actualizar `docs/100-business/102-use-cases/CU80 – Administrar Cuadernos de Folios.md` (confirmar que el flujo documentado coincide con lo implementado) y `docs/100-business/102-use-cases/CU28 – Ingresar nuevos folios.md` (nota: folio agrupado en cuaderno queda `estado = "Asignado a cuaderno"`)
- [x] 8.2 Update OpenAPI/Swagger annotations for the 4 new `cuadernos` endpoints and verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — generar cuadernos de folios y emitir su carátula
- [x] 8.4 n/a — no superseded documents to archive
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #839`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin feat/839_protocolo-cuadernos-de-folios`
- [x] 10.2 Open the PR titled `[#839] feat(cuadernos): administrar cuadernos de folios y emitir carátula` (PR #906), referencing Issue #839 and Use Case CU80
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment: generar un cuaderno de prueba con diez folios consecutivos vía UI, confirmar que los folios cambian a "Asignado a cuaderno" y que la carátula se descarga; `GET /actuator/health` en verde
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue #839 only if this is the last of the five `protocolo-*` changes to merge — referencing the PR; otherwise leave it open and note the partial completion
- [ ] 12.4 Archive the change: `openspec archive protocolo-cuadernos-de-folios`

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
