> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #823 exists, labeled, and linked to Use Cases CU-27 (Ingresar Nuevo Tipo de Documento) and CU-39 (Crear Plantilla Presupuesto)
- [x] 1.2 Use Case documentation exists at `docs/100-business/102-use-cases/CU27 – Ingresar Nuevo Tipo de Documento.md` and `CU39 – Crear Plantilla Presupuesto.md`; update both to document the cost linkage and the fixed/variable cost per document type before implementing
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/costos-documentos-presupuesto/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 n/a — `PlantillaCostoDocumento` follows the existing composite-key entity pattern already used by `PlantillaPresupuesto`, via the modern `service`/`repository` layers (not the legacy `jpa` package); no ADR required (see design.md — Decisions)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 823 --add-label "in-progress"`) — already done during triage

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/823_costos-documentos-presupuesto`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md — Testing Strategy, 6 scenarios)
- [x] 3.2 Write unit tests in `PagoServiceTest`: `shouldIncludeDocumentCostInPresupuestoTotal`, `shouldSumMultipleDocumentCostsInPresupuestoTotal`, `shouldNotChangeTotalWhenNoDocumentsHaveCost`
- [x] 3.3 Write unit tests in new `PlantillaCostoDocumentoServiceTest`: `shouldAcceptFixedCostForTipoDocumento`, `shouldAcceptVariableCostForTipoDocumento`, `shouldRejectWhenBothFixedAndVariableCostProvided`, `shouldRejectWhenNeitherFixedNorVariableCostProvided`
- [x] 3.4 Write integration tests in new `PlantillaCostoDocumentoControllerTest`: `shouldReturnCostosByTipoTramite`, `shouldReturnEmptyListWhenNoCostosDefined`
- [x] 3.5 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=PagoServiceTest,PlantillaCostoDocumentoServiceTest,PlantillaCostoDocumentoControllerTest`
- [x] 3.6 Confirm every `#### Scenario:` in `specs/costos-documentos-presupuesto/spec.md` maps to at least one test (cross-check against traceability.md — Requirement coverage)

## 4. Implementación

- [x] 4.1 Crear migración Flyway `V{n}__create_plantilla_costos_documento.sql` (resolver `{n}` como el siguiente disponible tras la última aplicada) creando la tabla `plantilla_costos_documento` (`fk_id_tipo_tramite`, `fk_id_tipo_documento` clave compuesta, `monto_fijo` NUMERIC NULL, `porcentaje_variable` NUMERIC NULL)
- [x] 4.2 Crear `negocio/PlantillaCostoDocumentoPK` (`@Embeddable`, mismo patrón que `PlantillaPresupuestoPK`) y `negocio/PlantillaCostoDocumento` (`@EmbeddedId`, `ManyToOne` a `TipoDeTramite` y `TipoDeDocumento`, `montoFijo`, `porcentajeVariable`)
- [x] 4.3 Crear `repository/PlantillaCostoDocumentoRepository extends JpaRepository<PlantillaCostoDocumento, PlantillaCostoDocumentoPK>` con `findByTipoDeTramiteIdTipoTramite`
- [x] 4.4 Crear `service/PlantillaCostoDocumentoService` con validación: exactamente uno de `montoFijo`/`porcentajeVariable` debe estar presente (lanza excepción de validación si ambos o ninguno)
- [x] 4.5 Crear `api/PlantillaCostoDocumentoController` (`GET/POST/PUT/DELETE /api/v1/plantilla-costos-documento`, `GET /api/v1/plantilla-costos-documento/tipo-tramite/{id}`) usando `PlantillaCostoDocumentoService`, no la capa `jpa`
- [x] 4.6 En `service/PagoService.calcularTotalPresupuesto`, sumar `importeAPagar` de `presupuesto.getTramiteList()[].getDocumentoPresentadoList()`
- [x] 4.7 Documentar los nuevos endpoints en `@Operation`/`@ApiResponse` (OpenAPI/Swagger)
- [x] 4.8 Alinear `frontend/src/types/index.ts` agregando el tipo `PlantillaCostoDocumento`
- [x] 4.9 En la pantalla de plantillas de presupuesto (CU39), agregar una sección para definir gastos fijos/variables por tipo de documento

## 5. Actualizar tests existentes

- [x] 5.1 Confirmar que `PagoServiceTest`/`PagoServiceIntegrationTest` existentes (presupuestos sin trámites o sin documentos presentados) siguen pasando sin cambios — cubierto por `shouldNotChangeTotalWhenNoDocumentsHaveCost`
- [x] 5.2 n/a — `PlantillaCostoDocumentoController` no tiene test class existente; los tests nuevos lo cubren por primera vez
- [x] 5.3 n/a — ningún test queda obsoleto por este cambio; `PlantillaPresupuestoController`/`PlantillaPresupuestoJpaController` no se modifican

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `mvn test -Ppg-integration` — Flyway schema validation guard test
- [x] 6.5 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [x] 6.6 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `frontend/tests/e2e/plantilla-costos-documento.spec.ts` (see design.md — Playwright Strategy): golden path (definir gasto fijo por tipo de documento y verlo listado; definir gasto variable y verlo listado) + edge path (plantilla sin gastos definidos muestra lista vacía)
- [x] 7.2 `cd frontend && npx playwright test` — all green
- [x] 7.3 Verify the new template section at 320px, 768px and 1024px
- [x] 7.4 n/a — this change has a UI surface, covered above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Actualizar `docs/100-business/102-use-cases/CU27 – Ingresar Nuevo Tipo de Documento.md` (el costo del documento se refleja en el presupuesto de su trámite) y `CU39 – Crear Plantilla Presupuesto.md` (gastos fijos/variables por tipo de documento)
- [x] 8.2 Update OpenAPI/Swagger annotations for `/api/v1/plantilla-costos-documento` endpoints and verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — el costo de los documentos presentados se refleja en el total del presupuesto; las plantillas de presupuesto pueden definir gastos por tipo de documento
- [x] 8.4 n/a — no superseded documents to archive
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #823`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin feat/823_costos-documentos-presupuesto`
- [x] 10.2 Open the PR titled `[#823] feat(presupuestos): costos de documentos y plantilla de gastos`, referencing Issue #823 and Use Cases CU-27/CU-39
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment: cargar un documento con `importeAPagar` en un trámite de un presupuesto de prueba y verificar que `GET /api/v1/pagos/presupuesto/{id}/saldo` lo incluye; definir un gasto fijo por tipo de documento en una plantilla y consultarlo; `GET /actuator/health` en verde
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue #823
- [ ] 12.4 Archive the change: `openspec archive costos-documentos-presupuesto`

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
