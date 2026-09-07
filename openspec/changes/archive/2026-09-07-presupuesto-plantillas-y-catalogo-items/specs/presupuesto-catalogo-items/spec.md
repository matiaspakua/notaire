<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Lets a user add existing reusable `Item`s from the catalog (CU71) to a
`Presupuesto`, instead of only being able to create a new `Item` from
scratch for each one.

## ADDED Requirements

### Requirement: Agregar ítems del catálogo a un presupuesto
El sistema SHALL permitir asociar a un presupuesto uno o más `Item`s
existentes del catálogo, creando una copia de cada uno con su nombre,
valor, porcentaje y observaciones, asociada a ese presupuesto, según
CU71 y RF-07.

#### Scenario: Agregado exitoso de un ítem del catálogo
- **WHEN** un usuario selecciona un ítem existente del catálogo y lo
  agrega a un presupuesto
- **THEN** el sistema crea una copia del ítem asociada al presupuesto,
  con el mismo nombre, valor, porcentaje y observaciones

#### Scenario: Agregado de varios ítems del catálogo en una sola operación
- **WHEN** un usuario selecciona varios ítems existentes del catálogo y
  los agrega a un presupuesto
- **THEN** el sistema crea una copia de cada ítem seleccionado, asociada
  al presupuesto

#### Scenario: Rechazo al referenciar un ítem de catálogo inexistente
- **WHEN** un usuario intenta agregar a un presupuesto un ítem del
  catálogo cuyo ID no existe
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que el ítem no existe
