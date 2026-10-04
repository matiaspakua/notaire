<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Register a client's document against a trámite and see it from the gestión. Source: #773; owner CU04.

## ADDED Requirements

### Requirement: Documents are created and listed with their type and date

The system MUST store the type, date and delivered flag sent by the Documentos screen and list each document with its type name, date, delivered flag and linked trámite.

#### Scenario: Create a document with a type and a date

- **WHEN** the screen creates a document choosing a type and a date
- **THEN** the list shows that type name and date

#### Scenario: Create a document for a trámite

- **WHEN** the screen creates a document choosing a gestión and one of its trámites
- **THEN** the list shows the linked trámite and the document is stored against it

### Requirement: The case summary lists the documents of the gestión

The case summary MUST list, for a gestión, every submitted document of its trámites with its name, type, trámite, prepared, released, observed, delivered and reentered flags and due date.

#### Scenario: A gestión with a linked document

- **WHEN** a document is registered against a trámite of the gestión
- **THEN** the case summary lists it with its flags

#### Scenario: A gestión without documents

- **WHEN** no trámite of the gestión has a document
- **THEN** the documents list is empty
