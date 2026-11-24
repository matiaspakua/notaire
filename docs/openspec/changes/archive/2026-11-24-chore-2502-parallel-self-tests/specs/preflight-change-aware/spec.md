## ADDED Requirements

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
