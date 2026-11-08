# cleanup-actions-workflow-name Specification

## Purpose
TBD - created by archiving change chore-1981-rename-cleanup-actions-workflow. Update Purpose after archive.
## Requirements
### Requirement: Cleanup Actions workflow uses the correct filename

The repository MUST ship `.github/workflows/cleanup-actions.yml` and MUST NOT ship
the typo filename that dropped the leading `c`. Permanent documentation and metrics
copy MUST reference `cleanup-actions.yml`.

#### Scenario: Typo workflow file is gone
- **GIVEN** the repository checkout
- **WHEN** `.github/workflows/` is inspected
- **THEN** `cleanup-actions.yml` exists and the typo filename does not

#### Scenario: Permanent references use cleanup-actions.yml
- **GIVEN** permanent tree files (excluding active OpenSpec drafts)
- **WHEN** searched for the typo workflow filename
- **THEN** no matches remain

