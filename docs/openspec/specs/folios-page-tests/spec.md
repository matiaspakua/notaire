# folios-page-tests Specification

## Purpose
TBD - created by archiving change test-2105-folios-page-tests. Update Purpose after archive.

## Requirements

### Requirement: The folios page rules are unit tested

The frontend unit suite MUST assert that a folio needs a number, a type and a notary, that a deed linked to another folio cannot be linked again while a folio keeps its own, that the list narrows by status on the server, and that folio types are searched, saved and deleted with failures reported.

#### Scenario: a folio needs a notary

- **WHEN** a folio is saved with a number and a type but no notary
- **THEN** the save is refused and nothing is sent

#### Scenario: a deed linked to another folio is excluded

- **WHEN** the deed picker is opened for a new folio and for the folio that owns the deed
- **THEN** the deed is excluded for the new folio and offered for its own folio
