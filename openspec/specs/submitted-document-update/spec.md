# submitted-document-update Specification

## Purpose

Editing a submitted document does not erase its progress. Source: #1241; owner CU72.

## Requirements

### Requirement: Update changes only the fields it carries

`PUT /api/v1/documento-presentado/{id}` MUST change only the fields present in the request and MUST keep every other stored field, including the name, the prepared, released, flagged and reentered flags, the trámite link and the dates.

#### Scenario: Delivered flag update keeps the rest

- **WHEN** a document with a name, a trámite link and raised flags is updated with only `delivered`
- **THEN** `delivered` changes and the name, the trámite link and every flag are unchanged

#### Scenario: Name update keeps the flags

- **WHEN** a document with raised flags is updated with only a new name
- **THEN** the name changes and the flags and the trámite link are unchanged

#### Scenario: Unknown document

- **WHEN** a document that does not exist is updated
- **THEN** the response is 404

### Requirement: Derived due fields follow the type and the date

When an update changes the document type or the entry date, the system MUST recompute whether the document expires, its due days and its due date from the resulting type and date.

#### Scenario: Date change recomputes the due date

- **WHEN** a document whose type expires after 10 days is updated with a new entry date
- **THEN** its due date is the new entry date plus 10 days
