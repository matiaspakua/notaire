<!-- Governed by CONSTITUTION.md. CU77, issue #1660. Evidence: both promtool/amtool tests skip without Docker; the pinned binaries accept the current files. -->

## ADDED Requirements

### Requirement: Alert rules and Alertmanager config are validated without Docker

When no Docker daemon is available, the infra tests MUST validate `alert-rules*.yml` with a local `promtool` and `alertmanager.yml` with a local `amtool`, and MUST skip only when neither Docker nor the local binary exists. The toolchain hook MUST install both binaries at the versions of the image tags in `infra/observability/docker-compose.yml` and `infra/tests/test_alertmanager.py`.

#### Scenario: local promtool rejects a broken rule file

- **WHEN** a local `promtool` checks a rule file with an invalid expression
- **THEN** the validation helper reports a non-zero exit

#### Scenario: local amtool rejects a broken config

- **WHEN** a local `amtool` checks an Alertmanager config with an unknown key
- **THEN** the validation helper reports a non-zero exit

#### Scenario: hook pins equal the compose image tags

- **WHEN** the hook's promtool and amtool versions are compared with the image tags
- **THEN** they are equal
