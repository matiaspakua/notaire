## ADDED Requirements

### Requirement: Sole-committer dry-run policy check
The system SHALL provide `aisdlc committer` that inspects commits since a base for
authors matching profile `[committer]` agent deny patterns (name, email, message
trailer) and optional runtime allowlists. The command SHALL report status
`pass`, `fail`, or `no-evaluable`, SHALL exit non-zero for `fail` only when
`required = true`, SHALL NOT rewrite git history, SHALL NOT write to a forge, and
SHALL NOT claim sandbox isolation.

#### Scenario: Unconfigured profile is no-evaluable
- **WHEN** the profile has no `[committer]` deny/allow patterns configured
- **THEN** `aisdlc committer` reports `no-evaluable` and exits 0

#### Scenario: Agent-authored commit fails when required
- **WHEN** `required = true` and a commit since base matches an agent name/email/trailer
- **THEN** the command reports `fail` and exits 1

#### Scenario: Advisory mode exits zero on fail
- **WHEN** `required = false` and an agent-authored commit is found
- **THEN** the command reports `fail` but exits 0

#### Scenario: Runtime-allowed author passes
- **WHEN** commits since base match the configured runtime author allowlist and no agent pattern
- **THEN** the command reports `pass` and exits 0
