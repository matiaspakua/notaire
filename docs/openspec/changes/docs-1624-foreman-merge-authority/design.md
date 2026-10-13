> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §10 Rules for AI Agents, §12 Governance.

## Context

See #1624. `app/cursor` merged #1594 and #1595 with red `Process Checks`. The
Constitution's Roles table gave merge authority to the Owner only, and
`cloud-foreman.md`, `FLEET-ARCHITECTURE.md` and `local-ai/sdlc/AI-SDLC.md` gave
it to the Foreman.

## Goals / Non-Goals

**Goals:**

- One rule, stated identically everywhere and enforced by GitHub plus the harness.

**Non-Goals:**

- A new required check, or changes to the ruleset itself.

## Decisions

1. **Delegation with an exclusion list.** The Foreman merges routine Worker PRs.
   The Owner keeps the PRs that change the rules, the exceptions, or the
   Foreman's own powers. That stops the Foreman from approving changes to
   itself.
2. **The ruleset plus `check-heavy-ci.sh`.** The ruleset blocks red required
   checks. `check-heavy-ci.sh` also refuses heavy jobs that are pending or
   missing, which can happen when an aggregator is green while its leaves are
   skipped.
3. **A contract test over the docs.** Phrase checks are blunt, but they catch the
   exact drift that caused #1624.

## Riesgos / Trade-offs

- [Phrase-based test is brittle under rewording] → the test names the canonical phrase "Foreman merge rule". Reword deliberately.
- [The Foreman misclassifies a harness PR as routine] → the ruleset still requires green checks. The Owner can revert.

## Testing Strategy

`workspace/tests/test_merge_authority.py`: 4 tests (8 failures and 1 error before, all OK after).

## Regression Strategy

`workspace/tests`, `local-ai/sdlc/tests`, and `bash -n local-ai/sdlc/foreman.sh`.

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
