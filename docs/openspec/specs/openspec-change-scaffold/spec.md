# openspec-change-scaffold Specification

## Purpose
TBD - created by archiving change feat-1661-scaffold-openspec-change. Update Purpose after archive.

## Requirements

### Requirement: A change is scaffolded from a spec file

`scaffold-openspec-change.py scaffold <spec.json>` MUST write a change whose proposal, design, delta spec, tasks and traceability contain no template placeholder comment bodies, with the given tasks in group 4 and one requirement-coverage row per delta-spec scenario, and MUST create the change folder when it is missing. `complete <name> --test <TestName>` MUST tick tasks in groups 1-8 and 9.1/9.3 and mark that test passing in the coverage table.

#### Scenario: scaffolded change passes plan validation

- **WHEN** a change is scaffolded from a spec file in a repository copy
- **THEN** `validate-sdlc-plan.sh` accepts it

#### Scenario: scenarios become coverage rows

- **WHEN** the delta spec has three scenarios
- **THEN** the traceability coverage table has three rows naming the given test

#### Scenario: complete ticks the implementation groups

- **WHEN** `complete` runs on a scaffolded change
- **THEN** every task in groups 1-8 is checked and the task-completion gate accepts the change

#### Scenario: missing folder is created

- **WHEN** the change folder does not exist
- **THEN** scaffolding creates it with the notaire-sdlc schema marker
