# controller-typed-returns Delta

## ADDED Requirements

### Requirement: WorkflowValidation validate handler is typed

`WorkflowValidationController#validate` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT use `Object` or `?` in the response body type.

#### Scenario: WorkflowValidation validate leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `WorkflowValidationController#validate` is absent from the baseline and is not loosely typed
