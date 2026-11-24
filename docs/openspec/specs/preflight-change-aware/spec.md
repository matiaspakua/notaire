# preflight-change-aware Specification

## Purpose

TBD - created by archiving change chore-2413-affected-preflight. Update Purpose after archive.

## Requirements

### Requirement: The full gate runs the suites the branch touches

`preflight.sh --full` SHALL compare the branch with origin/main and run a suite group only when
the branch changes a path that group covers. The groups are backend (plus format, lint and the
report-only checks), frontend, e2e-suite lint, Pages, and the server-backed suites (product e2e,
Bruno, HTTP, database V&V, Docker). Process checks, security gates and self-tests SHALL always run.
The summary SHALL list each skipped group as not affected by the branch.

#### Scenario: A factory-only branch skips the product suites

- **WHEN** a branch changes only `framework/ai-sdlc/factory-dashboard/src/ui/App.tsx`
- **THEN** the backend, frontend, Pages and server-backed groups are skipped as not affected, and
  the process checks still run

#### Scenario: A backend branch runs the backend and the server-backed suites

- **WHEN** a branch changes `backend-api/src/main/java/Foo.java`
- **THEN** the backend and server-backed groups run, and the frontend and Pages groups are skipped

#### Scenario: A docs branch runs Pages

- **WHEN** a branch changes `docs/README.md`
- **THEN** the Pages group runs, and the backend, frontend and server-backed groups are skipped

### Requirement: Cross-cutting changes run everything

The gate SHALL run every group when the branch changes the gate itself, the git hooks, the stack
scripts, a compose file, the root `pom.xml` or a Dockerfile, when `--all` is given, or when the
changed paths cannot be computed against origin/main.

#### Scenario: Changing the gate runs everything

- **WHEN** a branch changes `workspace/sdlc/preflight.sh`
- **THEN** every group runs

#### Scenario: No comparison base runs everything

- **WHEN** the changed paths cannot be listed against origin/main
- **THEN** every group runs

### Requirement: A re-push of a commit that already passed is not run again

The pre-push hook SHALL record a pass for the pushed commit and the gate mode. When every pushed
commit is the checked-out commit and it already passed in the same mode (or `--all` passed), the
hook SHALL skip the gate and say so. Any other commit, including a merge of main, SHALL run the
gate.

#### Scenario: Same commit pushed again

- **WHEN** the gate passed for commit C and C is pushed again
- **THEN** the hook prints that C already passed and exits without running the gate

#### Scenario: A new commit runs the gate

- **WHEN** a commit D that has no recorded pass is pushed
- **THEN** the hook runs the gate

### Requirement: Build files select the preflight groups they affect

`workspace/sdlc/preflight_affected.py` SHALL map `docker-compose*.yml` and `workspace/stack/` to the `stack`
group, the root `pom.xml` to `backend` and `stack`, and each `Dockerfile` to the groups of its module. No path SHALL
select every group; only a missing comparison base does.

#### Scenario: A compose change runs only the server-backed group

- **WHEN** a branch changes only `docker-compose.prod.yml`
- **THEN** the affected groups are `stack`

#### Scenario: The root POM runs the backend

- **WHEN** a branch changes only `pom.xml`
- **THEN** the affected groups are `backend` and `stack`

### Requirement: The process self-tests run in parallel

`workspace/sdlc/run_self_tests.py` SHALL run every `test_*.py` file of the process test directories as its own
`unittest discover -s <dir> -p <file>` process, in parallel up to the CPU count, and SHALL exit non-zero when any
file fails, printing the output of each failing file. `preflight.sh` SHALL use it for the process self-tests.

#### Scenario: A failing file fails the run and is named

- **WHEN** one test file in a test directory fails
- **THEN** the runner exits 1 and prints that file's name and unittest output

#### Scenario: Passing files run concurrently

- **WHEN** a directory holds several slow passing test files
- **THEN** the runner finishes in less time than running them one after the other
