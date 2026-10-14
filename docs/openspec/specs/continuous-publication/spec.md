# continuous-publication Specification

## Purpose

TBD - created by archiving change chore-1681-continuous-publication-rule. Update Purpose after archive.

## Requirements

### Requirement: Progress is published to the branch and the issue

The publish helper MUST push the current branch, confirm the remote head equals the local head, and comment on the issue with the branch, last commit and stage.

#### Scenario: push and comment

- **WHEN** the helper runs on a feature branch with a stage summary
- **THEN** the remote branch head equals the local head and an issue comment containing the branch, commit and stage is posted

#### Scenario: main is refused

- **WHEN** the helper runs on main
- **THEN** it exits non-zero and pushes and comments nothing

#### Scenario: failed push is reported

- **WHEN** the remote head does not equal the local head after the push
- **THEN** the helper exits non-zero and posts no comment

### Requirement: The rule is documented where agents read it

The Constitution, the Agent Card and the operational rules MUST state the rule.

#### Scenario: rule present in every document

- **WHEN** the four documents are read
- **THEN** each mentions publishing the branch immediately and commenting on the issue at each stage
