# controller-typed-returns Specification

## Purpose
Typed write handler returns; shrink the Object loose-return baseline.
## Requirements
### Requirement: Folio Item Copy write handlers are typed

`ItemController#create`, `FolioController#update`, and `FolioController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>`.

#### Scenario: Four handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those three method descriptors are absent from the baseline and are not loosely typed

### Requirement: FolioType and DocumentType write handlers are typed

`FolioTypeController#create`, `FolioTypeController#update`, `FolioTypeController#delete`, `DocumentTypeController#create`, `DocumentTypeController#update`, and `DocumentTypeController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>`.

#### Scenario: Six catalog write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those six method descriptors are absent from the baseline and are not loosely typed

### Requirement: IdentificationType create handler is typed

`IdentificationTypeController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>`.

#### Scenario: IdentificationType create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `IdentificationTypeController#create` is absent from the baseline and is not loosely typed

### Requirement: Property create handler is typed

`PropertyController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>`.

#### Scenario: Property create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `PropertyController#create` is absent from the baseline and is not loosely typed

### Requirement: ManagementStatus and WorkflowTransition deletes are typed

`ManagementStatusController#delete` and `WorkflowTransitionController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>`.

#### Scenario: Two empty-body deletes leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those two method descriptors are absent from the baseline and are not loosely typed

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

### Requirement: Role create and assign handlers are typed

`RoleController#createRole` and `RoleController#assignRoleToUser` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>`. Domain conflict/not-found exceptions SHALL be mapped by advice with the same HTTP shapes as before.

#### Scenario: Two role handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those two method descriptors are absent from the baseline and are not loosely typed

#### Scenario: Role name conflict create returns 409 error body

- **GIVEN** manage.create throws RoleNameConflictException
- **WHEN** POST /api/v1/roles
- **THEN** the response is 409 with `{"error": …}`

### Requirement: BudgetTemplate write handlers are typed

`BudgetTemplateController#create`, `BudgetTemplateController#update`, and `BudgetTemplateController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Three BudgetTemplate handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those three method descriptors are absent from the baseline and are not loosely typed

### Requirement: Concept write handlers are typed

`ConceptController#createConcept`, `ConceptController#updateConcept`, and `ConceptController#deleteConcept` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Three Concept write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those three method descriptors are absent from the baseline and are not loosely typed

### Requirement: WorkflowValidation validate handler is typed

`WorkflowValidationController#validate` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT use `Object` or `?` in the response body type.

#### Scenario: WorkflowValidation validate leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `WorkflowValidationController#validate` is absent from the baseline and is not loosely typed

