# Repository Metrics Baseline

> Generated: `2026-10-10T12:17:08Z` by `workspace/ci/repo-metrics.py` (#1417 / #1256 / #1197 P0.1, CU76).
>
> Regenerate: `python3 workspace/ci/repo-metrics.py --markdown docs/300-development/REPO-METRICS-BASELINE.md`

## Summary

| Metric | Value |
|--------|------:|
| Modules (`workspace/modules.yaml`) | 9 |
| Workflows total | 17 |
| Workflows with `paths:` filter (heuristic) | 2 |
| Always-loaded bytes | 105026 |
| Always-loaded tokens (est.) | 26256 |
| `deprecated/` bytes | 13406232 |
| `deprecated/` files | 767 |
| `docs/` bytes | 19876311 |
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
| `AGENTS.md` | 16589 |
| `CONSTITUTION.md` | 34245 |
| `.claude/rules/general.md` | 2709 |
| `.claude/rules/programming.md` | 8470 |
| `.claude/rules/code-quality.md` | 6263 |
| `.claude/rules/refactoring.md` | 7515 |
| `.claude/rules/ai-agent-workflow.md` | 3979 |
| `.claude/rules/ui-ux-design.md` | 16432 |
| `.claude/rules/database-migrations.md` | 3986 |
| `.claude/rules/hooks.md` | 4738 |

## Notes

- Token estimate is bytes/4 (rough); measure with the agent loader for exact counts.
- Workflow path-filter count is heuristic over YAML text; re-check when editing workflows.
- Script is offline-only; PR cross-area rates require gh and are recorded in ADR-024.

Related: [MODULE-OWNERSHIP.md](MODULE-OWNERSHIP.md), [REPO-SPLIT-PLAN.md](REPO-SPLIT-PLAN.md), ADR-024, #1197.
