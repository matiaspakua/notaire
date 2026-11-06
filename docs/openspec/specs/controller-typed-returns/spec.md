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
