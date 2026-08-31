# Feature Specification: Gestión form's presupuesto picker must identify the client

**Feature Branch**: `fix/889_presupuesto-picker-shows-client-name`

**Created**: 2026-08-31

**Status**: Draft

**Input**: User description: "The Gestión creation form's presupuesto dropdown only shows `Presupuesto #{id}`, with no client name, amount, or date, even though the API now returns the associated `persona` on each Presupuesto (#883). A user creating a Gestión for a specific client cannot tell which presupuesto belongs to that client from the picker alone."

## Notaire Traceability *(mandatory — CONSTITUTION.md §3)*

| Field | Value |
|-------|-------|
| **GitHub Issue** | #889 |
| **Use Case** | CU02 — Iniciar Gestión (`docs/100-business/102-use-cases/CU02 – Iniciar Gestión.md`) |

## Verified starting point *(not boilerplate — read before scaffolding)*

- `frontend/src/app/dashboard/gestiones/page.tsx:297-298` renders the
  presupuesto `<Select>` options as `Presupuesto #{p.idPresupuesto}` only.
- `Presupuesto.persona` (`frontend/src/types/index.ts:198`) is already typed
  and populated by the backend (fixed in #883), but unused in this render.
- Confirmed via a real end-to-end run of
  `frontend/tests/e2e/02-demo-two-full-cases.spec.ts` driving the actual UI:
  the Gestión step cannot select the presupuesto belonging to the just-created
  client because the option text carries no client-identifying information.
- No backend change needed — the data is already in the API response
  consumed by `usePresupuestos.ts`; this is a frontend rendering-only fix.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Elegir el presupuesto del cliente correcto al iniciar una Gestión (Priority: P1)

El Gestor/Escribano abre el formulario de nueva Gestión y debe poder
identificar, sin salir del formulario, cuál presupuesto pertenece al
cliente para el que está iniciando la gestión.

**Why this priority**: Sin esto, el usuario puede vincular por error una
Gestión al presupuesto de otro cliente, ya que el único dato visible es un
id interno sin significado de negocio — CU02 queda expuesto a un error de
selección silencioso.

**Independent Test**: Abrir el formulario de nueva Gestión y verificar que
cada opción del picker de presupuesto muestra el nombre del cliente
asociado (cuando existe).

**Acceptance Scenarios**:

1. **Given** un Presupuesto con cliente asociado, **When** se abre el
   picker de presupuesto en el formulario de nueva Gestión, **Then** la
   opción correspondiente muestra el nombre y apellido del cliente, no solo
   el id del presupuesto.
2. **Given** un Presupuesto sin cliente asociado (campo opcional), **When**
   se abre el picker, **Then** la opción sigue mostrando el id del
   presupuesto sin romper el listado ni mostrar datos inválidos.

### Edge Cases

- Presupuestos sin `persona` asociada deben seguir apareciendo en la lista,
  degradando de forma legible (solo el id), no ocultos ni con error.
- Nombres de cliente muy largos no deben romper el layout del `Select`
  (comportamiento existente de truncado/overflow del componente `Select` ya
  cubre esto — no se introduce lógica nueva de truncado).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE mostrar el nombre y apellido del cliente
  asociado a cada presupuesto en el picker de presupuesto del formulario de
  nueva Gestión, cuando ese presupuesto tiene un cliente asociado.
- **FR-002**: El sistema DEBE seguir mostrando el id del presupuesto como
  identificador cuando no hay cliente asociado.

### Key Entities

- **Presupuesto**: entidad existente, sin cambios; solo cambia cómo se
  renderiza su opción en el picker del frontend.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de los presupuestos con cliente asociado muestran ese
  cliente en el picker de la Gestión.
- **SC-002**: Ningún presupuesto desaparece ni rompe el picker por no tener
  cliente asociado.

## Assumptions

- Cambio exclusivamente de frontend; no se toca el backend ni el contrato
  de la API.
- No se requiere migración de datos.
