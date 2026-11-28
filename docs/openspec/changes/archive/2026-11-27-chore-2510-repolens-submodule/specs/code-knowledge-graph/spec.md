<!-- Governed by CONSTITUTION.md. CU76, issue #2510. -->

## Purpose

Notaire obtains the RepoLens product from its independent repository while keeping the stable `framework/repolens` path for modules, preflight and harness wiring.

## ADDED Requirements

### Requirement: RepoLens path is a git submodule of the product repository

Notaire MUST record `framework/repolens` as a git submodule whose URL is the independent RepoLens product repository, and MUST NOT vendor a second copy of that tree in the monorepo.

#### Scenario: gitmodules points at the product

- **WHEN** a developer inspects `.gitmodules`
- **THEN** an entry with `path = framework/repolens` names the `draclab/repolens` repository

#### Scenario: module verify still runs from the same path

- **WHEN** the submodule is initialized and `bash framework/repolens/verify.sh` runs
- **THEN** the RepoLens module gate executes from that path as before

#### Scenario: harness wiring still resolves the launcher

- **WHEN** `workspace.tests.test_repolens_integration` runs on an initialized checkout
- **THEN** the launcher under `framework/repolens/bin/repolens` and `MODULE.md` remain reachable

#### Scenario: fresh clone instructions initialize the submodule

- **WHEN** a developer follows the README clone instructions
- **THEN** the instructions use `--recurse-submodules` or an explicit `git submodule update --init`
