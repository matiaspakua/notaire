# Refresh Sonar legacy inventory from live analysis (#1760)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1760 |
| Use Case | CU78 – Security and Compliance / CU76 – Quality Assurance |
| Branch | `cursor/1760-sonar-live-inventory-refresh-69d3` |
| Gate 1 status | ready |

## Objetivo

Slice 1 ([#1756](https://github.com/matiaspakua/notaire/pull/1756)) seeded `SONAR-LEGACY-VULN-INVENTORY.md` with zero `VULNERABILITY`-typed rows because CI/cloud had no Sonar. Mac/local must run `run-sonar.sh` and rewrite the inventory from live `localhost:9000` so umbrella [#1535](https://github.com/matiaspakua/notaire/issues/1535) can classify real findings and open `fix` child issues.

## What Changes

- Live Sonar analysis (backend + frontend) against localhost:9000
- Regenerated `docs/300-development/SONAR-LEGACY-VULN-INVENTORY.md` with live rows + refresh provenance
- Heuristics for known live vuln clusters (`secrets:S6703`, `java:S4790`, `typescript:S5332`)
- Child issue for the remaining `fix` cluster (legacy MD5 / `java:S4790`)
- `run-sonar.sh` Docker network auto-detect (`notaire_devsecops-network` vs `infra_…`)
- Unit tests for heuristics + refresh note (CHANGELOG-free while peer hex work continues)

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Overall Sonar VULNERABILITY findings SHALL be inventoried from a live analysis when Sonar is available on Mac/local, not only from seed residuals | CU78, #1535 / #1760 | Changed (live refresh) |
| Each live VULNERABILITY row SHALL be classified (`fix` / `false_positive` / `accepted_risk` / `defer_hexagon`) with an English rationale; `fix` clusters get child issues | CU78, #1535 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `sonar-legacy-vuln-inventory`: Live refresh provenance and classification heuristics for known vuln clusters

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | product code unchanged in this PR |
| `frontend` | no | product code unchanged in this PR |
| `frontend-swing` | no | — |
| `infra` / observability | yes | inventory script, `run-sonar.sh` network detect, inventory markdown, unit tests |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: local Sonar token may regenerate on Mac (not committed)
- Dependencies: none

### Architecture review

No ADR. Hexagon `S4973` remains `defer_hexagon` (#1758). Do not reopen #1740.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/SONAR-LEGACY-VULN-INVENTORY.md` | Regenerated from live Sonar |
| `CHANGELOG.md` | n/a — CHANGELOG-free for this ops/quality refresh |

## Out of Scope

- Closing umbrella #1535
- Implementing the MD5 removal (#1762) in this PR
- Reopening #1740
- Fixing bulk CODE_SMELL MAJOR rows beyond inventory triage
