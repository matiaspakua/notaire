## ADDED Requirements

### Requirement: Board coherence reads the label taxonomy

The coherence check SHALL treat each legacy label and its taxonomy name (`workspace/sdlc/label_taxonomy.py`) as the same label, and SHALL add three rules for open issues (#1860): R6 `status:in-review` requires an open linked PR when the PR count is known; R7 `status:ready` and `status:blocked` are mutually exclusive; R8 a `status:ready` issue carries at most one `type:` label, merged legacy names counted once.

#### Scenario: New WIP name satisfies In Progress

- **WHEN** an open issue has Status In Progress and `status:in-progress`
- **THEN** the check passes

#### Scenario: In review without an open PR

- **WHEN** an open issue carries `status:in-review` and has zero open linked PRs
- **THEN** the check fails with R6

#### Scenario: Ready and blocked together

- **WHEN** an open issue carries `ready-for-dev` and `status:blocked`
- **THEN** the check fails with R7

#### Scenario: Ready issue with two types

- **WHEN** a ready issue carries `bug` and `type:feature`
- **THEN** the check fails with R8, while `TAREA` plus `chore` (both `type:chore`) passes

#### Scenario: Taxonomy exception label

- **WHEN** a PR carries `process:sdlc-exception`
- **THEN** the exception, TDD-evidence and archive-hygiene checks treat it as `sdlc-exception`
