# Rewrite refactoring.md for the current stack

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1070 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure (engineering standards / documentation) |
| Branch | `cursor/docs-1070-refactoring-rules-f458` |
| Gate 1 status | passed |

## Objetivo

`.claude/rules/refactoring.md` is always-loaded into every agent session but still
describes the 2025 migration target (standalone Swing client, Spring Boot 3.x /
Java 17, PostgreSQL 15, `com.notaria.*`, `EntityRequestDTO`). Agents that follow
it contradict [CONSTITUTION.md](../../../CONSTITUTION.md) and `CLAUDE.md`
(Next.js 16 + Spring Boot 4.1 / Java 21 / PostgreSQL 16, `com.licensis.notaire`,
`Dto*` naming). This change rewrites the rule to the current architecture and
adds a mechanical guard so the obsolete target cannot reappear unnoticed.

## What Changes

- Rewrite `.claude/rules/refactoring.md` for the current three-tier stack:
  PostgreSQL 16 + Spring Boot 4.1 (`com.licensis.notaire`) + Next.js frontend;
  no Swing as a development target; `Dto*` naming; prefer `repository` over
  legacy `jpa`.
- Extend `scripts/check-agent-rules.sh` so CI/preflight fails if
  `refactoring.md` again cites obsolete markers (`com.notaria`, Spring Boot 3,
  SwingWorker / Swing-as-target, `EntityRequestDTO`, PostgreSQL 15 as target).
- Align migration-status pointers: keep archived `MIGRATION-BACKLOG` /
  `MIGRATION-DASHBOARD` as history; fix `DEVELOPMENT-PLAN.md` wording that still
  names Swing as a target client; link README / docs entry points to the single
  live status sources (`docs/github/README.md` Delivery Board + milestones, and
  `docs/300-development/DEVELOPMENT-PLAN.md` phase table); refresh the SAD phase
  table where it still treats Swing retirement as future work.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Always-loaded agent rules must describe the current architecture, not a retired migration target | CU76 / CONSTITUTION.md §8, §10 | Made explicit |
| No current (non-archive) doc describes a Swing client as a development target | #1070 AC / CONSTITUTION.md §8 | Made explicit |
| Migration progress has one live status source; superseded dashboards stay archived | #1070 AC / CONSTITUTION.md §8 | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: extend the agent-rule validity requirement so
  `refactoring.md` cannot reintroduce obsolete migration-era targets.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | already removed; only prose that treated it as a target is corrected |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | existing `sdlc-process.yml` already runs `check-agent-rules.sh`; script gains new assertions |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Documentation/governance alignment only. Follows existing architecture
(repository over `jpa`, Next.js client, Flyway). No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `.claude/rules/refactoring.md` | Rewrite for current stack; remove Swing-as-target guidance |
| `scripts/check-agent-rules.sh` | Guard against obsolete markers in `refactoring.md` |
| `docs/300-development/DEVELOPMENT-PLAN.md` | Stop naming Swing as a target client; point at live status |
| `docs/200-architecture/201-SAD/sad.md` | Phase table: Swing retirement / Phase 6 status aligned with removal |
| `README.md` | Link to single live status sources for migration/delivery |
| `docs/000-archive/github/` | Already holds MIGRATION-BACKLOG/DASHBOARD — confirm pointer, no re-publish |
| `CHANGELOG.md` | n/a — not user-visible (agent rules / engineering docs) |

## Out of Scope

- Product feature code, package renames (`negocio`→`business` in CLAUDE.md/AGENTS.md), or Swing binary removal (already done; tracked historically under #585 / #811 / #899).
- Full rewrite of every ADR that mentions Swing historically (ADRs remain historical decisions).
- `local-ai/` harness changes.
