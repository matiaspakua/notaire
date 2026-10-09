# submitted-documents — delta

## Purpose

Registering and maintaining the documents clients submit for a gestión's trámites.

## MODIFIED Requirements

### Requirement: Create requires type and procedure

`POST /api/v1/documento-presentado` SHALL reject a body without `typeId` or `procedureId` with 400 naming the missing field and SHALL store nothing; the contract SHALL mark both required on create and neither on update.

#### Scenario: Missing type

- **WHEN** a create omits typeId
- **THEN** the response is 400, the message names typeId and no document is stored

#### Scenario: Missing procedure

- **WHEN** a create omits procedureId
- **THEN** the response is 400, the message names procedureId and no document is stored

#### Scenario: Empty body

- **WHEN** a create sends {}
- **THEN** the response is 400 and no document is stored

#### Scenario: Contract

- **WHEN** the OpenAPI document is generated
- **THEN** the POST request schema lists typeId and procedureId as required and the PUT schema does not

### Requirement: UI registers documents for a trámite

The Documentos dialog SHALL require the type and a trámite of a gestión to create a document, and SHALL let a document without a trámite be linked when edited, without forcing it.

#### Scenario: Create form

- **WHEN** the user creates a document
- **THEN** Save is enabled only after choosing a type and a trámite, and the document is listed in the gestión's case summary

#### Scenario: Legacy document

- **WHEN** the user edits a document stored without a trámite
- **THEN** the gestión/trámite selectors are offered and saving without choosing one is allowed
