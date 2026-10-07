# Tasks — Piloto de arquitectura hexagonal (slice de pagos)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) §5 Official SDLC Workflow.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #984 existe y está abierto
- [x] 1.2 Alcance de negocio atado a CU15 y CU47; la decisión arquitectónica se
      registra como ADR-021
- [x] 1.3 Issue movido a `in-progress`
- [x] 1.4 `proposal.md`, `design.md` y `traceability.md` escritos
- [x] 1.5 `skip_specs: true` justificado en `.openspec.yaml` (refactor puro: sin
      cambio de contrato, de datos ni de esquema)

## 2. Crear branch

- [x] 2.1 Branch `refactor/984_hexagonal-architecture-payment-pilot` creado desde `main` actualizado

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `unit/AuditPointcutCoverageTest` — fija la cobertura del pointcut de
      auditoría de forma agnóstica a la ubicación del controller, **antes** de
      mover nada
- [x] 3.2 `domain/payment/BudgetChargesTest` — aritmética de cargos sin framework
- [x] 3.3 `application/usecase/payment/InMemoryPaymentRepository` e
      `InMemoryBudgetLookup` — segunda implementación de cada puerto de salida
- [x] 3.4 `ProcessPaymentServiceTest`, `PaymentStatusServiceTest`,
      `EditPaymentServiceTest`, `PaymentQueryAndDeleteServiceTest`,
      `BudgetSummaryServiceTest` — 36 pruebas contra fakes

## 4. Implementación

- [x] 4.1 `domain.payment`: `PaymentDetails`, `PaymentStatus`, `BudgetCharges`, `ChargeLine`
- [x] 4.2 `application.port.in.payment`: 6 puertos de entrada + records de comando/resultado
- [x] 4.3 `application.port.out.payment`: `PaymentRepositoryPort`, `BudgetLookupPort` + records
- [x] 4.4 `application.usecase.payment`: 6 casos de uso, `@Transactional` en el caso de uso
- [x] 4.5 `adapter.out.persistence.payment`: `PaymentPersistenceAdapter`, `BudgetLookupAdapter`
- [x] 4.6 `adapter.in.web.payment`: `PaymentController` delgado + `PaymentWebMapper`
- [x] 4.7 `PaymentUpdateRequest` reemplaza la entidad JPA como `@RequestBody` de `PUT`
- [x] 4.8 Pointcut de `AuditAspect` ampliado a `adapter.in.web..*Controller`
- [x] 4.9 `BudgetController` consume `GetBudgetSummaryUseCase`
- [x] 4.10 `ManagementArchiveDebtService` y `ManagementResumenFinancieroService`
      consumen los puertos de entrada
- [x] 4.11 Borrar `api/PaymentController`, `service/PaymentService`,
      `service/StatusPayment`, `service/BudgetResumenService`,
      `service/mappers/PaymentMapper`
- [x] 4.12 Copia defensiva de fechas en los records de request (SpotBugs EI_EXPOSE_REP)

## 5. Actualizar tests existentes

- [x] 5.1 `unit/PaymentControllerTest` → `adapter/in/web/payment/PaymentControllerTest` (32)
- [x] 5.2 `service/unit/PaymentServiceTest` → `adapter/out/persistence/payment/PaymentPersistenceAdapterTest` (23)
- [x] 5.3 `unit/PaymentServiceTest` → `unit/PaymentUseCaseTest` (24)
- [x] 5.4 `unit/BudgetResumenServiceTest` → `unit/BudgetSummaryDtoTest` (4)
- [x] 5.5 `integration/PaymentServiceIntegrationTest` → `integration/PaymentUseCaseIntegrationTest` (26)
- [x] 5.6 `unit/PaginationTest` y `unit/SimpleControllersTest` recableados a `GetBudgetSummaryUseCase`
- [x] 5.7 `ManagementArchiveDebtServiceTest` y `ManagementResumenFinancieroServiceTest`
      recableados a los puertos
- [x] 5.8 Verificado: ninguna aserción, `@DisplayName` ni mensaje de excepción se debilitó

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — 1856 pruebas, 0 fallos, 0 saltadas
- [x] 6.2 `mvn verify -pl backend-api` — BUILD SUCCESS, "All coverage checks have been met"
- [x] 6.3 `mvn checkstyle:check -pl backend-api` — sin violaciones nuevas (se corrigió el
      único import no usado introducido por el cambio)
- [x] 6.4 `mvn spotbugs:check -pl backend-api -DskipSpotBugs=false` — 789 → 785 hallazgos
- [ ] 6.5 `mvn test -Ppg-integration` contra PostgreSQL

## 7. Ejecutar Playwright

- [ ] 7.1 `TS-0014-pagos-workflow.spec.ts` en verde contra el stack levantado
- [x] 7.2 Confirmado que no hacen falta escenarios E2E nuevos (sin cambio de UI ni de contrato)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md`
- [x] 8.2 `docs/200-architecture/202-ADR/README.md` — índice y conteo por categoría
- [x] 8.3 `CHANGELOG.md` — entry en `[Unreleased] / Changed`

## 9. Commits atómicos

- [x] 9.1 Un commit por paso del plan de migración, cada uno compilando por separado
- [x] 9.2 Todos referencian `#984` sin `Closes` (ninguno cierra el issue por sí solo)
- [x] 9.3 El commit que cierra el issue lleva `Closes #984` — pendiente hasta el PR

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` en verde
- [ ] 10.2 Push del branch
- [ ] 10.3 PR creado referenciando `Fixes #984`
- [ ] 10.4 `gh pr view --json mergeable,mergeStateStatus` → `MERGEABLE`
- [ ] 10.5 CI en verde

## 11. Deploy

- [ ] 11.1 Deploy estándar del backend (sin migración Flyway ni cambio de configuración)

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test del circuito de pagos en el dashboard
- [ ] 12.2 Issue #984 cerrado

## Definition of Done

- [x] Comportamiento observable idéntico: URLs, payloads, códigos de estado,
      datos persistidos y esquema sin cambios
- [x] Suite completa en verde, sin pruebas saltadas ni aserciones relajadas
- [x] Cobertura por encima del ratchet de JaCoCo
- [x] Reglas de negocio testeables sin Spring ni JPA
- [x] Cada puerto de salida con dos implementaciones (JPA real + fake en memoria)
- [x] Sin código muerto: los servicios superados fueron borrados, no deprecados
- [x] ADR-021 registra la decisión, el trade-off y lo que queda pendiente
- [ ] Pipeline completo y CI en verde
- [ ] PR sin conflictos y issue cerrado
