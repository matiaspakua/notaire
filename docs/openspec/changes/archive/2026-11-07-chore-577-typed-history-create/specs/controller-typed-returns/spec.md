# controller-typed-returns Delta

## ADDED Requirements

### Requirement: History create handler is typed

`HistoryController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: History create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `HistoryController#create` is absent from the baseline and is not loosely typed
