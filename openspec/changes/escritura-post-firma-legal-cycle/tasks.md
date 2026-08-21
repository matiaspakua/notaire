> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #832 exists, labeled, and linked to CU06/CU07/CU08/CU11/CU12/CU44
- [ ] 1.2 Use Case documentation (CU06, CU07, CU08, CU11, CU12, CU44) exists and is accurate
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/escritura-firma/spec.md`, `specs/testimonio-generacion-verificacion/spec.md`, `specs/testimonio-movimiento-inscripcion/spec.md`
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 ADR: n/a — sigue el layering existente (`service`/`api` sobre `negocio`/`repository`), no introduce un patrón arquitectónico nuevo (design.md — Decisions)
- [ ] 1.6 Move Issue #832 to IN PROGRESS (`gh issue edit 832 --add-label "in-progress"`)

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/832_escritura-post-firma-legal-cycle`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases from the Requirement coverage table in `traceability.md` (happy path, edge cases, error paths)
- [ ] 3.2 Write unit tests: `EscrituraFirmaServiceTest`, `TestimonioGeneracionServiceTest`, `TestimonioVerificacionServiceTest`, `MovimientoTestimonioServiceTest`
- [ ] 3.3 Write integration tests: `EscrituraFirmaControllerIntegrationTest`, `TestimonioAccionesControllerIntegrationTest`, `MovimientoTestimonioControllerIntegrationTest`, `TestimonioCopiaReportIntegrationTest`
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=EscrituraFirmaServiceTest,TestimonioGeneracionServiceTest,TestimonioVerificacionServiceTest,MovimientoTestimonioServiceTest`
- [ ] 3.5 Confirm every `#### Scenario:` in the three delta specs maps to at least one test (cross-check against `traceability.md` — Requirement coverage)

## 4. Implementación

- [ ] 4.1 `EscrituraFirmaService`: validar estado "Sin Firmar" + folio(s) asignado(s) y transicionar a "Firmada"
- [ ] 4.2 `POST /api/v1/escrituras/{id}/firmar` en `EscrituraController`, documentado en OpenAPI (`@Operation`, `@ApiResponse`)
- [ ] 4.3 `TestimonioGeneracionVerificacionService`: generar testimonio solo desde escritura "Firmada"
- [ ] 4.4 `POST /api/v1/testimonios/{id}/generar` en `TestimonioController`, documentado en OpenAPI
- [ ] 4.5 `TestimonioGeneracionVerificacionService`: verificar testimonio (observado/no observado + motivo)
- [ ] 4.6 `POST /api/v1/testimonios/{id}/verificar` en `TestimonioController`, documentado en OpenAPI
- [ ] 4.7 `GET /api/v1/reportes/testimonio/{id}/copia` en `ReporteController`, reutilizando el patrón JasperReports existente, solo para testimonios verificados
- [ ] 4.8 `MovimientoTestimonioService`: ingresar para inscripción (`fechaIngreso`), validando que no haya movimiento abierto
- [ ] 4.9 `MovimientoTestimonioService`: registrar inscripción (`fechaInscripcion`/`inscripta`), validando ingreso previo
- [ ] 4.10 `MovimientoTestimonioService`: retirar (`fechaSalida`, `numeroCarton`), validando que esté inscripto
- [ ] 4.11 `MovimientoTestimonioService`: reingresar, creando nuevo `MovimientoTestimonio` sin alterar el anterior, validando retiro previo
- [ ] 4.12 Cuatro endpoints `POST /api/v1/movimientos-testimonio/{id}/ingresar-inscripcion`, `.../registrar-inscripcion`, `.../retirar`, `.../reingresar` en `MovimientoTestimonioController`, documentados en OpenAPI
- [ ] 4.13 Frontend: reemplazar la edición libre de `estado` por la acción "Firmar" en `frontend/src/app/dashboard/escrituras`
- [ ] 4.14 Frontend: nueva pantalla de testimonios (generar, verificar, emitir copia) usando `FormContainer`/`FormSection`/`FormField`/`FormActions`
- [ ] 4.15 Frontend: nueva pantalla de movimientos de testimonio (ingresar/inscribir/retirar/reingresar) usando el mismo sistema de diseño
- [ ] 4.16 Confirmar en Swagger UI que los ocho endpoints nuevos aparecen correctamente documentados

## 5. Actualizar tests existentes

- [ ] 5.1 Confirmar que ningún test existente de `EscrituraController`/`TestimonioController`/`MovimientoTestimonioController` (CRUD genérico) cambia — este change es aditivo (design.md — Regression Strategy)
- [ ] 5.2 n/a — no se debilita ninguna aserción existente, no se identificó ningún test que requiera actualización
- [ ] 5.3 n/a — ningún test queda obsoleto

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add `frontend/tests/e2e/escritura-firma.spec.ts`, `testimonio-generacion-verificacion.spec.ts`, `testimonio-movimiento-inscripcion.spec.ts` (design.md — Playwright Strategy)
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify the three new/changed screens at 320px, 768px and 1024px
- [ ] 7.4 Golden path + edge/error paths from design.md — Playwright Strategy covered

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `docs/100-business/102-use-cases/CU06 – Firmar escritura (Esta Junto a Preparar Escritura).md`, `CU07 – Generar testimonio.md`, `CU08 – Verificar Testimonio.md`, `CU11 – Ingresar para inscripción.md`, `CU12 – Retirar testimonio.md`, `CU44 – Reingresar testimonio.md` (proposal.md — Documentation Impact)
- [ ] 8.2 Update OpenAPI/Swagger annotations for the eight new endpoints and verify in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — firma, generación/verificación de testimonio, circuito de inscripción y copia impresa
- [ ] 8.4 Archive superseded documents into `docs/archive/` — n/a, no document is superseded by this change
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #832`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/832_escritura-post-firma-legal-cycle`
- [ ] 10.2 Open the PR titled `[#832] feat: circuito legal posterior a la firma de escritura`, referencing Issue #832 and CU06/CU07/CU08/CU11/CU12/CU44
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test from design.md — Deployment Strategy (firmar, generar/verificar testimonio, emitir copia; `GET /actuator/health` en verde)
- [ ] 12.2 Verify the rollback path described in design.md — Rollback Strategy is still available
- [ ] 12.3 Close GitHub Issue #832, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive escritura-post-firma-legal-cycle`

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
