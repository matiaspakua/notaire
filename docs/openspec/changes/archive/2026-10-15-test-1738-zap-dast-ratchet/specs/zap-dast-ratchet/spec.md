## ADDED Requirements

### Requirement: ZAP baseline fails on non-allowlisted alerts

The weekly OWASP ZAP baseline workflow SHALL set `fail_action` to true and SHALL load a repository rules allowlist. Alerts that are not allowlisted SHALL fail the job.

#### Scenario: Workflow enables fail_action with rules file

- **WHEN** `.github/workflows/dast-zap.yml` is read
- **THEN** it configures `zaproxy/action-baseline` with `fail_action: true` and `rules_file_name` pointing at `.zap/rules.tsv`

#### Scenario: Sec-Fetch informational rules are allowlisted

- **WHEN** `.zap/rules.tsv` is read
- **THEN** it IGNORE-s alert id 90005 (Sec-Fetch-* headers) with a reason suitable for an unauthenticated API baseline
