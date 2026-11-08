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

### Requirement: Person write handlers are typed

`PersonController#createPerson`, `PersonController#updatePerson`, and `PersonController#deletePerson` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Three Person write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those three method descriptors are absent from the baseline and are not loosely typed

### Requirement: Notebook create handler is typed

`NotebookController#createNotebook` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Notebook create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `NotebookController#createNotebook` is absent from the baseline and is not loosely typed

### Requirement: ProcedureType write handlers are typed

`ProcedureTypeController#create`, `ProcedureTypeController#update`, and `ProcedureTypeController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Three ProcedureType write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those three method descriptors are absent from the baseline and are not loosely typed

### Requirement: Copy create handler is typed

`CopyController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Copy create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `CopyController#create` is absent from the baseline and is not loosely typed

### Requirement: History create handler is typed

`HistoryController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: History create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `HistoryController#create` is absent from the baseline and is not loosely typed

### Requirement: DocumentCostTemplate createCost handler is typed

`DocumentCostTemplateController#createCost` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: DocumentCostTemplate createCost leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `DocumentCostTemplateController#createCost` is absent from the baseline and is not loosely typed

### Requirement: Substitution create handler is typed

`SubstitutionController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Substitution create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `SubstitutionController#create` is absent from the baseline and is not loosely typed

### Requirement: Testimony write handlers are typed

`TestimonyController#create`, `TestimonyController#update`, and `TestimonyController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Domain failures SHALL be mapped by `TestimonyExceptionHandler` with the same HTTP shapes clients already receive.

#### Scenario: Three Testimony write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those three method descriptors are absent from the baseline and are not loosely typed

### Requirement: TestimonyMovement write handlers are typed

`TestimonyMovementController#create`, `TestimonyMovementController#update`, and `TestimonyMovementController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Domain failures SHALL be mapped by `TestimonyMovementExceptionHandler` with the same HTTP shapes clients already receive.

#### Scenario: Three TestimonyMovement write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those three method descriptors are absent from the baseline and are not loosely typed

### Requirement: ProcedureType assignWorkflow handler is typed

`ProcedureTypeController#assignWorkflow` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. NotFound and inactive-workflow failures SHALL be mapped by `ProcedureTypeExceptionHandler` with the same HTTP shapes clients already receive.

#### Scenario: assignWorkflow leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ProcedureTypeController#assignWorkflow` is absent from the baseline and is not loosely typed

### Requirement: Procedure create and update handlers are typed

`ProcedureController#create` and `ProcedureController#update` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. NotFound SHALL be mapped by `ProcedureExceptionHandler` with an empty 404; ServerError SHALL continue to throw to the global exception handler.

#### Scenario: Two Procedure write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those two method descriptors are absent from the baseline and are not loosely typed

### Requirement: ProcedureFolder putOnHold handler is typed

`ProcedureFolderController#putOnHold` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. NotFound and BadRequest SHALL be mapped by `ProcedureFolderExceptionHandler` with the same HTTP shapes clients already receive.

#### Scenario: putOnHold leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ProcedureFolderController#putOnHold` is absent from the baseline and is not loosely typed

### Requirement: RegistrationDraft write handlers are typed

`RegistrationDraftController#generate`, `RegistrationDraftController#submit`, `RegistrationDraftController#observe`, and `RegistrationDraftController#recordRegistration` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. NotFound and Invalid SHALL be mapped by `RegistrationDraftExceptionHandler` with the same HTTP shapes clients already receive: an empty 404 and a 400 body `{"message": …}`. Generate success stays 201 with `Location`; the three updates stay 200 with `DtoRegistrationDraft`.

#### Scenario: Four write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those four method descriptors are absent from the baseline and are not loosely typed

#### Scenario: Invalid and missing drafts keep their HTTP bodies

- **GIVEN** a generate, submit, observe, or record-registration call whose port result is Invalid or NotFound
- **WHEN** the controller advice handles the domain exception
- **THEN** Invalid responds 400 with `message` and NotFound responds 404 with an empty body

### Requirement: Management archive handler is typed

`ManagementController#archiving` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. NotFound and waiting-folder conflict SHALL be mapped by `ManagementArchiveExceptionHandler` with the same HTTP shapes clients already receive: an empty 404 and a 409 body `{"message", "waitingFolderNumbers"}`. Success stays 200 with `DtoArchivedManagement`.

#### Scenario: archiving leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ManagementController#archiving` is absent from the baseline and is not loosely typed

#### Scenario: Missing management and waiting folders keep their HTTP bodies

- **GIVEN** an archive call whose port result is NotFound or WaitingFolders
- **WHEN** the controller advice handles the domain exception
- **THEN** NotFound responds 404 with an empty body and WaitingFolders responds 409 with `message` and `waitingFolderNumbers`

### Requirement: User login handler is typed

`UserController#login` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Lockout and rejected login SHALL be mapped by `UserLoginExceptionHandler` with the same HTTP shapes clients already receive: a 429 body `{"valid": false, "message": …}` and a 200 body `{"valid": false}`. Success stays 200 with `valid: true`, `token`, user fields, and `person` only when the user has a person id.

#### Scenario: login leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `UserController#login` is absent from the baseline and is not loosely typed

#### Scenario: Rejected and locked logins keep their HTTP bodies

- **GIVEN** a login call that is locked, rejected (not found, bad credentials, or inactive), or fails unexpectedly
- **WHEN** the controller advice handles the domain exception
- **THEN** lockout responds 429 with `valid` false and `message`, and rejection responds 200 with only `valid` false

### Requirement: User logout handler is typed

`UserController#logout` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>`, `ResponseEntity<?>`, or `ResponseEntity<Map<String, Object>>`. A presented token, a missing token, and an invalid token stay 200 with `{"ok": true}` and the cleared session cookie.

#### Scenario: logout leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `UserController#logout` is absent from the baseline and is not loosely typed

#### Scenario: Logout keeps the success body and cleared cookie

- **GIVEN** a logout call with a bearer token, or with no token
- **WHEN** the controller handles it
- **THEN** the response is 200 with `ok` true and a Set-Cookie that clears the session cookie
- **AND** revocation receives the presented token, or null when none is presented

### Requirement: Management create handler is typed

`ManagementController#create` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Success stays 201 with `ManagementResponse`. Bad request, not found, and the Updated arm stay empty 400, empty 404, and empty 200. `SaveResult.ServerError` stays a thrown `IllegalStateException` mapped by `GlobalExceptionHandler` to 500 `ErrorResponse`.

#### Scenario: create leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ManagementController#create` is absent from the baseline and is not loosely typed

#### Scenario: Create keeps the 201 body and empty error statuses

- **GIVEN** a create call whose port result is Created, BadRequest, NotFound, or Updated
- **WHEN** the controller handles it
- **THEN** Created responds 201 with the management fields, and the other three respond with an empty body at 400, 404, and 200

### Requirement: Management transition handler is typed

`ManagementController#transition` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Success stays 200 with `id` and `currentStatus`. A workflow rejection stays 400 `ErrorResponse` with `message`. A missing management stays 404 `ErrorResponse` with `message`.

#### Scenario: transition leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `ManagementController#transition` is absent from the baseline and is not loosely typed

#### Scenario: Transition keeps the 200 body

- **GIVEN** a transition the use case accepts for an existing management
- **WHEN** the controller handles it
- **THEN** the response is 200 with that management id and the resulting current status name

#### Scenario: Transition keeps the 400 and 404 error bodies

- **GIVEN** a transition the workflow rejects, or a management that does not exist
- **WHEN** the controller handles it
- **THEN** rejection responds 400 with `message`, and a missing management responds 404 with `message`

