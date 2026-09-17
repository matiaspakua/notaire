# Las reglas de negocio del circuito financiero no se pueden testear sin JPA ni Spring

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #984 |
| Use Case | CU15 – Procesar Pago; CU47 – Consultar Estado de Pago. Decisión arquitectónica registrada en [ADR-021](../../../docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md) |
| Branch | `refactor/984_hexagonal-architecture-payment-pilot` |
| Gate 1 status | passed |

## Objetivo

`backend-api` arrastra del monolito Swing un layering `api → service →
repository/jpa` en el que las reglas de negocio viven pegadas al framework.
En el circuito financiero esto duele en dos lugares concretos:

1. `PaymentService` operaba directamente sobre las entidades JPA `Payment` y
   `Budget`, así que la regla de sobrepago (#848) y la aritmética de
   descuentos/recargos (#822) solo se podían ejercitar con un contexto de
   persistencia o con repositorios mockeados. El `PaymentController` ligaba la
   entidad JPA `Payment` como `@RequestBody`, con lo que la persistencia se
   filtraba hasta el contrato REST.
2. El cálculo de saldo pendiente estaba duplicado en `PaymentService`,
   `BudgetResumenService` y los dos servicios de resumen de gestión, con
   manejo de nulos ligeramente distinto en cada uno.

El slice de pagos/presupuestos es el mejor lugar para probar Ports & Adapters:
es donde las reglas son más densas, ya está cubierto end-to-end por
`TS-0014-pagos-workflow.spec.ts`, y su contrato REST lo consume el dashboard,
así que cualquier regresión se ve de inmediato.

## What Changes

- Se introducen, **solo para el slice de pagos/presupuestos**, los paquetes
  `domain.payment`, `application.port.in.payment`, `application.port.out.payment`,
  `application.usecase.payment`, `adapter.in.web.payment` y
  `adapter.out.persistence.payment`.
- El modelo de dominio (`PaymentDetails`, `PaymentStatus`, `BudgetCharges`,
  `ChargeLine`) queda sin Spring ni JPA: solo JDK.
- `PaymentController` pasa a ser un adaptador de entrada delgado que depende de
  los puertos de entrada, no de servicios. `@Transactional` se mueve del
  controller a los casos de uso.
- Las entidades JPA `Payment` y `Budget` quedan confinadas a los dos adaptadores
  de salida.
- `PUT /api/v1/pagos/{id}` liga un record `PaymentUpdateRequest` en lugar de la
  entidad JPA `Payment`. Los campos JSON aceptados son los mismos.
- El pointcut de `AuditAspect` se amplía a `adapter.in.web..*Controller` en el
  mismo commit que mueve el controller, para que la auditoría no se pierda.
- Se eliminan `service/PaymentService`, `service/StatusPayment`,
  `service/BudgetResumenService`, `service/mappers/PaymentMapper` y
  `api/PaymentController`, ya superados.
- `ManagementArchiveDebtService` y `ManagementResumenFinancieroService` consumen
  los puertos de entrada en lugar de `PaymentService`.

**BREAKING CHANGES:** Ninguno. URLs, payloads de request/response, códigos de
estado, datos persistidos y esquema de base de datos quedan idénticos.

## Reglas de negocio

Este cambio **no introduce ni modifica ninguna regla de negocio** — las mueve de
sitio. Se deja constancia de las que quedan explícitas al extraerse al dominio:

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un pago no puede exceder el saldo pendiente del presupuesto. | CU15, issue #848 | Made explicit — pasa de estar embebida en `PaymentService` a `ProcessPaymentService` sobre `BudgetCharges.pendingBalanceAfter`. |
| El monto de un pago debe ser mayor a cero. | CU15 | Made explicit — misma validación, ahora en el caso de uso. |
| El estado de pago es `NoPayments` sin pagos, `PAID` con saldo ≤ 0, `PARTIAL` en el resto. | CU47 | Made explicit — pasa de un `switch` en el service al tipo de dominio `PaymentStatus.of`. |
| Los ítems `DESCUENTO` restan del total y los `RECARGO` suman, compuestos sobre el total corriente. | CU15, issue #822 | Made explicit — se reproduce la aritmética legacy **exactamente** en `BudgetCharges.total()`. Ver design.md, Decisions. |

## Capabilities

### New Capabilities

_None — no hay capability nueva; el comportamiento observable es el mismo._

### Modified Capabilities

_None — ninguna capability cambia de comportamiento. La reestructuración es
interna y está documentada en ADR-021, no en un delta de spec (`skip_specs: true`)._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Nuevos paquetes `domain`/`application`/`adapter` para el slice de pagos; `api/PaymentController`, `service/PaymentService`, `service/StatusPayment`, `service/BudgetResumenService` y `service/mappers/PaymentMapper` eliminados; `api/BudgetController`, `audit/AuditAspect`, `ManagementArchiveDebtService` y `ManagementResumenFinancieroService` recableados. |
| `frontend` | no | El contrato REST no cambia. |
| `notaire-shared` | no | `DtoPaymentResponse` y `DtoBudgetResumen` se mantienen tal cual. |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: ninguna cambia de forma. `Payment` y `Budget` se siguen leyendo y
  escribiendo igual, ahora solo desde los adaptadores de salida.
- Endpoints: `/api/v1/pagos/**` y `/api/v1/presupuestos/{id}/resumen` — mismas
  firmas, mismos códigos de estado, mismos payloads.
- Database (Flyway `V{n}`): **ninguna migración**. El esquema no cambia.
- Configuration / `.env`: none.
- Dependencies: none new.

### Architecture review

Requiere ADR — es un patrón arquitectónico distinto al de
[ADR-002](../../../docs/200-architecture/202-ADR/ADR-002-module-structure.md),
aplicado deliberadamente a un solo slice.
[ADR-021](../../../docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md)
registra la decisión, el layout de paquetes, el trade-off y qué queda pendiente.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md` | Nuevo — decisión, layout, trade-offs y seguimiento del piloto. |
| `docs/200-architecture/202-ADR/README.md` | Índice: agregar ADR-021 y mover "Code Organization" a 2 ADRs. |
| `CHANGELOG.md` | Entry: reestructuración interna del circuito de pagos sin cambio de contrato. |

## Out of Scope

- El resto de `backend-api` (Escritura, Folio, Testimonio, Personas, etc.) — se
  aplicará slice por slice en issues de seguimiento, una vez validado el piloto.
- `Budget` / `BudgetService` / `BudgetController` en su núcleo: solo se recablea
  el endpoint de resumen (CU47), el resto del slice de presupuestos queda igual.
- Corregir la aritmética legacy de `BudgetCharges.total()` si resultara errónea —
  un refactor no debe cambiar números financieros en silencio; sería un issue
  propio con su caso de uso.
- Migrar `ManagementResumenFinancieroService` y `ManagementArchiveDebtService` a
  hexagonal: consumen los puertos, pero siguen siendo servicios en capas.
