# controller-typed-returns Delta

## ADDED Requirements

### Requirement: IdentificationType create handler is typed

`IdentificationTypeController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>`.

#### Scenario: IdentificationType create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `IdentificationTypeController#create` is absent from the baseline and is not loosely typed
