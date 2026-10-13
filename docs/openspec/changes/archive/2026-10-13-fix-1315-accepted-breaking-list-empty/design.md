# Design

## Context

Issue #1315 asks that the OpenAPI contract be coherent, complete, clear and valid, and that the accepted-breaking list added by #1312 be emptied once its changes merge, with a guard or documented rule to keep it empty. This is slice 1 of #1315; the per-controller contract work (status codes, constraints, validator in CI) follows in later slices.

## Goals / Non-Goals

Goal: an empty list on main and a CI guard that keeps it so. Non-goals: the per-endpoint contract audit, an OpenAPI linter in CI, the DELETE 200/204 decision.

## Decisions

Guard rather than only a documented rule: a stale entry fails the next pull request with a message naming it, so nobody has to remember. Pull requests only, because on main the base equals the revision and every entry would look stale. Matching mirrors oasdiff err-ignore (containment, case-insensitive) so an entry that oasdiff honours is never called stale. The binary is downloaded and checksum-verified instead of adding a second oasdiff action step, because the existing guard requires exactly one action step.

## Riesgos / Trade-offs

The first pull request after any merged entry fails until it removes the line; the message says exactly what to delete. A download failure of oasdiff fails the step; the URL is the same GitHub release the action uses.

## Testing Strategy

`test_check_accepted_breaking_changes.py` (10 cases, one with real oasdiff) committed first and observed failing: the checker and its wiring did not exist. Against main the new checker reports all 5 old entries as stale.

## Regression Strategy

`workspace/verify.sh`, `test_dast_contract_backup_assets.py`, all `unittest discover` suites from preflight, actionlint on the workflow; oasdiff against main.

## Playwright Strategy

No UI change.

## Deployment Strategy

CI only; no runtime change.

## Rollback Strategy

Revert the merge commit (restores the entries and removes the step).
