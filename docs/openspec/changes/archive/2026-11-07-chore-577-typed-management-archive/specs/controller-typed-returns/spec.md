# controller-typed-returns Delta

## ADDED Requirements

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
