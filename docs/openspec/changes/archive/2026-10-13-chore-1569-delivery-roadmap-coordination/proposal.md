# Coordinate the delivery roadmap and agent board

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1569 |
| Use Case | RNF-21 and RNF-22 — Methodology and documentation |
| Branch | `chore/1569_delivery_roadmap_coordination` |
| Gate 1 status | passed — strict OpenSpec + Constitution plan validation |

## Objetivo

Make GitHub milestones and Project #1 the shared planning surface from the
project's beginning through its future delivery trains. Remove contradictions
between the live board and permanent documentation so humans and any agent
fleet can identify history, current ownership, and the next claimable work.

## What Changes

- Keep every open and closed issue assigned to exactly one milestone.
- Extend roadmap dates to history cohorts and delivery trains.
- Configure a full-lifecycle roadmap, a claimable-work Kanban, and a compact
  agent queue without duplicating scheduling policy.
- Preserve Todo / In Progress / Done as the only Status values; derive agent
  lanes from existing area/type labels rather than creating `lane:*` labels.
- Publish the live milestone model, board-view contract, and agent claim
  protocol in the permanent project-management and AI SDLC documentation.
- Add executable policy checks so future drift is visible before merge.

## Reglas de negocio

<!-- The business rules this change introduces, alters, or makes explicit. State
     each rule in one sentence. The normative, testable form of each rule belongs
     in the delta spec as a Requirement + Scenarios; here, name them and cite the
     Use Case or business document they come from.
     If the rule is new and no Use Case covers it, say so - a missing Use Case
     must be created before Gate 1 passes. -->

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every issue has exactly one milestone; migrated history uses closed cohorts and newly completed work keeps the delivery train in which it shipped. | RNF-21, RNF-22 | Made explicit |
| The full-lifecycle roadmap spans history and future trains and is grouped by milestone. | RNF-21, RNF-22 | Changed |
| The Kanban contains only open, non-catalog delivery work and is grouped by Status. | RNF-21 | Changed |
| Agents claim only open, non-catalog, non-blocked work in a free derived lane, then synchronize Issue state, Status, and WIP labels. | RNF-21; #1550 / CU76 | Made explicit |
| Milestones are delivery trains or history cohorts, never SemVer releases. | RNF-21, RNF-22 | Made explicit |

## Capabilities

### New Capabilities
- `delivery-roadmap-coordination`: milestone coverage, board views, and the
  deterministic claim protocol shared by humans and agent fleets.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

<!-- Mark every module this change touches. Delete rows that do not apply, but do
     not delete the table: an empty table means the analysis was not done. -->

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | No product behavior changes |
| `frontend` | no | No application UI changes |
| `frontend-swing` | no | Retired |
| `infra` / observability | no | No runtime infrastructure changes |
| CI/CD (`.github/workflows`) | no | Existing process checks are reused |
| `docs` | yes | Project-management and AI SDLC operating model |
| `workspace` | yes | Idempotent GitHub policy, release-train mapping, and tests |
| `github-page` | yes | Generated milestone snapshot consumed by public docs |

### Surface area

<!-- Entities, endpoints, database schema (Flyway migration needed?), configuration,
     external dependencies. Name the endpoints and the migration version explicitly.
     State whether any change is BREAKING for API clients. -->

- Entities: GitHub Issues, milestones, Project v2 items/views/fields
- Endpoints: GitHub REST and GraphQL APIs only; no Notaire API change
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none; local authenticated `gh` session is required
- Dependencies: existing Python standard library and `gh` CLI

### Architecture review

This is process configuration, not product architecture. It centralizes policy
in `workspace/sdlc/milestone_policy.py`, keeps permanent guidance under
`docs/300-development/305-project-management/`, and links fleet documents to
that source. No ADR is required.

## Documentation Impact

<!-- Which PERMANENT documentation must be updated before merge (Gate 3).
     Permanent docs are the single source of truth; this proposal is not.
     Do not duplicate their content here - name the file and what changes in it.
     Mark documents to be archived into docs/000-archive/. -->

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/305-project-management/README.md` | Canonical model and navigation |
| `docs/300-development/305-project-management/MILESTONES.md` | Live history and delivery trains |
| `docs/300-development/305-project-management/ROADMAP.md` | View contract and verification commands |
| `docs/300-development/305-project-management/LABELS.md` | Derived-lane and workflow-label policy |
| `docs/300-development/305-project-management/PARALLEL-AGENTS.md` | Fleet claim and conflict protocol |
| `docs/300-development/305-project-management/RELEASES.md` | Current train names in release mapping |
| `docs/300-development/SBOM.md` | Current release gate train example |
| `docs/200-architecture/201-SAD/sad.md` | Migration phases point to current trains/history |
| `docs/github/README.md` | Concise GitHub board contract |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Link cloud agents to the shared coordination model |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Foreman selection and synchronization rules |
| `AGENTS.md` | Discoverable pointer for milestone/board work |
| `CONSTITUTION.md` | Release-rule example uses the current dated train |
| `.claude/agents/` and `.claude/skills/project-management/` | Agent entry points use the shared protocol |
| `CHANGELOG.md` | Record the project-management coordination improvement |

## Out of Scope

- No product feature, API, schema, or application UI change.
- No issue is closed or reprioritized as a product decision.
- No `lane:*` labels or additional Status columns are created.
- GitHub Project grouping that is not exposed by the public API remains a
  documented, verified UI setting rather than custom automation.
