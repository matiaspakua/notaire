<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Loads the `Concepto`s of a `TipoDeTramite`'s `PlantillaPresupuesto` as
`Item`s of a real `Presupuesto`, so quoting the same tipo de trámite
starts from a common, previously defined price list (CU39).

## ADDED Requirements

### Requirement: Cargar ítems de presupuesto desde la plantilla del tipo de trámite
El sistema SHALL permitir cargar en un presupuesto, como nuevos `Item`s,
los `Concepto`s de la `PlantillaPresupuesto` asociada al `TipoDeTramite`
elegido, copiando su nombre, valor y porcentaje al momento de la carga,
según CU39 y RF-04/RF-64 a RF-67.

#### Scenario: Carga exitosa desde una plantilla existente
- **WHEN** un usuario carga los ítems de plantilla en un presupuesto para
  un tipo de trámite que tiene una `PlantillaPresupuesto` con uno o más
  conceptos
- **THEN** el sistema crea un `Item` del presupuesto por cada concepto de
  la plantilla, con el mismo nombre, valor y porcentaje

#### Scenario: Ítems cargados no se recalculan si la plantilla cambia después
- **WHEN** la `PlantillaPresupuesto` de un tipo de trámite se modifica
  después de haber cargado sus ítems en un presupuesto existente
- **THEN** los ítems ya cargados en ese presupuesto conservan los valores
  con los que fueron creados, sin actualizarse automáticamente

#### Scenario: Rechazo cuando el tipo de trámite no tiene plantilla
- **WHEN** un usuario intenta cargar los ítems de plantilla en un
  presupuesto para un tipo de trámite que no tiene ninguna
  `PlantillaPresupuesto` definida
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que el tipo de trámite no tiene plantilla configurada
