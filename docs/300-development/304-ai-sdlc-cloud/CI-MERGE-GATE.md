# CI merge gate — do not trust light-only green

> Fleet process rule for Cursor Cloud foreman / merge-when-green.
> Also indexed from [`ENVIRONMENT-CHECKLIST.md` §7](ENVIRONMENT-CHECKLIST.md) and
> [`FLEET-ARCHITECTURE.md` §9](FLEET-ARCHITECTURE.md).

## False positive

**Light CI can look fully green while heavy CI is still pending.**

After a PR push, these often finish first (~12 checks):

- PR Validation
- Frontend (Vitest / TypeScript / lint)
- SDLC Process Checks

Meanwhile these may still be **pending**:

- `CI - Build, Test & Security` (backend Unit, Integration, Coverage Gate, …)
- `Playwright E2E — Full Suite` (Bruno API + UI E2E)

Subscriptions and `gh pr checks` can report “all green” when they only see the
completed light suite. That is **not** mergeable.

## Hard rule

**Never merge** until every required check below is terminal **success**
(or explicitly workflow-skipped):

| Required check | Workflow |
|----------------|----------|
| Unit Tests (backend) | `CI - Build, Test & Security` |
| Integration Tests | `CI - Build, Test & Security` |
| Coverage Gate (`mvn verify`) | `CI - Build, Test & Security` |
| API Tests (Bruno) | `Playwright E2E — Full Suite` (when that workflow runs) |
| UI E2E Tests (Playwright) | `Playwright E2E — Full Suite` |

Light-only green (Validate PR, Code Lint, Frontend Vitest/TypeScript, Process
Checks) is **insufficient**.

## Verify before merge

```bash
# Preferred — agents must run this before `gh pr merge`:
bash scripts/check-heavy-ci.sh <pr-number>

# Or manually:
gh run list --branch <pr-head-branch> --limit 10
# Ensure CI - Build, Test & Security and Playwright E2E are completed success
# for the PR head SHA (not merely queued/in_progress).
```

Also confirm no required check on the head SHA is still `pending` / `queued` /
`in_progress`.

## Related regression: BudgetResponse.person shape

A nested `person: { personId }` alone is **not** enough for UI E2E. The
frontend presupuesto pickers read `person.name` and `person.lastName`; missing
names render as `undefined, undefined` and fail Playwright (pagos/gestiones).
`PersonRef` must include `personId` **and** `name`/`lastName`.

## Context

Observed 2026-10-02 on PRs around #1126 / #1128 / #1132: premature merge was
avoided after light CI (~12 checks) reported success while backend CI and
Playwright were still pending. Applies to all future autonomous merges.

## Runner contention

Many open PR tips each trigger a full Playwright suite. Hotfixes can sit
`queued` for minutes behind superseded tips. Prefer not pushing docs-only or
low-priority PR commits while a main hotfix is waiting on Playwright. Agents
with read-only `gh` cannot `gh run cancel` superseded workflows (HTTP 403).
