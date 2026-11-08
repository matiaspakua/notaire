> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.


## Context

New-code Sonar severity is ratcheted (#1737). Legacy overall findings still open on #1535. Hex [#1755](https://github.com/matiaspakua/notaire/pull/1755) holds CHANGELOG Changed until merge — this design waits; no branch until then.

## Goals / Non-Goals

**Goals:** Measurable in-repo inventory of overall Sonar VULNERABILITY (and high-severity) issues with English classification; optional tiny safe fixes.  
**Non-Goals:** Closing all bugs/smells; hexagon jpa equality findings; SpotBugs fail-build.

## Decisions

1. **Ratcheting slice, not umbrella close** — PR uses `Refs #1535` unless AC is fully satisfied.
2. **Script next to `sonar-severity-report.py`** — reuse auth/URL env; prefer new `sonar-legacy-inventory.py` so severity report stays a one-liner for Mac smoke.
3. **Checked-in inventory** — human-readable markdown table; regenerate on Mac/local when Sonar is up; CI unit-tests the parser with fixtures (no live Sonar in cloud CI).
4. **Classification enum (English):** `fix` | `false_positive` | `accepted_risk` | `defer_hexagon`.
5. **CHANGELOG** — Unreleased after #1755; serialize vs #1754 if still open.
6. **English only** — identifiers, test method names, inventory headers, comments.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Sonar counts drift vs inventory | Document refresh command; re-run after `run-sonar.sh` |
| Over-fixing hexagon-owned rules | Default `defer_hexagon` for `*JpaController` / S4973 / RC_REF_COMPARISON |
| CHANGELOG conflict | Wait for #1755; re-check open CHANGELOG PRs before push |

## Testing Strategy

Failing unit tests for inventory JSON parsing / classification table emission first; then implement. No Playwright UI.

## Regression Strategy

`sonar-severity-report.py` behavior unchanged. Existing SonarAnalysis / infra tests stay green.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Docs + scripts ship with next release; no runtime deploy change.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None for slice 1 — later slices pick `fix` rows as child issues.
