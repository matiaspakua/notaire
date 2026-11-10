## Purpose
Design-only Isolator / sole-committer runtime boundary (ADR-004). Not an OS sandbox.

## Requirements

### Requirement: Isolator runtime boundary is design-only until evaluable
The system SHALL document an Isolator / sole-committer **runtime** boundary (grants,
write set, agent never commits) that is distinct from the `aisdlc committer` **policy**
dry-run (#2167/#2169) and from Grant write-set / expire policy checks. Until a real
OS Isolator containment backend successfully probes, Isolator status SHALL be
`no-evaluable`, SHALL report `sandbox=false`, and SHALL NOT claim isolation from
YAML, harness adapters, or profile flags. When a real backend (prefer Linux
`bwrap`) is present and a probe succeeds, Isolator MAY report `sandbox=true` with
`backend` naming that backend; when the backend is absent, Isolator MUST degrade
honestly (`backend=none` or equivalent, `sandbox=false`). `aisdlc doctor` SHALL
surface Isolator honesty without failing the profile solely for Isolator being
unevaluable when no backend is available.

#### Scenario: Isolator stub is no-evaluable and never a sandbox
- **WHEN** no OS Isolator containment backend is available (for example `bwrap` missing)
- **THEN** Isolator status is `no-evaluable` and `sandbox` is false

#### Scenario: Doctor reports Isolator no-evaluable without failing for it
- **WHEN** `aisdlc doctor` runs on a valid profile without a working Isolator backend
- **THEN** output mentions Isolator `no-evaluable` (or honest unavailable) and exit
  code is not 1 solely because Isolator is unevaluable

#### Scenario: Policy dry-run remains separate
- **WHEN** documenting Isolator
- **THEN** the ADR cites `aisdlc committer` and `aisdlc grant check` as policy checks
  Isolator must eventually enforce; expire schema alone is not Isolator OS complete

### Requirement: Isolator OS probe and optional run
The kit SHALL provide `aisdlc isolator status|probe|run` (JSON optional) that reports
honesty fields: `status`, `sandbox`, `backend`, `enforces_committer`, and detail.
`probe` SHALL detect whether a real containment backend (prefer `bwrap`) can contain
a trivial probe. `run` SHALL optionally execute a command under containment constrained
by Grant `write_set` when the backend is available and grants are not expired; when
the backend is missing, `run` MUST refuse without claiming sandbox. YAML/TOML alone
MUST NEVER set `sandbox=true`.

#### Scenario: isolator status without bwrap is honest
- **WHEN** `bwrap` is not available on PATH
- **THEN** `aisdlc isolator status --json` reports `sandbox=false` and does not claim
  a working OS sandbox

#### Scenario: isolator probe reports backend and sandbox
- **WHEN** `aisdlc isolator probe --json` runs
- **THEN** the payload includes `backend` and `sandbox` reflecting the real probe
  result (`sandbox=true` only if containment succeeded)

#### Scenario: isolator run refuses when sandbox unavailable
- **WHEN** no containment backend is available and `aisdlc isolator run` is invoked
- **THEN** the command exits non-zero without claiming the command ran under sandbox

#### Scenario: isolator run refuses expired grant
- **WHEN** all candidate grants are expired and `aisdlc isolator run` is invoked
- **THEN** the command fails closed and does not claim sandbox success

#### Scenario: doctor never claims sandbox without probe success
- **WHEN** `aisdlc doctor` runs without a successful Isolator probe
- **THEN** doctor INFO does not claim `sandbox=true`
