## ADDED Requirements

### Requirement: Security gates stay enabled

The local pipeline SHALL run the security case catalog and the DAST baseline.
The CI security job SHALL run the secret scan and the case catalog without
`continue-on-error`, and SHALL NOT depend on the Java build job. CodeQL SHALL
run on pull requests. The ZAP workflow SHALL keep its schedule.

#### Scenario: CI security does not wait for the Java build

- **WHEN** an engineer reads the security job in `.github/workflows/ci.yml`
- **THEN** its needs list includes `changes`, does not include `build`, and the secret scan and `security/run_gates.py --profile ci` have no `continue-on-error`

#### Scenario: CodeQL and ZAP workflows are not disabled

- **WHEN** an engineer reads `codeql.yml` and `dast-zap.yml`
- **THEN** CodeQL triggers on `pull_request` and ZAP has a `schedule`, and neither file contains `if: false`

### Requirement: The local report is a service on the infra hub

The observability stack SHALL serve the security case report on port 8890.
Homer SHALL link to that service. Prometheus SHALL probe it. The landing page
SHALL link to `reports/latest/index.html`.

#### Scenario: Homer and the probe name the report service

- **WHEN** an engineer reads the observability compose, Homer config, and Prometheus config
- **THEN** a `security-reports` service publishes `8890:80`, Homer contains `http://localhost:8890`, and Prometheus labels a probe `security-reports`

### Requirement: CI shows the case summary

The repository SHALL render the case report as Markdown that lists each case id
and status and does not include a secret value. The security job SHALL append
that summary to the Actions run.

#### Scenario: Summary lists status and hides secrets

- **WHEN** the summary renderer receives a passing secret-scan case
- **THEN** the text contains the case id and `PASS` and does not contain `ghp_`
