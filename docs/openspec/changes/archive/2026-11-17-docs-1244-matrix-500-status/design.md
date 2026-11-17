> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Eleven matrix rows name a Bruno file and still say `500-ERROR`. Those Bruno files assert 200, 201 or 204. CU48 has no Bruno file and stays `500-ERROR`.

## Goals / Non-Goals

**Goals:**

- Mark the eleven Bruno-backed rows OK.
- Fail a unit test if that contradiction returns.

**Non-Goals:**

- New Bruno or Playwright tests.
- A live HTTP probe of CU48.

## Decisions

The Bruno assertion is the evidence. The notes that say "Green in the idempotent suite" already agree. Notes that say "Base GET returns 500" are replaced because the named file asserts success.

## Riesgos / Trade-offs

- [A Bruno file later starts expecting 500] → the matrix would still say OK until someone updates it. The test only forbids the pair "has a Bruno path" and "500-ERROR".
- [CU48 is still a real 500] → left unchanged.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Bruno-backed row is not 500-ERROR | unit | `workspace/tests/test_validate_cu_api_matrix.py` |

- New unit tests: one method that reads the committed CSV.
- New integration tests: none.
- Coverage impact (JaCoCo raise-only floor; Owner >90% target #2046): none.

TDD: the new test fails on the current CSV, then the CSV is corrected.

## Regression Strategy

- Existing tests affected: none of the validator cases change.
- Full suite command: `python3 -m unittest workspace.tests.test_validate_cu_api_matrix`
- HTTP/Bruno API suite: not changed
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. Matrix text and a unit test only.

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: not added; `preflight.sh --full` still runs the suite

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): the new unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: the matrix stays corrected

## Migration Plan

One pull request. #1244 stays open.

## Open Questions

None.
