## ADDED Requirements

### Requirement: Notaire way fails on new high-severity violations

`infra/scripts/run-sonar.sh` SHALL configure the Notaire way quality gate so that `new_blocker_violations`, `new_critical_violations`, and `new_major_violations` fail the gate when greater than zero.

#### Scenario: Script declares severity gate metrics

- **WHEN** `run-sonar.sh` is read
- **THEN** it configures quality gate conditions for `new_blocker_violations`, `new_critical_violations`, and `new_major_violations` with error threshold 0

### Requirement: Severity report helper exists

The repo SHALL provide `infra/scripts/sonar-severity-report.py` that queries local Sonar for CRITICAL/MAJOR/BLOCKER issue counts on `notaire-backend` and `notaire-frontend` (for Mac/local evidence).

#### Scenario: Report script is present and names both projects

- **WHEN** the severity report script is inspected
- **THEN** it targets `notaire-backend` and `notaire-frontend` and requests severities BLOCKER, CRITICAL, and MAJOR
