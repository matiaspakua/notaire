# Observability Specification

## Purpose
Optional TelemetryPort (OTel NoOp when enabled) and OpenFeature-shaped flag stub; secrets redacted; off by default.

## Requirements

### Requirement: TelemetryPort defaults off and redacts secrets
The kit SHALL expose a TelemetryPort that is disabled by default, binds OpenTelemetry NoOpTracerProvider when enabled with exporter none, redacts prompts diffs and secrets, and SHALL NOT claim GenAI semantic conventions.

#### Scenario: Default off
- **WHEN** telemetry.enabled is false
- **THEN** the port does not record or inject TRACEPARENT

#### Scenario: Redaction
- **WHEN** attributes include prompt, diff or secret-shaped keys
- **THEN** those keys are absent from emitted attributes

### Requirement: Env behavior proven against optional SDK
The kit SHALL prove OTEL_SDK_DISABLED yields non-recording spans and that TRACEPARENT is empty on the no-op path when the optional OpenTelemetry packages are present. Kit self-tests that assert the absence of TRACEPARENT inject SHALL isolate from a parent process that already exports TRACEPARENT or OTEL_* so the scenario remains evaluable in cloud agent shells.

#### Scenario: SDK disabled
- **WHEN** OTEL_SDK_DISABLED is true and telemetry is enabled with exporter none
- **THEN** spans are not recording and TRACEPARENT is empty

#### Scenario: Parent TRACEPARENT does not fail default-off proof
- **WHEN** the parent process exports TRACEPARENT and the default-off telemetry scenario runs
- **THEN** the test clears TRACEPARENT and OTEL_* for its own evaluation, the kit command still succeeds, and TRACEPARENT remains unset in that isolated environment afterwards

### Requirement: FeatureFlagPort stub fails closed on policy keys
The kit SHALL provide an OpenFeature-shaped stub where evaluation never throws and core.* / security.* flags cannot override invariants.

#### Scenario: Forbidden key
- **WHEN** flag key starts with core. or security.
- **THEN** the default is returned with reason ERROR
