# AI Agent Development Workflow

Applies to every AI coding agent (Claude Code, OpenCode, Copilot, Codex and the
local-AI worker). **[`CONSTITUTION.md`](../../CONSTITUTION.md) §5 is the
workflow**; this file is only the operational cheat-sheet for it. Where they
differ, the Constitution wins. Roles (Owner, Foreman, Worker) are defined in
CONSTITUTION §10.

**Principles:** KISS, bounded Boy Scout, and self-improve while delivering — see
CONSTITUTION §2 (P2, P11, P12). Feed friction into gates/skills/Issues; do not leave
lessons only in chat.

## The steps and their commands

| Constitution step | What to do | Command / artifact |
|---|---|---|
| 1–2 Issue + Use Case | Find or create the Issue; it must name a Use Case (`CU-XX`) from `docs/100-business/`. No Use Case → write it first | `gh issue list --search "<task>"`; `gh issue create` with `.github/ISSUE_TEMPLATE/issue.md`; labels from `gh label list` |
| 3–5, 7 Specification (**Gate 1**) | OpenSpec change: proposal, traceability, specs, design, tasks | `bash workspace/sdlc/seed-openspec-change.sh <name> --issue N --use-case "…" --branch <b> --create`, then fill it in one step with `python3 workspace/sdlc/scaffold-openspec-change.py scaffold <spec.json>` (after the suites pass, `… complete <name> --test <TestName>` ticks groups 1-8); `openspec validate <name> --strict`; `bash workspace/sdlc/validate-sdlc-plan.sh <name>` |
| 6 Branch | From updated `main`; move the Issue to in progress | `git checkout main && git pull origin main`; `git checkout -b <type>/<issue>_<desc>`; `gh issue edit <n> --add-label in-progress` |
| 8–10 Failing tests first (**Gate 2**) | Write tests, run them, watch them fail; commit them before the production code | `mvn test -pl backend-api -Dtest=<NewTest>` (must fail) |
| 11–12 Implement | Minimum code to pass; update affected tests without weakening them | — |
| 13–15 Test | Unit + integration + coverage, Bruno API, Playwright for any UI change | `mvn verify -pl backend-api`; `bash testing/scripts/test.sh`; `cd testing/e2e && npx playwright test` |
| 16 Docs (**Gate 3**) | Update permanent docs; move outdated ones to `docs/000-archive/` | CONSTITUTION §8 |
| 17 Atomic commits | One logical change per commit, Conventional Commits. Only the commit that completes the Issue carries `Closes #<n>`; the others carry `Refs #<n>` | `.claude/rules/general.md` rule 6.1 |
| 17.5 Pipeline | Must exit 0 before the PR. Includes the security case catalog | `bash workspace/sdlc/run_pipeline.sh` (`python3 security/run_gates.py --profile local`) |
| 18 PR | Title `[#<n>] <type>: <description>`, body from `.github/PULL_REQUEST_TEMPLATE.md` | `gh pr create`; then `gh pr view <n> --json mergeable,mergeStateStatus` must be `MERGEABLE` |
| 19 Local full preflight | A PR is ready when `preflight.sh --full` has passed. GitHub does not re-run that suite on the pull request (#2001) | `bash workspace/sdlc/preflight.sh --full` |
| 20–21 Review + merge (**Gate 4**) | Foreman merge rule (CONSTITUTION §5 step 21): the Foreman merges a Worker PR when local `--full` passed and `check-heavy-ci.sh` exits 0; the Owner merges Constitution, `sdlc-exception` and harness PRs; Workers never merge | `bash workspace/sdlc/check-heavy-ci.sh <pr>` exit 0, then `gh pr merge` |
| 18 Archive in the PR | Archive the implemented OpenSpec change before merge (`multi_pr: true` if later PRs continue it); the checker fails otherwise. Then `markdownlint-cli2 --fix` on touched `docs/openspec/specs/<cap>/spec.md` (#1689) | `cd docs && openspec archive <change> --yes` |
| 22–24 Deploy, smoke test, close (**Gate 5**) | The Issue closes after merge, deploy and smoke test — not when the PR is opened | `cd.yml` |

## Continuous publication (mandatory, #1681)

Applies to every agent at every moment. GitHub must always hold the current state, so a sudden stop
loses nothing and another person or agent resumes from the Issue.

1. Created a branch → push it **immediately**: `git push -u origin <branch>`.
2. After **each group of atomic commits** (spec, failing tests, implementation, docs) → push **and**
   comment on the Issue: `bash workspace/sdlc/publish-progress.sh <issue> "<stage; what remains>"`.
   It pushes, checks `git ls-remote` equals `HEAD`, and posts branch, last commit, stage and the
   resume command. No comment is posted when the push is rejected.
3. A stage that only exists locally is not done. If the pre-push preflight rejects a push, report it on
   the Issue and fix the cause; never skip the hook (`PREFLIGHT_SKIP`, `--no-verify`) without the
   Owner's approval.

## Before pushing

Keep the branch conflict-free with `main`: `git fetch origin && git merge
origin/main`, re-run the affected suites, then `git push -u origin <branch>`.
Then confirm the push landed: `git ls-remote --heads origin <branch>`. Wrapped
or filtered output (RTK) can print `ok` while the pre-push preflight rejected it.
If an open PR shows `CONFLICTING`/`DIRTY`, fix it before starting other work.

## Decisions, deviations, corrections and risk

Adopted from the cross-repo assessment (2026-11, `docs/300-development/306-ai-sdlc-framework/CROSS-ASSESSMENT-2026-11.md`).

**Decision packet.** When you need the Owner, stop and send one packet, not a chat thread:

```text
Decision needed: <one line, plain words>
Context: <what is true now, with the evidence: command, file:line, issue>
Options:
  A. <option> — consequence: <what it changes, costs, risks>
  B. <option> — consequence: <…>
Recommendation (proposal): <A or B, and why>
```

At most three related questions per message. Ask only when the answer changes scope, a
contract, data, permissions, cost, an accepted risk or delivery; resolve a routine detail inside
the task (a local name, an error type) with a sensible default and record it in `tasks.md`.

**Deviations from the approved OpenSpec change.** Record each one in the change's `tasks.md`:

- *Mechanical* — it only applies what the proposal, design, tests or a recorded Owner decision
  already say. Record it with what you checked: "does not change scope/contract/data/permissions",
  "applies convention X (source)", or "executes decision Y (link)".
- *Decision* — it changes the objective, scope, an API or DB contract, data, permissions or an
  accepted risk. Stop with a decision packet **before** the commit; update the change after the
  answer (`openspec-update-change`). Missing information never makes a deviation mechanical.

**Write set.** The proposal's `### Write set` lists the paths the change may touch (globs in
backticks). `aisdlc scope` compares the PR diff with it; a path outside it is either a planning
gap (update the proposal) or scope creep (take it out).

**Bounded corrections.** A Foreman review or a failing gate gets one correction batch covering
every blocker, then one re-check limited to those blockers. A second batch only when the same
blocker still fails; after that, stop with a decision packet (or `BLOCKED`), never a third blind
attempt. Reproduce a blocker on `main` first: behavior the change did not touch is an advisory,
not a blocker of this PR.

**Tests: RED per rule, PRESERVE per touched behavior.** One failing test per acceptance scenario
(Gate 2), plus one test per existing behavior the change touches (shares the changed code) that
proves it still holds. No padding tests that assert nothing new.

**Risk line.** Every PR body ends with `Risk: <item> (<reason>)` or `Risk: none`. High-risk items:
changing or deleting stored data (Flyway on existing rows), changing a contract others consume
(REST shape, OpenAPI, events), authentication/authorization, and the merge path itself. Before
writing `Risk: none`, check whether the diff changes code that behavior you were not asked to
change also uses.

## Checked mechanically

These run in `workspace/sdlc/preflight.sh` on every pull request and in `sdlc-process.yml` on a version tag:

- every commit subject is a Conventional Commit;
- a PR that changes production code also changes tests;
- a PR without an OpenSpec change carries the Owner's `sdlc-exception` label;
- the always-loaded rule files are non-empty and reference paths that exist.

The kit stages add (`python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml gate <stage>`):

- `pre-pr` (advisory): `aisdlc scope` (write set) and `aisdlc size` (≤400 counted lines);
- `merge`: `aisdlc protected` — a PR touching an owner-only path goes to the Owner — and
  `aisdlc evidence verify` (hash-chained ledger).

`validate-sdlc-plan.sh` (in `pr-validation.yml`) checks every OpenSpec change.

## Fleet lessons (#1693)

For multi-agent / hex cadence (heavy-CI serialize, merge-main-never-rebase, CHANGELOG
Unreleased fence, multi_pr CHANGELOG hygiene (#2033), Mac vs CI, R2 one-domain slices, path hygiene after
`adapter.out.persistence` moves, human paste templates on 403, fleet/cost serialize,
no probe Issues):
`docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` and
`docs/300-development/304-ai-sdlc-cloud/MAC-LOCAL-VS-CI.md`. Steward checklist:
`.claude/skills/steward/SKILL.md`.

## Never

- ❌ Change code without an Issue, a Use Case and an OpenSpec change
- ❌ Write production code before its failing test
- ❌ Commit to or push to `main`; merge your own PR (Workers) or merge outside the Foreman merge rule (Foreman)
- ❌ Commit failing tests, `@Disabled` tests without approval, or secrets
- ❌ Leave dead code, duplicate code or outdated docs
- ❌ Open probe Issues to test permissions, or start the next hex domain before the current PR merges
- ❌ Open a PR before `preflight.sh --full` passes — GitHub will not re-run the suite
- ❌ Claim Mac local green equals CI green, or move packages without updating `docs/tests` path refs
- ❌ Open parallel heavy-CI PRs when one tip is still queued (fleet cost)
- ❌ Put `closes #N` (or markdown `#N` links for open multi_pr epics) in CHANGELOG `[Unreleased]` — Release Please will close the issue early (#2033)

## Exceptions

Only with explicit Owner approval (CONSTITUTION §12): emergency security
hotfixes, one-time migration scripts, trivial documentation typo fixes. Document
the exception in the commit message and the PR. The Owner marks the PR with the
`sdlc-exception` label; a PR without an OpenSpec change fails
`sdlc-process.yml` unless it carries that label.
