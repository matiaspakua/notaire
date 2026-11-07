# controller-typed-returns Delta

## ADDED Requirements

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
