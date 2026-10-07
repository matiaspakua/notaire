# AI Agent Development Workflow

Applies to every AI coding agent (Claude Code, OpenCode, Copilot, Codex and the
local-AI worker). **[`CONSTITUTION.md`](../../CONSTITUTION.md) §5 is the
workflow**; this file is only the operational cheat-sheet for it. Where they
differ, the Constitution wins. Roles (Owner, Foreman, Worker) are defined in
CONSTITUTION §10.

## The steps and their commands

| Constitution step | What to do | Command / artifact |
|---|---|---|
| 1–2 Issue + Use Case | Find or create the Issue; it must name a Use Case (`CU-XX`) from `docs/100-business/`. No Use Case → write it first | `gh issue list --search "<task>"`; `gh issue create` with `.github/ISSUE_TEMPLATE/issue.md`; labels from `gh label list` |
| 3–5, 7 Specification (**Gate 1**) | OpenSpec change: proposal, traceability, specs, design, tasks | `openspec new change "<name>"`; `openspec validate <name> --strict`; `bash workspace/sdlc/validate-sdlc-plan.sh <name>` |
| 6 Branch | From updated `main`; move the Issue to in progress | `git checkout main && git pull origin main`; `git checkout -b <type>/<issue>_<desc>`; `gh issue edit <n> --add-label in-progress` |
| 8–10 Failing tests first (**Gate 2**) | Write tests, run them, watch them fail; commit them before the production code | `mvn test -pl backend-api -Dtest=<NewTest>` (must fail) |
| 11–12 Implement | Minimum code to pass; update affected tests without weakening them | — |
| 13–15 Test | Unit + integration + coverage, Bruno API, Playwright for any UI change | `mvn verify -pl backend-api`; `bash testing/scripts/test.sh`; `cd testing/e2e && npx playwright test` |
| 16 Docs (**Gate 3**) | Update permanent docs; move outdated ones to `docs/000-archive/` | CONSTITUTION §8 |
| 17 Atomic commits | One logical change per commit, Conventional Commits. Only the commit that completes the Issue carries `Closes #<n>`; the others carry `Refs #<n>` | `.claude/rules/general.md` rule 6.1 |
| 17.5 Pipeline | Must exit 0 before the PR | `bash workspace/sdlc/run_pipeline.sh` |
| 18 PR | Title `[#<n>] <type>: <description>`, body from `.github/PULL_REQUEST_TEMPLATE.md` | `gh pr create`; then `gh pr view <n> --json mergeable,mergeStateStatus` must be `MERGEABLE` |
| 19 CI green | Fix red CI before asking for review | `bash workspace/sdlc/preflight.sh` mirrors CI locally |
| 20–21 Review + merge (**Gate 4**) | The Owner reviews and merges; agents never merge on their own authority | — |
| 22–24 Deploy, smoke test, close (**Gate 5**) | The Issue closes after merge, deploy and smoke test — not when the PR is opened | `cd.yml` |

## Before pushing

Keep the branch conflict-free with `main`: `git fetch origin && git merge
origin/main`, re-run the affected suites, then `git push -u origin <branch>`.
If an open PR shows `CONFLICTING`/`DIRTY`, fix it before starting other work.

## Checked mechanically

These run in `sdlc-process.yml` on every PR and in `workspace/sdlc/preflight.sh`:

- every commit subject is a Conventional Commit;
- a PR that changes production code also changes tests;
- a PR without an OpenSpec change carries the Owner's `sdlc-exception` label;
- the always-loaded rule files are non-empty and reference paths that exist.

`validate-sdlc-plan.sh` (in `pr-validation.yml`) checks every OpenSpec change.

## Never

- ❌ Change code without an Issue, a Use Case and an OpenSpec change
- ❌ Write production code before its failing test
- ❌ Commit to or push to `main`; merge your own PR
- ❌ Commit failing tests, `@Disabled` tests without approval, or secrets
- ❌ Leave dead code, duplicate code or outdated docs

## Exceptions

Only with explicit Owner approval (CONSTITUTION §12): emergency security
hotfixes, one-time migration scripts, trivial documentation typo fixes. Document
the exception in the commit message and the PR. The Owner marks the PR with the
`sdlc-exception` label; a PR without an OpenSpec change fails
`sdlc-process.yml` unless it carries that label.
