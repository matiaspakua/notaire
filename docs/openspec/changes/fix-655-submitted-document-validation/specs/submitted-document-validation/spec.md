# submitted-document-validation — delta

## Purpose

Submitted document create/update requests are validated before anything is stored.

## MODIFIED Requirements

### Requirement: Submitted document request validation

Creating or updating a submitted document SHALL answer 400 when `date` is not a real `yyyy-MM-dd` day and 404 when `typeId` or `procedureId` does not exist, and SHALL NOT store anything in either case.

#### Scenario: Invalid date on create

- **WHEN** a document is created with a date such as `05/09/2026` or `2026-02-30`
- **THEN** the response is 400 naming the `yyyy-MM-dd` format

#### Scenario: Unknown reference on create

- **WHEN** a document is created with an unknown type or procedure id
- **THEN** the response is 404 and no document is stored

#### Scenario: Invalid input on update

- **WHEN** a stored document is updated with an invalid date or an unknown reference
- **THEN** the response is 400 or 404 and the stored date and links are unchanged

#### Scenario: Partial update of a stored document

- **WHEN** only `deliveredBy` is updated on a stored dated document
- **THEN** the response is 200 and the due date is recomputed in calendar days
