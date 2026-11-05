# e2e-auth-storage Specification

## Purpose
TBD - created by archiving change test-1994-ts0109-mobile-auth-storage. Update Purpose after archive.
## Requirements
### Requirement: Shared admin storageState hydrates the dashboard
The system SHALL persist `localStorage["notaire-auth"]` with `isAuthenticated: true` into `tests/fixtures/admin-auth.json` during global-setup so the mobile Playwright project can mount dashboard pages without UI login.

#### Scenario: Mobile list pages leave the app shell skeleton
- **WHEN** a mobile-project test uses `storageState: tests/fixtures/admin-auth.json` and opens `/dashboard/people`
- **THEN** `data-testid=app-shell-skeleton` becomes hidden and list content can render

