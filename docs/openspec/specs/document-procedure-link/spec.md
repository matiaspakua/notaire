# document-procedure-link Specification

## Purpose
Register a client's document against a procedure and see it from the management. Source: #773; owner CU04.

## Requirements

### Requirement: Documents are created and listed with their type and date

The system MUST store the type, date and delivered flag sent by the Documents screen and list each document with its type name, date, delivered flag and linked procedure.

#### Scenario: Create a document with a type and a date

- **WHEN** the screen creates a document choosing a type and a date
- **THEN** the list shows that type name and date

#### Scenario: Create a document for a procedure

- **WHEN** the screen creates a document choosing a management and one of its procedures
- **THEN** the list shows the linked procedure and the document is stored against it

### Requirement: The case summary lists the documents of the management

The case summary MUST list, for a management, every submitted document of its procedures with its name, type, procedure, prepared, released, observed, delivered and reentered flags and due date.

#### Scenario: A management with a linked document

- **WHEN** a document is registered against a procedure of the management
- **THEN** the case summary lists it with its flags

#### Scenario: A management without documents

- **WHEN** no procedure of the management has a document
- **THEN** the documents list is empty
