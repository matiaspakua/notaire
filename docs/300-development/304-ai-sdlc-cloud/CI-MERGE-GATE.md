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
gh run list --branch <pr-head-branch> --limit 10
# Ensure CI - Build, Test & Security and Playwright E2E are completed success
# for the PR head SHA (not merely queued/in_progress).
```

Also confirm no required check on the head SHA is still `pending` / `queued` /
`in_progress`.

## Context

Observed 2026-10-02 on PRs around #1126 / #1128: premature merge was avoided
after light CI reported success while backend CI and Playwright were still
pending. Applies to all future autonomous merges.
