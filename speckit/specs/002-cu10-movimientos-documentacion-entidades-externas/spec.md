# Feature Specification: Registrar movimientos de documentación de entidades externas

**Feature Branch**: `feat/863_cu10-movimientos-documentacion-entidades-externas`

**Created**: 2026-08-28

**Status**: Draft

**Input**: User description: "Implementar CU10 — para una gestión dada, permitir registrar el movimiento de los documentos que deben ser aportados por entidades externas (registros, catastro), incluyendo la nomenclatura catastral cuando la gestión involucra un Inmueble, y transicionar automáticamente la gestión a 'Documentacion Completa' cuando todos esos documentos quedan entregados."

## Notaire Traceability *(mandatory — CONSTITUTION.md §3)*

| Field | Value |
|-------|-------|
| **GitHub Issue** | #863 |
| **Use Case** | CU-10 — Registrar movimientos de documentación de entidades externas (#163) |

## Verified starting point *(not boilerplate — read before scaffolding)*

The domain model already supports this Use Case without a new migration:

- `DocumentoPresentado` (`backend-api/.../negocio/DocumentoPresentado.java`)
  already has `quienEntrega`, `preparado`, `numeroCarton`, `fechaIngreso`,
  `fechaSalida`, `observado`, `importeAPagar`, `fechaPago`, `fechaLiberado`,
  `observaciones`, `entregado`.
- `ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA` already identifies the
  "entidad externa" delivery type.
- `Tramite.fkIdInmueble` → `Inmueble.nomenclaturaCatastral` already exposes
  the catastral nomenclature when a gestión involves an `Inmueble`.
- `GestionTransitionService.transicionar(idGestion, estadoDestino)` already
  validates and applies workflow transitions.

The gap is: no repository query scoped to "documentos de entidad externa de
una gestión", no service/controller to list/register their movement, and no
frontend screen. No new entity, no Flyway migration.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar documentación de entidad externa de una gestión (Priority: P1)

El Gestor/Escribano selecciona una gestión de la lista de gestiones en
trámite y el sistema le muestra el número de gestión, encabezado, fecha de
inicio, escribano, nomenclatura catastral (si la gestión involucra un
Inmueble) y la documentación que debe ser presentada por entidades externas.

**Why this priority**: Pasos 1-4 del Curso de Eventos — sin esto no hay
información sobre la que registrar movimientos.

**Independent Test**: Seleccionar una gestión con `DocumentoPresentado`
cuyo `quienEntrega` es `Entidad Externa` y verificar que el detalle devuelto
incluye la lista de esos documentos y, si hay `Inmueble` asociado, su
nomenclatura catastral.

**Acceptance Scenarios**:

1. **Given** una gestión con documentos de entidad externa y un trámite con `Inmueble` asociado, **When** el Gestor/Escribano la selecciona, **Then** el sistema presenta número de gestión, encabezado, fecha de inicio, escribano, nomenclatura catastral y la lista de documentos de entidad externa.
2. **Given** una gestión que no existe, **When** se solicita su detalle, **Then** el sistema responde 404 sin exponer detalles técnicos.

---

### User Story 2 - Registrar el movimiento de un documento de entidad externa (Priority: P1)

El Gestor/Escribano registra, para un documento de entidad externa
determinado, los datos de su movimiento (preparado, número de cartón, fecha
de ingreso/salida, observado, importe a pagar, fecha de pago/liberación,
observaciones, entregado) y guarda los cambios.

**Why this priority**: Pasos 5-6 del Curso de Eventos — es la acción
principal del caso de uso.

**Independent Test**: Registrar el movimiento de un documento de entidad
externa vía `PUT` y verificar que la respuesta refleja los datos guardados.

**Acceptance Scenarios**:

1. **Given** un documento de entidad externa perteneciente a la gestión, **When** el Gestor/Escribano guarda un movimiento válido, **Then** el sistema actualiza el documento y devuelve los datos guardados.
2. **Given** un documento que no pertenece a la gestión indicada, **When** se intenta registrar su movimiento, **Then** el sistema responde 400 (paso 7.1 — dato inválido) sin aplicar cambios.
3. **Given** un ID de documento presentado que no existe, **When** se intenta registrar su movimiento, **Then** el sistema responde 404.

---

### User Story 3 - Transición automática a "Documentacion Completa" (Priority: P2)

Cuando, tras registrar un movimiento, todos los documentos de entidad
externa de la gestión quedan entregados, el sistema intenta transicionar
automáticamente la gestión al estado "Documentacion Completa" (paso 7 del
Curso de Eventos), sin invalidar el movimiento ya guardado si el workflow no
lo permite.

**Why this priority**: Cierra el Curso de Eventos, pero depende de que la
User Story 2 ya persista el movimiento; es un efecto colateral best-effort,
no bloqueante.

**Independent Test**: Marcar como entregado el último documento pendiente de
una gestión y verificar que su estado pasa a "Documentacion Completa" cuando
el workflow define esa transición.

**Acceptance Scenarios**:

1. **Given** una gestión cuyo último documento de entidad externa pendiente se marca como entregado, **When** el workflow del trámite define una transición a "Documentacion Completa", **Then** el sistema transiciona automáticamente la gestión.
2. **Given** la misma situación pero el workflow no define esa transición, **When** se guarda el movimiento, **Then** el movimiento queda guardado igualmente y la gestión permanece en su estado actual, sin error visible al usuario (excepción 2.1 — ya entregados / transición no definida se maneja silenciosamente).

---

### Edge Cases

- ¿Qué pasa si una gestión no tiene documentos de entidad externa? El detalle debe mostrar una lista vacía, no un error.
- ¿Qué pasa si dos movimientos se registran concurrentemente sobre gestiones distintas? Cada intento de transición automática corre en su propia unidad de trabajo — un fallo de transición en una gestión no afecta al movimiento guardado de otra.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir consultar, para una gestión existente, número, encabezado, fecha de inicio, escribano, nomenclatura catastral (si aplica) y la lista de documentos de entidad externa (`GET /api/v1/gestiones/{id}/documentos-entidades-externas`).
- **FR-002**: El sistema DEBE responder 404 si la gestión no existe.
- **FR-003**: El sistema DEBE permitir registrar el movimiento de un documento de entidad externa perteneciente a la gestión (`PUT /api/v1/gestiones/{id}/documentos-entidades-externas/{idDocumentoPresentado}`).
- **FR-004**: El sistema DEBE responder 400 si el documento indicado no pertenece a la gestión o no es de entidad externa, y 404 si el documento no existe.
- **FR-005**: El sistema DEBE intentar transicionar automáticamente la gestión a "Documentacion Completa" cuando, tras el movimiento, todos sus documentos de entidad externa quedan entregados, sin invalidar el movimiento ya guardado si la transición no es válida en el workflow.

### Key Entities

- **DocumentoPresentado**: documento de una gestión, ya modelado; `quienEntrega = Entidad Externa` identifica el subconjunto de este caso de uso.
- **Tramite / Inmueble**: relación existente usada para resolver la nomenclatura catastral.
- **GestionDeEscritura**: gestión sobre la que se listan/registran los documentos.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El Gestor/Escribano puede consultar la documentación de entidad externa de cualquier gestión existente en una sola llamada.
- **SC-002**: El 100% de los movimientos registrados sobre documentos válidos (pertenecientes a la gestión, de entidad externa) se persisten correctamente.
- **SC-003**: El 100% de las gestiones cuyos documentos de entidad externa quedan totalmente entregados, y cuyo workflow define la transición, terminan en estado "Documentacion Completa" sin intervención manual.

## Assumptions

- La autenticación/autorización reutiliza el esquema JWT existente — no se agregan roles nuevos.
- La transición automática es best-effort: una `BusinessValidationException` o `ResourceNotFoundException` al intentar transicionar se registra en el log y no se propaga al cliente (el movimiento ya guardado no se revierte).
