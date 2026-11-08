# reports-page-tests Specification

## Purpose
TBD - created by archiving change test-2109-reports-page-tests. Update Purpose after archive.

## Requirements

### Requirement: The reports page rules are unit tested

The frontend unit suite MUST assert that an id-based report cannot be downloaded without its id, that the period reports send a notary and folio type only when one is chosen, and that a failed download or catalog load is reported.

#### Scenario: an id-based report needs its id

- **WHEN** the id field of a report is empty
- **THEN** its download action is disabled

#### Scenario: a period report omits an unchosen filter

- **WHEN** the notary and folio type are left on all
- **THEN** the report is requested without them
