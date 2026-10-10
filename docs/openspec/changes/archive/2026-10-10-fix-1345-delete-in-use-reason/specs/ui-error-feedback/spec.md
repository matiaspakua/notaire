# ui-error-feedback — delta

## Purpose

Mutation failures tell the user what happened and what to do.

## MODIFIED Requirements

### Requirement: A refused delete says why

When a DELETE fails, the page SHALL show `common.errors.inUse` as a warning with the server reason for 409, `common.errors.notFound` for 404, and the server message or the page fallback otherwise.

#### Scenario: Referenced record

- **WHEN** the user deletes a person the backend reports as referenced (409)
- **THEN** a warning toast shows the in-use text and the server reason, and no generic error

#### Scenario: Record already gone

- **WHEN** the backend answers 404
- **THEN** the toast says the record no longer exists

#### Scenario: Mapping and coverage

- **WHEN** presentDeleteError gets 409/404/500/network/401 failures and src/app is scanned
- **THEN** each maps as specified and no page toasts the generic delete error directly
