# Security reports on the infra hub

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1506 |
| Use Case | CU78 — Security and Compliance |
| Branch | `cursor/security-report-service-81d6` |
| Gate 1 status | passed |

## Objetivo

The case catalog from #1500 runs, but the HTML report is only a gitignored file
and a CI artifact. The security job also waits on the Java build, so a pull
request that skips that build skips the security gates. This change keeps every
security gate enabled and publishes the local report on the infra hub.

## What Changes

- The CI security job depends on the path-filter job, not on the Java build, and
  appends the case summary to the Actions run.
- A nginx service `security-reports` on port 8890 serves `reports/security`.
  Homer, the blackbox probe, and the stack-status dashboard include it.
- CodeQL and the weekly ZAP workflow stay enabled. Trivy and ZAP stay
  report-only. GitHub Pages is not added to `ci.yml`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Security gates that already exist stay enabled on every pull request that CI starts | CU78 | Made explicit |
| The local case report is reviewable from the infra hub | CU78 | New |
| The Actions run shows the same case summary; a hosted remote site is later | CU78 | New |

## Capabilities

### New Capabilities

- `security-report-service`: the infra page and the CI summary for the case catalog

### Modified Capabilities

- None

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | No product code |
| `frontend` | no | No product UI |
| `infra` / observability | yes | nginx service, Homer link, probe, Grafana panel |
| CI/CD (`.github/workflows`) | yes | Security job no longer needs the Java build; job summary |
| `security` | yes | `publish_summary.py` and the presence test |
| `docs` | yes | Monitoring, definition, CU78, changelog |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: pinned `nginx:1.27.5-alpine` in the observability compose only

### Architecture review

The service follows the existing observability compose, Homer hub, and blackbox
probe pattern. No ADR. The report directory stays gitignored.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/MONITORING.md` | Local URL and CI summary |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | Pointer to port 8890 |
| `infra/docs/DEFINITION.md` | New service row |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Pointer |
| `CHANGELOG.md` | One bullet under the existing Security heading |

## Out of Scope

- Failing CI on Trivy findings (#282) or on ZAP alerts (#281)
- Publishing the HTML to GitHub Pages (`ci.yml` must not gain `pages` permission)
- Object-level authorization (SR-11)
