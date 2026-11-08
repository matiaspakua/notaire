## Purpose

Define reliable, observable mode contracts for the dashboarded local pre-PR
pipeline that supports CU76 quality evidence.

## ADDED Requirements

### Requirement: Requested SonarQube enforcement is blocking

The pipeline MUST treat `--with-infra` and `--require-sonar` as requests for a
blocking SonarQube quality gate. A failed infrastructure start, an unavailable
SonarQube instance, or a failing analysis MUST make the pipeline exit non-zero.

#### Scenario: Infrastructure mode cannot silently skip SonarQube

- **WHEN** the developer runs `bash workspace/sdlc/run_pipeline.sh --with-infra`
- **THEN** the pipeline fails if SonarQube is not ready for analysis

#### Scenario: Required SonarQube is unavailable

- **WHEN** the developer runs `bash workspace/sdlc/run_pipeline.sh --require-sonar`
  while SonarQube is unavailable
- **THEN** the pipeline records a failed SonarQube phase and exits non-zero

### Requirement: Fast mode avoids server-backed work

The `--fast` mode MUST run only checks that do not require the application
Docker stack or a live API. It MUST omit application-stack startup and the DAST
baseline while continuing to report those phases as skipped in the dashboard.

#### Scenario: Fast mode skips Docker and DAST

- **WHEN** the developer runs `bash workspace/sdlc/run_pipeline.sh --fast`
- **THEN** the pipeline does not start the application stack or execute the
  DAST baseline and records both phases as skipped

### Requirement: Incompatible SonarQube flags are rejected

The pipeline MUST reject `--skip-sonar` when it is combined with either
`--with-infra` or `--require-sonar` before any quality gate runs.

#### Scenario: Conflicting SonarQube flags

- **WHEN** a developer combines `--skip-sonar` with `--with-infra` or
  `--require-sonar`
- **THEN** the command reports the conflict and exits with usage status 2
