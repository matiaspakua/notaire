## Purpose
Isolator / sole-committer runtime boundary (ADR-004). OS containment when a real
backend probes; sole-committer custody via hooks/wrap. YAML is never a sandbox.

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

### Requirement: Isolator sole-committer enforcement
The Isolator SHALL enforce axiom 2 by intercepting `git commit` in an agent worktree
unless Owner/runtime holds an explicit commit grant (environment token
`AISDLC_COMMIT_GRANT` or equivalent). The agent MUST NEVER receive that grant.
Enforcement SHALL work without OS containment (`bwrap`): git `core.hooksPath`
custody (or equivalent wrap) is sufficient for `enforces_committer`. `sandbox`
SHALL remain independent — `sandbox=true` only after a successful containment
probe. `enforces_committer=true` SHALL be reported **only** after a proven
enforcement probe in which an attempted commit without the grant fails closed.
YAML/TOML/harness flags MUST NEVER set `enforces_committer=true`. When the custody
mechanism cannot be installed or the probe fails, Isolator MUST report
`enforces_committer=false` and degrade honestly. CLI (`aisdlc isolator status|probe`
and wrap/run as applicable) and `aisdlc doctor` SHALL surface this honesty.
`[committer] required` MUST NOT be flipped to `true` by this change.

#### Scenario: commit without grant fails under Isolator custody
- **WHEN** Isolator commit custody is active and `AISDLC_COMMIT_GRANT` is unset
- **THEN** `git commit` fails closed and no new commit is created

#### Scenario: enforces_committer true only after proven probe
- **WHEN** Isolator runs an enforcement probe that attempts a commit without the grant
- **THEN** the probe records a failing interception and status/probe JSON may set
  `enforces_committer` to true; without that proven failure, `enforces_committer` stays false

#### Scenario: enforces_committer works without bwrap
- **WHEN** `bwrap` is not available and Isolator commit custody hooks can be installed
- **THEN** a successful enforcement probe MAY set `enforces_committer=true` while
  `sandbox` remains false

#### Scenario: sandbox stays false without containment probe
- **WHEN** sole-committer enforcement succeeds but no OS containment probe succeeded
- **THEN** status/probe JSON still reports `sandbox=false`

#### Scenario: wrap blocks agent git commit
- **WHEN** `aisdlc isolator wrap -- <cmd>` (or equivalent) runs a command that invokes
  `git commit` without the commit grant
- **THEN** the commit is refused and the wrap does not claim sandbox unless containment ran

#### Scenario: grant allows Owner/runtime commit
- **WHEN** Isolator commit custody is active and `AISDLC_COMMIT_GRANT` is set
- **THEN** `git commit` is allowed (Owner/runtime path)

#### Scenario: doctor never claims enforces_committer without probe
- **WHEN** `aisdlc doctor` runs and the enforcement probe has not proven interception
- **THEN** doctor INFO does not claim `enforces_committer=true`
