## Purpose

A clean checkout of `main` passes the repository's own gates.

## ADDED Requirements

### Requirement: Source directories are never ignored

The repository SHALL NOT ignore application source directories, and generated pipeline output at the repo root MUST stay ignored.

#### Scenario: Reports source is tracked

- **WHEN** `git check-ignore` is run on `frontend/src/app/dashboard/reports/page.tsx`
- **THEN** the path is not ignored, while `reports/pipeline/*` still is

### Requirement: Reports page

The frontend SHALL serve `/dashboard/reports` with one card for each of the seven PDF reports.

#### Scenario: Reports page renders and downloads

- **WHEN** the page is opened and a budget id is entered
- **THEN** seven report cards are shown and the budget download calls the budget report with that id

### Requirement: Migrations run on fresh databases

All Flyway migrations SHALL apply to an empty database, including when a legacy table is absent.

#### Scenario: Fresh database migrates

- **WHEN** Flyway runs on an empty PostgreSQL
- **THEN** it succeeds and `identifications` exists with its composite key
