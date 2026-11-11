## MODIFIED Requirements

### Requirement: Env behavior proven against optional SDK
The kit SHALL prove OTEL_SDK_DISABLED yields non-recording spans and that TRACEPARENT is empty on the no-op path when the optional OpenTelemetry packages are present. Kit self-tests that assert the absence of TRACEPARENT inject SHALL isolate from a parent process that already exports TRACEPARENT or OTEL_* so the scenario remains evaluable in cloud agent shells.

#### Scenario: SDK disabled
- **WHEN** OTEL_SDK_DISABLED is true and telemetry is enabled with exporter none
- **THEN** spans are not recording and TRACEPARENT is empty

#### Scenario: Parent TRACEPARENT does not fail default-off proof
- **WHEN** the parent process exports TRACEPARENT and the default-off telemetry scenario runs
- **THEN** the test clears TRACEPARENT and OTEL_* for its own evaluation, the kit command still succeeds, and TRACEPARENT remains unset in that isolated environment afterwards
