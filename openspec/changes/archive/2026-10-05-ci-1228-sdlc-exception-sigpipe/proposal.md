# Fix the false failure of check-sdlc-exception.sh

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1228 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `ci/1228_sdlc_exception_sigpipe` |
| Gate 1 status | draft |

## Objetivo

The check reports that a PR has no OpenSpec change although the diff lists one, because `grep -q` ends the pipe early and `git diff` dies of SIGPIPE under pipefail. It failed Process Checks on the archive PRs #1216-#1220.

## What Changes

- `scripts/check-sdlc-exception.sh` captures the diff output in a variable before matching.
- `scripts/tests/test_pr_checks.py` gains a test with a diff larger than the pipe buffer whose first path is an OpenSpec change.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A PR that carries an OpenSpec change always passes the exception check | Constitution §12, #1228 | Made explicit |

## Capabilities

### New Capabilities

- (none — `skip_specs: true`)

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | one script, one test |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

CI tooling only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one line |
