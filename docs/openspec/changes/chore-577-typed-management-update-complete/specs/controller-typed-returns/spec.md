# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Management update complete-case handler is typed

`ManagementController#updateCompleteCase` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Success stays 200 with `DtoManagementSummary`. Bad request, not found, and the unused Created arm stay empty 400, empty 404, and empty 201.

#### Scenario: updateCompleteCase leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ManagementController#updateCompleteCase` is absent from the baseline and is not loosely typed

#### Scenario: Update complete-case keeps 200 body and empty error statuses

- **GIVEN** a complete-case update the port accepts, validation failure, missing management, or the unused Created arm
- **WHEN** the controller handles it
- **THEN** success is 200 with the management summary body, bad request and not found are empty 400 and 404, and Created is empty 201
