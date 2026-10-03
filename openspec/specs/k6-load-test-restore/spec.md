# k6-load-test-restore Specification

## Purpose
Restore the deleted k6 load-test script and keep the weekly Performance
workflow green with SLO-tied thresholds and a published results artifact.
Source: #1047; owners CU74 (performance SLOs) and CU76 (QA/CI infrastructure).
## Requirements
### Requirement: k6 load-test script is present and API-current

The repository MUST contain `performance-test/k6/load-test.js` (path referenced
by `.github/workflows/performance-test.yml`). The script MUST authenticate via
`POST /api/v1/usuarios/login` using the English DTO fields `name` and
`password`, reuse a Bearer JWT, and exercise the highest-traffic read
endpoints `/api/v1/gestiones`, `/api/v1/presupuestos`, and `/api/v1/tramites`.

#### Scenario: k6 script file exists at workflow path

- **WHEN** the Performance workflow or asset validator resolves
  `performance-test/k6/load-test.js`
- **THEN** the file exists and is a valid k6 script importing `k6/http`

#### Scenario: Script uses English login DTO fields

- **WHEN** the script's `setup()` performs login
- **THEN** the JSON body uses `name` and `password` (not Spanish
  `nombre`/`contrasenia`) against `/api/v1/usuarios/login`

#### Scenario: Script covers gestiones presupuestos and tramites with Bearer auth

- **WHEN** the default VU function runs after setup
- **THEN** it GETs `/api/v1/gestiones`, `/api/v1/presupuestos`, and
  `/api/v1/tramites` with an `Authorization: Bearer` header from the setup token

### Requirement: Thresholds are tied to CU74 SLOs

The k6 `options` MUST declare `stages` and `thresholds` that enforce CU74
latency and reliability budgets for the load profile: `http_req_duration` p95
MUST be bounded by the CU74 objective (p95 &lt; 2s), and `http_req_failed`
MUST stay under 1% (`rate&lt;0.01`).

#### Scenario: Script declares stages and SLO thresholds

- **WHEN** a reviewer or asset test inspects `export const options`
- **THEN** it includes `stages`, `thresholds.http_req_duration` with a p95
  bound at or tighter than 2000ms, and `thresholds.http_req_failed` with
  `rate&lt;0.01`

### Requirement: Weekly workflow is green and publishes results

`.github/workflows/performance-test.yml` MUST continue to run on schedule and
`workflow_dispatch` (not on every PR), invoke k6 against a live local stack,
succeed when the restored script and thresholds pass, and upload a results
artifact that is actually produced by the run.

#### Scenario: Workflow wires k6 against a live stack on schedule

- **WHEN** the Performance workflow YAML is validated
- **THEN** it defines `schedule` and `workflow_dispatch`, does not gate
  `pull_request`, references `performance-test/k6/load-test.js`, and waits on
  backend `actuator/health` before k6

#### Scenario: Results published as artifact

- **WHEN** a k6 run completes (success or threshold failure after summary write)
- **THEN** the job produces `summary.json` (or the configured artifact path)
  and the upload-artifact step publishes it (not skipped solely because the
  file is missing)

#### Scenario: Asset unittest suite green

- **WHEN** `python3 scripts/test_performance_test_assets.py` runs on the
  restored tree
- **THEN** every assertion passes (script presence, stages/thresholds, English
  login, endpoint coverage, summary output, workflow wiring)

