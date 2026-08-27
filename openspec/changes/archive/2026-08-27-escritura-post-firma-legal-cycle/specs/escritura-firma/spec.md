<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Provides the "Firmar escritura" business action (CU06): a controlled state
transition that replaces the current unrestricted generic PUT on `Escritura`.

## ADDED Requirements

### Requirement: Firmar escritura con validación de estado y folio
El sistema SHALL permitir firmar una escritura solo cuando está en estado
"Sin Firmar" y tiene al menos un folio asignado, según CU06 y RF-27. La
acción SHALL rechazar la firma en cualquier otro caso, en lugar de aceptar
un cambio de estado arbitrario vía el endpoint CRUD genérico.

#### Scenario: Firma exitosa de una escritura lista
- **WHEN** el Escribano invoca la acción de firma sobre una escritura en
  estado "Sin Firmar" que tiene folio(s) asignado(s)
- **THEN** el sistema cambia el estado de la escritura a "Firmada" y
  responde con la escritura actualizada

#### Scenario: Rechazo por escritura ya firmada
- **WHEN** el Escribano invoca la acción de firma sobre una escritura que ya
  está en estado "Firmada" o posterior
- **THEN** el sistema rechaza la operación sin modificar el estado y
  responde con un error que indica el estado actual

#### Scenario: Rechazo por falta de folio asignado
- **WHEN** el Escribano invoca la acción de firma sobre una escritura en
  estado "Sin Firmar" que no tiene ningún folio asignado
- **THEN** el sistema rechaza la operación sin modificar el estado y
  responde con un error que indica la falta de folio
