# dast-api-contract-backup-tests Specification

## Purpose
Add CI/test infrastructure for OWASP ZAP baseline DAST against the running
stack, committed OpenAPI with PR diff, and backup→restore→smoke verification
gated on automated backups (#256). Source: #1067; CU76 / CU78 / CU75.
## Requirements
### Requirement: OWASP ZAP baseline runs against the compose stack

The repository MUST provide an automated OWASP ZAP baseline (or equivalent
baseline DAST) that targets the running application stack and publishes a
report artifact. Trivy SCA MUST remain in place.

#### Scenario: ZAP baseline job executes successfully

- **WHEN** the DAST workflow/job runs against a healthy compose (or CI-equivalent)
  stack
- **THEN** ZAP baseline completes, produces a report artifact, and the job
  outcome follows the documented warn/fail policy

#### Scenario: Trivy SCA is not removed

- **WHEN** CI security jobs are inspected after this change
- **THEN** Trivy scanning remains configured (DAST is additive)

### Requirement: OpenAPI spec is committed and diffed on PRs

An OpenAPI document reflecting the backend API MUST be committed in the
repository, and pull requests MUST run a diff against that document so
breaking contract changes are visible (and fail the gate per the chosen
breaking-change ruleset).

#### Scenario: OpenAPI artifact is present in the repo

- **WHEN** a contributor checks out `main` after this change
- **THEN** a committed OpenAPI file exists at the documented path and matches
  the generation/export process

#### Scenario: PR OpenAPI diff job runs

- **WHEN** a pull request that touches the API (or always, per workflow design)
  is opened
- **THEN** an OpenAPI diff job runs and fails when breaking changes are
  introduced without updating the committed artifact per policy

### Requirement: Backup→restore→smoke is verified when backups exist

Once automated backups from #256 are available, CI or a scheduled workflow MUST
perform backup→restore→smoke and succeed. While #256 is not available, the
workflow MUST explicitly skip (or no-op with a clear log) rather than claim a
false green restore.

#### Scenario: Restore smoke runs after #256 backup mechanism exists

- **WHEN** #256 backup tooling is present on the branch under test
- **THEN** the backup→restore→smoke workflow executes and verifies the restored
  database answers a documented smoke check

#### Scenario: Restore smoke does not false-green without #256

- **WHEN** #256 backup tooling is absent
- **THEN** the backup→restore job skips or exits with an explicit “blocked on
  #256” signal and MUST NOT report a successful restore that did not happen

### Requirement: Permanent docs describe the new gates

DevSecOps / TEST-PLAN (and deployment docs for backup-restore) MUST describe
how to run and interpret ZAP, OpenAPI diff, and backup-restore verification.

#### Scenario: Docs no longer list ZAP/OpenAPI-diff as unimplemented future only

- **WHEN** DevSecOps and TEST-PLAN docs are read after this change
- **THEN** they document the landed DAST and OpenAPI-diff processes (and the
  #256 gate for backup-restore)

