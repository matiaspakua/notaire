# dependabot-npm-docker Specification

## Purpose

Ensure Dependabot version updates cover the frontend npm tree and Docker
Dockerfiles so image and JS dependency bumps arrive as PRs, not only Maven and
GitHub Actions. Source: #1045; CU78.

## Requirements

### Requirement: Dependabot covers npm under frontend

The repository Dependabot configuration MUST include a `package-ecosystem`
entry for **npm** whose directory is **`/frontend`**. If that entry already
exists, it MUST be retained (not removed or duplicated).

#### Scenario: npm ecosystem targets frontend

- **WHEN** `.github/dependabot.yml` is inspected
- **THEN** it contains exactly one npm ecosystem entry with
  `directory: "/frontend"` (or equivalent path that Dependabot resolves to the
  Next.js app)

#### Scenario: existing maven and github-actions ecosystems remain

- **WHEN** `.github/dependabot.yml` is inspected after the change
- **THEN** the maven (`directory: "/"`) and github-actions ecosystems are still
  present

### Requirement: Dependabot covers docker for application Dockerfiles

The repository Dependabot configuration MUST include **docker** ecosystem
entries for every directory that owns an application `Dockerfile` used to build
the product images (at minimum `/backend-api` and `/frontend`).

#### Scenario: docker ecosystem for backend-api

- **WHEN** `.github/dependabot.yml` is inspected
- **THEN** it contains a docker ecosystem entry with
  `directory: "/backend-api"`

#### Scenario: docker ecosystem for frontend

- **WHEN** `.github/dependabot.yml` is inspected
- **THEN** it contains a docker ecosystem entry with
  `directory: "/frontend"`

#### Scenario: no duplicate docker directory entries

- **WHEN** all docker ecosystem entries in `.github/dependabot.yml` are listed
- **THEN** each `directory` value appears at most once

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
