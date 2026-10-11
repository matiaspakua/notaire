# ui-procedure-template-admin — delta

## Purpose

Admins manage the required-documents checklist of each procedure type.

## MODIFIED Requirements

### Requirement: Required-documents checklist admin

An ADMIN SHALL be able to add a document type to a procedure type's checklist, edit its notes and remove it with confirmation; a removed row SHALL no longer be returned by the API.

#### Scenario: Add

- **WHEN** an admin adds a document type to a procedure type
- **THEN** the dialog lists it by name and the required-documents screen shows it for that procedure type

#### Scenario: Duplicate

- **WHEN** the document type is already on the list
- **THEN** the add select doesn't offer it, and a duplicate POST answers 409

#### Scenario: Edit notes

- **WHEN** an admin edits the notes of a row
- **THEN** PUT stores them and the list shows them

#### Scenario: Remove

- **WHEN** an admin confirms removing a row
- **THEN** DELETE answers 204 and a GET by key answers 404
