<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Records every relevant estado change of a gestión (creation, valid
transition, archiving) as a `Historial` entry, and exposes that history
for consultation, so the bitácora CU13 promises to show is actually
populated for real gestiones.

## ADDED Requirements

### Requirement: Registrar en Historial los cambios de estado de una gestión
El sistema SHALL crear una entrada en `Historial` (con estado, fecha y
observaciones) cada vez que una gestión es creada, transicionada según
`gestion-workflow-transicion`, o archivada, según CU13 y RF-24/RF-110.

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

### Requirement: Consultar la bitácora de una gestión
El sistema SHALL permitir consultar el `Historial` completo de una
gestión, ordenado cronológicamente, según CU13.

#### Scenario: Consulta devuelve el historial completo ordenado
- **WHEN** un usuario consulta la bitácora de una gestión con entradas de
  `Historial` registradas
- **THEN** el sistema responde con todas las entradas de `Historial` de
  esa gestión, ordenadas por fecha
