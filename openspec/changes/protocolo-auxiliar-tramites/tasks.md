> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #839 exists, labeled, and linked to Use Case CU81 — Gestión de Trámites en Protocolo Auxiliar
- [ ] 1.2 Use Case documentation exists at `docs/100-business/102-use-cases/CU81 – Gestión de Trámites en Protocolo Auxiliar.md` — confirm it matches this change's scope before implementing
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/protocolo-auxiliar/spec.md`
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 n/a — no architectural deviation from the existing entity/repository/service/controller pattern; no ADR required (see design.md — Architecture review)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 839 --add-label "in-progress"`) — only if not already in progress from a sibling change under the same issue

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/839_protocolo-auxiliar-tramites`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md — Testing Strategy, 8 scenarios)
- [ ] 3.2 Write unit tests in `TipoDeFolioTest`: `shouldDefaultEsAuxiliarToFalse`; in `ProtocoloAuxiliarServiceTest`: `shouldKeepAuxiliarNumberingIndependentFromPrincipal`, `shouldNotGenerateCarpetaForAuxiliarEscritura`
- [ ] 3.3 Write integration tests in `TipoDeFolioControllerTest` (extend): `shouldMarkTipoDeFolioAsAuxiliar`; in `ProtocoloAuxiliarControllerTest`: `shouldListAvailableFoliosAuxiliares`, `shouldReturnEmptyWhenNoFoliosAuxiliaresAvailable`, `shouldCreateEscrituraOnAvailableFolioAuxiliar`, `shouldRejectEscrituraWhenNoFolioAuxiliarAvailable`
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=TipoDeFolioTest,TipoDeFolioControllerTest,ProtocoloAuxiliarServiceTest,ProtocoloAuxiliarControllerTest`
- [ ] 3.5 Confirm every `#### Scenario:` in `specs/protocolo-auxiliar/spec.md` maps to at least one test (cross-check against traceability.md — Requirement coverage)

## 4. Implementación

- [ ] 4.1 Flyway migration `V{n}__add_es_auxiliar_to_tipos_folio.sql`: columna `es_auxiliar boolean NOT NULL DEFAULT false` en `tipos_folio`
- [ ] 4.2 Agregar campo `esAuxiliar` a `negocio/TipoDeFolio.java` y su mapeo en `setAtributos`/`getDto`
- [ ] 4.3 Agregar `esAuxiliar` a `DtoTipoDeFolio` en `notaire-shared`
- [ ] 4.4 Extender `repository/FolioRepository` (o el existente) con una consulta de folios disponibles filtrando por `fkIdTipoFolio.esAuxiliar = true` y sin `fkIdEscritura`
- [ ] 4.5 Nuevo `service/ProtocoloAuxiliarService`: `listarFoliosDisponibles()`, `iniciarEscritura(folioId, clienteId)` — calcula el siguiente número correlativo como `MAX(Escritura.numero) + 1` entre escrituras sobre folios auxiliares, crea la `Escritura`, la vincula al `Folio` (`Folio.fkIdEscritura`), y crea un `Tramite` con `fkIdGestion = null`
- [ ] 4.6 Nuevo `api/ProtocoloAuxiliarController`: `GET /api/v1/protocolo-auxiliar/folios-disponibles`, `POST /api/v1/protocolo-auxiliar/escrituras`
- [ ] 4.7 Documentar los nuevos endpoints con `@Operation`/`@ApiResponse` (OpenAPI/Swagger)
- [ ] 4.8 Agregar el checkbox "Protocolo Auxiliar" al formulario existente en `frontend/src/app/dashboard/administracion/folios/page.tsx`
- [ ] 4.9 Nueva pantalla de Protocolo Auxiliar: listar folios disponibles, seleccionar folio y cliente, iniciar la escritura, usando `FormContainer`/`FormSection`/`FormField`/`FormActions` y `theme` tokens

## 5. Actualizar tests existentes

- [ ] 5.1 Revisar `TipoDeFolioControllerTest` (ver design.md — Regression Strategy): confirmar que ninguna aserción existente se rompe por el nuevo campo `esAuxiliar` (default `false`, aditivo)
- [ ] 5.2 No se anticipan cambios de aserciones existentes — solo confirmar que siguen pasando
- [ ] 5.3 n/a — ningún test queda obsoleto por este cambio

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add `frontend/tests/e2e/protocolo-auxiliar.spec.ts` (see design.md — Playwright Strategy): golden path (marcar tipo auxiliar, listar disponibles, iniciar escritura) + edge path (sin folios disponibles)
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify the Protocolo Auxiliar screen and the updated folios admin form at 320px, 768px and 1024px
- [ ] 7.4 n/a — this change has a UI surface, covered above

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Actualizar `docs/100-business/102-use-cases/CU81 – Gestión de Trámites en Protocolo Auxiliar.md` (confirmar que el flujo documentado coincide con lo implementado)
- [ ] 8.2 Update OpenAPI/Swagger annotations for the 2 new `protocolo-auxiliar` endpoints and verify in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — gestión de trámites en Protocolo Auxiliar
- [ ] 8.4 n/a — no superseded documents to archive
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #839`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/839_protocolo-auxiliar-tramites`
- [ ] 10.2 Open the PR titled `[#839] feat(protocolo-auxiliar): gestión de trámites en protocolo auxiliar`, referencing Issue #839 and Use Case CU81
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment: marcar un tipo de folio como auxiliar, iniciar una escritura de Protocolo Auxiliar vía UI, confirmar que no se genera carpeta de trámite; `GET /actuator/health` en verde
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue #839 only if this is the last of the five `protocolo-*` changes to merge — referencing the PR; otherwise leave it open and note the partial completion
- [ ] 12.4 Archive the change: `openspec archive protocolo-auxiliar-tramites`

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
