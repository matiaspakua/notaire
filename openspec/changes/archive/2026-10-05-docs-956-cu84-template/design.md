> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #956, Use Case CU84 – Login al sistema. The 2026-09-05 RF-CU traceability audit found CU84 off-template and two broken CSV rows.

## Goals / Non-Goals

**Goals:** A valid, unique Login requirement row; CU84 on the template; a guard so the cross-check stays automatable.
**Non-Goals:** Changing the login behaviour or renumbering other requirements.

## Decisions

1. A new requirement issue (#1224) supplies the GitHub ID, because no issue existed for the Login requirement and the CSV must reference a real one. Rejected: reusing #956, which tracks the documentation fix, not the requirement.
2. The guard checks structure only (shape, uniqueness, template rows); it does not call GitHub.

## Riesgos / Trade-offs

- Other documents quoting the old CU84 headings: none found by grep.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Every requirement row has a valid unique GitHub ID | static | `scripts/test_business_docs_traceability.py` |
| Login appears exactly once in the requirements | static | `scripts/test_business_docs_traceability.py` |
| Every Use Case file has Referencias Cruzadas and GitHub ID rows | static | `scripts/test_business_docs_traceability.py` |

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
