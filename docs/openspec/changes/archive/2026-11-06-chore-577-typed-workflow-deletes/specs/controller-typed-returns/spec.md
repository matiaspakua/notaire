# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Workflow definition and node deletes are typed

`WorkflowDefinitionController#delete` and `WorkflowNodeController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>`. Domain NotFound/InUse exceptions SHALL be mapped by controller advice with the same HTTP shapes as before.

#### Scenario: Two workflow deletes leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those two method descriptors are absent from the baseline and are not loosely typed

#### Scenario: WorkflowDefinition InUse delete returns 409 error body

- **GIVEN** manage.delete throws WorkflowDefinitionInUseException
- **WHEN** DELETE /api/v1/workflow-definition/{id}
- **THEN** the response is 409 with `{"error": …}`

#### Scenario: WorkflowNode NotFound delete returns 404

- **GIVEN** manage.delete throws WorkflowNodeNotFoundException
- **WHEN** DELETE /api/v1/workflow-node/{id}
- **THEN** the response is 404
