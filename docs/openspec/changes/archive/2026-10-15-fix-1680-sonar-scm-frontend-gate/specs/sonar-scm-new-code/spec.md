## ADDED Requirements

### Requirement: Frontend Sonar scanner sees git SCM
The frontend Sonar analysis SHALL run with git SCM available so the Community new-code period is not the entire source tree.

#### Scenario: Monorepo mount exposes .git
- **WHEN** an operator runs `bash infra/scripts/run-sonar.sh` with SonarQube up
- **THEN** the frontend scanner container mounts the application checkout root (not only `frontend/`)
- **AND** analysis uses `sonar.scm.provider=git`
- **AND** the frontend quality gate can pass with zero new-code violations when the tip is clean

#### Scenario: Backend analysis uses notaire-backend key
- **WHEN** Maven Sonar analysis runs from the reactor
- **THEN** it publishes under project key `notaire-backend`
