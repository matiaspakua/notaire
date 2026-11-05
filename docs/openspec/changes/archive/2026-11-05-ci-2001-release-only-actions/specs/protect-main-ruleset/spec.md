## MODIFIED Requirements

### Requirement: Ruleset requires the five named status checks

The desired `protect-main` ruleset SHALL NOT require status checks that product workflows no longer report on a pull request. The contexts `CI`, `Frontend CI`, `Playwright E2E`, `Code Lint`, and `PR Validation` SHALL be absent from `security/rulesets/protect-main.desired.json`. Those job names SHALL remain in the workflow files so a version-tag run can still publish them. Applying the desired ruleset to GitHub is an Owner action after merge (#1040); this change SHALL NOT call the ruleset API.

#### Scenario: Ruleset lists the five exact check names

- **WHEN** `security/rulesets/protect-main.desired.json` required status checks are listed
- **THEN** the contexts `CI`, `Frontend CI`, `Playwright E2E`, `Code Lint`, and `PR Validation` are absent (pull requests no longer produce them)
- **AND** the file still requires a pull request and still blocks deletion and non-fast-forward pushes

#### Scenario: Aggregator jobs publish those check names

- **WHEN** the workflow files are inspected
- **THEN** jobs exist with `name:` values `CI`, `Frontend CI`, `Playwright E2E`, and `PR Validation` that `needs` their suite's blocking jobs, and `Code Lint` continues to exist as the PR Validation lint job name
