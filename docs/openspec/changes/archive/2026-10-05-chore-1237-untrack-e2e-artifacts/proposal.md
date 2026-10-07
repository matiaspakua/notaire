# Untrack testing/e2e/node_modules and generated Playwright artifacts

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1237 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/1237_untrack_e2e_artifacts` |
| Gate 1 status | draft |

## Objetivo

main tracks 2925 files of testing/e2e/node_modules, the Playwright report and results, and a generated admin token fixture although testing/e2e/.gitignore excludes them. Remove them from the index and guard against it.

## What Changes

- `git rm --cached` of `testing/e2e/node_modules`, `testing/e2e/playwright-report`, `testing/e2e/test-results` and `testing/e2e/tests/fixtures/e2e-admin-token.txt`; the files stay on disk and ignored.
- `scripts/test_testing_standalone.py` fails when a tracked path lives under an ignored artifact directory of `testing/e2e`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Generated artifacts, dependency trees and credentials of the E2E suite are never tracked | CONSTITUTION security and repository hygiene | Made explicit |

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
| Docs / scripts / CI | yes | git index, one static guard |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Repository hygiene only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one entry |
