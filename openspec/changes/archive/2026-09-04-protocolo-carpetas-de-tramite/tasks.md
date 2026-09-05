> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #839 exists, labeled, and linked to Use Case CU85 — Administrar Carpetas de Trámite
- [x] 1.2 Use Case documentation exists at `docs/100-business/102-use-cases/CU85 – Administrar Carpetas de Trámite.md` — confirm it matches this change's scope before implementing
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/carpetas-de-tramite/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 n/a — no architectural deviation from the existing entity/repository/service/controller pattern; no ADR required (see design.md — Architecture review)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 839 --add-label "in-progress"`) — only if not already in progress from a sibling change under the same issue

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/839_protocolo-carpetas-de-tramite`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md — Testing Strategy, 9 scenarios)
- [x] 3.2 Write unit tests in `CarpetaTramiteServiceTest`: `shouldGenerateCarpetaOnSingleTramiteGestion`, `shouldGenerateOneCarpetaPerTramiteInMultiTramiteGestion`; extend `GestionArchiveDebtServiceTest`: `shouldArchiveAllActiveCarpetasOnGestionArchive`, `shouldRequireConfirmationWhenCarpetaInEsperaUnresolved`, `shouldArchiveCarpetaInEsperaOnExplicitConfirmation`
- [x] 3.3 Write integration tests in `CarpetaTramiteControllerTest`: `shouldReturnCarpetaByTramite`, `shouldReturnNotFoundForMissingCarpeta`, `shouldSetCarpetaToEsperaWithMotivo`, `shouldRejectEsperaWithoutMotivo`
- [x] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=CarpetaTramiteServiceTest,CarpetaTramiteControllerTest,GestionArchiveDebtServiceTest`
- [x] 3.5 Confirm every `#### Scenario:` in `specs/carpetas-de-tramite/spec.md` maps to at least one test (cross-check against traceability.md — Requirement coverage)

## 4. Implementación

- [x] 4.1 Flyway migration `V{n}__create_carpetas_tramite_table.sql`: nueva tabla `carpetas_tramite` (`id_carpeta`, `numero`, `estado`, `motivo_espera`, `fk_id_gestion`, `fk_id_tramite`, `version`)
- [x] 4.2 Nueva entidad `negocio/CarpetaTramite.java` (`idCarpeta`, `numero`, `estado`, `motivoEspera`, `fkIdGestion` `@ManyToOne`, `fkIdTramite` `@ManyToOne`), siguiendo el patrón `setAtributos(Dto)`/`getDto()`
- [x] 4.3 Nuevo `DtoCarpetaTramite` en `notaire-shared`, siguiendo la convención `DtoEntityName`
- [x] 4.4 Nuevo `repository/CarpetaTramiteRepository` (Spring Data JPA), con consultas por `fkIdTramite` y por `fkIdGestion`
- [x] 4.5 Nuevo `service/CarpetaTramiteService` con método de generación automática (una carpeta por trámite, estado "Activa") invocado desde el punto de alta de `Tramite`, y método para poner una carpeta en "Espera" con motivo obligatorio
- [x] 4.6 Nuevo `api/CarpetaTramiteController`: `GET /api/v1/carpetas/{id}`, `GET /api/v1/carpetas?gestionId=&tramiteId=`, `PUT /api/v1/carpetas/{id}/espera` (valida motivo obligatorio)
- [x] 4.7 Modificar `GestionArchiveDebtService.archivar` para: (a) listar carpetas del trámite/gestión en estado "Espera" sin resolver y, si `confirmado=false`, devolver el aviso sin archivar; (b) si `confirmado=true` o no hay carpetas en espera, archivar la gestión y pasar todas sus carpetas a "Archivada"
- [x] 4.8 Actualizar `POST /api/v1/gestiones/{id}/archivar` en `GestionController` para aceptar el parámetro de confirmación y propagar el aviso de carpetas en espera al llamador
- [x] 4.9 Documentar los nuevos endpoints con `@Operation`/`@ApiResponse` (OpenAPI/Swagger)
- [x] 4.10 Nueva pantalla de consulta de carpetas de trámite (por gestión o trámite) con acción "poner en espera", usando `FormContainer`/`FormSection`/`FormField`/`FormActions` y `theme` tokens

## 5. Actualizar tests existentes

- [x] 5.1 Revisar `GestionArchiveDebtServiceTest` (ver design.md — Regression Strategy): confirmar que las aserciones existentes sobre `deudaPendienteAlArchivar` y `EstadoDeGestion` siguen pasando con el nuevo parámetro de confirmación
- [x] 5.2 Actualizar las llamadas existentes a `GestionArchiveDebtService.archivar` en tests que no contemplaban el nuevo parámetro de confirmación, documentando el motivo
- [x] 5.3 n/a — ningún test queda obsoleto por este cambio

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `frontend/tests/e2e/carpetas-de-tramite.spec.ts` (see design.md — Playwright Strategy): golden path (generación automática, espera con motivo, archivado en cascada) + edge paths (espera sin motivo, archivado con carpeta en espera sin confirmar)
- [x] 7.2 `cd frontend && npx playwright test` — all green
- [x] 7.3 Verify the carpetas screen at 320px, 768px and 1024px
- [x] 7.4 n/a — this change has a UI surface, covered above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Actualizar `docs/100-business/102-use-cases/CU85 – Administrar Carpetas de Trámite.md` (completar GitHub ID, confirmar flujo) y `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` (nota: archivar la gestión ahora archiva sus carpetas, con alerta si alguna está en "Espera")
- [x] 8.2 Update OpenAPI/Swagger annotations for the 3 new `carpetas` endpoints and verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — generar y administrar carpetas de trámite
- [x] 8.4 n/a — no superseded documents to archive
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #839`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin feat/839_protocolo-carpetas-de-tramite`
- [x] 10.2 Open the PR titled `[#839] feat(carpetas): administrar carpetas de trámite`, referencing Issue #839 and Use Case CU85
- [x] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [x] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [x] 11.1 Merge via the Pull Request only — never push to `main`
- [x] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [x] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Run the smoke test on the target environment: iniciar un trámite de prueba, confirmar carpeta "Activa", archivar la gestión y confirmar carpeta "Archivada"; `GET /actuator/health` en verde
- [x] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue #839 only if this is the last of the five `protocolo-*` changes to merge — referencing the PR; otherwise leave it open and note the partial completion
- [ ] 12.4 Archive the change: `openspec archive protocolo-carpetas-de-tramite`

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
