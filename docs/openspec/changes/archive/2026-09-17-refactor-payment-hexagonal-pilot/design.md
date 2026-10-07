# Design — Piloto de arquitectura hexagonal (slice de pagos)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) §2, §7 y
> `.claude/rules/refactoring.md`. La decisión permanente vive en
> [ADR-021](../../../docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md);
> este documento cubre el *cómo* de esta ejecución en particular.

## Context

El circuito financiero (CU15/CU47) concentra las reglas de negocio más densas de
`backend-api` y a la vez las tiene más acopladas al framework: la validación de
sobrepago y la aritmética de descuentos/recargos solo eran ejercitables a través
de entidades JPA. El issue #984 pide probar Ports & Adapters en un único slice
vertical, con contrato REST intacto, antes de decidir si el patrón se extiende.

## Goals / Non-Goals

### Goals

- Aislar las reglas financieras del framework: dominio sin Spring ni JPA.
- Validar el diseño de los puertos de salida con **dos** implementaciones cada
  uno (adaptador JPA real + fake en memoria).
- Dejar el contrato REST byte a byte idéntico.
- No bajar la cobertura por debajo del ratchet de JaCoCo.

### Non-Goals

- Reescribir el resto de `backend-api`.
- Cambiar cualquier número financiero, aunque la aritmética legacy sea discutible.
- Cambiar el esquema de base de datos o los datos persistidos.
- Introducir un contenedor de composición manual: la composición sigue siendo
  Spring por constructor.

## Decisions

### D1 — Reproducir la aritmética legacy en vez de "arreglarla"

`BudgetCharges.total()` compone los recargos porcentuales sobre el **total
corriente**, no sobre el subtotal base, porque eso es lo que hacía
`PaymentService`. `BudgetSummaryService` deriva el total como
`pendingBalance + totalPaid` en vez de recalcularlo, para que el resultado en
coma flotante sea idéntico bit a bit al anterior. Un refactor que cambia números
financieros en silencio es un bug, no un refactor.

### D2 — Estrangular, no envolver

Se consideró dejar un `PaymentService` deprecado delegando en los casos de uso,
para no tocar a sus dos consumidores. Se descartó: una fachada que nadie necesita
es código muerto e invita a nuevos llamadores a saltarse los puertos. Recablear
`ManagementArchiveDebtService` y `ManagementResumenFinancieroService` cuesta dos
ediciones pequeñas y deja el slice completamente estrangulado.

### D3 — `existsById` en lugar de un segundo `findById`

La guarda de "pago no encontrado" en `EditPaymentService` solo necesita saber si
existe. Usar `existsById` evita un segundo viaje al repositorio en el camino
feliz y —esto es lo importante— permite conservar sin tocar la verificación
`verify(paymentRepository, times(1)).findById(1)` de la suite existente, en vez
de relajarla a `times(2)` para acomodar el refactor.

### D4 — `PaymentWebMapper` público como costura temporal

`BudgetController` sigue en el paquete `api` mientras consume
`GetBudgetSummaryUseCase`, así que necesita el mapper. Se documenta como costura
transitoria en el Javadoc y en ADR-021, con la condición explícita de volver a
`package-private` cuando el slice de presupuestos migre.

### D5 — Ampliar el pointcut de auditoría en el mismo commit que mueve el controller

`AuditAspect` filtraba por paquete (`com.licensis.notaire.api..*Controller`).
Mover el controller fuera de ese paquete lo habría sacado del rastro de auditoría
(ADR-013) **sin ningún test en rojo**. Era el riesgo de regresión silenciosa más
alto del cambio, así que se mitigó antes de tocar producción: primero
`AuditPointcutCoverageTest`, escrito para ser agnóstico de la ubicación del
controller, y recién después el movimiento junto con la ampliación del pointcut.

## Riesgos / Trade-offs

| Riesgo | Mitigación |
|--------|------------|
| Regresión silenciosa en el rastro de auditoría al mover el controller. | `AuditPointcutCoverageTest` (agnóstico de ubicación), escrito **antes** del movimiento. D5. |
| Deriva del contrato REST al reescribir el controller. | Las 32 pruebas de `PaymentControllerTest` se conservan con los mismos `@DisplayName`, aserciones de body y códigos de estado; solo cambia el colaborador mockeado. |
| Cambio de números por reordenar operaciones en coma flotante. | D1: aritmética reproducida y derivación del total preservada. |
| Más tipos para el mismo comportamiento (6 casos de uso donde había 1 service). | Aceptado y documentado como trade-off en ADR-021; por eso el patrón se limita a un slice y se revisa antes de extenderlo. |
| Mensajes de log que cambian y rompen alertas u observabilidad. | Los mensajes del controller se compararon línea a línea con el legacy (p. ej. se restauró "Error al actualizar pago ID={}"). |

## Testing Strategy

TDD en el orden que exige P1, adaptado a un refactor (CONSTITUTION §7: las
suites existentes son las pruebas de caracterización):

1. **Pin antes de tocar nada**: `AuditPointcutCoverageTest` (5 pruebas) fija la
   cobertura del pointcut de forma agnóstica a la ubicación del controller.
2. **Dominio primero, sin framework**: `domain/payment/BudgetChargesTest` ejercita
   la aritmética de cargos sin Spring, JPA ni mocks.
3. **Casos de uso contra fakes**: `application/usecase/payment/*Test` (36 pruebas)
   usan `InMemoryPaymentRepository` e `InMemoryBudgetLookup`. Que cada puerto de
   salida tenga dos implementaciones es lo que valida que el puerto sea un puerto
   y no un repositorio disfrazado.
4. **Suites existentes retargeteadas, nunca debilitadas**: mismos `@DisplayName`,
   mismas aserciones, mismos mensajes de excepción, mismas verificaciones de
   repositorio.

| Antes | Después | Qué ejercita |
|-------|---------|--------------|
| `unit/PaymentServiceTest` | `unit/PaymentUseCaseTest` | casos de uso sobre adaptadores reales, repositorios mockeados |
| `service/unit/PaymentServiceTest` | `adapter/out/persistence/payment/PaymentPersistenceAdapterTest` | los adaptadores de salida |
| `unit/PaymentControllerTest` | `adapter/in/web/payment/PaymentControllerTest` | el controller sobre puertos de entrada mockeados |
| `unit/BudgetResumenServiceTest` | `unit/BudgetSummaryDtoTest` | `BudgetSummaryService` + mapeo a `DtoBudgetResumen` |
| `integration/PaymentServiceIntegrationTest` | `integration/PaymentUseCaseIntegrationTest` | beans de caso de uso end-to-end sobre los adaptadores JPA reales |

## Regression Strategy

Al no haber delta de comportamiento (`skip_specs: true`), la red de seguridad es
la suite completa, no un conjunto nuevo de aserciones:

- `mvn test -pl backend-api` completo en verde, sin pruebas saltadas ni
  `@Disabled` agregados.
- `mvn verify -pl backend-api` con el gate de JaCoCo: la cobertura del slice
  nuevo debe quedar en línea o por encima de la del código que reemplaza.
- Ninguna aserción preexistente se relaja para acomodar el refactor. Donde el
  refactor chocaba con una verificación (segundo `findById` en la edición), se
  cambió el código de producción, no la prueba (D3).

## Playwright Strategy

No hay cambios de UI ni de contrato REST, así que no se agregan escenarios E2E
nuevos. La suite existente `TS-0014-pagos-workflow.spec.ts` cubre el circuito de
pagos desde el dashboard y actúa como verificación end-to-end de que el contrato
no se movió; se ejecuta vía `bash scripts/run_pipeline.sh` antes del PR.

## Deployment Strategy

Despliegue normal del backend, sin pasos previos ni posteriores: no hay migración
Flyway, no hay cambio de configuración ni de `.env`, y no hay cambio de contrato
que obligue a coordinar con el frontend. Un despliegue estándar del artefacto de
`backend-api` alcanza.

## Rollback Strategy

Revertir los commits del slice y redesplegar. Es seguro porque el cambio es
puramente estructural: no hay migración que deshacer, ni datos escritos en un
formato nuevo, ni contrato que los clientes ya hayan adoptado. No hay ventana en
la que una versión revertida quede incompatible con datos producidos por la
versión nueva.

## Migration Plan

Sin migración de datos. La migración es de código y se hizo en commits atómicos,
cada uno compilando y con la suite en verde:

1. Fijar la cobertura del pointcut de auditoría.
2. Extraer el dominio sin framework.
3. Definir puertos de entrada y salida.
4. Casos de uso orquestando solo a través de puertos.
5. Adaptadores JPA de salida.
6. Pruebas de casos de uso contra fakes en memoria.
7. Mover el controller al adaptador de entrada (+ ampliar el pointcut).
8. Retargetear las suites legacy.
9. Recablear `BudgetController` y borrar `BudgetResumenService`/`PaymentMapper`.
10. Recablear los servicios de gestión y borrar `PaymentService`/`StatusPayment`.
11. ADR-021 y documentación.

## Open Questions

- ¿Se extiende el patrón a un segundo slice? Se decide después de revisar este
  piloto; el candidato natural es el resumen financiero de gestión.
- ¿La aritmética compuesta de `BudgetCharges.total()` es la intención de negocio
  o un bug heredado? Necesita confirmación funcional; si es un bug, issue propio.
