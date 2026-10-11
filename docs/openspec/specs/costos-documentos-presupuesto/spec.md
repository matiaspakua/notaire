# costos-documentos-presupuesto Specification

## Purpose

Connects the cost of a submitted document (stamp, tax) to the total of the
budget of its procedure, and allows defining expected fixed or variable
expenses by document type on the budget template of a procedure
type (CU27, CU39).

## Requirements

### Requirement: Include document costs in the budget total
The system SHALL add the cost (`amountToPay`) of the submitted
documents linked to the procedures of a budget when calculating its
total.

#### Scenario: Budget with one document that has a cost
- **WHEN** a budget has a procedure with a submitted document whose
  cost is greater than zero
- **THEN** the budget total includes that cost

#### Scenario: Budget with several documents that have a cost
- **WHEN** a budget has a procedure with several submitted
  documents, each with an associated cost
- **THEN** the budget total includes the sum of all those costs

#### Scenario: Budget with no documents that have a cost
- **WHEN** a budget has no submitted document with an associated
  cost
- **THEN** the budget total is calculated as it is today, with no extra
  cost

### Requirement: Define fixed or variable expenses by document type on the template
The system SHALL allow defining, for a procedure type, an expected fixed or
variable expense linked to a document type.

#### Scenario: Define a fixed expense by document type
- **WHEN** a fixed expense for a document type is defined on the budget
  template of a procedure type
- **THEN** the system accepts and stores that definition

#### Scenario: Define a variable expense by document type
- **WHEN** a variable expense (a percentage) for a document type is defined
  on the budget template of a procedure type
- **THEN** the system accepts and stores that definition

#### Scenario: Read the document-type expenses of a template
- **WHEN** the budget template of a procedure type that has document-type
  expenses defined is read
- **THEN** the system responds with those expenses
