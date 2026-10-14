# object-authz-decision Specification

## Purpose
TBD - created by archiving change docs-1637-object-authz-adr. Update Purpose after archive.

## Requirements

### Requirement: The ownership model is recorded before object-level authorization is built

An ADR MUST state the facts about record ownership in the schema, the options considered, the proposed decision, its consequences and the open question for the Owner, and MUST be listed in the ADR index.

#### Scenario: the ADR is indexed

- **WHEN** the ADR index is read
- **THEN** it lists ADR-029 with status Proposed

#### Scenario: the ADR states the open question

- **WHEN** ADR-029 is read
- **THEN** it contains a section asking the Owner which deployment model applies
