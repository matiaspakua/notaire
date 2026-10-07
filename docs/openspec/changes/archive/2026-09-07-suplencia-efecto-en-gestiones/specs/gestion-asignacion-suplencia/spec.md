<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Hace que registrar una suplencia (CU22) tenga efecto práctico sobre la
asignación de gestiones nuevas, en lugar de ser un dato aislado que nadie
consulta (CU59, RF-89).

## ADDED Requirements

### Requirement: Redirigir la asignación de gestión al suplente activo
El sistema SHALL asignar una gestión al `fkIdSuplente` de una `Suplencia`
cuando el escribano solicitado tiene esa suplencia activa (`fechaInicio` ≤
fecha de la gestión ≤ `fechaFin`) como `fkIdSuplantado`, según RF-89.

#### Scenario: Creación de gestión sin suplencia activa
- **WHEN** se crea una gestión con un escribano que no tiene ninguna
  suplencia activa para la fecha de la gestión
- **THEN** el sistema asigna la gestión al escribano solicitado

#### Scenario: Creación de gestión con suplencia activa
- **WHEN** se crea una gestión con un escribano que tiene una suplencia
  activa como suplantado para la fecha de la gestión
- **THEN** el sistema asigna la gestión al suplente de esa suplencia en
  lugar del escribano solicitado

#### Scenario: Edición de gestión con suplencia activa
- **WHEN** se edita una gestión existente cambiando el escribano a uno que
  tiene una suplencia activa como suplantado para la fecha de la gestión
- **THEN** el sistema asigna la gestión al suplente de esa suplencia en
  lugar del escribano solicitado

### Requirement: Registrar el redireccionamiento en la gestión
El sistema SHALL dejar constancia, en las observaciones de la gestión, de
que la asignación fue redirigida por una suplencia activa, identificando al
escribano solicitado y al suplente asignado.

#### Scenario: Observaciones registran el redireccionamiento
- **WHEN** el sistema asigna una gestión al suplente en lugar del escribano
  solicitado
- **THEN** las observaciones de la gestión identifican al escribano
  solicitado y al suplente asignado
