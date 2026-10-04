> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1242, Use Case CU76 – Quality Assurance and Testing Infrastructure. The backlog holds 276 open issues, several outdated, and no document states the measured baseline.

## Goals / Non-Goals

**Goals:** One measured, dated report; each finding linked to an issue; no duplicate issues.
**Non-Goals:** Fixing any finding; closing existing issues (tracked by #1248).

## Decisions

1. Existing open issues that already describe a finding are commented with current measurements, not duplicated. Rejected: opening a new issue per observation.
2. The report records the exact baseline commit and commands so it can be re-measured. Rejected: undated prose.

## Riesgos / Trade-offs

- The Spanish-text and unreachable-endpoint measurements are heuristics; the report and issues state the method and require confirmation before work.

- Description mapping from retired names is done once by position and type and reviewed by hand; a wrong mapping would be a documentation error, not a runtime one.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Documentation links stay valid | static | `scripts/test_docs_links.py` |
| Business documentation traceability stays valid | static | `scripts/test_business_docs_traceability.py` |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: n/a
- Coverage impact: none

## Regression Strategy

- Existing tests affected: none
- Full suite command: `bash scripts/preflight.sh`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit

## Rollback Strategy

- Revert the PR.
