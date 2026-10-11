## Purpose

Prove from real traffic that the observability stack works.

## ADDED Requirements

### Requirement: Signals are checked

The validation SHALL check that all Prometheus targets are up, backend metrics arrive both by scrape and through the OTLP collector, the frontend exposes metrics, Loki holds backend and frontend request logs, log lines carry a trace id, Tempo holds backend traces, Alertmanager is wired, and the SonarQube projects exist.

#### Scenario: All signals present

- **WHEN** the stack has served traffic
- **THEN** every check reports PASS and the command exits zero

#### Scenario: A signal is missing

- **WHEN** a check finds no data
- **THEN** it reports FAIL with the reason and the command exits non-zero

### Requirement: Unavailable optional checks are skipped, not failed

A check that needs credentials the environment does not provide SHALL report SKIP with the reason.

#### Scenario: SonarQube is not authenticated

- **WHEN** SonarQube answers 401
- **THEN** the check reports SKIP
