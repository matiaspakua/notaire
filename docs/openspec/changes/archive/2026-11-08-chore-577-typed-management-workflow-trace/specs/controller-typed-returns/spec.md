# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Management workflow-trace handler is typed

`ManagementController#getWorkflowTrace` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Success stays 200 with `DtoManagementWorkflowTrace`. Expected validation failures stay 400 with `{"error": …}` via `ManagementWorkflowTraceExceptionHandler`.

#### Scenario: getWorkflowTrace leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ManagementController#getWorkflowTrace` is absent from the baseline and is not loosely typed

#### Scenario: Workflow trace keeps 200 body and 400 error shape

- **GIVEN** a management with a trace the port can build, or a validation failure such as a missing management
- **WHEN** the controller handles it
- **THEN** success is 200 with the workflow trace body and validation failure is 400 with `error`
