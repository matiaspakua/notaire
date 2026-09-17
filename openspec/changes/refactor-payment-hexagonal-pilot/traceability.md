# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #984 | open |
| Use Case | CU15 – Procesar Pago; CU47 – Consultar Estado de Pago (alcance de negocio del piloto; la decisión en sí es [ADR-021](../../../docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md)) | exists |
| Specification | `proposal.md` + `design.md`; sin delta de spec (`skip_specs: true` — refactor puro, sin cambio de comportamiento) | done |
| Branch | `refactor/984_hexagonal-architecture-payment-pilot` | created |
| Tasks | `tasks.md` | 47/60 complete |
| Commits | `8026af6`, `2f1f241`, `ce67675`, `b88f0c2`, `4f6b6f5`, `fa26014`, `0897b6a`, `eb5fe5a`, `0ab6116`, `da1b6e5`, `efd2239`, `8d93306`, `1f4e877`, `2b513aa` | local (not pushed) |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

Sin delta de spec: las "acceptance criteria" de este cambio son los criterios del
issue #984 y la conservación del comportamiento existente.

| Acceptance criterion (issue #984) | Test / evidence | Status |
|-----------------------------------|-----------------|--------|
| ADR-021 documenta decisión, layout de paquetes y rollout slice a slice | `docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md` | done |
| Paquetes `domain` / `application.port.in` / `application.port.out` / `application.usecase` / `adapter.in.web` / `adapter.out.persistence` introducidos para el slice | árbol bajo `backend-api/src/main/java/com/licensis/notaire/{domain,application,adapter}/**/payment` | done |
| Comportamiento de `PaymentController`/`PaymentService`/`BudgetResumenService` preservado (pruebas de caracterización pasan sin cambios) | `adapter/in/web/payment/PaymentControllerTest` (32), `unit/PaymentUseCaseTest` (24), `adapter/out/persistence/payment/PaymentPersistenceAdapterTest` (23), `unit/BudgetSummaryDtoTest` (4) | done |
| Puertos de salida con implementación real JPA; sin cambio en datos ni esquema | `PaymentPersistenceAdapter`, `BudgetLookupAdapter`; sin migración Flyway en el cambio | done |
| Casos de uso testeados con puertos falsos, sin contexto de Spring | `application/usecase/payment/*Test` (36) sobre `InMemoryPaymentRepository` / `InMemoryBudgetLookup` | done |
| Tests de integración siguen usando H2/PostgreSQL como antes | `integration/PaymentUseCaseIntegrationTest` extiende `ServiceIntegrationTest` | done |
| Rastro de auditoría no se pierde al mover el controller | `unit/AuditPointcutCoverageTest` (5, agnóstico de ubicación) | done |
| `mvn verify -pl backend-api` pasa | 1856 pruebas, 0 fallos; "All coverage checks have been met" | done |
| `bash scripts/run_pipeline.sh` pasa | — | pending |
| La cobertura no baja del ratchet de JaCoCo | LINE 86.51% / BRANCH 74.33% global; slice nuevo 94.9–100% por paquete | done |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md` | yes | `1f4e877` |
| `docs/200-architecture/202-ADR/README.md` | yes | `1f4e877` |
| `CHANGELOG.md` | no | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #984 abierto y en progreso; `proposal.md`, `design.md`, `tasks.md` completos; `skip_specs: true` justificado (refactor sin cambio de comportamiento) |
| 2 | Failing tests written, test cases designed | yes | `AuditPointcutCoverageTest` y `BudgetChargesTest` escritos antes de tocar producción; suites de casos de uso escritas contra puertos antes de mover el controller |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api`: 1856 pruebas, 0 fallos, gate de JaCoCo cumplido; ADR-021 e índice actualizados |
| 4 | CI green, review approved, no conflicts | no | — |
| 5 | Deployed, smoke test passed, Issue closed | no | — |

## Exceptions

None.
