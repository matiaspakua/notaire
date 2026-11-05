# Merge gate — local full preflight

> Fleet process rule for Cursor Cloud foreman / merge-when-ready.
> Also indexed from [`ENVIRONMENT-CHECKLIST.md` §7](ENVIRONMENT-CHECKLIST.md) and
> [`FLEET-ARCHITECTURE.md` §9](FLEET-ARCHITECTURE.md).

## The gate (#2001)

Product GitHub workflows do not run on a pull request or on push to `main`.
A pull request is ready when this has passed on the head:

```bash
bash workspace/sdlc/preflight.sh --full
```

A run without `--full` is not sufficient. GitHub will not re-run the skipped
suites. CodeQL stays weekly (and on version tags) and is not local.

`bash workspace/sdlc/check-heavy-ci.sh <pr>` exits 0 when those workflows were
not started for the head. It still fails if one was dispatched and is pending
or red. Do not wait for check names that will never appear.

## Who may merge

The **Foreman merge rule** (CONSTITUTION §5 step 21, #1624): the Foreman merges
a Worker PR once `preflight.sh --full` has passed and `check-heavy-ci.sh` exits 0.
The Owner merges Constitution amendments, `sdlc-exception` PRs and PRs that change
the Foreman's harness or merge path. Workers never merge.

## Ruleset (#1040)

Desired `protect-main` no longer requires `CI`, `Frontend CI`, `Playwright E2E`,
`Code Lint`, or `PR Validation`, because pull requests do not report them.
The Owner applies `bash security/apply-protect-main-ruleset.sh --apply` after
the #2001 merge. Until then GitHub refuses every merge. If the live ruleset
also requires `OpenAPI Contract`, remove that context too.

## What --full covers

Integration, coverage, Bruno, and Playwright run inside
`bash workspace/sdlc/preflight.sh --full` ([`CI-PREFLIGHT.md`](../CI-PREFLIGHT.md)).
They also run on a version tag. They do not run on the pull request.

## OpenSpec archive hygiene (in addition to heavy CI)

Heavy CI green is not enough if the PR leaves OpenSpec lifecycle drift:

- **Archive in the PR (#1623):** a PR that adds or edits an active change must
  archive it before merge (`cd docs && openspec archive <name> --yes`), unless
  the change sets `multi_pr: true`. Another change's closed-issue leftover only
  warns on a PR; a version-tag Main Health run fails on it (#1622, #2001).
- A PR that touches an active change must have implementation task groups
  **3–8** checked in that change’s `tasks.md` (groups 1–2 and 9–12 may stay
  open until after merge), unless the PR carries `sdlc-exception`.

Mechanical gate (Process Checks / preflight):

```bash
python3 workspace/sdlc/check-openspec-archive-hygiene.py origin/main
# inventory-only (closed-issue leftovers):
python3 workspace/sdlc/check-openspec-archive-hygiene.py --inventory-only
```

Agents must verify OpenSpec archive hygiene and task completion (or run that
checker) before merge — same bar as heavy CI. See issue #1252 / CU76.

## Board Status / label coherence (in addition to heavy CI)

Before merge/close and during board hygiene, sync **Issue state**, Delivery
Board **Status** (Todo / In Progress / Done), and workflow labels
(`in-progress`, `blocked`). Project has **no Blocked Status** — use the
`blocked` label. CLOSED leftovers must not stay In Progress; Done work should
be CLOSED without WIP labels. Do not invent Owner product scope.

```bash
python3 workspace/sdlc/check-board-status-label-coherence.py --scope pr
python3 workspace/sdlc/check-board-status-label-coherence.py --scope wip
```

Process Checks / preflight run `--scope pr` (skips clearly when `gh` auth or
project access is missing). See issue #1550 / CU76 and
[`docs/github/README.md`](../../github/README.md).

## Verify before merge

```bash
# REQUIRED — agents must run this before `gh pr merge`.
# Do not merge on subscription “all checks success” alone.
bash workspace/sdlc/check-heavy-ci.sh <pr-number>
# Also confirm OpenSpec archive / task-completion hygiene:
python3 workspace/sdlc/check-openspec-archive-hygiene.py origin/main
# Board Status / label coherence for PR-linked issues:
python3 workspace/sdlc/check-board-status-label-coherence.py --scope pr

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

## Stale PR diagnosis — sync `main` before inventing product fixes

If **Integration** and/or **Playwright** fail with Budget/person /
`undefined, undefined` symptoms (budget pickers showing missing client
names) and the branch is **behind `main`**, **merge `origin/main` first**.
On a **pushed** branch: merge, never rebase or force-push (steward rule).

Do not invent product fixes for a class of failures already fixed on `main`.
[#1132](https://github.com/matiaspakua/notaire/pull/1132) nested
`BudgetResponse.person` as `PersonRef` with `personId` **and** `name` /
`lastName`. A nested `person: { personId }` alone is not enough for UI E2E.

Checklist when Integration/Playwright go red on a long-lived tip:

1. `git fetch origin main && git merge-base --is-ancestor origin/main HEAD`
   (if not ancestor → `git merge origin/main`; never rebase a pushed branch).
2. Re-run heavy CI; only then debug product code.

## CHANGELOG `[Unreleased]` fence (#1693 / Release Please)

Only **one** open PR may edit `CHANGELOG.md` `[Unreleased]`. While a release-cut
or Release Please PR holds that section, other PRs omit Unreleased entries or
wait. Never commit on `release-please--branches--*`. Details:
[`FLEET-LESSONS-HEX-1693.md`](FLEET-LESSONS-HEX-1693.md).

## Sync after each land

After a squash lands on `main`, every open tip that still needs work must
**merge `origin/main`** before the next push. Do not start the next hex/R2
domain PR until the in-flight one merges (serialize domains + heavy CI).

## Process Checks before ready

Before `gh pr ready` / merge, Process Checks and OpenSpec archive hygiene must
be green (Spotless, Conventional Commits, archive groups 3–8, INVENTORY regen
when test/architecture inventory drifted). After Java **package moves**, update
english-locals / `docs/tests` path refs in the same PR (#1894 / #1907). Light
advisory checks are **not** merge authority — see
[`FLEET-LESSONS-HEX-1693.md`](FLEET-LESSONS-HEX-1693.md).

**Cost:** prefer one heavy-CI PR and domain-sized slices; docs-only tips should
rely on path-scoped skips (#1257) instead of re-running full Bruno/Playwright
locally when CI already skips them.

## Context

Observed 2026-10-02 on PRs around #1126 / #1128 / #1132 / #1136 / #1137:
premature merge was avoided after light CI (~12–18 checks) or a CI
subscription “all N checks success” while backend CI and Playwright were still
pending; stale tips looked like product bugs until updated from `main` (merge, not rebase) to pick up #1132. Applies
to all future autonomous merges.

## Runner contention — serialize heavy CI

Many open PR tips each trigger a full `CI - Build, Test & Security` + Playwright
suite. Contending tips sit `queued` for minutes; light jobs finish first and
produce **light-only subscription false greens** (“all N checks success” while
heavy workflows are still pending). That is **not** mergeable — always run
`bash workspace/sdlc/check-heavy-ci.sh <pr>` before merge.

**Serialize:** prefer **one heavy-CI PR at a time**.

| Priority | Do |
|----------|----|
| In-flight product / hotfix PR waiting on Integration or Playwright | Let its heavy suite finish (or fail) before pushing more tips that enqueue another full suite |
| Docs / stale-tip / low-priority PRs | Wait — do not push commits that re-trigger Playwright while a higher-priority tip is queued |
| New product work | Do **not** open a new product PR until the in-flight heavy suite finishes |
| Dependabot / bulk dependency PRs | Convert to draft while a feature PR’s heavy suite is queued or running |

Agents with read-only `gh` cannot `gh run cancel` superseded workflows (HTTP 403),
so avoid creating the queue in the first place.

**Concurrency (same ref):** `.github/workflows/ci.yml` and
`playwright-e2e.yml` set `cancel-in-progress: true` so a newer push on the same
PR branch cancels superseded heavy runs. `deploy-github-page.yml` keeps
`cancel-in-progress: false` so a pages deploy is not aborted mid-flight.
(Issue #1148 / CU76.)

**Dependabot floods:** a batch of Dependabot PRs can enqueue many Playwright
suites and starve feature tips. Convert those Dependabot PRs to **draft** so
new runs stop competing (agents often cannot cancel in-flight runs — HTTP 403).
Re-ready them only after the product PR’s heavy gate is green or merged.
(Observed #1139–#1144 vs #1137/#1138, 2026-10-02.)

## Docker image vs heavy tests (#1859)

`check-heavy-ci.sh` does **not** require Build Docker Image. Unit/Integration/Coverage/
Bruno/Playwright can be green while Docker fails on a transient Maven Central resolve
of an otherwise valid Boot parent (cached after `go-offline || true`). Before changing
the Boot pin in `pom.xml`, confirm the artifact exists on Central and that `main`’s
Docker job recently passed the same version — then fix `backend-api/Dockerfile` with
`-U` (see [`FLEET-LESSONS-HEX-1693.md`](FLEET-LESSONS-HEX-1693.md) — Docker Build Image).
Required `protect-main` aggregators may still need Docker/CI green to merge.

## Related: CodeQL advanced vs default setup

Adding `.github/workflows/codeql.yml` while GitHub Code Scanning **default
setup** is enabled causes SARIF rejection / Analyze failures. Ops notes and
`security/enable-gh-secure.sh`:
[DevSecOps — CodeQL](../../200-architecture/208-devsecops/README.md#codeql-advanced-vs-default-setup).
