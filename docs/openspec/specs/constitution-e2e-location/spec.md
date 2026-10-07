# constitution-e2e-location Specification

## Purpose

Keep the Constitution consistent with the repository after the Playwright suite moved. Source: #1210; owner CU76.

## Requirements

### Requirement: The Constitution names the real Playwright location

`CONSTITUTION.md` MUST refer to the Playwright suite as living in `testing/e2e` and MUST NOT mention
`frontend/tests/e2e` or `cd frontend && npx playwright`.

#### Scenario: No stale Playwright path in the Constitution

- **WHEN** the legacy-reference guard scans the tracked files, `CONSTITUTION.md` included
- **THEN** it reports no reference to the old location

#### Scenario: The E2E command points at testing/e2e

- **WHEN** step 15 of the workflow is read
- **THEN** the command is `cd testing/e2e && npx playwright test`

#### Scenario: Agent rule files stay consistent

- **WHEN** `bash workspace/sdlc/check-agent-rules.sh` runs
- **THEN** it exits 0
