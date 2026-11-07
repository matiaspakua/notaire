# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Notebook create handler is typed

`NotebookController#createNotebook` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Notebook create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `NotebookController#createNotebook` is absent from the baseline and is not loosely typed
