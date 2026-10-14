<!-- Governed by CONSTITUTION.md. CU78, issue #712. Evidence: local trivy fs with the CI flags found 0 fixed CRITICAL/HIGH findings on 2026-10-14. -->

## ADDED Requirements

### Requirement: The Trivy source scan blocks on fixed CRITICAL and HIGH findings

The `security` job in `ci.yml` MUST run Trivy with severity CRITICAL,HIGH and exit code 1 over the sources with unfixed findings ignored, and MUST keep its report artifact when the scan fails. The local security gate MUST use the same severity and exit code. A `.trivyignore` entry MUST carry a justification comment.

#### Scenario: a fixed HIGH finding fails the build

- **WHEN** a dependency with a fixed HIGH vulnerability is present
- **THEN** the Trivy step exits 1 and the job fails

#### Scenario: the report survives a failure

- **WHEN** the Trivy step fails
- **THEN** the display and upload steps still run

#### Scenario: the local gate matches CI

- **WHEN** `security/run_gates.py` runs SEC-SCA-01
- **THEN** it scans CRITICAL,HIGH with exit code 1
