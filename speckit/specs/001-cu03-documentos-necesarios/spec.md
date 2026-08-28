# Feature Specification: Lista de documentos y certificados necesarios por trámite

**Feature Branch**: `feat/860_documentos-necesarios-tramite`

**Created**: 2026-08-28

**Status**: Draft

**Input**: User description: "Implementar CU03 — pantalla donde el Recepcionista selecciona un trámite y ve/imprime la lista de documentos y certificados necesarios (nombre, si vence, días de validez, quién entrega)."

## Notaire Traceability *(mandatory — CONSTITUTION.md §3)*

| Field | Value |
|-------|-------|
| **GitHub Issue** | #860 |
| **Use Case** | CU-03 — Lista documentos y certificados necesarios |

## Verified starting point *(not boilerplate — read before scaffolding)*

The backend for this Use Case already exists and does not need to be built:

- `TipoDeDocumento` (`backend-api/.../negocio/TipoDeDocumento.java`) already has
  `nombre`, `vence`, `diasVencimiento`, `quienEntrega`.
- `PlantillaTramite` links `TipoDeTramite` ↔ `TipoDeDocumento`, exposed by
  `PlantillaTramiteController` at `GET /api/v1/plantilla-tramite/tipo-tramite/{idTipoTramite}`.
- `TipoDeTramiteController` (`GET /api/v1/tipo-tramite`) already lists trámites,
  and `frontend/src/hooks/useTiposTramite.ts` already consumes it.

The gap is exclusively the frontend screen + a hook for the one endpoint above.
No Flyway migration, no new entity, no new backend endpoint.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar documentos necesarios para un trámite (Priority: P1)

El Recepcionista, ante una Persona que solicita iniciar un trámite, selecciona
ese trámite de una lista y el sistema le muestra los documentos y
certificados requeridos con nombre, si vence, días de validez y quién hace
la entrega.

**Why this priority**: Es el flujo principal de CU03 (pasos 1-7 del Curso de
Eventos) — sin esto no hay caso de uso.

**Independent Test**: Seleccionar un trámite con `PlantillaTramite` cargadas
en la base y verificar que la tabla en pantalla muestra las 4 columnas
correctas para cada documento asociado.

**Acceptance Scenarios**:

1. **Given** un trámite con documentos configurados en `PlantillaTramite`, **When** el Recepcionista lo selecciona de la lista, **Then** el sistema presenta la lista de documentos con nombre, si vence, días de validez y quién entrega.
2. **Given** la lista de trámites disponibles, **When** la pantalla carga, **Then** el sistema la busca y la presenta (paso 4-5 del Curso de Eventos) usando el endpoint `GET /api/v1/tipo-tramite` ya existente.

---

### User Story 2 - Imprimir la lista de documentos (Priority: P2)

El Recepcionista solicita la impresión de la información obtenida para
entregársela a la Persona en papel.

**Why this priority**: Paso 8-10 del Curso de Eventos — cierra el caso de
uso, pero depende de que la User Story 1 ya muestre los datos.

**Independent Test**: Con la lista visible, disparar la acción de impresión
y verificar que se genera una vista imprimible con la misma información.

**Acceptance Scenarios**:

1. **Given** la lista de documentos visible en pantalla, **When** el Recepcionista solicita imprimir, **Then** el sistema genera una vista imprimible con nombre, si vence, días de validez y quién entrega de cada documento.

---

### User Story 3 - Trámite sin documentos configurados (Priority: P3)

Cubre la excepción 7.1 del CU: si no se encuentra información para el
trámite seleccionado, el sistema debe notificarlo en vez de mostrar una
pantalla vacía o un error técnico.

**Why this priority**: Excepción documentada explícitamente en el CU, pero
solo se activa en un caso borde.

**Independent Test**: Seleccionar un `TipoDeTramite` sin filas en
`PlantillaTramite` y verificar que se muestra un mensaje claro, sin error de
consola ni pantalla en blanco.

**Acceptance Scenarios**:

1. **Given** un trámite sin documentos asociados en `PlantillaTramite`, **When** el Recepcionista lo selecciona, **Then** el sistema muestra un mensaje indicando que no hay documentos configurados para ese trámite (excepción 7.1), sin lanzar un error.

---

### Edge Cases

- ¿Qué pasa si el usuario selecciona un trámite y luego cambia a otro antes de que responda el backend? La lista debe reflejar siempre el último trámite seleccionado (evitar condición de carrera de queries).
- ¿Cómo se comporta la impresión si el navegador bloquea el diálogo de impresión? Debe seguir siendo posible ver/copiar la información en pantalla.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir al Recepcionista seleccionar un trámite de la lista de trámites disponibles (reutilizando `GET /api/v1/tipo-tramite`).
- **FR-002**: El sistema DEBE mostrar, para el trámite seleccionado, la lista de documentos/certificados requeridos consultando `GET /api/v1/plantilla-tramite/tipo-tramite/{idTipoTramite}`.
- **FR-003**: Cada fila de la lista DEBE mostrar nombre, si vence (sí/no), días de validez (cuando vence) y quién hace la entrega (Cliente/Entidad Externa).
- **FR-004**: El sistema DEBE ofrecer una acción de impresión que genere una vista imprimible con la misma información.
- **FR-005**: El sistema DEBE mostrar un mensaje claro cuando el trámite seleccionado no tiene documentos configurados (excepción 7.1), sin error técnico visible al usuario.

### Key Entities

- **TipoDeTramite**: trámite disponible para selección (ya modelado, sin cambios).
- **TipoDeDocumento**: documento/certificado, con `nombre`, `vence`, `diasVencimiento`, `quienEntrega` (ya modelado, sin cambios).
- **PlantillaTramite**: relación trámite↔documento requerido (ya modelada, sin cambios).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El Recepcionista puede obtener la lista completa de documentos necesarios para cualquier trámite configurado en menos de 2 clics desde la pantalla.
- **SC-002**: El 100% de los trámites con `PlantillaTramite` configuradas muestran las 4 columnas requeridas sin datos faltantes.
- **SC-003**: La acción de imprimir produce una vista legible con la misma información mostrada en pantalla, en el 100% de los casos con documentos configurados.

## Assumptions

- La autenticación/autorización de la pantalla reutiliza el esquema JWT ya existente del dashboard — no se agregan roles nuevos.
- "Imprimir" se resuelve con `window.print()` + estilos `@media print` (no se requiere generación de PDF en el servidor para este alcance).
- El endpoint `GET /api/v1/plantilla-tramite/tipo-tramite/{idTipoTramite}` devuelve `[]` (no 404) cuando el trámite no tiene documentos asociados — confirmado por `PlantillaTramiteRepository.findByTipoDeTramiteIdTipoTramite`, que es una consulta de colección estándar de Spring Data.
