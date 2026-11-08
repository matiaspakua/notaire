# ai-sdlc-self-improvement Specification

## Purpose
TBD - created by archiving change docs-2023-self-improvement-ai-sdlc. Update Purpose after archive.
## Requirements
### Requirement: Agents self-improve the AI SDLC while delivering

While implementing a change, an AI agent MUST treat process friction, gate failures and near-misses as input to improve: update CONSTITUTION/framework rules when Owner-scoped, mechanize lessons into gates or ratchets when possible, and leave bounded Boy Scout cleanups in the same PR. The TDD evidence gate MUST count `docs/tests/` as test evidence.

#### Scenario: docs/tests counts for TDD evidence

- **GIVEN** a tip whose only test change is under `docs/tests/` and that later changes `backend-api/src/main`
- **WHEN** `check-tdd-evidence.sh` runs against that tip
- **THEN** it exits 0 (tests before production)

#### Scenario: Constitution names self-improvement

- **GIVEN** the Notaire Constitution tip
- **WHEN** an agent loads §2 principles and §10 must-dos
- **THEN** a self-improvement / learning-loop principle and matching must-do are present

