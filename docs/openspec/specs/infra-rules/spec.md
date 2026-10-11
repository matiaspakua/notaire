# infra-rules Specification

## Purpose
Keep the infra module self-contained.
## Requirements
### Requirement: Infra rules hold for the security reports service

The compose file SHALL NOT reference a path outside `infra/` without the exemption marker and an overridable variable, and the repository SHALL contain exactly one `nginx.conf`.

#### Scenario: Infra standalone guards pass

- **WHEN** `bash infra/verify.sh` runs
- **THEN** the self-containment, single nginx.conf and `.env.example` coverage guards pass

#### Scenario: The service still serves reports

- **WHEN** the observability stack starts
- **THEN** `http://localhost:8890/` and `/reports/` answer 200

