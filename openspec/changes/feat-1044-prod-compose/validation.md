---
cursor:
  subagentId: "bc-d5786561-8eb2-5bb0-b579-deb2851750a7"
---

# Validation — feat-1044 OpenSpec draft

| Check | Result |
|-------|--------|
| Pattern reference | `internal/openspec-1047/` + `openspec/schemas/notaire-sdlc` |
| Schema | `notaire-sdlc` |
| Live Issue | #1044 OPEN (`gh issue view 1044`) |
| Use Cases | CU78 – Security, Privacy and Compliance; CU75 – Database Management and Migrations |
| Compose evidence | Root `docker-compose.yml` publishes 5432/8080/3000/5050; `ENVIRONMENT: development`; `${VAR:-admin}`; pgAdmin `SERVER_MODE=False`; `SPRING_FLYWAY_BASELINE_ON_MIGRATE: true`; no `docker-compose.prod.yml` |
| Guard note | `ProductionCredentialsGuard` currently also requires non-`admin` pgAdmin/Grafana/exporter passwords — prod least-privilege needs guard alignment |
| Scenarios | 8 × `#### Scenario:` |
| Mandatory task groups | 12 present |
| Serialize | after **#1047** merges (queue: #1057 PR #1150 → #1048 → #1047 → #1044) |
| Repo branch / PR / push | **none** (by design) |
| `bash scripts/validate-sdlc-plan.sh feat-1044-prod-compose` | **PASS** (temp copy into `openspec/changes/`, then removed; no repo leave-behind) |

As-of: 2026-10-03.
