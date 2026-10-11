# documento-presentado-herencia-vencimiento Specification

## Purpose

Makes a submitted document inherit the expiry and the responsible party of its
document type when it is created, instead of always forcing empty values,
removing the data block that prevents "informar próximos vencimientos"
(CU42).

## Requirements

### Requirement: Inherit expiry and the responsible party from the document type
The system SHALL copy `expires`, `dueDays`, and `deliveredBy` from the
linked `DocumentType` when creating a submitted document, instead of
always assigning `expires = false` and `deliveredBy = ""`.

#### Scenario: Create a submitted document of a type that expires
- **WHEN** a submitted document is created from a document type
  with `expires = true`, and `dueDays` and `deliveredBy` filled in
- **THEN** the submitted document is stored with those same values of
  `expires`, `dueDays`, and `deliveredBy`

#### Scenario: Create a submitted document of a type that does not expire
- **WHEN** a submitted document is created from a document type
  with `expires = false`
- **THEN** the submitted document is stored with `expires = false` and without
  `dateDue`

### Requirement: Calculate the due date when the document is created
The system SHALL calculate `dateDue` as `dateEntry` plus
`dueDays` when the linked document type has `expires = true` and
`dateEntry` was provided.

#### Scenario: Due-date calculation
- **WHEN** a submitted document is created with `dateEntry` provided, from
  a document type with `expires = true` and `dueDays`
  filled in
- **THEN** the submitted document is stored with `dateDue` equal to
  `dateEntry` plus `dueDays` days

#### Scenario: No entry date means no due date
- **WHEN** a submitted document is created without `dateEntry`, from a
  document type with `expires = true`
- **THEN** the submitted document is stored without `dateDue`
