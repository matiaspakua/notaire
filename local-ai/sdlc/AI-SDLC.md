# Local AI SDLC — foreman/worker harness

How Notaire issues get solved end-to-end by the **local worker** (Codex CLI +
Qwen3.5-9B on oMLX), supervised by a **foreman** (Claude Code, or a human).
The process itself is `CONSTITUTION.md`; this harness is one way of executing
it. It adds nothing on top of the Constitution, it just splits it into phases a
9B model can manage and checks each one mechanically.

## Why a harness

A 9B local model with a 64K window cannot hold the Constitution, the rules and
a codebase in its head at once, and it tends to report success it has not
earned. So:

1. **One phase per worker run.** Each `codex exec` gets a short brief
   (`WORKER.md`, a summary of the Constitution) plus one phase prompt
   (`prompts/*.md`). Fresh context every phase, so nothing gets lost to compaction.
2. **Deterministic gates.** After each phase `foreman.sh` checks the result
   with real commands (`openspec validate`, `validate-sdlc-plan.sh`, the test
   command, `preflight.sh`, `run_pipeline.sh`, `gh pr checks`). The model's own
   claims are never trusted.
3. **Feedback loop.** If a gate fails, the same phase runs again with the gate
   output attached (`retry.md`), up to `MAX_ATTEMPTS`. If it still fails, the
   harness stops and the foreman steps in.
4. **Merges need the foreman.** The harness stops at `review`. The foreman
   reads the diff and runs `foreman.sh <n> merge`, which merges the PR, waits
   for CI and CD on `main`, runs the smoke test and closes the issue (Gate 5).

## Phases → Constitution

| Phase | Who | Constitution step | Gate (deterministic) |
|---|---|---|---|
| `triage` | worker | 1 Issue, 2 Refine | `triage.env` schema + cross-checks; every criterion DONE/TODO with a test (or `command`) as proof |
| `setup` | harness | 6 Branch, IN PROGRESS | branch from fresh `origin/main`, `openspec new change`, `in-progress` label |
| `spec` | worker | 3 Spec, 4 Impact, 5 Arch, 7 AC | `openspec validate --strict` + `validate-sdlc-plan.sh` → **Gate 1** |
| `tests` | worker | 8–10 test design, TDD | tests committed, `TEST_CMD` must **fail** → **Gate 2** |
| `implement` | worker | 11–13 | `TEST_CMD` green + full suite green + `Closes #n` |
| `docs` | worker | 16 permanent docs | CHANGELOG for feat/fix, SDLC plan still valid |
| `quality` | harness→worker | 13–14, lint | `scripts/preflight.sh` |
| `pipeline` | harness→worker | 14–15, 17.5 | `scripts/run_pipeline.sh` (stack + E2E + Bruno) → **Gate 3** |
| `pr` | worker | 18 | PR title `[#n] type(scope): …`, body references issue, branch pushed |
| `ci` | harness→worker | 19 | check runs on the last commit without `[skip ci]` settle green; failed-job logs go to the worker |
| `review` | **foreman** | 20 | human/Claude review of the diff → **Gate 4**; findings go back via `fix` |
| `merge` | harness | 21–24 | CI + CD green on merge commit, `/actuator/health` UP, issue closed → **Gate 5** |

On every gate the branch must be clean and every commit subject must be
Conventional Commits.

## Usage

```bash
# one-time: the local stack
bash local-ai/setup-omlx-codex.sh           # oMLX + model + codex profile (full access)
git worktree add --detach ../notaire-localai origin/main
ln -s "$PWD/.env" ../notaire-localai/.env   # never copy .env
(cd ../notaire-localai/frontend && npm ci)  # deps before the agent
(cd ../notaire-localai && bash scripts/install-git-hooks.sh)

# per issue
local-ai/sdlc/foreman.sh 1069                     # runs triage … ci, stops at review
STOP_AFTER=spec local-ai/sdlc/foreman.sh 1069     # pause after a phase to inspect
local-ai/sdlc/foreman.sh 1069 tests               # re-run from a phase
local-ai/sdlc/foreman.sh 1069 fix                 # worker applies $RUNS/1069/gate4.md, then ci + review
local-ai/sdlc/foreman.sh 1069 merge               # after review: merge + Gate 5
```

The worker runs in a **dedicated git worktree** (`../notaire-localai`) so it
never touches the maintainer's working copy. Its Docker stack uses
`COMPOSE_PROJECT_NAME=notaire-localai`.

Two directories per issue, so the worker can never damage harness evidence:

| Where | Owner | Content |
|---|---|---|
| `../notaire-localai-runs/<n>/` (`RUNS`) | harness | `issue.md` snapshot, normalized `triage.env` (adds `SURFACE`, `BRANCH`, `CHANGE`), `prompt-<phase>.md`, `worker-<phase>.log`, `last-<phase>.md`, `io-<phase>/` snapshots, `gate-<name>.out`, `gates.log`, `scope.out`, `phases.done`, `pr.number` |
| `../notaire-localai/.localai/<n>/` (`IO`, git-excluded) | worker | skeletons it fills: `triage.env`, `triage.md`; later `tests.env`, `pr-body.md`, `BLOCKED.md` |

## Guardrails (enforced after every worker run)

| Guard | What it does |
|---|---|
| Scope guard | paths outside the phase's allowed regex are reverted; worker is told in the retry |
| Ref guard | branch switches, new local branches, and commits in no-commit phases (triage) are reverted |
| Git hooks (at commit time) | the worker's git runs with `core.hooksPath=RUNS/<n>/githooks` (injected via `GIT_CONFIG_*`, which Codex would otherwise strip). `pre-commit` rejects forbidden files (build output, logs, `.localai/`, `.env`, backups) and files outside the phase scope; `pre-push` refuses unless the phase is `pr`; `reference-transaction` refuses any ref update except fast-forwards of the issue branch from `BASE` (no branch switch, no new branch, no rewriting earlier phases) |
| Ref guard (after the run) | backstop for the hooks: reverts HEAD switches, deletes new branches, mixed-resets commits that rewrite history or add forbidden paths |
| Frozen TEST_CMD | when Gate 2 passes, `tests.env` is copied to `RUNS/<n>/tests.env`; later gates and prompts read that copy, so the worker cannot change the red-proven command and a wiped `.localai/` cannot lose it |
| Edit tool | `bin/edit.py` (copied to `RUNS/<n>/edit.py`): exact-once OLD/NEW search-replace. Codex `apply_patch` fails with the local model; `sed` line edits are not idempotent and a small model stacks them; `cat >` rewrites drop unread parts. It refuses stray marker lines and ambiguous matches |
| Diff hygiene in `gate_green` | `git diff --check` (conflict markers, whitespace) and a collateral-deletion check (a non-test file losing >10 lines and >25% of its content) |
| Phase base | guards measure from the phase start, not the attempt start, so a retry may amend the phase's own commits; the CI fix loop re-bases after every push so pushed commits are never rewritten |
| Full suite is harness-only | the worker runs single test classes; `gate_green` runs the full suite. A 9B model does not wait for a 6-minute Maven run: it reruns it in parallel and corrupts `target/` |
| Ledger is harness-owned | `bin/ledger.py`. After Gate 2, `tasks.md` may change only by `[ ]`→`[x]` (`ticks-only`, checked in `gate_green`/`gate_docs`); the worker kept rewriting it with invented results ("PR created", "CI green") or rewording items instead of ticking them. After the docs phase the foreman writes the Commits and CHANGELOG rows of `traceability.md` (`record_ledger`); the worker copied SHAs from `main` |
| Harness pushes | `push_branch`/`record_pr` in `phase_pr`; `githooks/pre-push` denies every worker push. The local model marked `git push` as needing sandbox escalation (refused under approval=never) and looped: sed-edits of the ledger, duplicate commits, `git reset` to the remote, `pkill -9 git`, `--no-verify`, `-f`. Earlier, the repo's 10-minute pre-push preflight made it start a second push; two Maven runs on one `target/` failed 452 tests. The harness rebases over CI-bot report commits, pushes with `PREFLIGHT_SKIP=1` (quality already ran preflight; CI re-runs it), and records the PR number and ticks 10.1–10.2 itself. The worker only writes the PR body and runs `gh pr create/edit` |
| Foreman backups are hidden | Manual foreman backups of worker commits go to `refs/foreman/backup/<issue>-<phase>`, never `refs/heads/`: the worker lists branches and checked out a `backup/` branch to "recover" discarded commits. `push_branch` refuses a detached HEAD (it would push the stale branch ref), and `ALLOW_COMMIT=0` makes `githooks/pre-commit` deny commits in the pr phase |
| CI judged on the last real commit | CI bots push `docs: add PR validation report … [skip ci]` onto the PR branch. That commit has no check runs, so `gh pr checks` (which reads the PR head) reported failure and started a needless ci-fix worker. `ci_sha`/`ci_state`/`wait_ci` read `check-runs` of the last commit whose message lacks `[skip ci]`; the merge gate uses the same test |
| Gate 4 feedback channel | Once a PR is open, re-running a phase to apply review notes is wrong: `pr` forbids commits, `docs` re-runs quality and pipeline. `foreman.sh <n> fix` feeds `$STATE/gate4.md` to the worker (`prompts/10-review.md`, commits allowed, pushes not), then the harness pushes and re-runs `ci` and `review`. Notes that work: exact OLD/NEW blocks plus CHECK/EXPECTED lines; "copy the gates.log lines" failed because the log holds every retry |
| Failed rebase is aborted | A conflicting `pull --rebase` in `push_branch` left `rebase-merge/` behind, and every later push failed. `push_branch` now runs `git rebase --abort` before stopping |
| Gate 5 follows workflow_run | `cd.yml` is triggered by `workflow_run` when CI finishes, so it runs on main's head at that moment — the CI bot's `[skip ci]` report commit, not the merge commit. `--commit <merge sha>` found nothing (#1069 logged a false `MISSING`). `main_run` takes the newest main run whose head descends from the merge commit |
| Gates | deterministic, with actionable messages (exact line, exact fix). Cosmetic noise (backticks, dash variants) is normalized, not failed |
| Cross-checks | `TYPE` must equal the issue title prefix; `DB_CHANGE=yes` needs a new `V*__.sql`; criteria come only from the issue's checklist |

## Foreman duties

- **Choose the issue order.** Start with small, well-bounded issues, and don't
  hand the worker epics or roadmap umbrellas (`roadmap`, `phase:*`). Split
  those into child issues first.
- **Review at every stop.** Read `worker-<phase>.log` and the diff. If the
  worker is off track, fix the prompt or the brief. Improving the harness
  beats patching one branch.
- **Code review (Gate 4).** Check correctness, fit with the Constitution, no
  scope creep, and that the tests really prove the behaviour. The code owner
  merging counts as approval (Constitution §5 step 20).
- **Send review notes back.** When a phase passes its gate but is wrong
  (scope creep, wrong names), write `RUNS/<n>/review-<phase>.md`, reset the
  branch to before the phase, drop the phase from `phases.done` and rerun it.
  `render` appends the notes as "Foreman review — fix ALL of these". Keep them
  concrete: exact lines, exact names, what to delete.
- **Never edit `foreman.sh` while it runs.** Bash reads scripts lazily; an edit
  mid-run can make it execute garbage.
- **Tasks.md ticks.** In implement the worker ticks only groups 4-5, as a pure
  `[ ]`→`[x]` change; the foreman ticks later groups after its own gates run
  them. The model otherwise writes results it never produced ("1,940 tests pass").
- **Harness bugs look like worker bugs.** Before sending a retry, reproduce the
  failing gate by hand. `RECHECK=1 ./foreman.sh <n> <phase>` re-runs a phase's
  gate without calling the worker. Never use `| grep -q` in the harness:
  under `pipefail` it gives SIGPIPE false negatives (use `has`).
- **Check commit messages against the diff.** The worker writes plausible
  bodies that claim changes it never made. Gate 4 compares every body line
  with `git show --stat`.
- **Record lessons.** Recurring worker mistakes go into `WORKER.md` or the
  phase prompts, so the next issue benefits.

## Tooling requirements checked

| Need | How |
|---|---|
| Filesystem, `~/.m2`, `~/.npm`, `.git` of worktree | codex profile `sandbox_mode = "danger-full-access"` (workspace-write blocked all of these) |
| GitHub | `gh` CLI, keychain auth |
| Docker | Docker Desktop socket |
| Non-interactive codex | `codex exec … </dev/null` (without it, exec waits on stdin forever) |
| Timeouts on macOS | `perl -e 'alarm …; exec …'` (no coreutils `timeout`) |
