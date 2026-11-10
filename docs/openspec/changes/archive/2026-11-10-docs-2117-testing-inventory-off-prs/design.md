> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`generate-testing-inventory.py` rewrites a shared monolith with a timestamp and
global suite counts. `validate-testing-inventory.py` required the committed file
to match a fresh regen (timestamp stripped). Parallel test PRs therefore edited
the same count lines; conflict resolutions kept stale counts (see #2117 evidence).

## Goals / Non-Goals

**Goals:** (a) discovery-based validation (TS-nnnn + readable trees + printed
counts); (b) stop feature PRs from committing a shared count table; (c) keep an
on-demand / docs-build human report; (d) TDD + docs/fleet lesson update.
**Non-Goals:** New PR CI workflow; Constitution merge policy; lowering preflight;
per-suite record files; Wilson/portal/Context Lake; `[committer] required=true`.

## Decisions

1. **Discovery is the gate** — validate walks Playwright specs and confirms
   backend/frontend/Bruno trees are readable; prints counts; never diffs a
   committed count table.
2. **Stub + generated sibling** — committed `INVENTORY.md` is a stable stub (no
   shared counts). Full report goes to `INVENTORY.generated.md` (gitignored).
3. **Docs build regen** — github-page `prebuild` runs the generator so
   `preflight.sh --full` produces a fresh local report without committing it.
   Pages metrics already use `tech-facts.testCounts()` from disk.
4. **Fleet lesson** — after merging `main`, regenerate locally if you need the
   report; never keep either side's count line in a conflict (and feature PRs
   should not touch the generated report at all).
5. **KISS** — no bot that commits to `main`; no new workflow.

## Riesgos / Trade-offs

Committed stub can lag vs disk for humans browsing GitHub.com — mitigated by
generate + Pages live counts. Agents that ignore docs may still try to commit
`INVENTORY.generated.md` — gitignore blocks the usual path.

## Testing Strategy

Update `testing/tests/test_testing_inventory.py`:
- validator passes on current tree and prints counts
- unprefixed Playwright spec fails
- validator does **not** fail when the stub / missing generated report differs
  from a fresh full report
- generator writes real Vitest/backend counts into the generated report
RED then GREEN; preflight `--full` is Gate 3 proof.

## Regression Strategy

Existing MTP / process-architecture doc tests stay. Migration inventory still
marks the path as a documented hotspot with an updated note.

## Playwright Strategy

n/a — no product UI change. Naming guard remains in the inventory validator.

## Deployment Strategy

Docs/scripts only; no runtime deploy. Pages picks up prebuild generate on next
docs build / preflight.

## Rollback Strategy

Revert the PR (restore monolith validation).

## Migration Plan

This PR replaces the committed monolith with the stub and archives the OpenSpec
delta into main specs. Open feature PRs should drop `INVENTORY.md` hunks on
rebase.

## Open Questions

None.
