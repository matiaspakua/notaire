# Conectar el motor de estados y la bitácora a la pantalla real de gestión

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #833 |
| Use Case | CU13 – Ver historial de gestión (#166); CU83 – Definir Workflow de Estados y Transiciones (#451, #453, #454, #455); CU16 – Archivar Gestión (#169, #819) |
| Branch | `feat/833_gestion-workflow-y-bitacora` |
| Gate 1 status | pending |

## Objetivo

El motor de definición de workflows (CU83) ya existe y valida que un
`WorkflowDefinition` sea consistente (un nodo inicial, al menos un nodo
final alcanzable), y ya expone un trace de solo lectura
(`GET /{id}/workflow-trace`, `WorkflowTraceService`) que cruza el
`WorkflowNode`/`WorkflowTransition` de un tipo de trámite con el
`Historial` de una gestión real. Pero nada en el flujo real de alta/edición
de una gestión (`GestionController`) escribe en `Historial`, así que la
bitácora que CU13 promete mostrar está vacía para cualquier caso real. Y
el endpoint `PUT /{id}` de gestión acepta la entidad completa —incluido
`estado`— sin validar esa transición contra las `WorkflowTransition`
definidas para el tipo de trámite, así que hoy nada impide, a nivel de
API, mover una gestión de "Borrador" a "Registrada" o viceversa sin pasar
por el grafo. El endpoint de archivado (`POST /{id}/archivar`, CU16,
issue #819) ya es una acción explícita en la UI, pero tampoco valida la
transición contra el workflow ni deja rastro en `Historial` (`openspec/explore.md`,
hallazgo 3).

## What Changes

- Nueva acción de negocio "Transicionar estado de gestión": valida el
  cambio de estado propuesto contra las `WorkflowTransition` del
  `WorkflowDefinition` asignado al `TipoDeTramite` de la gestión (nodo
  origen = estado actual, nodo destino = estado propuesto debe existir
  como arista), en lugar de aceptarlo sin regla vía el `PUT` genérico.
- Cada transición válida (incluida la creación inicial de la gestión y el
  archivado) SHALL registrar una entrada en `Historial` con el estado, la
  fecha y las observaciones, poblando la bitácora que CU13 necesita.
- `POST /{id}/archivar` (CU16) pasa a reutilizar la misma validación de
  transición — solo archiva si "Archivada" es un destino válido desde el
  estado actual según el workflow del tipo de trámite— y registra su
  propia entrada de `Historial`.
- La pantalla de gestión (`frontend/src/app/dashboard/gestiones`) expone
  una acción explícita "Cambiar estado" que ofrece solo los estados
  destino válidos según el workflow, y una vista de bitácora que lista el
  `Historial` de la gestión seleccionada (CU13).

**BREAKING CHANGES:** Ninguno — `GestionController`, `Historial`,
`WorkflowDefinition`/`WorkflowNode`/`WorkflowTransition` y
`GestionArchiveDebtService` se mantienen; este cambio añade una capa de
validación y de registro sobre las acciones que ya existen.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un cambio de estado de gestión solo es válido si existe una `WorkflowTransition` del estado actual al estado propuesto, dentro del `WorkflowDefinition` del tipo de trámite de la gestión. | CU83 | New |
| Toda gestión creada, transicionada o archivada registra una entrada en su `Historial` con estado, fecha y observaciones. | CU13, RF-24, RF-110 | Made explicit |
| Archivar una gestión (CU16) solo es válido si "Archivada" es un destino alcanzable desde el estado actual según el workflow del tipo de trámite. | CU83, CU16 | New |
| El saldo pendiente se sigue advirtiendo (sin bloquear) al archivar, como ya implementa `GestionArchiveDebtService` (issue #819). | CU16, RF-22, RF-37 | Sin cambios |

## Capabilities

### New Capabilities
- `gestion-workflow-transicion`: valida y aplica un cambio de estado de una
  gestión contra las transiciones definidas en el `WorkflowDefinition` de
  su tipo de trámite.
- `gestion-bitacora`: registra en `Historial` cada cambio de estado
  relevante de una gestión (alta, transición, archivado) y lo expone para
  consulta (CU13).

### Modified Capabilities
- `gestion-archive-debt-check`: el archivado pasa a validar la transición
  contra el workflow antes de aplicarse, y registra una entrada de
  `Historial` al confirmarse. El cálculo y la advertencia de saldo
  pendiente (issue #819) no cambian.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Nuevo endpoint de acción `POST /api/v1/gestiones/{id}/transicionar`; nuevo servicio `GestionTransitionService` (valida contra `WorkflowTransitionRepository`/`WorkflowNodeRepository`, escribe `Historial`); `GestionArchiveDebtService.archivar` pasa a delegar la validación de transición al mismo servicio y a escribir `Historial`; nuevo endpoint `GET /api/v1/gestiones/{id}/historial` para CU13. |
| `frontend` | yes | Acción "Cambiar estado" (con selector limitado a destinos válidos) y vista de bitácora en `frontend/src/app/dashboard/gestiones`. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `GestionDeEscritura`, `Historial`, `WorkflowNode`,
  `WorkflowTransition` (sin cambios de esquema; se agrega un servicio de
  negocio que las cruza).
- Endpoints (nuevos): `POST /api/v1/gestiones/{id}/transicionar`;
  `GET /api/v1/gestiones/{id}/historial`.
- Endpoints (comportamiento cambiado, misma firma):
  `POST /api/v1/gestiones/{id}/archivar` (ahora valida transición y
  escribe `Historial`, además de lo que ya hacía).
- Database (Flyway `V{n}`): ninguna — `Historial`, `WorkflowNode` y
  `WorkflowTransition` ya existen con todos los campos necesarios.
- Configuration / `.env`: none.
- Dependencies: none new.

### Architecture review

Sigue el layering existente (`service` para las reglas de negocio, `api`
para los controllers de acción) y reutiliza el motor de workflow ya
implementado bajo [ADR-014](../../200-architecture/202-ADR/ADR-014-workflow-engine.md).
No introduce un patrón arquitectónico nuevo, no requiere un nuevo ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU13 – Ver historial de gestión.md` | Anotar el endpoint de bitácora nuevo en Referencias Cruzadas. |
| `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` | Anotar que el archivado ahora valida la transición contra el workflow. |
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | Anotar que las transiciones definidas aquí ahora se validan en el flujo real de gestión, no solo en el trace de solo lectura. |
| `CHANGELOG.md` | Entry: los cambios de estado de una gestión (incluido el archivado) se validan contra el workflow definido y quedan registrados en su bitácora. |

## Out of Scope

- El diseño/edición de `WorkflowDefinition`, `WorkflowNode` y
  `WorkflowTransition` en sí (CU83 ya cubre su propio CRUD y validación
  estructural) — este cambio solo consume esas definiciones desde el
  flujo real de gestión.
- Cualquier cambio al cálculo de saldo pendiente o a la advertencia de
  deuda al archivar (issue #819, ya implementado).
- Presupuestar con plantillas/ítems (issue #834) y las suplencias sin
  efecto práctico (issue #836), que tocan la misma pantalla de gestión
  desde otro ángulo pero no se resuelven aquí.
