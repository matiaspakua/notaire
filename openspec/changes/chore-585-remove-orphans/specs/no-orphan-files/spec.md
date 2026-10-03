<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Keep unreachable QA scripts out of `testing/`. Source: #585; owner CU76.

## ADDED Requirements

### Requirement: QA scripts are reachable

Every tracked shell script under `testing/` outside `e2e-swing/` MUST be invoked by
`testing/scripts/run.sh` or by another script under `testing/`, unless it is on the documented
exemption list (the stable `test.sh` wrapper and the CI-only coverage script).

#### Scenario: No orphaned script under testing

- **WHEN** the scripts under `testing/` are scanned for callers
- **THEN** every script has a caller or is on the exemption list

#### Scenario: Removed cURL scripts are gone

- **WHEN** the tracked files are listed
- **THEN** `testing/integration/http/` contains only `test-all-endpoints-v2.sh`

### Requirement: Removal does not break references

No active file outside the archives MUST reference a removed path.

#### Scenario: No live reference to a removed path

- **WHEN** tracked files outside `docs/000-archive`, `openspec/changes/archive` and
  `CHANGELOG.md` are searched for the removed script names
- **THEN** none is found, except in historical OpenSpec specs and this change
