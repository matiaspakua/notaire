# Inventory and classify legacy Sonar vulnerabilities (#1535 slice 1)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.


| Field | Value |
|-------|-------|
| GitHub Issue | #1535 (first ratcheting slice; umbrella stays open) |
| Use Case | CU78 – Security and Compliance |
| Branch | `cursor/chore-1535-sonar-legacy-vuln-inventory-2ea7` |
| Gate 1 status | ready (draft offline) |

## Objetivo

[#1535](https://github.com/matiaspakua/notaire/issues/1535) still tracks legacy Sonar findings. [#1717](https://github.com/matiaspakua/notaire/pull/1717) cleared several production blockers; [#1737](https://github.com/matiaspakua/notaire/issues/1737) ratchets **new** BLOCKER/CRITICAL/MAJOR. Overall vulnerabilities were never inventory-classified in-repo, so progress is not measurable and the umbrella AC cannot close.

Full triage (all bugs + smells) is too large for one PR. This slice delivers a durable inventory + classification for **vulnerability-type** issues and only small safe fixes.

## What Changes

- Script to export overall unresolved Sonar issues (BLOCKER/CRITICAL/MAJOR and/or type VULNERABILITY) for `notaire-backend` and `notaire-frontend`
- Checked-in inventory markdown with English classification columns
- Unit tests for the exporter (mocked API)
- Optional small true-defect vuln fixes with tests when safe and non-hexagon
- CHANGELOG Unreleased (Changed and/or Fixed)
- Note in `.claude/rules/code-quality.md` pointing at the inventory

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Legacy Sonar VULNERABILITY findings SHALL be listed in-repo and classified (fix, false_positive, accepted_risk, defer_hexagon) before the umbrella #1535 can close | CU78, #1535 | New (slice 1) |

## Capabilities

### New Capabilities

- `sonar-legacy-vuln-inventory`: Export and classify overall Sonar vulnerability / high-severity inventory for Notaire projects

### Modified Capabilities

- (none — `sonar-severity-ratchet` remains new-code only)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | maybe | only if a small classified `fix` lands in this PR |
| `frontend` | maybe | only if a small classified `fix` lands in this PR |
| `frontend-swing` | no | — |
| `infra` / observability | yes | inventory script (+ tests under `infra/tests` or `workspace/tests`) |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none (uses existing Sonar credentials)
- Dependencies: none

### Architecture review

No ADR. Hexagon `S4973` remains under #1693 (`defer_hexagon`).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Inventory markdown under `docs/300-development/` | New / regenerated |
| `.claude/rules/code-quality.md` | Pointer to inventory + refresh command |
| `CHANGELOG.md` | Unreleased Changed (and Fixed if product fixes) |

## Out of Scope

- Closing umbrella #1535 entirely
- jpa `S4973` / SpotBugs RC (#1693)
- SpotBugs/Checkstyle fail-build (#711/#710/#566)
- Mac #1740 re-smoke
- Mass smell cleanup
