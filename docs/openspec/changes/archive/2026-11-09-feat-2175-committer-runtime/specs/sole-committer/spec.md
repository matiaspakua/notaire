## MODIFIED Requirements

### Requirement: Sole-committer dry-run policy check
The system SHALL provide `aisdlc committer` that inspects commits since a base for
authors matching profile `[committer]` agent deny patterns (name, email, message
trailer) and optional runtime allowlists. The check SHALL be **no-evaluable** unless
at least one of `runtime_names` or `runtime_emails` is configured (opt-in activation).
When Notaire’s profile configures Owner `runtime_*`, the check SHALL be evaluable
while `required` remains false unless the Owner later enables fail-closed.
The command SHALL report status `pass`, `fail`, or `no-evaluable`, SHALL exit non-zero
for `fail` only when `required = true`, SHALL NOT rewrite git history, SHALL NOT write
to a forge, and SHALL NOT claim sandbox isolation.

#### Scenario: Unconfigured profile is no-evaluable
- **WHEN** the profile has no `[committer]` runtime allowlist configured
- **THEN** `aisdlc committer` reports `no-evaluable` and exits 0

#### Scenario: Agent patterns alone stay no-evaluable
- **WHEN** agent deny patterns are set but `runtime_names` and `runtime_emails` are empty
- **THEN** `aisdlc committer` reports `no-evaluable` and exits 0

#### Scenario: Agent-authored commit fails when required
- **WHEN** a runtime allowlist is configured, `required = true`, and a commit since base matches an agent name/email/trailer
- **THEN** the command reports `fail` and exits 1

#### Scenario: Advisory mode exits zero on fail
- **WHEN** a runtime allowlist is configured, `required = false`, and an agent-authored commit is found
- **THEN** the command reports `fail` but exits 0

#### Scenario: Runtime-allowed author passes
- **WHEN** a runtime allowlist is configured and commits since base match it with no agent pattern
- **THEN** the command reports `pass` and exits 0

#### Scenario: Notaire Owner runtime makes committer evaluable and advisory
- **WHEN** Notaire’s `.aisdlc` profile sets Owner `runtime_names` / `runtime_emails` and `required = false`
- **THEN** `aisdlc committer` is not `no-evaluable` and exits 0 even if status is `fail`
