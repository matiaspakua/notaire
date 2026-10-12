# Schedule milestones, labels, and agent lanes

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1561 |
| Use Case | RNF-21, RNF-22 |
| Branch | `cursor/project-management-roadmap-4e9a` |
| Gate 1 status | passed |

## Objetivo

A project manager cannot see how Notaire was organized or what is scheduled next:
SAD §11.3 points at milestones that do not exist, open work is bucketed only by
priority, and nothing tells two Cloud agents which paths they may edit together.

## What Changes

- Add a scheduling policy (`workspace/sdlc/milestone_policy.py`) and an idempotent
  GitHub apply/check script.
- Document history, six forward trains, priority, labels, and parallel lanes in
  `docs/github/PROJECT-MANAGEMENT.md`.
- Render the scope and progress chart on the GitHub Pages technical docs tab.
- Point the SAD, the fleet, and the foreman at that schedule.
- Correct the broken milestone numbers in SAD §11.3.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Delivery work is scheduled on a dated train, not by copying the priority label into the milestone | RNF-21, RNF-22, #1561 | New |
| Requirements and use cases are placed on the train that satisfies them and are not pickable work | RNF-21, #902, #898 | Changed |
| At most one in-progress issue occupies a lane; cross blocks frontend and backend | RNF-22, fleet architecture | New |

## Capabilities

### New Capabilities

- `delivery-schedule`: how open and closed issues are placed on milestones, and which lanes an agent may pick

### Modified Capabilities

- None

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |
| `docs` | yes | Handbook, SAD §11.3, fleet pointer |
| `github-page` | yes | `/docs/management/` chart |
| `workspace` | yes | Policy module, unit tests, apply script |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway): none
- Configuration / `.env`: none
- Dependencies: none
- Architecture: no ADR. The schedule is process documentation. SAD §11.3 is corrected, not redesigned.

### Architecture review

Follows the existing docs and fleet layout. No new runtime component.

## Documentation Impact

| Document | Change |
|----------|--------|
| `docs/github/PROJECT-MANAGEMENT.md` | New handbook |
| `docs/github/README.md` | Points at the trains |
| `docs/github/milestone-snapshot.json` | Counts for the chart |
| `docs/200-architecture/201-SAD/sad.md` | §11.3 milestone alignment |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Pick rule |
| `.claude/agents/cloud-foreman.md` | Lane rule |
| `CHANGELOG.md` | Unreleased documentation note |

## Out of Scope

Closing duplicate catalog issues (two CU01 cards). Implementing product pages.
Editing GitHub from this agent: the token received HTTP 403 on milestone, label,
and project mutations. The apply script is the way a token with Issues and
Projects write performs that edit.
