> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1236, Use Case CU76 – Quality Assurance and Testing Infrastructure. Found while writing the E2E spec of #774: a gestión created through the helper cannot be loaded by id.

## Goals / Non-Goals

**Goals:** Helper-created gestiones are real, loadable gestiones.
**Non-Goals:** Changing the gestiones API to reject unknown fields.

## Decisions

1. A static guard on the stale field name instead of making the API strict, which would affect every client.

## Riesgos / Trade-offs

- TS-0018 uses the helper; it is re-run to prove it still passes with a loadable gestión.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| The helper sends notaryPersonId | static | `scripts/test_e2e_reliability.py` |
| No E2E source sends fkIdNotaryPerson | static | `scripts/test_e2e_reliability.py` |

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
