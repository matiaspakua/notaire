# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Copy create handler is typed

`CopyController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Copy create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `CopyController#create` is absent from the baseline and is not loosely typed
