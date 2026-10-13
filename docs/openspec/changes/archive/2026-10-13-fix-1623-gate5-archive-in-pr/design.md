> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 step 24, §6 Gate 5.

## Context

See the proposal. `validate-sdlc-plan.sh` validates every active change on every PR and failed on any closed issue. The hygiene check ran a live inventory on PRs. Archiving happened after merge, so every merge created a leftover.

## Goals / Non-Goals

**Goals:** no leftover can exist after a merge; no PR is blocked by another's leftover; bad deltas fail at Gate 1; `main` shows process breakage.
**Non-Goals:** auto-archiving bots; changing the OpenSpec CLI.

## Decisions

1. **Archive in the PR, not after.** Smoke evidence is written into the archived ledger later. Rejected alternative: a post-merge bot PR. It adds a second PR per change and still leaves a window where every PR is red.
2. **`multi_pr: true` opt-out**, declared in the change. That keeps it visible and reviewable, unlike a label.
3. **Warn on PRs, fail on `main`.** Blast radius stays on the PR that caused it. `main-health` makes the leftover visible within one run.
4. **The delta check is a separate Python helper**, unit-testable and called by the bash validator. When the helper is missing it notes this; it never silently passes.

## Riesgos / Trade-offs

- [Agents must archive before the first push, because preflight blocks it] → intended: push green. The template's 10.1 spells it out.
- [`multi_pr` abused to skip archiving] → `main-health` inventory fails when its issue closes.

## Testing Strategy

`test_openspec_archive_hygiene.py` (+5 tests, one fixture extended with `multi_pr`), `test_check_delta_ops.py` (6), `test_sdlc_process_on_main.py` (3), docs tests via `openspec_changes.change()`. Red observed for each before implementing.

## Regression Strategy

All 8 self-test suites, `validate-sdlc-plan.sh`, `--inventory-only`, `openspec validate --specs --strict` (182/182), actionlint on the workflow.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Effective on merge. **Owner merges** (Constitution amendment).

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
