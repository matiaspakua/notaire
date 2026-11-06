# controller-typed-returns Delta

## ADDED Requirements

### Requirement: ManagementStatus and WorkflowTransition deletes are typed

`ManagementStatusController#delete` and `WorkflowTransitionController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>`.

#### Scenario: Two empty-body deletes leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those two method descriptors are absent from the baseline and are not loosely typed
