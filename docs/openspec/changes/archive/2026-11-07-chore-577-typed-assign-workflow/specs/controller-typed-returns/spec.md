# controller-typed-returns Delta

## ADDED Requirements

### Requirement: ProcedureType assignWorkflow handler is typed

`ProcedureTypeController#assignWorkflow` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. NotFound and inactive-workflow failures SHALL be mapped by `ProcedureTypeExceptionHandler` with the same HTTP shapes clients already receive.

#### Scenario: assignWorkflow leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ProcedureTypeController#assignWorkflow` is absent from the baseline and is not loosely typed
