# CI Preflight — the pull-request gate

## The problem this solves

GitHub Actions does not re-run product CI on a pull request or on push to `main`
(#2001). Those workflows run on a version tag (`v*` / `notaire-v*`) or on
`workflow_dispatch`. This script is the gate. A run **without `--full` is not
sufficient to open a pull request**. There is no GitHub safety net for the
suites `--full` adds.

The release workflows are:

| Workflow | Gates |
|---|---|
| `ci.yml` | Build & Compile, Unit Tests, Integration Tests, Coverage Gate, Security Scan (Trivy fs, report-only), Docker Build (image build + Trivy image scan, report-only), Code Quality (SpotBugs, report-only) |
| `codeql.yml` | CodeQL analysis for Java, JavaScript/TypeScript, and GitHub Actions (GitHub-hosted only; uploads SARIF to code scanning; findings do not fail the job) |
| `pr-validation.yml` | Validate PR, Quick Build, Dependency Analysis (report-only), **Code Lint (Checkstyle + Spotless)**, Branch Naming |
| `frontend-ci.yml` | TypeScript Check, **ESLint (blocking, #1048)**, Unit Tests (Vitest), Build (Next.js) |
| `playwright-e2e.yml` | Build Backend/Frontend, API Tests (Bruno), UI E2E Tests (Playwright: the suite lives in `testing/e2e`; type-check and ESLint run first, before the stack starts) |
| `sdlc-process.yml` | Commit messages, TDD evidence, `sdlc-exception` label, agent rule files, process-script self-tests |

**Not all of these are reachable from the commands developers normally run.** The
clearest example, and the one that motivated this tooling: Spotless is
deliberately *not* bound to any Maven lifecycle phase in `backend-api/pom.xml`
(see #705 — binding it breaks the shallow-checkout jobs, because `ratchetFrom`
needs full git history). It runs only from `pr-validation.yml`'s lint job. So:

```bash
mvn verify -pl backend-api   # ✅ green locally
git push                     # ❌ "Code Lint" fails in CI
```

That happened on PR #721. `workspace/sdlc/preflight.sh --full` is now the only run of those gates before a pull request.

## Usage

Install once per clone:

```bash
bash workspace/sdlc/install-git-hooks.sh
```

That sets `core.hooksPath=.githooks`, so the hooks are version-controlled, apply
to every worktree, and update on `git pull`. From then on `git push` runs
`preflight.sh --full` and refuses a push that would open an unproven pull request.

Run it by hand any time:

```bash
bash workspace/sdlc/preflight.sh            # NOT sufficient to open a pull request
bash workspace/sdlc/preflight.sh --fix      # auto-fix what is fixable, then verify
bash workspace/sdlc/preflight.sh --fast     # format/lint/compile/typecheck only; NOT sufficient to open a PR
bash workspace/sdlc/preflight.sh --full     # mandatory before push/PR
bash workspace/sdlc/preflight.sh --list     # local check -> release-workflow mapping
```

`--full` needs the stack running (`bash workspace/stack/start.sh`): backend on `:8080`,
frontend on `:3000`. It also runs a local Docker Compose build + smoke test
(skipped with a warning if the Docker daemon isn't running) and the real
Bruno API collection (`backend-api/api-test/`) — not just the legacy
`testing/integration/http/` cURL suite, which `--full` also still runs as an extra local
smoke check with no corresponding CI job.

`--full` also runs the database V&V suite (`bash testing/scripts/run.sh database`, Docker only, no
running stack needed), the local mirror of `.github/workflows/database-vv.yml`.

`bash workspace/sdlc/preflight.sh` (without `--fast`) also runs three report-only
checks that mirror advisory CI jobs: `mvn dependency:analyze`, SpotBugs, and
a Trivy filesystem scan (skipped with a warning if `trivy` isn't installed
locally). None of these block the push — they match the report-only
(`|| true` / `exit-code: '0'`) severity of their CI counterparts.

CodeQL (`codeql.yml`) is listed by `--list` and skipped locally. It needs the
GitHub-hosted CodeQL bundle and `security-events: write` to upload results, so
a laptop run would not match CI.

## Path filters on a release run (#1257, #2001)

Product workflows no longer run on `pull_request`. Their `Path filter` job still
exists. On a tag or `workflow_dispatch` (any event other than `pull_request`)
every filter output is forced true, so the release run is the full suite. Do
**not** add workflow-level `on.paths`: GitHub does not apply paths to tag pushes.

Historical pull-request behavior, kept so a future `pull_request` trigger would
still classify paths:

| Filter output | Typical paths | Effect when false (and `ci` false) |
|---------------|---------------|-------------------------------------|
| `backend` | `backend-api/**`, root Maven/Docker | Skip Java build/test/coverage leaves |
| `frontend` | `frontend/**` | Skip Vitest / Next build leaves |
| `testing` / `product` | `testing/**` or backend/frontend/ci | Skip Playwright/Bruno stack when `product` false |
| `openapi` | OpenAPI artifact / export script | Skip OpenAPI contract job when unused |
| `docs` | `docs/**`, `CONSTITUTION.md`, `AGENTS.md`, `.claude/**`, `github-page/**` | Alone → all heavy leaves skip |
| `ci` | `.github/workflows/**`, `workspace/sdlc/**`, `workspace/tests/**`, … | Forces full relevant suites |

Suite aggregator names (`CI`, `Frontend CI`, `Playwright E2E`, …) stay so a tag
run can still publish them. They are not required on pull requests. The Owner
drops them from `protect-main` (#1040).

Local `preflight.sh --full` is the pull-request gate. Path filters are not a
license to skip it.

Guards: `python3 workspace/tests/test_ci_workflow_invariants.py`.

### Playwright shards (#1258 / #1197 P0.3)

On product PRs, `e2e-tests` runs a 3-shard matrix (`--shard=i/3`). Blob reports
merge in `e2e-merge-reports`, whose check name stays **`UI E2E Tests (Playwright)`**
so `check-heavy-ci.sh` still matches. The merge job fails if any shard failed.
Docs-only path filters still skip the whole E2E stack (aggregator + heavy-CI
script accept `skipped`).

### What `--fix` fixes automatically

| Problem | Fix applied |
|---|---|
| Java formatting (trailing whitespace, unused imports, tabs, missing EOF newline) | `mvn spotless:apply` (or the fallback below) |
| Auto-fixable ESLint violations | `eslint src --fix` |

Everything else — failing tests, coverage below the ratchet floor, type errors —
is reported for you to fix, never silently patched.

## Bypassing (deliberately)

```bash
PREFLIGHT_SKIP=1 git push …    # skip the gates for one push (WIP spike, etc.)
PREFLIGHT_FAST=1 git push …    # format/lint/compile/typecheck only
git push --no-verify …         # git's built-in hook bypass
```

A bypass is for a documented, known reason (name it in the PR description), never to get past
a failure you have not diagnosed. On a clean `main` the gates must pass without one (#1185).

## The git-worktree caveat

Spotless resolves the repository through JGit, which walks parent directories
looking for a `.git` **directory**. Inside a git worktree, `.git` is a **file**
holding a `gitdir:` pointer, so the plugin aborts with:

```text
Cannot find git repository in any parent directory
```

CI checks out normally and is unaffected, but this makes the format gate
unrunnable from a worktree — precisely where you'd want it. `preflight.sh`
detects this and falls back to `backend-api/tools/spotless-fallback.sh`, which implements
the same four rules configured in the pom:

```xml
<removeUnusedImports/> <trimTrailingWhitespace/> <endWithNewline/>
<indent><spaces>true</spaces><spacesPerTab>4</spacesPerTab></indent>
```

against the same file set (`.java` under `backend-api/` differing from
`origin/main` — Spotless ratchets per-file, so a touched file is formatted in
full). It was validated against PR #721 by confirming it flags exactly the two
files CI flagged, with the same diff.

Unused-import removal in the fallback is conservative: an import is dropped only
when its simple name appears nowhere else in the file. Wildcard and static
imports are never touched (`checkstyle.xml` already bans wildcards).

## `run-pipeline.sh` is retired

An earlier, parallel local pipeline script (`run-pipeline.sh`, 11 phases) existed
at the repo root with zero references anywhere else in the repo — not wired into
CLAUDE.md, CONSTITUTION.md, ADR-012, this doc, or the git hooks. `preflight.sh`
is, and always was, the canonical one. Its two genuinely unique phases (Docker
build/smoke test, advisory Trivy filesystem scan) were folded into `preflight.sh`
above; the script itself has been removed.

## `run_pipeline.sh` — the mandatory pre-PR gate

`preflight.sh` mirrors CI gates but assumes the Docker stack is already up and
only prints to the terminal — there's no single artifact proving a branch is
ready. `workspace/sdlc/run_pipeline.sh` is that command: it brings the stack up
itself (`workspace/stack/start.sh`), runs `workspace/sdlc/validate-sdlc-plan.sh`, runs
`preflight.sh --full`, adds a markdown-lint pass (ratcheted against
`origin/main`, same policy as the Spotless gate — see the git-worktree caveat
above) that has no CI job yet, and writes one HTML dashboard plus a log per
run under `reports/pipeline/<timestamp>/` (git-ignored).

```bash
bash workspace/sdlc/run_pipeline.sh
```

It composes rather than duplicates: everything under "what CI checks" above
runs through `preflight.sh --full`, so a new or changed gate in
`.github/workflows/` only needs updating in `preflight.sh` (per the section
below) to be picked up here automatically. This is Gate 4's CI-equivalent
evidence requirement (`CONSTITUTION.md`) and the Step 5 "run all tests"
requirement (`.claude/rules/ai-agent-workflow.md`) — run it before opening a
PR.

## Keeping local and CI in sync

`preflight.sh` mirrors CI by convention, not by magic — nothing enforces that
they agree. **When you add or change a gate in `.github/workflows/`, update
`preflight.sh` in the same PR.** Each check in the script names the CI job it
mirrors, and `--list` prints the mapping, so drift is easy to spot in review.

Checks that CI runs with `|| true` (Checkstyle) are non-blocking here too, so
local severity matches CI severity rather than being stricter.

Scripts that run in the gates must work with both BSD (macOS) and GNU (Linux)
tools. CI runs on Linux and developers on macOS, so a GNU-only idiom passes CI and
fails locally: `sed -i -E ...` reads `-E` as the backup suffix on BSD sed (#1185).
Edit through a temporary file (`sed -E ... f > f.new && mv f.new f`) instead of `sed -i`.

The `sdlc-process.yml` checks run locally against `origin/main..HEAD`. The
`sdlc-exception` label check needs the PR's labels, so preflight reads them
with `gh pr view` and skips that one check while the branch has no PR yet.

## Merge gate

`bash workspace/sdlc/preflight.sh --full` is the proof. GitHub does not re-run
it on the pull request. `check-heavy-ci.sh` exits 0 when those workflows were
not started on the head, and fails if one was started and is pending or red.
See [`304-ai-sdlc-cloud/CI-MERGE-GATE.md`](304-ai-sdlc-cloud/CI-MERGE-GATE.md).

```bash
bash workspace/sdlc/preflight.sh --full
bash workspace/sdlc/check-heavy-ci.sh <pr-number>
```

If product jobs on a tag fail with Budget/person / `undefined, undefined` on a
tip behind `main`, merge `origin/main` into the branch first (#1132). Do not rebase.
