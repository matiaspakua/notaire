> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

ASLI-66 (#2073) and ASLI-67 (#2071) showed ambient Cursor `gh pr create` failing
with GraphQL `createPullRequest` 403. Push still worked. ManagePullRequest on a
`cursor/*` tip opened #2229; when that path was unavailable, the Owner used a
compare URL (#2228). Label friction is already documented (#2221); PR-create
friction was not.

## Goals / Non-Goals

**Goals:** (a) durable runbook in FLEET-IDENTITY / FLEET-LESSONS; (b) one short
pointer in steward / workflow / foreman; (c) TDD docs unittest; (d) keep
`FLEET_APP_*` optional and honest (no false claim scopes were granted).

**Non-Goals:** Configuring Cursor dashboard secrets; product code; weakening
Gate 1; reopening #2221/#2218; Wilson/Portal/Context Lake.

## Decisions

1. **Document observed facts, not hoped-for scopes** — ambient `gh` may lack
   `createPullRequest`; do not claim App PR write was granted mid-run.
2. **Preferred path** — push tip matching cloud `cursor/<desc>` (and optionally
   constitutional `type/#_desc`), then ManagePullRequest / Cursor PR tools.
3. **Owner compare fallback** — exact title (`[#n] type(scope): …`) and body with
   `Closes #n` when forge tools fail.
4. **KISS** — docs + unittest only; no new shell script for PR create.

## Riesgos / Trade-offs

Agents that ignore the skill still hit `gh` 403 — the skill/docs are the taught
path. Dual branch tips (`cursor/*` + `type/#_desc`) add a push step but match
proven ASLI-67 success.

## Testing Strategy

`workspace/tests/test_forge_pr_create_fallback_docs.py`: assert FLEET-IDENTITY /
FLEET-LESSONS name `createPullRequest`, ManagePullRequest / Cursor PR tools,
`cursor/` tip, Owner compare fallback, and that `FLEET_APP_*` is optional for
labels without claiming PR scopes granted. Assert steward / workflow / foreman
point at the cloud docs path. RED then GREEN.

## Regression Strategy

Existing `test_add_issue_wip_label.py` (#2221) unchanged. Live smoke optional:
`gh pr create` may 403 under ambient auth — document, do not fail Gate 1.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Docs/skills only.

## Rollback Strategy

Revert the PR.

## Migration Plan

None. New tips follow the documented PR-open path.
