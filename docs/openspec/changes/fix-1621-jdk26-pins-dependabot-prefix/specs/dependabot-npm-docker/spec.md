<!-- Governed by CONSTITUTION.md. CU78, issue #1621. Evidence: #1615/#1616 merged JDK 27 and a floating maven tag over red Process Checks; every Dependabot PR fails PR Validation on the `deps` prefix. -->

## ADDED Requirements

### Requirement: Dependabot PRs use allowed Conventional Commit types

Every Dependabot `commit-message.prefix` (and `prefix-development`, when set) MUST be a type accepted by both the PR title check in `pr-validation.yml` and `workspace/sdlc/check-commit-messages.sh`, so a bot PR can pass the same gates as any other PR.

#### Scenario: Every Dependabot prefix is an allowed type

- **WHEN** each `updates[].commit-message` in `.github/dependabot.yml` is read
- **THEN** every `prefix` and `prefix-development` appears in the `TYPES` list of both `pr-validation.yml` and `check-commit-messages.sh`

### Requirement: Dependabot does not bump the JDK major

The `/backend-api` docker ecosystem MUST ignore `version-update:semver-major` for `eclipse-temurin` and `maven`, because the JDK major is the project toolchain and changes only through a planned change to `pom.xml`, CI and both images.

#### Scenario: Backend docker entry ignores JDK majors

- **WHEN** the docker ecosystem entry with `directory: "/backend-api"` is read
- **THEN** its `ignore` list holds `eclipse-temurin` and `maven`, each with `version-update:semver-major` in `update-types`
