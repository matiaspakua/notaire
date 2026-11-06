# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Property create handler is typed

`PropertyController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>`.

#### Scenario: Property create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `PropertyController#create` is absent from the baseline and is not loosely typed
