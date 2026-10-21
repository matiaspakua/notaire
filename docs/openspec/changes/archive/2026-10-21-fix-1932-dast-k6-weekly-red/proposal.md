# Weekly DAST ZAP and k6 runs stay green on informational noise and artifact quota

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1932 |
| Use Case | CU78 — Security · CU74 — Performance and Caching |
| Branch | `fix/1932_dast_k6_weekly_red` |
| Gate 1 status | passed |

## Objetivo

The weekly scheduled runs on `main` went red on 2026-10-19. ZAP run 39236879790 reported
`WARN-NEW: Non-Storable Content [10049]` on three 404 answers (`/`, `/robots.txt`,
`/sitemap.xml`); the #1738 ratchet fails on any non-allowlisted WARN. k6 run 39232613714
passed its thresholds but failed on `Upload k6 results`: *Artifact storage quota has been hit*.

## What Changes

- `.zap/rules.tsv`: ignore informational rule 10049 with a reviewed reason. `fail_action: true` stays.
- `dast-zap.yml` and `performance-test.yml`: the report upload steps get `continue-on-error: true`, so an
  account-level artifact quota outage cannot turn a passing scan or load test red. The scan step still decides.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The ZAP allowlist holds only reviewed informational rules, each with a reason | #1738 ratchet | Made explicit (test enforces the reviewed set) |
| A report upload is evidence, not a gate | #1932 | New |

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none; CI configuration only (`skip_specs: true`).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github`) | yes | `dast-zap.yml`, `performance-test.yml` upload steps |
| `security` | yes | `.zap/rules.tsv` entry; `security/tests/test_zap_dast_ratchet.py` |
| `workspace` | no | – |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Configuration only; follows ADR-006 / #1738. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |

## Out of Scope

- Raising the account artifact quota (Owner, Settings → Billing).
- Making ZAP scan authenticated endpoints (separate issue if wanted).
