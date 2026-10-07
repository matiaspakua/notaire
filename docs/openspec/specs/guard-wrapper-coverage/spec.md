# guard-wrapper-coverage Specification

## Purpose

Make every top-level guard run in CI. Source: #1209; owner CU76.

## Requirements

### Requirement: Every top-level guard has a discoverable wrapper

Each `scripts/test_*.py` MUST have a wrapper of the same name in `scripts/tests/`, unless it is
listed in the meta-guard's exemption map with a reason.

#### Scenario: A guard without a wrapper fails the meta-guard

- **WHEN** a top-level `scripts/test_*.py` has no wrapper
- **THEN** `scripts/tests/test_guard_wrappers.py` fails and names it

#### Scenario: Wrapped guards are collected

- **WHEN** `python3 -m unittest discover -s scripts/tests` runs
- **THEN** the tests of every wrapped guard are collected and pass

#### Scenario: A missing external tool skips

- **WHEN** `kustomize` is not installed
- **THEN** the kustomize guard skips its tests instead of erroring
