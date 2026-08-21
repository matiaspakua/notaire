<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Evita que se emita una copia de un testimonio que ya fue presentado e
inscripto en el registro, protegiendo la integridad del protocolo notarial
descrito en CU87.

## ADDED Requirements

### Requirement: Rechazar copia de un testimonio ya inscripto
El sistema SHALL rechazar el alta de una copia cuando el testimonio de
origen tiene al menos un movimiento marcado como inscripto.

#### Scenario: Testimonio con movimiento inscripto
- **WHEN** se solicita el alta de una copia cuyo testimonio de origen tiene
  un `MovimientoTestimonio` con `inscripta = true`
- **THEN** el sistema rechaza la solicitud con `409 Conflict` y no se crea
  la copia

#### Scenario: Testimonio sin movimientos inscriptos
- **WHEN** se solicita el alta de una copia cuyo testimonio de origen no
  tiene ningún `MovimientoTestimonio` con `inscripta = true`
- **THEN** la copia se crea normalmente (comportamiento sin cambios)

#### Scenario: Testimonio sin movimientos registrados
- **WHEN** se solicita el alta de una copia cuyo testimonio de origen no
  tiene ningún `MovimientoTestimonio` registrado
- **THEN** la copia se crea normalmente (comportamiento sin cambios)
