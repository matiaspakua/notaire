# pipeline-green-on-main Specification

## Purpose

Make the pre-PR pipeline pass on a clean `main` with no bypass. Source: #1185; owner CU76.

## Requirements

### Requirement: Active OpenSpec changes have an open issue

Every change under `docs/openspec/changes/` (excluding `archive/`) MUST reference an issue that
is still open; completed work MUST be archived.

#### Scenario: No active change has a closed issue

- **WHEN** `bash workspace/sdlc/validate-sdlc-plan.sh` runs on the repository
- **THEN** it reports no `exists but is CLOSED` problem and exits 0

#### Scenario: Archived changes keep their content

- **WHEN** a change is archived
- **THEN** its proposal, design, tasks, traceability and specs exist unchanged under
  `docs/openspec/changes/archive/` and its delta specs are folded into `docs/openspec/specs/`
  or listed as skipped in the design

### Requirement: Gate scripts are portable

Scripts that run in the pipeline MUST work with both BSD (macOS) and GNU sed.

#### Scenario: Seed script fills values on BSD and GNU sed

- **WHEN** `workspace/sdlc/seed-openspec-change.sh` runs with `--issue`, `--use-case` and `--branch`
- **THEN** the seeded proposal and traceability contain those values on macOS and Linux

### Requirement: Each release has unique subsection headings

A release section of `CHANGELOG.md` MUST NOT repeat a `###` heading, and regrouping MUST NOT
add, remove or reword any entry.

#### Scenario: Each release has unique headings

- **WHEN** `docs/tests/test_changelog_structure.py` runs
- **THEN** no release section contains two identical `###` headings

#### Scenario: No changelog entry is lost

- **WHEN** the regrouped `CHANGELOG.md` is compared with the previous version
- **THEN** the multiset of non-heading, non-blank lines is identical

### Requirement: The pipeline needs no bypass

The pre-PR pipeline MUST pass on this branch without `PREFLIGHT_SKIP` or `--no-verify`.

#### Scenario: Pipeline passes on main without bypass

- **WHEN** `bash workspace/sdlc/run_pipeline.sh` runs on this branch
- **THEN** it exits 0 and `PREFLIGHT_SKIP` is not needed to push
