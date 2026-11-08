# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Management create handler is typed

`ManagementController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Success stays 201 with `ManagementResponse`. Bad request, not found, and the Updated arm stay empty 400, empty 404, and empty 200. `SaveResult.ServerError` stays a thrown `IllegalStateException` mapped by `GlobalExceptionHandler` to 500 `ErrorResponse`.

#### Scenario: create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ManagementController#create` is absent from the baseline and is not loosely typed

#### Scenario: Create keeps the 201 body and empty error statuses

- **GIVEN** a create call whose port result is Created, BadRequest, NotFound, or Updated
- **WHEN** the controller handles it
- **THEN** Created responds 201 with the management fields, and the other three respond with an empty body at 400, 404, and 200
