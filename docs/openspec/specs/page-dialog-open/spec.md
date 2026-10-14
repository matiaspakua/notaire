# page-dialog-open Specification

## Purpose
TBD - created by archiving change test-1669-open-page-dialogs. Update Purpose after archive.

## Requirements

### Requirement: Every dashboard page opens the dialogs its buttons lead to

The frontend unit suite MUST click each enabled button of every populated dashboard page once and MUST fail when a click throws. The Vitest coverage floors MUST be at least 67% statements, 60% branches, 63% functions and 68% lines.

#### Scenario: buttons are clicked without error

- **WHEN** every enabled button of a populated page is clicked once
- **THEN** the page still renders without an application error or an unhandled error

#### Scenario: the floors follow the measurement

- **WHEN** the configured thresholds are compared with the documented floors
- **THEN** they are equal or higher
