> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

[#1756](https://github.com/matiaspakua/notaire/pull/1756) shipped the exporter + seed inventory. #1760 is the Mac/local live refresh: Sonar was previously unreachable from cloud workers.

## Goals / Non-Goals

**Goals:** Live overall inventory markdown; every VULNERABILITY row classified; child issue for remaining `fix`; tests for heuristics; reliable `run-sonar.sh` network selection on Mac.  
**Non-Goals:** Product fixes for #1762; CHANGELOG entry; reopening #1740; clearing all MAJOR smells.

## Decisions

1. **Live analysis first** — `bash infra/scripts/run-sonar.sh` (backend Maven + frontend scanner) then `sonar-legacy-inventory.py --write`.
2. **Heuristics for known clusters** — CI ephemeral Postgres secrets → `accepted_risk`; docker-internal OTLP HTTP → `accepted_risk`; legacy MD5 verify → `fix` + child #1762.
3. **Provenance line** — inventory header records `Last live refresh` + row count.
4. **Docker network auto-detect** — prefer `notaire_devsecops-network` when present (compose project name on this Mac).
5. **CHANGELOG-free** — ops/quality inventory refresh; peer hex CHANGELOG traffic preferred (#1730 guidance).

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Stale SONAR_TOKEN → 401 | `run-sonar.sh` regenerates when empty; operators may clear `.env` token |
| Wrong Docker network → frontend scan skip | Auto-detect existing compose network |
| 700+ high-severity smell rows | Bulk `accepted_risk` / `defer_hexagon` triage; focus AC on VULNERABILITY |

## Testing Strategy

Failing unit tests for vuln heuristics + refresh provenance first; then implement. No Playwright.

## Regression Strategy

Existing inventory fixture tests stay green. `sonar-severity-report.py` unchanged.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Docs + scripts only; no runtime deploy.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
