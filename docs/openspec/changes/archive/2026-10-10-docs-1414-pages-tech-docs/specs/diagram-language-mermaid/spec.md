# diagram-language-mermaid

Active documentation uses Mermaid as the sole diagram language.

## ADDED Requirements

### Requirement: ADR records the decision

An Architecture Decision Record MUST state that Mermaid is canonical for active documentation and
that new PlantUML must not be added to active docs.

#### Scenario: ADR-027 accepted

- **WHEN** the ADR index is opened
- **THEN** ADR-027 (or the recorded Mermaid decision) appears with status Accepted

### Requirement: Diagrams directory policy

`docs/200-architecture/204-diagrams/README.md` MUST state the Mermaid policy and how existing
`.puml` sources are treated (archive / migrate, no new active PlantUML).

#### Scenario: Policy readable

- **WHEN** a contributor adds a diagram under active architecture docs
- **THEN** the README instructs them to use Mermaid
