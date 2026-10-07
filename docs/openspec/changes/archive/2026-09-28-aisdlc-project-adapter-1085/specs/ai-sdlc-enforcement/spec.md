## ADDED Requirements

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
