# ai-sdlc-enforcement Specification

## Purpose

Mechanical checks of the CONSTITUTION process: the local-AI harness gates
(env parsing, static test checks, runnable review notes, metrics) and the PR
checks run by `sdlc-process.yml` and `scripts/preflight.sh`. Issue #1083
introduced them from the findings in `local-ai/AUDIT.md`.

## Requirements

### Requirement: Harness env files accept quoted values

The harness SHALL read `KEY=value` files (`triage.env`, `tests.env`) with one
parser that drops a trailing `#` comment and one pair of surrounding single or
double quotes, both in `kv()` and in the prompt renderer.

#### Scenario: Double-quoted command

- **WHEN** `tests.env` contains `TEST_CMD="mvn -q test -Dtest=FooTest"`
- **THEN** the harness reads `mvn -q test -Dtest=FooTest` without the quotes

#### Scenario: Single-quoted value with trailing comment

- **WHEN** a line reads `KIND='code'  # triage`
- **THEN** the value is `code`

#### Scenario: Inner quotes are kept

- **WHEN** a line reads `TEST_CMD=bash -c "exit 1"`
- **THEN** the value is `bash -c "exit 1"`

### Requirement: Static review gates on test changes

Before the red run, the harness SHALL reject a branch whose test changes add an
absolute home path, add a test class whose file name already exists at another
path, or add no assertion.

#### Scenario: Absolute home path

- **WHEN** an added line in a test file contains `/Users/` or `/home/`
- **THEN** the gate fails and names the file

#### Scenario: Duplicate test class

- **WHEN** the branch adds `FooTest.java` and another `FooTest.java` already exists on the base
- **THEN** the gate fails and names both paths

#### Scenario: No new assertion

- **WHEN** the added lines of all changed test files contain no assertion call
- **THEN** the gate fails

#### Scenario: Clean test change

- **WHEN** the test change adds a new uniquely named test with an assertion and no home path
- **THEN** the static gate passes

### Requirement: Foreman review notes are machine-checked

`foreman.sh <issue> check` SHALL run each `CHECK:` line of the pending review
notes in the worker worktree and report its output next to `EXPECTED:`. When
`EXPECTED:` is a single literal token, it SHALL mark the check PASS or FAIL and
exit non-zero if any check fails.

#### Scenario: Literal expectation met

- **WHEN** a note has `CHECK: echo 0` and `EXPECTED: 0`
- **THEN** the check reports PASS

#### Scenario: Literal expectation missed

- **WHEN** a note has `CHECK: echo 2` and `EXPECTED: 0`
- **THEN** the check reports FAIL and the command exits non-zero

#### Scenario: Free-text expectation

- **WHEN** `EXPECTED:` is a sentence
- **THEN** the check prints the output and the expectation for the foreman to judge, without failing

### Requirement: Per-gate metrics

Every gate result the harness logs SHALL also be appended to
`$RUNS/<issue>/metrics.jsonl` as one JSON object with `ts`, `issue`, `gate`,
`result` and `detail`.

#### Scenario: Gate result recorded

- **WHEN** the harness logs a gate result
- **THEN** `metrics.jsonl` gains one line that parses as JSON with those keys

### Requirement: PR commits are conventional

CI and preflight SHALL fail when a non-merge commit in the PR range has a subject
that is not a Conventional Commit.

#### Scenario: Bad subject

- **WHEN** a commit subject is `update stuff`
- **THEN** the check fails and prints the subject

#### Scenario: Good subjects

- **WHEN** every subject matches `<type>(<scope>)?!?: <description>`
- **THEN** the check passes

### Requirement: TDD evidence on production-code PRs

CI and preflight SHALL fail a PR that changes production code (`src/main/`,
`frontend/src/` non-test files) when the range changes no test file, or when a
production-code commit comes before the first commit that changes a test, unless
the PR carries the `sdlc-exception` label.

#### Scenario: Production code without tests

- **WHEN** the range changes `backend-api/src/main/...` and no test file
- **THEN** the check fails

#### Scenario: Tests first

- **WHEN** the first commit changes a test and a later commit changes production code
- **THEN** the check passes

#### Scenario: Code before tests

- **WHEN** a production-code commit comes before the first test commit
- **THEN** the check fails and names the commit

### Requirement: SDLC exceptions are labelled

CI SHALL fail a PR that touches no `openspec/changes/` path unless it carries the
`sdlc-exception` label or is opened by a dependency bot.

#### Scenario: No change folder, no label

- **WHEN** the PR changes no `openspec/changes/` path and has no `sdlc-exception` label
- **THEN** the check fails and explains CONSTITUTION §12

#### Scenario: Labelled exception

- **WHEN** the same PR has the `sdlc-exception` label
- **THEN** the check passes

### Requirement: Agent rule files stay valid

CI and preflight SHALL fail when an always-loaded agent rule file is empty or
references a repository path that does not exist. In addition, CI and preflight
SHALL fail when `.claude/rules/refactoring.md` describes an obsolete migration
target rather than the current stack (Spring Boot 4.1 / Java 21 / PostgreSQL 16,
package `com.licensis.notaire`, Next.js frontend, `Dto*` DTOs).

#### Scenario: Empty rule file

- **WHEN** a file under `.claude/rules/` is empty
- **THEN** the check fails and names it

#### Scenario: Dead path

- **WHEN** a rule file references `` `docs/does-not-exist/` ``
- **THEN** the check fails and names the file and the path

#### Scenario: No rule files found

- **WHEN** the check runs against a root that has no agent rule files
- **THEN** it fails instead of passing with nothing checked

#### Scenario: Obsolete package root rejected

- **WHEN** `.claude/rules/refactoring.md` contains the string `com.notaria`
- **THEN** `scripts/check-agent-rules.sh` fails and names the obsolete marker

#### Scenario: Swing-as-target markers rejected

- **WHEN** `.claude/rules/refactoring.md` contains `SwingWorker`, `JOptionPane`,
  or `standalone Swing GUI client` as current guidance
- **THEN** the check fails and names the obsolete marker

#### Scenario: Obsolete Boot Java Postgres markers rejected

- **WHEN** `.claude/rules/refactoring.md` contains `Spring Boot 3`, `Java 17`,
  `PostgreSQL 15`, or `EntityRequestDTO` as current target guidance
- **THEN** the check fails and names the obsolete marker

#### Scenario: Current stack markers accepted

- **WHEN** `.claude/rules/refactoring.md` describes Spring Boot 4.1, Java 21,
  PostgreSQL 16, `com.licensis.notaire`, Next.js, and `Dto*` naming, and
  contains none of the obsolete markers above
- **THEN** `scripts/check-agent-rules.sh` passes for that file

### Requirement: Changes must declare their schema

`validate-sdlc-plan.sh` SHALL fail an active change whose `.openspec.yaml` has
no `schema:` line, instead of skipping it.

#### Scenario: Missing schema line

- **WHEN** an active change has no `schema:` line
- **THEN** the validator reports an error for that change

### Requirement: Project values come from the adapter

The harness SHALL read project-specific values from `.aisdlc/project.yml`
through `bin/adapter.py get <dotted.key> [name=value…]`, which substitutes
`{name}` placeholders and fails with a message naming the key when the key is
missing.

#### Scenario: Scalar with placeholder

- **WHEN** `spec.validate` is `openspec validate {change} --strict` and the harness runs `get spec.validate change=foo-1`
- **THEN** it prints `openspec validate foo-1 --strict`

#### Scenario: List value

- **WHEN** `gates.main_workflows` is `[ci.yml, cd.yml]`
- **THEN** `get gates.main_workflows` prints `ci.yml` and `cd.yml` on separate lines

#### Scenario: Missing key

- **WHEN** the harness asks for a key the adapter does not define
- **THEN** the command exits non-zero and names the missing key and the adapter file

### Requirement: Surfaces are derived from the adapter

The harness SHALL derive a change's surfaces by matching its file list against
each adapter surface `root`, and SHALL build the full-suite command from the
matched surfaces' `suite` commands.

#### Scenario: Two surfaces

- **WHEN** the file list has one path under `backend-api/` and one under `frontend/`
- **THEN** `surfaces` prints `backend,frontend`

#### Scenario: No surface

- **WHEN** no path is under a surface root
- **THEN** `surfaces` prints `none`

#### Scenario: Suite for two surfaces

- **WHEN** the harness asks for `suite backend,frontend`
- **THEN** it prints both suite commands, each in parentheses, joined by `&&`

#### Scenario: Legacy value both

- **WHEN** a stored `triage.env` has `SURFACE=both`
- **THEN** `suite both` returns the suite for every surface

### Requirement: TEST_CMD uses the surface's single-test command

Before the red run, the harness SHALL reject a `TEST_CMD` that does not start
with the text before `{test}` in the `test_one` command of one of the change's
surfaces, and SHALL show the expected form.

#### Scenario: Missing module flag

- **WHEN** the surface is `backend` and TEST_CMD is `mvn -q -B test -Dtest=FooTest`
- **THEN** the check fails and prints `mvn -q -B test -pl backend-api -Dtest=<TestClass>`

#### Scenario: Correct command

- **WHEN** the surface is `backend` and TEST_CMD is `mvn -q -B test -pl backend-api -Dtest=FooTest,BarTest`
- **THEN** the check passes

### Requirement: The implement scope admits planned files

The implement phase scope SHALL allow `.localai/`, the change's OpenSpec folder,
the adapter `source_roots`, and, as exact paths, every existing file listed in
triage `## Files to Edit` or in the change's traceability `## Planned Files`.

#### Scenario: Planned file outside the source roots

- **WHEN** traceability `## Planned Files` lists `CONSTITUTION.md` and the file exists
- **THEN** the implement scope matches `CONSTITUTION.md`

#### Scenario: Unplanned file

- **WHEN** `README.md` is outside the source roots and neither list names it
- **THEN** the implement scope does not match `README.md`

#### Scenario: Non-path cell

- **WHEN** a Planned Files cell holds a bare class name that is not an existing path
- **THEN** it adds nothing to the scope

### Requirement: The worker watchdog enforces the timeout

The harness SHALL run each worker in its own process group and SHALL end the
whole group when `WORKER_TIMEOUT` expires, with KILL if TERM does not stop it,
exiting 124.

#### Scenario: Worker finishes in time

- **WHEN** the worker exits with status 3 before the timeout
- **THEN** the watchdog exits 3

#### Scenario: Worker ignores TERM

- **WHEN** the worker and its child ignore TERM and outlive the timeout
- **THEN** both are killed and the watchdog exits 124

### Requirement: The harness repairs tasks.md to base plus ticks

After Gate 2, when the worker changes `tasks.md` beyond ticking boxes, the
harness SHALL restore the base file and SHALL keep only the `[x]` ticks the
worker set on base items, matched by task ID.

#### Scenario: Tick kept

- **WHEN** the worker's file ticks item `8.1`, which is unticked in the base
- **THEN** the repaired file ticks `8.1`

#### Scenario: Added item dropped

- **WHEN** the worker's file adds an item `5.4` absent from the base
- **THEN** the repaired file equals the base apart from ticks

#### Scenario: Not-applicable mark dropped

- **WHEN** the worker's file marks base item `8.2` as `[n/a]`
- **THEN** the repaired file keeps `8.2` unticked with its base text

### Requirement: The spec gate requires the ledger rows

The spec gate SHALL reject a `traceability.md` in which the `Commits` or the
`Pull Request` row is not present exactly once.

#### Scenario: Ledger rows present

- **WHEN** `traceability.md` has one `Commits` row and one `Pull Request` row
- **THEN** the row check passes

#### Scenario: Ledger row missing

- **WHEN** `traceability.md` has no `Commits` row
- **THEN** the row check fails and names `Commits`

### Requirement: The adapter provides a Markdown lint fix command

The project adapter SHALL declare `gates.docs_lint_fix`, which the harness runs
on the Markdown files it lints before it lints them.

#### Scenario: Adapter declares the lint fix command

- **WHEN** the adapter lacks `gates.docs_lint_fix`
- **THEN** adapter validation reports it missing

### Requirement: Triage may name the surface its tests live on

When no Files to Edit path is under a surface root, the change's surface SHALL be
the `TEST_SURFACE` from `triage.env`, if the adapter defines that surface.

#### Scenario: Fallback used

- **WHEN** Files to Edit lists only `docs/x.csv` and `TEST_SURFACE` is `backend`
- **THEN** the change's surface is `backend`

#### Scenario: Path surface wins

- **WHEN** Files to Edit lists `frontend/a.tsx` and `TEST_SURFACE` is `backend`
- **THEN** the change's surface is `frontend`

#### Scenario: Unknown fallback ignored

- **WHEN** Files to Edit lists only `docs/x.csv` and `TEST_SURFACE` is `mobile`
- **THEN** the change's surface is `none`

### Requirement: Triage restores values the harness derived

Before checking `triage.env`, the triage gate SHALL restore every key the harness
seeded with a real value among `ISSUE`, `USE_CASE` and `TYPE`, and log the repair.

#### Scenario: Derived value restored

- **WHEN** the seed has `TYPE=docs` and the worker wrote `TYPE=fix`
- **THEN** `triage.env` holds `TYPE=docs` and `TYPE` is reported as repaired

#### Scenario: Underivable value kept

- **WHEN** the seed has `TYPE=?` and the worker wrote `TYPE=fix`
- **THEN** `triage.env` keeps `TYPE=fix` and nothing is reported

### Requirement: Triage proofs must be able to fail

The triage gate SHALL reject a `command` proof whose program only searches or prints.

#### Scenario: Search command rejected

- **WHEN** a criterion reads `proven by: command bash grep -r X docs/`
- **THEN** the criterion is reported as not a proof

#### Scenario: Script command accepted

- **WHEN** a criterion reads `proven by: command bash scripts/preflight.sh`
- **THEN** nothing is reported

### Requirement: Promised tests need a tests phase

The triage gate SHALL reject `new test` proofs when `KIND` is not `code`.

#### Scenario: Docs kind with new test rejected

- **WHEN** `KIND=docs` and a criterion reads `proven by: new test XTest#shouldY`
- **THEN** the gate reports a KIND conflict

#### Scenario: Code kind with new test accepted

- **WHEN** `KIND=code` and a criterion reads `proven by: new test XTest#shouldY`
- **THEN** nothing is reported

### Requirement: The spec gate removes specs a skip_specs change left behind

When a non-code change sets `skip_specs: true`, the spec gate SHALL delete its
`specs/` folder before validation and log the repair.

#### Scenario: Leftover specs removed

- **WHEN** KIND is `ci`, `.openspec.yaml` sets `skip_specs: true` and `specs/ci/spec.md` exists
- **THEN** `specs/` is removed and `openspec validate --strict` runs on the rest

### Requirement: The spec gate unticks tasks outside groups 1-2

The spec gate SHALL untick every task whose group number is greater than 2.

#### Scenario: Premature tick removed

- **WHEN** `tasks.md` has `- [x] 4.1 Delete the Jenkinsfile`
- **THEN** it reads `- [ ] 4.1 Delete the Jenkinsfile` and 4.1 is reported as unticked

#### Scenario: Prerequisite tick kept

- **WHEN** `tasks.md` has `- [x] 1.1 GitHub Issue exists`
- **THEN** the line is unchanged and nothing is reported

### Requirement: Markdown repair fixes fence languages and table pipes

Before markdown lint, the harness SHALL give a bare opening code fence the
language `text` and append the missing trailing pipe to a table row.

#### Scenario: Bare opening fence

- **WHEN** a file has a code block opened by a bare fence
- **THEN** the opening fence reads ```` ```text ```` and the closing fence stays bare

#### Scenario: Table row without trailing pipe

- **WHEN** a table row reads `| Use Case | CU76`
- **THEN** it reads `| Use Case | CU76 |`

### Requirement: The worker's recursive searches skip dependency trees

The harness SHALL run the worker's shell with `grep`/`find` shims that prune
`node_modules`, `target`, `.git` and `.next` from recursive searches.

#### Scenario: Recursive grep skips node_modules

- **WHEN** the worker runs `grep -rl jenkins .` in a tree with `src/` and `node_modules/`
- **THEN** only the file under `src/` is listed

### Requirement: A pending review note requires a change

When a foreman review note for the phase exists and the worker's run changed no
file and made no commit, the attempt SHALL fail with a message naming the note.

#### Scenario: Review note ignored

- **WHEN** `review-spec.md` exists and the spec worker exits without changing anything
- **THEN** the attempt fails and the retry prompt asks to apply the review

### Requirement: The uncommitted-changes gate names the staging command

The message SHALL include `git add -A -- <files> && git commit --amend --no-edit`
for the files it lists.

#### Scenario: Unstaged edits after an amend

- **WHEN** the branch has uncommitted changes to `a.md`
- **THEN** the message contains `git add -A -- a.md && git commit --amend --no-edit`

### Requirement: The worker agent is selectable

The harness SHALL run the worker with the agent named by `AGENT` or the adapter's
`backend.agent`, keeping the git hooks path and the crawl-guard shims for either.

#### Scenario: OpenCode command

- **WHEN** the agent is `opencode`
- **THEN** the command is `opencode run --pure --auto --format json --dir <wt> -m <model> <prompt>`
  and the environment disables the project config and points `OPENCODE_CONFIG_DIR` at `local-ai/opencode`

#### Scenario: Codex command unchanged

- **WHEN** the agent is `codex`
- **THEN** the command is `codex exec --profile <profile> ... -o <last> <prompt>` as before

#### Scenario: Final message extracted

- **WHEN** an OpenCode JSON event stream ends with a `text` part `DONE`
- **THEN** the last message is `DONE`

#### Scenario: Guards kept

- **WHEN** the worker environment is built for either agent
- **THEN** it sets the git `core.hooksPath` override and puts `bin/shims` first on `PATH`

### Requirement: OpenSpec changes are seeded from notaire-sdlc templates

After `openspec new change`, or when invoked on an existing change folder,
`scripts/seed-openspec-change.sh` SHALL copy
`openspec/schemas/notaire-sdlc/templates/{proposal,design,tasks,traceability}.md`
into the change directory when those files are absent, and SHALL fill the Issue,
Use Case, Branch, and change-name values it is given. It SHALL NOT overwrite an
existing non-empty artifact file.

#### Scenario: Seed copies four templates when absent

- **WHEN** a change folder has only `.openspec.yaml` and the seed script runs
- **THEN** `proposal.md`, `design.md`, `tasks.md` and `traceability.md` exist and
  contain the mandatory `##` headings from the schema templates

#### Scenario: Seed leaves an existing file alone

- **WHEN** `proposal.md` already exists with filled content and the seed script runs
- **THEN** that file's content is unchanged

#### Scenario: Seed fills known header values

- **WHEN** the seed script is given `--issue 1108`, a Use Case string, and a branch
- **THEN** `proposal.md` and `traceability.md` show `#1108`, the Use Case, and the
  branch instead of the template placeholders

### Requirement: Unfilled template HTML-comment sections fail Gate 1

`scripts/validate-sdlc-plan.sh` SHALL reject a `notaire-sdlc` change when a
mandatory `##` section body in `proposal.md`, `design.md`, `tasks.md`, or
`traceability.md` is still only one or more template `<!-- ... -->` HTML
comments (after stripping comments and whitespace the body is empty). The error
SHALL name the file and the heading.

#### Scenario: Leftover HTML-comment body rejected

- **WHEN** `proposal.md` has `## Objetivo` whose body is only
  `<!-- Why this change is needed -->`
- **THEN** the validator exits non-zero and the message names `proposal.md` and
  `Objetivo`

#### Scenario: Filled section body accepted

- **WHEN** every checked `##` section has non-comment prose or table content
- **THEN** the leftover-comment check does not fail that change

#### Scenario: Rejection names file and heading

- **WHEN** `design.md` `## Decisions` is still only a template HTML comment
- **THEN** the failure text includes `design.md` and `Decisions`
