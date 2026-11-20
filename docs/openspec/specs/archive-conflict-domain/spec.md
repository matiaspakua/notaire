# archive-conflict-domain Specification

## Purpose
An unconfirmed archive of a management with a folder on hold is a domain conflict that carries folder numbers. Issue #2351 stays open for the other business.* imports.

## Requirements

### Requirement: Archive hold rejection is a domain exception

When a management has a procedure folder on hold and archiving is not confirmed, the archive SHALL be rejected with a domain exception that carries the folder numbers and SHALL NOT expose the persistence entity.

#### Scenario: an unconfirmed archive names the folder number

- **WHEN** a management has a folder on hold numbered 1 and archiving is not confirmed
- **THEN** the rejection is a domain conflict and its folder numbers are 1
