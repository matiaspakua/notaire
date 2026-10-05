> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1228, Use Case CU76 – Quality Assurance and Testing Infrastructure. Reproduced locally with a merge commit of an archive-only branch.

## Goals / Non-Goals

**Goals:** No false negative from the exception check.
**Non-Goals:** Changing the policy of the check.

## Decisions

1. Capture then match, rather than dropping pipefail: pipefail protects the other commands of the script.

## Riesgos / Trade-offs

- None: the other branches (label, bot, neither) are covered by the existing tests.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| A large diff starting with an OpenSpec path passes | static | `scripts/tests/test_pr_checks.py` SdlcExceptionTest |

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
