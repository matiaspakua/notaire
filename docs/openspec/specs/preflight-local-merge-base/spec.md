# preflight-local-merge-base Specification

## Purpose

TBD - created by archiving change fix-1655-preflight-local-merge-base. Update Purpose after archive.

## Requirements

### Requirement: Local checks use the merge-base diff

Outside CI the process checks MUST diff base...HEAD even when HEAD is a merge commit; in CI (GITHUB_ACTIONS=true) they MUST keep diffing from the merge parent.

#### Scenario: branch that merged main still shows its OpenSpec path

- **WHEN** a branch adds an OpenSpec change, then merges main which gained other commits, and check-sdlc-exception.sh runs without GITHUB_ACTIONS
- **THEN** it exits 0

#### Scenario: CI merge checkout keeps its behaviour

- **WHEN** the same checks run on a pull-request merge commit with GITHUB_ACTIONS=true
- **THEN** the diff starts at the first parent as before

#### Scenario: archive hygiene and doc traceability follow the same rule

- **WHEN** diff_base runs on a merge commit without GITHUB_ACTIONS
- **THEN** it returns the given base unchanged
