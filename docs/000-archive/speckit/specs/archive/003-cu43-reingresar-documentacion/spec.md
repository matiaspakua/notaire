# Feature Specification: Reingresar documentación

**Feature Branch**: `feat/865_cu43-reingresar-documentacion`

**Created**: 2026-08-28

**Status**: Draft

**Input**: User description: "Implementar CU43 — un documento presentado ha sido devuelto observado o ha vencido; el Gestor/Escribano selecciona una gestión, ve sus trámites, selecciona uno, ve la documentación necesaria para ese trámite (PlantillaTramite) y elige el tipo de documento a reingresar; el sistema agrega un nuevo DocumentoPresentado (reingresado=true) a la lista de documentos presentados de la gestión."

## Notaire Traceability *(mandatory — CONSTITUTION.md §3)*

| Field | Value |
|-------|-------|
| **GitHub Issue** | #865 |
| **Use Case** | CU-43 — Reingresar documentación (#196) |

## Verified starting point *(not boilerplate — read before scaffolding)*

The domain model already supports this Use Case without a new migration:

- `DocumentoPresentado` (`backend-api/.../negocio/DocumentoPresentado.java`)
  already has a `reingresado` (Boolean) column and setter.
- `PlantillaTramite` (composite PK `PlantillaTramitePK(fkIdTipoTramite,
  fkIdTipoDocumento)`, added for CU03/#860) already resolves "documentación
  necesaria por trámite" via
  `PlantillaTramiteRepository.findByTipoDeTramiteIdTipoTramite(Integer)`.
- `TramiteRepository.findByFkIdGestionIdGestion(Integer)` already lists a
  gestión's trámites.
- `TipoDeDocumento` already carries `nombre`, `vence`, `diasVencimiento`,
  `quienEntrega` — the fields a new `DocumentoPresentado` needs when it is
  created from a reingreso.

The gap is: no endpoint composes "trámites de una gestión + su
documentación necesaria" in one call, no endpoint creates a new
`DocumentoPresentado` from a chosen `(idTramite, idTipoDocumento)` pair, and
no frontend screen. No new entity, no Flyway migration.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar trámites y documentación necesaria de una gestión (Priority: P1)

El Gestor/Escribano selecciona una gestión y el sistema le muestra sus
trámites junto con, para cada uno, la documentación necesaria según su
`PlantillaTramite`.

**Why this priority**: Pasos 3-6 del Curso de Eventos — sin esto no hay
información sobre la que elegir qué reingresar.

**Independent Test**: Seleccionar una gestión con al menos un trámite cuyo
`TipoDeTramite` tiene `PlantillaTramite` configurada, y verificar que la
respuesta agrupa la documentación necesaria por trámite.

**Acceptance Scenarios**:

1. **Given** una gestión con trámites cuyos tipos de trámite tienen documentación necesaria configurada, **When** el Gestor/Escribano la selecciona, **Then** el sistema devuelve cada trámite de la gestión junto con su lista de documentación necesaria (tipo de documento, si vence, días de vencimiento, quién entrega).
2. **Given** una gestión que no existe, **When** se solicita su documentación de reingreso, **Then** el sistema responde 404 sin exponer detalles técnicos.
3. **Given** una gestión cuyo trámite no tiene `PlantillaTramite` configurada, **When** se consulta, **Then** ese trámite se devuelve con una lista de documentación necesaria vacía, no un error.

---

### User Story 2 - Reingresar un tipo de documento (Priority: P1)

El Gestor/Escribano indica, para un trámite de la gestión, el tipo de
documento a reingresar, y el sistema agrega un nuevo `DocumentoPresentado`
(`reingresado=true`) a la lista de documentos presentados de la gestión.

**Why this priority**: Pasos 7-8 del Curso de Eventos — es la acción
principal del caso de uso.

**Independent Test**: Reingresar un tipo de documento que sí forma parte de
la `PlantillaTramite` del trámite elegido y verificar que se crea un
`DocumentoPresentado` con `reingresado=true`, vinculado a ese trámite y tipo
de documento.

**Acceptance Scenarios**:

1. **Given** un trámite de la gestión y un tipo de documento que forma parte de su `PlantillaTramite`, **When** el Gestor/Escribano lo reingresa, **Then** el sistema crea un `DocumentoPresentado` con `reingresado=true`, vinculado al trámite y al tipo de documento, con nombre/vence/díasVencimiento/quiénEntrega heredados del `TipoDeDocumento`.
2. **Given** un tipo de documento que NO forma parte de la `PlantillaTramite` del trámite elegido, **When** se intenta reingresarlo, **Then** el sistema responde 400 (paso 7.1 — dato inválido) sin crear el documento.
3. **Given** un trámite que no pertenece a la gestión indicada, **When** se intenta reingresar un documento en él, **Then** el sistema responde 400 sin crear el documento.
4. **Given** un ID de trámite que no existe, **When** se intenta reingresar un documento en él, **Then** el sistema responde 404.

---

### Edge Cases

- ¿Qué pasa si una gestión no tiene trámites? El detalle debe mostrar una lista vacía, no un error.
- ¿Qué pasa si el mismo tipo de documento se reingresa más de una vez para el mismo trámite? El Curso de Eventos no lo prohíbe (un documento puede volver a observarse/vencer y reingresarse otra vez) — cada reingreso crea un nuevo `DocumentoPresentado` independiente; no hay unicidad forzada.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir consultar, para una gestión existente, sus trámites junto con la documentación necesaria de cada uno según `PlantillaTramite` (`GET /api/v1/gestiones/{id}/reingreso-documentacion`).
- **FR-002**: El sistema DEBE responder 404 si la gestión no existe.
- **FR-003**: El sistema DEBE permitir crear un nuevo `DocumentoPresentado` (`reingresado=true`) para un trámite de la gestión y un tipo de documento elegidos (`POST /api/v1/gestiones/{id}/reingreso-documentacion`).
- **FR-004**: El sistema DEBE responder 400 si el tipo de documento elegido no forma parte de la `PlantillaTramite` del trámite, o si el trámite no pertenece a la gestión indicada; DEBE responder 404 si el trámite no existe.

### Key Entities

- **DocumentoPresentado**: documento de una gestión, ya modelado; este caso de uso crea nuevas instancias con `reingresado = true`.
- **PlantillaTramite**: relación existente (CU03) usada para validar que el tipo de documento reingresado es efectivamente necesario para el trámite.
- **Tramite / GestionDeEscritura**: trámite sobre el que se reingresa el documento, y gestión a la que pertenece.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El Gestor/Escribano puede consultar, en una sola llamada, los trámites de cualquier gestión existente junto con su documentación necesaria.
- **SC-002**: El 100% de los reingresos sobre pares (trámite, tipo de documento) válidos crean un `DocumentoPresentado` con `reingresado=true` correctamente vinculado.
- **SC-003**: El 100% de los intentos de reingreso con un tipo de documento no requerido por el trámite, o con un trámite ajeno a la gestión, son rechazados sin crear datos inconsistentes.

## Assumptions

- La autenticación/autorización reutiliza el esquema JWT existente — no se agregan roles nuevos.
- No se fuerza unicidad sobre reingresos repetidos del mismo (trámite, tipo de documento) — el Curso de Eventos no lo exige y un documento puede volver a observarse/vencer más de una vez.
- Los campos operativos del nuevo `DocumentoPresentado` (fecha de ingreso, número de cartón, etc.) se completan luego mediante el flujo de movimiento ya existente (mismo patrón que CU10) — el reingreso solo crea el registro inicial.
