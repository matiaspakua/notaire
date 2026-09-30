# Local AI SDLC — foreman/worker harness

How Notaire issues get solved end-to-end by the **local worker** (Codex CLI +
Qwen3-Coder-30B-A3B on oMLX), supervised by a **foreman** (Claude Code, or a human).
The process itself is `CONSTITUTION.md`; this harness is one way of executing
it. It adds nothing on top of the Constitution, it just splits it into phases a
small local model can manage and checks each one mechanically.

## Why a harness

A local model with a 32K window cannot hold the Constitution, the rules and
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
| `triage` | worker | 1 Issue, 2 Refine | `triage.env` schema + cross-checks; every criterion DONE/TODO with a test (or `command`) as proof. Seeded `ISSUE`/`USE_CASE`/`TYPE` are restored (`triage-repaired REVIEW`); search/print commands are not proofs; `new test` needs `KIND=code` (`bin/triage_check.py`) |
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
local-ai/sdlc/foreman.sh 1069 check               # run the CHECK lines of pending review notes
local-ai/sdlc/foreman.sh 1069 merge               # after review: merge + Gate 5
PROFILE_SPEC=omlx-large local-ai/sdlc/foreman.sh 1069   # a different codex profile for one phase
```

The worker runs in a **dedicated git worktree** (`../notaire-localai`) so it
never touches the maintainer's working copy. Its Docker stack uses
`COMPOSE_PROJECT_NAME=notaire-localai`.

### Project adapter (`.aisdlc/project.yml`)

Every project-specific value the harness uses lives in one file at the repo
root, `.aisdlc/project.yml`; `foreman.sh` holds only the generic phase and gate
machinery. It reads the file through `bin/adapter.py` and stops at start-up if
a required key is missing (`adapter.py validate`). `AISDLC_PROJECT` points it
at another file.

| Key | Used for |
|---|---|
| `paths.worktree`, `paths.runs` | defaults for `WT` and `RUNS` (relative to the repo root) |
| `backend.profile` | default Codex `PROFILE` |
| `spec.schema`, `spec.tasks_template`, `spec.validate`, `spec.plan_check` | spec and docs gates (`{change}` is filled in) |
| `surfaces.<name>.root`, `.test_one`, `.suite` | a change's surfaces, its full suite and the TEST_CMD form |
| `source_roots`, `test_files`, `db_migrations` | phase scopes, test-file detection, the DB-change cross-check |
| `gates.docs_lint`, `gates.docs_lint_fix`, `gates.preflight`, `gates.pipeline`, `gates.start`, `gates.health_url`, `gates.main_workflows` | docs, quality, pipeline and Gate 5 commands |
| `compose_project`, `guards.forbidden` | Docker project name; paths the worker may never commit |

`SURFACE` in `triage.env` is the comma list of surfaces whose `root` holds a
file from triage's Files to Edit (`backend`, `frontend`, `backend,frontend`,
`none`). When no path is under a root and `KIND=code`, it is triage's
`TEST_SURFACE` (pre-filled `backend`): a rule about a docs/data file is proven
by a test that reads the file (#1064). The older value `both` still means every surface. To use the harness
in another repo, write its adapter; the phase prompts still carry Notaire
examples (AUDIT §7).

Two directories per issue, so the worker can never damage harness evidence:

| Where | Owner | Content |
|---|---|---|
| `../notaire-localai-runs/<n>/` (`RUNS`) | harness | `issue.md` snapshot, normalized `triage.env` (adds `SURFACE`, `BRANCH`, `CHANGE`), `prompt-<phase>.md`, `worker-<phase>.log`, `last-<phase>.md`, `io-<phase>/` snapshots, `gate-<name>.out`, `gates.log`, `scope.out`, `phases.done`, `pr.number` |
| `../notaire-localai/.localai/<n>/` (`IO`, git-excluded) | worker | skeletons it fills: `triage.env`, `triage.md`; later `tests.env`, `pr-body.md`, `BLOCKED.md` |

## Guardrails (enforced after every worker run)

| Guard | What it does |
|---|---|
| Scope guard | paths outside the phase's allowed regex are reverted. A reverted violation does not fail the attempt: it is logged `scope-reverted REVIEW` in `gates.log` for Gate 4, and heads `gate.out` only if the phase gate fails (#1064 lost a triage attempt to an OpenSpec file already reverted). The implement scope (`bin/scope.py`) also admits, as exact paths, the existing files planned in triage `## Files to Edit` and traceability `## Planned Files`, so a spec that plans a doc outside the source roots can go green |
| Ref guard | branch switches, new local branches, and commits in no-commit phases (triage) are reverted |
| Git hooks (at commit time) | the worker's git runs with `core.hooksPath=RUNS/<n>/githooks` (injected via `GIT_CONFIG_*`, which Codex would otherwise strip). `pre-commit` rejects forbidden files (build output, logs, `.localai/`, `.env`, backups) and files outside the phase scope; `pre-push` refuses unless the phase is `pr`; `reference-transaction` refuses any ref update except fast-forwards of the issue branch from `BASE` (no branch switch, no new branch, no rewriting earlier phases) |
| Ref guard (after the run) | backstop for the hooks: reverts HEAD switches, deletes new branches, mixed-resets commits that rewrite history or add forbidden paths |
| Frozen TEST_CMD | when Gate 2 passes, `tests.env` is copied to `RUNS/<n>/tests.env`; later gates and prompts read that copy, so the worker cannot change the red-proven command and a wiped `.localai/` cannot lose it |
| Edit tool | `bin/edit.py` (copied to `RUNS/<n>/edit.py`): exact-once OLD/NEW search-replace. Codex `apply_patch` fails with the local model; `sed` line edits are not idempotent and a small model stacks them; `cat >` rewrites drop unread parts. It refuses stray marker lines and ambiguous matches |
| Diff hygiene in `gate_green` | `git diff --check` (conflict markers, whitespace) and a collateral-deletion check (a non-test file losing >10 lines and >25% of its content) |
| Phase base | guards measure from the phase start, not the attempt start, so a retry may amend the phase's own commits; the CI fix loop re-bases after every push so pushed commits are never rewritten |
| Full suite is harness-only | the worker runs single test classes; `gate_green` runs the full suite. A local model does not wait for a 6-minute Maven run: it reruns it in parallel and corrupts `target/` |
| Ledger is harness-owned | `bin/ledger.py`. After Gate 2, `tasks.md` may change only by `[ ]`→`[x]`. When it changes otherwise, `repair_tasks` (in `gate_green`/`gate_docs`) restores the base file with the worker's ticks (`restore-ticks`, matched by task ID), commits the repair and logs the discarded lines as `tasks-repaired` for Gate 4 — the #1063 worker could not undo its own `[-]` marks and added items in three retries; the worker kept rewriting it with invented results ("PR created", "CI green") or rewording items instead of ticking them. `gate_spec` requires the `Commits` and `Pull Request` rows exactly once (`ledger.py rows`), because the harness fills them later. After the docs phase the foreman writes the Commits and CHANGELOG rows of `traceability.md` (`record_ledger`); the worker copied SHAs from `main` |
| Harness pushes | `push_branch`/`record_pr` in `phase_pr`; `githooks/pre-push` denies every worker push. The local model marked `git push` as needing sandbox escalation (refused under approval=never) and looped: sed-edits of the ledger, duplicate commits, `git reset` to the remote, `pkill -9 git`, `--no-verify`, `-f`. Earlier, the repo's 10-minute pre-push preflight made it start a second push; two Maven runs on one `target/` failed 452 tests. The harness rebases over CI-bot report commits, pushes with `PREFLIGHT_SKIP=1` (quality already ran preflight; CI re-runs it), and records the PR number and ticks 10.1–10.2 itself. The worker only writes the PR body and runs `gh pr create/edit` |
| Foreman backups are hidden | Manual foreman backups of worker commits go to `refs/foreman/backup/<issue>-<phase>`, never `refs/heads/`: the worker lists branches and checked out a `backup/` branch to "recover" discarded commits. `push_branch` refuses a detached HEAD (it would push the stale branch ref), and `ALLOW_COMMIT=0` makes `githooks/pre-commit` deny commits in the pr phase |
| CI judged on the last real commit | CI bots push `docs: add PR validation report … [skip ci]` onto the PR branch. That commit has no check runs, so `gh pr checks` (which reads the PR head) reported failure and started a needless ci-fix worker. `ci_sha`/`ci_state`/`wait_ci` read `check-runs` of the last commit whose message lacks `[skip ci]`; the merge gate uses the same test |
| Gate 4 feedback channel | Once a PR is open, re-running a phase to apply review notes is wrong: `pr` forbids commits, `docs` re-runs quality and pipeline. `foreman.sh <n> fix` feeds `$STATE/gate4.md` to the worker (`prompts/10-review.md`, commits allowed, pushes not), then the harness pushes and re-runs `ci` and `review`. Notes that work: exact OLD/NEW blocks plus CHECK/EXPECTED lines; "copy the gates.log lines" failed because the log holds every retry |
| Failed rebase is aborted | A conflicting `pull --rebase` in `push_branch` left `rebase-merge/` behind, and every later push failed. `push_branch` now runs `git rebase --abort` before stopping |
| Gate 5 follows workflow_run | `cd.yml` is triggered by `workflow_run` when CI finishes, so it runs on main's head at that moment — the CI bot's `[skip ci]` report commit, not the merge commit. `--commit <merge sha>` found nothing (#1069 logged a false `MISSING`). `main_run` takes the newest main run whose head descends from the merge commit |
| Archive after merge | `validate-sdlc-plan.sh` rejects any change whose issue is CLOSED, and it runs on every PR. After Gate 5 closed #1069, every open PR failed plan validation until `remove-dead-credentials-1069` was archived (done in #1074, precedent `3dfb3f0`). Until the harness does it itself: after `merge`, the next PR fills the ledger rows (CI run, merge commit, cd.yml, smoke) with `bin/ledger.py` and runs `openspec archive <change> --yes` |
| Sibling worktrees are not the worker's | Worktrees share `refs/heads`. The ref guard used to delete any branch created during a run, so the foreman's own PR branch in `../notaire-harness` was blamed on the #1063 worker and burned its last retry. Branches checked out in another worktree are now skipped |
| Build dirs anchored | `FORBIDDEN` matched `(^\|/)coverage/` anywhere, so the pre-commit hook refused `openspec/changes/<c>/specs/coverage/spec.md` and the worker "cleaned up" by deleting its own spec. Build-output dirs now match only at the root or one module deep |
| Spec repairs | `repair_spec`, first in `gate_spec`, applies fixes with one right answer and logs `spec-repaired REVIEW`: a non-code change with `skip_specs: true` loses a leftover `specs/` (Qwen3-Coder lost 3 #1049 attempts to one), and `ledger.py untick-after 2` unticks every task past groups 1-2 (gpt-oss ticked 4.1/4.2 before implementation) |
| Worker searches skip dependency trees | `bin/crawl_guard.py`, linked as `bin/shims/grep` and `bin/shims/find`, prunes `node_modules`, `target`, `.git` and `.next` from recursive searches. `run_worker` sets `ZDOTDIR=sdlc/zdot`, whose startup files source the user's own and then put the shims first on `PATH`, and turns off Codex's shell snapshot, which restores the `PATH` captured at startup. #1049: 30 of 40 worker searches were `find`/`grep -r` |
| Plan shape is gated | `validate-sdlc-plan.sh` skips a change without `schema: notaire-sdlc` and only checks task group numbers. `gate_spec` also requires the schema line and the template's 12 group headings plus item IDs, which the ledger ticks (`10.1`, `10.2`) |
| Harness fixes lint | `md_fix` runs `bin/md_repair.py` (a bare opening fence gets `text`, MD040; a table row gets its trailing pipe, MD055 — neither is `--fix`-able, and the #1049 gpt-oss spec failed its last attempt only on them), then `gates.docs_lint_fix` (`markdownlint-cli2 --fix`) before `md_lint`: in `gate_spec` on the change folder (the spec commit carries the fixes) and in `gate_docs` on every touched `.md` (committed as `style(docs): fix markdown lint`). The worker gets only errors `--fix` cannot solve. The #1063 docs worker was handed blank-line errors the spec phase left |
| Stale block cleared | `run_worker` deletes `IO/BLOCKED.md` before each run; the run's copy stays in `RUNS/<n>/io-<label>/`. A block written by the #1063 pr worker failed the later Gate 4 fix run after the fix was committed |
| `Closes` checked by the harness | `phase_pr` checks `Closes #n` in full commit messages before the worker runs; `09-pr.md` no longer asks. The #1063 pr worker read `--oneline` subjects and blocked twice |
| No builds in spec | The #1063 worker started Maven in the spec phase and polled it for 15+ minutes. `03-spec.md` forbids builds; coverage and test facts come from the issue and triage |
| Gates | deterministic, with actionable messages (exact line, exact fix). Cosmetic noise (backticks, dash variants) is normalized, not failed |
| History policy | The harness owns the branch history. `phase_spec` commits the spec itself and folds every review round into that one commit (`--amend`); `squash_spec_churn` folds a trailing run of openspec-only commits into one before the PR. Nothing already pushed is rewritten. The PR is merged with `gh pr merge --merge`, so main keeps the red→green commits as evidence of Gate 2 |
| Quoted env values | `bin/envfile.py` reads every `KEY=value` file the worker writes (`triage.env`, `tests.env`). It strips one pair of surrounding quotes and a trailing `# comment`, as a shell would. #1063's `TEST_CMD="mvn …"` kept its quotes, so the red gate ran a command named `mvn …` and "failed" for the wrong reason |
| Static test checks | `bin/static_checks.py`, run by the red gate on the branch's test changes: no absolute home path (`/Users/`, `/home/`), no new test class whose file name already exists elsewhere (extend it instead), and at least one added assertion. A failing test is not proof of a useful test |
| Review notes are enforced | A pending `RUNS/<n>/review-<phase>.md` must be acted on: an attempt whose worker changed no file fails with the note repeated, and the note's `CHECK:`/`EXPECTED:` lines run after the phase gate (`review_checks_pass`), so a note the worker only half-applied goes back with the failing checks. A note applied in a passing run is renamed `applied-review-<phase>-<ts>.md` (logged `review-applied`), out of the glob `render` reads. #1049's spec worker first ignored a note, then edited one unrelated line, and the gate passed both times |
| Review notes are executable | `foreman.sh <n> check` runs every `CHECK:` line of the pending `review-*.md` notes and `gate4.md` (`bin/review_check.py`). A single-token `EXPECTED` gives PASS/FAIL, free text gives JUDGE for the foreman; exit 1 on any FAIL |
| Gate metrics | `gate_log` also appends `{ts, issue, gate, result, detail}` to `RUNS/<n>/metrics.jsonl` (`bin/metrics.py`), so retries and failure causes per gate can be counted across issues |
| Per-phase model | `PROFILE_<PHASE>` (e.g. `PROFILE_SPEC=omlx-gptoss`) overrides the codex `PROFILE` for one phase, so another model can take the phases the default local model gets wrong. The profiles come from `setup-omlx-codex.sh` presets (`local-ai/README.md`); on #1049 gpt-oss-20b passed the spec checks Qwen3-Coder failed 6 times |
| bash ≥ 4 | `foreman.sh` exits at once under bash 3 (macOS `/bin/bash` is 3.2), whose empty-array expansion under `set -u` broke the harness case by case. Run it as `./foreman.sh` so `env` picks Homebrew bash |
| TEST_CMD form | The red gate rejects a `TEST_CMD` that does not start with the surface's `test_one` command up to `{test}`, and shows the expected form. #1063's worker dropped `-pl backend-api`, so Maven errored in another module instead of failing on the new assertions |
| Harness self-tests | `python3 -m unittest discover -s local-ai/sdlc/tests`; they run in `sdlc-process.yml` |
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
  gate without calling the worker; a failing gate stops the run instead of
  starting a worker retry. Never use `| grep -q` in the harness:
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
| Timeouts on macOS | `bin/watchdog.py WORKER_TIMEOUT codex exec …` (no coreutils `timeout`): the worker runs in its own process group, which gets TERM on timeout and KILL after `WATCHDOG_GRACE` s (exit 124). A perl alarm did not stop `codex exec` |
