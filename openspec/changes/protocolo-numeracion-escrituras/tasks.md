> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #839 exists, labeled, and linked to Use Case CU86 — Controlar Numeración Correlativa de Escrituras
- [ ] 1.2 Use Case documentation exists at `docs/100-business/102-use-cases/CU86 – Controlar Numeración Correlativa de Escrituras.md`; complete its `GitHub ID` field with `#839` and confirm it matches this change's scope before implementing
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/numeracion-escrituras/spec.md`
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 n/a — no architectural deviation from the existing entity/repository/service/controller pattern; no ADR required (see design.md — Architecture review)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 839 --add-label "in-progress"`) — only if not already in progress from a sibling change under the same issue

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/839_protocolo-numeracion-escrituras`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md — Testing Strategy, 5 scenarios)
- [ ] 3.2 Write unit tests in `NumeracionEscrituraServiceTest`: `shouldAcceptNumberMatchingExpectedCorrelativo`, `shouldKeepAuxiliarNumberingIndependentFromPrincipal`
- [ ] 3.3 Write integration tests in `EscrituraControllerTest` (extend): `shouldRejectDuplicateNumeroWithinScope`, `shouldRejectGapWithoutJustification`, `shouldAcceptGapWithJustification`
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=NumeracionEscrituraServiceTest,EscrituraControllerTest`
- [ ] 3.5 Confirm every `#### Scenario:` in `specs/numeracion-escrituras/spec.md` maps to at least one test (cross-check against traceability.md — Requirement coverage)

## 4. Implementación

- [ ] 4.1 Extender `repository/EscrituraRepository` con una consulta acotada por escribano, año y `esAuxiliar` (join a través de `Escritura.folioList` -> `Folio.fkIdPersonaEscribano`/`anio`/`fkIdTipoFolio.esAuxiliar`) para calcular `MAX(numero)` y detectar duplicados dentro del alcance
- [ ] 4.2 Nuevo `service/NumeracionEscrituraService`: `calcularSiguienteCorrelativo(escribano, anio, esAuxiliar)`, `validar(numeroPropuesto, escribano, anio, esAuxiliar, justificacion)` — devuelve resultado OK / DUPLICADO / SALTO_SIN_JUSTIFICAR / SALTO_JUSTIFICADO
- [ ] 4.3 Integrar la validación en `service/EscrituraService#save`: derivar escribano/año/esAuxiliar del folio asociado a la escritura (si existe), invocar `NumeracionEscrituraService#validar`, y guardar la justificación en `Escritura.observaciones` cuando corresponda
- [ ] 4.4 Actualizar `api/EscrituraController` para propagar el rechazo (409 duplicado, 400 salto sin justificar) con mensajes claros
- [ ] 4.5 Refactorizar `service/ProtocoloAuxiliarService` (de `protocolo-auxiliar-tramites`) para delegar el cálculo del siguiente correlativo auxiliar en `NumeracionEscrituraService#calcularSiguienteCorrelativo`, eliminando la lógica duplicada
- [ ] 4.6 Documentar la nueva validación en el `@Operation`/`@ApiResponse` de `POST`/`PUT /api/v1/escrituras` (OpenAPI/Swagger)
- [ ] 4.7 En el formulario de preparación/firma de escritura (frontend), mostrar el correlativo esperado y, ante un salto, mostrar el campo de justificación antes de reintentar el guardado

## 5. Actualizar tests existentes

- [ ] 5.1 Revisar `EscrituraControllerTest` existentes (ver design.md — Regression Strategy): confirmar que los tests de creación/actualización que ya usan números correlativos siguen pasando sin cambios
- [ ] 5.2 Revisar `ProtocoloAuxiliarServiceTest` (de `protocolo-auxiliar-tramites`): confirmar que sigue pasando sin cambios tras el refactor a `NumeracionEscrituraService`
- [ ] 5.3 n/a — ningún test queda obsoleto por este cambio

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add `frontend/tests/e2e/numeracion-escrituras.spec.ts` (see design.md — Playwright Strategy): golden path (número correlativo esperado) + edge paths (duplicado, salto sin justificar, salto justificado)
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify the escritura form's numeración feedback at 320px, 768px and 1024px
- [ ] 7.4 n/a — this change has a UI surface, covered above

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Actualizar `docs/100-business/102-use-cases/CU86 – Controlar Numeración Correlativa de Escrituras.md` (completar `GitHub ID`, confirmar que el flujo documentado coincide con lo implementado)
- [ ] 8.2 Update OpenAPI/Swagger annotations for `POST`/`PUT /api/v1/escrituras` and verify in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — control de numeración correlativa de escrituras
- [ ] 8.4 n/a — no superseded documents to archive
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #839`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/839_protocolo-numeracion-escrituras`
- [ ] 10.2 Open the PR titled `[#839] feat(numeracion-escrituras): controlar numeración correlativa de escrituras`, referencing Issue #839 and Use Case CU86
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment: preparar una escritura con el correlativo esperado (se guarda), intentar un duplicado (se rechaza), intentar un salto sin justificación (se rechaza) y con justificación (se guarda); `GET /actuator/health` en verde
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue #839 — this is the last of the five `protocolo-*` changes under this Issue to merge; verify all four siblings (`protocolo-cuadernos-de-folios`, `protocolo-carpetas-de-tramite`, `protocolo-auxiliar-tramites`, `protocolo-minuta-inscripcion`) are already merged before closing
- [ ] 12.4 Archive the change: `openspec archive protocolo-numeracion-escrituras`

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
