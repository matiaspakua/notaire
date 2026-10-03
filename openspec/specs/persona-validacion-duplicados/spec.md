# persona-validacion-duplicados Specification

## Purpose

Prevents two `Person` records from coexisting with the same
identification type and identification number, so budgets, managements,
and payments are never split across two unlinked records of the same
real person (CU17, CU18). Enforced in the application service (#835) and
at the database via unique index on
`people (fk_id_tipo_identificacion, identification_number)` (#799).

## Requirements

### Requirement: Database uniqueness of person identification type and number

The system SHALL enforce at the PostgreSQL schema level that no two rows
in `people` share the same `(fk_id_tipo_identificacion, identification_number)`.
Migrations MUST fail fast (or apply agreed cleanup keeping the lowest id)
when duplicate groups already exist before creating the unique index.

#### Scenario: Second insert with same type and number is rejected by the database

- **WHEN** a second `people` row is inserted with the same identification type
  and identification number as an existing row, bypassing the service-layer check
- **THEN** the database rejects the insert (unique constraint / unique index
  violation)

#### Scenario: Migration refuses to proceed when duplicate groups exist

- **WHEN** Flyway applies the uniqueness migration and the `people` table
  already contains one or more groups with the same type and number
- **THEN** the migration fails with a clear error (or completes only after
  keeping the lowest `id` per agreed cleanup) and does not leave the schema
  without the uniqueness guarantee

#### Scenario: Concurrent create race still surfaces as HTTP 409

- **WHEN** two create requests race past the service-layer duplicate check and
  one insert wins at the database
- **THEN** the losing request is mapped to HTTP 409 Conflict compatible with
  the existing #835 client path (including `existingPersonId` when resolvable)

### Requirement: Rechazar alta de persona con documento duplicado

El sistema SHALL rechazar la creación de una `Persona` cuando ya existe
otra `Persona` con el mismo `TipoIdentificacion` y
`numeroIdentificacion`, según CU17 y CU18. El rechazo SHALL aplicarse tanto
en el servicio de aplicación (#835) como mediante la restricción única de base
de datos (#799).

#### Scenario: Alta exitosa con documento no registrado

- **WHEN** un usuario crea una persona con un tipo y número de
  identificación que no pertenece a ninguna persona existente
- **THEN** el sistema crea la persona

#### Scenario: Rechazo de alta con documento ya registrado

- **WHEN** un usuario intenta crear una persona con el mismo tipo y
  número de identificación que una persona ya existente
- **THEN** el sistema rechaza la creación y responde con un error que
  identifica a la persona existente

### Requirement: Rechazar edición de persona hacia un documento duplicado

El sistema SHALL rechazar la edición de una `Persona` cuando el tipo y
número de identificación propuestos ya pertenecen a otra `Persona`
distinta, según CU17 y CU18.

#### Scenario: Edición exitosa sin cambiar el documento

- **WHEN** un usuario edita una persona existente sin cambiar su tipo ni
  número de identificación
- **THEN** el sistema guarda los cambios

#### Scenario: Rechazo de edición hacia un documento de otra persona

- **WHEN** un usuario edita una persona cambiando su tipo o número de
  identificación al de otra persona ya existente
- **THEN** el sistema rechaza la edición y responde con un error que
  identifica a la persona existente con ese documento
