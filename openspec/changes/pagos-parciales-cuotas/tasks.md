> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #821 exists, labeled, and linked to Use Cases CU-15 (Procesar Pago) and CU-47 (Consultar Pago)
- [ ] 1.2 Use Case documentation exists at `docs/100-business/102-use-cases/CU15 – Procesar Pago.md` and `CU47 – Consultar Pago.md`; update both to document the partial-payment flow and the payment status before implementing
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/pagos-parciales-cuotas/spec.md`
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 n/a — no architectural deviation from the existing service/repository/controller pattern; no ADR required (see design.md — Architecture review)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 821 --add-label "in-progress"`) — already done during triage

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/821_pagos-parciales-cuotas`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md — Testing Strategy, 9 scenarios)
- [ ] 3.2 Write unit tests in `PagoServiceTest`: `shouldAcceptPaymentCoveringFullTotal`, `shouldAcceptPartialPaymentBelowSaldo`, `shouldReturnSinPagosStatusWhenNoPaymentsRegistered`, `shouldReturnParcialStatusWhenBalancePending`, `shouldReturnSaldadoStatusWhenBalanceZero`
- [ ] 3.3 Write integration tests: `PagoServiceIntegrationTest#shouldAcceptSequenceOfPartialPaymentsReachingTotal`; `PagoControllerTest#shouldReturnEstadoParcialForPresupuesto`, `#shouldReturnEstadoSaldadoForPresupuesto`, `#shouldReturnNotFoundWhenPresupuestoDoesNotExist`
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=PagoServiceTest,PagoServiceIntegrationTest,PagoControllerTest`
- [ ] 3.5 Confirm every `#### Scenario:` in `specs/pagos-parciales-cuotas/spec.md` maps to at least one test (cross-check against traceability.md — Requirement coverage)

## 4. Implementación

- [ ] 4.1 Extender `repository/PagoRepository` con `countByFkIdPresupuestoIdPresupuesto(Integer idPresupuesto)`
- [ ] 4.2 En `service/PagoService`, agregar enum `EstadoPago` (`SIN_PAGOS`, `PARCIAL`, `SALDADO`) y método `calcularEstadoPago(Integer idPresupuesto)`, reutilizando `calcularSaldoPendiente` y el nuevo conteo de pagos
- [ ] 4.3 En `api/PagoController`, agregar `GET /api/v1/pagos/presupuesto/{idPresupuesto}/estado` devolviendo el `EstadoPago`, con el mismo manejo de errores (404 si el presupuesto no existe) que `getSaldoPendiente`
- [ ] 4.4 Confirmar que `PagoService#procesarPago` sigue aceptando pagos parciales sin exigir que cubran el saldo total (comportamiento actual, sin cambios de código — solo test que lo confirma)
- [ ] 4.5 Documentar el nuevo endpoint en `@Operation`/`@ApiResponse` (OpenAPI/Swagger)
- [ ] 4.6 En el listado de pagos del presupuesto (frontend, `usePagos.ts` y la pantalla que lo consuma), agregar un hook que consulte el nuevo endpoint y muestre una etiqueta visible ("Sin pagos" / "Parcialmente abonado" / "Saldado")

## 5. Actualizar tests existentes

- [ ] 5.1 Revisar `PagoControllerTest#shouldGetPagosByPresupuesto` (ver design.md — Regression Strategy): confirmar que sigue pasando sin cambios, ya que el endpoint existente no se modifica
- [ ] 5.2 Revisar `BusinessWorkflowIntegrationTest`, `RemainingControllersIntegrationTest` y `UseCaseDomainsIntegrationTest`: confirmar que las aserciones sobre `/api/v1/pagos/presupuesto/{id}` siguen pasando sin cambios
- [ ] 5.3 n/a — ningún test queda obsoleto por este cambio

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add `frontend/tests/e2e/pagos-estado.spec.ts` (see design.md — Playwright Strategy): golden path (pago parcial → "parcialmente abonado" → pago restante → "saldado") + edge paths (presupuesto sin pagos, presupuesto inexistente)
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify the payment status label at 320px, 768px and 1024px
- [ ] 7.4 n/a — this change has a UI surface, covered above

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Actualizar `docs/100-business/102-use-cases/CU15 – Procesar Pago.md` (pago parcial como curso alternativo válido) y `CU47 – Consultar Pago.md` (nuevo estado de pago consultable)
- [ ] 8.2 Update OpenAPI/Swagger annotations for `GET /api/v1/pagos/presupuesto/{idPresupuesto}/estado` and verify in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — estado de pago consultable por presupuesto
- [ ] 8.4 n/a — no superseded documents to archive
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #821`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/821_pagos-parciales-cuotas`
- [ ] 10.2 Open the PR titled `[#821] feat(pagos): estado de pago para presupuestos con pagos parciales`, referencing Issue #821 and Use Cases CU-15/CU-47
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment: registrar un pago parcial sobre un presupuesto de prueba, consultar `GET /api/v1/pagos/presupuesto/{id}/estado` (devuelve "parcialmente abonado"), registrar el pago restante (devuelve "saldado"); `GET /actuator/health` en verde
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue #821
- [ ] 12.4 Archive the change: `openspec archive pagos-parciales-cuotas`

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
