# Package Owner decision for deprecated/ and history purge (#1261)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1261 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-1261-owner-decision-pack-cf98` |
| Gate 1 status | passed |

## Objetivo

Phase 0.6 of `REPO-SPLIT-PLAN.md` (#1197) needs an Owner choice on `deprecated/`
(~13.4 MB, ~767 files) and the history rewrite deferred by ADR-022. Package the
decision request in ADR-022 and GitHub Pages so the Owner can pick A/B/C without
agents inventing a deletion or rewrite.

## What Changes

- Amends `ADR-022` with a **Pending Owner decision (#1261)** section (options A/B/C; no choice recorded).
- Surfaces ADR-022 and #1261 on the GitHub Pages Architecture docs page.
- Points `REPO-SPLIT-PLAN.md` P0.6 acceptance at the ADR section.
- Adds a guard test that ADR-022 references #1261 and forbids a premature "Accepted" choice without Owner language.
- Does **not** delete `deprecated/`, rewrite history, or pick an option.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Agents must not remove `deprecated/` or rewrite git history until Owner records a choice in ADR-022 | #1261, ADR-022, #1197 P0.6 | Made explicit |

## Capabilities

### New Capabilities

- `deprecated-owner-decision`: ADR-022 and Pages document the Owner decision request for `deprecated/` and history purge until a choice is recorded.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `docs` | yes | ADR-022, REPO-SPLIT-PLAN, OpenSpec |
| `github-page` | yes | Architecture page links |
| `workspace` | yes | guard unit test |
| `backend-api` / `frontend` | no | — |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Documentation + Pages only. Amends existing ADR-022; does not change Accepted decisions already in force.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-022-git-history-rewrite-and-large-binaries.md` | Pending Owner decision section |
| `docs/300-development/REPO-SPLIT-PLAN.md` | P0.6 row points at ADR-022 section |
| `github-page/app/docs/architecture/page.tsx` | Link ADR-022 + #1261 |
| `CHANGELOG.md` | Unreleased note |
