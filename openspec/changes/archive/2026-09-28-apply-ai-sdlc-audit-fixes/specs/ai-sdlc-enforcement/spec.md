## ADDED Requirements

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
references a repository path that does not exist.

#### Scenario: Empty rule file

- **WHEN** a file under `.claude/rules/` is empty
- **THEN** the check fails and names it

#### Scenario: Dead path

- **WHEN** a rule file references `` `docs/does-not-exist/` ``
- **THEN** the check fails and names the file and the path

#### Scenario: No rule files found

- **WHEN** the check runs against a root that has no agent rule files
- **THEN** it fails instead of passing with nothing checked

### Requirement: Changes must declare their schema

`validate-sdlc-plan.sh` SHALL fail an active change whose `.openspec.yaml` has
no `schema:` line, instead of skipping it.

#### Scenario: Missing schema line

- **WHEN** an active change has no `schema:` line
- **THEN** the validator reports an error for that change
