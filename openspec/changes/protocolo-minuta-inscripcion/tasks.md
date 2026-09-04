> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #839 exists, labeled, and linked to Use Case CU82 — Generar Minuta de Inscripción
- [x] 1.2 Use Case documentation exists at `docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md` — confirm it matches this change's scope before implementing
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/minuta-inscripcion/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 n/a — no architectural deviation from the existing entity/repository/service/controller/reporte pattern; no ADR required (see design.md — Architecture review)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 839 --add-label "in-progress"`) — only if not already in progress from a sibling change under the same issue

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/839_protocolo-minuta-inscripcion`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md — Testing Strategy, 7 scenarios)
- [x] 3.2 Write unit tests in `MinutaInscripcionServiceTest`: validación de datos completos, transiciones de estado (Generada → Presentada → Observada / Inscripta)
- [x] 3.3 Write integration tests in `InmuebleControllerTest` (extend): `shouldSaveMatriculaTomoFolioFincaYLinderos`; in `MinutaInscripcionControllerTest`: `shouldGenerateMinutaWhenDataIsComplete`, `shouldRejectGenerationWhenDataIsIncomplete`, `shouldRegisterPresentacion`, `shouldRegisterObservacion`, `shouldRegisterInscripcionDefinitiva`; in `ReporteControllerTest` (extend): `shouldGenerateMinutaInscripcionReport`
- [x] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=InmuebleControllerTest,MinutaInscripcionServiceTest,MinutaInscripcionControllerTest,ReporteControllerTest`
- [x] 3.5 Confirm every `#### Scenario:` in `specs/minuta-inscripcion/spec.md` maps to at least one test (cross-check against traceability.md — Requirement coverage)

## 4. Implementación

- [x] 4.1 Flyway migration `V{n}__add_datos_registrales_inmueble_and_create_minutas_inscripcion.sql`: columnas `matricula`, `tomo_folio_finca`, `linderos` en `inmuebles`; tabla `minutas_inscripcion` (`id_minuta_inscripcion`, `numero`, `precio_operacion`, `estado`, `fecha_generacion`, `fecha_presentacion`, `numero_entrada_registral`, `fecha_recepcion`, `numero_inscripcion_definitivo`, `observaciones_registro`, `fecha_subsanacion`, `fk_id_escritura` UNIQUE)
- [x] 4.2 Agregar `matricula`, `tomoFolioFinca`, `linderos` a `negocio/Inmueble.java` y su mapeo en `setAtributos`/`getDto`; agregar los mismos campos a `DtoInmueble` en `notaire-shared`
- [x] 4.3 Nueva entidad `negocio/MinutaInscripcion.java` (`@OneToOne` con `Escritura`) y `DtoMinutaInscripcion` en `notaire-shared`; estados como constantes en `ConstantesNegocio` (Generada, Presentada, Observada, Inscripta)
- [x] 4.4 Nuevo `repository/MinutaInscripcionRepository`
- [x] 4.5 Nuevo `service/MinutaInscripcionService`: `generar(idEscritura)` (valida completitud de datos catastrales/registrales del inmueble del trámite, crea la minuta en estado Generada), `presentar(id, fecha, numeroEntradaRegistral)`, `observar(id, observaciones, fechaSubsanacion)`, `inscribir(id, fechaRecepcion, numeroInscripcionDefinitivo)`
- [x] 4.6 Nuevo `api/MinutaInscripcionController`: `POST /api/v1/minutas-inscripcion`, `PUT /api/v1/minutas-inscripcion/{id}/presentar`, `PUT /api/v1/minutas-inscripcion/{id}/observar`, `PUT /api/v1/minutas-inscripcion/{id}/inscribir`
- [x] 4.7 Documentar los nuevos endpoints con `@Operation`/`@ApiResponse` (OpenAPI/Swagger)
- [x] 4.8 Nuevo template JasperReports (`.jrxml`) para el formulario normalizado de la minuta, siguiendo el patrón de `src/main/resources/reportes/`; nuevo endpoint `GET /api/v1/reportes/minuta-inscripcion/{id}` en `ReporteController`
- [x] 4.9 Agregar los campos registrales al formulario existente de administración de inmuebles (si existe) o a la pantalla de trámite de inmuebles
- [x] 4.10 Nueva pantalla de Minuta de Inscripción: generar desde una escritura, mostrar/editar datos registrales del inmueble, avanzar el circuito (presentar, observar, inscribir), descargar el reporte, usando `FormContainer`/`FormSection`/`FormField`/`FormActions` y `theme` tokens

## 5. Actualizar tests existentes

- [x] 5.1 Revisar `InmuebleControllerTest` y `ReporteControllerTest` (ver design.md — Regression Strategy): confirmar que ninguna aserción existente se rompe por los nuevos campos/endpoints (aditivos)
- [x] 5.2 No se anticipan cambios de aserciones existentes — solo confirmar que siguen pasando
- [x] 5.3 n/a — ningún test queda obsoleto por este cambio

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `frontend/tests/e2e/minuta-inscripcion.spec.ts` (see design.md — Playwright Strategy): golden path (completar datos registrales, generar minuta, presentar, inscribir) + edge path (datos catastrales incompletos, observación del registro)
- [x] 7.2 `cd frontend && npx playwright test` — all green
- [x] 7.3 Verify the Minuta de Inscripción screen at 320px, 768px and 1024px
- [x] 7.4 n/a — this change has a UI surface, covered above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Actualizar `docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md` (confirmar que el flujo documentado coincide con lo implementado)
- [x] 8.2 Update OpenAPI/Swagger annotations for the new `minutas-inscripcion` endpoints and the new report endpoint, and verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — generación y seguimiento de la Minuta de Inscripción
- [x] 8.4 n/a — no superseded documents to archive
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #839`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/839_protocolo-minuta-inscripcion`
- [ ] 10.2 Open the PR titled `[#839] feat(minuta-inscripcion): generar y hacer seguimiento de la minuta de inscripción`, referencing Issue #839 and Use Case CU82
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment: completar datos registrales de un inmueble, generar una minuta desde una escritura aprobada, descargar el reporte, registrar presentación e inscripción definitiva; `GET /actuator/health` en verde
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue #839 only if this is the last of the five `protocolo-*` changes to merge — referencing the PR; otherwise leave it open and note the partial completion
- [ ] 12.4 Archive the change: `openspec archive protocolo-minuta-inscripcion`

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
