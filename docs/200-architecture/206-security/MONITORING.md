# Security monitoring and dashboard

Ops by Design for CU78, following the specialization guide §3.5 and §4.7.
Signals that exist today are login outcomes, availability, and logs. A
vulnerability count from GitHub is not scraped into Grafana.

## Signals

| Question | Signal | Where |
|----------|--------|-------|
| Are logins failing? | `notaire_operation_total{operation="login",status=...}` | Micrometer, Prometheus |
| Is lockout tripping? | `status="locked"` | `LoginLockoutSpike` in `infra/observability/prometheus/alert-rules.yml` |
| Is the API up? | `up`, `probe_success` | Existing stack alerts |
| What did a request do? | method, path, status, duration, trace id | `RequestLoggingFilter`, Loki, Tempo |
| Which control failed in CI? | Secret scan, Semgrep, CodeQL, Trivy artifact, security case report | GitHub Actions artifact `security-case-report` |
| Which local case failed before the PR? | `reports/security/latest/report.json` | `python3 security/run_gates.py --profile local` |

Login statuses emitted by `UserController`: `success`, `bad_credentials`,
`inactive`, `not_found`, `locked`, `error`.

## Alerts

| Alert | Meaning | Severity |
|-------|---------|----------|
| `HighLoginFailureRate` | More than half of logins fail for 2 minutes | warning |
| `SuspiciousLoginActivity` | Bad-credential rate stays high for 1 minute | critical |
| `LoginLockoutSpike` | Any locked-login rate over 5 minutes, for 1 minute | warning |

Alertmanager routes them to the local UI (#1465). No mail or chat receiver is
configured, because that needs credentials the owner has not provided.
Critical inhibits warning when the `service` label matches. Login alerts do
not set `service`, so they are not suppressed by a host-down warning on
another label.

## Dashboard

Grafana provisions `notaire-security`
(`infra/observability/grafana/provisioning/dashboards/notaire-security.json`).

Panels:

- Login failure ratio over 5 minutes.
- Bad-credential and locked counts over 1 hour.
- Firing security alerts (`HighLoginFailureRate`, `SuspiciousLoginActivity`,
  `LoginLockoutSpike`).
- A link back to `notaire-auth` for the outcome time series.

The Notaire status dashboard links here. Empty Prometheus results render as
no-data. That is a quiet system or a missing scrape, and the panel stays
usable either way.

Dependency CVEs stay on the GitHub Security tab and the Trivy artifact. This
dashboard does not invent a CVE panel without an exporter.

## Abnormal situations

When an alert fires, the operator records the CU77 incident note: start, end,
who was affected, what failed, and what will change in code or in the threat
model. That is the guide §3.7 template, kept short on purpose.

## What this does not monitor

- Read access to `Person` (SR-08).
- Object-level authorization failures, until those checks exist (SR-11).
- Container syscalls (Falco is a later candidate).
- General API rate-limit breaches (#280).
