## Purpose
Design-only Isolator / sole-committer runtime boundary (ADR-004). Not an OS sandbox.

## Requirements

### Requirement: Isolator runtime boundary is design-only until evaluable
The system SHALL document an Isolator / sole-committer **runtime** boundary (grants,
write set, agent never commits) that is distinct from the `aisdlc committer` **policy**
dry-run (#2167/#2169). Until a real OS Isolator runtime is configured, Isolator status
SHALL be `no-evaluable`, SHALL report `sandbox=false`, and SHALL NOT claim isolation from
YAML, harness adapters, or profile flags. `aisdlc doctor` SHALL surface that
`no-evaluable` status without failing the profile solely for Isolator being unevaluable.

#### Scenario: Isolator stub is no-evaluable and never a sandbox
- **WHEN** no OS Isolator runtime is configured
- **THEN** Isolator status is `no-evaluable` and `sandbox` is false

#### Scenario: Doctor reports Isolator no-evaluable without failing for it
- **WHEN** `aisdlc doctor` runs on a valid profile without an Isolator runtime
- **THEN** output mentions Isolator `no-evaluable` and exit code is not 1 solely because Isolator is unevaluable

#### Scenario: Policy dry-run remains separate
- **WHEN** documenting Isolator
- **THEN** the ADR cites `aisdlc committer` as the policy check Isolator must eventually enforce
