# CI merge gate — do not trust light-only green

> Fleet process rule for Cursor Cloud foreman / merge-when-green.
> Also indexed from [`ENVIRONMENT-CHECKLIST.md` §7](ENVIRONMENT-CHECKLIST.md) and
> [`FLEET-ARCHITECTURE.md` §9](FLEET-ARCHITECTURE.md).

## False positive

**Light CI can look fully green while heavy CI is still pending.**

After a PR push, these often finish first (~12–18 checks):

- PR Validation
- Frontend (Vitest / TypeScript / lint)
- SDLC Process Checks
- Other fast jobs (CodeQL Analyze matrix, Dependabot submit, …)

Meanwhile these may still be **pending** (or not yet reported to the
subscription):

- `CI - Build, Test & Security` (backend Unit, Integration, Coverage Gate, …)
- `Playwright E2E — Full Suite` (Bruno API + UI E2E)

**Do not trust CI subscription success alone.** Cursor / GitHub CI subscriptions
can deliver “all N checks success” (e.g. **18 checks** on #1137) when they only
see the completed light suite — while `CI - Build, Test & Security` and/or
Playwright are still **pending** or not yet in the rollup. That delivery is
**not** mergeable. Always run `bash scripts/check-heavy-ci.sh <pr>` before
`gh pr merge`; ignore subscription “success” unless that script exits 0.

`gh pr checks` has the same blind spot when heavy jobs are missing or still
queued. Count of green checks ≠ heavy gate.

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
Checks, CodeQL Analyze, …) is **insufficient** — even when a subscription says
“all N checks success.”

Docs-only PRs are not exempt: `playwright-e2e.yml` still runs on PRs into
`main`, so Bruno + Playwright remain required unless the workflow is
explicitly skipped for that tip.

Local mirror before push: [`CI-PREFLIGHT.md`](../CI-PREFLIGHT.md)
(`bash scripts/preflight.sh`, optionally `--full`).

## Verify before merge

```bash
# REQUIRED — agents must run this before `gh pr merge`.
# Do not merge on subscription “all checks success” alone.
bash scripts/check-heavy-ci.sh <pr-number>

# Or manually (still confirm the four required names are success):
gh run list --branch <pr-head-branch> --limit 10
# Ensure CI - Build, Test & Security and Playwright E2E are completed success
# for the PR head SHA (not merely queued/in_progress).
```

Exit 0 from `check-heavy-ci.sh` is the only subscription-safe merge signal.
The script treats required checks that are absent from `gh pr checks` as
**fail**: if the parent workflow (`CI - Build, Test & Security` or
`Playwright E2E — Full Suite`) is queued/pending/in_progress on the PR head, it
prints `pending (workflow …)` instead of bare `missing` — still not mergeable.
Also confirm no required check on the head SHA is still `pending` / `queued` /
`in_progress`.

## Stale PR diagnosis — rebase before inventing product fixes

If **Integration** and/or **Playwright** fail with Budget/person /
`undefined, undefined` symptoms (presupuesto pickers showing missing client
names) and the branch is **behind `main`**, **rebase onto `main` first**.

Do not invent product fixes for a class of failures already fixed on `main`.
[#1132](https://github.com/matiaspakua/notaire/pull/1132) nested
`BudgetResponse.person` as `PersonRef` with `personId` **and** `name` /
`lastName`. A nested `person: { personId }` alone is not enough for UI E2E.

Checklist when Integration/Playwright go red on a long-lived tip:

1. `git fetch origin main && git merge-base --is-ancestor origin/main HEAD`
   (if not ancestor → rebase/merge `main`).
2. Re-run heavy CI; only then debug product code.

## Context

Observed 2026-10-02 on PRs around #1126 / #1128 / #1132 / #1136 / #1137:
premature merge was avoided after light CI (~12–18 checks) or a CI
subscription “all N checks success” while backend CI and Playwright were still
pending; stale tips looked like product bugs until rebased onto #1132. Applies
to all future autonomous merges.

## Runner contention

Many open PR tips each trigger a full Playwright suite. Hotfixes can sit
`queued` for minutes behind superseded tips. Prefer not pushing docs-only or
low-priority PR commits while a main hotfix is waiting on Playwright. Agents
with read-only `gh` cannot `gh run cancel` superseded workflows (HTTP 403).

## Related: CodeQL advanced vs default setup

Adding `.github/workflows/codeql.yml` while GitHub Code Scanning **default
setup** is enabled causes SARIF rejection / Analyze failures. Ops notes and
`scripts/enable-gh-secure.sh`:
[DevSecOps — CodeQL](../../200-architecture/208-devsecops/README.md#codeql-advanced-vs-default-setup).
