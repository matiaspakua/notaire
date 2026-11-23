## ADDED Requirements

### Requirement: TDD order is checked commit by commit when the profile asks

When `[tdd] order = true`, `aisdlc tdd` SHALL walk the non-merge commits since the base in order and fail when a
commit changes a production path before any commit has changed a test path, naming that commit's short hash and
subject. A commit that changes both counts as tests first. Without `order`, `aisdlc tdd` SHALL keep checking only
that a production change comes with a test change.

#### Scenario: production code committed before its test

- **GIVEN** `[tdd] order = true` and a commit changing a production path followed by a commit changing a test path
- **WHEN** `aisdlc tdd` runs
- **THEN** it exits 1 and names the production commit

#### Scenario: the test is committed first

- **GIVEN** `[tdd] order = true` and a commit changing a test path followed by a commit changing a production path
- **WHEN** `aisdlc tdd` runs
- **THEN** it exits 0

#### Scenario: order is not configured

- **GIVEN** no `[tdd] order` and a production commit followed by a test commit
- **WHEN** `aisdlc tdd` runs
- **THEN** it exits 0

### Requirement: A configured label waives TDD evidence

`aisdlc tdd` SHALL accept `--labels` (comma-separated labels of the change under review). When one of them is listed
in `[tdd] waive_labels`, the check SHALL exit 0 and name the label; a label not listed SHALL NOT waive it.

#### Scenario: the exception label waives the check

- **GIVEN** `[tdd] waive_labels = ["sdlc-exception"]` and a production change without tests
- **WHEN** `aisdlc tdd --labels chore,sdlc-exception` runs
- **THEN** it exits 0 and names `sdlc-exception`

#### Scenario: another label does not waive it

- **GIVEN** the same profile and change
- **WHEN** `aisdlc tdd --labels chore` runs
- **THEN** it exits 1
