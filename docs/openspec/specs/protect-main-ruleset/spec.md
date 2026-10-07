# protect-main-ruleset Specification

## Purpose

TBD - created by archiving change ci-1040-protect-main-ruleset. Update Purpose after archive.

## Requirements

### Requirement: Default branch ruleset enforces PR-only merges

The repository's active ruleset on the default branch (`protect-main`) MUST
require that changes reach `main` only through a pull request. Direct pushes
to `main` MUST be rejected for non-bypass actors.

#### Scenario: Ruleset requires pull request

- **WHEN** the live ruleset `protect-main` is inspected via the GitHub API
- **THEN** it includes a `pull_request` rule targeting the default branch /
  `main`, and a non-bypass actor cannot push commits directly to `main`

### Requirement: Ruleset requires the five named status checks

Merges into `main` MUST require these exact GitHub check-run names to pass:
`CI`, `Frontend CI`, `Playwright E2E`, `Code Lint`, and `PR Validation`.

#### Scenario: Ruleset lists the five exact check names

- **WHEN** the live ruleset `protect-main` required status checks are listed
- **THEN** the required check contexts include exactly the names `CI`,
  `Frontend CI`, `Playwright E2E`, `Code Lint`, and `PR Validation` (each
  present; no substitution by unrelated job titles)

#### Scenario: Aggregator jobs publish those check names

- **WHEN** the workflow files on `main` are inspected
- **THEN** jobs exist with `name:` values `CI`, `Frontend CI`,
  `Playwright E2E`, and `PR Validation` that `needs` their suite's blocking
  jobs, and `Code Lint` continues to exist as the PR Validation lint job name

### Requirement: Force-push and deletion remain blocked

The ruleset MUST continue to block force-pushes and deletion of the default
branch.

#### Scenario: Non-fast-forward and deletion rules present

- **WHEN** the live ruleset `protect-main` is inspected
- **THEN** it includes `non_fast_forward` and `deletion` rules (in addition to
  pull-request and required status checks)

### Requirement: Bot bypass is temporary and removed after #1041

Any ruleset bypass that exists solely so CI bots can commit reports to `main`
MUST be removed once #1041 has stopped those commits. After this change is
fully applied, bypass actors for that purpose MUST be empty.

#### Scenario: No durable bot bypass after #1041

- **WHEN** #1041 is merged and the #1040 ruleset apply step completes
- **THEN** `protect-main` does not grant a durable bypass to GitHub Actions
  (or a CI report token) for skipping PR / status-check rules on `main`

### Requirement: Hooks documentation reflects enforced protection

`.claude/rules/hooks.md` MUST NOT claim that GitHub branch protection /
rulesets are absent on `main`. It MUST describe the Claude push guard as
defense-in-depth alongside the active `protect-main` ruleset.

#### Scenario: hooks.md gap text updated

- **WHEN** `.claude/rules/hooks.md` is read after the change
- **THEN** it no longer states that protection returns 404 / is “not currently
  configured”, and it references the active `protect-main` ruleset as the
  GitHub-side control
