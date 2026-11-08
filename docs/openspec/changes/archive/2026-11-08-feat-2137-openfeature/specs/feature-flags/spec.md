## ADDED Requirements

### Requirement: In-process JSON provider
The kit SHALL provide a FeatureFlagPort with an in-process JSON/flagd file provider whose evaluation never throws and returns default plus reason.

#### Scenario: Corrupt file
- **WHEN** the flags file is corrupt
- **THEN** evaluation returns the default with reason ERROR

### Requirement: Flags cannot weaken invariants
Flags SHALL NOT disable required gates or raise factory.max_open_prs above the profile ceiling.

#### Scenario: Required gate skip flag
- **WHEN** gate.X.skip is true and gate X is required
- **THEN** the gate still runs

#### Scenario: PR ceiling flag
- **WHEN** factory.max_open_prs flag is above the profile ceiling
- **THEN** the effective cap remains the profile ceiling

### Requirement: Flags off by default
Notaire and templates SHALL default [flags] enabled = false.

#### Scenario: Default profile
- **WHEN** flags are disabled
- **THEN** config show uses the profile PR ceiling unchanged