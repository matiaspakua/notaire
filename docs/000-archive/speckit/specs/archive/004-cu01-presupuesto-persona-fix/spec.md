# Feature Specification: Presupuesto persistence must keep its client association

**Feature Branch**: `fix/883_presupuesto-persona-association`

**Created**: 2026-08-31

**Status**: Draft

**Input**: User description: "Presupuesto creation from the real UI silently drops the persona/cliente association: `POST/PUT /api/v1/presupuestos` bind to the raw `Presupuesto` JPA entity, whose relation field is `fkIdPersona`, while the frontend sends `persona` (the DTO's field name). Fix the API contract so a Presupuesto created or edited from the UI keeps its client link."

## Notaire Traceability *(mandatory — CONSTITUTION.md §3)*

| Field | Value |
|-------|-------|
| **GitHub Issue** | #883 |
| **Use Case** | CU01 — Preparar Presupuesto (`docs/100-business/102-use-cases/CU01 – Preparar Presupuesto.md`) |

## Verified starting point *(not boilerplate — read before scaffolding)*

- `PresupuestoController` (`backend-api/.../api/PresupuestoController.java`)
  binds `create`/`update` directly to the raw entity:
  `@RequestBody Presupuesto entity`.
- `Presupuesto` (`backend-api/.../negocio/Presupuesto.java`) declares the
  client relation as `private Persona fkIdPersona;`
  (`@JoinColumn(name = "fk_id_persona")`), and only maps it to/from the
  DTO's public `persona` field inside `getDto()` / `setAtributos()` — methods
  the raw-entity endpoints never call on the way in.
- The frontend (`frontend/src/types/index.ts`'s `Presupuesto` interface,
  used by `frontend/src/app/dashboard/presupuestos/page.tsx`) sends the
  field as `persona`, matching `DtoPresupuesto`, not the entity.
- Confirmed via manual reproduction: `POST /api/v1/presupuestos` with
  `"persona":{"idPersona":1}` returns `201` with `"fkIdPersona":null`;
  the same request with `"fkIdPersona":{"idPersona":1}` correctly persists
  the relation. No new entity, no Flyway migration — this is a request/
  response contract bug, the same class as #879.
- Other read endpoints (`GET /presupuestos`, `GET /presupuestos/{id}`) also
  return the raw entity, so they already expose `fkIdPersona` (not
  `persona`) in JSON today — the frontend's list/detail rendering that reads
  `p.persona` is *also* silently broken for the same reason, not just create/
  update. See User Story 2.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Crear un Presupuesto con cliente asociado (Priority: P1)

El Gestor/Escribano crea un Presupuesto desde la pantalla de Presupuestos,
eligiendo un cliente existente. El sistema debe persistir esa asociación.

**Why this priority**: Sin esto, todo presupuesto creado desde la UI queda
sin cliente asociado — CU01 queda roto de punta a punta pese a que el
formulario aparenta funcionar.

**Independent Test**: Enviar `POST /api/v1/presupuestos` con el campo que
usa el frontend (`persona`) y verificar que el Presupuesto persistido queda
vinculado a esa persona.

**Acceptance Scenarios**:

1. **Given** una persona existente, **When** se crea un Presupuesto enviando esa persona en el campo que usa el frontend, **Then** el Presupuesto persistido queda vinculado a esa persona (verificable vía `GET /api/v1/presupuestos/{id}`).
2. **Given** un Presupuesto existente sin cliente, **When** se lo edita para asociarle un cliente, **Then** el Presupuesto persistido queda vinculado a esa persona.
3. **Given** un Presupuesto sin cliente asociado (campo omitido), **When** se lo crea, **Then** el sistema lo acepta igualmente (la asociación es opcional, sin excepciones ni datos corruptos).

---

### User Story 2 - Leer un Presupuesto con su cliente asociado (Priority: P1)

El Gestor/Escribano lista o busca Presupuestos y debe ver el cliente
asociado a cada uno, no un campo vacío.

**Why this priority**: La búsqueda por apellido de cliente
(`/dashboard/presupuestos`, CU60) depende de que la respuesta de lectura
incluya el cliente con el mismo nombre de campo que el frontend espera.

**Independent Test**: Crear un Presupuesto con cliente asociado y verificar
que `GET /api/v1/presupuestos` y `GET /api/v1/presupuestos/{id}` devuelven
ese cliente en el campo que el frontend consume.

**Acceptance Scenarios**:

1. **Given** un Presupuesto con cliente asociado, **When** se lo consulta por `GET /api/v1/presupuestos/{id}`, **Then** la respuesta incluye los datos del cliente asociado en el campo que el frontend consume.
2. **Given** varios Presupuestos, algunos con cliente asociado y otros sin él, **When** se listan vía `GET /api/v1/presupuestos`, **Then** cada uno refleja correctamente si tiene o no cliente asociado, sin mezclar datos entre registros.

---

### Edge Cases

- ¿Qué pasa si se envía un `idPersona` inexistente? El sistema no debe crear
  el Presupuesto con una referencia inválida — debe responder con un error
  claro (400/404), no una excepción no controlada ni un dato corrupto.
- ¿Qué pasa con Presupuestos ya existentes en la base, creados antes de este
  fix, sin cliente asociado? Quedan igual (no se migran datos existentes) —
  fuera de alcance de este cambio.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE persistir la asociación cliente/persona de un Presupuesto cuando se crea (`POST /api/v1/presupuestos`) usando el mismo nombre de campo que envía el frontend.
- **FR-002**: El sistema DEBE persistir la asociación cliente/persona de un Presupuesto cuando se edita (`PUT /api/v1/presupuestos/{id}`) usando el mismo nombre de campo que envía el frontend.
- **FR-003**: El sistema DEBE devolver la asociación cliente/persona de un Presupuesto en las respuestas de lectura (`GET /api/v1/presupuestos`, `GET /api/v1/presupuestos/{id}`) usando el mismo nombre de campo que el frontend consume.
- **FR-004**: El sistema DEBE seguir aceptando la creación/edición de un Presupuesto sin cliente asociado (campo opcional).

### Key Entities

- **Presupuesto**: entidad existente; este cambio corrige cómo su relación
  con `Persona` se expone/recibe en la API REST, sin nueva columna ni
  migración.
- **Persona**: entidad existente, sin cambios.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de los Presupuestos creados o editados desde la UI con un cliente seleccionado quedan correctamente vinculados a ese cliente.
- **SC-002**: El 100% de las lecturas de Presupuesto (lista y detalle) reflejan correctamente el cliente asociado, permitiendo que la búsqueda por apellido (CU60) vuelva a funcionar.
- **SC-003**: Ningún Presupuesto existente pierde datos por este cambio (no se toca ningún registro fuera de las pruebas propias del cambio).

## Assumptions

- No se requiere migración de datos: los Presupuestos ya creados sin cliente
  simplemente quedan sin cliente, igual que hoy.
- La autenticación/autorización reutiliza el esquema JWT existente.
- El fix se limita al contrato de `PresupuestoController`; no se tocan otros
  controladores que ya usan DTOs correctamente.
