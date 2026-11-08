# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Management transition handler is typed

`ManagementController#transition` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Success stays 200 with `id` and `currentStatus`. A workflow rejection stays 400 `ErrorResponse` with `message`. A missing management stays 404 `ErrorResponse` with `message`.

#### Scenario: transition leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ManagementController#transition` is absent from the baseline and is not loosely typed

#### Scenario: Transition keeps the 200 body

- **GIVEN** a transition the use case accepts for an existing management
- **WHEN** the controller handles it
- **THEN** the response is 200 with that management id and the resulting current status name

#### Scenario: Transition keeps the 400 and 404 error bodies

- **GIVEN** a transition the workflow rejects, or a management that does not exist
- **WHEN** the controller handles it
- **THEN** rejection responds 400 with `message`, and a missing management responds 404 with `message`
