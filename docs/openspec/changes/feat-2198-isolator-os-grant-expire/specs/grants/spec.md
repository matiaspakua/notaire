## MODIFIED Requirements

### Requirement: Optional grants in the profile
The profile SHALL accept optional `[[grants]]` entries. Each entry SHALL declare a
non-empty `id` and MAY declare `worktree` (string), `write_set` (list of path globs),
`required_checks` (list of gate names), `budget` (object with the same units as stage
effort: `tokens`, `time_seconds`, `questions`, `review_lines`), optional `stage`
(must match a declared gate stage when set), optional `expires_at` (ISO-8601 UTC
timestamp string), and optional `ttl_seconds` (positive integer). Omitted or empty
grants SHALL be a no-op. Expire fields are kill-switch data toward Isolator OS —
they MUST NOT alone claim sandbox isolation.

#### Scenario: grant expire fields parse and show
- **WHEN** a profile declares `[[grants]]` with `expires_at` and/or `ttl_seconds`
- **THEN** the kit loads those fields and surfaces them in `grant list --json` /
  `grant show --json` / `config show --redacted`

## ADDED Requirements

### Requirement: Grant expire fails closed
The kit SHALL evaluate Grant expire when `expires_at` and/or `ttl_seconds` is set.
A grant is expired when `expires_at` is in the past (UTC) or when `ttl_seconds` is
set and the evaluation clock is at or beyond the grant's effective deadline.
Expired grants SHALL fail closed for enforcement surfaces (`grant check` when the
only evaluable grants are expired; `isolator run`). Expire evaluation MUST NOT claim
Isolator OS shipped or set Isolator `sandbox=true`.

#### Scenario: expired grant fails closed
- **WHEN** a grant has `expires_at` in the past
- **THEN** expire evaluation reports expired/fail and enforcement surfaces refuse
  the grant (fail closed)

#### Scenario: ttl_seconds past fails closed
- **WHEN** a grant has `ttl_seconds` and the evaluation clock is past the effective
  deadline derived from that TTL
- **THEN** expire evaluation reports expired/fail (fail closed)
