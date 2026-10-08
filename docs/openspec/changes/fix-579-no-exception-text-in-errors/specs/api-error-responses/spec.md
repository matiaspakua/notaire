# api-error-responses — delta

## Purpose

API error responses carry a safe, standard body and never expose internal exception text.

## MODIFIED Requirements

### Requirement: Exception text stays on the server

A controller that catches an exception SHALL NOT return that exception's message to the client unless it is an application-authored `NotaireException`; the response SHALL be the standard `ErrorResponse` with the same status code as before.

#### Scenario: Database error on create

- **WHEN** saving a new folio type fails with a not-null violation
- **THEN** the response is 409 with a generic `message` and no SQL, table, column or row values

#### Scenario: Database error on update

- **WHEN** saving an existing document type fails with a database error
- **THEN** the response is 500 with the generic message

#### Scenario: Application message

- **WHEN** the service throws a `BusinessValidationException` with a user message
- **THEN** the response keeps that message

### Requirement: Guard against regressions

The test suite SHALL fail when a controller returns a caught `Exception`'s message in a response body.

#### Scenario: No controller echoes exception text

- **WHEN** the controller sources are scanned
- **THEN** no `catch (Exception e)` block puts `e.getMessage()` in a response
