# controller-typed-returns Delta

## ADDED Requirements

### Requirement: ProcedureFolder putOnHold handler is typed

`ProcedureFolderController#putOnHold` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. NotFound and BadRequest SHALL be mapped by `ProcedureFolderExceptionHandler` with the same HTTP shapes clients already receive.

#### Scenario: putOnHold leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ProcedureFolderController#putOnHold` is absent from the baseline and is not loosely typed
