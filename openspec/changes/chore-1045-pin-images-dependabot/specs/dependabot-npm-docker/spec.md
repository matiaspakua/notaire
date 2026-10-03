<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Ensure Dependabot version updates cover the frontend npm tree and Docker
Dockerfiles so image and JS dependency bumps arrive as PRs, not only Maven and
GitHub Actions. Source: #1045; CU78.

## ADDED Requirements

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
