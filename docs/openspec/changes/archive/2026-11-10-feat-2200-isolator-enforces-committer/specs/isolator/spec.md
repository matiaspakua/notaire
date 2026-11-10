## ADDED Requirements

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
