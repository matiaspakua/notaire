# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Management create complete-case handler is typed

`ManagementController#createCompleteCase` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Success stays 201 with `DtoManagementSummary`. Bad request, not found, and the unused Updated arm stay empty 400, empty 404, and empty 200.

#### Scenario: createCompleteCase leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ManagementController#createCompleteCase` is absent from the baseline and is not loosely typed

#### Scenario: Create complete-case keeps the 201 body and empty error statuses

- **GIVEN** a complete-case create the port accepts, or validation failures, missing data, or the unused Updated arm
- **WHEN** the controller handles it
- **THEN** success is 201 with the management summary body, bad request and not found are empty 400 and 404, and Updated is empty 200
