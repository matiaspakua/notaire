<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Permite registrar que un folio está vinculado a la escritura que lo ocupa,
completando el protocolo notarial descrito en CU87, y evita que dos escrituras
distintas ocupen el mismo folio.

## ADDED Requirements

### Requirement: Vincular un folio a una escritura
El sistema SHALL permitir asociar un folio a una escritura al crear o editar
el folio, y SHALL marcar el folio como `Utilizado` al hacerlo.

#### Scenario: Alta de folio vinculado a una escritura
- **WHEN** se crea un folio informando el `escrituraId` de una escritura
  existente
- **THEN** el folio se guarda con esa escritura vinculada y con
  `estado = "Utilizado"`

#### Scenario: Edición de folio para vincularlo a una escritura
- **WHEN** se edita un folio existente sin escritura vinculada, informando un
  `escrituraId`
- **THEN** el folio se guarda con esa escritura vinculada y con
  `estado = "Utilizado"`

#### Scenario: Alta de folio sin vincular
- **WHEN** se crea un folio sin informar `escrituraId`
- **THEN** el folio se guarda sin escritura vinculada, con el `estado`
  informado en la solicitud (comportamiento sin cambios)

### Requirement: Rechazar vincular un folio ya utilizado
El sistema SHALL rechazar la vinculación de un folio a una escritura cuando
ese folio ya está vinculado a otra escritura y su `estado` es `Utilizado`.

#### Scenario: Folio ya vinculado a otra escritura
- **WHEN** se intenta vincular a una escritura un folio cuyo `estado` ya es
  `Utilizado` y que está vinculado a una escritura distinta
- **THEN** el sistema rechaza la solicitud con `409 Conflict` y el folio no
  cambia

#### Scenario: Re-vincular el mismo folio a la misma escritura
- **WHEN** se edita un folio informando el mismo `escrituraId` al que ya
  estaba vinculado
- **THEN** el sistema acepta la solicitud sin error (no es un conflicto,
  es una actualización idempotente)

### Requirement: Consultar la escritura vinculada a un folio
El sistema SHALL incluir la escritura vinculada, si existe, en la respuesta
de consulta de un folio.

#### Scenario: Consultar un folio con escritura vinculada
- **WHEN** se consulta un folio (`GET /api/v1/folio/{id}` o el listado) que
  tiene una escritura vinculada
- **THEN** la respuesta incluye los datos de esa escritura

#### Scenario: Consultar un folio sin escritura vinculada
- **WHEN** se consulta un folio que no tiene ninguna escritura vinculada
- **THEN** la respuesta no incluye datos de escritura para ese folio
