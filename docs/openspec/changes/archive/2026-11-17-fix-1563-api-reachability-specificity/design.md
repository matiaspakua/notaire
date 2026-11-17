> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`reachable()` treats every exact fit as covered. `{id}` is an exact fit for the literal segment `date`, so the parameterized route hides behind the fixed route. #1250 already records the same kind of endpoint as bucket A.

## Goals / Non-Goals

**Goals:**

- Prefer the template with the fewest wildcards.
- Keep the existing wildcard fallback when no exact template fits.
- Allowlist every endpoint the stricter scan newly reports, with the bucket A reason.

**Non-Goals:**

- Changing OpenAPI or the frontend.
- A new allowlist bucket.

## Decisions

Fewest wildcards, not "reject any wildcard when a fixed template also fits", because two templates can tie (both one wildcard). A tie keeps both. The date-versus-id case has zero versus one, so only the fixed template remains.

Newly uncovered GET-by-id routes use the existing bucket A sentence. That is the decision #1250 already made for the same shape. This change does not ask the Owner again.

## Riesgos / Trade-offs

- [A literal that really is the id, written without `${}`] → it would no longer cover `{id}`. The frontend uses `${id}` for those calls, and the existing test `test_wildcard_prefers_the_path_parameter_over_a_fixed_segment` still guards that direction.
- [The allowlist grows] → each new line names the endpoint and the reason, which is the point of the gate.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Date route is not a call to the id route | unit | `contracts/tests/test_api_reachability.py` |

- New unit tests: one method on `ScannerTest`.
- New integration tests: none.
- Coverage impact (JaCoCo raise-only floor; Owner >90% target #2046): none. This is a Python guard.

TDD: add the test, watch it fail, then change `reachable()`.

## Regression Strategy

- Existing tests affected: `test_every_endpoint_is_called_from_the_ui_or_allowlisted` will name the newly visible endpoints until the allowlist records them. Assertions are not weakened.
- Full suite command: `python3 contracts/tests/test_api_reachability.py`
- HTTP/Bruno API suite: not affected
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. The scanner and the allowlist only.

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: not run for this change alone; `preflight.sh --full` still runs the suite

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `python3 contracts/tests/test_api_reachability.py`

## Rollback Strategy

- Revert safe: yes. No data is written.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: the gate stays stricter until revert

## Migration Plan

One pull request.

## Open Questions

None.
