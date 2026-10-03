<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## MODIFIED Requirements

### Requirement: Registrar en Historial los cambios de estado de una gestión
The system SHALL create a History entry (status, date, and optional notes)
whenever a gestión status is first set or changes on any write path that
persists status — including plain create, plain update, and complete-case
update — in addition to complete-case create, valid workflow transition, and
archive already covered by #833, per CU13 / RF-110 / CU02 / CU53.

#### Scenario: Alta de gestión registra su estado inicial
- **WHEN** se crea una nueva gestión
- **THEN** el sistema registra una entrada en `Historial` con el estado
  inicial de la gestión y la fecha de creación

#### Scenario: Transición válida registra el nuevo estado
- **WHEN** una gestión cambia de estado mediante una transición válida
- **THEN** el sistema registra una nueva entrada en `Historial` con el
  estado destino y la fecha del cambio

#### Scenario: Archivado registra el estado archivado
- **WHEN** una gestión es archivada
- **THEN** el sistema registra una nueva entrada en `Historial` con el
  estado "Archivada" y la fecha del archivado

#### Scenario: Plain create with status writes initial History
- **WHEN** a client creates a gestión via plain POST with a management status
- **THEN** the system appends one History row for that initial status

#### Scenario: Plain create without status writes no History
- **WHEN** a client creates a gestión via plain POST without a management status
- **THEN** the system does not invent a History row

#### Scenario: Plain update that changes status writes History
- **WHEN** a client updates a gestión via plain PUT and the status id differs
  from the previous status id
- **THEN** the system appends a History row for the new status

#### Scenario: Plain update that keeps status writes no History
- **WHEN** a client updates a gestión via plain PUT and the status id is
  unchanged (or omitted so status stays the same)
- **THEN** the system does not append a History row for that update

#### Scenario: Complete-case update that changes status writes History
- **WHEN** a client updates a gestión via PUT complete-case and the status id
  differs from the previous status id
- **THEN** the system appends a History row for the new status

#### Scenario: Complete-case update that keeps status writes no History
- **WHEN** a client updates a gestión via PUT complete-case and the status id
  is unchanged
- **THEN** the system does not append a History row for that update

## ADDED Requirements

### Requirement: Current status falls back to entity status when History empty
The system SHALL expose the current status of a gestión via
`GET .../estado-actual`. When History rows exist, the latest by date is used.
When History is empty, the system MUST synthesize a summary from the gestión's
current status association (management id and as-of-now). Missing gestión or
null status MUST yield 404. Per CU13 / RF-24.

#### Scenario: estado-actual from History when rows exist
- **WHEN** a client requests current status for a gestión that has History rows
- **THEN** the system returns 200 with the latest History summary by date

#### Scenario: estado-actual entity-status fallback when History empty
- **WHEN** a client requests current status for an existing gestión that has a
  status but no History rows
- **THEN** the system returns 200 with a synthesized summary from the entity
  status (management id, status id/name, as-of-now)

#### Scenario: estado-actual 404 when gestión missing
- **WHEN** a client requests current status for a gestión id that does not exist
- **THEN** the system returns 404

#### Scenario: estado-actual 404 when status null and History empty
- **WHEN** a client requests current status for an existing gestión with no
  History rows and no status assigned
- **THEN** the system returns 404
