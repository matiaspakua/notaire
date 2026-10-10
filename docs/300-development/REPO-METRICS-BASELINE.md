# Repository Metrics Baseline

> Generated: `2026-10-10T15:01:46Z` by `workspace/ci/repo-metrics.py` (#1417 / #1256 / #1197 P0.1, CU76).
>
> Regenerate: `python3 workspace/ci/repo-metrics.py --markdown docs/300-development/REPO-METRICS-BASELINE.md`

## Summary

| Metric | Value |
|--------|------:|
| Modules (`workspace/modules.yaml`) | 9 |
| Workflows total | 17 |
| Workflows with `paths:` filter (heuristic) | 2 |
| Always-loaded bytes | 8139 |
| Always-loaded tokens (est.) | 2034 |
| `deprecated/` bytes | 13406232 |
| `deprecated/` files | 767 |
| `docs/` bytes | 19940517 |
| PlantUML `.puml` under `204-diagrams/` | 116 |

## Modules

| Module | Path | Fleet | Depends on |
|--------|------|-------|------------|
| `backend-api` | `backend-api` | backend | — |
| `frontend` | `frontend` | frontend | backend-api |
| `infra` | `infra` | infrastructure | backend-api |
| `testing` | `testing` | qa | backend-api, frontend |
| `local-ai` | `local-ai` | ai | — |
| `docs` | `docs` | knowledge | — |
| `security` | `security` | security | — |
| `contracts` | `contracts` | foreman | — |
| `workspace` | `workspace` | foreman | backend-api, frontend, infra, testing, local-ai, docs, contracts, security |

## Always-loaded agent files

| Path | Bytes |
|------|------:|
| `CLAUDE.md` | 100 |
| `AGENTS.md` | 2447 |
| `CONSTITUTION-AGENT-CARD.md` | 1613 |
| `.claude/rules/ai-agent-workflow.md` | 3979 |

## Notes

- Token estimate is bytes/4 (rough); measure with the agent loader for exact counts.
- Workflow path-filter count is heuristic over YAML text; re-check when editing workflows.
- Script is offline-only; PR cross-area rates require gh and are recorded in ADR-024.

Related: [MODULE-OWNERSHIP.md](MODULE-OWNERSHIP.md), [REPO-SPLIT-PLAN.md](REPO-SPLIT-PLAN.md), ADR-024, #1197.
