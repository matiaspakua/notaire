<!-- Governed by CONSTITUTION.md. CU76, issue #1694. -->

## ADDED Requirements

### Requirement: The target architecture is decided and discoverable

ADR-030 MUST be Accepted and state the dependency rules, conventions, enforcement and migration method for backend and frontend; AGENTS.md MUST point agents to the rule file.

#### Scenario: ADR is accepted and complete

- **WHEN** ADR-030 is read
- **THEN** its status is Accepted and it contains the backend rules, the frontend rules, the enforcement section and the migration method

#### Scenario: agents are routed to the rule

- **WHEN** AGENTS.md is read
- **THEN** its on-demand table lists hexagonal-architecture.md and the file exists

### Requirement: Existing documents do not contradict the target

The SAD building-block view and refactoring.md MUST NOT describe a controller calling a repository or the legacy jpa package as the pattern to follow.

#### Scenario: no contradiction

- **WHEN** refactoring.md and SAD section 5.2 are read
- **THEN** controllers are described as calling inbound ports only and the layered api-service-repository diagram is gone
