## ADDED Requirements

### Requirement: Grant write-set policy dry-run
The kit SHALL provide `aisdlc grant check [--base] [--json]` that compares changed
paths since `base` (default profile base) against the union of declared Grant
`write_set` globs. Matching SHALL use the same path-glob honesty as scope write-set
checks (`repo.matches` / directory expansion). This is a **policy** dry-run only:
it MUST NOT claim OS isolation, sandbox, bubblewrap, landlock, or Isolator runtime
enforcement.

#### Scenario: no grant declared is no-evaluable
- **WHEN** a profile has no `[[grants]]` (or every grant has an empty `write_set`)
- **THEN** `grant check --json` exits 0 with status `no-evaluable` and does not claim sandbox

#### Scenario: paths inside write_set pass
- **WHEN** a profile declares a Grant with a non-empty `write_set` and every changed
  path since base matches those globs
- **THEN** `grant check --json` reports status `pass`

#### Scenario: path outside write_set fails
- **WHEN** at least one changed path since base is outside every Grant `write_set`
- **THEN** `grant check --json` reports status `fail` and names the outside path(s)

#### Scenario: doctor denies sandbox for grant check
- **WHEN** `aisdlc doctor` runs with or without grants declared
- **THEN** doctor INFO states Grant write-set check is policy-only / not a sandbox,
  and Isolator remains `no-evaluable` with `sandbox=false`
