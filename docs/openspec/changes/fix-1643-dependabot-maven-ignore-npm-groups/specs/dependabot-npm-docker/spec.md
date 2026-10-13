<!-- Governed by CONSTITUTION.md. CU78, issue #1643. Evidence: #1629 proposed maven:3-eclipse-temurin-27-alpine despite the #1621 semver-major ignore; 5 single-package npm PRs serialized behind strict required checks. -->

## ADDED Requirements

### Requirement: Dependabot ignores every maven builder update

The `/backend-api` docker ecosystem MUST ignore all updates of the `maven` image (no `update-types` restriction), because Dependabot cannot read its `<maven>-eclipse-temurin-<jdk>` tag as semver, so a semver-major rule cannot keep the JDK fixed.

#### Scenario: maven ignore has no update-type filter

- **WHEN** the `ignore` list of the `/backend-api` docker entry is read
- **THEN** it holds a `maven` rule without `update-types`

### Requirement: npm minor and patch updates arrive as one PR

The `/frontend` npm ecosystem MUST group all minor and patch updates into a single Dependabot PR, leaving major updates as separate PRs, so strict required checks do not force one rebase and full CI run per package.

#### Scenario: frontend npm group covers minor and patch only

- **WHEN** the `groups` of the `/frontend` npm entry are read
- **THEN** one group matches pattern `*` with update types `minor` and `patch`, and not `major`
