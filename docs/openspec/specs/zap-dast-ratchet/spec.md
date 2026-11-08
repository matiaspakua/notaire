# zap-dast-ratchet Specification

## Purpose
Blocking weekly OWASP ZAP baseline with a reviewed `.zap/rules.tsv` allowlist. Source: #1738; owner CU78.

## Requirements

### Requirement: ZAP baseline fails on non-allowlisted alerts

The weekly OWASP ZAP baseline workflow SHALL set `fail_action` to true and SHALL load a repository rules allowlist. Alerts that are not allowlisted SHALL fail the job.

#### Scenario: Workflow enables fail_action with rules file

- **WHEN** `.github/workflows/dast-zap.yml` is read
- **THEN** it configures `zaproxy/action-baseline` with `fail_action: true` and `rules_file_name` pointing at `.zap/rules.tsv`

#### Scenario: Sec-Fetch informational rules are allowlisted

- **WHEN** `.zap/rules.tsv` is read
- **THEN** it IGNORE-s alert id 90005 (Sec-Fetch-* headers) with a reason suitable for an unauthenticated API baseline

### Requirement: ZAP allowlist holds only reviewed informational rules

`.zap/rules.tsv` SHALL list only reviewed informational alert ids, each as `IGNORE` with a written reason. Informational rule 10049 (Non-Storable Content) SHALL be ignored, because the API's 404 answers are meant not to be stored by shared caches.

#### Scenario: Non-Storable Content is allowlisted with a reason

- **WHEN** `.zap/rules.tsv` is read
- **THEN** it IGNORE-s alert id 10049 with a parenthesised reason

#### Scenario: An unreviewed allowlist entry fails the guard

- **WHEN** an entry whose id is outside the reviewed set is added to `.zap/rules.tsv`
- **THEN** `security/tests/test_zap_dast_ratchet.py` fails

### Requirement: Report uploads are evidence, not a gate

The report upload steps of the weekly DAST and k6 workflows SHALL run `if: always()` with `continue-on-error: true`. The scan or test step SHALL alone decide the job result.

#### Scenario: Artifact quota outage does not fail a passing run

- **WHEN** `dast-zap.yml` or `performance-test.yml` is read
- **THEN** every `actions/upload-artifact` step sets `if: always()` and `continue-on-error: true`
