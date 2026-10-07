## Purpose

Each part of the system is a self-describing, independently verifiable module. Source: #1197, #1292; owner CU76.

## ADDED Requirements

### Requirement: Every module is declared in the manifest and documents its contract

`workspace/modules.yaml` MUST list each module with a path, responsibility, verify command and `depends_on`; each listed module MUST contain `MODULE.md` and an executable `verify.sh`.

#### Scenario: Manifest and folders agree

- **WHEN** the manifest guard runs
- **THEN** every listed module exists with `MODULE.md` and an executable `verify.sh`, and every `depends_on` names a listed module

#### Scenario: Dependencies are acyclic

- **WHEN** the manifest guard runs
- **THEN** the `depends_on` graph has no cycle

### Requirement: A module verifies itself

`verify.sh` MUST run only that module's build, tests, format and lint, from any working directory.

#### Scenario: verify.sh is runnable

- **WHEN** `bash -n <module>/verify.sh` runs for each module
- **THEN** it parses without errors and starts with `set -euo pipefail`
