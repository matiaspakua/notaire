# frontend-otel-browser-traces Specification

## Purpose
Browser OpenTelemetry traces for the Next.js frontend: opt-in export of
`notaire-frontend` spans through a same-origin OTLP BFF to the OTel collector
(CU77 / #1739).

## Requirements

### Requirement: Browser traces export through same-origin BFF

When `NEXT_PUBLIC_OTEL_TRACES_ENABLED` is true, the browser SHALL export OTLP traces to `/api/otlp/v1/traces`, which the Next.js server proxies to the OTel collector. The resource `service.name` SHALL be `notaire-frontend`.

#### Scenario: OTLP proxy forwards traces to collector URL

- **WHEN** a client POSTs an OTLP body to `/api/otlp/v1/traces` and `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` is set
- **THEN** the handler forwards the body upstream and does not require BACKEND_URL

#### Scenario: Tracing init is opt-in and names the service

- **WHEN** browser tracing is configured for export
- **THEN** service name is `notaire-frontend` and init is a no-op unless `NEXT_PUBLIC_OTEL_TRACES_ENABLED=true`
