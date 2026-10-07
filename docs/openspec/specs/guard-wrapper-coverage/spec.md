# guard-wrapper-coverage Specification

## Purpose

Make every guard run in CI. Source: #1209, reshaped by #1307 (ADR-026); owner CU76.

## Requirements

### Requirement: Every guard lives in a module `tests/` directory that CI and preflight discover

Each guard MUST live in the `tests/` directory of the module it guards (or `workspace/tests/` when it
reads several modules), and both `workspace/sdlc/preflight.sh` and `sdlc-process.yml` MUST run
`python3 -m unittest discover` on every such directory. No wrapper layer exists.

#### Scenario: A guard in a discovered directory runs

- **WHEN** `python3 -m unittest discover -s <module>/tests` runs for each module listed in preflight
- **THEN** the tests of every guard in it are collected and pass

#### Scenario: No guard sits outside a discovered directory

- **WHEN** `workspace/tests/test_scripts_layout.py` runs
- **THEN** no tracked file remains under `scripts/` and none references a removed path

#### Scenario: A missing external tool skips

- **WHEN** `kustomize` is not installed
- **THEN** the kustomize guard skips its tests instead of erroring
