# controller-typed-returns Delta

## ADDED Requirements

### Requirement: FolioType and DocumentType write handlers are typed

`FolioTypeController#create`, `FolioTypeController#update`, `FolioTypeController#delete`, `DocumentTypeController#create`, `DocumentTypeController#update`, and `DocumentTypeController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>`.

#### Scenario: Six catalog write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those six method descriptors are absent from the baseline and are not loosely typed
