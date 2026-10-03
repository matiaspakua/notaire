# tipo-documento-vencimiento-config Specification

## Purpose

Permite cargar y editar, desde la pantalla de administración, si un tipo de
documento vence, en cuántos días y quién es responsable de entregarlo,
completando datos que ya existen en el modelo pero que hoy son imposibles de
ingresar (CU27, CU32).

## Requirements

### Requirement: Cargar vencimiento y responsable al crear un tipo de documento

The system SHALL allow entering, when creating a document type, whether it
expires (`expires`), the number of validity days (`dueDays`), and who is
responsible for delivering or returning it (`deliveredBy`), according to CU27.
This change keeps that behavior and adds residual `enabled` / `returned`
alongside it (see ADDED requirements).

#### Scenario: Alta de tipo de documento que vence

- **WHEN** an administrator creates a document type indicating that it expires,
  with a number of validity days and a responsible party
- **THEN** the system stores the document type with `expires = true`, the given
  `dueDays`, and the given `deliveredBy`

#### Scenario: Alta de tipo de documento que no vence

- **WHEN** an administrator creates a document type indicating that it does not
  expire
- **THEN** the system stores the document type with `expires = false` without
  requiring `dueDays`

### Requirement: Modificar vencimiento y responsable de un tipo de documento

El sistema SHALL permitir modificar `vence`, `diasVencimiento` y
`quienEntrega` de un tipo de documento existente que no esté en uso, según
CU32.

#### Scenario: Modificación de vencimiento y responsable

- **WHEN** un usuario modifica un tipo de documento existente que no está en
  uso, cambiando `vence`, `diasVencimiento` o `quienEntrega`
- **THEN** el sistema guarda los nuevos valores

#### Scenario: Modificación bloqueada por tipo de documento en uso

- **WHEN** un usuario intenta modificar un tipo de documento que ya está en
  uso (referenciado por una plantilla de trámite o un documento presentado)
- **THEN** el sistema rechaza la modificación con un mensaje indicando que
  debe crear un tipo de documento nuevo

### Requirement: Load enabled and returned when creating a document type

The system SHALL allow an administrator to set whether a document type is
enabled (`enabled`) and whether documents of that type are returned
(`returned`) when creating a document type, in addition to the expiration
fields already covered by this capability (CU27). On the empty create form,
defaults SHALL be `enabled = true` and `returned = false`.

#### Scenario: Create form defaults for enabled and returned

- **WHEN** an administrator opens the new document-type form
- **THEN** the system shows `enabled` checked and `returned` unchecked by
  default

#### Scenario: Create document type with enabled and returned configured

- **WHEN** an administrator creates a document type with `enabled` and
  `returned` set explicitly
- **THEN** the system persists those values and returns them on subsequent
  reads of the document type

### Requirement: Edit enabled and returned on an unused document type

The system SHALL allow modifying `enabled` and `returned` of an existing
document type that is not in use, according to CU32. The edit form SHALL
pre-fill the stored values.

#### Scenario: Edit form pre-fills enabled and returned

- **WHEN** an administrator opens an existing document type for edit
- **THEN** the system shows the stored `enabled` and `returned` values

#### Scenario: Update enabled and returned succeeds when not in use

- **WHEN** an administrator updates `enabled` and/or `returned` on a document
  type that is not in use
- **THEN** the system persists the new values
