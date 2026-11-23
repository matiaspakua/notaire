## MODIFIED Requirements

### Requirement: Dependabot PRs use allowed Conventional Commit types

Every Dependabot `commit-message.prefix` (and `prefix-development`, when set) MUST be a type accepted by both the PR title check in `pr-validation.yml` and the commit pattern of Notaire's profile (`.aisdlc/aisdlc.toml` `[commits] pattern`, checked by `aisdlc commits`), so a bot PR can pass the same gates as any other PR.

#### Scenario: Every Dependabot prefix is an allowed type

- **WHEN** each `updates[].commit-message` in `.github/dependabot.yml` is read
- **THEN** every `prefix` and `prefix-development` appears in the `TYPES` list of `pr-validation.yml` and in the types of the profile's `[commits] pattern`
