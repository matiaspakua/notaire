> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #822 exists, labeled, and linked to Use Cases CU-45 (Modificar Presupuesto) and CU-71 (Gestión de Items)
- [x] 1.2 Use Case documentation exists at `docs/100-business/102-use-cases/CU45 – Modificar Presupuesto.md` and `CU71 – Gestión de Items.md`; update both to document the item type, mandatory reason and total effect before implementing
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/descuentos-recargos-presupuesto/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 n/a — no architectural deviation from the existing service/repository/controller pattern; `ItemService` corrects a pre-existing deviation instead of introducing a new one (see design.md — Decisions); no ADR required
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 822 --add-label "in-progress"`) — already done during triage

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/822_descuentos-recargos-presupuesto`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md — Testing Strategy, 12 scenarios)
- [x] 3.2 Write unit tests in new `ItemServiceTest`: `shouldTreatItemWithoutTypeAsNormal`, `shouldAcceptDiscountItemWithReason`, `shouldAcceptSurchargeItemWithReason`, `shouldRejectDiscountItemWithoutReason`, `shouldRejectSurchargeItemWithoutReason`, `shouldAcceptNormalItemWithoutReason`
- [x] 3.3 Write unit tests in `PagoServiceTest`: `shouldSubtractDiscountItemFromTotal`, `shouldAddSurchargeItemToTotal`, `shouldSumOnlyNormalItemsWhenNoDiscountsOrSurcharges`
- [x] 3.4 Write integration tests in new `ItemControllerTest`: `shouldReturnDiscountsAndSurchargesForPresupuesto`, `shouldReturnEmptyListWhenNoDiscountsOrSurcharges`, `shouldReturnNotFoundWhenPresupuestoDoesNotExistForReport`
- [x] 3.5 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=ItemServiceTest,PagoServiceTest,ItemControllerTest`
- [x] 3.6 Confirm every `#### Scenario:` in `specs/descuentos-recargos-presupuesto/spec.md` maps to at least one test (cross-check against traceability.md — Requirement coverage)

## 4. Implementación

- [x] 4.1 Crear migración Flyway `V{n}__add_tipo_motivo_to_items.sql` (resolver `{n}` como el siguiente disponible tras la última aplicada) agregando `tipo VARCHAR NOT NULL DEFAULT 'NORMAL'` y `motivo VARCHAR NULL` a `items`
- [x] 4.2 Agregar enum `TipoItem` (`NORMAL`, `DESCUENTO`, `RECARGO`) y campos `tipo`/`motivo` a `negocio/Item.java`, con getters/setters y actualización de `setAtributos`/`getDto`/`DtoItem`
- [x] 4.3 Crear `service/ItemService`, moviendo la lógica de `ItemController` (`findAll`, `findById`, `findByPresupuesto`, `create`, `update`, `delete`) y agregando la validación: `tipo` `DESCUENTO`/`RECARGO` requiere `motivo` no vacío (lanza excepción de validación si falta)
- [x] 4.4 Agregar `ItemService.findDescuentosYRecargosByPresupuesto(Integer idPresupuesto)` reutilizando `ItemRepository.findByFkIdPresupuestoIdPresupuesto` filtrado por tipo
- [x] 4.5 Refactorizar `api/ItemController` para inyectar `ItemService` en vez de `ItemRepository`, y agregar `GET /api/v1/items/presupuesto/{idPresupuesto}/descuentos-recargos`
- [x] 4.6 En `service/PagoService.calcularTotalPresupuesto`, aplicar el signo según `item.getTipo()` (resta para `DESCUENTO`, suma para `RECARGO` y `NORMAL`/`null`), tanto para el monto fijo como para el cálculo por porcentaje
- [x] 4.7 Documentar el nuevo endpoint en `@Operation`/`@ApiResponse` (OpenAPI/Swagger)
- [x] 4.8 Alinear `frontend/src/types/index.ts`'s `Item` con la entidad real, agregando `tipo`/`motivo`
- [x] 4.9 En la pantalla de ítems de presupuesto (CU71), agregar selector de tipo y campo de motivo obligatorio cuando corresponde, y mostrar el reporte de descuentos/recargos

## 5. Actualizar tests existentes

- [x] 5.1 Confirmar que `PagoServiceTest`/`PagoServiceIntegrationTest` existentes (ítems sin `tipo`) siguen pasando sin cambios, ya que `tipo == null` se trata como `NORMAL`
- [x] 5.2 n/a — `ItemController` no tiene test class existente; los tests nuevos de `ItemControllerTest` cubren su comportamiento por primera vez
- [x] 5.3 n/a — ningún test queda obsoleto por este cambio

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `mvn test -Ppg-integration` — Flyway schema validation guard test
- [x] 6.5 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [x] 6.6 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `frontend/tests/e2e/items-descuentos-recargos.spec.ts` (see design.md — Playwright Strategy): golden path (agregar descuento → total baja; agregar recargo → total sube) + edge paths (guardar sin motivo falla; presupuesto sin descuentos/recargos)
- [x] 7.2 `cd frontend && npx playwright test` — all green
- [x] 7.3 Verify the item type selector and reason field at 320px, 768px and 1024px
- [x] 7.4 n/a — this change has a UI surface, covered above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Actualizar `docs/100-business/102-use-cases/CU45 – Modificar Presupuesto.md` (descuento/recargo como curso alternativo, efecto en el total) y `CU71 – Gestión de Items.md` (tipo de ítem, motivo obligatorio, reporte)
- [x] 8.2 Update OpenAPI/Swagger annotations for `GET /api/v1/items/presupuesto/{idPresupuesto}/descuentos-recargos` and verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — ítems de presupuesto pueden clasificarse como descuento o recargo con motivo
- [x] 8.4 n/a — no superseded documents to archive
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #822`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/822_descuentos-recargos-presupuesto`
- [ ] 10.2 Open the PR titled `[#822] feat(items): descuentos y recargos con motivo estructurado`, referencing Issue #822 and Use Cases CU-45/CU-71
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment: crear un ítem de tipo descuento con motivo sobre un presupuesto de prueba, verificar que el total baja; consultar `GET /api/v1/items/presupuesto/{id}/descuentos-recargos` y verificar que lo lista; `GET /actuator/health` en verde
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue #822
- [ ] 12.4 Archive the change: `openspec archive descuentos-recargos-presupuesto`

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
