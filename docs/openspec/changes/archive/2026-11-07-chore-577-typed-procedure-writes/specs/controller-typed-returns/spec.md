# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Procedure create and update handlers are typed

`ProcedureController#create` and `ProcedureController#update` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. NotFound SHALL be mapped by `ProcedureExceptionHandler` with an empty 404; ServerError SHALL continue to throw to the global exception handler.

#### Scenario: Two Procedure write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those two method descriptors are absent from the baseline and are not loosely typed
